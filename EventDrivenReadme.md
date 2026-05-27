# Joblix — Event-Driven Data Flow

This document traces exactly how data moves through the Joblix platform for every major user action — from the Angular frontend, through the API Gateway, into the microservices, into the database, back to the frontend, and across services via RabbitMQ.

---

## Architecture Overview

```
Browser (Angular)
      │  HTTP + JWT
      ▼
API Gateway (port 9090)          ← validates JWT, injects X-User-Id / X-User-Role
      │  routes by path prefix
      ├──▶ AuthService    (8081)  ← users table
      ├──▶ JobService     (8082)  ← jobs table
      ├──▶ AppService     (8083)  ← applications table
      └──▶ AdminService   (8084)  ← audit_logs table

Services publish events to:
      RabbitMQ (jobportal.exchange)
            └──▶ NotificationService (8085) ← sends emails via SMTP
```

---

## Event 1 — User Registration (Sign Up)

### Step-by-step data flow

```
1. USER fills RegisterComponent form
   Payload sent:
   {
     name: "Nithin Kumar",
     email: "nithin@example.com",
     password: "secret123",
     phone: "9876543210",
     role: "JOB_SEEKER",          // or "RECRUITER"
     companyName: "Acme Ltd"      // only for RECRUITER
   }

2. RegisterComponent.onSubmit()
   → AuthApiService.register(formData)
   → POST /api/auth/register

3. API Gateway
   → Public route — no JWT check
   → Forwards to AuthService (lb://auth-service)

4. AuthController.register(@RequestBody RegisterRequest)
   → AuthService.register(request)

5. AuthService.register()
   a. Validates role != ADMIN
   b. Checks if email already exists in users table
      - If PENDING_VERIFICATION → updates record, generates fresh OTP
      - If ACTIVE/BANNED → throws UserAlreadyExistsException (409)
   c. Generates 6-digit OTP: String.format("%06d", random.nextInt(999999))
   d. Creates User entity:
      {
        name, email, password (BCrypt hashed),
        role, phone, companyName (RECRUITER only),
        status: PENDING_VERIFICATION,
        emailVerificationOtp: "482931",
        emailVerificationExpiry: now + 10 minutes
      }
   e. userRepository.saveAndFlush(user)  ← DB write (users table)
   f. After transaction commits:
      rabbitTemplate.convertAndSend(
        "jobportal.exchange",
        "registration.otp",
        PasswordResetEvent { email, name, otp }
      )

6. RabbitMQ routes PasswordResetEvent to registration.otp.queue

7. NotificationService — RegistrationOtpListener.handleRegistrationOtp()
   → EmailService.sendRegistrationOtpEmail(email, name, otp)
   → SMTP sends: "Joblix - Verify Your Email Address" with OTP

8. AuthController returns to frontend:
   HTTP 200: { message: "OTP sent to your email. Please verify to activate your account." }

9. RegisterComponent receives response
   → router.navigate(['/auth/verify-registration'], { queryParams: { email } })

10. VerifyRegistrationComponent shown
    User enters 6-digit OTP (or "000000" for test bypass)

11. AuthApiService.verifyRegistrationOtp(email, otp)
    → POST /api/auth/verify-registration { email, otp }

12. AuthService.verifyRegistrationOtp()
    a. Finds user by email
    b. Validates status == PENDING_VERIFICATION
    c. If otp == "000000" → bypass (test mode), skip OTP check
       Else → validates otp matches stored value and is not expired
    d. user.status = ACTIVE
       user.emailVerificationOtp = null
       user.emailVerificationExpiry = null
    e. userRepository.save(user)  ← DB update (users table)

13. HTTP 200: { message: "Email verified successfully. You can now log in." }

14. VerifyRegistrationComponent
    → toast.success("Account activated!")
    → router.navigate(['/auth/login'])
```

