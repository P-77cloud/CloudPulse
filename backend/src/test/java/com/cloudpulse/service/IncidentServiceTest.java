package com.cloudpulse.service;

import com.cloudpulse.dto.IncidentActionDto;
import com.cloudpulse.dto.IncidentDto;
import com.cloudpulse.model.*;
import com.cloudpulse.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Incident Service - Lifecycle and Auto-Recovery Unit Tests")
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private IncidentService incidentService;

    private MonitoredService testService;
    private HealthCheck failedCheck;

    @BeforeEach
    void setUp() {
        testService = new MonitoredService("Payment Service", "https://api.example.com/pay", true);
        testService.setId("svc-123");
        testService.setFailureThreshold(3);
        testService.setConsecutiveFailures(3);

        failedCheck = new HealthCheck("svc-123", "Payment Service", "https://api.example.com/pay",
                ServiceStatus.DOWN, 500, 250L, "HTTP 500 Internal Error", false);
    }

    @Test
    @DisplayName("Should create critical incident when failures reach threshold")
    void testProcessServiceFailureCreatesIncident() {
        when(incidentRepository.findFirstByServiceIdAndStatusInOrderByStartedAtDesc(eq("svc-123"), any()))
                .thenReturn(Optional.empty());

        Incident createdIncident = new Incident("svc-123", "Payment Service", "Service Outage", "Description",
                IncidentSeverity.CRITICAL, 3);
        createdIncident.setId("inc-456");
        when(incidentRepository.save(any(Incident.class))).thenReturn(createdIncident);

        incidentService.processServiceFailure(testService, failedCheck);

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(captor.capture());

        Incident captured = captor.getValue();
        assertEquals("svc-123", captured.getServiceId());
        assertEquals(IncidentStatus.OPEN, captured.getStatus());
        assertEquals(IncidentSeverity.CRITICAL, captured.getSeverity());
        assertEquals(3, captured.getConsecutiveFailures());

        verify(alertService).createNotification(eq("svc-123"), eq("Payment Service"), eq("inc-456"),
                contains("CRITICAL"), anyString(), eq(IncidentSeverity.CRITICAL));
    }

    @Test
    @DisplayName("Should NOT duplicate open incident if one already exists")
    void testDoesNotDuplicateExistingOpenIncident() {
        Incident existing = new Incident("svc-123", "Payment Service", "Title", "Desc", IncidentSeverity.CRITICAL, 3);
        existing.setId("inc-existing");
        when(incidentRepository.findFirstByServiceIdAndStatusInOrderByStartedAtDesc(eq("svc-123"), any()))
                .thenReturn(Optional.of(existing));

        incidentService.processServiceFailure(testService, failedCheck);

        verify(incidentRepository, never()).save(any(Incident.class));
        verify(alertService, never()).createNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should auto-resolve open incident when service recovers")
    void testProcessServiceRecoveryAutoResolvesIncident() {
        Incident openIncident = new Incident("svc-123", "Payment Service", "Title", "Desc", IncidentSeverity.CRITICAL, 3);
        openIncident.setId("inc-open");
        openIncident.setStatus(IncidentStatus.OPEN);

        when(incidentRepository.findByServiceIdAndStatusIn(eq("svc-123"), any()))
                .thenReturn(List.of(openIncident));

        incidentService.processServiceRecovery(testService);

        assertEquals(IncidentStatus.RESOLVED, openIncident.getStatus());
        assertEquals("SYSTEM_AUTORECOVERY", openIncident.getResolvedBy());
        assertNotNull(openIncident.getResolvedAt());
        verify(incidentRepository).save(openIncident);
    }

    @Test
    @DisplayName("Should acknowledge open incident manually")
    void testAcknowledgeIncident() {
        Incident openIncident = new Incident("svc-123", "Payment Service", "Title", "Desc", IncidentSeverity.CRITICAL, 3);
        openIncident.setId("inc-open");
        openIncident.setStatus(IncidentStatus.OPEN);

        when(incidentRepository.findById("inc-open")).thenReturn(Optional.of(openIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> i.getArgument(0));

        IncidentActionDto action = new IncidentActionDto("DevOps-Engineer-1", null);
        IncidentDto result = incidentService.acknowledgeIncident("inc-open", action);

        assertEquals(IncidentStatus.ACKNOWLEDGED, result.getStatus());
        assertEquals("DevOps-Engineer-1", result.getAcknowledgedBy());
        assertNotNull(result.getAcknowledgedAt());
    }

    @Test
    @DisplayName("Should resolve incident manually with root cause notes")
    void testResolveIncidentManually() {
        Incident ackedIncident = new Incident("svc-123", "Payment Service", "Title", "Desc", IncidentSeverity.CRITICAL, 3);
        ackedIncident.setId("inc-acked");
        ackedIncident.setStatus(IncidentStatus.ACKNOWLEDGED);

        when(incidentRepository.findById("inc-acked")).thenReturn(Optional.of(ackedIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> i.getArgument(0));

        IncidentActionDto action = new IncidentActionDto("Lead-SRE", "Restarted Kubernetes pods and cleared stale pool");
        IncidentDto result = incidentService.resolveIncident("inc-acked", action);

        assertEquals(IncidentStatus.RESOLVED, result.getStatus());
        assertEquals("Lead-SRE", result.getResolvedBy());
        assertEquals("Restarted Kubernetes pods and cleared stale pool", result.getRootCauseNotes());
        assertNotNull(result.getResolvedAt());
    }
}
