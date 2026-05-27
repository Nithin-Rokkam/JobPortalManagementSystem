# Technical Documentation — Joblix Job Portal Management System

---

## 1. Project Root Structure

```
Sprint1-JobPortal-Management-System-main/
├── .env                          ← All secrets and env vars (not committed to VCS)
├── docker-compose.yml            ← Full stack orchestration (10 containers)
├── pom.xml                       ← Maven parent POM (aggregator)
├── README.md                     ← Main project documentation
├── EventDrivenReadme.md          ← Step-by-step event flow documentation
├── docs/                         ← Case study / business requirements (Word doc)
├── JPMS-EurekaServer/            ← Service registry
├── JPMS-ApiGateWay/              ← API Gateway + JWT filter
├── JPMS-AuthService/             ← Authentication + user management
├── JPMS-JobService/              ← Job listings management
├── JPMS-ApplicationService/      ← Job applications management
├── JPMS-AdminService/            ← Platform administration
├── JPMS-NotificationService/     ← Event-driven email notifications
├── jpms-frontend/                ← Angular 19+ SPA
└── jacoco-aggregator/            ← Aggregate JaCoCo code coverage report
```

---

## 2. Service Module Structures

### 2.1 Auth Service (`JPMS-AuthService`)

```
src/main/java/com/capg/jobportal/
├── JpmsAuthServiceApplication.java
├── config/
│   ├── SecurityConfig.java        ← Permits all (Gateway handles JWT)
│   ├── RabbitMQConfig.java        ← Declares exchange, queues, bindings
│   └── CloudinaryConfig.java      ← Cloudinary SDK bean configuration
├── controller/
│   ├── AuthController.java        ← Public + JWT-protected auth APIs (/api/auth/**)
│   └── InternalAuthController.java ← Feign-only internal APIs (/api/internal/users/**)
├── service/
│   └── AuthService.java           ← All business logic (650+ lines)
├── dao/
│   └── UserRepository.java        ← Spring Data JPA repository
├── entity/
│   └── User.java                  ← @Entity for users table
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── UserProfileResponse.java
│   ├── UserInfoResponse.java
│   └── ErrorResponse.java
├── enums/
│   ├── Role.java                  ← JOB_SEEKER, RECRUITER, ADMIN
│   └── UserStatus.java            ← ACTIVE, BANNED, PENDING_VERIFICATION
├── event/
│   └── PasswordResetEvent.java    ← AMQP payload (used for both OTP types)
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── UserAlreadyExistsException.java
│   └── GlobalExceptionHandler.java ← @RestControllerAdvice
├── security/
│   └── JwtUtil.java               ← Token generation and validation
└── util/
    └── CloudinaryUtil.java        ← Profile picture + resume upload
```

---

### 2.2 Job Service (`JPMS-JobService`)

```
src/main/java/com/capg/jobportal/
├── JpmsJobServiceApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── RabbitMQConfig.java
├── controller/
│   ├── JobController.java         ← /api/jobs/**
│   └── InternalJobController.java ← /api/internal/jobs/**
├── service/
│   └── JobService.java            ← CRUD + search + RabbitMQ publish
├── repository/
│   └── JobRepository.java
├── entity/
│   └── Job.java
├── dto/
│   ├── JobRequestDTO.java
│   ├── JobResponseDTO.java
│   └── PagedResponse.java         ← Generic paginated wrapper
├── event/
│   └── JobPostedEvent.java
├── enums/
│   ├── JobType.java
│   └── JobStatus.java
├── Exceptions/
│   └── GlobalExceptionHandler.java
└── security/
    └── JwtUtil.java               ← Local copy for any token extraction needs
```

---

### 2.3 Application Service (`JPMS-ApplicationService`)

