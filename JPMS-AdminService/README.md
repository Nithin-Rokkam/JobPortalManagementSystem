# JPMS — Admin Service

<<<<<<< HEAD
## Overview

The **Admin Service** is a Spring Boot microservice (port **8084**) providing platform administration for the Job Portal Management System. It registers with **Eureka** as `admin-service` and is accessed through the **API Gateway** at `localhost:9090/api/admin/**`.

All endpoints require a valid **JWT** with the **ADMIN** role. The API Gateway validates the JWT and forwards `X-User-Id` and `X-User-Role` headers — no JWT parsing happens inside Admin Service.

| Property | Value |
|---|---|
| **Port** | `8084` |
| **Eureka Name** | `admin-service` |
| **Database** | MySQL — `admin_db` |
| **Spring Boot** | `3.2.5` |
| **Spring Cloud** | `2023.0.1` |

---

## Request Flow

```
Client (Postman)
   │
   │  Authorization: Bearer <JWT>
   ▼
API Gateway (:9090)
   │  • Validates JWT
   │  • Extracts userId, role from claims
   │  • Sets X-User-Id, X-User-Role headers
   ▼
Admin Service (:8084)
   │  • Reads X-User-Id, X-User-Role
   │  • Enforces ADMIN role
   │  • Delegates to AdminService
   ▼
Feign Clients → Internal Endpoints
   ├── AuthServiceClient     → auth-service         /api/internal/users/**
   ├── AdminJobClient        → job-service           /api/internal/jobs/**
   └── AdminAppClient        → application-service   /api/internal/applications/**
=======
**Port:** `8084` | **Spring Application Name:** `admin-service`

The Admin Service provides the governance and monitoring layer of the Joblix platform. It exposes a set of ADMIN-only REST APIs for user management (ban, unban, delete), job management (view, delete), platform reporting, and audit log retrieval. It does not have its own user or job data — instead it orchestrates calls to AuthService, JobService, and ApplicationService via Feign clients, and maintains its own `audit_logs` table for tracking admin actions.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Main Application Class](#main-application-class)
4. [Entity — AuditLog](#entity--auditlog)
5. [DTOs](#dtos)
6. [Repository — AuditLogRepository](#repository--auditlogrepository)
7. [Feign Clients](#feign-clients)
8. [Security](#security)
9. [Service — AdminService](#service--adminservice)
10. [Controller — AdminController](#controller--admincontroller)
11. [Exception Handling](#exception-handling)
12. [Configuration](#configuration)
13. [API Reference](#api-reference)

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 (audit_logs table only) |
| Inter-Service | OpenFeign |
| Service Discovery | Netflix Eureka Client |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Logging | Log4j2 |
| Build | Maven |

> Note: AdminService has **no RabbitMQ dependency** — it is excluded from RabbitMQ auto-configuration in `application.yml`.

---

## Project Structure

```
JPMS-AdminService/src/main/java/com/capg/jobportal/
├── JpmsAdminServiceApplication.java      ← Entry point
├── client/
│   ├── AdminAppClient.java               ← Feign client → ApplicationService
│   ├── AdminJobClient.java               ← Feign client → JobService
│   └── AuthServiceClient.java            ← Feign client → AuthService
├── config/
│   └── SwaggerConfig.java                ← OpenAPI configuration
├── controller/
│   └── AdminController.java              ← All admin REST endpoints
├── dto/
│   ├── ApplicationStats.java             ← Stats from ApplicationService
│   ├── ErrorResponse.java                ← Standardized error wrapper
│   ├── JobResponse.java                  ← Job data from JobService
│   ├── PlatformReport.java               ← Aggregated platform report
│   └── UserResponse.java                 ← User data from AuthService
├── exception/
│   ├── AccessDeniedException.java        ← Thrown when non-admin accesses endpoints
│   └── GlobalExceptionHandler.java       ← @ControllerAdvice error handler
├── model/
│   └── AuditLog.java                     ← JPA entity for audit_logs table
├── repository/
│   └── AuditLogRepository.java           ← JPA repository for AuditLog
├── security/
│   └── SecurityConfig.java               ← Stateless Spring Security config
└── service/
    └── AdminService.java                 ← All business logic
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
```

---

<<<<<<< HEAD
## Feign Client → Auth Service Endpoint Mapping

| Admin Feign Method | HTTP | Auth Internal Endpoint | Auth Controller Method |
|---|---|---|---|
| `getAllUsers()` | `GET` | `/api/internal/users` | `InternalController.getAllUsers()` |
| `deleteUser(id)` | `DELETE` | `/api/internal/users/{id}` | `InternalController.deleteUser(id)` |
| `banUser(id)` | `PUT` | `/api/internal/users/{id}/ban` | `InternalController.banUser(id)` |
| `unbanUser(id)` | `PUT` | `/api/internal/users/{id}/unban` | `InternalController.unbanUser(id)` |

### Job Service Mapping

| Admin Feign Method | HTTP | Job Internal Endpoint |
|---|---|---|
| `getAllJobs()` | `GET` | `/api/internal/jobs/all` |
| `deleteJob(id)` | `DELETE` | `/api/internal/jobs/{id}` |

### Application Service Mapping

| Admin Feign Method | HTTP | App Internal Endpoint |
|---|---|---|
| `getStats()` | `GET` | `/api/internal/applications/stats` |

---

## Prerequisites

- Java 17+, Maven 3.8+
- MySQL running with database `admin_db` created
- Eureka Server on `localhost:8761`
- Auth Service on `localhost:8081`
- Job Service running
- Application Service running
- API Gateway on `localhost:9090`

---

## Running the Service

```bash
cd JPMS-AdminService
mvn spring-boot:run
```

---

## REST API Reference

### Base URL

| Access | URL |
|---|---|
| **Via Gateway (recommended)** | `http://localhost:9090/api/admin` |
| **Direct** | `http://localhost:8084/api/admin` |

