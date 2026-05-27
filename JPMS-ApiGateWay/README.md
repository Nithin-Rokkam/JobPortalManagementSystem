# JPMS — API Gateway

**Port:** `9090` | **Spring Application Name:** `api-gateway`

The API Gateway is the single entry point for all client requests to the Joblix platform. It is built on Spring Cloud Gateway (reactive, non-blocking) and performs JWT validation, route forwarding to downstream microservices, internal endpoint protection, and CORS preflight handling. It injects `X-User-Id` and `X-User-Role` headers into every authenticated request so downstream services can perform role-based logic without re-parsing the JWT.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Main Application Class](#main-application-class)
4. [GatewayJwtFilter — Core Security Filter](#gatewayjwtfilter--core-security-filter)
5. [JwtUtil — Token Parsing](#jwtutil--token-parsing)
6. [CorsConfig](#corsconfig)
7. [Route Configuration](#route-configuration)
8. [Public vs Protected Routes](#public-vs-protected-routes)
9. [Configuration](#configuration)

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.x + Spring Cloud Gateway (Reactive) |
| Security | Custom JWT Global Filter |
| Service Discovery | Netflix Eureka Client |
| Build | Maven |

---

## Project Structure

```
JPMS-ApiGateWay/src/main/java/com/capg/jobportal/
├── JpmsApiGateWayApplication.java        ← Entry point
├── config/
│   └── CorsConfig.java                   ← CORS configuration
├── filter/
│   └── GatewayJwtFilter.java             ← Global JWT authentication filter
└── util/
    └── JwtUtil.java                      ← JWT parsing and validation utility
```

---

## Main Application Class

```java
@SpringBootApplication
@EnableDiscoveryClient   // Registers with Eureka, enables lb:// routing
public class JpmsApiGateWayApplication { ... }
```

**Annotations:**
- `@EnableDiscoveryClient` — enables Eureka-based service discovery so routes can use `lb://service-name` URIs for client-side load balancing.

---

## GatewayJwtFilter — Core Security Filter

```java
@Component
public class GatewayJwtFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) { ... }

    @Override
    public int getOrder() { return -1; }  // Highest priority — runs before all other filters
}
```

**Implements:**
- `GlobalFilter` — applied to every request passing through the Gateway.
- `Ordered` with `getOrder() = -1` — ensures this filter runs before Spring Cloud Gateway's built-in filters.

### Filter Execution Flow

```
Incoming Request
       │
       ▼
1. Allow OPTIONS (CORS preflight) → pass through
       │
       ▼
2. Block /internal/** paths → 403 Forbidden
       │
       ▼
3. Allow /swagger-ui/** and /v3/api-docs/** → pass through
       │
       ▼
4. Check isPublicRoute(path, method) → pass through if public
       │
       ▼
5. Extract Authorization header
   → Missing or not "Bearer ..." → 401 Unauthorized
       │
       ▼
6. Validate JWT token via JwtUtil.isTokenValid()
   → Invalid/expired → 401 Unauthorized
       │
       ▼
7. Extract userId and role from token
       │
       ▼
8. Mutate request: add X-User-Id and X-User-Role headers
       │
       ▼
9. Forward to downstream microservice
```

### `isPublicRoute(path, method)`
Returns `true` (no auth required) for:
```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/forgot-password
POST /api/auth/reset-password
POST /api/auth/verify-registration
POST /api/auth/resend-registration-otp
GET  /api/jobs
GET  /api/jobs/search
GET  /api/jobs/{numeric-id}   (regex: /api/jobs/\d+)
```

### `onError(exchange, HttpStatus)`
Sets the response status code and completes the response without forwarding the request. Used for 401 and 403 responses.

### Header Injection
After successful JWT validation, the filter mutates the request to add:
- `X-User-Id` — the user's ID extracted from the JWT subject.
- `X-User-Role` — the user's role extracted from the JWT `role` claim.

Downstream services read these headers instead of re-parsing the JWT, keeping them decoupled from authentication logic.

---

## JwtUtil — Token Parsing

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}") private String secret;

    private Key getSigningKey()
    // Builds HMAC-SHA key from the secret string

    public Claims extractAllClaims(String token)
    // Parses JWT, verifies signature, returns all claims

    public boolean isTokenValid(String token)
    // Returns true if token parses successfully and expiry is in the future

    public String extractUserId(String token)
    // Returns the subject (userId) from claims

    public String extractRole(String token)
    // Returns the "role" custom claim
}
```

The Gateway's `JwtUtil` uses the **same secret** as the AuthService's `JwtUtil` — both read from `${jwt.secret}` which is set via the `API_GATEWAY_JWT_SECRET` environment variable. This shared secret is what allows the Gateway to independently verify tokens without calling the AuthService.

---

## CorsConfig

Configures CORS to allow the Angular frontend to communicate with the Gateway:
- Allowed origins: `http://localhost:4200` (and production domain).
- Allowed methods: `GET, POST, PUT, PATCH, DELETE, OPTIONS`.
- Allowed headers: `*` (including `Authorization`, `Content-Type`).
- `allowCredentials: true`.

---

## Route Configuration

Defined in `application.yml` under `spring.cloud.gateway.routes`:

| Route ID | URI | Path Predicate | Description |
|---|---|---|---|
| `auth-service` | `lb://auth-service` | `/api/auth/**` | All auth endpoints |
| `job-service` | `lb://job-service` | `/api/jobs/**` | All job endpoints |
| `application-service` | `lb://application-service` | `/api/applications/**` | All application endpoints |
| `admin-service` | `lb://admin-service` | `/api/admin/**` | All admin endpoints |
| `auth-swagger` | `lb://auth-service` | `/auth-docs/**` | Swagger docs proxy |
| `swagger-ui-direct` | `lb://auth-service` | `/swagger-ui/**` | Swagger UI |
| `api-docs-direct` | `lb://auth-service` | `/v3/api-docs/**` | OpenAPI JSON |
| `auth-service-internal` | `lb://auth-service` | `/api/internal/users/**` | Internal (Feign only) |
| `job-service-internal` | `lb://job-service` | `/api/internal/jobs/**` | Internal (Feign only) |
| `application-service-internal` | `lb://application-service` | `/api/internal/applications/**` | Internal (Feign only) |

**`lb://` prefix** — tells Spring Cloud Gateway to use Eureka for service discovery and client-side load balancing.

> Internal routes (`/api/internal/**`) are routed through the Gateway but blocked by `GatewayJwtFilter` for any external request. Feign clients between services call each other directly via Eureka, bypassing the Gateway entirely.

---

## Public vs Protected Routes

| Route | Auth Required | Notes |
|---|---|---|
| `POST /api/auth/register` | No | |
| `POST /api/auth/login` | No | |
| `POST /api/auth/refresh` | No | |
| `POST /api/auth/forgot-password` | No | |
| `POST /api/auth/reset-password` | No | |
| `POST /api/auth/verify-registration` | No | |
| `POST /api/auth/resend-registration-otp` | No | |
| `GET /api/jobs` | No | Public job browsing |
| `GET /api/jobs/search` | No | Public job search |
| `GET /api/jobs/{id}` | No | Public job detail |
| All other `/api/**` | Yes — JWT Bearer token | |
| `/api/internal/**` | Blocked (403) | Internal use only |

---

## Configuration

### application.yml (key settings)

```yaml
server.port: 9090
spring.application.name: api-gateway
spring.cloud.gateway.discovery.locator.enabled: false  # Manual route config
jwt.secret: ${API_GATEWAY_JWT_SECRET}
eureka.client.service-url.defaultZone: http://localhost:8761/eureka/
eureka.instance.prefer-ip-address: true
```

The `jwt.secret` must match the value used by AuthService to sign tokens. Both are set from the same environment variable in the `.env` file.
