package com.cloudpulse.dto;

import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.model.ServiceStatus;

import java.time.Instant;

public class ServiceResponseDto {

    private String id;
    private String name;
    private String url;
    private String httpMethod;
    private int expectedStatusCode;
    private int checkIntervalSeconds;
    private int timeoutMs;
    private ServiceStatus status;
    private int consecutiveFailures;
    private Instant lastCheckTime;
    private Long lastResponseTimeMs;
    private Double uptimePercentage;
    private boolean enabled;
    private boolean authorizedEndpoint;
    private int failureThreshold;
    private Instant createdAt;
    private Instant updatedAt;

    public ServiceResponseDto() {
    }

    public static ServiceResponseDto fromEntity(MonitoredService service) {
        ServiceResponseDto dto = new ServiceResponseDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setUrl(service.getUrl());
        dto.setHttpMethod(service.getHttpMethod());
        dto.setExpectedStatusCode(service.getExpectedStatusCode());
        dto.setCheckIntervalSeconds(service.getCheckIntervalSeconds());
        dto.setTimeoutMs(service.getTimeoutMs());
        dto.setStatus(service.getStatus());
        dto.setConsecutiveFailures(service.getConsecutiveFailures());
        dto.setLastCheckTime(service.getLastCheckTime());
        dto.setLastResponseTimeMs(service.getLastResponseTimeMs());
        dto.setUptimePercentage(service.getUptimePercentage());
        dto.setEnabled(service.isEnabled());
        dto.setAuthorizedEndpoint(service.isAuthorizedEndpoint());
        dto.setFailureThreshold(service.getFailureThreshold());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public int getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(int expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }
    public int getCheckIntervalSeconds() { return checkIntervalSeconds; }
    public void setCheckIntervalSeconds(int checkIntervalSeconds) { this.checkIntervalSeconds = checkIntervalSeconds; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public Instant getLastCheckTime() { return lastCheckTime; }
    public void setLastCheckTime(Instant lastCheckTime) { this.lastCheckTime = lastCheckTime; }
    public Long getLastResponseTimeMs() { return lastResponseTimeMs; }
    public void setLastResponseTimeMs(Long lastResponseTimeMs) { this.lastResponseTimeMs = lastResponseTimeMs; }
    public Double getUptimePercentage() { return uptimePercentage; }
    public void setUptimePercentage(Double uptimePercentage) { this.uptimePercentage = uptimePercentage; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isAuthorizedEndpoint() { return authorizedEndpoint; }
    public void setAuthorizedEndpoint(boolean authorizedEndpoint) { this.authorizedEndpoint = authorizedEndpoint; }
    public int getFailureThreshold() { return failureThreshold; }
    public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
