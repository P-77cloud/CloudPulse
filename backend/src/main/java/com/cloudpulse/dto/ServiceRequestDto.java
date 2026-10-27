package com.cloudpulse.dto;

import jakarta.validation.constraints.*;

public class ServiceRequestDto {

    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Service URL is required")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL must start with http:// or https://")
    private String url;

    private String httpMethod = "GET";

    @Min(value = 100, message = "Expected status code must be a valid HTTP status (>= 100)")
    @Max(value = 599, message = "Expected status code must be a valid HTTP status (<= 599)")
    private int expectedStatusCode = 200;

    @Min(value = 5, message = "Check interval must be at least 5 seconds")
    @Max(value = 3600, message = "Check interval cannot exceed 3600 seconds")
    private int checkIntervalSeconds = 30;

    @Min(value = 500, message = "Timeout must be at least 500ms")
    @Max(value = 30000, message = "Timeout cannot exceed 30000ms")
    private int timeoutMs = 5000;

    @AssertTrue(message = "You must explicitly authorize monitoring for this endpoint")
    private boolean authorizedEndpoint = false;

    @Min(value = 1, message = "Failure threshold must be at least 1")
    @Max(value = 10, message = "Failure threshold cannot exceed 10")
    private int failureThreshold = 3;

    private boolean enabled = true;

    public ServiceRequestDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public int getCheckIntervalSeconds() {
        return checkIntervalSeconds;
    }

    public void setCheckIntervalSeconds(int checkIntervalSeconds) {
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isAuthorizedEndpoint() {
        return authorizedEndpoint;
    }

    public void setAuthorizedEndpoint(boolean authorizedEndpoint) {
        this.authorizedEndpoint = authorizedEndpoint;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
