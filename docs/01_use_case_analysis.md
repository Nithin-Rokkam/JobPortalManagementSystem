# Use Case Analysis — Joblix Job Portal Management System

---

## 1. Actor Definitions

| Actor | Role |
|---|---|
| **Guest** | Unauthenticated user — public read-only access |
| **Job Seeker** | Verified user — browses jobs, applies, tracks applications |
| **Recruiter** | Verified user — posts jobs, manages applicants |
| **Admin** | Platform supervisor — governance, bans, reports |
| **Notification Service** | Internal system — sends automated emails via RabbitMQ |
| **Cloudinary** | External storage — profile pictures and resumes |

---

## 2. Use Case Tables

### Authentication & Account

| ID | Use Case | Actor | Key Rule |
|---|---|---|---|
| UC-01 | Register Account | Guest | ADMIN role blocked; re-registers PENDING accounts |
| UC-02 | Verify Email OTP | Guest | 10-min OTP; moves account to ACTIVE |
| UC-03 | Resend Registration OTP | Guest | Only for PENDING_VERIFICATION accounts |
| UC-04 | Login | Guest | Blocks BANNED and PENDING accounts |
| UC-05 | Refresh Access Token | Any User | DB-backed UUID; rotated on every use |
| UC-06 | Logout | Any User | Clears refresh token from DB |
| UC-07 | Forgot Password | Guest | 6-digit OTP; 10-min expiry |
| UC-08 | Reset Password | Guest | Validates OTP; clears OTP fields after success |

### Profile Management

| ID | Use Case | Actor | Key Rule |
|---|---|---|---|
| UC-09 | View Own Profile | Any User | Reads by X-User-Id header |
| UC-10 | Upload Profile Picture | Any User | Uploaded to Cloudinary |
| UC-11 | Upload Resume to Profile | Job Seeker | Stored as resume_url on user record |
| UC-12 | Update Company Name | Recruiter | Trimmed before save |

### Job Management

| ID | Use Case | Actor | Key Rule |
|---|---|---|---|
| UC-13 | Browse Jobs | Guest / Any | Paginated; excludes DELETED |
| UC-14 | Search Jobs | Guest / Any | Filter by title, location, type, experience |
| UC-15 | View Job Detail | Guest / Any | Returns 404 for DELETED jobs |
| UC-16 | Post New Job | Recruiter | Role checked at service layer |
| UC-17 | Update Own Job | Recruiter | Ownership verified: postedBy == userId |
| UC-18 | Soft Delete Own Job | Recruiter | Sets status=DELETED; stays in DB |
| UC-19 | View My Posted Jobs | Recruiter | Paginated; excludes DELETED |

### Application Management

| ID | Use Case | Actor | Key Rule |
|---|---|---|---|
| UC-20 | Apply for Job | Job Seeker | Validates job status, deadline, no duplicates |
| UC-21 | View My Applications | Job Seeker | All statuses |
| UC-22 | View Application Detail | Job Seeker | Own applications only |
| UC-23 | View Applicants for Job | Recruiter | Job ownership verified |
| UC-24 | Update Application Status | Recruiter | Strict state machine enforced |
| UC-25 | Delete Application | Recruiter | Hard delete; ownership required |

### Platform Administration

| ID | Use Case | Actor | Key Rule |
|---|---|---|---|
| UC-26 | View All Users | Admin | Via Feign to Auth Service |
| UC-27 | Ban User | Admin | Self-ban blocked; token invalidated; audit logged |
| UC-28 | Unban User | Admin | Restores ACTIVE status; audit logged |
| UC-29 | Delete User | Admin | Hard delete via Feign; audit logged |
| UC-30 | View All Jobs | Admin | Includes DELETED jobs |
| UC-31 | Delete Job | Admin | Soft-delete via Feign; audit logged |
| UC-32 | View Platform Report | Admin | 3 sequential Feign calls |
| UC-33 | View Audit Logs | Admin | All admin action history |

### Notifications (System-Driven)

