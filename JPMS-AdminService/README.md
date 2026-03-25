# JPMS — Admin Service

## Overview

The **Admin Service** is a Spring Boot microservice (port **8084**) providing platform administration for the Job Portal Management System. It registers with **Eureka** as `admin-service` and is accessed through the **API Gateway** at `localhost:9090/api/admin/**`.

All endpoints require a valid **JWT** with the **ADMIN** role. The API Gateway validates the JWT and forwards `X-User-Id` and `X-User-Role` headers — no JWT parsing happens inside Admin Service.

| Property | Value |
|---|---|
| **Port** | `8084` |
| **Eureka Name** | `admin-service` |
| **Database** | MySQL — `admin_db` |
| **Spring Boot** | `3.2.5` |
| **Spring Cloud** | `2023.0.1` |

---

## Request Flow

```
Client (Postman)
   │
   │  Authorization: Bearer <JWT>
   ▼
API Gateway (:9090)
   │  • Validates JWT
   │  • Extracts userId, role from claims
   │  • Sets X-User-Id, X-User-Role headers
   ▼
Admin Service (:8084)
   │  • Reads X-User-Id, X-User-Role
   │  • Enforces ADMIN role
   │  • Delegates to AdminService
   ▼
Feign Clients → Internal Endpoints
   ├── AuthServiceClient     → auth-service         /api/internal/users/**
   ├── AdminJobClient        → job-service           /api/internal/jobs/**
   └── AdminAppClient        → application-service   /api/internal/applications/**
```

---

## Feign Client → Auth Service Endpoint Mapping

| Admin Feign Method | HTTP | Auth Internal Endpoint | Auth Controller Method |
|---|---|---|---|
| `getAllUsers()` | `GET` | `/api/internal/users` | `InternalController.getAllUsers()` |
| `deleteUser(id)` | `DELETE` | `/api/internal/users/{id}` | `InternalController.deleteUser(id)` |
| `banUser(id)` | `PUT` | `/api/internal/users/{id}/ban` | `InternalController.banUser(id)` |
| `unbanUser(id)` | `PUT` | `/api/internal/users/{id}/unban` | `InternalController.unbanUser(id)` |

### Job Service Mapping

| Admin Feign Method | HTTP | Job Internal Endpoint |
|---|---|---|
| `getAllJobs()` | `GET` | `/api/internal/jobs/all` |
| `deleteJob(id)` | `DELETE` | `/api/internal/jobs/{id}` |

### Application Service Mapping

| Admin Feign Method | HTTP | App Internal Endpoint |
|---|---|---|
| `getStats()` | `GET` | `/api/internal/applications/stats` |

---

## Prerequisites

- Java 17+, Maven 3.8+
- MySQL running with database `admin_db` created
- Eureka Server on `localhost:8761`
- Auth Service on `localhost:8081`
- Job Service running
- Application Service running
- API Gateway on `localhost:9090`

---

## Running the Service

```bash
cd JPMS-AdminService
mvn spring-boot:run
```

---

## REST API Reference

### Base URL

| Access | URL |
|---|---|
| **Via Gateway (recommended)** | `http://localhost:9090/api/admin` |
| **Direct** | `http://localhost:8084/api/admin` |

> **Always use the Gateway URL.** Direct access bypasses JWT verification and will be missing the `X-User-Id` / `X-User-Role` headers.

---

### API Summary

| # | Method | Endpoint | Description |
|---|---|---|---|
| 1 | `GET` | `/api/admin/users` | List all users |
| 2 | `DELETE` | `/api/admin/users/{id}` | Delete a user |
| 3 | `PUT` | `/api/admin/users/{id}/ban` | Ban a user |
| 4 | `PUT` | `/api/admin/users/{id}/unban` | Unban a user |
| 5 | `GET` | `/api/admin/jobs` | List all jobs |
| 6 | `DELETE` | `/api/admin/jobs/{id}` | Delete a job |
| 7 | `GET` | `/api/admin/reports` | Platform report |
| 8 | `GET` | `/api/admin/audit-logs` | View audit trail |

---

### 1. `GET /api/admin/users` — Get All Users

```
GET http://localhost:9090/api/admin/users
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "JOB_SEEKER",
    "status": "ACTIVE"
  }
]
```

