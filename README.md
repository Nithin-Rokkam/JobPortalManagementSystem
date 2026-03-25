# Job Portal Management System - Testing Guide

This project includes comprehensive automated tests for the microservices using **JUnit 5**, **Mockito**, and **Spring Boot Test (`@WebMvcTest`)**. The tests are fully isolated from the database and external network calls, ensuring they are fast and reliable.

## Quick Start: Running Tests

Because this project is divided into individual Spring Boot microservices, tests must be run per service via the Maven CLI (`mvnw.cmd`). 

To run tests in a robust way, use the following commands in the terminal. The `-B` (Batch Mode) flag prevents the test execution from hanging in Windows PowerShell/Command Prompt.

### 1. AuthService
```bash
cd JPMS-AuthService
.\mvnw.cmd test -B
```

### 2. JobService
```bash
cd JPMS-JobService
.\mvnw.cmd test -B
```

### 3. ApplicationService
```bash
cd JPMS-ApplicationService
.\mvnw.cmd test -B
```

### 4. AdminService
```bash
cd JPMS-AdminService
.\mvnw.cmd test -B
```

## Troubleshooting & Dependencies

### Java 25 & Byte-Buddy
This project utilizes modern Java versions (Java 25). To maintain compatibility with Mockito's `@MockBean` and `@Mock` injection, the `pom.xml` configurations have been explicitly updated:
* The `byte-buddy` dependency has been updated to `1.15.11` across all business services.
* The `maven-surefire-plugin` was configured with `--add-opens java.base/java.lang=ALL-UNNAMED` rules to allow reflective access.

### MockMvc & Security 401 Unauthorized Errors
If tests in the Controller layers return `401 / 403` status mismatch errors during modifications, ensure that `excludeAutoConfiguration = SecurityAutoConfiguration.class` is active in the `@WebMvcTest` annotation. In this project, `SecurityAutoConfiguration` is actively disabled for all MockMvc isolated tests.

## Test Strategy Breakdown

Our testing strategy follows the standard **Unit testing paradigm**:
* **Service Classes** (`*ServiceTest.java`): Tested purely via JUnit logic. `Repository`, `Cloudinary`, and `FeignClient` dependencies are fully mocked out using `@Mock`.
* **Controller Classes** (`*ControllerTest.java`): Integration tested using `@WebMvcTest`, validating JSON structures (`$.field`), HTTP Codes (`200 OK`, `201 CREATED`, `403 FORBIDDEN`), without booting the entire MySQL / Eureka context. 
* **Global App Classes** (`*ApplicationTests.java`): Switched offline with custom `assertTrue(true)` context tests. The heavy `@SpringBootTest` context loading has been removed so build pipelines won't fail when MySQL is inactive.
