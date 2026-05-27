# Low Level Design (LLD) — Joblix Job Portal Management System

---

## 1. Module Breakdown

### 1.1 Auth Service

| Class | Package | Key Responsibilities |
|---|---|---|
| `AuthController` | controller | 12 endpoints: register, verify-registration, resend-registration-otp, login, refresh, logout, update-picture, update-resume, get-profile, update-company-name, forgot-password, reset-password |
| `InternalAuthController` | controller | 7 endpoints (Feign-only): getAllUsers, deleteUser, banUser, unbanUser, invalidateToken, getJobSeekerEmails, getUserInfo, updateSelectedByCompany |
| `AuthService` | service | 16 methods; uses `TransactionSynchronizationManager.afterCommit()` for OTP event publishing |
| `JwtUtil` | security | generateAccessToken(userId, role), generateRefreshToken(), isTokenValid(), extractUserId(), extractRole() |
| `CloudinaryUtil` | util | uploadProfilePicture(MultipartFile), uploadResume(MultipartFile) |
| `User` | entity | 18 columns; `@PrePersist` sets createdAt; `@PreUpdate` sets updatedAt |
| `UserRepository` | dao | findByEmail, findByRefreshToken, findByRole, findById |
| `GlobalExceptionHandler` | exception | Maps: UserAlreadyExistsException to 409, ResourceNotFoundException to 404, IllegalArgumentException to 400 |

### 1.2 Job Service

| Class | Package | Key Responsibilities |
|---|---|---|
| `JobController` | controller | 7 endpoints: POST /api/jobs, GET /api/jobs, GET /api/jobs/{id}, GET /api/jobs/search, PUT /api/jobs/{id}, DELETE /api/jobs/{id}, GET /api/jobs/my-jobs |
| `InternalJobController` | controller | 2 endpoints (Feign-only): GET /api/internal/jobs/all, DELETE /api/internal/jobs/{id} |
| `JobService` | service | 8 methods; `postJob()` does synchronous RabbitMQ publish after `jobRepository.save()` |
| `JobRepository` | repository | findByStatusNot(DELETED, pageable), findByIdAndStatusNot(id, DELETED), findByPostedByAndStatusNot, searchJobs, findAll |

### 1.3 Application Service

| Class | Package | Key Responsibilities |
|---|---|---|
| `ApplicationController` | controller | 6 endpoints: POST, GET my, GET by id, GET by job, PATCH status, DELETE |
| `InternalApplicationController` | controller | 1 endpoint (Feign-only): GET /api/internal/applications/stats |
| `ApplicationService` | service | 6 methods; notification failure is silently caught; `getApplicationStats()` omits SELECTED |
| `ApplicationRepository` | dao | existsByUserIdAndJobId, findByUserId, findByJobId, findByIdAndUserId, findAll |
| `AuthServiceClient` | client | getUserInfo(userId), updateSelectedByCompany(seekerId, Map body) |
| `JobServiceClient` | client | getJobById(id, X-User-Id header, X-User-Role header) — calls /api/jobs/{id} public endpoint |

### 1.4 Admin Service

| Class | Package | Key Responsibilities |
|---|---|---|
| `AdminController` | controller | 8 endpoints; all validate `assertAdmin(role)` at controller level |
| `AdminService` | service | Orchestrates Feign calls; writes AuditLog; banUser() has self-ban check |
| `AuditLogRepository` | repository | Standard JPA findAll(); no custom queries |
| `AuthServiceClient` | client | getAllUsers(), deleteUser(id), banUser(id), unbanUser(id), invalidateToken(id) |
| `AdminJobClient` | client | getAllJobs() to /api/internal/jobs/all, deleteJob(id) to DELETE /api/internal/jobs/{id} |
| `AdminAppClient` | client | getStats() to /api/internal/applications/stats |

### 1.5 Notification Service