**Data stored in DB after registration:**
```
users table row:
  id              | auto-generated
  name            | "Nithin Kumar"
  email           | "nithin@example.com"
  password        | "$2a$10$..." (BCrypt hash)
  role            | "JOB_SEEKER"
  status          | "ACTIVE"
  phone           | "9876543210"
  refresh_token   | null (set on login)
  created_at      | timestamp
```

---

## Event 2 — User Login

### Step-by-step data flow

```
1. USER fills LoginComponent form
   { email: "nithin@example.com", password: "secret123" }

2. AuthApiService.login(credentials)
   → POST /api/auth/login

3. API Gateway → public route → AuthService

4. AuthService.login()
   a. Finds user by email
   b. Checks status != BANNED, != PENDING_VERIFICATION
   c. passwordEncoder.matches(rawPassword, hashedPassword)
   d. Generates JWT access token:
      jwtUtil.generateAccessToken(userId, role)
      Payload: { sub: "42", role: "JOB_SEEKER", iat, exp }
      Signed with HMAC-SHA256 using jwt.secret
   e. Generates UUID refresh token
   f. user.refreshToken = refreshToken
      userRepository.save(user)  ← DB update (users table)

5. HTTP 200: AuthResponse {
     accessToken:  "eyJhbGci...",
     refreshToken: "uuid-string",
     userId:       42,
     name:         "Nithin Kumar",
     email:        "nithin@example.com",
     role:         "JOB_SEEKER"
   }

6. LoginComponent.onSubmit() success handler:
   auth.saveTokens(res.accessToken, res.refreshToken)
   → localStorage.setItem("joblix_access_token", accessToken)
   → localStorage.setItem("joblix_refresh_token", refreshToken)

   auth.saveUser({ id, name, email, role, profilePictureUrl, resumeUrl })
   → localStorage.setItem("joblix_user", JSON.stringify(user))
   → currentUserSubject.next(user)   ← BehaviorSubject emits new value

7. HeaderComponent (subscribed to currentUser$) re-renders:
   - Shows user avatar / initials
   - Shows role-specific nav links

8. auth.redirectByRole()
   JOB_SEEKER  → /seeker/dashboard
   RECRUITER   → /recruiter/dashboard
   ADMIN       → /admin/dashboard
```

**State maintained in frontend after login:**
```
localStorage:
  joblix_access_token   → JWT (short-lived, e.g. 2.5 hours)
  joblix_refresh_token  → UUID (long-lived, e.g. 7 days)
  joblix_user           → { id, name, email, role, profilePictureUrl, resumeUrl }

AuthService.currentUserSubject (BehaviorSubject):
  → All components subscribing to currentUser$ get the user object reactively
  → HeaderComponent uses it for avatar, nav links, dropdown
  → Guards use auth.isLoggedIn() and auth.getRole() for route protection
```

---

## Event 3 — JWT Token Refresh (Automatic)

### Step-by-step data flow

```
1. User makes any API call with an expired access token

2. API Gateway GatewayJwtFilter:
   jwtUtil.isTokenValid(token) → false (expired)
   → Returns HTTP 401 Unauthorized

3. Angular AuthInterceptor catches 401:
   if (error.status === 401 && !req.url.includes('/refresh')) {
     refreshToken = auth.getRefreshToken()  ← from localStorage
     authApi.refresh(refreshToken)
     → POST /api/auth/refresh { refreshToken }
   }

4. AuthService.refresh()
   a. Finds user by stored refreshToken in DB
   b. Checks status != BANNED
   c. Generates new access token + new refresh token (rotation)
   d. user.refreshToken = newRefreshToken
      userRepository.save(user)  ← DB update

5. HTTP 200: { accessToken, refreshToken, role, userId, name, email }

6. AuthInterceptor success:
   auth.saveTokens(newAccess, newRefresh)  ← updates localStorage
   Retries original request with new access token

7. If refresh fails (invalid/expired refresh token):
   auth.logout()
   → clears localStorage
   → currentUserSubject.next(null)
   → router.navigate(['/auth/login'])
```

---

## Event 4 — Recruiter Posts a Job

### Step-by-step data flow