> **Always use the Gateway URL.** Direct access bypasses JWT verification and will be missing the `X-User-Id` / `X-User-Role` headers.

---

### API Summary

| # | Method | Endpoint | Description |
|---|---|---|---|
| 1 | `GET` | `/api/admin/users` | List all users |
| 2 | `DELETE` | `/api/admin/users/{id}` | Delete a user |
| 3 | `PUT` | `/api/admin/users/{id}/ban` | Ban a user |
| 4 | `PUT` | `/api/admin/users/{id}/unban` | Unban a user |
| 5 | `GET` | `/api/admin/jobs` | List all jobs |
| 6 | `DELETE` | `/api/admin/jobs/{id}` | Delete a job |
| 7 | `GET` | `/api/admin/reports` | Platform report |
| 8 | `GET` | `/api/admin/audit-logs` | View audit trail |

---

### 1. `GET /api/admin/users` — Get All Users

```
GET http://localhost:9090/api/admin/users
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "JOB_SEEKER",
    "status": "ACTIVE"
  }
]
```

**403 Forbidden** (non-ADMIN token)

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. ADMIN role required.",
  "timestamp": "2026-03-23T10:15:00"
}
```

---

### 2. `DELETE /api/admin/users/{id}` — Delete a User

```
DELETE http://localhost:9090/api/admin/users/3
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "User deleted successfully" }
```

---

### 3. `PUT /api/admin/users/{id}/ban` — Ban a User

```
PUT http://localhost:9090/api/admin/users/3/ban
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "User banned successfully" }
```

> Banned users get "Account suspended" on login.

---

### 4. `PUT /api/admin/users/{id}/unban` — Unban a User

```
PUT http://localhost:9090/api/admin/users/3/unban
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "User unbanned successfully" }
```

---

### 5. `GET /api/admin/jobs` — Get All Jobs

```
GET http://localhost:9090/api/admin/jobs
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
[
  {
    "id": 1,
    "title": "Java Backend Developer",
    "companyName": "TechCorp Inc.",
    "location": "Bangalore",
    "salary": "12-18 LPA",
    "experience": "3-5 years",
    "description": "Build microservices...",
    "postedByEmail": "recruiter@example.com",
    "createdAt": "2026-03-22T10:30:00"
  }
]
```

---

### 6. `DELETE /api/admin/jobs/{id}` — Delete a Job

```
DELETE http://localhost:9090/api/admin/jobs/1
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "Job deleted successfully" }
```

---

### 7. `GET /api/admin/reports` — Platform Report

```
GET http://localhost:9090/api/admin/reports
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{
  "totalUsers": 15,
  "totalJobs": 8,
  "applicationStats": {
    "totalApplications": 25,
    "appliedCount": 10,
    "underReviewCount": 8,
    "shortlistedCount": 5,
    "rejectedCount": 2
  },
  "users": [ ... ],
  "jobs": [ ... ]
}
```

---

### 8. `GET /api/admin/audit-logs` — View Audit Logs

```
GET http://localhost:9090/api/admin/audit-logs
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
[
  {
    "id": 1,
    "action": "DELETE_USER",
    "performedBy": "admin:5",
    "details": "Deleted user ID: 3",
    "createdAt": "2026-03-23T10:20:00"
  },
  {
    "id": 2,
    "action": "BAN_USER",
    "performedBy": "admin:5",
    "details": "Banned user ID: 7",
    "createdAt": "2026-03-23T10:25:00"
  }
]
```

---

## Postman Testing Workflow

### Step 1 — Register an ADMIN user

```
POST http://localhost:9090/api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Admin User",
  "email": "admin@portal.com",
  "password": "admin12345",
  "phone": "9000000000",
  "role": "ADMIN"
}
```

### Step 2 — Login as ADMIN

```
POST http://localhost:9090/api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@portal.com",
  "password": "admin12345"
}
```

Copy the `accessToken` from the response.

### Step 3 — Set Authorization in Postman

For all admin requests, set:
- **Header**: `Authorization` = `Bearer <paste_access_token>`

### Step 4 — Test Endpoints

| # | Request | URL |
|---|---|---|
| 1 | `GET` | `http://localhost:9090/api/admin/users` |
| 2 | `GET` | `http://localhost:9090/api/admin/jobs` |
| 3 | `GET` | `http://localhost:9090/api/admin/reports` |
| 4 | `PUT` | `http://localhost:9090/api/admin/users/2/ban` |
| 5 | `PUT` | `http://localhost:9090/api/admin/users/2/unban` |
| 6 | `DELETE` | `http://localhost:9090/api/admin/users/3` |
| 7 | `DELETE` | `http://localhost:9090/api/admin/jobs/1` |
| 8 | `GET` | `http://localhost:9090/api/admin/audit-logs` |

