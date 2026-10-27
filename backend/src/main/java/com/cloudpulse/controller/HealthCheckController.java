package com.cloudpulse.controller;

import com.cloudpulse.dto.ApiResponse;
import com.cloudpulse.dto.HealthCheckResultDto;
import com.cloudpulse.model.HealthCheck;
import com.cloudpulse.repository.HealthCheckRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/health-checks")
public class HealthCheckController {

    private final HealthCheckRepository healthCheckRepository;

    public HealthCheckController(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<Page<HealthCheckResultDto>>> getChecksForService(
            @PathVariable String serviceId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<HealthCheckResultDto> page = healthCheckRepository.findByServiceIdOrderByTimestampDesc(serviceId, pageable)
                .map(HealthCheckResultDto::fromEntity);
        return ResponseEntity.ok(ApiResponse.ok("Health checks retrieved", page));
    }

    @GetMapping("/service/{serviceId}/recent")
    public ResponseEntity<ApiResponse<List<HealthCheckResultDto>>> getRecentChecksForService(
            @PathVariable String serviceId) {
        List<HealthCheckResultDto> list = healthCheckRepository.findTop50ByServiceIdOrderByTimestampDesc(serviceId)
                .stream()
                .map(HealthCheckResultDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Recent health checks retrieved", list));
    }
}