```
src/main/java/com/capg/jobportal/
├── JpmsApplicationServiceApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── RabbitMQConfig.java
│   └── FeignConfig.java
├── controller/
│   ├── ApplicationController.java         ← /api/applications/**
│   └── InternalApplicationController.java ← /api/internal/applications/**
├── service/
│   └── ApplicationService.java
├── dao/
│   └── ApplicationRepository.java
├── entity/
│   └── Application.java
├── dto/
│   ├── ApplicationResponse.java
│   ├── RecruiterApplicationResponse.java
│   ├── StatusUpdateRequest.java
│   └── ApplicationStats.java
├── client/
│   ├── AuthServiceClient.java     ← Feign → Auth Service
│   └── JobServiceClient.java      ← Feign → Job Service
├── event/
│   ├── JobAppliedEvent.java
│   └── ApplicationStatusChangedEvent.java
├── enums/
│   └── ApplicationStatus.java
├── exception/
│   └── GlobalExceptionHandler.java
└── util/
    └── CloudinaryUtil.java
```

---

### 2.4 Admin Service (`JPMS-AdminService`)

```
src/main/java/com/capg/jobportal/
├── JpmsAdminServiceApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── FeignConfig.java
├── controller/
│   └── AdminController.java       ← /api/admin/**
├── service/
│   └── AdminService.java
├── model/
│   └── AuditLog.java
├── repository/
│   └── AuditLogRepository.java
├── client/
│   ├── AuthServiceClient.java
│   ├── AdminJobClient.java
│   └── AdminAppClient.java
├── dto/
│   ├── UserResponse.java
│   ├── JobResponse.java
│   ├── PlatformReport.java
│   └── ApplicationStats.java
├── exception/
│   ├── AccessDeniedException.java
│   └── GlobalExceptionHandler.java
└── security/
    └── (minimal — role assertion in controller)
```

---

### 2.5 Notification Service (`JPMS-NotificationService`)

```
src/main/java/com/capg/jobportal/
├── JpmsNotificationServiceApplication.java
├── config/
│   └── RabbitMQConfig.java        ← Queue declarations matching publishers
├── listener/
│   ├── JobPostedListener.java
│   ├── JobAppliedListener.java
│   ├── ApplicationStatusChangedListener.java
│   ├── PasswordResetListener.java
│   └── RegistrationOtpListener.java
├── service/
│   └── EmailService.java          ← HTML email templates per event type
├── client/
│   └── AuthServiceClient.java     ← Feign → Auth Service
├── event/
│   ├── JobPostedEvent.java
│   ├── JobAppliedEvent.java
│   ├── ApplicationStatusChangedEvent.java
│   └── PasswordResetEvent.java
└── dto/
    └── UserInfoResponse.java
```

---

### 2.6 API Gateway (`JPMS-ApiGateWay`)

```
src/main/java/com/capg/jobportal/
├── JpmsApiGateWayApplication.java ← @EnableDiscoveryClient
├── config/
│   └── CorsConfig.java            ← CORS for localhost:4200
├── filter/
│   └── GatewayJwtFilter.java      ← GlobalFilter, Ordered(-1), JWT validation
└── util/
    └── JwtUtil.java               ← Stateless token parsing (no DB)
```

---

### 2.7 Angular Frontend (`jpms-frontend`)