### Step 5 — Verify Access Control

Login as `JOB_SEEKER` or `RECRUITER` and hit any admin endpoint → expect `403 Forbidden`.

---

## End-to-End Testing Order

| Step | Service | Action |
|---|---|---|
| 1 | Auth | Register `RECRUITER`, `JOB_SEEKER`, `ADMIN` |
| 2 | Auth | Login as `RECRUITER` → save token |
| 3 | Job | Create jobs |
| 4 | Auth | Login as `JOB_SEEKER` → save token |
| 5 | Application | Apply for jobs |
| 6 | Auth | Login as `ADMIN` → save token |
| 7 | Admin | View users, jobs, reports |
| 8 | Admin | Ban/unban a user |
| 9 | Admin | Delete user/job |
| 10 | Admin | View audit logs |
=======
## Main Application Class

```java
@SpringBootApplication
@EnableFeignClients   // Activates all @FeignClient interfaces
public class JpmsAdminServiceApplication { ... }
```

**Annotations:**
- `@SpringBootApplication` — enables auto-configuration, component scan, and configuration.
- `@EnableFeignClients` — scans and activates `AuthServiceClient`, `AdminJobClient`, and `AdminAppClient`.

> Note: `@EnableDiscoveryClient` is not explicitly present but Eureka client is active via `spring-cloud-starter-netflix-eureka-client` on the classpath.

