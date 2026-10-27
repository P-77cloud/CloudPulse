package com.cloudpulse.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "incidents")
public class Incident {

    @Id
    private String id;

    @Indexed
    private String serviceId;

    private String serviceName;

    private String title;

    private String description;

    @Indexed
    private IncidentStatus status = IncidentStatus.OPEN;

    private IncidentSeverity severity = IncidentSeverity.CRITICAL;

    @Indexed
    private Instant startedAt = Instant.now();

    private Instant acknowledgedAt;

    private Instant resolvedAt;

    private String acknowledgedBy;

    private String resolvedBy;

    private String rootCauseNotes;

    private int consecutiveFailures;

    public Incident() {
    }

    public Incident(String serviceId, String serviceName, String title, String description,
                    IncidentSeverity severity, int consecutiveFailures) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.consecutiveFailures = consecutiveFailures;
        this.status = IncidentStatus.OPEN;
        this.startedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getRootCauseNotes() {
        return rootCauseNotes;
    }

    public void setRootCauseNotes(String rootCauseNotes) {
        this.rootCauseNotes = rootCauseNotes;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }
}
