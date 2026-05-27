# Architecture Diagram — Joblix Job Portal Management System

---

## Architectural Patterns

| Pattern | Implementation | Key Detail |
|---|---|---|
| API Gateway | Spring Cloud Gateway (reactive) | GatewayJwtFilter at Order=-1; injects X-User-Id and X-User-Role |
| Service Discovery | Netflix Eureka (client-side load balancing) | All services register; Feign uses lb://service-name |
| Event-Driven Architecture | RabbitMQ jobportal.exchange, 5 queues | Auth uses afterCommit(); Job uses sync publish; App uses best-effort |
| Synchronous Inter-Service | OpenFeign declarative HTTP clients | Resolved via Eureka — bypasses API Gateway entirely |
| Stateless Authentication | JWT (HMAC-SHA256) + UUID refresh token | Refresh token stored in DB; rotated on every /api/auth/refresh |
| External Storage Delegation | Cloudinary SDK | Used by Auth Service (profile pic + resume) and Application Service |
| Shared Database | Single MySQL 8 jobportal_db | No cross-service FK constraints at DB level |
| Distributed Tracing | Zipkin (optional) | Micrometer integration; services start even if Zipkin is unavailable |

---

## Full Architecture Diagram

```mermaid
graph TB
    classDef fe    fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef gw    fill:#6A1B9A,stroke:#4A148C,color:#fff
    classDef svc   fill:#1B5E20,stroke:#0D3D0D,color:#fff
    classDef infra fill:#37474F,stroke:#263238,color:#fff
    classDef ext   fill:#4E342E,stroke:#3E2723,color:#fff
    classDef reg   fill:#E65100,stroke:#BF360C,color:#fff

    Browser["Browser"]:::fe

    subgraph FrontendLayer["Frontend Layer"]
        Angular["Angular SPA\nPort 4200\nLazy-loaded modules\nAuthInterceptor + Guards"]:::fe
    end

    subgraph GatewayLayer["API Gateway Layer"]
        Gateway["API Gateway\nPort 9090\nSpring Cloud Gateway\nGatewayJwtFilter\nCorsConfig"]:::gw
    end

    subgraph ServiceRegistry["Service Registry"]
        Eureka["Eureka Server\nPort 8761\nService Registration\nDiscovery via lb://"]:::reg
    end

    subgraph BusinessServices["Business Services Layer"]
        AuthSvc["Auth Service\nPort 8081\nRegistration and OTP\nLogin and Token Mgmt\nProfile Management\nInternal User API"]:::svc
        JobSvc["Job Service\nPort 8082\nJob CRUD\nSearch and Pagination\nSoft Delete\nInternal Job API"]:::svc
        AppSvc["Application Service\nPort 8083\nApply for Job\nStatus Pipeline\nApplicant Mgmt\nInternal Stats API"]:::svc
        AdminSvc["Admin Service\nPort 8084\nUser Governance\nJob Oversight\nPlatform Report\nAudit Logs"]:::svc
    end

    subgraph NotificationLayer["Notification Layer"]
        NotifSvc["Notification Service\nPort 8085\n5 RabbitMQ Listeners\nEmailService\nJavaMailSender"]:::svc
    end

    subgraph DataLayer["Data Layer"]
        MySQL["MySQL 8\nPort 3307\njobportal_db\nusers, jobs, applications, audit_logs"]:::infra
    end

    subgraph MessagingLayer["Messaging Layer"]
        RabbitMQ["RabbitMQ\nPort 5672 and 15672\nExchange: jobportal.exchange\n5 Queues"]:::infra
    end

    subgraph ExternalServices["External Services"]
        Cloudinary["Cloudinary\nProfile Pictures\nResumes"]:::ext
        SMTP["Gmail SMTP\nEmail Delivery\nTLS Port 587"]:::ext
        Zipkin["Zipkin\nPort 9411\nDistributed Tracing"]:::ext
    end

    Browser --> Angular
    Angular -->|"HTTP REST + JWT Bearer to port 9090"| Gateway

    Gateway -->|"/api/auth/** to auth-service"| AuthSvc
    Gateway -->|"/api/jobs/** to job-service"| JobSvc
    Gateway -->|"/api/applications/** to application-service"| AppSvc
    Gateway -->|"/api/admin/** to admin-service"| AdminSvc

    AuthSvc -.->|"Register"| Eureka
    JobSvc -.->|"Register"| Eureka
    AppSvc -.->|"Register"| Eureka
    AdminSvc -.->|"Register"| Eureka
    NotifSvc -.->|"Register"| Eureka
    Gateway -.->|"Discover via lb://"| Eureka

    AuthSvc -->|"JPA: users table"| MySQL
    JobSvc -->|"JPA: jobs table"| MySQL
    AppSvc -->|"JPA: applications table"| MySQL
    AdminSvc -->|"JPA: audit_logs table"| MySQL

    AuthSvc -->|"afterCommit: registration.otp\npassword.reset"| RabbitMQ
    JobSvc -->|"sync publish: job.posted"| RabbitMQ
    AppSvc -->|"best-effort: job.applied\napplication.status.changed"| RabbitMQ

    RabbitMQ -->|"Consumes all 5 queues"| NotifSvc

    AdminSvc -->|"Feign: GET, DELETE, PUT /api/internal/users/**"| AuthSvc
    AdminSvc -->|"Feign: GET, DELETE /api/internal/jobs/**"| JobSvc
    AdminSvc -->|"Feign: GET /api/internal/applications/stats"| AppSvc
    AppSvc -->|"Feign: GET /api/internal/users/{id}/info\nPUT /api/internal/users/{id}/selected-company"| AuthSvc
    AppSvc -->|"Feign: GET /api/jobs/{id} (public endpoint)"| JobSvc
    NotifSvc -->|"Feign: GET /api/internal/users/**"| AuthSvc

    AuthSvc -->|"Upload profile pictures and resumes"| Cloudinary
    AppSvc -->|"Upload application resumes"| Cloudinary
    NotifSvc -->|"Send emails"| SMTP

    AuthSvc -.->|"Traces"| Zipkin
    JobSvc -.->|"Traces"| Zipkin
    AppSvc -.->|"Traces"| Zipkin
    AdminSvc -.->|"Traces"| Zipkin
    NotifSvc -.->|"Traces"| Zipkin
    Gateway -.->|"Traces"| Zipkin
```

