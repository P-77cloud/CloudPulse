package com.cloudpulse.service;

import com.cloudpulse.exception.ResourceNotFoundException;
import com.cloudpulse.model.HealthCheck;
import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.model.ServiceStatus;
import com.cloudpulse.repository.HealthCheckRepository;
import com.cloudpulse.repository.MonitoredServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class HealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final MonitoredServiceRepository serviceRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final HealthCheckExecutorService executorService;
    private final IncidentService incidentService;
    private final AlertService alertService;

    @Value("${cloudpulse.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public HealthCheckScheduler(MonitoredServiceRepository serviceRepository,
                                HealthCheckRepository healthCheckRepository,
                                HealthCheckExecutorService executorService,
                                IncidentService incidentService,
                                AlertService alertService) {
        this.serviceRepository = serviceRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.executorService = executorService;
        this.incidentService = incidentService;
        this.alertService = alertService;
    }

    /**
     * Periodic scheduler polling all enabled services that are due for a health check.
     */
    @Scheduled(fixedRateString = "${cloudpulse.scheduler.check-interval-ms:15000}")
    public void runScheduledChecks() {
        if (!schedulerEnabled) {
            return;
        }

        List<MonitoredService> services = serviceRepository.findAllByEnabledTrue();
        Instant now = Instant.now();

        for (MonitoredService service : services) {
            // Check if service has authorizedEndpoint
            if (!service.isAuthorizedEndpoint()) {
                log.warn("Skipping check for service '{}': endpoint not marked as authorized.", service.getName());
                continue;
            }

            // Determine if check is due based on service's checkIntervalSeconds
            if (service.getLastCheckTime() != null) {
                long elapsedSeconds = java.time.Duration.between(service.getLastCheckTime(), now).getSeconds();
                if (elapsedSeconds < service.getCheckIntervalSeconds()) {
                    continue;
                }
            }

            performCheck(service);
        }
    }

    /**
     * Performs an on-demand check for a specific service.
     */
    public HealthCheck performOnDemandCheck(String serviceId) {
        MonitoredService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));
        return performCheck(service);
    }

    /**
     * Executes the check, records metric, updates service status, and evaluates alerts/incidents.
     */
    public synchronized HealthCheck performCheck(MonitoredService service) {
        HealthCheck check = executorService.executeCheck(service);
        HealthCheck savedCheck = healthCheckRepository.save(check);

        // Update service state
        service.setLastCheckTime(savedCheck.getTimestamp());
        service.setLastResponseTimeMs(savedCheck.getResponseTimeMs());
        service.setStatus(savedCheck.getStatus());

        if (savedCheck.isSuccess()) {
            service.setConsecutiveFailures(0);
            serviceRepository.save(service);
            incidentService.processServiceRecovery(service);
        } else {
            service.setConsecutiveFailures(service.getConsecutiveFailures() + 1);
            serviceRepository.save(service);
            incidentService.processServiceFailure(service, savedCheck);
        }

        // Evaluate alert rules
        alertService.evaluateRules(service, savedCheck);

        log.debug("Health check finished for '{}': status={}, latency={}ms",
                service.getName(), savedCheck.getStatus(), savedCheck.getResponseTimeMs());

        return savedCheck;
    }
}
