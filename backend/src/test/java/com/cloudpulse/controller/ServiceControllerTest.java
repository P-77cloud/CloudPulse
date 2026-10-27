package com.cloudpulse.controller;

import com.cloudpulse.dto.ServiceResponseDto;
import com.cloudpulse.model.ServiceStatus;
import com.cloudpulse.service.HealthCheckScheduler;
import com.cloudpulse.service.MonitoredServiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@DisplayName("Service Controller - Web MVC REST API Unit Tests")
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitoredServiceService serviceService;

    @MockBean
    private HealthCheckScheduler healthCheckScheduler;

    @Test
    @DisplayName("GET /api/v1/services should return 200 OK and list of services")
    void testGetAllServices() throws Exception {
        ServiceResponseDto dto = new ServiceResponseDto();
        dto.setId("svc-1");
        dto.setName("Auth API");
        dto.setUrl("https://api.example.com/auth");
        dto.setStatus(ServiceStatus.UP);
        dto.setUptimePercentage(99.9);

        when(serviceService.getAllServices()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("svc-1"))
                .andExpect(jsonPath("$.data[0].name").value("Auth API"))
                .andExpect(jsonPath("$.data[0].status").value("UP"));
    }

    @Test
    @DisplayName("POST /api/v1/services with valid payload should return 201 Created")
    void testCreateServiceSuccess() throws Exception {
        ServiceResponseDto responseDto = new ServiceResponseDto();
        responseDto.setId("svc-created");
        responseDto.setName("Billing API");
        responseDto.setUrl("https://billing.example.com");
        responseDto.setStatus(ServiceStatus.UNKNOWN);

        when(serviceService.createService(any())).thenReturn(responseDto);

        String jsonPayload = """
                {
                    "name": "Billing API",
                    "url": "https://billing.example.com",
                    "httpMethod": "GET",
                    "expectedStatusCode": 200,
                    "checkIntervalSeconds": 30,
                    "timeoutMs": 5000,
                    "authorizedEndpoint": true,
                    "failureThreshold": 3,
                    "enabled": true
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("svc-created"))
                .andExpect(jsonPath("$.data.name").value("Billing API"));
    }

    @Test
    @DisplayName("POST /api/v1/services without authorizedEndpoint=true should return 400 Bad Request")
    void testCreateServiceWithoutAuthorizationFails() throws Exception {
        String invalidPayload = """
                {
                    "name": "Unauthorized Service",
                    "url": "https://unauthorized.example.com",
                    "authorizedEndpoint": false
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
