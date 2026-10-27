package com.cloudpulse.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "alert_rules")
public class AlertRule {

    @Id
    private String id;

    private String name;

    private String serviceId; // null or "ALL" means applies globally

    private AlertCondition condition;

    private double threshold;

    private boolean enabled = true;

    private Instant createdAt = Instant.now();

    public AlertRule() {
    }

    public AlertRule(String name, String serviceId, AlertCondition condition, double threshold) {
        this.name = name;
        this.serviceId = serviceId;
        this.condition = condition;
        this.threshold = threshold;
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public AlertCondition getCondition() {
        return condition;
    }

    public void setCondition(AlertCondition condition) {
        this.condition = condition;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
