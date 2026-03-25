# JPMS — Job Service

Handles all job-related operations. Only recruiters can post, update, or delete jobs. Supports pagination and multi-filter search. Part of the Job Portal Management System microservices architecture.

---

## Service Info

| Property | Value |
|---|---|
| Service Name | JPMS-JobService |
| Port | 8082 |
| Base URL (Direct) | `http://localhost:8082` |
| Base URL (Via Gateway) | `http://localhost:9090` |
| Database | Oracle |
| Registers with Eureka | Yes |

> Always test via the API Gateway on port **9090** in production. Use port **8082** only for direct local testing.

---

## Package Structure

```
com.jobportal
├── controller
│   └── JobController.java
├── service
│   └── JobService.java
├── repository
│   └── JobRepository.java
├── entity
│   ├── Job.java
│   ├── JobType.java
│   └── JobStatus.java
├── dto
│   ├── JobRequestDTO.java
│   ├── JobResponseDTO.java
│   └── PagedResponse.java
├── config
│   └── SecurityConfig.java
└── JpmsJobServiceApplication.java
```

---

## Enums

**JobType**
```
FULL_TIME | PART_TIME | REMOTE | CONTRACT
```

**JobStatus**
```
ACTIVE | CLOSED | DRAFT | DELETED
```

---

## Authentication Headers

This service does not validate JWT itself. The API Gateway validates the token and forwards these headers to the service:

| Header | Description | Example |
|---|---|---|
| `X-User-Id` | ID of the logged-in user | `1` |
| `X-User-Role` | Role of the logged-in user | `RECRUITER` |

Public endpoints do not require these headers.

---

## Pagination

All list endpoints support pagination via query parameters:

| Parameter | Default | Description |
|---|---|---|
| `page` | `0` | Page number (starts from 0) |
| `size` | `10` | Number of records per page |

Paginated responses always follow this structure:

```json
{
  "content": [ ],
  "currentPage": 0,
  "totalPages": 3,
  "totalElements": 25,
  "last": false
}
```

---

## API Endpoints

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| POST | `/api/jobs` | RECRUITER | Create a new job |
| GET | `/api/jobs` | Public | Get all jobs (paginated) |
| GET | `/api/jobs/{id}` | Public | Get single job by ID |
| GET | `/api/jobs/search` | Public | Search jobs with filters |
| PUT | `/api/jobs/{id}` | RECRUITER (owner) | Update a job |
| DELETE | `/api/jobs/{id}` | RECRUITER (owner) | Soft delete a job |
| GET | `/api/jobs/my-jobs` | RECRUITER | Get recruiter's own jobs |

---

## Testing — API Endpoints

---

### 1. POST /api/jobs — Create a Job

**Required headers:**
```
X-User-Id: 1
X-User-Role: RECRUITER
Content-Type: application/json
```

**Test 1 — Create job 1 (userId 1)**
```
POST http://localhost:8082/api/jobs
```
```json
{
  "title": "Senior Java Developer",
  "companyName": "TechCorp India",
  "location": "Bangalore",
  "salary": 1500000.00,
  "experienceYears": 3,
  "jobType": "FULL_TIME",
  "skillsRequired": "Java, Spring Boot, Microservices, MySQL",
  "description": "We are looking for a Senior Java Developer with strong experience in Spring Boot and microservices architecture.",
  "deadline": "2025-06-30"
}
```

Expected: `201 Created`

```json
{
  "id": 1,
  "title": "Senior Java Developer",
  "companyName": "TechCorp India",
  "location": "Bangalore",
  "salary": 1500000.00,
  "experienceYears": 3,
  "jobType": "FULL_TIME",
  "skillsRequired": "Java, Spring Boot, Microservices, MySQL",
  "description": "We are looking for a Senior Java Developer...",
  "status": "ACTIVE",
  "deadline": "2025-06-30",
  "postedBy": 1,
  "createdAt": "2025-03-22T10:30:00",
  "updatedAt": "2025-03-22T10:30:00"
}
```

