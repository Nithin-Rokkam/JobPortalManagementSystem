# JPMS AdminService - Admin API Testing Guide

This guide helps you test admin endpoints exposed through API Gateway.

## 1) Prerequisites

- Java + Maven installed
- Services needed for this flow are running (at minimum API Gateway and AdminService)
- Gateway URL reachable at:

`http://localhost:9090`

## 2) Why You May See 403

Admin endpoints require:

- Header: `X-User-Role: ADMIN`

If this header is missing or has any other value (like `USER`, `RECRUITER`, `JOB_SEEKER`), API returns:

- `403 Forbidden`
- Message: `Access denied. ADMIN role required.`

This is expected authorization behavior.

## 3) Postman Setup

Create a Postman Environment variable:

- `baseUrl = http://localhost:9090`

Use request URLs as:

- `{{baseUrl}}/api/admin/...`

### Common Headers

For admin success calls:

- `X-User-Role: ADMIN`

For modify operations (delete/ban/unban), also include:

- `X-User-Id: 1` (example admin id)

## 4) Core Test Cases

### A. Role validation on Users API

#### Test A1 - Missing role header
- Method: `GET`
- URL: `{{baseUrl}}/api/admin/users`
- Headers: _none_
- Expected: `403 Forbidden`

#### Test A2 - Non-admin role
- Method: `GET`
- URL: `{{baseUrl}}/api/admin/users`
- Headers:
  - `X-User-Role: USER`
- Expected: `403 Forbidden`

#### Test A3 - Admin role
- Method: `GET`
- URL: `{{baseUrl}}/api/admin/users`
- Headers:
  - `X-User-Role: ADMIN`
- Expected: `200 OK` + users list

---

### B. Other admin read APIs

#### Test B1 - Jobs list
- Method: `GET`
- URL: `{{baseUrl}}/api/admin/jobs`
- Header:
  - `X-User-Role: ADMIN`
- Expected: `200 OK`

#### Test B2 - Reports
- Method: `GET`
- URL: `{{baseUrl}}/api/admin/reports`
- Header:
  - `X-User-Role: ADMIN`
- Expected: `200 OK`

#### Test B3 - Audit logs
- Method: `GET`
- URL: `{{baseUrl}}/api/admin/audit-logs`
- Header:
  - `X-User-Role: ADMIN`
- Expected: `200 OK`

---

### C. Admin write APIs (require role + user id)

#### Test C1 - Delete user
- Method: `DELETE`
- URL: `{{baseUrl}}/api/admin/users/{id}`
- Headers:
  - `X-User-Role: ADMIN`
  - `X-User-Id: 1`
- Expected: `200 OK` on valid id

#### Test C2 - Ban user
- Method: `PUT`
- URL: `{{baseUrl}}/api/admin/users/{id}/ban`
- Headers:
  - `X-User-Role: ADMIN`
  - `X-User-Id: 1`
- Expected: `200 OK` on valid id

#### Test C3 - Unban user
- Method: `PUT`
- URL: `{{baseUrl}}/api/admin/users/{id}/unban`
- Headers:
  - `X-User-Role: ADMIN`
  - `X-User-Id: 1`
- Expected: `200 OK` on valid id

#### Test C4 - Delete job
- Method: `DELETE`
- URL: `{{baseUrl}}/api/admin/jobs/{id}`
- Headers:
  - `X-User-Role: ADMIN`
  - `X-User-Id: 1`
- Expected: `200 OK` on valid id

## 5) curl Quick Checks

curl -i http://localhost:9090/api/admin/users 
curl -i -H "X-User-Role: USER" http://localhost:9090/api/admin/users
curl -i -H "X-User-Role: ADMIN" http://localhost:9090/api/admin/users 
curl -i -H "X-User-Role: ADMIN" http://localhost:9090/api/admin/jobs 
curl -i -H "X-User-Role: ADMIN" http://localhost:9090/api/admin/reports 
curl -i -H "X-User-Role: ADMIN" http://localhost:9090/api/admin/audit-logs


## 6) Response Expectations Summary

- Missing role header -> `403`
- Role is not `ADMIN` -> `403`
- Role is `ADMIN` -> `200` (if downstream services/data are available)
- Missing required `X-User-Id` on write APIs -> typically `400`

## 7) Troubleshooting

- `403 Forbidden` with admin APIs:
  - Check exact header key/value:
    - Key: `X-User-Role`
    - Value: `ADMIN` (uppercase)
- `400 Bad Request` on write APIs:
  - Ensure `X-User-Id` header is present
- `5xx` or downstream errors:
  - Verify dependent services are up and reachable from AdminService
