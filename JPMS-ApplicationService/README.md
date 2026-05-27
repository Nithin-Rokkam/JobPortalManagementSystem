# JPMS — Application Service

**Port:** `8083` | **Spring Application Name:** `application-service`

The Application Service manages the complete job application lifecycle on the Joblix platform. It handles job seekers applying for jobs (with resume upload or reuse), application status tracking, recruiter-side applicant management, and status transitions with email notifications via RabbitMQ. It communicates with both the Auth Service and Job Service via Feign clients.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Main Application Class](#main-application-class)
4. [Entity — Application](#entity--application)
5. [Enums](#enums)
6. [DTOs](#dtos)
7. [Repository — ApplicationRepository](#repository--applicationrepository)
8. [Feign Clients](#feign-clients)
9. [Security](#security)
10. [Service — ApplicationService](#service--applicationservice)
11. [Controllers](#controllers)
12. [RabbitMQ Events Published](#rabbitmq-events-published)
13. [Configuration](#configuration)
14. [API Reference](#api-reference)

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| File Storage | Cloudinary |
| Messaging | RabbitMQ (AMQP) |
| Inter-Service | OpenFeign |
| Service Discovery | Netflix Eureka Client |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Logging | Log4j2 |
| Build | Maven |

---

## Project Structure

```
JPMS-ApplicationService/src/main/java/com/capg/jobportal/
├── JpmsApplicationServiceApplication.java  ← Entry point
├── client/
│   ├── AuthServiceClient.java              ← Feign client → AuthService
│   └── JobServiceClient.java               ← Feign client → JobService
├── config/
│   ├── RabbitMQConfig.java                 ← Exchange, queue, binding declarations
│   └── SwaggerConfig.java                  ← OpenAPI configuration
├── controller/
│   ├── ApplicationController.java          ← Seeker + recruiter application APIs
│   └── InternalApplicationController.java  ← Internal stats API for AdminService
├── dao/
│   └── ApplicationRepository.java          ← JPA repository
├── dto/
│   ├── ApplicationResponse.java            ← Application response DTO
│   ├── ApplicationStats.java               ← Aggregated stats DTO
│   ├── ErrorResponse.java                  ← Standardized error wrapper
│   ├── JobClientResponse.java              ← Job data received from JobService
│   ├── RecruiterApplicationResponse.java   ← Applicant view for recruiter
│   ├── StatusUpdateRequest.java            ← Status change request DTO
│   └── UserInfoResponse.java               ← User info from AuthService
├── entity/
│   └── Application.java                    ← JPA entity mapped to `applications` table
├── enums/
│   └── ApplicationStatus.java              ← APPLIED | UNDER_REVIEW | SHORTLISTED | SELECTED | REJECTED
├── event/
│   ├── ApplicationStatusChangedEvent.java  ← RabbitMQ event for status changes
│   └── JobAppliedEvent.java                ← RabbitMQ event when seeker applies
├── exception/
│   ├── DuplicateApplicationException.java
│   ├── ForbiddenException.java
│   ├── GlobalExceptionHandler.java         ← @ControllerAdvice error handler
│   ├── InvalidStatusTransitionException.java
│   └── ResourceNotFoundException.java
├── security/
│   └── SecurityConfig.java                 ← Stateless Spring Security config
├── service/
│   └── ApplicationService.java             ← All business logic
└── util/
    └── CloudinaryUtil.java                 ← Cloudinary resume upload helper
```

---

## Main Application Class

```java
@SpringBootApplication
@EnableDiscoveryClient   // Registers with Eureka Server
@EnableFeignClients      // Enables OpenFeign inter-service clients
public class JpmsApplicationServiceApplication { ... }
```

**Annotations:**
- `@EnableDiscoveryClient` — registers as `application-service` in Eureka.
- `@EnableFeignClients` — scans and activates all `@FeignClient` interfaces.

---

## Entity — Application

**Table:** `applications`

```java
@Entity
@Table(
    name = "applications",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "job_id"},
        name = "uk_user_job"
    )
)
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;                    // FK → User.id (JOB_SEEKER)

    @Column(name = "job_id", nullable = false)
    private Long jobId;                     // FK → Job.id

    @Column(name = "resume_url", nullable = false, columnDefinition = "TEXT")
    private String resumeUrl;               // Cloudinary URL

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "recruiter_note", columnDefinition = "TEXT")
    private String recruiterNote;

    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist   // Sets appliedAt and updatedAt on insert
    @PreUpdate    // Updates updatedAt on every save
}
```

**Key JPA Annotations:**
- `@UniqueConstraint(columnNames = {"user_id", "job_id"})` — prevents a seeker from applying to the same job twice at the DB level.
- `@Column(updatable = false)` on `appliedAt` — ensures the application timestamp is never modified after creation.
- `@Enumerated(EnumType.STRING)` — stores status as readable string.

---

## Enums

### ApplicationStatus
```java
public enum ApplicationStatus {
    APPLIED, UNDER_REVIEW, SHORTLISTED, SELECTED, REJECTED
}
```

**Valid Status Transitions:**
```
APPLIED → UNDER_REVIEW
UNDER_REVIEW → SHORTLISTED | REJECTED
SHORTLISTED → SELECTED | REJECTED
SELECTED → REJECTED
REJECTED → (terminal — no further transitions)
```

---

## DTOs

### ApplicationResponse
```java
@Data @NoArgsConstructor
public class ApplicationResponse {
    Long id, userId, jobId;
    String resumeUrl, coverLetter;
    ApplicationStatus status;
    LocalDateTime appliedAt, updatedAt;

    public static ApplicationResponse fromEntity(Application app) { ... }
}
```
Static factory method maps entity to DTO.

### RecruiterApplicationResponse
Extended response for recruiter view — includes `seekerName` and `seekerEmail` fetched from AuthService.

### StatusUpdateRequest
```java
public class StatusUpdateRequest {
    @NotNull ApplicationStatus newStatus;
    String recruiterNote;   // Optional feedback
}
```

### ApplicationStats
```java
public class ApplicationStats {
    int totalApplications;
    long appliedCount, underReviewCount, shortlistedCount, rejectedCount;
}
```

### JobClientResponse
Mirrors the job data returned by JobService's `GET /api/jobs/{id}` — includes `id`, `title`, `companyName`, `status`, `deadline`, `postedBy`.

---

## Repository — ApplicationRepository

```java
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);
    List<Application> findByJobId(Long jobId);
    Optional<Application> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
}
```

- `findByUserId` — seeker's own applications.
- `findByJobId` — all applicants for a specific job.
- `findByIdAndUserId` — fetch a specific application ensuring it belongs to the seeker.
- `existsByUserIdAndJobId` — duplicate application check.

---

## Feign Clients

### AuthServiceClient
```java
@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/internal/users/{id}/info")
    UserInfoResponse getUserInfo(@PathVariable("id") Long userId);

    @PutMapping("/api/internal/users/{seekerId}/selected-company")
    void updateSelectedByCompany(@PathVariable Long seekerId,
                                  @RequestBody Map<String, String> body);
}
```
- `getUserInfo` — fetches seeker name and email for notification events and recruiter view.
- `updateSelectedByCompany` — called when a seeker's status is set to `SELECTED`, stores the company name on the seeker's profile.

### JobServiceClient
```java
@FeignClient(name = "job-service")
public interface JobServiceClient {
    @GetMapping("/api/jobs/{id}")
    JobClientResponse getJobById(
        @PathVariable("id") Long id,
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-User-Role") String role
    );
}
```
Used to validate job existence, status, deadline, and recruiter ownership before processing applications.

---

## Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // CSRF disabled, stateless sessions
    // All requests permitted — auth handled at API Gateway
}
```

---

## Service — ApplicationService

### `applyForJob(jobId, coverLetter, useExistingResume, existingResumeUrl, resumeFile, seekerId)`
1. Fetches job via `JobServiceClient.getJobById()`.
2. Validates job is not `DELETED` or `CLOSED`.
3. Validates application deadline has not passed.
4. Checks for duplicate application via `existsByUserIdAndJobId()`.
5. Handles resume: uses existing URL or uploads new file to Cloudinary.
6. Creates and saves `Application` entity with status `APPLIED`.
7. Fetches seeker info from AuthService.
8. Publishes `JobAppliedEvent` to RabbitMQ (notifies recruiter).

### `getMyApplications(seekerId)`
Returns all applications for the seeker as `List<ApplicationResponse>`.

### `getApplicationById(id, seekerId)`
Fetches application by ID, validates it belongs to the requesting seeker. Throws `ForbiddenException` if not.

### `getApplicantsForJob(jobId, recruiterId)`
1. Fetches job, validates recruiter ownership.
2. Fetches all applications for the job.
3. For each application, fetches seeker info from AuthService and enriches the response.

### `updateApplicationStatus(id, StatusUpdateRequest, recruiterId)`
1. Fetches application and validates recruiter owns the job.
2. Calls `validateStatusTransition()` to enforce the state machine.
3. Updates status and optional recruiter note.
4. If new status is `SHORTLISTED`, `SELECTED`, or `REJECTED` → publishes `ApplicationStatusChangedEvent` to RabbitMQ.
5. If new status is `SELECTED` → calls `AuthServiceClient.updateSelectedByCompany()` to record the company on the seeker's profile.

### `validateStatusTransition(current, next)`
Enforces the valid transition rules. Throws `InvalidStatusTransitionException` for invalid moves.

### `getApplicationStats()`
Iterates all applications and counts by status. Returns `ApplicationStats` for admin reporting.

### `deleteApplication(id, recruiterId)`
Validates recruiter owns the job, then permanently deletes the application record.

---

## Controllers

### ApplicationController — `/api/applications/**`

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/applications` | JWT | JOB_SEEKER | Apply for a job (multipart) |
| `GET` | `/api/applications/my-applications` | JWT | JOB_SEEKER | Get own applications |
| `GET` | `/api/applications/{id}` | JWT | JOB_SEEKER | Get specific application |
| `GET` | `/api/applications/job/{jobId}` | JWT | RECRUITER | Get all applicants for a job |
| `PATCH` | `/api/applications/{id}/status` | JWT | RECRUITER | Update application status |
| `DELETE` | `/api/applications/{id}` | JWT | RECRUITER | Delete an application |

**Key annotations:**
- `@Tag(name = "Application APIs")` — Swagger grouping.
- `@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)` — accepts multipart form data for resume upload.
- `@RequestParam` — reads individual form fields from multipart request.
- `@RequestHeader("X-User-Id")` / `@RequestHeader("X-User-Role")` — Gateway-injected user context.
- `@Valid` on `@RequestBody` — triggers Bean Validation on `StatusUpdateRequest`.

### InternalApplicationController — `/api/internal/**`

Annotated with `@Hidden`. Blocked from external access by the Gateway.

| Method | Endpoint | Used By | Description |
|---|---|---|---|
| `GET` | `/api/internal/applications/stats` | AdminService | Aggregated application statistics |

---

## RabbitMQ Events Published

### JobAppliedEvent
```java
@Data @NoArgsConstructor
public class JobAppliedEvent {
    Long jobId;
    String jobTitle;
    Long seekerId;
    String seekerName;
    String seekerEmail;
    Long recruiterId;
}
```

| Exchange | Routing Key | Consumed By |
|---|---|---|
| `jobportal.exchange` | `job.applied` | NotificationService → `job.applied.queue` |

**Trigger:** Published after a seeker successfully applies. NotificationService sends an email alert to the recruiter.

### ApplicationStatusChangedEvent
```java
public class ApplicationStatusChangedEvent {
    Long applicationId, jobId, seekerId;
    String jobTitle, seekerName, seekerEmail, newStatus;
}
```

| Exchange | Routing Key | Consumed By |
|---|---|---|
| `jobportal.exchange` | `application.status.changed` | NotificationService → status queue |

**Trigger:** Published when status changes to `SHORTLISTED`, `SELECTED`, or `REJECTED`. NotificationService sends the appropriate email to the seeker.

---

## Configuration

### application.yml (key settings)

```yaml
server.port: 8083
spring.application.name: application-service
spring.datasource.url: jdbc:mysql://jobportal-db:3306/jobportal_db
spring.rabbitmq.host: rabbitmq
rabbitmq.exchange: jobportal.exchange
rabbitmq.routing-key: job.applied
rabbitmq.status-routing-key: application.status.changed
spring.servlet.multipart.max-file-size: 10MB
cloudinary.cloud-name: ${APPLICATION_SERVICE_CLOUDINARY_CLOUD_NAME}
eureka.client.service-url.defaultZone: http://eureka-server:8761/eureka/
```