---

**Test 2 — Create job 2 (userId 2)**
```
X-User-Id: 2
X-User-Role: RECRUITER
```
```json
{
  "title": "React Frontend Developer",
  "companyName": "StartupXYZ",
  "location": "Mumbai",
  "salary": 900000.00,
  "experienceYears": 2,
  "jobType": "REMOTE",
  "skillsRequired": "React, JavaScript, TypeScript, CSS",
  "description": "We need a creative React developer to build beautiful user interfaces.",
  "deadline": "2025-07-15"
}
```

Expected: `201 Created`

---

**Test 3 — Create job 3 (userId 1)**
```
X-User-Id: 1
X-User-Role: RECRUITER
```
```json
{
  "title": "DevOps Engineer",
  "companyName": "CloudBase",
  "location": "Hyderabad",
  "salary": 1200000.00,
  "experienceYears": 4,
  "jobType": "FULL_TIME",
  "skillsRequired": "AWS, Docker, Kubernetes, Jenkins",
  "description": "Looking for DevOps engineer to manage cloud infrastructure.",
  "deadline": "2025-08-01"
}
```

Expected: `201 Created`

---

**Test 4 — Create job 4 (userId 1)**
```
X-User-Id: 1
X-User-Role: RECRUITER
```
```json
{
  "title": "Python Developer",
  "companyName": "DataMinds",
  "location": "Pune",
  "salary": 1100000.00,
  "experienceYears": 2,
  "jobType": "CONTRACT",
  "skillsRequired": "Python, Django, REST APIs, PostgreSQL",
  "description": "Contract role for Python developer with Django experience.",
  "deadline": "2025-05-30"
}
```

Expected: `201 Created`

---

**Test 5 — Create job 5 (userId 1)**
```
X-User-Id: 1
X-User-Role: RECRUITER
```
```json
{
  "title": "Junior Java Developer",
  "companyName": "TechCorp India",
  "location": "Bangalore",
  "salary": 600000.00,
  "experienceYears": 1,
  "jobType": "FULL_TIME",
  "skillsRequired": "Java, Spring Boot, SQL",
  "description": "Entry level Java developer role for fresh graduates.",
  "deadline": "2025-09-01"
}
```

Expected: `201 Created`

---

**Test 6 — Wrong role tries to post (should fail)**
```
X-User-Id: 5
X-User-Role: JOB_SEEKER
```
```json
{
  "title": "React Developer",
  "companyName": "StartupXYZ",
  "location": "Mumbai",
  "salary": 900000.00,
  "experienceYears": 2,
  "jobType": "REMOTE",
  "skillsRequired": "React, JavaScript",
  "description": "Looking for React developer for remote position.",
  "deadline": "2025-07-15"
}
```

Expected: `500` — `Only recruiters can post jobs`

---

### 2. GET /api/jobs — Get All Jobs (Paginated)

**Public — no headers required**

**Test 1 — Default (no params)**
```
GET http://localhost:8082/api/jobs
```

Expected: `200 OK` — page 0, size 10, sorted by createdAt descending

---

**Test 2 — Page 0, size 2**
```
GET http://localhost:8082/api/jobs?page=0&size=2
```

Expected: `200 OK`
```json
{
  "content": [
    { "id": 5, "title": "Junior Java Developer", "location": "Bangalore" },
    { "id": 4, "title": "Python Developer", "location": "Pune" }
  ],
  "currentPage": 0,
  "totalPages": 3,
  "totalElements": 5,
  "last": false
}
```

---

**Test 3 — Page 1, size 2**
```
GET http://localhost:8082/api/jobs?page=1&size=2
```

Expected: `200 OK`
```json
{
  "content": [
    { "id": 3, "title": "DevOps Engineer", "location": "Hyderabad" },
    { "id": 2, "title": "React Frontend Developer", "location": "Mumbai" }
  ],
  "currentPage": 1,
  "totalPages": 3,
  "totalElements": 5,
  "last": false
}
```