```
src/app/
├── app.ts                         ← Root component
├── app.html
├── app-module.ts                  ← Root NgModule
├── app-routing-module.ts          ← Lazy-loaded routes
├── core/
│   ├── core.module.ts
│   ├── guards/
│   │   ├── auth.guard.ts          ← Checks isLoggedIn()
│   │   └── role.guard.ts          ← Checks getRole() vs route.data.roles
│   ├── interceptors/
│   │   └── auth.interceptor.ts    ← Attaches JWT + handles 401 refresh
│   └── services/
│       ├── auth.service.ts        ← BehaviorSubject<User>, localStorage mgmt
│       ├── toast.service.ts       ← Toast notification service
│       └── loader.service.ts      ← Loading spinner service
├── layout/
│   ├── layout.module.ts
│   ├── shell/                     ← Authenticated layout wrapper
│   ├── header/                    ← Top navigation bar (avatar, role nav)
│   ├── sidebar/                   ← Role-specific sidebar
│   └── footer/
├── features/
│   ├── home/ (HomeModule)         ← Public landing page
│   ├── auth/ (AuthModule)
│   │   ├── services/auth-api.service.ts  ← HTTP calls to /api/auth/**
│   │   └── components/
│   │       ├── login/
│   │       ├── register/
│   │       ├── verify-registration/
│   │       ├── forgot-password/
│   │       └── reset-password/
│   ├── seeker/ (SeekerModule)
│   │   ├── services/ (jobs API, applications API)
│   │   └── components/
│   │       ├── seeker-dashboard/
│   │       ├── browse-jobs/
│   │       ├── job-detail/        ← Apply modal embedded
│   │       ├── my-applications/
│   │       ├── saved-jobs/
│   │       └── seeker-profile/
│   ├── recruiter/ (RecruiterModule)
│   │   ├── services/ (jobs API, applications API)
│   │   └── components/
│   │       ├── recruiter-dashboard/
│   │       ├── post-job/
│   │       ├── my-jobs/
│   │       ├── edit-job/
│   │       ├── view-applicants/   ← ATS panel + PDF preview sidebar
│   │       ├── all-applicants/
│   │       └── recruiter-profile/
│   └── admin/ (AdminModule)
│       ├── services/ (admin API)
│       └── components/
│           ├── admin-dashboard/
│           ├── user-management/
│           ├── job-management/
│           ├── platform-report/   ← Chart.js integration
│           ├── audit-logs/
│           └── admin-profile/
└── shared/
    └── components/
        └── unauthorized/          ← Shown on role mismatch
```

---

## 3. Configuration Overview

### 3.1 Service application.yml Settings

| Setting | Auth (8081) | Job (8082) | App (8083) | Admin (8084) | Notification (8085) | Gateway (9090) |
|---|---|---|---|---|---|---|
| `server.port` | 8081 | 8082 | 8083 | 8084 | 8085 | 9090 |
| `spring.application.name` | auth-service | job-service | application-service | admin-service | notification-service | api-gateway |
| `spring.datasource.url` | jdbc:mysql://... | jdbc:mysql://... | jdbc:mysql://... | jdbc:mysql://... | — | — |
| `spring.jpa.ddl-auto` | update | update | update | update | — | — |
| `spring.rabbitmq.host` | rabbitmq | rabbitmq | rabbitmq | — | rabbitmq | — |
| `eureka.client.service-url` | :8761/eureka/ | :8761/eureka/ | :8761/eureka/ | :8761/eureka/ | :8761/eureka/ | :8761/eureka/ |
| `jwt.secret` | ${AUTH_SERVICE_JWT_SECRET} | — | — | — | — | ${API_GATEWAY_JWT_SECRET} |
| `cloudinary.*` | ✅ | — | ✅ | — | — | — |

### 3.2 RabbitMQ Configuration (Declared by each publisher/consumer)

| Property | Value |
|---|---|
| Exchange name | `jobportal.exchange` |
| Exchange type | Topic (or Direct — both work for exact routing keys used) |
| Queue durability | Durable (survives broker restart) |
| Message format | JSON (via Jackson `MessageConverter`) |

---

## 4. Environment Variable Reference (`.env`)

| Variable | Service | Description |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Docker MySQL | Root DB password |
| `AUTH_SERVICE_DB_USERNAME` | Auth Service | DB user |
| `AUTH_SERVICE_DB_PASSWORD` | Auth Service | DB password |
| `AUTH_SERVICE_JWT_SECRET` | Auth Service | JWT signing secret (must match Gateway) |
| `AUTH_SERVICE_JWT_ACCESS_EXPIRY` | Auth Service | Access token TTL in ms (e.g., 9000000 = 2.5h) |
| `AUTH_SERVICE_JWT_REFRESH_EXPIRY` | Auth Service | Refresh token TTL in ms (e.g., 604800000 = 7 days) |
| `AUTH_SERVICE_CLOUDINARY_CLOUD_NAME` | Auth Service | Cloudinary cloud name |
| `AUTH_SERVICE_CLOUDINARY_API_KEY` | Auth Service | Cloudinary API key |
| `AUTH_SERVICE_CLOUDINARY_API_SECRET` | Auth Service | Cloudinary API secret |
| `JOB_SERVICE_DB_USERNAME` | Job Service | DB user |
| `JOB_SERVICE_DB_PASSWORD` | Job Service | DB password |
| `APPLICATION_SERVICE_DB_USERNAME` | Application Service | DB user |
| `APPLICATION_SERVICE_DB_PASSWORD` | Application Service | DB password |
| `APPLICATION_SERVICE_CLOUDINARY_*` | Application Service | Cloudinary credentials (separate set) |
| `ADMIN_SERVICE_DB_USERNAME` | Admin Service | DB user |
| `ADMIN_SERVICE_DB_PASSWORD` | Admin Service | DB password |
| `API_GATEWAY_JWT_SECRET` | API Gateway | Must equal `AUTH_SERVICE_JWT_SECRET` |
| `EMAIL` | Notification Service | Gmail address (sender) |
| `EMAIL_PASSWORD` | Notification Service | Gmail App Password (not account password) |