| Class | Package | Key Responsibilities |
|---|---|---|
| `JobPostedListener` | listener | job.posted.queue; fetches recruiter via Feign, sends confirmation; fetches all seeker emails, sends job alert |
| `JobAppliedListener` | listener | job.applied.queue; fetches recruiter via Feign, sends application alert |
| `ApplicationStatusChangedListener` | listener | application.status.queue; routes to sendShortlistedEmail, sendRejectedEmail, or sendSelectedEmail |
| `PasswordResetListener` | listener | password.reset.queue; calls emailService.sendPasswordResetEmail() |
| `RegistrationOtpListener` | listener | registration.otp.queue; calls emailService.sendRegistrationOtpEmail() |
| `EmailService` | service | JavaMailSender with HTML-formatted emails per event type |
| `AuthServiceClient` | client | getUserInfo(id), getJobSeekerEmails() |

---

## 2. Class Diagram (Core Domain)

```mermaid
classDiagram
    class User {
        +Long id
        +String name
        +String email
        +String password
        +Role role
        +String phone
        +UserStatus status
        +String profilePictureUrl
        +String resumeUrl
        +String refreshToken
        +String resetPasswordOtp
        +LocalDateTime otpExpiryTime
        +String emailVerificationOtp
        +LocalDateTime emailVerificationExpiry
        +String companyName
        +String selectedByCompany
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class Role {
        <<enumeration>>
        JOB_SEEKER
        RECRUITER
        ADMIN
    }
    class UserStatus {
        <<enumeration>>
        ACTIVE
        BANNED
        PENDING_VERIFICATION
    }

    class Job {
        +Long id
        +String title
        +String companyName
        +String location
        +BigDecimal salary
        +Integer experienceYears
        +JobType jobType
        +String skillsRequired
        +String description
        +JobStatus status
        +LocalDate deadline
        +Long postedBy
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class JobType {
        <<enumeration>>
        FULL_TIME
        PART_TIME
        REMOTE
        CONTRACT
    }
    class JobStatus {
        <<enumeration>>
        ACTIVE
        CLOSED
        DRAFT
        DELETED
    }

    class Application {
        +Long id
        +Long userId
        +Long jobId
        +String resumeUrl
        +String coverLetter
        +ApplicationStatus status
        +String recruiterNote
        +LocalDateTime appliedAt
        +LocalDateTime updatedAt
    }
    class ApplicationStatus {
        <<enumeration>>
        APPLIED
        UNDER_REVIEW
        SHORTLISTED
        SELECTED
        REJECTED
    }

    class AuditLog {
        +Long id
        +String action
        +String performedBy
        +String details
        +LocalDateTime createdAt
    }

    User --> Role
    User --> UserStatus
    Job --> JobType
    Job --> JobStatus
    Application --> ApplicationStatus
```

---

## 3. Application Status State Machine

```mermaid
stateDiagram-v2
    direction LR

    [*] --> APPLIED : Seeker submits application

    APPLIED --> UNDER_REVIEW : Recruiter opens review\n[no email sent]

    UNDER_REVIEW --> SHORTLISTED : Candidate shortlisted\n[email sent to seeker]
    UNDER_REVIEW --> REJECTED : Not suitable\n[email sent to seeker]

    SHORTLISTED --> SELECTED : Offer extended\n[email sent + company saved on profile]
    SHORTLISTED --> REJECTED : Did not proceed\n[email sent to seeker]

    SELECTED --> REJECTED : Offer rescinded\n[email sent to seeker]

    REJECTED --> [*] : Terminal state\n[no further transitions allowed]
```

---

## 4. Sequence Diagram — Apply for Job