---

## Authenticated Request Flow

```mermaid
sequenceDiagram
    participant B as Browser
    participant G as API Gateway
    participant S as Business Service

    B->>G: HTTP Request + Authorization: Bearer JWT

    Note over G: GatewayJwtFilter executes
    G->>G: Extract JWT from Authorization header
    G->>G: Validate signature with shared secret
    G->>G: Check token expiry

    alt Token invalid or expired
        G-->>B: 401 Unauthorized
    else Token valid
        G->>G: Extract userId and role from JWT claims
        G->>G: Inject X-User-Id and X-User-Role headers
        G->>S: Forward request with injected headers
        S->>S: Read X-User-Id and X-User-Role
        S->>S: Perform role-based business logic
        S-->>G: Response
        G-->>B: Response
    end
```

---

## Event-Driven Notification Flow

```mermaid
sequenceDiagram
    participant S as Business Service
    participant R as RabbitMQ
    participant N as Notification Service
    participant A as Auth Service
    participant E as Gmail SMTP

    S->>S: Business operation complete - DB saved
    S->>R: convertAndSend with exchange, routingKey, event

    Note over R: Routes message to appropriate queue
    R->>N: Deliver message to RabbitListener

    N->>A: Feign GET /api/internal/users/{id}/info
    A-->>N: UserInfoResponse with name and email

    N->>N: EmailService builds HTML email
    N->>E: JavaMailSender sends MimeMessage
    E-->>N: OK
```

---

## Component Dependency Map

```mermaid
graph LR
    classDef fe    fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef gw    fill:#6A1B9A,stroke:#4A148C,color:#fff
    classDef svc   fill:#1B5E20,stroke:#0D3D0D,color:#fff
    classDef infra fill:#37474F,stroke:#263238,color:#fff
    classDef ext   fill:#4E342E,stroke:#3E2723,color:#fff

    FE["Angular SPA"]:::fe
    GW["API Gateway"]:::gw
    ES["Eureka Server"]:::infra
    AS["Auth Service"]:::svc
    JS["Job Service"]:::svc
    APS["Application Service"]:::svc
    ADS["Admin Service"]:::svc
    NS["Notification Service"]:::svc
    DB["MySQL 8"]:::infra
    RMQ["RabbitMQ"]:::infra
    CLD["Cloudinary"]:::ext
    SMT["Gmail SMTP"]:::ext
    ZPK["Zipkin"]:::ext

    FE -->|"REST"| GW
    GW -->|"lb://"| ES
    GW --> AS
    GW --> JS
    GW --> APS
    GW --> ADS

    AS -->|"JPA"| DB
    JS -->|"JPA"| DB
    APS -->|"JPA"| DB
    ADS -->|"JPA"| DB

    AS -->|"upload"| CLD
    APS -->|"upload"| CLD

    AS -->|"publish"| RMQ
    JS -->|"publish"| RMQ
    APS -->|"publish"| RMQ

    NS -->|"consume"| RMQ
    NS -->|"Feign"| AS
    NS -->|"SMTP"| SMT

    ADS -->|"Feign"| AS
    ADS -->|"Feign"| JS
    ADS -->|"Feign"| APS

    APS -->|"Feign"| AS
    APS -->|"Feign"| JS

    AS -.->|"traces"| ZPK
    JS -.->|"traces"| ZPK
    APS -.->|"traces"| ZPK
    ADS -.->|"traces"| ZPK
    NS -.->|"traces"| ZPK
    GW -.->|"traces"| ZPK

    AS -.->|"register"| ES
    JS -.->|"register"| ES
    APS -.->|"register"| ES
    ADS -.->|"register"| ES
    NS -.->|"register"| ES
```

---

## Service Topology Summary

