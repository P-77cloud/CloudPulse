package com.cloudpulse.service;

import com.cloudpulse.model.HealthCheck;
import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.model.ServiceStatus;
import com.cloudpulse.security.UrlSecurityValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Health Check Executor Service - Unit Tests")
class HealthCheckExecutorServiceTest {

    @Mock
    private UrlSecurityValidator urlSecurityValidator;

    @InjectMocks
    private HealthCheckExecutorService executorService;

    @Test
    @DisplayName("Should block execution and return DOWN status if security validation fails")
    void testSecurityValidationBlocksCheck() {
        doThrow(new com.cloudpulse.exception.SecurityValidationException("Blocked private IP"))
                .when(urlSecurityValidator).validateUrl(anyString());

        MonitoredService service = new MonitoredService("Internal Router", "http://192.168.1.1", true);
        service.setId("svc-bad");

        HealthCheck check = executorService.executeCheck(service);

        assertNotNull(check);
        assertEquals(ServiceStatus.DOWN, check.getStatus());
        assertFalse(check.isSuccess());
        assertTrue(check.getErrorMessage().contains("Security validation blocked check"));
    }
}
