package com.cloudpulse.dto;

import com.cloudpulse.model.HealthCheck;
import com.cloudpulse.model.ServiceStatus;

import java.time.Instant;

public class HealthCheckResultDto {

    private String id;
    private String serviceId;
    private String serviceName;
    private String url;
    private Instant timestamp;
    private ServiceStatus status;
    private Integer statusCode;
    private Long responseTimeMs;
    private String errorMessage;
    private boolean isSuccess;

    public HealthCheckResultDto() {
    }

    public static HealthCheckResultDto fromEntity(HealthCheck check) {
        HealthCheckResultDto dto = new HealthCheckResultDto();
        dto.setId(check.getId());
        dto.setServiceId(check.getServiceId());
        dto.setServiceName(check.getServiceName());
        dto.setUrl(check.getUrl());
        dto.setTimestamp(check.getTimestamp());
        dto.setStatus(check.getStatus());
        dto.setStatusCode(check.getStatusCode());
        dto.setResponseTimeMs(check.getResponseTimeMs());
        dto.setErrorMessage(check.getErrorMessage());
        dto.setSuccess(check.isSuccess());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { isSuccess = success; }
}
