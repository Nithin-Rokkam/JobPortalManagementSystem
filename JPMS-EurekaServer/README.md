# JPMS — Eureka Server

**Port:** `8761` | **Spring Application Name:** `eureka-server`

The Eureka Server is the service registry and discovery backbone of the Joblix microservices platform. All microservices register themselves with Eureka on startup and use it to discover each other by name (e.g., `auth-service`, `job-service`) rather than hardcoded URLs. The API Gateway uses Eureka to resolve `lb://service-name` routes for client-side load balancing.

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| Service Registry | Netflix Eureka Server |
| Build | Maven |

---

## Project Structure

```
JPMS-EurekaServer/src/main/java/com/capg/jobportal/
└── JpmsEurekaServerApplication.java   ← Entry point
```

---

## Main Application Class

```java
@SpringBootApplication
@EnableEurekaServer   // Activates the Eureka registry server
public class JpmsEurekaServerApplication { ... }
```

**Annotations:**
- `@EnableEurekaServer` — turns this Spring Boot application into a Netflix Eureka registry server. All other services with `@EnableDiscoveryClient` will register here.

---

## Configuration

### application.yml

```yaml
server.port: 8761
spring.application.name: eureka-server

eureka:
  instance:
    hostname: eureka-server
  client:
    register-with-eureka: false   # Server does not register itself
    fetch-registry: false         # Server does not fetch registry from peers
  server:
    wait-time-in-ms-when-sync-empty: 0
    enable-self-preservation: false   # Disabled for dev — prevents stale registrations
```

**Key settings explained:**
- `register-with-eureka: false` — the Eureka Server itself does not register as a client.
- `fetch-registry: false` — standalone mode, no peer replication.
- `enable-self-preservation: false` — in development, this prevents Eureka from keeping stale service registrations when heartbeats are missed. **Enable this in production.**
- `wait-time-in-ms-when-sync-empty: 0` — starts immediately without waiting for peer sync.

---

## Services That Register With Eureka

| Service | Registered Name | Port |
|---|---|---|
| API Gateway | `api-gateway` | 9090 |
| Auth Service | `auth-service` | 8081 |
| Job Service | `job-service` | 8082 |
| Application Service | `application-service` | 8083 |
| Admin Service | `admin-service` | 8084 |
| Notification Service | `notification-service` | 8085 |

---

## Eureka Dashboard

Once running, the Eureka dashboard is accessible at:
```
http://localhost:8761
```

It shows all registered service instances, their status (UP/DOWN), and metadata.

---

## Startup Order

Eureka Server must start **before** all other microservices. In Docker Compose, use `depends_on` to enforce this:

```yaml
auth-service:
  depends_on:
    - eureka-server
    - jobportal-db
    - rabbitmq
```
