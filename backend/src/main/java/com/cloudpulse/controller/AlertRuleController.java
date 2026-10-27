package com.cloudpulse.controller;

import com.cloudpulse.dto.AlertRuleDto;
import com.cloudpulse.dto.ApiResponse;
import com.cloudpulse.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alert-rules")
public class AlertRuleController {

    private final AlertService alertService;

    public AlertRuleController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertRuleDto>>> getAllRules() {
        List<AlertRuleDto> rules = alertService.getAllRules();
        return ResponseEntity.ok(ApiResponse.ok("Alert rules retrieved", rules));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlertRuleDto>> createRule(@Valid @RequestBody AlertRuleDto dto) {
        AlertRuleDto created = alertService.createRule(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Alert rule created", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable String id) {
        alertService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.ok("Alert rule deleted", null));
    }
}