| ID | Use Case | Trigger | Queue |
|---|---|---|---|
| UC-34 | Send Registration OTP Email | Register / Resend | registration.otp.queue |
| UC-35 | Send Password Reset Email | Forgot Password | password.reset.queue |
| UC-36 | Send Job Posted Confirmation | Post New Job | job.posted.queue |
| UC-37 | Send Job Alert to All Seekers | Post New Job | job.posted.queue |
| UC-38 | Send Application Alert to Recruiter | Apply for Job | job.applied.queue |
| UC-39 | Send Status Change Email to Seeker | Update Status | application.status.queue |

---

## 3. Use Case Diagrams

---

### Diagram 1 — System Actor Overview

```mermaid
graph LR
    classDef actor  fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef domain fill:#E3F2FD,stroke:#1565C0,color:#1A237E

    Guest(["Guest"]):::actor
    Seeker(["Job Seeker"]):::actor
    Recruiter(["Recruiter"]):::actor
    Admin(["Admin"]):::actor

    D1["Auth and Account\nRegister, Login, OTP, Password Reset"]:::domain
    D2["Profile Management\nPicture, Resume, Company Name"]:::domain
    D3["Job Management\nBrowse, Search, Post, Edit, Delete"]:::domain
    D4["Application Management\nApply, Track, Review, Update Status"]:::domain
    D5["Administration\nUsers, Jobs, Reports, Audit Logs"]:::domain

    Guest --> D1
    Guest --> D3

    Seeker --> D1
    Seeker --> D2
    Seeker --> D3
    Seeker --> D4

    Recruiter --> D1
    Recruiter --> D2
    Recruiter --> D3
    Recruiter --> D4

    Admin --> D1
    Admin --> D2
    Admin --> D5
```

---

### Diagram 2 — Authentication and Account Management

```mermaid
graph LR
    classDef actor   fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef usecase fill:#F5F5F5,stroke:#90A4AE,color:#263238
    classDef system  fill:#6A1B9A,stroke:#4A148C,color:#fff
    classDef notify  fill:#E65100,stroke:#BF360C,color:#fff

    G(["Guest"]):::actor
    U(["Any Logged-in User"]):::actor
    NS(["Notification Service"]):::system

    subgraph AUTH["Authentication and Account Management"]
        UC01["UC-01  Register Account"]:::usecase
        UC02["UC-02  Verify Email OTP"]:::usecase
        UC03["UC-03  Resend Registration OTP"]:::usecase
        UC04["UC-04  Login"]:::usecase
        UC05["UC-05  Refresh Access Token"]:::usecase
        UC06["UC-06  Logout"]:::usecase
        UC07["UC-07  Forgot Password"]:::usecase
        UC08["UC-08  Reset Password"]:::usecase
        UC34["UC-34  Send Registration OTP Email"]:::notify
        UC35["UC-35  Send Password Reset Email"]:::notify
    end

    G --> UC01
    G --> UC02
    G --> UC03
    G --> UC04
    G --> UC07
    G --> UC08
    U --> UC05
    U --> UC06

    UC01 -- "include" --> UC34
    UC03 -- "include" --> UC34
    UC07 -- "include" --> UC35

    NS -.-> UC34
    NS -.-> UC35
```

---

### Diagram 3 — Profile Management

```mermaid
graph LR
    classDef actor   fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef usecase fill:#F5F5F5,stroke:#90A4AE,color:#263238
    classDef system  fill:#1B5E20,stroke:#0D3D0D,color:#fff

    S(["Job Seeker"]):::actor
    R(["Recruiter"]):::actor
    A(["Admin"]):::actor
    CLD(["Cloudinary"]):::system

    subgraph PROFILE["Profile Management"]
        UC09["UC-09  View Own Profile"]:::usecase
        UC10["UC-10  Upload Profile Picture"]:::usecase
        UC11["UC-11  Upload Resume to Profile"]:::usecase
        UC12["UC-12  Update Company Name"]:::usecase
    end

    S --> UC09
    S --> UC10
    S --> UC11

    R --> UC09
    R --> UC10
    R --> UC12

    A --> UC09
    A --> UC10

    UC10 -- "include" --> CLD
    UC11 -- "include" --> CLD
```

