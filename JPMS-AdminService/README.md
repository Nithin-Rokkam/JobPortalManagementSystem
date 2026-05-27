# JPMS — Admin Service

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
```

---

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