---

## Entity — AuditLog

**Table:** `audit_logs`

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String action;          // e.g., "DELETE_USER", "BAN_USER", "DELETE_JOB"

    @Column(name = "performed_by", nullable = false, length = 150)
    private String performedBy;     // e.g., "admin:42"

    @Column(columnDefinition = "TEXT")
    private String details;         // Human-readable description

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(String action, String performedBy, String details) { ... }
}
```

**Key JPA Annotations:**
- `@PrePersist` — automatically sets `createdAt` timestamp when the record is first persisted.
- `@Column(columnDefinition = "TEXT")` — allows long detail strings.

**Audit Actions Recorded:**
| Action | Trigger |
|---|---|
| `DELETE_USER` | Admin deletes a user |
| `BAN_USER` | Admin bans a user |
| `UNBAN_USER` | Admin unbans a user |
| `DELETE_JOB` | Admin deletes a job |

---

## DTOs

### UserResponse
Mirrors the user data returned by AuthService's internal API. Contains id, name, email, role, status, profilePictureUrl, companyName, createdAt.

### JobResponse
Mirrors the job data returned by JobService's internal API. Contains id, title, companyName, location, status, postedBy, createdAt.

### ApplicationStats
```java
public class ApplicationStats {
    int totalApplications;
    long appliedCount, underReviewCount, shortlistedCount, rejectedCount;
}
```

### PlatformReport
```java
public class PlatformReport {
    int totalUsers;
    int totalJobs;
    ApplicationStats applicationStats;
    List<UserResponse> users;
    List<JobResponse> jobs;
}
```
Aggregated report combining data from all three downstream services.

---

## Repository — AuditLogRepository

```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Uses standard JpaRepository methods: findAll(), save()
}
```

---

## Feign Clients

### AuthServiceClient
```java
@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/internal/users")
    List<UserResponse> getAllUsers();

    @DeleteMapping("/api/internal/users/{id}")
    void deleteUser(@PathVariable Long id);

    @PutMapping("/api/internal/users/{id}/ban")
    void banUser(@PathVariable Long id);

    @PutMapping("/api/internal/users/{id}/unban")
    void unbanUser(@PathVariable Long id);

    @PutMapping("/api/internal/users/{id}/invalidate-token")
    void invalidateToken(@PathVariable Long id);
}
```
Communicates with AuthService's `InternalAuthController`. All calls go to `/api/internal/**` endpoints which are blocked from external access by the Gateway.

### AdminJobClient
```java
@FeignClient(name = "job-service")
public interface AdminJobClient {
    @GetMapping("/api/internal/jobs/all")
    List<JobResponse> getAllJobs();

    @DeleteMapping("/api/internal/jobs/{id}")
    void deleteJob(@PathVariable Long id);
}
```
Communicates with JobService's `InternalJobController`.

### AdminAppClient
```java
@FeignClient(name = "application-service")
public interface AdminAppClient {
    @GetMapping("/api/internal/applications/stats")
    ApplicationStats getStats();
}
```
Communicates with ApplicationService's `InternalApplicationController` to fetch aggregated stats for the platform report.

---

## Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // CSRF disabled, stateless sessions
    // All requests permitted — role check done manually in controller
}
```

Role validation is done manually in the controller via the `assertAdmin(role)` helper method rather than Spring Security annotations, since the role comes from the `X-User-Role` header injected by the Gateway.

---

## Service — AdminService

### `getAllUsers()`
Calls `AuthServiceClient.getAllUsers()` and returns the list. Logs total count.

### `deleteUser(id, adminId)`
1. Calls `AuthServiceClient.deleteUser(id)`.
2. Saves `AuditLog("DELETE_USER", "admin:" + adminId, "Deleted user ID: " + id)`.

### `banUser(id, adminId)`
1. Prevents admin from banning themselves (throws `IllegalArgumentException`).
2. Calls `AuthServiceClient.banUser(id)`.
3. Attempts `AuthServiceClient.invalidateToken(id)` — logs warning if it fails but continues.
4. Saves `AuditLog("BAN_USER", ...)`.

### `unbanUser(id, adminId)`
1. Calls `AuthServiceClient.unbanUser(id)`.
2. Saves `AuditLog("UNBAN_USER", ...)`.

### `getAllJobs()`
Calls `AdminJobClient.getAllJobs()` and returns the list.

### `deleteJob(id, adminId)`
1. Calls `AdminJobClient.deleteJob(id)`.
2. Saves `AuditLog("DELETE_JOB", ...)`.

### `getReport()`
1. Fetches users from AuthService.
2. Fetches jobs from JobService.
3. Fetches application stats from ApplicationService.
4. Assembles and returns `PlatformReport`.

### `getAuditLogs()`
Returns all records from `AuditLogRepository.findAll()`.

---

## Controller — AdminController

**Base path:** `/api/admin`

All endpoints read `X-User-Role` from the request header and call `assertAdmin(role)` before proceeding. If the role is not `ADMIN`, an `AccessDeniedException` is thrown.

```java
private void assertAdmin(String role) {
    if (role == null || !role.equalsIgnoreCase("ADMIN")) {
        throw new AccessDeniedException("Access denied. ADMIN role required.");
    }
}
```

| Method | Endpoint | Headers Required | Description |
|---|---|---|---|
| `GET` | `/api/admin/users` | `X-User-Role` | Get all users |
| `DELETE` | `/api/admin/users/{id}` | `X-User-Id`, `X-User-Role` | Delete user by ID |
| `PUT` | `/api/admin/users/{id}/ban` | `X-User-Id`, `X-User-Role` | Ban user |
| `PUT` | `/api/admin/users/{id}/unban` | `X-User-Id`, `X-User-Role` | Unban user |
| `GET` | `/api/admin/jobs` | `X-User-Role` | Get all jobs |
| `DELETE` | `/api/admin/jobs/{id}` | `X-User-Id`, `X-User-Role` | Delete job by ID |
| `GET` | `/api/admin/reports` | `X-User-Role` | Get platform report |
| `GET` | `/api/admin/audit-logs` | `X-User-Role` | Get all audit logs |

**Key annotations:**
- `@Tag(name = "Admin APIs")` — Swagger grouping.
- `@Operation(summary = "...")` — Swagger endpoint description.
- `@Parameter(description = "...")` — Swagger parameter documentation.
- `@RequestHeader("X-User-Role")` — reads role injected by API Gateway.
- `@RequestHeader("X-User-Id")` — reads admin ID for audit logging.

---

## Exception Handling

### GlobalExceptionHandler (`@ControllerAdvice`)
Catches and maps exceptions to HTTP responses:
- `AccessDeniedException` → `403 Forbidden`
- `ResourceNotFoundException` → `404 Not Found`
- General `Exception` → `500 Internal Server Error`

All error responses use the `ErrorResponse` DTO: `{ timestamp, status, error, message }`.

---

## Configuration

### application.yml (key settings)

```yaml
server.port: 8084
spring.application.name: admin-service
spring.datasource.url: jdbc:mysql://jobportal-db:3306/jobportal_db
# RabbitMQ excluded — AdminService does not use messaging
spring.autoconfigure.exclude:
  - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
eureka.client.service-url.defaultZone: http://eureka-server:8761/eureka/
```

All sensitive values (DB credentials) are injected from environment variables.
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
