package com.cloudpulse.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "health_checks")
@CompoundIndex(name = "service_timestamp_idx", def = "{'serviceId': 1, 'timestamp': -1}")
public class HealthCheck {

    @Id
    private String id;

    @Indexed
    private String serviceId;

    private String serviceName;

    private String url;

    @Indexed
    private Instant timestamp = Instant.now();

    private ServiceStatus status;

    private Integer statusCode;

    private Long responseTimeMs;

    private String errorMessage;

    private boolean isSuccess;

    public HealthCheck() {
    }

    public HealthCheck(String serviceId, String serviceName, String url, ServiceStatus status,
                       Integer statusCode, Long responseTimeMs, String errorMessage, boolean isSuccess) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.url = url;
        this.status = status;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.errorMessage = errorMessage;
        this.isSuccess = isSuccess;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