```
1. RECRUITER fills PostJobComponent form:
   {
     title: "Senior Java Developer",
     companyName: "Acme Ltd",
     location: "Bangalore",
     description: "...",
     jobType: "FULL_TIME",
     salary: 1200000,
     experienceYears: 3,
     deadline: "2026-06-30"
   }

2. RecruiterJobsApiService.postJob(data)
   → POST /api/jobs
   → Authorization: Bearer <accessToken>

3. API Gateway GatewayJwtFilter:
   a. Validates JWT → valid
   b. Extracts userId=42, role="RECRUITER"
   c. Injects headers: X-User-Id: 42, X-User-Role: RECRUITER
   d. Forwards to JobService (lb://job-service)

4. JobController.postJob(@RequestBody, @RequestHeader X-User-Id, X-User-Role)
   → JobService.postJob(dto, postedBy=42, userRole="RECRUITER")

5. JobService.postJob()
   a. Validates role == "RECRUITER"
   b. Converts DTO to Job entity
   c. job.postedBy = 42
      job.status = ACTIVE
   d. jobRepository.save(job)  ← DB write (jobs table)
   e. Builds JobPostedEvent:
      {
        jobId, recruiterId: 42,
        title, companyName, location,
        jobType, salary, experienceYears, description
      }
   f. rabbitTemplate.convertAndSend(
        "jobportal.exchange", "job.posted", event
      )

6. RabbitMQ routes JobPostedEvent to job.posted.queue

7. NotificationService — JobPostedListener.handleJobPosted()
   a. Fetches recruiter info:
      AuthServiceClient.getUserInfo(recruiterId)
      → GET /api/internal/users/42/info  (Feign, direct to AuthService)
      → Returns { id, name, email }
   b. EmailService.sendJobPostedConfirmation(recruiterEmail, event)
      → SMTP: "Job Posted Successfully: Senior Java Developer"
   c. Fetches all active seeker emails:
      AuthServiceClient.getJobSeekerEmails()
      → GET /api/internal/users/job-seeker-emails
      → Returns ["seeker1@mail.com", "seeker2@mail.com", ...]
   d. For each seeker email:
      EmailService.sendJobAlert(seekerEmail, event)
      → SMTP: "New Job Alert: Senior Java Developer at Acme Ltd"

8. HTTP 201: JobResponseDTO {
     id, title, companyName, location, jobType,
     salary, experienceYears, description,
     status: "ACTIVE", postedBy: 42,
     createdAt, updatedAt
   }

9. PostJobComponent receives response
   → toast.success("Job posted successfully!")
   → router.navigate(['/recruiter/my-jobs'])
```

**Data stored in DB:**
```
jobs table row:
  id              | auto-generated
  title           | "Senior Java Developer"
  company_name    | "Acme Ltd"
  location        | "Bangalore"
  job_type        | "FULL_TIME"
  status          | "ACTIVE"
  posted_by       | 42 (recruiter userId)
  created_at      | timestamp
```

---

## Event 5 — Job Seeker Browses and Applies for a Job

### 5a — Browsing Jobs

```
1. BrowseJobsComponent.ngOnInit()
   → JobsApiService.getAll(page=0, size=10)
   → GET /api/jobs?page=0&size=10

2. API Gateway → public route (no JWT needed) → JobService

3. JobService.getAllJobs()
   → jobRepository.findByStatusNot(DELETED, pageable)
   → DB query: SELECT * FROM jobs WHERE status != 'DELETED' ORDER BY created_at DESC

4. HTTP 200: PagedResponse<JobResponseDTO> {
     content: [ { id, title, companyName, location, ... }, ... ],
     totalPages, totalElements, currentPage, size
   }

5. BrowseJobsComponent renders job cards
   Each JobCardComponent receives [job] input and [showApplyButton]="true"
```

### 5b — Clicking Apply Now

