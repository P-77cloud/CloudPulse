package com.cloudpulse.service;

import com.cloudpulse.dto.IncidentActionDto;
import com.cloudpulse.dto.IncidentDto;
import com.cloudpulse.exception.BadRequestException;
import com.cloudpulse.exception.ResourceNotFoundException;
import com.cloudpulse.model.*;
import com.cloudpulse.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;
    private final AlertService alertService;

    public IncidentService(IncidentRepository incidentRepository, AlertService alertService) {
        this.incidentRepository = incidentRepository;
        this.alertService = alertService;
    }

    /**
     * Evaluates consecutive health check failures. Creates an incident if threshold exceeded.
     */
    public void processServiceFailure(MonitoredService service, HealthCheck check) {
        int threshold = service.getFailureThreshold() > 0 ? service.getFailureThreshold() : 3;

        if (service.getConsecutiveFailures() >= threshold) {
            // Check if there is already an active (OPEN or ACKNOWLEDGED) incident for this service
            List<IncidentStatus> activeStatuses = List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED);
            Optional<Incident> existingIncident = incidentRepository
                    .findFirstByServiceIdAndStatusInOrderByStartedAtDesc(service.getId(), activeStatuses);

            if (existingIncident.isEmpty()) {
                String title = "Service Outage: " + service.getName() + " is DOWN";
                String description = String.format("Service '%s' has failed %d consecutive health checks. Last error: %s",
                        service.getName(), service.getConsecutiveFailures(), check.getErrorMessage());

                Incident incident = new Incident(
                        service.getId(),
                        service.getName(),
                        title,
                        description,
                        IncidentSeverity.CRITICAL,
                        service.getConsecutiveFailures()
                );

                Incident savedIncident = incidentRepository.save(incident);
                log.warn("Created new incident '{}' for service '{}'", savedIncident.getId(), service.getName());

                // Trigger in-app notification
                alertService.createNotification(
                        service.getId(),
                        service.getName(),
                        savedIncident.getId(),
                        "CRITICAL: Incident Created",
                        title,
                        IncidentSeverity.CRITICAL
                );
            }
        }
    }

    /**
     * Evaluates service recovery. Automatically resolves any open or acknowledged incident.
     */
    public void processServiceRecovery(MonitoredService service) {
        List<IncidentStatus> activeStatuses = List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED);
        List<Incident> activeIncidents = incidentRepository.findByServiceIdAndStatusIn(service.getId(), activeStatuses);

        for (Incident incident : activeIncidents) {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolvedAt(Instant.now());
            incident.setResolvedBy("SYSTEM_AUTORECOVERY");
            incident.setRootCauseNotes("Service recovered and passed subsequent health check.");
            incidentRepository.save(incident);

            log.info("Auto-resolved incident '{}' for service '{}'", incident.getId(), service.getName());

            alertService.createNotification(
                    service.getId(),
                    service.getName(),
                    incident.getId(),
                    "RESOLVED: Service Recovered",
                    "Service '" + service.getName() + "' is back UP and operating normally.",
                    IncidentSeverity.INFO
            );
        }
    }

    public Page<IncidentDto> getAllIncidents(Pageable pageable) {
        return incidentRepository.findByOrderByStartedAtDesc(pageable)
                .map(IncidentDto::fromEntity);
    }

    public IncidentDto getIncidentById(String id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
        return IncidentDto.fromEntity(incident);
    }

    public IncidentDto acknowledgeIncident(String id, IncidentActionDto action) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            throw new BadRequestException("Cannot acknowledge an already resolved incident.");
        }

        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        incident.setAcknowledgedAt(Instant.now());
        incident.setAcknowledgedBy(action.getUser());
        Incident updated = incidentRepository.save(incident);

        alertService.createNotification(
                incident.getServiceId(),
                incident.getServiceName(),
                incident.getId(),
                "ACKNOWLEDGED: Incident " + incident.getId(),
                "Incident acknowledged by " + action.getUser(),
                IncidentSeverity.WARNING
        );

        return IncidentDto.fromEntity(updated);
    }

    public IncidentDto resolveIncident(String id, IncidentActionDto action) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(Instant.now());
        incident.setResolvedBy(action.getUser());
        if (action.getRootCauseNotes() != null && !action.getRootCauseNotes().isBlank()) {
            incident.setRootCauseNotes(action.getRootCauseNotes());
        }
        Incident updated = incidentRepository.save(incident);

        alertService.createNotification(
                incident.getServiceId(),
                incident.getServiceName(),
                incident.getId(),
                "MANUALLY RESOLVED: Incident " + incident.getId(),
                "Resolved by " + action.getUser() + (incident.getRootCauseNotes() != null ? " - " + incident.getRootCauseNotes() : ""),
                IncidentSeverity.INFO
        );

        return IncidentDto.fromEntity(updated);
    }

    public long getActiveIncidentCount() {
        return incidentRepository.countByStatus(IncidentStatus.OPEN) +
                incidentRepository.countByStatus(IncidentStatus.ACKNOWLEDGED);
    }
}