**403 Forbidden** (non-ADMIN token)

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. ADMIN role required.",
  "timestamp": "2026-03-23T10:15:00"
}
```

---

### 2. `DELETE /api/admin/users/{id}` — Delete a User

```
DELETE http://localhost:9090/api/admin/users/3
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "User deleted successfully" }
```

---

### 3. `PUT /api/admin/users/{id}/ban` — Ban a User

```
PUT http://localhost:9090/api/admin/users/3/ban
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "User banned successfully" }
```

> Banned users get "Account suspended" on login.

---

### 4. `PUT /api/admin/users/{id}/unban` — Unban a User

```
PUT http://localhost:9090/api/admin/users/3/unban
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "User unbanned successfully" }
```

---

### 5. `GET /api/admin/jobs` — Get All Jobs

```
GET http://localhost:9090/api/admin/jobs
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
[
  {
    "id": 1,
    "title": "Java Backend Developer",
    "companyName": "TechCorp Inc.",
    "location": "Bangalore",
    "salary": "12-18 LPA",
    "experience": "3-5 years",
    "description": "Build microservices...",
    "postedByEmail": "recruiter@example.com",
    "createdAt": "2026-03-22T10:30:00"
  }
]
```

---

### 6. `DELETE /api/admin/jobs/{id}` — Delete a Job

```
DELETE http://localhost:9090/api/admin/jobs/1
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{ "message": "Job deleted successfully" }
```

---

### 7. `GET /api/admin/reports` — Platform Report

```
GET http://localhost:9090/api/admin/reports
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
{
  "totalUsers": 15,
  "totalJobs": 8,
  "applicationStats": {
    "totalApplications": 25,
    "appliedCount": 10,
    "underReviewCount": 8,
    "shortlistedCount": 5,
    "rejectedCount": 2
  },
  "users": [ ... ],
  "jobs": [ ... ]
}
```

---

### 8. `GET /api/admin/audit-logs` — View Audit Logs

```
GET http://localhost:9090/api/admin/audit-logs
Authorization: Bearer <JWT_TOKEN>
```

**200 OK**

```json
[
  {
    "id": 1,
    "action": "DELETE_USER",
    "performedBy": "admin:5",
    "details": "Deleted user ID: 3",
    "createdAt": "2026-03-23T10:20:00"
  },
  {
    "id": 2,
    "action": "BAN_USER",
    "performedBy": "admin:5",
    "details": "Banned user ID: 7",
    "createdAt": "2026-03-23T10:25:00"
  }
]
```

---

## Postman Testing Workflow

### Step 1 — Register an ADMIN user

```
POST http://localhost:9090/api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Admin User",
  "email": "admin@portal.com",
  "password": "admin12345",
  "phone": "9000000000",
  "role": "ADMIN"
}
```

### Step 2 — Login as ADMIN

```
POST http://localhost:9090/api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@portal.com",
  "password": "admin12345"
}
```

Copy the `accessToken` from the response.

### Step 3 — Set Authorization in Postman

For all admin requests, set:
- **Header**: `Authorization` = `Bearer <paste_access_token>`

### Step 4 — Test Endpoints

| # | Request | URL |
|---|---|---|
| 1 | `GET` | `http://localhost:9090/api/admin/users` |
| 2 | `GET` | `http://localhost:9090/api/admin/jobs` |
| 3 | `GET` | `http://localhost:9090/api/admin/reports` |
| 4 | `PUT` | `http://localhost:9090/api/admin/users/2/ban` |
| 5 | `PUT` | `http://localhost:9090/api/admin/users/2/unban` |
| 6 | `DELETE` | `http://localhost:9090/api/admin/users/3` |
| 7 | `DELETE` | `http://localhost:9090/api/admin/jobs/1` |
| 8 | `GET` | `http://localhost:9090/api/admin/audit-logs` |

### Step 5 — Verify Access Control

Login as `JOB_SEEKER` or `RECRUITER` and hit any admin endpoint → expect `403 Forbidden`.

---

## End-to-End Testing Order

| Step | Service | Action |
|---|---|---|
| 1 | Auth | Register `RECRUITER`, `JOB_SEEKER`, `ADMIN` |
| 2 | Auth | Login as `RECRUITER` → save token |
| 3 | Job | Create jobs |
| 4 | Auth | Login as `JOB_SEEKER` → save token |
| 5 | Application | Apply for jobs |
| 6 | Auth | Login as `ADMIN` → save token |
| 7 | Admin | View users, jobs, reports |
| 8 | Admin | Ban/unban a user |
| 9 | Admin | Delete user/job |
| 10 | Admin | View audit logs |
