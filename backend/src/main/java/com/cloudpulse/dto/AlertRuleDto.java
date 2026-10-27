package com.cloudpulse.dto;

import com.cloudpulse.model.AlertCondition;
import com.cloudpulse.model.AlertRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class AlertRuleDto {

    private String id;

    @NotBlank(message = "Rule name is required")
    private String name;

    private String serviceId; // null or "ALL"

    @NotNull(message = "Alert condition is required")
    private AlertCondition condition;

    private double threshold;

    private boolean enabled = true;

    private Instant createdAt;

    public AlertRuleDto() {
    }

    public static AlertRuleDto fromEntity(AlertRule rule) {
        AlertRuleDto dto = new AlertRuleDto();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setServiceId(rule.getServiceId());
        dto.setCondition(rule.getCondition());
        dto.setThreshold(rule.getThreshold());
        dto.setEnabled(rule.isEnabled());
        dto.setCreatedAt(rule.getCreatedAt());
        return dto;
    }

    public AlertRule toEntity() {
        AlertRule rule = new AlertRule();
        rule.setId(this.id);
        rule.setName(this.name);
        rule.setServiceId(this.serviceId);
        rule.setCondition(this.condition);
        rule.setThreshold(this.threshold);
        rule.setEnabled(this.enabled);
        if (this.createdAt != null) {
            rule.setCreatedAt(this.createdAt);
        }
        return rule;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public AlertCondition getCondition() { return condition; }
    public void setCondition(AlertCondition condition) { this.condition = condition; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
