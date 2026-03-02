# ════════════════════════════════════════════════════════════════════════════
# Stage 1 — Build the application JAR
# ════════════════════════════════════════════════════════════════════════════
FROM gradle:8-jdk21 AS builder

WORKDIR /workspace

# Copy dependency metadata first for layer caching
COPY gradle/                  gradle/
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY build.gradle              build.gradle
COPY settings.gradle           settings.gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon --quiet

# Copy source and build
COPY src/ src/
RUN gradle bootJar --no-daemon -x test -x qualityGate

# ════════════════════════════════════════════════════════════════════════════
# Stage 2 — Minimal JRE runtime image
# ════════════════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="your-team@example.com" \
      org.opencontainers.image.source="https://github.com/your-org/hmtt-service-template"

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Own all files as the non-root user
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Default profile: prod (structured JSON logging, strict security)
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