---

## 5. Dependency Mapping (Key Maven Dependencies)

### All Services (via Spring Boot 3.x parent BOM)
- `spring-boot-starter-web` — REST controllers
- `spring-boot-starter-actuator` — health endpoints
- `spring-boot-starter-data-jpa` — ORM
- `mysql-connector-j` — MySQL JDBC driver
- `lombok` — `@Getter`, `@Setter`, `@NoArgsConstructor`
- `spring-cloud-starter-netflix-eureka-client` — service registration

### Auth Service / Application Service (additional)
- `spring-boot-starter-amqp` — RabbitMQ / AMQP
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` — JWT generation and validation
- `spring-boot-starter-security` — BCrypt password encoder
- `cloudinary-http44` — Cloudinary Java SDK
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI

### Job Service (additional)
- `spring-boot-starter-amqp` — RabbitMQ publisher
- `spring-cloud-starter-openfeign` — Feign client (internal use)

### Admin / Application Services (additional)
- `spring-cloud-starter-openfeign` — Feign clients to other services

### API Gateway (different stack)
- `spring-cloud-starter-gateway` — Reactive gateway
- `spring-cloud-starter-netflix-eureka-client`
- `jjwt-*` — Local JWT validation (no call to Auth Service)

### Notification Service
- `spring-boot-starter-amqp` — Consumer
- `spring-boot-starter-mail` — JavaMailSender
- `spring-cloud-starter-openfeign` — Feign to Auth Service

### Angular Frontend
- `@angular/core` ~19.x
- `@angular/router` — lazy-loaded modules
- `@angular/common/http` — HttpClient + Interceptors
- `rxjs` — BehaviorSubject, Observable
- `chart.js` — Platform report charts (Admin module)

---

## 6. Build & Runtime Flow

### Backend Build
```bash
# Each service builds independently
cd JPMS-AuthService
./mvnw clean package -DskipTests

# Or build all via Docker Compose (builds Dockerfile in each service dir)
docker-compose up --build
```

**Dockerfile pattern** (multi-stage, each service):
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Build
```bash
cd jpms-frontend
npm install
npm run dev          # Development server on :4200
npm run build        # Production build → dist/ (served by Nginx in Docker)
```

### Docker Compose Startup Order
```
1. MySQL (health: mysqladmin ping)
2. RabbitMQ (health: rabbitmq-diagnostics ping)
3. Zipkin (health: /health)
4. Eureka Server (health: /actuator/health)
5. API Gateway (health: /actuator/health) — depends on Eureka, Zipkin
6. Auth Service (health: /actuator/health) — depends on DB, RabbitMQ, Eureka, Zipkin
7. Job Service (health: /actuator/health) — depends on DB, RabbitMQ, Eureka, Zipkin
8. Application Service — same as Job Service
9. Admin Service — depends on all above + Auth/Job/App services being healthy
10. Notification Service — depends on RabbitMQ, Eureka, Zipkin
11. Angular Frontend (Nginx) — depends on API Gateway healthy
```

---

## 7. Integration Points

| Integration | Protocol | Direction | Config |
|---|---|---|---|
| Services → Eureka | HTTP REST | Outbound | `eureka.client.service-url.defaultZone` |
| Gateway → Services | HTTP (reactive) | Internal | `lb://service-name` routes in application.yml |
| Services → RabbitMQ | AMQP | Outbound (publish) | `spring.rabbitmq.host`, port 5672 |
| Notification → RabbitMQ | AMQP | Inbound (consume) | `@RabbitListener(queues = "${rabbitmq.*.queue}")` |
| Services → MySQL | JDBC | Outbound | `spring.datasource.url`, credentials via env |
| Auth/App → Cloudinary | HTTPS | Outbound | Cloudinary SDK, credentials via env |
| Notification → Gmail | SMTP/TLS | Outbound | `spring.mail.host=smtp.gmail.com`, port 587 |
| Services → Zipkin | HTTP | Outbound | Micrometer tracing auto-configuration |
| Frontend → Gateway | HTTP REST | Outbound | `environment.apiUrl = http://localhost:9090` |
| Feign (App → Auth) | HTTP (Eureka) | Internal | `@FeignClient(name = "auth-service")` |
| Feign (Admin → *) | HTTP (Eureka) | Internal | Multiple `@FeignClient` per target service |