```mermaid
sequenceDiagram
    participant FE as Angular Frontend
    participant GW as API Gateway
    participant APP as Application Service
    participant JOB as Job Service
    participant AUTH as Auth Service
    participant CLD as Cloudinary
    participant RMQ as RabbitMQ

    FE->>GW: POST /api/applications\nAuthorization: Bearer JWT

    GW->>GW: GatewayJwtFilter validates JWT
    GW->>GW: Extracts userId=55, role=JOB_SEEKER
    GW->>GW: Injects X-User-Id: 55, X-User-Role: JOB_SEEKER
    GW->>APP: Forward request

    APP->>JOB: Feign GET /api/jobs/{jobId}\nX-User-Id: 55, X-User-Role: JOB_SEEKER
    JOB-->>APP: JobClientResponse with status, deadline, postedBy

    APP->>APP: Validate job not null
    APP->>APP: Validate status not DELETED or CLOSED
    APP->>APP: Validate deadline not before today
    APP->>APP: Check existsByUserIdAndJobId(55, jobId)
    APP->>APP: If duplicate, throw DuplicateApplicationException 409

    alt useExistingResume is false
        APP->>CLD: cloudinaryUtil.uploadResume(resumeFile)
        CLD-->>APP: Cloudinary resume URL
    else useExistingResume is true
        APP->>APP: Use provided existingResumeUrl
    end

    APP->>APP: Create and save Application with status APPLIED

    APP->>AUTH: Feign GET /api/internal/users/55/info
    AUTH-->>APP: UserInfoResponse with name and email

    APP->>RMQ: convertAndSend JobAppliedEvent
    Note over APP,RMQ: If this fails, WARN log only\nApplication already saved successfully

    APP-->>GW: 201 ApplicationResponse
    GW-->>FE: 201 Created

    Note over RMQ: Async delivery to job.applied.queue
```

---

## 5. Sequence Diagram — Update Application Status

```mermaid
sequenceDiagram
    participant FE as Angular Frontend
    participant GW as API Gateway
    participant APP as Application Service
    participant JOB as Job Service
    participant AUTH as Auth Service
    participant RMQ as RabbitMQ

    FE->>GW: PATCH /api/applications/{id}/status\nBody: newStatus SHORTLISTED\nAuthorization: Bearer JWT

    GW->>GW: Validate JWT, userId=10, role=RECRUITER
    GW->>GW: Inject X-User-Id: 10, X-User-Role: RECRUITER
    GW->>APP: Forward request

    APP->>APP: applicationRepository.findById(id)
    APP->>APP: If not found, throw 404 ResourceNotFoundException

    APP->>JOB: Feign GET /api/jobs/{jobId}\nX-User-Id: 10, X-User-Role: RECRUITER
    JOB-->>APP: JobClientResponse with postedBy, title, companyName

    APP->>APP: If job null or postedBy != 10, throw ForbiddenException 403

    APP->>APP: validateStatusTransition(current, next)
    APP->>APP: If invalid, throw InvalidStatusTransitionException 400
    APP->>APP: If current is REJECTED, throw InvalidStatusTransitionException 400

    APP->>APP: Update status to SHORTLISTED
    APP->>APP: applicationRepository.save(app)

    alt newStatus is SHORTLISTED, SELECTED, or REJECTED
        APP->>AUTH: Feign GET /api/internal/users/{seekerId}/info
        AUTH-->>APP: UserInfoResponse with name and email
        APP->>RMQ: convertAndSend ApplicationStatusChangedEvent
        Note over APP,RMQ: Failure is error-logged only, response not affected
    end

    alt newStatus is SELECTED
        APP->>AUTH: Feign PUT /api/internal/users/{seekerId}/selected-company
        AUTH-->>APP: 200 OK
        Note over APP,AUTH: Failure is error-logged only
    end

    APP-->>GW: 200 ApplicationResponse
    GW-->>FE: 200 OK
```

---

## 6. Sequence Diagram — Admin Ban User

