package com.cloudpulse.controller;

import com.cloudpulse.dto.ApiResponse;
import com.cloudpulse.service.SeedDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoSeedController {

    private final SeedDataService seedDataService;

    public DemoSeedController(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seedDemoData() {
        seedDataService.seedDemoData();
        return ResponseEntity.ok(ApiResponse.ok("Demo data successfully seeded for evaluation.", "DEMO_SEEDED"));
    }
}
