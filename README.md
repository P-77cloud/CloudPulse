# CloudPulse — Cloud Infrastructure Monitoring Platform

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen.svg?logo=springboot)
![Java](https://img.shields.io/badge/Java-21_LTS-orange.svg?logo=openjdk)
![React](https://img.shields.io/badge/React-18-blue.svg?logo=react)
![Vite](https://img.shields.io/badge/Vite-5-purple.svg?logo=vite)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0+-green.svg?logo=mongodb)
![JUnit 5](https://img.shields.io/badge/JUnit-5.10-red.svg?logo=junit5)
![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg?logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

> **CloudPulse** is an enterprise-grade cloud infrastructure health, uptime, and incident management platform built with **Java 21**, **Spring Boot 3**, **React**, and **MongoDB**. It continuously executes scheduled HTTP/HTTPS probes, computes millisecond latency trends and rolling SLA uptime percentages, detects outages, and orchestrates incident escalation with automatic recovery tracking and anti-SSRF protection.

---

## Architecture Overview

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

## Key Features

1. **Live Dashboard & Real Measurements**: Real response times, uptime percentage calculations, and health check histories without simulated or fake cloud metrics.
2. **Scheduled Probes & On-Demand Checking**: Background polling via Spring `@Scheduled` and `ThreadPoolTaskScheduler`, plus instant "Check Now" triggers for live testing.
3. **Anti-SSRF Security Defense**: DNS resolution and IP inspection blocking loopback addresses (`127.0.0.1`, `localhost`), cloud metadata endpoints (`169.254.169.254`, `169.254.170.2`), and private RFC 1918 subnets (`10.0.0.0/8`, `192.168.0.0/16`).
4. **Automated Incident Escalation & Auto-Recovery**: Opens incidents after configurable consecutive failures (default: 3). Automatically resolves incidents with MTTR calculation when services recover.
5. **Incident Lifecycle Management**: Operator acknowledgment, manual resolution with root-cause post-mortem notes, and severity tags (`CRITICAL`, `WARNING`, `INFO`).
6. **Configurable Alert Rules & Notifications**: Threshold alarms for high response times or repeated check failures, paired with an in-app sliding notification drawer.
7. **Interactive Visualizations**: Latency progression charts built with **Recharts**, SLA strip indicators, and dark-mode glassmorphic styling.
8. **Explicitly Labeled Demo Seeder**: Includes a "Seed Demo Outage" feature strictly tagged `[DEMO]` for evaluations and interviews.

---

## Folder Structure

```
CloudPulse/
├── backend/
│   ├── src/
│   │   ├── main/java/com/cloudpulse/
│   │   │   ├── CloudPulseApplication.java
│   │   │   ├── config/              # CORS & Scheduling configs
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── dto/                 # Request & Response DTOs
│   │   │   ├── exception/           # Centralized @RestControllerAdvice
│   │   │   ├── model/               # MongoDB Document Entities & Enums
│   │   │   ├── repository/          # Spring Data Mongo Repositories
│   │   │   ├── security/            # Anti-SSRF URL Security Validator
│   │   │   └── service/             # Probes, Scheduler, Incidents, Alerts
│   │   └── test/java/com/cloudpulse/# JUnit 5 & Mockito Tests
│   ├── pom.xml
│   ├── mvnw & mvnw.cmd
│   └── .mvn/
├── frontend/
│   ├── src/
│   │   ├── components/              # Nav, Charts, Tables, Modals, Incidents
│   │   ├── services/api.js          # REST client
│   │   ├── utils/formatters.js      # Uptime, Latency, & Time-ago helpers
│   │   ├── App.jsx
│   │   └── index.css                # Dark-mode design system & animations
│   ├── package.json
│   └── vite.config.js
├── docs/
│   ├── ARCHITECTURE.md              # Detailed architecture & sequence diagrams
│   ├── API_DOCUMENTATION.md         # Full REST API documentation & cURL examples
│   └── INTERVIEW_PREP.md            # Placement interview questions & answers
├── .github/workflows/
│   └── ci.yml                       # GitHub Actions CI for backend & frontend
├── docker-compose.yml               # Multi-container local orchestration
├── Dockerfile.backend               # Spring Boot multi-stage Dockerfile
├── Dockerfile.frontend              # React + NGINX Dockerfile
├── nginx.conf                       # Reverse proxy configuration
├── .env.example
├── .gitignore
└── README.md
```

---

## Getting Started (Windows 11 & VS Code)

### Prerequisites
- **Java 21 LTS** (`java -version`)
- **Maven 3.9+** (or use included `./mvnw`)
- **Node.js 20+** and **npm** (`node -v`, `npm -v`)
- **MongoDB 6.0+** running on `localhost:27017`

---

### Step 1: Start MongoDB
Ensure your local MongoDB service is running:
```powershell
# In PowerShell (Run as Administrator if needed):
Get-Service -Name "*mongo*"
# If stopped:
Start-Service MongoDB
```

---

### Step 2: Run Backend (Spring Boot 3)
```powershell
cd backend

# Run automated tests
mvn clean test

# Run backend application locally
mvn spring-boot:run
```
The backend starts on `http://localhost:8080`.
Verify backend health: `http://localhost:8080/actuator/health`

---

### Step 3: Run Frontend (React + Vite)
Open a new PowerShell terminal:
```powershell
cd frontend

# Install dependencies (first time only)
npm install

# Start Vite dev server
npm run dev
```
Open your browser at **`http://localhost:5173`**.

---

### Alternative: Run with Docker Compose
If Docker Desktop is installed:
```bash
docker-compose up -d --build
```
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- MongoDB: `localhost:27017`

---

## Testing & Quality Assurance

### Run Backend Unit Tests (JUnit 5 & Mockito)
```powershell
cd backend
mvn test
```
**Test Coverage Includes:**
- `UrlSecurityValidatorTest`: Validates anti-SSRF defenses (loopback rejection, metadata blocking `169.254.169.254`, scheme checks).
- `IncidentServiceTest`: State transitions, auto-recovery logic, and incident de-duplication.
- `HealthCheckExecutorServiceTest`: Real probe executions and security boundary enforcement.
- `ServiceControllerTest`: Spring WebMvcTest endpoints and validation rules.

### Run Frontend Production Build
```powershell
cd frontend
npm run build
```

---

## Core REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/dashboard/summary` | Aggregated metrics & chart timeseries |
| `GET` | `/api/v1/services` | List all monitored services with 24h uptime |
| `POST` | `/api/v1/services` | Create new service with SSRF validation |
| `POST` | `/api/v1/services/{id}/check` | Run immediate on-demand probe |
| `PATCH` | `/api/v1/services/{id}/toggle` | Pause or resume monitoring |
| `DELETE`| `/api/v1/services/{id}` | Cascade delete service & historical logs |
| `GET` | `/api/v1/health-checks/service/{id}/recent` | Recent 50 checks for deep dive modal |
| `GET` | `/api/v1/incidents` | List active & resolved outages |
| `POST` | `/api/v1/incidents/{id}/acknowledge` | Acknowledge incident by operator |
| `POST` | `/api/v1/incidents/{id}/resolve` | Resolve incident with root cause notes |
| `GET` | `/api/v1/alert-rules` | View configured threshold rules |
| `POST` | `/api/v1/alert-rules` | Create threshold alert rule |
| `GET` | `/api/v1/notifications/recent` | Recent notifications & unread counts |
| `POST` | `/api/v1/demo/seed` | Seed labeled `[DEMO]` outage & incident data |

Full API documentation available in [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md).

---

## Placement & Interview Highlights

For CSE placement preparation, refer to [docs/INTERVIEW_PREP.md](docs/INTERVIEW_PREP.md) which includes:
- System design decisions: Spring Boot 3 + MongoDB for write-heavy time-series telemetry.
- Security deep-dive: Multi-layer SSRF defense against cloud metadata theft.
- Concurrency patterns: Non-blocking probes using `ThreadPoolTaskScheduler` and Java 21 `HttpClient`.
- Real SLA arithmetic: Formula for 24h rolling uptime calculation utilizing compound indexes.

---

## License

This project is licensed under the MIT License — feel free to use it for learning, university projects, and placement portfolios!
