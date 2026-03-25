# Job Portal Management System - Comprehensive Testing Guide

This document provides a deep dive into the testing architecture, test flows, and execution strategy for the Job Portal Management System microservices. The project utilizes **JUnit 5**, **Mockito**, and **Spring Boot Test (`@WebMvcTest`)** to ensure robust, isolated, and fast-executing testing suites.

---

## 🏗️ Testing Architecture & Strategy

The testing strategy is designed to isolate different layers of the application to ensure that tests run quickly without relying on heavy external dependencies like active MySQL databases, Cloudinary accounts, or Eureka Servers. 

We separate our tests into two primary categories across all microservices:

### 1. Service Layer Tests (Unit Tests)
* **Goal**: Verify core business logic, exception handling, data transformations, and role validations.
* **Tools**: `Mockito` (`@ExtendWith(MockitoExtension.class)`), `@InjectMocks`, `@Mock`.
* **Flow**:
  1. **Mock Dependencies**: All external dependencies (Repositories, Feign Clients, JWT Utilities, Cloudinary uploaders) are mocked using `@Mock`.
  2. **Inject Mocks**: The service under test automatically receives these mocks via `@InjectMocks`.
  3. **Stubbing**: We use `when(...).thenReturn(...)` to simulate database queries finding users or external API calls succeeding.
  4. **Execution & Assertion**: We call the target service method and use Assertions (`assertEquals`, `assertNotNull`, `assertThrows`) to verify the data integrity or expected failure paths.
  5. **Verification**: We use `verify(repository, times(1)).save(any())` to guarantee that data mutations natively occurred exactly when they were intended to.

### 2. Controller Layer Tests (Integration Tests)
* **Goal**: Verify REST HTTP endpoints, request routing, JSON serialization mappings, and HTTP status codes.
* **Tools**: `MockMvc`, `@WebMvcTest`.
* **Flow**:
  1. **Web Context Loading**: `@WebMvcTest` slices the application and loads strictly the web layer (Controllers) without booting the entire application context, saving massive amounts of memory and build time.
  2. **Security Bypassing**: To avoid generating complex and expiring JWT tokens strictly for endpoint testing, Spring Security (`SecurityAutoConfiguration`) is explicitly disabled in the test context.
  3. **Service Mocking**: The underlying business logic (Service) is mocked using Spring's `@MockBean`.
  4. **MockMvc Requests**: We simulate incoming HTTP requests (`GET`, `POST`, `PUT`, `DELETE`) with exact JSON payloads and headers.
  5. **JSON Assertions**: We chain assertions like `.andExpect(status().isOk())` and `.andExpect(jsonPath("$.message").value("..."))` to validate the HTTP response.

---

## 🔍 Microservice Test Flows Explained

### 1. AuthService Tests
**Service Layer (`AuthServiceTest.java`)**:
* **Registration Flow**: Simulates a new user signup. It mocks `UserRepository` to return empty (simulating the email is available), mocks the `PasswordEncoder`, and verifies that the system saves the record. It also tests the alternative route: throwing `UserAlreadyExistsException` if the email is taken.
* **Login Flow**: Tests successful JWT generation by mocking `JwtUtil`. It extensively covers security logic failure scenarios, such as throwing `InvalidCredentialsException` for bad passwords and `DisabledException` for users whose accounts have been banned by an admin.

**Controller Layer (`AuthControllerTest.java`)**:
* Directly simulates HTTP `POST` requests to `/api/auth/register` and `/api/auth/login`. Ensures the JSON request payload seamlessly translates into the internal `AuthRequest` DTO and maps the service's `AuthResponse` back to the browser.

### 2. JobService Tests
**Service Layer (`JobServiceTest.java`)**:
* **Job Posting & RBAC**: Contains Role-Based Access Control logic testing. It verifies that a user with the `EMPLOYER` role can successfully execute `save()`. Conversely, it asserts that a `ForbiddenException` is thrown strictly if a `JOB_SEEKER` attempts to post a job.
* **Job Retrieval**: Mocks `JobRepository` to return lists of fake jobs and validates that DTO factory mappers assemble the `JobResponse` accurately with the correct status flags.

**Controller Layer (`JobControllerTest.java`)**:
* Tests standard endpoints: `/api/jobs` and `/api/jobs/{id}`. 
* Strictly validates endpoints mapped to `DELETE` appropriately return HTTP `204 No Content` upon completion.

### 3. ApplicationService Tests
**Service Layer (`ApplicationServiceTest.java`)**:
* **Application Submission (Cross-Service)**: This service relies on inter-service communications. The test mocks `AdminJobClient` (Feign Client) to simulate successfully pinging the remote `JobService` to verify the job actually exists.
* **State Machine & Logic**: Verifies logic preventing duplicate job applications. Mocks the internal `CloudinaryUtil` static methods ensuring that resumes are properly verified dynamically without hitting AWS/Cloudinary cloud networks.

### 4. AdminService Tests
**Service Layer (`AdminServiceTest.java`)**:
* **Orchestration**: As the AdminService mostly aggregates, tests mock the multiple Feign clients (`AdminUserClient`, `AdminJobClient`) to verify that the Admin service correctly pulls and stitches together statistical Reports and Audit Logs from the surrounding services.

**Controller Layer (`AdminControllerTest.java`)**:
* **Role Gateways**: Verifies that passing a header `X-User-Role: JOB_SEEKER` to an admin-restricted endpoint accurately triggers the system to return an `HTTP 403 Forbidden` flag, validating that administrative data leaks are functionally impossible.

---

## ⚙️ How to Run the Tests

Because the microservices operate independently within the Monorepo structure, tests must be executed individually inside each service directory.

**Important:** The `-B` (Batch Mode) flag is critical for Windows environments. It prevents the internal test execution loggers from prompting you with `Terminate batch job (Y/N)?` and fatally freezing your terminal.

Execute the following sequentially within your terminal at the root directory:

```bash
# 1. Run AuthService Tests
cd JPMS-AuthService
.\mvnw.cmd test -B
cd ..

# 2. Run JobService Tests
cd JPMS-JobService
.\mvnw.cmd test -B
cd ..

# 3. Run ApplicationService Tests
cd JPMS-ApplicationService
.\mvnw.cmd test -B
cd ..

# 4. Run AdminService Tests
cd JPMS-AdminService
.\mvnw.cmd test -B
cd ..
```

---

## 🛠️ Behind The Scenes: Java 25 Mockito Engine Fixes

During configuration, special low-level adjustments were made to secure execution on **Java 25**:

1. **Byte-Buddy Updating**: Mockito dynamically generates dummy class files (proxies) using a library named `byte-buddy`. The older version packaged with Spring Boot 3.2.5 crashed on Java 25. The tests are stabilized by explicitly injecting `<byte-buddy.version>1.15.11</byte-buddy.version>` into all `pom.xml` dependencies.
2. **Reflection Access (Surefire Plugin)**: Modern JVMs fiercely restrict "Reflective Access" (peeking into private variables). The `maven-surefire-plugin` was manually updated to include JVM arguments (`--add-opens java.base/java.lang=ALL-UNNAMED`) to manually authorize Mockito to peer inside Java's core boundaries.
3. **Application Smoke Tests**: The heavy, default `@SpringBootTest` context loading files (e.g. `JpmsAuthServiceApplicationTests.java`) were neutralized to execute a lightweight `assertTrue(true)` statement. This specifically prevents Jenkins/GitHub build pipelines from failing automatically when a physical MySQL database connection cannot be located during the initial build phase.