```mermaid
sequenceDiagram
    participant FE as Angular Frontend
    participant GW as API Gateway
    participant ADM as Admin Service
    participant AUTH as Auth Service
    participant DB as audit_logs MySQL

    FE->>GW: PUT /api/admin/users/{id}/ban\nAuthorization: Bearer JWT

    GW->>GW: Validate JWT, adminId=1, role=ADMIN
    GW->>GW: Inject X-User-Id: 1, X-User-Role: ADMIN
    GW->>ADM: Forward request

    ADM->>ADM: assertAdmin(role=ADMIN) passes

    ADM->>ADM: If id equals adminId, throw IllegalArgumentException 400\nAdmin cannot ban themselves

    ADM->>AUTH: Feign PUT /api/internal/users/{id}/ban
    Note over AUTH: Sets status=BANNED and clears refreshToken
    AUTH-->>ADM: 200 OK

    ADM->>AUTH: Feign PUT /api/internal/users/{id}/invalidate-token
    Note over AUTH: Clears refreshToken as extra safety
    AUTH-->>ADM: 200 OK
    Note over ADM,AUTH: If invalidateToken fails, WARN log only\nBan already succeeded

    ADM->>DB: INSERT INTO audit_logs\naction=BAN_USER, performedBy=admin:1

    ADM-->>GW: 200 User banned successfully
    GW-->>FE: 200 OK
```

---

## 7. API Contract Reference

### Auth Service Endpoints

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/api/auth/register` | No | name, email, password, role, phone | 200 message / 409 |
| POST | `/api/auth/verify-registration` | No | email, otp | 200 message / 400 |
| POST | `/api/auth/resend-registration-otp` | No | email | 200 / 400 |
| POST | `/api/auth/login` | No | email, password | 200 AuthResponse / 400 |
| POST | `/api/auth/refresh` | No | refreshToken | 200 AuthResponse / 404 |
| POST | `/api/auth/logout` | No | refreshToken | 200 / 404 |
| POST | `/api/auth/forgot-password` | No | email | 200 / 404 |
| POST | `/api/auth/reset-password` | No | email, otp, newPassword | 200 / 400 |
| PUT | `/api/auth/profile/picture` | Yes | multipart picture | 200 profilePictureUrl |
| PUT | `/api/auth/profile/resume` | Yes | multipart resume | 200 resumeUrl |
| GET | `/api/auth/profile` | Yes | — | 200 UserProfileResponse |
| PUT | `/api/auth/profile/company-name` | Yes | companyName | 200 |

### Job Service Endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/jobs` | Yes (RECRUITER) | Returns 201 JobResponseDTO; publishes JobPostedEvent |
| GET | `/api/jobs` | No | Paginated, excludes DELETED |
| GET | `/api/jobs/search` | No | Params: title, location, jobType, experienceYears |
| GET | `/api/jobs/{id}` | No | 404 if DELETED |
| PUT | `/api/jobs/{id}` | Yes (RECRUITER) | Ownership check + all fields overwrite |
| DELETE | `/api/jobs/{id}` | Yes (RECRUITER) | Soft-delete (status=DELETED) |
| GET | `/api/jobs/my-jobs` | Yes (RECRUITER) | Paginated, excludes DELETED |

### Application Service Endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/applications` | Yes (SEEKER) | multipart form; validates job + duplicate |
| GET | `/api/applications/my` | Yes (SEEKER) | All applications by seeker |
| GET | `/api/applications/{id}` | Yes (SEEKER) | Own applications only |
| GET | `/api/applications/job/{jobId}` | Yes (RECRUITER) | Enriched with seeker name/email |
| PATCH | `/api/applications/{id}/status` | Yes (RECRUITER) | Body: newStatus, optional recruiterNote |
| DELETE | `/api/applications/{id}` | Yes (RECRUITER) | Hard delete; ownership check |

