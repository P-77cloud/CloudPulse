package com.cloudpulse.service;

import com.cloudpulse.model.HealthCheck;
import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.model.ServiceStatus;
import com.cloudpulse.security.UrlSecurityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Service
public class HealthCheckExecutorService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckExecutorService.class);

    private final UrlSecurityValidator urlSecurityValidator;
    private final HttpClient httpClient;

    @Value("${cloudpulse.health-check.user-agent:CloudPulse-Monitor/1.0}")
    private String userAgent;

    public HealthCheckExecutorService(UrlSecurityValidator urlSecurityValidator) {
        this.urlSecurityValidator = urlSecurityValidator;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Executes a real HTTP/HTTPS health check against the monitored service.
     * Enforces SSRF security validation before executing.
     */
    public HealthCheck executeCheck(MonitoredService service) {
        String url = service.getUrl();
        long startTime = System.currentTimeMillis();
        int expectedCode = service.getExpectedStatusCode() > 0 ? service.getExpectedStatusCode() : 200;
        int timeoutMs = service.getTimeoutMs() > 0 ? service.getTimeoutMs() : 5000;

        // 1. SSRF Security Validation
        try {
            urlSecurityValidator.validateUrl(url);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("Health check blocked by security policy for service '{}' ({}): {}",
                    service.getName(), url, e.getMessage());
            return new HealthCheck(
                    service.getId(),
                    service.getName(),
                    url,
                    ServiceStatus.DOWN,
                    0,
                    duration,
                    "Security validation blocked check: " + e.getMessage(),
                    false
            );
        }

        // 2. Perform real HTTP Check
        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", userAgent);

            String method = service.getHttpMethod() != null ? service.getHttpMethod().toUpperCase() : "GET";
            if ("HEAD".equals(method)) {
                reqBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
            } else {
                reqBuilder.GET();
            }

            HttpResponse<Void> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.discarding());
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = response.statusCode();

            boolean isSuccess = (statusCode == expectedCode);
            ServiceStatus status;

            if (isSuccess) {
                // If response time is very high (> 2500ms), flag as DEGRADED
                status = duration > 2500 ? ServiceStatus.DEGRADED : ServiceStatus.UP;
            } else {
                status = ServiceStatus.DOWN;
            }

            return new HealthCheck(
                    service.getId(),
                    service.getName(),
                    url,
                    status,
                    statusCode,
                    duration,
                    isSuccess ? null : "Expected HTTP " + expectedCode + ", but received HTTP " + statusCode,
                    isSuccess
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long duration = System.currentTimeMillis() - startTime;
            return new HealthCheck(
                    service.getId(),
                    service.getName(),
                    url,
                    ServiceStatus.DOWN,
                    0,
                    duration,
                    "Health check interrupted: " + e.getMessage(),
                    false
            );
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("Health check I/O error for service '{}' ({}): {}", service.getName(), url, e.getMessage());
            return new HealthCheck(
                    service.getId(),
                    service.getName(),
                    url,
                    ServiceStatus.DOWN,
                    0,
                    duration,
                    "Connection failed: " + e.getMessage(),
                    false
            );
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Unexpected error during health check for service '{}': {}", service.getName(), e.getMessage());
            return new HealthCheck(
                    service.getId(),
                    service.getName(),
                    url,
                    ServiceStatus.DOWN,
                    0,
                    duration,
                    "Check execution error: " + e.getMessage(),
                    false
            );
        }
    }
}
