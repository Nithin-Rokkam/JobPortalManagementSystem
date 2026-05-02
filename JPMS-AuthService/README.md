# JPMS — Auth Service

**Port:** `8081` | **Spring Application Name:** `auth-service`

The Auth Service is the identity and access management backbone of the Joblix platform. It handles user registration with email OTP verification, JWT-based login, token refresh/logout, profile management (picture and resume uploads via Cloudinary), and password reset via OTP. It also exposes internal APIs consumed by AdminService and NotificationService.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Main Application Class](#main-application-class)
4. [Entity — User](#entity--user)
5. [Enums](#enums)
6. [DTOs](#dtos)
7. [Repository — UserRepository](#repository--userrepository)
8. [Security](#security)
9. [Service — AuthService](#service--authservice)
10. [Controllers](#controllers)
11. [Utility — CloudinaryUtil](#utility--cloudinaryutil)
12. [Configuration](#configuration)
13. [RabbitMQ Events Published](#rabbitmq-events-published)
14. [API Reference](#api-reference)

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| Security | Spring Security (stateless, JWT) |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| File Storage | Cloudinary |
| Messaging | RabbitMQ (AMQP) |
| Service Discovery | Netflix Eureka Client |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Logging | Log4j2 |
| Build | Maven |

---

## Project Structure

```
JPMS-AuthService/src/main/java/com/capg/jobportal/
├── JpmsAuthServiceApplication.java       ← Entry point
├── config/
│   ├── RabbitMQConfig.java               ← Exchange, queue, binding declarations
│   └── SwaggerConfig.java                ← OpenAPI configuration
├── controller/
│   ├── AuthController.java               ← Public + authenticated auth APIs
│   └── InternalAuthController.java       ← Internal APIs for AdminService
├── dao/
│   └── UserRepository.java               ← JPA repository for User entity
├── dto/
│   ├── AuthResponse.java                 ← Login/register response payload
│   ├── ErrorResponse.java                ← Standardized error wrapper
│   ├── LoginRequest.java                 ← Login credentials DTO
│   ├── RegisterRequest.java              ← Registration payload DTO
│   ├── UserInfoResponse.java             ← Minimal user info (id, name, email)
│   └── UserProfileResponse.java          ← Full profile response DTO
├── entity/
│   └── User.java                         ← JPA entity mapped to `users` table
├── enums/
│   ├── Role.java                         ← JOB_SEEKER | RECRUITER | ADMIN
│   └── UserStatus.java                   ← ACTIVE | BANNED | PENDING_VERIFICATION
├── event/
│   └── PasswordResetEvent.java           ← RabbitMQ event for OTP delivery
├── exception/
│   ├── GlobalExceptionhandler.java       ← @ControllerAdvice error handler
│   ├── ResourceNotFoundException.java
│   └── UserAlreadyExistsException.java
├── security/
│   ├── JwtUtil.java                      ← JWT generation and validation
│   └── SecurityConfig.java              ← Spring Security filter chain
├── service/
│   └── AuthService.java                  ← All business logic
└── util/
    └── CloudinaryUtil.java               ← Cloudinary upload helpers
```

---

## Main Application Class

```java
@SpringBootApplication
@EnableDiscoveryClient   // Registers with Eureka Server
public class JpmsAuthServiceApplication { ... }
```

**Annotations:**
- `@SpringBootApplication` — enables auto-configuration, component scan, and configuration.
- `@EnableDiscoveryClient` — registers this service with the Eureka Server so other services can discover it by name (`auth-service`).

---

## Entity — User

**Table:** `users`

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;                    // BCrypt hashed

    @Enumerated(EnumType.STRING)
    private Role role;                          // JOB_SEEKER | RECRUITER | ADMIN

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    private UserStatus status;                  // ACTIVE | BANNED | PENDING_VERIFICATION

    private String profilePictureUrl;           // Cloudinary URL
    private String resumeUrl;                   // Cloudinary URL
    private String refreshToken;                // Stored for token rotation
    private String resetPasswordOtp;            // 6-digit OTP for password reset
    private LocalDateTime otpExpiryTime;        // OTP expiry (10 min window)
    private String emailVerificationOtp;        // 6-digit OTP for email verification
    private LocalDateTime emailVerificationExpiry;
    private String companyName;                 // Recruiter only
    private String selectedByCompany;           // Set when seeker is SELECTED

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist  // Auto-sets createdAt and updatedAt on insert
    @PreUpdate   // Auto-updates updatedAt on every save
}
```

**Key JPA Annotations:**
- `@Entity` — marks this class as a JPA-managed entity.
- `@Table(name = "users")` — maps to the `users` database table.
- `@Id` + `@GeneratedValue(IDENTITY)` — auto-increment primary key.
- `@Column(unique = true)` on `email` — enforces uniqueness at DB level.
- `@Enumerated(EnumType.STRING)` — stores enum values as strings (e.g., `"RECRUITER"`) rather than ordinals.
- `@PrePersist` / `@PreUpdate` — JPA lifecycle callbacks that auto-manage timestamps.

---

## Enums

### Role
```java
public enum Role {
    JOB_SEEKER, RECRUITER, ADMIN
}
```

### UserStatus
```java
public enum UserStatus {
    ACTIVE, BANNED, PENDING_VERIFICATION
}
```
`PENDING_VERIFICATION` is the initial state after registration. The account becomes `ACTIVE` only after email OTP verification.

---

## DTOs

### RegisterRequest
```java
@Data @NoArgsConstructor
public class RegisterRequest {
    @NotBlank String name;
    @NotBlank @Email String email;
    @NotBlank @Size(min = 8) String password;
    String phone;
    @NotNull Role role;
    String companyName;   // Optional, used for RECRUITER role
}
```

### LoginRequest
```java
@Data @NoArgsConstructor
public class LoginRequest {
    @NotBlank @Email String email;
    @NotBlank @Size(min = 8) String password;
}
```

### AuthResponse
```java
@Data @NoArgsConstructor
public class AuthResponse {
    String message;
    String accessToken;
    String refreshToken;
    String role;
    Long userId;
    String name;
    String email;
}
```
Two constructors: one for message-only responses (registration), one for full token responses (login/refresh).

### UserProfileResponse
Full profile DTO returned by `GET /api/auth/profile`. Contains all non-sensitive user fields including `profilePictureUrl`, `resumeUrl`, `companyName`, `selectedByCompany`.

### UserInfoResponse
Minimal DTO (id, name, email) used for inter-service communication.

---

## Repository — UserRepository

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
    List<User> findByRole(Role role);
}
```

- `findByEmail` — used during login and registration duplicate checks.
- `findByRefreshToken` — used during token refresh and logout.
- `findByRole` — used to fetch all job seeker emails for notifications.

---

## Security

### SecurityConfig

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // CSRF disabled (stateless JWT)
        // Session: STATELESS
        // All requests permitted (auth is handled at API Gateway level)
        // Swagger endpoints explicitly permitted
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // BCrypt with strength 10
    }
}
```

**Why all requests are permitted:** The Auth Service sits behind the API Gateway which handles JWT validation. The service itself trusts the `X-User-Id` header injected by the Gateway. Spring Security is configured stateless to avoid session overhead.

### JwtUtil

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.access-expiry-ms}") private long accessExpiryMs;
    @Value("${jwt.refresh-expiry-ms}") private long refreshExpiryMs;

    public String generateAccessToken(Long userId, String role)
    // Builds JWT with subject=userId, claim "role", signed with HMAC-SHA256

    public String generateRefreshToken()
    // Returns a UUID string (stored in DB for rotation)

    public Claims extractAllClaims(String token)
    // Parses and verifies token signature, returns all claims

    public boolean isTokenValid(String token)
    // Returns true if token parses successfully and is not expired

    public String extractUserId(String token)
    // Returns the subject (userId) from token claims

    public String extractRole(String token)
    // Returns the "role" custom claim from token
}
```

---

## Service — AuthService

The core business logic class. All methods are documented below.

### `register(RegisterRequest)`
1. Blocks ADMIN self-registration.
2. Checks if email already exists:
   - If `PENDING_VERIFICATION` → updates details and generates a fresh OTP.
   - If `ACTIVE`/`BANNED` → throws `UserAlreadyExistsException`.
3. Creates new `User` with status `PENDING_VERIFICATION`.
4. Generates a 6-digit OTP, sets 10-minute expiry.
5. Uses `@Transactional` + `TransactionSynchronizationManager.registerSynchronization` to publish the `PasswordResetEvent` to RabbitMQ **after** the DB transaction commits (prevents race conditions).

### `verifyRegistrationOtp(email, otp)`
1. Finds user by email.
2. Validates status is `PENDING_VERIFICATION`.
3. Validates OTP matches and is not expired.
4. Sets status to `ACTIVE`, clears OTP fields.

### `resendRegistrationOtp(email)`
Generates a new OTP for a `PENDING_VERIFICATION` user and re-publishes the event after transaction commit.

### `login(LoginRequest)`
1. Finds user by email.
2. Checks for `BANNED` or `PENDING_VERIFICATION` status.
3. Verifies password with `BCryptPasswordEncoder.matches()`.
4. Generates new access token (JWT) and refresh token (UUID).
5. Stores refresh token in DB.
6. Returns `AuthResponse` with both tokens and user info.

### `refresh(refreshToken)`
1. Finds user by stored refresh token.
2. Checks for `BANNED` status.
3. Generates new access + refresh tokens (token rotation).
4. Updates DB with new refresh token.

### `logout(refreshToken)`
Finds user by refresh token and sets it to `null` in DB.

### `updateProfilePicture(userId, MultipartFile)`
Uploads file to Cloudinary via `CloudinaryUtil`, updates `profilePictureUrl` in DB.

### `updateProfileResume(userId, MultipartFile)`
Uploads file to Cloudinary via `CloudinaryUtil`, updates `resumeUrl` in DB.

### `getProfile(userId)`
Returns `UserProfileResponse.fromEntity(user)` — a static factory method that maps the entity to the DTO.

### `getAllUsers()`
Returns all users as `List<UserProfileResponse>`. Used by `InternalAuthController`.

### `deleteUser(userId)` / `updateUserStatus(userId, status)`
Admin operations. `updateUserStatus` also clears the refresh token (forces re-login).

### `invalidateTokenByUserId(userId)`
Clears refresh token without changing status. Called after ban.

### `forgotPassword(email)`
Generates OTP, saves with 10-minute expiry, publishes `PasswordResetEvent` to `password.reset` routing key.

### `resetPassword(email, otp, newPassword)`
Validates OTP and expiry, BCrypt-encodes new password, clears OTP fields.

### `updateCompanyName(userId, companyName)` / `updateSelectedByCompany(seekerId, companyName)`
Internal update methods for recruiter company name and seeker selection status.

---

## Controllers

### AuthController — `/api/auth/**`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new user, sends OTP |
| `POST` | `/api/auth/login` | Public | Authenticate, returns JWT tokens |
| `POST` | `/api/auth/refresh` | Public | Rotate tokens using refresh token |
| `POST` | `/api/auth/logout` | Public | Invalidate refresh token |
| `POST` | `/api/auth/verify-registration` | Public | Verify email OTP, activate account |
| `POST` | `/api/auth/resend-registration-otp` | Public | Resend email verification OTP |
| `POST` | `/api/auth/forgot-password` | Public | Request password reset OTP |
| `POST` | `/api/auth/reset-password` | Public | Reset password with OTP |
| `GET` | `/api/auth/profile` | JWT (`X-User-Id` header) | Get logged-in user profile |
| `PUT` | `/api/auth/profile/picture` | JWT | Upload profile picture (multipart) |
| `PUT` | `/api/auth/profile/resume` | JWT | Upload resume (multipart) |
| `PUT` | `/api/auth/profile/company` | JWT | Update recruiter company name |

**Key annotations used:**
- `@RestController` — combines `@Controller` + `@ResponseBody`.
- `@RequestMapping("/api/auth")` — base path for all endpoints.
- `@Tag(name = "Auth APIs")` — Swagger grouping.
- `@Operation(summary = "...")` — Swagger endpoint description.
- `@Parameter(description = "...")` — Swagger parameter documentation.
- `@Valid` on `@RequestBody` — triggers Bean Validation on DTOs.
- `@RequestHeader("X-User-Id")` — reads the user ID injected by the API Gateway.
- `@RequestPart("picture")` — reads multipart file from form data.

### InternalAuthController — `/api/internal/**`

Annotated with `@Hidden` (excluded from Swagger). Strictly for inter-service calls — blocked from external access by the API Gateway.

| Method | Endpoint | Used By | Description |
|---|---|---|---|
| `GET` | `/api/internal/users` | AdminService | Fetch all users |
| `DELETE` | `/api/internal/users/{id}` | AdminService | Delete user |
| `PUT` | `/api/internal/users/{id}/ban` | AdminService | Ban user |
| `PUT` | `/api/internal/users/{id}/unban` | AdminService | Unban user |
| `PUT` | `/api/internal/users/{id}/invalidate-token` | AdminService | Clear refresh token |
| `GET` | `/api/internal/users/job-seeker-emails` | NotificationService | All active seeker emails |
| `GET` | `/api/internal/users/{id}/info` | NotificationService, AppService | Minimal user info |
| `PUT` | `/api/internal/users/{seekerId}/selected-company` | ApplicationService | Update selected company |

---

## Utility — CloudinaryUtil

Wraps the Cloudinary SDK. Provides:
- `uploadProfilePicture(MultipartFile)` → uploads to `profile_pictures/` folder, returns URL.
- `uploadResume(MultipartFile)` → uploads to `resumes/` folder with `raw` resource type, returns URL.

Configured via `cloudinary.cloud-name`, `cloudinary.api-key`, `cloudinary.api-secret` from `application.yml`.

---

## Configuration

### application.yml (key settings)

```yaml
server.port: 8081
spring.application.name: auth-service
spring.datasource.url: jdbc:mysql://jobportal-db:3306/jobportal_db
jwt.secret: ${AUTH_SERVICE_JWT_SECRET}
jwt.access-expiry-ms: ${AUTH_SERVICE_JWT_ACCESS_EXPIRY}
jwt.refresh-expiry-ms: ${AUTH_SERVICE_JWT_REFRESH_EXPIRY}
cloudinary.cloud-name: ${AUTH_SERVICE_CLOUDINARY_CLOUD_NAME}
eureka.client.service-url.defaultZone: http://eureka-server:8761/eureka/
```

All sensitive values are injected from environment variables (`.env` file).

---

## RabbitMQ Events Published

| Event Class | Exchange | Routing Key | Trigger |
|---|---|---|---|
| `PasswordResetEvent` | `jobportal.exchange` | `password.reset` | `forgotPassword()` |
| `PasswordResetEvent` | `jobportal.exchange` | `registration.otp` | `register()` / `resendRegistrationOtp()` |

Both events carry `email`, `name`, and `otp` fields. The NotificationService listens on the corresponding queues and sends the email.

> Events are published **after transaction commit** using `TransactionSynchronizationManager` to guarantee DB consistency before the notification fires.
