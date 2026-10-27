package com.cloudpulse.service;

import com.cloudpulse.dto.AlertRuleDto;
import com.cloudpulse.dto.NotificationDto;
import com.cloudpulse.exception.ResourceNotFoundException;
import com.cloudpulse.model.*;
import com.cloudpulse.repository.AlertRuleRepository;
import com.cloudpulse.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRuleRepository alertRuleRepository;
    private final NotificationRepository notificationRepository;

    public AlertService(AlertRuleRepository alertRuleRepository, NotificationRepository notificationRepository) {
        this.alertRuleRepository = alertRuleRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Checks if any custom alert rules are triggered by this health check.
     */
    public void evaluateRules(MonitoredService service, HealthCheck check) {
        List<AlertRule> rules = alertRuleRepository.findByEnabledTrue();

        for (AlertRule rule : rules) {
            // Check if rule applies to this specific service or all services
            if (rule.getServiceId() != null && !rule.getServiceId().isBlank() &&
                    !"ALL".equalsIgnoreCase(rule.getServiceId()) &&
                    !rule.getServiceId().equals(service.getId())) {
                continue;
            }

            switch (rule.getCondition()) {
                case RESPONSE_TIME_EXCEEDS:
                    if (check.getResponseTimeMs() != null && check.getResponseTimeMs() > rule.getThreshold()) {
                        createNotification(
                                service.getId(),
                                service.getName(),
                                null,
                                "High Latency Alert: " + service.getName(),
                                String.format("Response time of %dms exceeded threshold of %.0fms for rule '%s'",
                                        check.getResponseTimeMs(), rule.getThreshold(), rule.getName()),
                                IncidentSeverity.WARNING
                        );
                    }
                    break;

                case CONSECUTIVE_FAILURES:
                    if (service.getConsecutiveFailures() == (int) rule.getThreshold()) {
                        createNotification(
                                service.getId(),
                                service.getName(),
                                null,
                                "Consecutive Failures Alert: " + service.getName(),
                                String.format("Service '%s' has failed %d consecutive checks, reaching threshold of %.0f",
                                        service.getName(), service.getConsecutiveFailures(), rule.getThreshold()),
                                IncidentSeverity.CRITICAL
                        );
                    }
                    break;

                case STATUS_DOWN:
                    if (check.getStatus() == ServiceStatus.DOWN && service.getConsecutiveFailures() == 1) {
                        createNotification(
                                service.getId(),
                                service.getName(),
                                null,
                                "Service Down Alert: " + service.getName(),
                                "Service '" + service.getName() + "' transitioned to DOWN (" + (check.getErrorMessage() != null ? check.getErrorMessage() : "Probe failed") + ")",
                                IncidentSeverity.CRITICAL
                        );
                    }
                    break;
            }
        }
    }

    public void createNotification(String serviceId, String serviceName, String incidentId,
                                   String title, String message, IncidentSeverity severity) {
        Notification notification = new Notification(serviceId, serviceName, incidentId, title, message, severity);
        notificationRepository.save(notification);
        log.info("Notification created [{}]: {}", severity, title);
    }

    // Alert Rule CRUD
    public List<AlertRuleDto> getAllRules() {
        return alertRuleRepository.findAll().stream()
                .map(AlertRuleDto::fromEntity)
                .collect(Collectors.toList());
    }

    public AlertRuleDto createRule(AlertRuleDto dto) {
        AlertRule entity = dto.toEntity();
        AlertRule saved = alertRuleRepository.save(entity);
        return AlertRuleDto.fromEntity(saved);
    }

    public void deleteRule(String id) {
        if (!alertRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alert rule not found with ID: " + id);
        }
        alertRuleRepository.deleteById(id);
    }

    // Notification retrieval and management
    public Page<NotificationDto> getNotifications(Pageable pageable) {
        return notificationRepository.findByOrderByTimestampDesc(pageable)
                .map(NotificationDto::fromEntity);
    }

    public List<NotificationDto> getRecentNotifications() {
        return notificationRepository.findTop20ByOrderByTimestampDesc().stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public long getUnreadNotificationCount() {
        return notificationRepository.countByReadFalse();
    }

    public void markNotificationAsRead(String id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllNotificationsAsRead() {
        List<Notification> unread = notificationRepository.findAll().stream()
                .filter(n -> !n.isRead())
                .peek(n -> n.setRead(true))
                .collect(Collectors.toList());
        notificationRepository.saveAll(unread);
    }
}
