# CloudPulse REST API Documentation

Base URL: `http://localhost:8080/api/v1`

All responses follow the unified envelope:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2026-10-27T11:15:00.000Z"
}
```

---

## 1. Dashboard API

### Get Dashboard Summary
Aggregated metrics including active service count, status distribution, overall uptime, average latency, open incidents, and chart timeseries data.

- **URL:** `/dashboard/summary`
- **Method:** `GET`
- **Sample Request:**
  ```bash
  curl -X GET http://localhost:8080/api/v1/dashboard/summary
  ```
- **Sample Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Dashboard summary retrieved",
    "data": {
      "totalServices": 3,
      "upServices": 2,
      "downServices": 1,
      "degradedServices": 0,
      "unknownServices": 0,
      "openIncidents": 1,
      "overallUptimePercentage": 98.07,
      "averageResponseTimeMs": 315.7,
      "totalHealthChecksRecorded": 14,
      "latencyHistory": [
        {
          "timestamp": "2026-10-27T11:12:05.002Z",
          "responseTimeMs": 322,
          "serviceName": "GitHub Public REST API",
          "success": true
        }
      ]
    }
  }
  ```

---

## 2. Monitored Services API

### List All Services
- **URL:** `/services`
- **Method:** `GET`
- **Response:** Array of `ServiceResponseDto` objects with calculated 24h uptime.

### Get Service by ID
- **URL:** `/services/{id}`
- **Method:** `GET`

### Create Monitored Service
Enforces Jakarta validation and SSRF security verification.

- **URL:** `/services`
- **Method:** `POST`
- **Headers:** `Content-Type: application/json`
- **Request Body:**
  ```json
  {
    "name": "Production Auth API",
    "url": "https://api.github.com",
    "httpMethod": "GET",
    "expectedStatusCode": 200,
    "checkIntervalSeconds": 30,
    "timeoutMs": 5000,
    "failureThreshold": 3,
    "authorizedEndpoint": true,
    "enabled": true
  }
  ```
- **Response:** `201 Created`

### Update Service
- **URL:** `/services/{id}`
- **Method:** `PUT`
- **Request Body:** Same schema as Create.

### Delete Service
Performs a cascade deletion of related metrics, incidents, alert rules, and notifications.
- **URL:** `/services/{id}`
- **Method:** `DELETE`
- **Response:** `200 OK`

### Toggle Service Monitoring (Pause/Resume)
- **URL:** `/services/{id}/toggle`
- **Method:** `PATCH`

### Run On-Demand Check Now
Immediately executes an HTTP/HTTPS check and returns the live measurement without waiting for the next scheduler interval.
- **URL:** `/services/{id}/check`
- **Method:** `POST`
- **Sample Request:**
  ```bash
  curl -X POST http://localhost:8080/api/v1/services/6ae086f5513ad61fc02f612f/check
  ```
- **Sample Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Health check executed",
    "data": {
      "id": "6ae0870f513ad61fc02f6135",
      "serviceId": "6ae086f5513ad61fc02f612f",
      "serviceName": "GitHub Public REST API",
      "url": "https://api.github.com",
      "timestamp": "2026-10-27T11:12:15.025Z",
      "status": "UP",
      "statusCode": 200,
      "responseTimeMs": 29,
      "errorMessage": null,
      "success": true
    }
  }
  ```

---

## 3. Health Checks API

### Get Paginated Checks for a Service
- **URL:** `/health-checks/service/{serviceId}?page=0&size=20`
- **Method:** `GET`

### Get Recent 50 Checks for Service Charts
- **URL:** `/health-checks/service/{serviceId}/recent`
- **Method:** `GET`

---

## 4. Incident Management API

### List Incidents
- **URL:** `/incidents?page=0&size=20`
- **Method:** `GET`

### Acknowledge Incident
- **URL:** `/incidents/{id}/acknowledge`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "user": "Alice-DevOps"
  }
  ```

### Resolve Incident
- **URL:** `/incidents/{id}/resolve`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "user": "Bob-SRE",
    "rootCauseNotes": "Cleared blocked thread pool and rotated Redis connection cache."
  }
  ```

---

## 5. Alert Rules API

### List Rules
- **URL:** `/alert-rules`
- **Method:** `GET`

### Create Alert Rule
- **URL:** `/alert-rules`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "name": "High P95 Latency Alarm",
    "serviceId": null,
    "condition": "RESPONSE_TIME_EXCEEDS",
    "threshold": 1200,
    "enabled": true
  }
  ```

### Delete Alert Rule
- **URL:** `/alert-rules/{id}`
- **Method:** `DELETE`

---

## 6. Notifications API

- `GET /notifications` (Paginated notifications)
- `GET /notifications/recent` (Recent 20 alerts)
- `GET /notifications/unread-count` (Badge counter)
- `PATCH /notifications/{id}/read` (Mark specific alert read)
- `POST /notifications/read-all` (Mark all read)

---

## 7. Demo Data Seeder API

Seeds an explicitly labeled sample outage scenario for evaluation and interviews.
- **URL:** `/demo/seed`
- **Method:** `POST`
- **Sample Request:**
  ```bash
  curl -X POST http://localhost:8080/api/v1/demo/seed
  ```
- **Result:** Seeds `[DEMO] Payment Gateway Service` with 4 consecutive failures, an active critical incident, and latency history.
