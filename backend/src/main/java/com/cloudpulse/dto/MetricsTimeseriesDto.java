package com.cloudpulse.dto;

import java.time.Instant;

public class MetricsTimeseriesDto {

    private Instant timestamp;
    private Long responseTimeMs;
    private String serviceName;
    private boolean isSuccess;

    public MetricsTimeseriesDto() {
    }

    public MetricsTimeseriesDto(Instant timestamp, Long responseTimeMs, String serviceName, boolean isSuccess) {
        this.timestamp = timestamp;
        this.responseTimeMs = responseTimeMs;
        this.serviceName = serviceName;
        this.isSuccess = isSuccess;
    }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { isSuccess = success; }
}