---

### Diagram 4 — Job Management

```mermaid
graph LR
    classDef actor   fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef usecase fill:#F5F5F5,stroke:#90A4AE,color:#263238
    classDef notify  fill:#E65100,stroke:#BF360C,color:#fff
    classDef system  fill:#6A1B9A,stroke:#4A148C,color:#fff

    G(["Guest"]):::actor
    S(["Job Seeker"]):::actor
    R(["Recruiter"]):::actor
    NS(["Notification Service"]):::system

    subgraph JOB["Job Management"]
        subgraph PUBLIC["Public - No Login Required"]
            UC13["UC-13  Browse Jobs"]:::usecase
            UC14["UC-14  Search Jobs"]:::usecase
            UC15["UC-15  View Job Detail"]:::usecase
        end
        subgraph REC["Recruiter Only"]
            UC16["UC-16  Post New Job"]:::usecase
            UC17["UC-17  Update Own Job"]:::usecase
            UC18["UC-18  Soft Delete Own Job"]:::usecase
            UC19["UC-19  View My Posted Jobs"]:::usecase
        end
        UC36["UC-36  Send Job Posted\nConfirmation to Recruiter"]:::notify
        UC37["UC-37  Send Job Alert\nto All Seekers"]:::notify
    end

    G --> UC13
    G --> UC14
    G --> UC15

    S --> UC13
    S --> UC14
    S --> UC15

    R --> UC16
    R --> UC17
    R --> UC18
    R --> UC19

    UC16 -- "include" --> UC36
    UC16 -- "include" --> UC37

    NS -.-> UC36
    NS -.-> UC37
```

---

### Diagram 5 — Application Management

```mermaid
graph LR
    classDef actor   fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef usecase fill:#F5F5F5,stroke:#90A4AE,color:#263238
    classDef notify  fill:#E65100,stroke:#BF360C,color:#fff
    classDef system  fill:#6A1B9A,stroke:#4A148C,color:#fff
    classDef ext     fill:#1B5E20,stroke:#0D3D0D,color:#fff

    S(["Job Seeker"]):::actor
    R(["Recruiter"]):::actor
    NS(["Notification Service"]):::system
    CLD(["Cloudinary"]):::ext

    subgraph APP["Application Management"]
        subgraph SEEK["Job Seeker Actions"]
            UC20["UC-20  Apply for Job"]:::usecase
            UC21["UC-21  View My Applications"]:::usecase
            UC22["UC-22  View Application Detail"]:::usecase
        end
        subgraph REC2["Recruiter Actions"]
            UC23["UC-23  View Applicants for Job"]:::usecase
            UC24["UC-24  Update Application Status"]:::usecase
            UC25["UC-25  Delete Application"]:::usecase
        end
        UC38["UC-38  Alert Email to Recruiter"]:::notify
        UC39["UC-39  Status Change Email to Seeker"]:::notify
    end

    S --> UC20
    S --> UC21
    S --> UC22

    R --> UC23
    R --> UC24
    R --> UC25

    UC20 -- "include" --> UC38
    UC20 -. "extend: new resume only" .-> CLD
    UC24 -- "include: SHORTLISTED, SELECTED, REJECTED" --> UC39

    NS -.-> UC38
    NS -.-> UC39
```

---

### Diagram 6 — Platform Administration

