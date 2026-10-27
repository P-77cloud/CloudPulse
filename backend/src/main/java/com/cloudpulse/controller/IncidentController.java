package com.cloudpulse.controller;

import com.cloudpulse.dto.ApiResponse;
import com.cloudpulse.dto.IncidentActionDto;
import com.cloudpulse.dto.IncidentDto;
import com.cloudpulse.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentDto>>> getIncidents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<IncidentDto> incidents = incidentService.getAllIncidents(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Incidents retrieved successfully", incidents));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentDto>> getIncidentById(@PathVariable String id) {
        IncidentDto incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.ok(incident));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<IncidentDto>> acknowledgeIncident(
            @PathVariable String id,
            @Valid @RequestBody IncidentActionDto actionDto) {
        IncidentDto acknowledged = incidentService.acknowledgeIncident(id, actionDto);
        return ResponseEntity.ok(ApiResponse.ok("Incident acknowledged", acknowledged));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<IncidentDto>> resolveIncident(
            @PathVariable String id,
            @Valid @RequestBody IncidentActionDto actionDto) {
        IncidentDto resolved = incidentService.resolveIncident(id, actionDto);
        return ResponseEntity.ok(ApiResponse.ok("Incident resolved successfully", resolved));
    }
}
