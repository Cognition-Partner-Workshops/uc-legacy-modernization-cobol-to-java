# ============================================================
# CardDemo Modernized - Multi-stage Docker Build
# Migrated from mainframe COBOL/CICS to containerized Spring Boot
# ============================================================

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Add non-root user for security
RUN addgroup -S carddemo && adduser -S carddemo -G carddemo

COPY --from=builder /app/target/carddemo-modernized-*.jar app.jar

# Set ownership
RUN chown -R carddemo:carddemo /app
USER carddemo

# Expose application port
EXPOSE 8080

# Health check for Kubernetes liveness/readiness probes
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# JVM tuning for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