```
1. User clicks "Apply Now" on a JobCardComponent
   → JobCardComponent.onApply(event)
   → event.stopPropagation()  (prevents card click navigation)
   → router.navigate(['/seeker/jobs', job.id])

2. Angular Router activates JobDetailComponent
   Route: /seeker/jobs/:id  (declared in SharedModule, routed in SeekerModule)

3. JobDetailComponent.ngOnInit()
   a. Reads job id from route params
   b. JobsApiService.getById(id)
      → GET /api/jobs/{id}  (public, no JWT)
      → JobService returns full job details
   c. ApplicationsApiService.getMyApplications()
      → GET /api/applications/my-applications
      → Checks if user already applied (sets alreadyApplied flag)

4. User clicks "Apply for this Position" button
   → JobDetailComponent.apply()
   → showModal = true  (apply modal opens)
```

### 5c — Submitting Application

```
1. User fills apply modal:
   - Cover letter (optional)
   - Resume: use existing profile resume OR upload new file

2. JobDetailComponent.submitApplication()
   → ApplicationsApiService.apply({
       jobId, coverLetter,
       useExistingResume: true/false,
       existingResumeUrl: "https://cloudinary.com/...",
       resume: File (if uploading new)
     })
   → POST /api/applications  (multipart/form-data)
   → Authorization: Bearer <accessToken>

3. API Gateway:
   Validates JWT → injects X-User-Id: 55, X-User-Role: JOB_SEEKER
   → Forwards to ApplicationService

4. ApplicationController.applyForJob()
   Validates role == "JOB_SEEKER"
   → ApplicationService.applyForJob()

5. ApplicationService.applyForJob()
   a. Fetches job via Feign:
      JobServiceClient.getJobById(jobId, userId, "JOB_SEEKER")
      → GET /api/jobs/{id}  (direct Feign call to JobService)
   b. Validates job status != DELETED/CLOSED
   c. Validates deadline not passed
   d. Checks duplicate: applicationRepository.existsByUserIdAndJobId(55, jobId)
   e. Resume handling:
      - useExistingResume=true  → uses existingResumeUrl directly
      - useExistingResume=false → cloudinaryUtil.uploadResume(file)
                                  → uploads to Cloudinary, returns URL
   f. Creates Application entity:
      {
        userId: 55, jobId,
        resumeUrl: "https://cloudinary.com/...",
        coverLetter, status: APPLIED
      }
   g. applicationRepository.save(app)  ← DB write (applications table)
   h. Fetches seeker info via Feign:
      AuthServiceClient.getUserInfo(55)
      → GET /api/internal/users/55/info
   i. Publishes JobAppliedEvent to RabbitMQ:
      {
        jobId, jobTitle,
        seekerId: 55, seekerName, seekerEmail,
        recruiterId: 42
      }
      rabbitTemplate.convertAndSend("jobportal.exchange", "job.applied", event)

6. RabbitMQ routes JobAppliedEvent to job.applied.queue

7. NotificationService — JobAppliedListener.handleJobApplied()
   a. AuthServiceClient.getUserInfo(recruiterId=42)
      → Returns recruiter name + email
   b. EmailService.sendApplicationAlert(recruiterEmail, event)
      → SMTP: "New Application Received: Senior Java Developer"
      → Body includes seeker name and email

8. HTTP 201: ApplicationResponse {
     id, userId: 55, jobId,
     resumeUrl, coverLetter,
     status: "APPLIED",
     appliedAt, updatedAt
   }

9. JobDetailComponent success handler:
   alreadyApplied = true  (button changes to "Application Submitted")
   toast.success("Application submitted successfully! 🎉")
```

**Data stored in DB:**
```
applications table row:
  id          | auto-generated
  user_id     | 55 (seeker)
  job_id      | 101
  resume_url  | "https://res.cloudinary.com/..."
  cover_letter| "I am a great fit because..."
  status      | "APPLIED"
  applied_at  | timestamp
  UNIQUE(user_id, job_id)  ← prevents duplicate applications
```

---

## Event 6 — Recruiter Updates Application Status

### Step-by-step data flow

