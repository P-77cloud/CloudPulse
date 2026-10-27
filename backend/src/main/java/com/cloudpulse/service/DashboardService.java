package com.cloudpulse.service;

import com.cloudpulse.dto.DashboardSummaryDto;
import com.cloudpulse.dto.MetricsTimeseriesDto;
import com.cloudpulse.model.HealthCheck;
import com.cloudpulse.model.IncidentStatus;
import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.model.ServiceStatus;
import com.cloudpulse.repository.HealthCheckRepository;
import com.cloudpulse.repository.IncidentRepository;
import com.cloudpulse.repository.MonitoredServiceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final MonitoredServiceRepository serviceRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final IncidentRepository incidentRepository;

    public DashboardService(MonitoredServiceRepository serviceRepository,
                            HealthCheckRepository healthCheckRepository,
                            IncidentRepository incidentRepository) {
        this.serviceRepository = serviceRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.incidentRepository = incidentRepository;
    }

    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        List<MonitoredService> services = serviceRepository.findAll();
        summary.setTotalServices(services.size());

        long up = 0, down = 0, degraded = 0, unknown = 0;
        double uptimeSum = 0;
        int activeServicesCount = 0;

        for (MonitoredService s : services) {
            if (s.getStatus() == ServiceStatus.UP) up++;
            else if (s.getStatus() == ServiceStatus.DOWN) down++;
            else if (s.getStatus() == ServiceStatus.DEGRADED) degraded++;
            else unknown++;

            if (s.isEnabled() && s.getUptimePercentage() != null) {
                uptimeSum += s.getUptimePercentage();
                activeServicesCount++;
            }
        }

        summary.setUpServices(up);
        summary.setDownServices(down);
        summary.setDegradedServices(degraded);
        summary.setUnknownServices(unknown);

        // Overall Uptime
        summary.setOverallUptimePercentage(
                activeServicesCount > 0 ? Math.round((uptimeSum / activeServicesCount) * 100.0) / 100.0 : 100.0
        );

        // Active incidents count
        long activeIncidents = incidentRepository.countByStatus(IncidentStatus.OPEN) +
                incidentRepository.countByStatus(IncidentStatus.ACKNOWLEDGED);
        summary.setOpenIncidents(activeIncidents);

        // Total health checks
        summary.setTotalHealthChecksRecorded(healthCheckRepository.count());

        // Calculate average response time over recent 24h
        Instant past24h = Instant.now().minus(24, ChronoUnit.HOURS);
        List<HealthCheck> recentChecks = healthCheckRepository.findByTimestampAfterOrderByTimestampAsc(past24h);

        double avgLatency = recentChecks.stream()
                .filter(HealthCheck::isSuccess)
                .mapToLong(HealthCheck::getResponseTimeMs)
                .average()
                .orElse(0.0);
        summary.setAverageResponseTimeMs(Math.round(avgLatency * 10.0) / 10.0);

        // Latency history for charts (last 30 checks)
        int listSize = recentChecks.size();
        List<HealthCheck> chartChecks = listSize > 30 ? recentChecks.subList(listSize - 30, listSize) : recentChecks;

        List<MetricsTimeseriesDto> timeseries = chartChecks.stream()
                .map(c -> new MetricsTimeseriesDto(c.getTimestamp(), c.getResponseTimeMs(), c.getServiceName(), c.isSuccess()))
                .collect(Collectors.toList());
        summary.setLatencyHistory(timeseries);

        return summary;
    }
}
