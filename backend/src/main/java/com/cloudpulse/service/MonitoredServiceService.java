package com.cloudpulse.service;

import com.cloudpulse.dto.ServiceRequestDto;
import com.cloudpulse.dto.ServiceResponseDto;
import com.cloudpulse.exception.ResourceNotFoundException;
import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.model.ServiceStatus;
import com.cloudpulse.repository.AlertRuleRepository;
import com.cloudpulse.repository.HealthCheckRepository;
import com.cloudpulse.repository.IncidentRepository;
import com.cloudpulse.repository.MonitoredServiceRepository;
import com.cloudpulse.repository.NotificationRepository;
import com.cloudpulse.security.UrlSecurityValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonitoredServiceService {

    private final MonitoredServiceRepository serviceRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationRepository notificationRepository;
    private final UrlSecurityValidator urlSecurityValidator;

    public MonitoredServiceService(MonitoredServiceRepository serviceRepository,
                                   HealthCheckRepository healthCheckRepository,
                                   IncidentRepository incidentRepository,
                                   AlertRuleRepository alertRuleRepository,
                                   NotificationRepository notificationRepository,
                                   UrlSecurityValidator urlSecurityValidator) {
        this.serviceRepository = serviceRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.incidentRepository = incidentRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.notificationRepository = notificationRepository;
        this.urlSecurityValidator = urlSecurityValidator;
    }

    public List<ServiceResponseDto> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::enrichWithRealUptime)
                .collect(Collectors.toList());
    }

    public ServiceResponseDto getServiceById(String id) {
        MonitoredService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));
        return enrichWithRealUptime(service);
    }

    public ServiceResponseDto createService(ServiceRequestDto dto) {
        // Validate URL against SSRF
        urlSecurityValidator.validateUrl(dto.getUrl());

        MonitoredService service = new MonitoredService();
        service.setName(dto.getName().trim());
        service.setUrl(dto.getUrl().trim());
        service.setHttpMethod(dto.getHttpMethod() != null ? dto.getHttpMethod() : "GET");
        service.setExpectedStatusCode(dto.getExpectedStatusCode());
        service.setCheckIntervalSeconds(dto.getCheckIntervalSeconds());
        service.setTimeoutMs(dto.getTimeoutMs());
        service.setAuthorizedEndpoint(dto.isAuthorizedEndpoint());
        service.setFailureThreshold(dto.getFailureThreshold());
        service.setEnabled(dto.isEnabled());
        service.setStatus(ServiceStatus.UNKNOWN);
        service.setCreatedAt(Instant.now());
        service.setUpdatedAt(Instant.now());

        MonitoredService saved = serviceRepository.save(service);
        return ServiceResponseDto.fromEntity(saved);
    }

    public ServiceResponseDto updateService(String id, ServiceRequestDto dto) {
        MonitoredService existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));

        urlSecurityValidator.validateUrl(dto.getUrl());

        existing.setName(dto.getName().trim());
        existing.setUrl(dto.getUrl().trim());
        existing.setHttpMethod(dto.getHttpMethod() != null ? dto.getHttpMethod() : "GET");
        existing.setExpectedStatusCode(dto.getExpectedStatusCode());
        existing.setCheckIntervalSeconds(dto.getCheckIntervalSeconds());
        existing.setTimeoutMs(dto.getTimeoutMs());
        existing.setAuthorizedEndpoint(dto.isAuthorizedEndpoint());
        existing.setFailureThreshold(dto.getFailureThreshold());
        existing.setEnabled(dto.isEnabled());
        existing.setUpdatedAt(Instant.now());

        MonitoredService saved = serviceRepository.save(existing);
        return enrichWithRealUptime(saved);
    }

    public void deleteService(String id) {
        if (!serviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service not found with ID: " + id);
        }
        // Cascade delete related metrics and history
        healthCheckRepository.deleteByServiceId(id);
        incidentRepository.deleteByServiceId(id);
        alertRuleRepository.deleteByServiceId(id);
        notificationRepository.deleteByServiceId(id);
        serviceRepository.deleteById(id);
    }

    public ServiceResponseDto toggleServiceEnabled(String id) {
        MonitoredService existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));
        existing.setEnabled(!existing.isEnabled());
        existing.setUpdatedAt(Instant.now());
        return enrichWithRealUptime(serviceRepository.save(existing));
    }

    /**
     * Calculates real 24-hour uptime percentage from actual recorded health checks.
     */
    private ServiceResponseDto enrichWithRealUptime(MonitoredService service) {
        Instant past24h = Instant.now().minus(24, ChronoUnit.HOURS);
        long totalChecks = healthCheckRepository.countByServiceIdAndTimestampAfter(service.getId(), past24h);
        long successfulChecks = healthCheckRepository.countByServiceIdAndIsSuccessTrueAndTimestampAfter(service.getId(), past24h);

        if (totalChecks > 0) {
            double uptime = Math.round(((double) successfulChecks / totalChecks) * 10000.0) / 100.0;
            service.setUptimePercentage(uptime);
        } else {
            service.setUptimePercentage(100.0);
        }

        return ServiceResponseDto.fromEntity(service);
    }
}