```
1. RECRUITER opens ViewApplicantsComponent for a job
   → RecruiterAppsApiService.getApplicantsForJob(jobId)
   → GET /api/applications/job/{jobId}
   → ApplicationService fetches applications + enriches with seeker info (Feign)

2. Recruiter changes status dropdown for an applicant
   → RecruiterAppsApiService.updateApplicationStatus(applicationId, newStatus)
   → PATCH /api/applications/{id}/status
   → Body: { newStatus: "SHORTLISTED", recruiterNote: "Strong profile" }

3. API Gateway → injects X-User-Id: 42, X-User-Role: RECRUITER → ApplicationService

4. ApplicationService.updateApplicationStatus()
   a. Fetches application by id
   b. Fetches job via Feign → validates recruiter owns the job
   c. validateStatusTransition(current, next):
      Valid transitions:
        APPLIED → UNDER_REVIEW
        UNDER_REVIEW → SHORTLISTED | REJECTED
        SHORTLISTED → SELECTED | REJECTED
        SELECTED → REJECTED
   d. app.status = newStatus
      app.recruiterNote = "Strong profile"
      applicationRepository.save(app)  ← DB update

   e. If newStatus is SHORTLISTED, SELECTED, or REJECTED:
      Fetches seeker info via Feign (AuthServiceClient.getUserInfo)
      Publishes ApplicationStatusChangedEvent:
      {
        applicationId, jobId, jobTitle,
        seekerId, seekerName, seekerEmail,
        newStatus: "SHORTLISTED"
      }
      rabbitTemplate.convertAndSend(
        "jobportal.exchange", "application.status.changed", event
      )

   f. If newStatus == SELECTED:
      AuthServiceClient.updateSelectedByCompany(seekerId, { companyName })
      → PUT /api/internal/users/{seekerId}/selected-company
      → AuthService sets user.selectedByCompany = "Acme Ltd"
      → DB update (users table)

5. RabbitMQ routes ApplicationStatusChangedEvent to application.status.queue

6. NotificationService — ApplicationStatusChangedListener.handleStatusChange()
   SHORTLISTED → EmailService.sendShortlistedEmail(event)
                 → SMTP: "Congratulations! You've been Shortlisted for Senior Java Developer"
   REJECTED    → EmailService.sendRejectedEmail(event)
                 → SMTP: "Update regarding your application for Senior Java Developer"
   SELECTED    → EmailService.sendSelectedEmail(event)
                 → SMTP: "Selection Confirmation: Senior Java Developer"

7. HTTP 200: ApplicationResponse { id, status: "SHORTLISTED", updatedAt, ... }

8. ViewApplicantsComponent updates the applicant row status badge reactively
```

---

## Event 7 — Password Reset (Forgot Password)

### Step-by-step data flow

```
1. User clicks "Forgot Password" on LoginComponent
   → router.navigate(['/auth/forgot-password'])

2. ForgotPasswordComponent submits email
   → AuthApiService.forgotPassword(email)
   → POST /api/auth/forgot-password { email }

3. API Gateway → public route → AuthService

4. AuthService.forgotPassword()
   a. Finds user by email
   b. Generates 6-digit OTP
   c. user.resetPasswordOtp = otp
      user.otpExpiryTime = now + 10 minutes
      userRepository.save(user)  ← DB update
   d. rabbitTemplate.convertAndSend(
        "jobportal.exchange", "password.reset",
        PasswordResetEvent { email, name, otp }
      )

5. RabbitMQ routes to password.reset.queue

6. NotificationService — PasswordResetListener.handlePasswordReset()
   → EmailService.sendOtpEmail(email, name, otp)
   → SMTP: "Joblix - Password Reset OTP"

7. HTTP 200: { message: "OTP sent to your email" }

8. User enters OTP on ResetPasswordComponent
   → AuthApiService.resetPassword(email, otp, newPassword)
   → POST /api/auth/reset-password { email, otp, newPassword }

9. AuthService.resetPassword()
   a. Validates otp matches stored value
   b. Validates otpExpiryTime not passed
   c. user.password = BCrypt.encode(newPassword)
      user.resetPasswordOtp = null
      user.otpExpiryTime = null
      userRepository.save(user)  ← DB update

10. HTTP 200: { message: "Password reset successfully" }
    → router.navigate(['/auth/login'])
```

