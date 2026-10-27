package com.cloudpulse.service;

import com.cloudpulse.model.*;
import com.cloudpulse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeedDataService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataService.class);

    private final MonitoredServiceRepository serviceRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final IncidentRepository incidentRepository;
    private final NotificationRepository notificationRepository;

    public SeedDataService(MonitoredServiceRepository serviceRepository,
                           AlertRuleRepository alertRuleRepository,
                           HealthCheckRepository healthCheckRepository,
                           IncidentRepository incidentRepository,
                           NotificationRepository notificationRepository) {
        this.serviceRepository = serviceRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.incidentRepository = incidentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        if (serviceRepository.count() == 0) {
            log.info("Initializing CloudPulse with default real endpoints and alert rules...");

            // 1. GitHub Public API
            MonitoredService githubService = new MonitoredService(
                    "GitHub Public REST API",
                    "https://api.github.com",
                    true
            );
            githubService.setExpectedStatusCode(200);
            githubService.setCheckIntervalSeconds(30);
            githubService.setTimeoutMs(5000);
            githubService.setFailureThreshold(3);
            githubService.setStatus(ServiceStatus.UNKNOWN);
            serviceRepository.save(githubService);

            // 2. HTTPBin Status Endpoint
            MonitoredService httpbinService = new MonitoredService(
                    "HTTPBin Echo Service",
                    "https://httpbin.org/status/200",
                    true
            );
            httpbinService.setExpectedStatusCode(200);
            httpbinService.setCheckIntervalSeconds(45);
            httpbinService.setTimeoutMs(5000);
            httpbinService.setFailureThreshold(3);
            httpbinService.setStatus(ServiceStatus.UNKNOWN);
            serviceRepository.save(httpbinService);

            // 3. Default Alert Rules
            AlertRule latencyRule = new AlertRule(
                    "High Latency Warning (> 1500ms)",
                    null,
                    AlertCondition.RESPONSE_TIME_EXCEEDS,
                    1500.0
            );
            alertRuleRepository.save(latencyRule);

            AlertRule failureRule = new AlertRule(
                    "Service Outage Alarm (>= 3 failures)",
                    null,
                    AlertCondition.CONSECUTIVE_FAILURES,
                    3.0
            );
            alertRuleRepository.save(failureRule);

            log.info("CloudPulse initialization completed with 2 monitored services and 2 alert rules.");
        }
    }

    /**
     * Resets and purges all records strictly tagged with [DEMO].
     * Never removes or alters real monitored endpoints or real metrics.
     */
    public synchronized void resetDemoData() {
        log.info("Resetting demo data: purging all records marked [DEMO]...");

        // 1. Find all demo services
        List<MonitoredService> allServices = serviceRepository.findAll();
        List<MonitoredService> demoServices = allServices.stream()
                .filter(s -> s.getName() != null && s.getName().contains("[DEMO]"))
                .collect(Collectors.toList());

        for (MonitoredService demoSvc : demoServices) {
            healthCheckRepository.deleteByServiceId(demoSvc.getId());
            incidentRepository.deleteByServiceId(demoSvc.getId());
            notificationRepository.deleteByServiceId(demoSvc.getId());
            alertRuleRepository.deleteByServiceId(demoSvc.getId());
            serviceRepository.deleteById(demoSvc.getId());
        }

        // Clean any stray incidents with [DEMO] in title
        incidentRepository.findAll().stream()
                .filter(inc -> inc.getTitle() != null && inc.getTitle().contains("[DEMO]"))
                .forEach(inc -> incidentRepository.deleteById(inc.getId()));

        // Clean any stray notifications with [DEMO] in title
        notificationRepository.findAll().stream()
                .filter(n -> n.getTitle() != null && n.getTitle().contains("[DEMO]"))
                .forEach(n -> notificationRepository.deleteById(n.getId()));

        log.info("Demo data reset completed. All [DEMO] records cleanly removed.");
    }

    /**
     * Seeds labeled demo data for evaluation / viva demonstration.
     * IDEMPOTENT: First cleans any prior demo records so duplicate services are never created.
     */
    public synchronized void seedDemoData() {
        // First ensure idempotency: clean up existing demo records
        resetDemoData();

        log.info("Seeding single canonical DEMO Payment Gateway for outage demonstration...");

        MonitoredService demoService = new MonitoredService(
                "[DEMO] Payment Gateway Service",
                "https://httpbin.org/status/503",
                true
        );
        demoService.setExpectedStatusCode(200);
        demoService.setCheckIntervalSeconds(15);
        demoService.setFailureThreshold(3);
        demoService.setStatus(ServiceStatus.DOWN);
        demoService.setConsecutiveFailures(4);
        demoService.setLastCheckTime(Instant.now());
        demoService.setLastResponseTimeMs(412L);
        demoService.setUptimePercentage(94.2);
        MonitoredService savedService = serviceRepository.save(demoService);

        // Add 10 demo health check records
        for (int i = 10; i >= 1; i--) {
            boolean success = i > 4;
            HealthCheck hc = new HealthCheck(
                    savedService.getId(),
                    savedService.getName(),
                    savedService.getUrl(),
                    success ? ServiceStatus.UP : ServiceStatus.DOWN,
                    success ? 200 : 503,
                    (long) (180 + Math.random() * 200),
                    success ? null : "[DEMO] Upstream 503 Service Unavailable",
                    success
            );
            hc.setTimestamp(Instant.now().minus(i * 30L, ChronoUnit.SECONDS));
            healthCheckRepository.save(hc);
        }

        // Add demo incident
        Incident demoIncident = new Incident(
                savedService.getId(),
                savedService.getName(),
                "[DEMO] Payment Gateway Intermittent 503 Outage",
                "[DEMO DATA] Upstream banking provider gateway returned HTTP 503 during simulated load spike.",
                IncidentSeverity.CRITICAL,
                4
        );
        demoIncident.setStartedAt(Instant.now().minus(5, ChronoUnit.MINUTES));
        Incident savedIncident = incidentRepository.save(demoIncident);

        // Add demo notification
        Notification notif = new Notification(
                savedService.getId(),
                savedService.getName(),
                savedIncident.getId(),
                "[DEMO] Outage Alert: Payment Gateway",
                "[DEMO DATA] Payment Gateway failed 4 consecutive health checks.",
                IncidentSeverity.CRITICAL
        );
        notificationRepository.save(notif);

        log.info("Canonical DEMO scenario successfully seeded.");
    }
}
