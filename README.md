# hmtt-service-template

A production-ready Spring Boot 3.5 / Java 21 reference service demonstrating
package-by-feature architecture, MapStruct mapping, Swagger UI, Spring Security,
Micrometer metrics, and a comprehensive quality toolchain.

---

## Architecture overview

```
┌─────────────────────────────────────────────────────────┐
│                        HTTP Client                       │
└───────────────────────────┬─────────────────────────────┘
                            │
              Spring Security (Basic Auth)
                            │
┌───────────────────────────▼─────────────────────────────┐
│  uk.co.hmtt.template.item                                  │
│  ┌─────────────────┐   ┌───────────────────────────────┐ │
│  │ ItemController  │──▶│ ItemService / ItemServiceImpl │ │
│  └─────────────────┘   └───────────────┬───────────────┘ │
│                                        │                  │
│                         ┌─────────────▼──────────┐       │
│                         │   ItemRepository        │       │
│                         │   (JpaRepository<Item>) │       │
│                         └─────────────┬──────────┘       │
└───────────────────────────────────────┼─────────────────┘
                                        │
                             H2 in-memory DB
                        (swap for Postgres in prod)

  uk.co.hmtt.template.common
  ┌──────────────────────────────────────────────┐
  │  GlobalExceptionHandler  ErrorResponse  AppConfig │
  └──────────────────────────────────────────────┘
```

---

## Package structure (package-by-feature)

```
src/main/java/uk/co/hmtt/template/
├── HmttServiceTemplateApplication.java  ← entry point
├── item/                                ← Item feature
│   ├── Item.java                        ← @Entity
│   ├── ItemController.java              ← REST endpoints
│   ├── ItemMapper.java                  ← MapStruct interface
│   ├── ItemRepository.java              ← Spring Data JPA
│   ├── ItemRequest.java                 ← request record (DTO)
│   ├── ItemResponse.java                ← response record (DTO)
│   ├── ItemService.java                 ← service contract
│   └── ItemServiceImpl.java             ← service implementation
└── common/                              ← cross-cutting concerns
    ├── AppConfig.java                   ← Security + Micrometer config
    ├── ErrorResponse.java               ← error envelope record
    └── GlobalExceptionHandler.java      ← @RestControllerAdvice

src/test/java/uk/co/hmtt/template/
├── ArchitectureTest.java                ← ArchUnit rules
└── item/
    ├── ItemControllerTest.java          ← @WebMvcTest
    ├── ItemMapperTest.java              ← pure unit test
    └── ItemServiceImplTest.java         ← Mockito unit test

src/functionalTest/java/uk/co/hmtt/template/
└── ItemFunctionalTest.java              ← @SpringBootTest + TestRestTemplate
```

---

## How to build

```bash
./gradlew build
```

> This runs compilation, unit tests, SpotBugs, Checkstyle, JaCoCo (≥ 80 % line coverage),
> OWASP Dependency Check, and PIT mutation testing.
> On first run, OWASP downloads the NVD database — this may take a few minutes.

---

## Common tasks

| Goal | Command |
|------|---------|
| Unit tests only | `./gradlew test` |
| Architecture tests | included in `./gradlew test` |
| Mutation tests | `./gradlew pitest` |
| Full build + FTs | `./gradlew fullBuild` |
| All quality checks | `./gradlew qualityGate` |
| Run the app | `./gradlew bootRun` |
| Run via Docker | `docker-compose up` |
| Format code | `./gradlew spotlessApply` |

---

## Running the app locally

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Or set the environment variable:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

---

## Running via Docker

```bash
docker-compose up --build
```

The app starts on port **8080** with `SPRING_PROFILES_ACTIVE=prod` (JSON logging).

---

## Useful endpoints

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
| Info (build metadata) | http://localhost:8080/actuator/info |
| Prometheus metrics | http://localhost:8080/actuator/prometheus |
| H2 Console (local only) | http://localhost:8080/h2-console |

---

## Security

Default credentials (**development only — replace before going to production**):

| Field | Value |
|-------|-------|
| Username | `user` |
| Password | `password` |

The in-memory user is defined in `AppConfig.java`. Replace with OAuth 2 / JWT authentication
before deploying to a real environment.

Swagger UI, `/actuator/health`, and `/actuator/info` are publicly accessible.
All other endpoints require HTTP Basic authentication.

---

## Spring profiles

| Profile | Logging | Notes |
|---------|---------|-------|
| `local` | Human-readable console | H2 console enabled |
| `dev` | Structured JSON (Logstash) | For shared dev environments |
| `prod` | Structured JSON (Logstash) | Default inside Docker |

---

## Code formatting (Spotless)

A **pre-commit Git hook** is automatically installed the first time you run any Gradle task.
It runs `./gradlew spotlessApply` before each commit so no manual setup is required.

To apply formatting manually:

```bash
./gradlew spotlessApply
```

To check formatting without modifying files (as CI does):

```bash
./gradlew spotlessCheck
```

---

## IDE setup

### IntelliJ IDEA
1. Install the **Lombok plugin** (`Settings › Plugins`).
2. Enable annotation processing (`Settings › Build › Compiler › Annotation Processors › Enable`).
3. Install the **MapStruct Support** plugin for navigation.

### VS Code
Install the **Extension Pack for Java** and the **Lombok Annotations Support** extension.

---

## Quality reports

After running `./gradlew build` or `./gradlew qualityGate`, reports are in:

| Tool | Location |
|------|----------|
| JaCoCo | `build/reports/jacoco/test/html/index.html` |
| SpotBugs | `build/reports/spotbugs/main.html` |
| Checkstyle | `build/reports/checkstyle/main.html` |
| OWASP | `build/reports/dependency-check-report.html` |
| PIT | `build/reports/pitest/index.html` |

---

## CI/CD

The GitHub Actions workflow (`.github/workflows/build.yml`) runs on push and pull request to
`main` and `develop`:

1. Compiles and runs unit tests (`./gradlew build -x qualityGate`)
2. Runs all quality checks (`./gradlew qualityGate`)
3. Uploads JaCoCo, SpotBugs, Checkstyle, and PIT reports as build artefacts

Dependabot is configured to check for Gradle and GitHub Actions dependency updates weekly
(`.github/dependabot.yml`).