---

## Event 8 — Admin Manages Users

### Step-by-step data flow

```
1. ADMIN opens UserManagementComponent
   → AdminApiService.getUsers()
   → GET /api/admin/users
   → Authorization: Bearer <admin-token>

2. API Gateway → injects X-User-Id: 1, X-User-Role: ADMIN → AdminService

3. AdminController.getAllUsers()
   assertAdmin(role)  ← throws 403 if not ADMIN
   → AdminService.getAllUsers()
   → AuthServiceClient.getAllUsers()  (Feign → AuthService)
   → GET /api/internal/users
   → AuthService returns all users from DB

4. HTTP 200: List<UserResponse> [ { id, name, email, role, status, ... }, ... ]

5. Admin clicks "Ban User" for userId=55
   → AdminApiService.banUser(55)
   → PUT /api/admin/users/55/ban

6. AdminService.banUser(55, adminId=1)
   a. Prevents self-ban check
   b. AuthServiceClient.banUser(55)
      → PUT /api/internal/users/55/ban
      → AuthService: user.status = BANNED, user.refreshToken = null
      → DB update (users table)
   c. AuthServiceClient.invalidateToken(55)
      → PUT /api/internal/users/55/invalidate-token
      → AuthService: user.refreshToken = null (forces logout)
   d. auditLogRepository.save(
        AuditLog { action: "BAN_USER", performedBy: "admin:1",
                   details: "Banned user ID: 55" }
      )  ← DB write (audit_logs table)

7. HTTP 200: { message: "User banned successfully" }

8. UserManagementComponent updates the user row status badge to "BANNED"
```

---

## Event 9 — Profile Picture / Resume Upload

### Step-by-step data flow

```
1. User selects a file in their profile component
   → AuthApiService.uploadProfilePicture(file)
   → PUT /api/auth/profile/picture  (multipart/form-data)
   → Authorization: Bearer <token>

2. API Gateway → injects X-User-Id: 55 → AuthService

3. AuthController.uploadProfilePicture(@RequestPart file, @RequestHeader X-User-Id)
   → AuthService.updateProfilePicture(55, file)

4. AuthService.updateProfilePicture()
   a. cloudinaryUtil.uploadProfilePicture(file)
      → Cloudinary SDK uploads file to "profile_pictures/" folder
      → Returns: "https://res.cloudinary.com/dq4e4wh8x/image/upload/..."
   b. user.profilePictureUrl = cloudinaryUrl
      userRepository.save(user)  ← DB update (users table)

5. HTTP 200: { profilePictureUrl: "https://res.cloudinary.com/..." }

6. Profile component success handler:
   profile.profilePictureUrl = res.profilePictureUrl
   auth.saveUser({ ...auth.getUser(), profilePictureUrl: res.profilePictureUrl })
   → Updates localStorage.joblix_user
   → currentUserSubject.next(updatedUser)  ← BehaviorSubject emits

7. HeaderComponent (subscribed to currentUser$) re-renders:
   → <img [src]="user.profilePictureUrl"> replaces the initials span
   → Avatar in navbar updates immediately without page reload
```

---

## Event 10 — Admin Platform Report

### Step-by-step data flow

```
1. ADMIN opens PlatformReportComponent
   → AdminApiService.getReport()
   → GET /api/admin/reports

2. AdminService.getReport()
   Makes 3 parallel Feign calls:

   a. AuthServiceClient.getAllUsers()
      → GET /api/internal/users  (AuthService → users table)
      → Returns List<UserResponse>

   b. AdminJobClient.getAllJobs()
      → GET /api/internal/jobs/all  (JobService → jobs table, including DELETED)
      → Returns List<JobResponse>

   c. AdminAppClient.getStats()
      → GET /api/internal/applications/stats  (ApplicationService)
      → ApplicationService.getApplicationStats():
         Counts all applications by status:
         { totalApplications, appliedCount, underReviewCount,
           shortlistedCount, rejectedCount }

3. AdminService assembles PlatformReport:
   {
     totalUsers: 150,
     totalJobs: 48,
     applicationStats: { totalApplications: 320, appliedCount: 180, ... },
     users: [...],
     jobs: [...]
   }

4. HTTP 200: PlatformReport

5. PlatformReportComponent renders:
   - Stats cards (total users, jobs, applications)
   - Charts (application status breakdown via Chart.js)
   - User and job tables
```

