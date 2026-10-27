package com.cloudpulse.dto;

import com.cloudpulse.model.Incident;
import com.cloudpulse.model.IncidentSeverity;
import com.cloudpulse.model.IncidentStatus;

import java.time.Instant;

public class IncidentDto {

    private String id;
    private String serviceId;
    private String serviceName;
    private String title;
    private String description;
    private IncidentStatus status;
    private IncidentSeverity severity;
    private Instant startedAt;
    private Instant acknowledgedAt;
    private Instant resolvedAt;
    private String acknowledgedBy;
    private String resolvedBy;
    private String rootCauseNotes;
    private int consecutiveFailures;
    private Long durationSeconds;

    public IncidentDto() {
    }

    public static IncidentDto fromEntity(Incident incident) {
        IncidentDto dto = new IncidentDto();
        dto.setId(incident.getId());
        dto.setServiceId(incident.getServiceId());
        dto.setServiceName(incident.getServiceName());
        dto.setTitle(incident.getTitle());
        dto.setDescription(incident.getDescription());
        dto.setStatus(incident.getStatus());
        dto.setSeverity(incident.getSeverity());
        dto.setStartedAt(incident.getStartedAt());
        dto.setAcknowledgedAt(incident.getAcknowledgedAt());
        dto.setResolvedAt(incident.getResolvedAt());
        dto.setAcknowledgedBy(incident.getAcknowledgedBy());
        dto.setResolvedBy(incident.getResolvedBy());
        dto.setRootCauseNotes(incident.getRootCauseNotes());
        dto.setConsecutiveFailures(incident.getConsecutiveFailures());

        if (incident.getStartedAt() != null) {
            Instant end = incident.getResolvedAt() != null ? incident.getResolvedAt() : Instant.now();
            dto.setDurationSeconds(java.time.Duration.between(incident.getStartedAt(), end).getSeconds());
        }
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getRootCauseNotes() { return rootCauseNotes; }
    public void setRootCauseNotes(String rootCauseNotes) { this.rootCauseNotes = rootCauseNotes; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
}