---

**Test 4 — Last page**
```
GET http://localhost:8082/api/jobs?page=2&size=2
```

Expected: `200 OK`
```json
{
  "content": [
    { "id": 1, "title": "Senior Java Developer", "location": "Bangalore" }
  ],
  "currentPage": 2,
  "totalPages": 3,
  "totalElements": 5,
  "last": true
}
```

---

### 3. GET /api/jobs/{id} — Get Job by ID

**Public — no headers required**

**Test 1 — Valid ID**
```
GET http://localhost:8082/api/jobs/1
```

Expected: `200 OK` with full job details

---

**Test 2 — Invalid ID (should fail)**
```
GET http://localhost:8082/api/jobs/999
```

Expected: `500` — `Job not found with id: 999`

---

### 4. GET /api/jobs/search — Search Jobs

**Public — no headers required**

**Test 1 — Search by title**
```
GET http://localhost:8082/api/jobs/search?title=java
```

Expected: `200 OK` — returns jobs with "java" in title
```json
{
  "content": [
    { "id": 5, "title": "Junior Java Developer" },
    { "id": 1, "title": "Senior Java Developer" }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 2,
  "last": true
}
```

---

**Test 2 — Search by location**
```
GET http://localhost:8082/api/jobs/search?location=bangalore
```

Expected: `200 OK` — returns all Bangalore jobs

---

**Test 3 — Search by jobType**
```
GET http://localhost:8082/api/jobs/search?jobType=FULL_TIME
```

Expected: `200 OK` — returns FULL_TIME jobs only

---

**Test 4 — Search by experience**
```
GET http://localhost:8082/api/jobs/search?experienceYears=2
```

Expected: `200 OK` — returns jobs needing 2 or fewer years

---

**Test 5 — Multiple filters combined**
```
GET http://localhost:8082/api/jobs/search?title=java&location=bangalore&jobType=FULL_TIME
```

Expected: `200 OK` — returns jobs matching all filters

---

**Test 6 — Search with pagination**
```
GET http://localhost:8082/api/jobs/search?title=java&page=0&size=1
```

Expected: `200 OK`
```json
{
  "content": [
    { "id": 5, "title": "Junior Java Developer" }
  ],
  "currentPage": 0,
  "totalPages": 2,
  "totalElements": 2,
  "last": false
}
```

---

**Test 7 — No results (should return empty list, not 404)**
```
GET http://localhost:8082/api/jobs/search?title=blockchain
```

Expected: `200 OK`
```json
{
  "content": [],
  "currentPage": 0,
  "totalPages": 0,
  "totalElements": 0,
  "last": true
}
```

---

**Test 8 — Invalid jobType (should fail with clear message)**
```
GET http://localhost:8082/api/jobs/search?jobType=WRONG_TYPE
```

Expected: `500` — `Invalid job type: WRONG_TYPE. Valid values are: FULL_TIME, PART_TIME, REMOTE, CONTRACT`

---

### 5. PUT /api/jobs/{id} — Update a Job

**Test 1 — Correct owner updates their job**
```
PUT http://localhost:8082/api/jobs/1
Content-Type: application/json
X-User-Id: 1
X-User-Role: RECRUITER
```
```json
{
  "title": "Senior Java Developer — Updated",
  "companyName": "TechCorp India",
  "location": "Bangalore",
  "salary": 1800000.00,
  "experienceYears": 4,
  "jobType": "FULL_TIME",
  "skillsRequired": "Java, Spring Boot, Microservices, MySQL, Redis",
  "description": "Updated description — now looking for 4 years experience.",
  "deadline": "2025-08-30"
}
```

Expected: `200 OK` with updated fields

---

**Test 2 — Wrong owner tries to update (should fail)**
```
X-User-Id: 2
X-User-Role: RECRUITER
```
```json
{
  "title": "Hacked Title",
  "companyName": "TechCorp India",
  "location": "Bangalore",
  "salary": 500000.00,
  "experienceYears": 1,
  "jobType": "PART_TIME",
  "skillsRequired": "Java",
  "description": "Trying to update someone elses job.",
  "deadline": "2025-05-01"
}
```