---

## RabbitMQ Exchange and Queue Summary

```
Exchange: jobportal.exchange  (topic/direct)

┌─────────────────────────────┬──────────────────────────────┬──────────────────────────────┐
│ Routing Key                 │ Queue                        │ Consumer                     │
├─────────────────────────────┼──────────────────────────────┼──────────────────────────────┤
│ job.posted                  │ job.posted.queue             │ JobPostedListener            │
│ job.applied                 │ job.applied.queue            │ JobAppliedListener           │
│ application.status.changed  │ application.status.queue     │ ApplicationStatusChangedList │
│ password.reset              │ password.reset.queue         │ PasswordResetListener        │
│ registration.otp            │ registration.otp.queue       │ RegistrationOtpListener      │
└─────────────────────────────┴──────────────────────────────┴──────────────────────────────┘
```

---

## Frontend State Management Summary

```
AuthService (BehaviorSubject<User | null>)
  ├── Initialized from localStorage on app start
  ├── Updated on: login, register, profile picture upload, logout
  └── Subscribed by: HeaderComponent, Guards, Dashboard components

localStorage keys:
  joblix_access_token   → JWT (used by AuthInterceptor on every request)
  joblix_refresh_token  → UUID (used by AuthInterceptor on 401 retry)
  joblix_user           → JSON { id, name, email, role, profilePictureUrl, resumeUrl }

AuthInterceptor (HttpInterceptorFn)
  → Attaches Authorization: Bearer <token> to every outgoing HTTP request
  → On 401: calls /api/auth/refresh → retries original request
  → On refresh failure: calls auth.logout() → clears state → redirects to login

Route Guards:
  authGuard    → checks auth.isLoggedIn() → redirects to /auth/login if false
  roleGuard    → checks auth.getRole() against route.data.roles → redirects to /unauthorized
```

---

## Feign Client Communication Map (Service-to-Service)

```
AdminService ──(Feign)──▶ AuthService
  GET  /api/internal/users              → getAllUsers()
  DELETE /api/internal/users/{id}       → deleteUser()
  PUT  /api/internal/users/{id}/ban     → banUser()
  PUT  /api/internal/users/{id}/unban   → unbanUser()
  PUT  /api/internal/users/{id}/invalidate-token → invalidateToken()

AdminService ──(Feign)──▶ JobService
  GET    /api/internal/jobs/all         → getAllJobsForAdmin()
  DELETE /api/internal/jobs/{id}        → deleteJobByAdmin()

AdminService ──(Feign)──▶ ApplicationService
  GET /api/internal/applications/stats  → getApplicationStats()

ApplicationService ──(Feign)──▶ AuthService
  GET /api/internal/users/{id}/info                    → getUserInfo()
  PUT /api/internal/users/{seekerId}/selected-company  → updateSelectedByCompany()

ApplicationService ──(Feign)──▶ JobService
  GET /api/jobs/{id}                    → getJobById() (validates job before apply)

NotificationService ──(Feign)──▶ AuthService
  GET /api/internal/users/job-seeker-emails  → getJobSeekerEmails()
  GET /api/internal/users/{id}/info          → getUserInfo()

NOTE: All /api/internal/** endpoints are blocked by the API Gateway
for external requests. Feign clients call services directly via
Eureka service discovery (lb://service-name), bypassing the Gateway.
```

---

*Joblix — Event-Driven Data Flow Documentation | Capgemini Training Sprint 1*
