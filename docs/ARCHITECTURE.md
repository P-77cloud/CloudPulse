# CloudPulse Architecture & Design Specification

## 1. System Overview

**CloudPulse** is an enterprise-grade cloud infrastructure health, uptime, and incident management platform. It continuously probes critical HTTP/HTTPS endpoints, records latency distributions, calculates 24-hour and rolling SLA uptime percentages, detects anomalous downtimes, and orchestrates incident workflows with auto-recovery and MTTR tracking.

```
+-------------------------------------------------------------------------+
|                        CloudPulse Architecture                          |
+-------------------------------------------------------------------------+
|                                                                         |
|  +------------------------+             +----------------------------+  |
|  |     React Frontend     |  HTTP/JSON  |     Spring Boot Backend    |  |
|  |   (Vite + Recharts)    |<----------->|      (Java 21 / REST)      |  |
|  +------------------------+             +--------------+-------------+  |
|                                                        |                |
|                                       +----------------+-------------+  |
|                                       |                              |  |
|                                       v                              v  |
|                         +---------------------------+  +-------------+  |
|                         |    MongoDB (cloudpulse)   |  | Target HTTP |  |
|                         |  - services               |  |  Endpoints  |  |
|                         |  - health_checks          |  |  (Probes)   |  |
|                         |  - incidents              |  +-------------+  |
|                         |  - alert_rules            |                   |
|                         |  - notifications          |                   |
|                         +---------------------------+                   |
+-------------------------------------------------------------------------+
```

---

## 2. Layered Spring Boot Architecture

CloudPulse follows standard clean software engineering layers:

```
[ HTTP Requests / Browser Client ]
                │
                ▼
┌────────────────────────────────────────┐
│         Presentation Layer             │
│   • ServiceController                  │
│   • HealthCheckController              │
│   • IncidentController                 │
│   • AlertRuleController                │
│   • NotificationController             │
│   • DashboardController                │
│   • GlobalExceptionHandler             │
└──────────────────┬─────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────┐
│            Business Layer              │
│   • HealthCheckScheduler (Polling)     │
│   • HealthCheckExecutorService (HTTP)  │
│   • MonitoredServiceService (CRUD)     │
│   • IncidentService (State Machine)    │
│   • AlertService (Rules & Events)      │
│   • DashboardService (Aggregation)     │
│   • UrlSecurityValidator (SSRF Wall)   │
└──────────────────┬─────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────┐
│           Persistence Layer            │
│   • MonitoredServiceRepository         │
│   • HealthCheckRepository              │
│   • IncidentRepository                 │
│   • AlertRuleRepository                │
│   • NotificationRepository             │
└──────────────────┬─────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────┐
│        MongoDB 7.0+ Database           │
│   Database: cloudpulse                 │
└────────────────────────────────────────┘
```

---

## 3. Real-Time Health Check & Incident Flow

The diagram below illustrates how an automated scheduled health check flows through security validation, HTTP execution, metric persistence, alert rule evaluation, and incident state transitions:

```mermaid
sequenceDiagram
    autonumber
    participant Scheduler as HealthCheckScheduler
    participant Security as UrlSecurityValidator
    participant Probe as HealthCheckExecutor
    participant Target as Target Endpoint
    participant DB as MongoDB
    participant IncMgr as IncidentService
    participant AlertMgr as AlertService

    Scheduler->>DB: Query enabled services where check is due
    DB-->>Scheduler: List<MonitoredService>
    
    loop For each due service
        Scheduler->>Security: validateUrl(service.url)
        alt SSRF / Prohibited IP detected
            Security-->>Scheduler: Throw SecurityValidationException
            Scheduler->>DB: Save HealthCheck(status=DOWN, error="Security violation")
        else URL is Valid and Authorized
            Scheduler->>Probe: executeCheck(service)
            Probe->>Target: HTTP GET / HEAD (with timeout)
            Target-->>Probe: HTTP Response (Status, Latency)
            Probe-->>Scheduler: HealthCheck Entity (status, ms, code)
            
            Scheduler->>DB: Save HealthCheck record
            Scheduler->>DB: Update MonitoredService state
            
            alt HealthCheck is Failed (status == DOWN)
                Scheduler->>IncMgr: processServiceFailure(service, check)
                alt consecutiveFailures >= failureThreshold
                    IncMgr->>DB: Create Incident (status=OPEN, severity=CRITICAL)
                    IncMgr->>AlertMgr: Dispatch Outage Notification
                end
            else HealthCheck is Successful (status == UP)
                Scheduler->>IncMgr: processServiceRecovery(service)
                alt Active Incident Exists
                    IncMgr->>DB: Resolve Incident (status=RESOLVED, by="SYSTEM_AUTORECOVERY")
                    IncMgr->>AlertMgr: Dispatch Recovery Notification
                end
            end
            
            Scheduler->>AlertMgr: evaluateRules(service, check)
        end
    end
```