---

## 8. Logging Strategy

**Framework:** Apache Log4j2 (via `spring-boot-starter-log4j2`)

**Pattern used in all services:**
```java
private static final Logger logger = LogManager.getLogger(ClassName.class);
```

**Log levels per operation type:**
| Level | Usage |
|---|---|
| `INFO` | Successful operations: login, register, job created, application submitted |
| `WARN` | Security events: banned user login, invalid OTP, forbidden role access |
| `DEBUG` | Token operations: refresh, profile fetch |
| `ERROR` | Email send failures in NotificationService |

**Log output:** Console (stdout) — captured by Docker logging driver. No file appender configured in the implementation reviewed.

---

## 9. Testing Components

**Framework:** JUnit 5 + Mockito + Spring Boot Test

**Coverage Tool:** JaCoCo (with `jacoco-aggregator` Maven module for aggregate reports)

**Test types per service:**

| Service | Controller Tests | Service Tests | Coverage Target |
|---|---|---|---|
| Auth Service | `AuthControllerTest` | `AuthServiceTest` | 95%+ |
| Job Service | `JobControllerTest` | `JobServiceTest` | 95%+ |
| Application Service | `ApplicationControllerTest` | `ApplicationServiceTest` | 95%+ |
| Admin Service | `AdminControllerTest` | `AdminServiceTest` | 95%+ |
| Notification Service | Listener tests | EmailService tests | 95%+ |
| API Gateway | Gateway filter tests | JwtUtil tests | 95%+ |
| Eureka Server | Application context test | — | 100% |

**JaCoCo Aggregator** (`jacoco-aggregator/`) produces a unified HTML report across all service modules via the `jacoco-maven-plugin` aggregate goal.

---

## 10. Developer Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop
- Valid `.env` file at project root

### Local Development (without Docker)
```bash
# 1. Start infrastructure only
docker-compose up mysql rabbitmq zipkin eureka-server -d

# 2. Start each service individually (with IDE or Maven)
cd JPMS-AuthService && ./mvnw spring-boot:run
cd JPMS-JobService  && ./mvnw spring-boot:run
# ... etc

# 3. Start frontend
cd jpms-frontend && npm install && npm run dev
```

### Full Stack (Docker Compose)
```bash
# From project root
docker-compose up --build

# Verify all services are UP
# http://localhost:8761  → Eureka Dashboard
# http://localhost:15672 → RabbitMQ UI (guest/guest)
# http://localhost:9090/swagger-ui/index.html → API docs
# http://localhost:4200  → Angular frontend
# http://localhost:9411  → Zipkin traces
```

### Running Tests
```bash
# Run all tests with coverage
./mvnw test -pl JPMS-AuthService,JPMS-JobService,JPMS-ApplicationService,JPMS-AdminService,JPMS-NotificationService,JPMS-ApiGateWay

# Aggregate JaCoCo report
./mvnw verify -pl jacoco-aggregator
# Report at: jacoco-aggregator/target/site/jacoco-aggregate/index.html
```
