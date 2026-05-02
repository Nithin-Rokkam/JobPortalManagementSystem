# Joblix — Job Portal Management System (JPMS)

A full-stack, enterprise-grade job portal built on a Spring Boot microservices backend and an Angular 19+ frontend. The system supports three user roles — Job Seeker, Recruiter, and Admin — with JWT-based authentication, event-driven email notifications via RabbitMQ, file uploads via Cloudinary, and a modern "Celestial" UI design system.

---

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Microservices Overview](#microservices-overview)
3. [Infrastructure Components](#infrastructure-components)
4. [Inter-Service Communication](#inter-service-communication)
5. [Data Flow — Key Scenarios](#data-flow--key-scenarios)
6. [Database Schema](#database-schema)
7. [Authentication & Security Flow](#authentication--security-flow)
8. [RabbitMQ Event Bus](#rabbitmq-event-bus)
9. [API Summary](#api-summary)
10. [Frontend Overview](#frontend-overview)
11. [Environment Variables](#environment-variables)
12. [Running the Full Stack](#running-the-full-stack)
13. [Service READMEs](#service-readmes)

---

## System Architecture

```
Browser (Angular 19+)
         │
         │  HTTP (REST + JWT)
         ▼
┌─────────────────────────────────────────────────────────┐
│              API Gateway  (port 9090)                   │
│  Spring Cloud Gateway (Reactive)                        │
│  • GatewayJwtFilter — validates JWT, injects headers    │
│  • Blocks /internal/** from external access             │
│  • Routes lb://service-name via Eureka discovery        │
└──────┬──────────┬──────────┬──────────┬─────────────────┘
       │          │          │          │
       ▼          ▼          ▼          ▼
  AuthService  JobService  AppService  AdminService
  (port 8081)  (port 8082) (port 8083) (port 8084)
       │          │          │
       └──────────┴──────────┴──→ RabbitMQ (port 5672)
                                        │
                                        ▼
                               NotificationService
                                  (port 8085)
                                  JavaMailSender
                                        │
                                        ▼
                                  User Inboxes

All services register with:
  Eureka Server (port 8761) — Service Discovery
  MySQL DB      (port 3307) — Shared Database
  Zipkin        (port 9411) — Distributed Tracing (optional)
```

---

## Microservices Overview

| Service | Port | Eureka Name | Responsibility |
|---|---|---|---|
| **Eureka Server** | 8761 | `eureka-server` | Service registry and discovery |
| **API Gateway** | 9090 | `api-gateway` | JWT validation, routing, CORS |
| **Auth Service** | 8081 | `auth-service` | Registration, login, tokens, profiles, file uploads |
| **Job Service** | 8082 | `job-service` | Job CRUD, search, pagination |
| **Application Service** | 8083 | `application-service` | Job applications, status management |
| **Admin Service** | 8084 | `admin-service` | User/job governance, reports, audit logs |
| **Notification Service** | 8085 | `notification-service` | Event-driven email notifications |

---

## Infrastructure Components

| Component | Port | Purpose |
|---|---|---|
| MySQL 8 | 3307 | Shared relational database for all services |
| RabbitMQ | 5672 (AMQP), 15672 (UI) | Async message broker for event-driven notifications |
| Cloudinary | — (cloud) | Profile picture and resume file storage |
| Zipkin | 9411 | Distributed request tracing (optional) |

---

## Inter-Service Communication

### Synchronous — OpenFeign (REST)

Feign clients make direct HTTP calls between services using Eureka-resolved service names. These calls go **directly between services** (not through the Gateway).

```
AdminService ──(Feign)──→ AuthService   [GET/DELETE/PUT /api/internal/users/**]
AdminService ──(Feign)──→ JobService    [GET/DELETE /api/internal/jobs/**]
AdminService ──(Feign)──→ AppService    [GET /api/internal/applications/stats]

ApplicationService ──(Feign)──→ AuthService  [GET /api/internal/users/{id}/info]
                                              [PUT /api/internal/users/{id}/selected-company]
ApplicationService ──(Feign)──→ JobService   [GET /api/jobs/{id}]

NotificationService ──(Feign)──→ AuthService [GET /api/internal/users/job-seeker-emails]
                                              [GET /api/internal/users/{id}/info]
```

All `/api/internal/**` endpoints are blocked by the Gateway's `GatewayJwtFilter` for external requests — they are only reachable via Feign between services.

### Asynchronous — RabbitMQ (AMQP)

Event-driven communication for notifications. Publishers fire-and-forget; the Notification Service consumes and sends emails.

```
JobService ──(publish)──→ jobportal.exchange [routing: job.posted]
                                    │
                                    └──→ job.posted.queue
                                              │
                                    NotificationService (JobPostedListener)
                                    → Email to recruiter (confirmation)
                                    → Email to all job seekers (job alert)

ApplicationService ──(publish)──→ jobportal.exchange [routing: job.applied]
                                    │
                                    └──→ job.applied.queue
                                              │
                                    NotificationService (JobAppliedListener)
                                    → Email to recruiter (new application alert)

ApplicationService ──(publish)──→ jobportal.exchange [routing: application.status.changed]
                                    │
                                    └──→ application.status.queue
                                              │
                                    NotificationService (ApplicationStatusChangedListener)
                                    → Email to seeker (shortlisted / selected / rejected)

AuthService ──(publish)──→ jobportal.exchange [routing: password.reset]
                                    │
                                    └──→ password.reset.queue
                                              │
                                    NotificationService (PasswordResetListener)
                                    → OTP email to user

AuthService ──(publish)──→ jobportal.exchange [routing: registration.otp]
                                    │
                                    └──→ registration.otp.queue
                                              │
                                    NotificationService (RegistrationOtpListener)
                                    → Email verification OTP to new user
```

---

## Data Flow — Key Scenarios

### 1. User Registration

```
Browser
  │ POST /api/auth/register { name, email, password, role }
  ▼
API Gateway
  │ Public route — no JWT check
  ▼
AuthService (AuthController → AuthService.register())
  │ 1. Validates email uniqueness
  │ 2. Creates User with status=PENDING_VERIFICATION
  │ 3. Generates 6-digit OTP, sets 10-min expiry
  │ 4. Saves user to DB (saveAndFlush)
  │ 5. After transaction commit → publishes PasswordResetEvent
  │    to RabbitMQ (routing: registration.otp)
  ▼
NotificationService (RegistrationOtpListener)
  │ Sends email verification OTP to user
  ▼
Browser receives: { message: "OTP sent to your email" }
  │ User enters OTP at /auth/verify-registration
  ▼
AuthService (verifyRegistrationOtp())
  │ Validates OTP and expiry
  │ Sets user status = ACTIVE
  ▼
User can now log in
```

### 2. Login and Token Flow

```
Browser
  │ POST /api/auth/login { email, password }
  ▼
API Gateway → AuthService
  │ 1. Validates credentials (BCrypt password check)
  │ 2. Checks status (ACTIVE required)
  │ 3. Generates JWT access token (HMAC-SHA256, contains userId + role)
  │ 4. Generates UUID refresh token, stores in DB
  │ 5. Returns { accessToken, refreshToken, userId, name, email, role }
  ▼
Browser stores tokens in localStorage
  │ All subsequent requests include: Authorization: Bearer <accessToken>
  ▼
API Gateway (GatewayJwtFilter)
  │ 1. Extracts token from Authorization header
  │ 2. Validates signature and expiry using shared JWT secret
  │ 3. Extracts userId and role from claims
  │ 4. Injects X-User-Id and X-User-Role headers
  │ 5. Forwards to downstream service
  ▼
Downstream service reads X-User-Id and X-User-Role for RBAC
```

### 3. Recruiter Posts a Job

```
Browser (Recruiter)
  │ POST /api/jobs { title, companyName, location, ... }
  │ Authorization: Bearer <token>
  ▼
API Gateway
  │ Validates JWT → injects X-User-Id=42, X-User-Role=RECRUITER
  ▼
JobService (JobController → JobService.postJob())
  │ 1. Validates role == RECRUITER
  │ 2. Creates Job entity, sets postedBy=42, status=ACTIVE
  │ 3. Saves to DB
  │ 4. Publishes JobPostedEvent to RabbitMQ
  ▼
NotificationService (JobPostedListener)
  │ 1. Fetches recruiter email from AuthService (Feign)
  │ 2. Sends job posting confirmation to recruiter
  │ 3. Fetches all active job seeker emails from AuthService (Feign)
  │ 4. Sends job alert email to each seeker
  ▼
Browser receives: JobResponseDTO { id, title, ... }
```

### 4. Job Seeker Applies for a Job

```
Browser (Job Seeker)
  │ POST /api/applications (multipart: jobId, resume file, coverLetter)
  │ Authorization: Bearer <token>
  ▼
API Gateway → ApplicationService (ApplicationController → ApplicationService.applyForJob())
  │ 1. Validates role == JOB_SEEKER
  │ 2. Fetches job via JobServiceClient (Feign → JobService)
  │ 3. Validates job is ACTIVE and deadline not passed
  │ 4. Checks for duplicate application
  │ 5. Uploads resume to Cloudinary
  │ 6. Creates Application entity, status=APPLIED
  │ 7. Fetches seeker info from AuthServiceClient (Feign → AuthService)
  │ 8. Publishes JobAppliedEvent to RabbitMQ
  ▼
NotificationService (JobAppliedListener)
  │ Fetches recruiter info from AuthService (Feign)
  │ Sends application alert email to recruiter
  ▼
Browser receives: ApplicationResponse { id, status: "APPLIED", ... }
```

### 5. Recruiter Updates Application Status to SHORTLISTED

```
Browser (Recruiter)
  │ PATCH /api/applications/15/status { newStatus: "SHORTLISTED" }
  ▼
API Gateway → ApplicationService (updateApplicationStatus())
  │ 1. Validates role == RECRUITER
  │ 2. Fetches application, validates recruiter owns the job
  │ 3. Validates status transition (UNDER_REVIEW → SHORTLISTED is valid)
  │ 4. Updates status in DB
  │ 5. Fetches seeker info from AuthService (Feign)
  │ 6. Publishes ApplicationStatusChangedEvent { newStatus: "SHORTLISTED" }
  ▼
NotificationService (ApplicationStatusChangedListener)
  │ Sends congratulatory shortlisting email to seeker
  ▼
Browser receives: ApplicationResponse { status: "SHORTLISTED" }
```

### 6. Admin Bans a User

```
Browser (Admin)
  │ PUT /api/admin/users/7/ban
  │ Authorization: Bearer <admin-token>
  ▼
API Gateway → AdminService (AdminController.banUser())
  │ 1. Reads X-User-Role, calls assertAdmin() — throws 403 if not ADMIN
  │ 2. Prevents self-ban
  │ 3. Calls AuthServiceClient.banUser(7) (Feign → AuthService)
  │    → AuthService sets user.status = BANNED, clears refreshToken
  │ 4. Calls AuthServiceClient.invalidateToken(7) (Feign → AuthService)
  │    → AuthService clears refreshToken (forces logout)
  │ 5. Saves AuditLog { action: "BAN_USER", performedBy: "admin:1" }
  ▼
Browser receives: { message: "User banned successfully" }
```

---

## Database Schema

All services share a single MySQL database (`jobportal_db`). Each service manages its own tables via JPA `ddl-auto: update`.

### `users` table (AuthService)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| name | VARCHAR(100) | Required |
| email | VARCHAR(150) UNIQUE | Required |
| password | VARCHAR | BCrypt hashed |
| role | ENUM | JOB_SEEKER / RECRUITER / ADMIN |
| phone | VARCHAR(20) | Optional |
| status | ENUM | ACTIVE / BANNED / PENDING_VERIFICATION |
| profile_picture_url | VARCHAR | Cloudinary URL |
| resume_url | VARCHAR | Cloudinary URL |
| refresh_token | VARCHAR | UUID, nullable |
| reset_password_otp | VARCHAR | 6-digit OTP |
| otp_expiry_time | DATETIME | 10-min window |
| email_verification_otp | VARCHAR | 6-digit OTP |
| email_verification_expiry | DATETIME | 10-min window |
| company_name | VARCHAR(200) | Recruiter only |
| selected_by_company | VARCHAR(200) | Set when seeker is SELECTED |
| created_at | DATETIME | Auto-set on insert |
| updated_at | DATETIME | Auto-updated |

### `jobs` table (JobService)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| title | VARCHAR(200) | Required |
| company_name | VARCHAR(150) | Required |
| location | VARCHAR(150) | Required |
| salary | DECIMAL(12,2) | Optional |
| experience_years | INT | Optional |
| job_type | ENUM | FULL_TIME / PART_TIME / REMOTE / CONTRACT |
| skills_required | TEXT | Optional |
| description | TEXT | Required |
| status | ENUM | ACTIVE / CLOSED / DRAFT / DELETED |
| deadline | DATE | Optional |
| posted_by | BIGINT | FK → users.id (RECRUITER) |
| created_at | DATETIME | Auto-set |
| updated_at | DATETIME | Auto-updated |

### `applications` table (ApplicationService)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| user_id | BIGINT | FK → users.id (JOB_SEEKER) |
| job_id | BIGINT | FK → jobs.id |
| resume_url | TEXT | Cloudinary URL |
| cover_letter | TEXT | Optional |
| status | ENUM | APPLIED / UNDER_REVIEW / SHORTLISTED / SELECTED / REJECTED |
| recruiter_note | TEXT | Optional feedback |
| applied_at | DATETIME | Auto-set, not updatable |
| updated_at | DATETIME | Auto-updated |
| UNIQUE | (user_id, job_id) | Prevents duplicate applications |

### `audit_logs` table (AdminService)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| action | VARCHAR(100) | DELETE_USER / BAN_USER / UNBAN_USER / DELETE_JOB |
| performed_by | VARCHAR(150) | e.g., "admin:1" |
| details | TEXT | Human-readable description |
| created_at | DATETIME | Auto-set on insert |

---

## Authentication & Security Flow

### JWT Token Structure
```
Header: { alg: "HS256", typ: "JWT" }
Payload: {
    sub: "42",           // userId
    role: "RECRUITER",   // user role
    iat: 1700000000,     // issued at
    exp: 1700003600      // expiry
}
Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
```

### Token Lifecycle
1. **Login** → AuthService generates access token (short-lived) + refresh token (UUID, stored in DB).
2. **Every request** → Angular interceptor attaches `Authorization: Bearer <accessToken>`.
3. **Gateway** → validates token signature and expiry, injects `X-User-Id` and `X-User-Role`.
4. **401 response** → Angular interceptor calls `/api/auth/refresh` with refresh token.
5. **Refresh success** → new tokens saved, original request retried.
6. **Refresh failure** → `auth.logout()` called, user redirected to login.
7. **Logout** → refresh token cleared from DB; access token expires naturally.
8. **Ban** → refresh token cleared from DB immediately; access token expires within its TTL.

### Role-Based Access Control
| Role | Accessible Routes |
|---|---|
| Public (no auth) | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `GET /api/jobs`, `GET /api/jobs/search`, `GET /api/jobs/{id}` |
| JOB_SEEKER | `/api/applications` (POST, GET own), `/api/auth/profile` |
| RECRUITER | `/api/jobs` (POST, PUT, DELETE own), `/api/applications/job/{id}`, `/api/applications/{id}/status` |
| ADMIN | `/api/admin/**` |

---

## RabbitMQ Event Bus

### Exchange
All events use a single topic/direct exchange: `jobportal.exchange`

### Queues and Bindings

| Queue | Routing Key | Publisher | Consumer |
|---|---|---|---|
| `job.posted.queue` | `job.posted` | JobService | NotificationService |
| `job.applied.queue` | `job.applied` | ApplicationService | NotificationService |
| `application.status.queue` | `application.status.changed` | ApplicationService | NotificationService |
| `password.reset.queue` | `password.reset` | AuthService | NotificationService |
| `registration.otp.queue` | `registration.otp` | AuthService | NotificationService |

### Event Payloads

**JobPostedEvent** — `{ jobId, recruiterId, title, companyName, location, jobType, salary, experienceYears, description }`

**JobAppliedEvent** — `{ jobId, jobTitle, seekerId, seekerName, seekerEmail, recruiterId }`

**ApplicationStatusChangedEvent** — `{ applicationId, jobId, jobTitle, seekerId, seekerName, seekerEmail, newStatus }`

**PasswordResetEvent** — `{ email, name, otp }`

---

## API Summary

### Auth Service — `/api/auth/**`
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/register` | Public | Register new user |
| POST | `/login` | Public | Login, get tokens |
| POST | `/refresh` | Public | Rotate tokens |
| POST | `/logout` | JWT | Invalidate refresh token |
| POST | `/verify-registration` | Public | Verify email OTP |
| POST | `/resend-registration-otp` | Public | Resend OTP |
| POST | `/forgot-password` | Public | Request password reset OTP |
| POST | `/reset-password` | Public | Reset password with OTP |
| GET | `/profile` | JWT | Get own profile |
| PUT | `/profile/picture` | JWT | Upload profile picture |
| PUT | `/profile/resume` | JWT | Upload resume |
| PUT | `/profile/company` | JWT | Update company name |

### Job Service — `/api/jobs/**`
| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| POST | `/` | JWT | RECRUITER | Post new job |
| GET | `/` | Public | Any | Get all jobs (paginated) |
| GET | `/{id}` | Public | Any | Get job by ID |
| GET | `/search` | Public | Any | Search jobs |
| PUT | `/{id}` | JWT | RECRUITER | Update own job |
| DELETE | `/{id}` | JWT | RECRUITER | Soft-delete own job |
| GET | `/my-jobs` | JWT | RECRUITER | Get recruiter's jobs |

### Application Service — `/api/applications/**`
| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| POST | `/` | JWT | JOB_SEEKER | Apply for a job |
| GET | `/my-applications` | JWT | JOB_SEEKER | Get own applications |
| GET | `/{id}` | JWT | JOB_SEEKER | Get application by ID |
| GET | `/job/{jobId}` | JWT | RECRUITER | Get applicants for a job |
| PATCH | `/{id}/status` | JWT | RECRUITER | Update application status |
| DELETE | `/{id}` | JWT | RECRUITER | Delete application |

### Admin Service — `/api/admin/**`
| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| GET | `/users` | JWT | ADMIN | Get all users |
| DELETE | `/users/{id}` | JWT | ADMIN | Delete user |
| PUT | `/users/{id}/ban` | JWT | ADMIN | Ban user |
| PUT | `/users/{id}/unban` | JWT | ADMIN | Unban user |
| GET | `/jobs` | JWT | ADMIN | Get all jobs |
| DELETE | `/jobs/{id}` | JWT | ADMIN | Delete job |
| GET | `/reports` | JWT | ADMIN | Platform report |
| GET | `/audit-logs` | JWT | ADMIN | Audit logs |

---

## Frontend Overview

The Angular frontend is organized into lazy-loaded feature modules:

| Module | Route Prefix | Role | Key Components |
|---|---|---|---|
| Auth | `/auth` | Public | Login, Register, OTP Verify, Forgot/Reset Password |
| Home | `/home` | Public | Landing page |
| Seeker | `/seeker` | JOB_SEEKER | Dashboard, Browse Jobs, Job Detail, My Applications, Profile |
| Recruiter | `/recruiter` | RECRUITER | Dashboard, Post Job, My Jobs, Edit Job, View Applicants, Profile |
| Admin | `/admin` | ADMIN | Dashboard, User Management, Job Management, Reports, Audit Logs, Profile |

**State management:** `AuthService` holds a `BehaviorSubject<User>` that all components subscribe to. No NgRx needed at this scale.

**HTTP:** `AuthInterceptor` automatically attaches JWT tokens and handles 401 refresh transparently.

**Theme:** Dark/light toggle persisted to `localStorage`. Applied via `data-theme` attribute on `<html>`.

See [jpms-frontend/README.md](jpms-frontend/README.md) for full frontend documentation.

---

## Environment Variables

All sensitive configuration is stored in the root `.env` file and injected into Docker containers.

| Variable | Used By | Description |
|---|---|---|
| `AUTH_SERVICE_DB_USERNAME` | AuthService | DB username |
| `AUTH_SERVICE_DB_PASSWORD` | AuthService | DB password |
| `AUTH_SERVICE_JWT_SECRET` | AuthService | JWT signing secret |
| `AUTH_SERVICE_JWT_ACCESS_EXPIRY` | AuthService | Access token TTL (ms) |
| `AUTH_SERVICE_JWT_REFRESH_EXPIRY` | AuthService | Refresh token TTL (ms) |
| `AUTH_SERVICE_CLOUDINARY_CLOUD_NAME` | AuthService | Cloudinary cloud name |
| `AUTH_SERVICE_CLOUDINARY_API_KEY` | AuthService | Cloudinary API key |
| `AUTH_SERVICE_CLOUDINARY_API_SECRET` | AuthService | Cloudinary API secret |
| `JOB_SERVICE_DB_USERNAME` | JobService | DB username |
| `JOB_SERVICE_DB_PASSWORD` | JobService | DB password |
| `APPLICATION_SERVICE_DB_USERNAME` | ApplicationService | DB username |
| `APPLICATION_SERVICE_DB_PASSWORD` | ApplicationService | DB password |
| `APPLICATION_SERVICE_CLOUDINARY_*` | ApplicationService | Cloudinary credentials |
| `ADMIN_SERVICE_DB_USERNAME` | AdminService | DB username |
| `ADMIN_SERVICE_DB_PASSWORD` | AdminService | DB password |
| `API_GATEWAY_JWT_SECRET` | API Gateway | Must match `AUTH_SERVICE_JWT_SECRET` |
| `EMAIL` | NotificationService | Gmail address for sending emails |
| `EMAIL_PASSWORD` | NotificationService | Gmail App Password |

> `API_GATEWAY_JWT_SECRET` and `AUTH_SERVICE_JWT_SECRET` **must be identical** — the Gateway uses this secret to independently verify tokens without calling the Auth Service.

---

## Running the Full Stack

### Prerequisites
- Docker & Docker Compose
- Node.js 20+ (for frontend)
- Java 17+ (for local backend development)

### 1. Configure Environment
Copy and fill in the `.env` file at the project root with all required values.

### 2. Start Backend (Docker)
```bash
docker-compose up --build
```

**Startup order (managed by `depends_on`):**
1. MySQL + RabbitMQ
2. Eureka Server
3. API Gateway + Auth Service + Job Service + Application Service + Admin Service
4. Notification Service

### 3. Start Frontend
```bash
cd jpms-frontend
npm install
npm run dev
```
Frontend runs at `http://localhost:4200`.

### 4. Verify Services
| URL | Description |
|---|---|
| `http://localhost:8761` | Eureka Dashboard — all services should show UP |
| `http://localhost:9090/swagger-ui/index.html` | Swagger UI (proxied from AuthService) |
| `http://localhost:15672` | RabbitMQ Management UI (guest/guest) |
| `http://localhost:4200` | Angular Frontend |

---

## Service READMEs

Each microservice has its own detailed README:

| Service | README |
|---|---|
| Eureka Server | [JPMS-EurekaServer/README.md](JPMS-EurekaServer/README.md) |
| API Gateway | [JPMS-ApiGateWay/README.md](JPMS-ApiGateWay/README.md) |
| Auth Service | [JPMS-AuthService/README.md](JPMS-AuthService/README.md) |
| Job Service | [JPMS-JobService/README.md](JPMS-JobService/README.md) |
| Application Service | [JPMS-ApplicationService/README.md](JPMS-ApplicationService/README.md) |
| Admin Service | [JPMS-AdminService/README.md](JPMS-AdminService/README.md) |
| Notification Service | [JPMS-NotificationService/README.md](JPMS-NotificationService/README.md) |
| Angular Frontend | [jpms-frontend/README.md](jpms-frontend/README.md) |

---

*Joblix — Capgemini Training Sprint 1 | Built with Spring Boot 3.x + Angular 19+*
