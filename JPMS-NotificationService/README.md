# JPMS — Notification Service

**Port:** `8085` | **Spring Application Name:** `notification-service`

The Notification Service is a purely event-driven microservice with no REST endpoints. It listens to RabbitMQ queues and sends transactional emails in response to platform events: job postings, job applications, application status changes, password resets, and email verification OTPs. It uses a Feign client to fetch user details from the Auth Service when needed.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Main Application Class](#main-application-class)
4. [RabbitMQ Events Consumed](#rabbitmq-events-consumed)
5. [Feign Client — AuthServiceClient](#feign-client--authserviceclient)
6. [Listeners](#listeners)
7. [Service — EmailService](#service--emailservice)
8. [Configuration](#configuration)

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| Messaging | RabbitMQ (AMQP) — consumer only |
| Email | JavaMailSender (SMTP) |
| Inter-Service | OpenFeign |
| Service Discovery | Netflix Eureka Client |
| Logging | Log4j2 |
| Build | Maven |

---

## Project Structure

```
JPMS-NotificationService/src/main/java/com/capg/jobportal/
├── JpmsNotificationServiceApplication.java  ← Entry point
├── client/
│   └── AuthServiceClient.java               ← Feign client → AuthService
├── config/
│   └── RabbitMQConfig.java                  ← Queue and exchange declarations
├── dto/
│   └── UserInfoResponse.java                ← User info from AuthService
├── event/
│   ├── ApplicationStatusChangedEvent.java   ← Status change event payload
│   ├── JobAppliedEvent.java                 ← Job application event payload
│   ├── JobPostedEvent.java                  ← Job posted event payload
│   └── PasswordResetEvent.java              ← OTP event payload
├── listener/
│   ├── ApplicationStatusChangedListener.java ← Handles SHORTLISTED/SELECTED/REJECTED
│   ├── JobAppliedListener.java              ← Handles new job applications
│   ├── JobPostedListener.java               ← Handles new job postings
│   ├── PasswordResetListener.java           ← Handles password reset OTPs
│   └── RegistrationOtpListener.java         ← Handles email verification OTPs
└── service/
    └── EmailService.java                    ← All email sending logic
```

---

## Main Application Class

```java
@SpringBootApplication
@EnableFeignClients   // Activates AuthServiceClient
public class JpmsNotificationServiceApplication { ... }
```

**Annotations:**
- `@EnableFeignClients` — activates the `AuthServiceClient` Feign interface for calling AuthService.

> Note: No `@EnableDiscoveryClient` is explicitly present, but Eureka client is active via the starter dependency.

---

## RabbitMQ Events Consumed

| Queue | Routing Key | Published By | Listener |
|---|---|---|---|
| `job.posted.queue` | `job.posted` | JobService | `JobPostedListener` |
| `job.applied.queue` | `job.applied` | ApplicationService | `JobAppliedListener` |
| `application.status.queue` | `application.status.changed` | ApplicationService | `ApplicationStatusChangedListener` |
| `password.reset.queue` | `password.reset` | AuthService | `PasswordResetListener` |
| `registration.otp.queue` | `registration.otp` | AuthService | `RegistrationOtpListener` |

All queues are bound to the `jobportal.exchange` (topic or direct exchange declared in `RabbitMQConfig`).

---

## Feign Client — AuthServiceClient

```java
@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthServiceClient {
    @GetMapping("/api/internal/users/job-seeker-emails")
    List<String> getJobSeekerEmails();

    @GetMapping("/api/internal/users/{id}/info")
    UserInfoResponse getUserInfo(@PathVariable("id") Long userId);
}
```

- `getJobSeekerEmails()` — fetches all active job seeker email addresses. Used by `JobPostedListener` to send job alerts.
- `getUserInfo(id)` — fetches name and email for a specific user. Used by `JobAppliedListener` to get recruiter details.

> Uses `url = "${auth.service.url}"` (direct URL) rather than Eureka service name to avoid routing through the Gateway for internal calls.

---

## Listeners

### JobPostedListener

```java
@Component
public class JobPostedListener {
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleJobPosted(JobPostedEvent event) { ... }
}
```

**On receiving `JobPostedEvent`:**
1. Fetches recruiter info via `AuthServiceClient.getUserInfo(event.getRecruiterId())`.
2. Sends job posting confirmation email to the recruiter.
3. Fetches all active job seeker emails via `AuthServiceClient.getJobSeekerEmails()`.
4. Sends job alert email to each job seeker.

Each email send is wrapped in a try-catch so a failure for one recipient does not stop the others.

---

### JobAppliedListener

```java
@Component
public class JobAppliedListener {
    @RabbitListener(queues = "${rabbitmq.applied.queue}")
    public void handleJobApplied(JobAppliedEvent event) { ... }
}
```

**On receiving `JobAppliedEvent`:**
1. Fetches recruiter info via `AuthServiceClient.getUserInfo(event.getRecruiterId())`.
2. Sends application alert email to the recruiter with applicant name and email.

---

### ApplicationStatusChangedListener

```java
@Component
public class ApplicationStatusChangedListener {
    @RabbitListener(queues = "${rabbitmq.status.queue}")
    public void handleStatusChange(ApplicationStatusChangedEvent event) { ... }
}
```

**On receiving `ApplicationStatusChangedEvent`:**
- If `newStatus == "SHORTLISTED"` → sends congratulatory shortlisting email to seeker.
- If `newStatus == "REJECTED"` → sends formal rejection email to seeker.
- If `newStatus == "SELECTED"` → sends final selection/offer email to seeker.

Each branch is independently try-catched with error logging.

---

### PasswordResetListener

```java
@Component
public class PasswordResetListener {
    @RabbitListener(queues = "password.reset.queue")
    public void handlePasswordReset(PasswordResetEvent event) { ... }
}
```

**On receiving `PasswordResetEvent`:**
Sends OTP email for password reset with 10-minute validity notice.

---

### RegistrationOtpListener

```java
@Component
public class RegistrationOtpListener {
    @RabbitListener(queues = "registration.otp.queue")
    public void handleRegistrationOtp(PasswordResetEvent event) { ... }
}
```

**On receiving `PasswordResetEvent` (reused for registration OTP):**
Sends email verification OTP for new account activation.

---

## Service — EmailService

All email sending is centralized in `EmailService`. Uses `JavaMailSender` (Spring Mail) with `SimpleMailMessage`.

```java
@Service
public class EmailService {
    @Autowired private JavaMailSender mailSender;
    @Value("${spring.mail.username}") private String senderEmail;
    ...
}
```

### Methods

| Method | Recipient | Subject | Trigger |
|---|---|---|---|
| `sendJobAlert(toEmail, JobPostedEvent)` | Job Seeker | "New Job Alert: {title} at {company}" | New job posted |
| `sendJobPostedConfirmation(recruiterEmail, JobPostedEvent)` | Recruiter | "Job Posted Successfully: {title}" | New job posted |
| `sendApplicationAlert(recruiterEmail, JobAppliedEvent)` | Recruiter | "New Application Received: {title}" | Seeker applies |
| `sendShortlistedEmail(ApplicationStatusChangedEvent)` | Seeker | "Congratulations! You've been Shortlisted for {title}" | Status → SHORTLISTED |
| `sendRejectedEmail(ApplicationStatusChangedEvent)` | Seeker | "Update regarding your application for {title}" | Status → REJECTED |
| `sendSelectedEmail(ApplicationStatusChangedEvent)` | Seeker | "Selection Confirmation: {title}" | Status → SELECTED |
| `sendOtpEmail(toEmail, name, otp)` | User | "Joblix - Password Reset OTP" | Forgot password |
| `sendRegistrationOtpEmail(toEmail, name, otp)` | User | "Joblix - Verify Your Email Address" | Registration |

All emails are plain text (`SimpleMailMessage`). The sender address is read from `${spring.mail.username}`.

---

## Configuration

### application.yml (key settings)

```yaml
spring.application.name: notification-service
spring.mail.host: smtp.gmail.com
spring.mail.port: 587
spring.mail.username: ${EMAIL}
spring.mail.password: ${EMAIL_PASSWORD}   # Gmail App Password
spring.mail.properties.mail.smtp.auth: true
spring.mail.properties.mail.smtp.starttls.enable: true
rabbitmq.queue: job.posted.queue
rabbitmq.applied.queue: job.applied.queue
rabbitmq.status.queue: application.status.queue
auth.service.url: http://auth-service:8081
eureka.client.service-url.defaultZone: http://eureka-server:8761/eureka/
```

> `EMAIL_PASSWORD` must be a Gmail **App Password** (not the account password). Generate one at Google Account → Security → 2-Step Verification → App Passwords.
