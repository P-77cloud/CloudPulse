package com.cloudpulse.service;

import com.cloudpulse.model.Incident;
import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Seed Data Service - Idempotent Demo Seeding and Safe Reset Unit Tests")
class SeedDataServiceTest {

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private SeedDataService seedDataService;

    private MonitoredService realService;
    private MonitoredService demoService;

    @BeforeEach
    void setUp() {
        realService = new MonitoredService("GitHub Public REST API", "https://api.github.com", true);
        realService.setId("real-1");

        demoService = new MonitoredService("[DEMO] Payment Gateway Service", "https://httpbin.org/status/503", true);
        demoService.setId("demo-1");
    }

    @Test
    @DisplayName("resetDemoData should only delete records tagged [DEMO] and preserve real services")
    void testResetDemoDataPreservesRealServices() {
        when(serviceRepository.findAll()).thenReturn(List.of(realService, demoService));

        seedDataService.resetDemoData();

        // Verify only demo service ID was deleted
        verify(serviceRepository).deleteById("demo-1");
        verify(serviceRepository, never()).deleteById("real-1");

        verify(healthCheckRepository).deleteByServiceId("demo-1");
        verify(incidentRepository).deleteByServiceId("demo-1");
        verify(notificationRepository).deleteByServiceId("demo-1");
    }

    @Test
    @DisplayName("seedDemoData should be idempotent and purge prior demo records before creating new one")
    void testSeedDemoDataIsIdempotent() {
        when(serviceRepository.findAll()).thenReturn(List.of(demoService));
        when(serviceRepository.save(any(MonitoredService.class))).thenAnswer(i -> {
            MonitoredService s = i.getArgument(0);
            s.setId("demo-new");
            return s;
        });
        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> {
            Incident inc = i.getArgument(0);
            inc.setId("inc-demo-new");
            return inc;
        });

        seedDataService.seedDemoData();

        // Verifies previous demo service was purged first
        verify(serviceRepository).deleteById("demo-1");
        // Verifies new single demo service was saved
        verify(serviceRepository).save(any(MonitoredService.class));
        verify(incidentRepository).save(any(Incident.class));
    }
}
