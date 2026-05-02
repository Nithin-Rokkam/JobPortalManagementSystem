# JPMS — Job Service

**Port:** `8082` | **Spring Application Name:** `job-service`

The Job Service manages the complete lifecycle of job postings on the Joblix platform. It handles job creation by recruiters, public browsing and search with pagination, job updates and soft-deletes, and exposes internal APIs for admin-level operations. When a job is posted, it publishes a `JobPostedEvent` to RabbitMQ so the Notification Service can alert all job seekers.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Main Application Class](#main-application-class)
4. [Entity — Job](#entity--job)
5. [Enums](#enums)
6. [DTOs](#dtos)
7. [Repository — JobRepository](#repository--jobrepository)
8. [Security](#security)
9. [Service — JobService](#service--jobservice)
10. [Controllers](#controllers)
11. [RabbitMQ Events Published](#rabbitmq-events-published)
12. [Configuration](#configuration)
13. [API Reference](#api-reference)

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Messaging | RabbitMQ (AMQP) |
| Service Discovery | Netflix Eureka Client |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Logging | Log4j2 |
| Build | Maven |

---

## Project Structure

```
JPMS-JobService/src/main/java/com/capg/jobportal/
├── JpmsJobServiceApplication.java        ← Entry point
├── config/
│   ├── RabbitMQConfig.java               ← Exchange, queue, binding declarations
│   └── SwaggerConfig.java                ← OpenAPI configuration
├── controller/
│   ├── JobController.java                ← Public + recruiter job APIs
│   └── InternalJobController.java        ← Internal APIs for AdminService
├── dto/
│   ├── ErrorResponse.java                ← Standardized error wrapper
│   ├── JobRequestDTO.java                ← Job creation/update payload
│   ├── JobResponseDTO.java               ← Job response payload
│   └── PagedResponse.java                ← Generic pagination wrapper
├── entity/
│   └── Job.java                          ← JPA entity mapped to `jobs` table
├── enums/
│   ├── JobStatus.java                    ← ACTIVE | CLOSED | DRAFT | DELETED
│   └── JobType.java                      ← FULL_TIME | PART_TIME | REMOTE | CONTRACT
├── event/
│   └── JobPostedEvent.java               ← RabbitMQ event published on job creation
├── Exceptions/
│   ├── ForbiddenException.java
│   ├── GlobalExceptionHandler.java       ← @ControllerAdvice error handler
│   ├── InvalidJobTypeException.java
│   └── ResourceNotFoundException.java
├── repository/
│   └── JobRepository.java                ← JPA repository with custom queries
├── security/
│   └── SecurityConfig.java               ← Stateless Spring Security config
└── service/
    └── JobService.java                   ← All business logic
```

---

## Main Application Class

```java
@SpringBootApplication
@EnableDiscoveryClient   // Registers with Eureka Server
public class JpmsJobServiceApplication { ... }
```

**Annotations:**
- `@SpringBootApplication` — enables auto-configuration, component scan, and configuration.
- `@EnableDiscoveryClient` — registers this service with Eureka as `job-service`.

---

## Entity — Job

**Table:** `jobs`

```java
@Entity
@Table(name = "jobs")
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "company_name", length = 150, nullable = false)
    private String companyName;

    @Column(name = "location", length = 150, nullable = false)
    private String location;

    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 20, nullable = false)
    private JobType jobType;

    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private JobStatus status = JobStatus.ACTIVE;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "posted_by")
    private Long postedBy;                  // FK → User.id (RECRUITER)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist   // Sets createdAt, updatedAt, and default status on insert
    @PreUpdate    // Updates updatedAt on every save
}
```

**Key JPA Annotations:**
- `@Enumerated(EnumType.STRING)` — stores enum as readable string in DB.
- `@Column(columnDefinition = "TEXT")` — maps to MySQL TEXT type for long content.
- `@Column(precision = 12, scale = 2)` — maps salary to `DECIMAL(12,2)`.
- `@PrePersist` / `@PreUpdate` — lifecycle callbacks for automatic timestamp management.

---

## Enums

### JobType
```java
public enum JobType {
    FULL_TIME, PART_TIME, REMOTE, CONTRACT
}
```

### JobStatus
```java
public enum JobStatus {
    ACTIVE, CLOSED, DRAFT, DELETED
}
```
`DELETED` is used for soft deletes — the record remains in the DB but is excluded from public queries.

---

## DTOs

### JobRequestDTO
```java
@Data @NoArgsConstructor
public class JobRequestDTO {
    String title;
    String companyName;
    String location;
    BigDecimal salary;
    Integer experienceYears;
    String jobType;          // String input, converted to JobType enum in service
    String skillsRequired;
    String description;
    String status;
    LocalDate deadline;
}
```

### JobResponseDTO
```java
@Data @NoArgsConstructor
public class JobResponseDTO {
    Long id;
    String title;
    String companyName;
    String location;
    BigDecimal salary;
    Integer experienceYears;
    String jobType;
    String skillsRequired;
    String description;
    String status;
    LocalDate deadline;
    Long postedBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### PagedResponse\<T\>
Generic wrapper for paginated results:
```java
public class PagedResponse<T> {
    List<T> content;
    int currentPage;
    int totalPages;
    long totalElements;
    boolean last;
}
```

---

## Repository — JobRepository

```java
public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findByStatusNot(JobStatus status, Pageable pageable);
    Optional<Job> findByIdAndStatusNot(Long id, JobStatus status);
    Page<Job> findByPostedByAndStatusNot(Long postedBy, JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE ...")
    Page<Job> searchJobs(String title, String location,
                         JobType jobType, Integer experienceYears,
                         Pageable pageable);
}
```

- `findByStatusNot(DELETED)` — excludes soft-deleted jobs from public listings.
- `findByIdAndStatusNot` — fetches a single job excluding deleted ones.
- `findByPostedByAndStatusNot` — recruiter's own jobs excluding deleted.
- `searchJobs` — custom JPQL query with optional filters (null-safe with `IS NULL OR` conditions).

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

The service trusts `X-User-Id` and `X-User-Role` headers injected by the Gateway. No JWT parsing happens here.

---

## Service — JobService

### `postJob(JobRequestDTO, postedBy, userRole)`
1. Validates role is `RECRUITER` — throws `ForbiddenException` otherwise.
2. Converts DTO to `Job` entity via `convertToEntity()`.
3. Sets `postedBy` and `status = ACTIVE`.
4. Saves to DB.
5. Builds `JobPostedEvent` and publishes to RabbitMQ exchange with `job.posted` routing key.
6. Returns `JobResponseDTO`.

### `getAllJobs(page, size)`
Fetches all non-deleted jobs sorted by `createdAt` descending. Returns `PagedResponse<JobResponseDTO>`.

### `getJobById(id)`
Fetches a single job by ID, excluding deleted ones. Throws `ResourceNotFoundException` if not found.

### `searchJobs(title, location, jobType, experienceYears, page, size)`
- Converts `jobType` string to `JobType` enum (throws `InvalidJobTypeException` on invalid value).
- Delegates to `JobRepository.searchJobs()` with null-safe optional filters.
- Returns paginated results.

### `updateJob(id, JobRequestDTO, currentUserId, currentUserRole)`
1. Validates role is `RECRUITER`.
2. Fetches job, validates ownership (`postedBy == currentUserId`).
3. Updates all mutable fields.
4. Saves and returns updated DTO.

### `deleteJob(id, userId, role)`
Soft delete — sets `status = DELETED`. Validates recruiter role and ownership.

### `getMyJobs(userId, role, page, size)`
Returns paginated jobs posted by the recruiter, excluding deleted ones.

### `getAllJobsForAdmin()` / `deleteJobByAdmin(id)`
Admin-only operations. `getAllJobsForAdmin()` returns all jobs including deleted ones. `deleteJobByAdmin()` soft-deletes without ownership check.

### Private Helpers
- `convertToEntity(JobRequestDTO)` — maps DTO fields to `Job` entity.
- `convertToResponseDTO(Job)` — maps entity to `JobResponseDTO`.
- `buildPagedResponse(Page<Job>)` — wraps Spring Data `Page` into `PagedResponse`.

---

## Controllers

### JobController — `/api/jobs/**`

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/jobs` | JWT | RECRUITER | Post a new job |
| `GET` | `/api/jobs` | Public | Any | Get all jobs (paginated) |
| `GET` | `/api/jobs/{id}` | Public | Any | Get job by ID |
| `GET` | `/api/jobs/search` | Public | Any | Search jobs with filters |
| `PUT` | `/api/jobs/{id}` | JWT | RECRUITER | Update own job |
| `DELETE` | `/api/jobs/{id}` | JWT | RECRUITER | Soft-delete own job |
| `GET` | `/api/jobs/my-jobs` | JWT | RECRUITER | Get recruiter's own jobs |

**Key annotations:**
- `@Tag(name = "Job APIs")` — Swagger grouping.
- `@Operation(summary = "...")` — Swagger endpoint description.
- `@RequestHeader("X-User-Id")` / `@RequestHeader("X-User-Role")` — reads Gateway-injected headers.
- `@RequestParam(defaultValue = "0")` — pagination defaults.

### InternalJobController — `/api/internal/**`

Annotated with `@Hidden` (excluded from Swagger). Blocked from external access by the Gateway.

| Method | Endpoint | Used By | Description |
|---|---|---|---|
| `GET` | `/api/internal/jobs/all` | AdminService | Fetch all jobs (including deleted) |
| `DELETE` | `/api/internal/jobs/{id}` | AdminService | Admin soft-delete any job |

---

## RabbitMQ Events Published

### JobPostedEvent

```java
@Data @NoArgsConstructor
public class JobPostedEvent {
    Long jobId;
    Long recruiterId;
    String title;
    String companyName;
    String location;
    String jobType;
    BigDecimal salary;
    Integer experienceYears;
    String description;
}
```

| Exchange | Routing Key | Queue (consumed by) |
|---|---|---|
| `jobportal.exchange` | `job.posted` | NotificationService → `job.posted.queue` |

**Flow:** After a job is saved to DB, `JobService.postJob()` builds this event and calls `rabbitTemplate.convertAndSend(exchange, routingKey, event)`. The NotificationService receives it and sends confirmation email to the recruiter + job alert emails to all active job seekers.

---

## Configuration

### application.yml (key settings)

```yaml
server.port: 8082
spring.application.name: job-service
spring.datasource.url: jdbc:mysql://jobportal-db:3306/jobportal_db
spring.rabbitmq.host: rabbitmq
rabbitmq.exchange: jobportal.exchange
rabbitmq.routing-key: job.applied    # Note: used for job.posted routing
eureka.client.service-url.defaultZone: http://eureka-server:8761/eureka/
```

All sensitive values (DB credentials) are injected from environment variables.
