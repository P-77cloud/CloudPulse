package com.cloudpulse.dto;

import com.cloudpulse.model.IncidentSeverity;
import com.cloudpulse.model.Notification;

import java.time.Instant;

public class NotificationDto {

    private String id;
    private String serviceId;
    private String serviceName;
    private String incidentId;
    private String title;
    private String message;
    private IncidentSeverity severity;
    private Instant timestamp;
    private boolean read;

    public NotificationDto() {
    }

    public static NotificationDto fromEntity(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setServiceId(notification.getServiceId());
        dto.setServiceName(notification.getServiceName());
        dto.setIncidentId(notification.getIncidentId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setSeverity(notification.getSeverity());
        dto.setTimestamp(notification.getTimestamp());
        dto.setRead(notification.isRead());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getIncidentId() { return incidentId; }
    public void setIncidentId(String incidentId) { this.incidentId = incidentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
