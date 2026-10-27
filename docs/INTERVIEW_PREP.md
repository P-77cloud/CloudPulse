# CloudPulse — Placement Interview Preparation Guide (2027 CSE)

This guide is tailored specifically for third-year Computer Science students presenting **CloudPulse** in technical interviews, system design rounds, and project vivas.

---

## 1. Project Pitch (30-Second Elevator Pitch)

> *"CloudPulse is a full-stack cloud infrastructure monitoring and incident management platform built with Spring Boot 3, Java 21, React, and MongoDB. It performs automated, concurrent HTTP/HTTPS health checks, measures real millisecond latency distributions, dynamically computes SLA uptime percentages, and automatically escalates outages into incidents with auto-recovery and MTTR tracking. Crucially, I engineered an anti-SSRF security firewall to prevent attackers from using the monitoring engine to probe private internal subnets or cloud metadata endpoints."*

---

## 2. Core Technical Questions & Model Answers

### Q1: Why did you choose MongoDB instead of a relational database like PostgreSQL?
**Answer:**
1. **Time-Series Metric Ingestion Characteristics:** Health check events are write-heavy, append-only logs. MongoDB handles high-throughput document inserts natively without the heavy write-locking overhead of relational table constraints.
2. **Flexible Service Configuration Schema:** Different endpoints need varying metadata (headers, expected status codes, authentication schemes, timeout rules). A document store allows schema evolution without costly table migrations.
3. **Compound Index Performance:** By indexing `{ serviceId: 1, timestamp: -1 }`, MongoDB allows sub-millisecond retrieval of the latest checks or 24-hour ranges for SLA calculation without table joins.

---

### Q2: How does your Anti-SSRF (Server-Side Request Forgery) defense work?
**Answer:**
A naive monitor checks if `url.contains("localhost")`, which is fatally flawed because an attacker can pass `http://127.0.0.1`, `http://0.0.0.0`, `http://[::1]`, private RFC 1918 IPs (`10.0.0.1`, `192.168.1.1`), or DNS hostnames pointing to cloud metadata: `http://169.254.169.254`.

In CloudPulse, my `UrlSecurityValidator`:
1. **Restricts Protocols:** Enforces `http` or `https` schemes; rejects `file://`, `ftp://`, `gopher://`.
2. **Rejects Embedded Credentials:** Prevents userinfo obfuscation (`http://admin:pass@host`).
3. **Resolves DNS and Inspects All IP Addresses:** Uses `InetAddress.getAllByName(host)` and tests the resolved IPs:
   - `isLoopbackAddress()` (blocks 127.0.0.0/8, ::1)
   - `isLinkLocalAddress()` (blocks 169.254.0.0/16 — protecting AWS/GCP/Azure IMDS metadata endpoints)
   - `isSiteLocalAddress()` (blocks RFC 1918 private subnets 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)
   - `isAnyLocalAddress()` & `isMulticastAddress()`
4. **Requires Explicit Authorization:** Operators must verify endpoint ownership (`authorizedEndpoint = true`).

---

### Q3: How do the scheduled health checks execute without blocking threads?
**Answer:**
- In Spring Boot, I configured a dedicated `ThreadPoolTaskScheduler` with a pool size of 5 worker threads (`cloudpulse-monitor-pool`).
- The scheduler runs periodically (default 15 seconds) and checks which enabled services have exceeded their individual `checkIntervalSeconds`.
- The probe uses Java 21's modern non-blocking `HttpClient` with strict connection timeouts and request timeouts, ensuring that a slow or hanging external server never exhausts the application's thread pool.

---

### Q4: How is 24-Hour Uptime Percentage calculated?
**Answer:**
```java
Instant past24h = Instant.now().minus(24, ChronoUnit.HOURS);
long total = healthCheckRepository.countByServiceIdAndTimestampAfter(id, past24h);
long success = healthCheckRepository.countByServiceIdAndIsSuccessTrueAndTimestampAfter(id, past24h);
double uptime = (double) success / total * 100.0;
```
Because of the compound index on `{ serviceId: 1, timestamp: -1 }`, the query relies on an index scan (`IXSCAN`) rather than a collection scan (`COLLSCAN`), computing accurate real percentages efficiently.

---

### Q5: Explain the Incident State Machine and Auto-Recovery.
**Answer:**
1. **Consecutive Failure Threshold:** Instead of triggering an incident on a single transient network blip (which creates alert fatigue), CloudPulse waits for `failureThreshold` consecutive failed checks (default: 3).
2. **De-duplication:** Before opening an incident, it queries `findByServiceIdAndStatusIn(id, [OPEN, ACKNOWLEDGED])`. If one already exists, it updates failure count rather than generating duplicate spam.
3. **Auto-Recovery:** When a subsequent probe succeeds:
   - Consecutive failures counter is reset to 0.
   - Any active open/acknowledged incidents for that service transition automatically to `RESOLVED` with `resolvedBy = "SYSTEM_AUTORECOVERY"`.
   - MTTR (Mean Time to Resolution) is computed via `Duration.between(startedAt, resolvedAt)`.

---

### Q6: How did you test your application?
**Answer:**
1. **Unit Testing:** Used **JUnit 5** and **Mockito** to test business logic in isolation without depending on a running database. Tested the incident state transitions, auto-recovery logic, and health check executor.
2. **SSRF Security Unit Tests:** Wrote parameterized tests (`@ParameterizedTest` with `@ValueSource`) proving that loopbacks, metadata IPs (169.254.169.254), and dangerous protocols are strictly rejected.
3. **Web Layer Testing:** Used `@WebMvcTest` with Spring's `MockMvc` to verify HTTP status codes (200, 201, 400 Bad Request on invalid payloads).
4. **Production Build Verification:** Built the Maven fat JAR (`mvn package`) and React bundle with Vite (`npm run build`).

---

### Q7: If you had to scale this system to monitor 100,000 services, what would you change?
**Answer:**
1. **Distributed Probe Workers:** Decouple scheduling from execution using **Apache Kafka** or **RabbitMQ**. A scheduler pushes check tasks to a queue; multiple stateless worker nodes consume and execute the HTTP probes.
2. **Caching Active State:** Cache latest service states in **Redis** to eliminate database queries for dashboard reads.
3. **Time-Series Compaction:** Implement time-series collection bucketing in MongoDB or roll up older health checks into hourly and daily summary records.