### Admin Service Endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | `/api/admin/users` | Yes (ADMIN) | All users via Feign |
| DELETE | `/api/admin/users/{id}` | Yes (ADMIN) | Hard delete + audit log |
| PUT | `/api/admin/users/{id}/ban` | Yes (ADMIN) | Feign: ban + invalidateToken + audit log |
| PUT | `/api/admin/users/{id}/unban` | Yes (ADMIN) | Feign: unban + audit log |
| GET | `/api/admin/jobs` | Yes (ADMIN) | All jobs including DELETED via Feign |
| DELETE | `/api/admin/jobs/{id}` | Yes (ADMIN) | Soft-delete via Feign + audit log |
| GET | `/api/admin/reports` | Yes (ADMIN) | Aggregated PlatformReport via 3 Feign calls |
| GET | `/api/admin/audit-logs` | Yes (ADMIN) | All audit_logs records |

---

## 8. Exception Handling

| Exception Class | Service | HTTP Status | Trigger |
|---|---|---|---|
| `UserAlreadyExistsException` | Auth | 409 Conflict | Email already ACTIVE/BANNED |
| `ResourceNotFoundException` | Auth/Job/App | 404 Not Found | Entity not found |
| `IllegalArgumentException` | Auth/Admin/App | 400 Bad Request | Invalid OTP, self-ban, invalid credentials |
| `ForbiddenException` | Job/App | 403 Forbidden | Role mismatch, ownership check failure |
| `DuplicateApplicationException` | App | 409 Conflict | Seeker already applied to same job |
| `InvalidStatusTransitionException` | App | 400 Bad Request | Invalid state machine transition |
| `AccessDeniedException` | Admin | 403 Forbidden | assertAdmin() fails |
| `MaxUploadSizeExceededException` | App | 413 Payload Too Large | Resume file over 5MB |
| JWT validation failure | Gateway | 401 Unauthorized | GatewayJwtFilter — response set directly |
| Internal path access | Gateway | 403 Forbidden | GatewayJwtFilter blocks /api/internal/** |

---

## 9. Internal Dependency Flows

### ApplicationService.applyForJob() chain

```
applyForJob(jobId, coverLetter, useExistingResume, existingResumeUrl, resumeFile, seekerId)
  1. jobServiceClient.getJobById(jobId, seekerId, JOB_SEEKER)  --> Feign: /api/jobs/{id}
  2. Validate job status (DELETED/CLOSED) and deadline
  3. applicationRepository.existsByUserIdAndJobId(seekerId, jobId)  --> Duplicate check
  4. [if not useExistingResume] cloudinaryUtil.uploadResume(resumeFile)
  5. applicationRepository.save(application)
  6. authServiceClient.getUserInfo(seekerId)  --> Feign: /api/internal/users/{id}/info
  7. rabbitTemplate.convertAndSend(exchange, routingKey, JobAppliedEvent)
     [on failure] WARN log only -- application success guaranteed
```

### ApplicationService.updateApplicationStatus() chain

```
updateApplicationStatus(id, StatusUpdateRequest, recruiterId)
  1. applicationRepository.findById(id)
  2. jobServiceClient.getJobById(app.jobId, recruiterId, RECRUITER)  --> Ownership verify
  3. If job null or postedBy != recruiterId --> ForbiddenException
  4. validateStatusTransition(current, next)  --> State machine check
  5. applicationRepository.save(app)
  6. [if SHORTLISTED or SELECTED or REJECTED]
     a. authServiceClient.getUserInfo(app.userId)
     b. rabbitTemplate.convertAndSend(exchange, statusRoutingKey, event)
  7. [if SELECTED]
     a. authServiceClient.updateSelectedByCompany(seekerId, companyName)
        [on failure] error log only
```

### AdminService.banUser() chain

```
banUser(id, adminId)
  1. If id equals adminId --> IllegalArgumentException (self-ban blocked)
  2. authServiceClient.banUser(id)         --> PUT /api/internal/users/{id}/ban
  3. authServiceClient.invalidateToken(id) --> PUT /api/internal/users/{id}/invalidate-token
     [on failure] WARN log only
  4. auditLogRepository.save(AuditLog BAN_USER)
```
