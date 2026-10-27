package com.cloudpulse.controller;

import com.cloudpulse.dto.ApiResponse;
import com.cloudpulse.dto.HealthCheckResultDto;
import com.cloudpulse.dto.ServiceRequestDto;
import com.cloudpulse.dto.ServiceResponseDto;
import com.cloudpulse.model.HealthCheck;
import com.cloudpulse.service.HealthCheckScheduler;
import com.cloudpulse.service.MonitoredServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final MonitoredServiceService serviceService;
    private final HealthCheckScheduler healthCheckScheduler;

    public ServiceController(MonitoredServiceService serviceService,
                             HealthCheckScheduler healthCheckScheduler) {
        this.serviceService = serviceService;
        this.healthCheckScheduler = healthCheckScheduler;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponseDto>>> getAllServices() {
        List<ServiceResponseDto> services = serviceService.getAllServices();
        return ResponseEntity.ok(ApiResponse.ok("Services retrieved successfully", services));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> getServiceById(@PathVariable String id) {
        ServiceResponseDto service = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.ok(service));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponseDto>> createService(@Valid @RequestBody ServiceRequestDto dto) {
        ServiceResponseDto created = serviceService.createService(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Service created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> updateService(
            @PathVariable String id,
            @Valid @RequestBody ServiceRequestDto dto) {
        ServiceResponseDto updated = serviceService.updateService(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Service updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable String id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.ok("Service and associated metrics deleted successfully", null));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> toggleService(@PathVariable String id) {
        ServiceResponseDto updated = serviceService.toggleServiceEnabled(id);
        return ResponseEntity.ok(ApiResponse.ok("Service status toggled", updated));
    }

    @PostMapping("/{id}/check")
    public ResponseEntity<ApiResponse<HealthCheckResultDto>> triggerCheckNow(@PathVariable String id) {
        HealthCheck check = healthCheckScheduler.performOnDemandCheck(id);
        return ResponseEntity.ok(ApiResponse.ok("Health check executed", HealthCheckResultDto.fromEntity(check)));
    }
}