| Service | Port | Role | DB Tables | Publishes | Consumes | Feign Clients | Publish Timing |
|---|---|---|---|---|---|---|---|
| Eureka Server | 8761 | Infrastructure | none | none | none | none | none |
| API Gateway | 9090 | Infrastructure | none | none | none | none | none |
| Auth Service | 8081 | Business | users | registration.otp, password.reset | none | None | afterCommit() |
| Job Service | 8082 | Business | jobs | job.posted | none | None | synchronous |
| Application Service | 8083 | Business | applications | job.applied, application.status.changed | none | AuthServiceClient, JobServiceClient | best-effort |
| Admin Service | 8084 | Business | audit_logs | none | none | AuthServiceClient, AdminJobClient, AdminAppClient | none |
| Notification Service | 8085 | Business | none | none | All 5 queues | AuthServiceClient | none |
| Zipkin | 9411 | Infrastructure | none | none | none | none | none |
| MySQL 8 | 3307 | Infrastructure | All 4 tables | none | none | none | none |
| RabbitMQ | 5672/15672 | Infrastructure | none | none | none | none | none |

---

## RabbitMQ Queue Architecture

```mermaid
graph LR
    classDef pub   fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef ex    fill:#4E342E,stroke:#3E2723,color:#fff
    classDef queue fill:#37474F,stroke:#263238,color:#fff
    classDef smtp  fill:#1B5E20,stroke:#0D3D0D,color:#fff

    subgraph Publishers
        AUTH["Auth Service\nafterCommit"]:::pub
        JOB["Job Service\nsynchronous"]:::pub
        APP["Application Service\nbest-effort"]:::pub
    end

    EX["jobportal.exchange"]:::ex

    subgraph Queues["Queues and Listeners"]
        Q1["registration.otp.queue\nRegistrationOtpListener"]:::queue
        Q2["password.reset.queue\nPasswordResetListener"]:::queue
        Q3["job.posted.queue\nJobPostedListener"]:::queue
        Q4["job.applied.queue\nJobAppliedListener"]:::queue
        Q5["application.status.queue\nApplicationStatusChangedListener"]:::queue
    end

    SMTP["Gmail SMTP"]:::smtp

    AUTH -->|"routing: registration.otp"| EX
    AUTH -->|"routing: password.reset"| EX
    JOB  -->|"routing: job.posted"| EX
    APP  -->|"routing: job.applied"| EX
    APP  -->|"routing: application.status.changed"| EX

    EX --> Q1
    EX --> Q2
    EX --> Q3
    EX --> Q4
    EX --> Q5

    Q1 -->|"OTP email to new user"| SMTP
    Q2 -->|"OTP email to user"| SMTP
    Q3 -->|"Confirmation to recruiter + Alert to all seekers"| SMTP
    Q4 -->|"Application alert to recruiter"| SMTP
    Q5 -->|"Status email to seeker"| SMTP
```

> **Note:** ApplicationStatusChangedListener only sends emails for SHORTLISTED, SELECTED, and REJECTED. UNDER_REVIEW does NOT trigger any email.

---

## API Gateway Route Table

| Route | Path Predicate | Target Service | Auth Required | Notes |
|---|---|---|---|---|
| auth-service | `/api/auth/**` | lb://auth-service | Partial | register, login, refresh, forgot/reset password are public |
| job-service | `/api/jobs/**` | lb://job-service | Partial | GET /api/jobs, /search, /{id} are public |
| application-service | `/api/applications/**` | lb://application-service | Yes | All require JWT |
| admin-service | `/api/admin/**` | lb://admin-service | Yes (ADMIN) | All require JWT + ADMIN role |
| auth-service-internal | `/api/internal/users/**` | lb://auth-service | Blocked 403 | Feign-only |
| job-service-internal | `/api/internal/jobs/**` | lb://job-service | Blocked 403 | Feign-only |
| app-service-internal | `/api/internal/applications/**` | lb://application-service | Blocked 403 | Feign-only |

---

## Frontend Module Architecture

```
Angular SPA (jpms-frontend)
│
├── AppModule (root)
│   ├── AppRoutingModule (lazy routes)
│   └── CoreModule
│       ├── AuthService (BehaviorSubject<User>)
│       ├── AuthInterceptor (JWT attach + 401 refresh)
│       ├── AuthGuard (isLoggedIn check)
│       ├── RoleGuard (role array check)
│       └── ToastService / LoaderService
│
├── LayoutModule (shell, header, footer, sidebar)
│
└── Features (Lazy-Loaded)
    ├── HomeModule (/home)
    ├── AuthModule (/auth)
    ├── SeekerModule (/seeker)
    │   ├── SeekerDashboard
    │   ├── BrowseJobs
    │   ├── JobDetail + Apply Modal
    │   ├── MyApplications
    │   └── SeekerProfile
    ├── RecruiterModule (/recruiter)
    │   ├── RecruiterDashboard
    │   ├── PostJob / MyJobs / EditJob
    │   ├── ViewApplicants (ATS panel + PDF preview)
    │   └── RecruiterProfile
    └── AdminModule (/admin)
        ├── AdminDashboard
        ├── UserManagement / JobManagement
        ├── PlatformReport (Chart.js)
        ├── AuditLogs
        └── AdminProfile
```