---

## 4. Incident State Machine

```
      [ Service Fails >= Threshold ]
                    │
                    ▼
           ┌─────────────────┐
           │   STATUS: OPEN  │
           └────────┬────────┘
                    │
         ┌──────────┴──────────┐
         │ (Manual Ack)        │ (Service Recovers)
         ▼                     ▼
┌─────────────────┐   ┌─────────────────┐
│  ACKNOWLEDGED   │   │    RESOLVED     │
└────────┬────────┘   │  (Auto-Recovery)│
         │            └─────────────────┘
         │ (Manual Resolve     ▲
         │  with notes)        │
         └─────────────────────┘
```

1. **OPEN**: Automatically triggered when consecutive failures hit `failureThreshold` (default: 3).
2. **ACKNOWLEDGED**: Operator takes ownership of the incident to prevent duplicate alerts.
3. **RESOLVED**: Either automatically resolved when consecutive subsequent checks return HTTP 200/UP, or manually resolved with root cause notes.

---

## 5. MongoDB Collections & Indexing Strategy

| Collection | Key Indexed Fields | Index Type | Purpose |
| :--- | :--- | :--- | :--- |
| `services` | `name`, `status`, `enabled` | Single field | Fast lookup of active services and status counts |
| `health_checks` | `serviceId`, `timestamp` | Compound `{serviceId: 1, timestamp: -1}` | Fast retrieval of recent 24h checks for SLA uptime and latency charts without full-collection scan |
| `incidents` | `serviceId`, `status`, `startedAt` | Single & Compound | Fast retrieval of open incidents and MTTR calculation |
| `alert_rules` | `enabled`, `serviceId` | Single field | Active rule lookup during health evaluation |
| `notifications` | `timestamp`, `read` | Single field | Sorting notifications chronologically with unread filter |

---

## 6. SSRF Protection & Security Architecture

Cloud infrastructure monitors are prime targets for **Server-Side Request Forgery (SSRF)** attacks. If an attacker inputs an internal URL, a naive monitor could probe private microservices or steal IAM cloud credentials via the link-local metadata service.

CloudPulse incorporates a strict multi-layer SSRF defense:
1. **Protocol Restriction**: Only `http://` and `https://` are permitted. Schemes like `file://`, `gopher://`, `ftp://`, and `ldap://` are rejected.
2. **Credential Stripping**: Reject URLs with embedded credentials (`http://admin:pass@host`).
3. **DNS Resolution & IP Inspection**: Resolves host DNS via `InetAddress.getAllByName(host)` and inspects each resolved IP:
   - Loopback (`127.0.0.0/8`, `::1`)
   - Link-Local & Cloud Metadata (`169.254.169.254`, `169.254.170.2`, `fe80::/10`)
   - Private RFC 1918 networks (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`)
   - Any local / wildcard (`0.0.0.0`)
   - Multicast ranges (`224.0.0.0/4`)
4. **Mandatory Authorization Flag**: Services require `authorizedEndpoint = true` to confirm intentional operator monitoring.