Expected: `500` — `You can only update your own jobs`

---

**Test 3 — Wrong role tries to update (should fail)**
```
X-User-Id: 1
X-User-Role: JOB_SEEKER
```
```json
{
  "title": "Senior Java Developer",
  "companyName": "TechCorp India",
  "location": "Bangalore",
  "salary": 1500000.00,
  "experienceYears": 3,
  "jobType": "FULL_TIME",
  "skillsRequired": "Java, Spring Boot",
  "description": "Job seeker trying to update a job.",
  "deadline": "2025-06-30"
}
```

Expected: `500` — `Only recruiters can update jobs`

---

### 6. DELETE /api/jobs/{id} — Delete a Job (Soft Delete)

**Test 1 — Correct owner deletes their job**
```
DELETE http://localhost:8082/api/jobs/3
X-User-Id: 1
X-User-Role: RECRUITER
```

Expected: `204 No Content` (empty response body)

---

**Test 2 — Verify soft delete — GET the deleted job**
```
GET http://localhost:8082/api/jobs/3
```

Expected: `500` — `Job not found with id: 3`

---

**Test 3 — Verify deleted job does not appear in list**
```
GET http://localhost:8082/api/jobs
```

Expected: `totalElements` is now 4, job 3 does not appear in content

---

**Test 4 — Wrong owner tries to delete (should fail)**
```
DELETE http://localhost:8082/api/jobs/1
X-User-Id: 2
X-User-Role: RECRUITER
```

Expected: `500` — `You can only delete your own jobs`

---

**Test 5 — Wrong role tries to delete (should fail)**
```
DELETE http://localhost:8082/api/jobs/1
X-User-Id: 1
X-User-Role: JOB_SEEKER
```

Expected: `500` — `Only recruiters can delete jobs`

---

### 7. GET /api/jobs/my-jobs — Recruiter's Own Jobs

**Test 1 — Recruiter sees their own jobs**
```
GET http://localhost:8082/api/jobs/my-jobs
X-User-Id: 1
X-User-Role: RECRUITER
```

Expected: `200 OK` — only jobs where `postedBy = 1`
```json
{
  "content": [
    { "id": 5, "title": "Junior Java Developer", "postedBy": 1 },
    { "id": 4, "title": "Python Developer", "postedBy": 1 },
    { "id": 1, "title": "Senior Java Developer — Updated", "postedBy": 1 }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 3,
  "last": true
}
```

---

**Test 2 — With pagination**
```
GET http://localhost:8082/api/jobs/my-jobs?page=0&size=1
```

Expected: `200 OK` — first job only, `totalElements` still shows full count

---

**Test 3 — Wrong role (should fail)**
```
GET http://localhost:8082/api/jobs/my-jobs
X-User-Id: 5
X-User-Role: JOB_SEEKER
```

Expected: `500` — `Only recruiters can access this endpoint`

---

## Business Rules

- Only users with role `RECRUITER` can create, update, or delete jobs
- A recruiter can only modify **their own** jobs — verified by comparing `X-User-Id` with `job.postedBy`
- Delete is a **soft delete** — the record stays in the database with `status = DELETED` and is excluded from all public responses
- All `GET` endpoints are public — no authentication required
- Jobs are always returned sorted by `createdAt` descending — newest first
- Search filters are all optional — any combination can be used together

---

## HTTP Status Code Reference

| Status | Meaning | When it happens |
|---|---|---|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Job successfully posted |
| 204 | No Content | Job successfully deleted |
| 500 | Server Error | Role mismatch, ownership violation, job not found |

---

## application.properties

```properties
spring.application.name=JPMS-JobService
server.port=8082

spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_oracle_username
spring.datasource.password=your_oracle_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect
spring.jpa.open-in-view=false

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.enabled=false
```