```mermaid
graph LR
    classDef actor   fill:#B71C1C,stroke:#7F0000,color:#fff
    classDef usecase fill:#F5F5F5,stroke:#90A4AE,color:#263238
    classDef audit   fill:#37474F,stroke:#263238,color:#fff

    A(["Admin"]):::actor

    subgraph ADM["Platform Administration - All require ADMIN role"]
        subgraph USERS["User Governance"]
            UC26["UC-26  View All Users"]:::usecase
            UC27["UC-27  Ban User"]:::usecase
            UC28["UC-28  Unban User"]:::usecase
            UC29["UC-29  Delete User"]:::usecase
        end
        subgraph JOBS["Job Oversight"]
            UC30["UC-30  View All Jobs"]:::usecase
            UC31["UC-31  Delete Job"]:::usecase
        end
        subgraph REP["Analytics and Audit"]
            UC32["UC-32  View Platform Report"]:::usecase
            UC33["UC-33  View Audit Logs"]:::usecase
        end
        AL["Audit Log\nAuto-recorded for every write action"]:::audit
    end

    A --> UC26
    A --> UC27
    A --> UC28
    A --> UC29
    A --> UC30
    A --> UC31
    A --> UC32
    A --> UC33

    UC27 -- "writes" --> AL
    UC28 -- "writes" --> AL
    UC29 -- "writes" --> AL
    UC31 -- "writes" --> AL
```

---

### Diagram 7 — Event-Driven Notification Flow

```mermaid
graph TD
    classDef pub    fill:#1565C0,stroke:#0D47A1,color:#fff
    classDef broker fill:#4E342E,stroke:#3E2723,color:#fff
    classDef queue  fill:#37474F,stroke:#263238,color:#fff
    classDef smtp   fill:#1B5E20,stroke:#0D3D0D,color:#fff

    subgraph PUB["Event Publishers"]
        P1["Register or Resend OTP\nPublished after DB commit"]:::pub
        P2["Forgot Password\nPublished synchronously"]:::pub
        P3["Post New Job\nPublished synchronously"]:::pub
        P4["Apply for Job\nPublished best-effort"]:::pub
        P5["Update Status\nSHORTLISTED, SELECTED, or REJECTED\nPublished best-effort"]:::pub
    end

    RMQ["RabbitMQ\njobportal.exchange"]:::broker

    subgraph NS["Notification Service Listeners"]
        L1["registration.otp.queue\nRegistrationOtpListener"]:::queue
        L2["password.reset.queue\nPasswordResetListener"]:::queue
        L3["job.posted.queue\nJobPostedListener"]:::queue
        L4["job.applied.queue\nJobAppliedListener"]:::queue
        L5["application.status.queue\nApplicationStatusChangedListener"]:::queue
    end

    SMTP["Gmail SMTP\nEmail Delivery"]:::smtp

    P1 --> RMQ
    P2 --> RMQ
    P3 --> RMQ
    P4 --> RMQ
    P5 --> RMQ

    RMQ --> L1
    RMQ --> L2
    RMQ --> L3
    RMQ --> L4
    RMQ --> L5

    L1 -- "OTP email to new user" --> SMTP
    L2 -- "OTP email to user" --> SMTP
    L3 -- "Confirmation to recruiter + Job alert to all seekers" --> SMTP
    L4 -- "New application alert to recruiter" --> SMTP
    L5 -- "Status update email to seeker" --> SMTP
```

---

## 4. Application Status State Machine

> Core business rule enforced by `validateStatusTransition()` in Application Service.

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

## 5. Include / Extend Summary

| From | Relationship | To | When |
|---|---|---|---|
| UC-01 Register | include | UC-34 Registration OTP Email | Always |
| UC-03 Resend OTP | include | UC-34 Registration OTP Email | Always |
| UC-07 Forgot Password | include | UC-35 Password Reset Email | Always |
| UC-16 Post New Job | include | UC-36 Recruiter Confirmation | Always |
| UC-16 Post New Job | include | UC-37 Seeker Job Alert | Always |
| UC-20 Apply for Job | include | UC-38 Recruiter Alert | Always (best-effort) |
| UC-24 Update Status | include | UC-39 Seeker Status Email | Only SHORTLISTED / SELECTED / REJECTED |
| UC-10 Upload Picture | include | Cloudinary | Always |
| UC-11 Upload Resume | include | Cloudinary | Always |
| UC-20 Apply for Job | extend | Cloudinary | Only when uploading a new resume |
