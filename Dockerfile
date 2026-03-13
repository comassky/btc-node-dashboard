# ----------------------------------------------------------------------
# Stage 1: Builder - compile and create the Quarkus fast-jar archive
# ----------------------------------------------------------------------
FROM maven:3-eclipse-temurin-25-alpine AS builder

WORKDIR /build

# Copy Maven configuration files first for better caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Pre-download dependencies with cache mount (faster rebuilds)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B -ntp

# Copy source code
COPY src /build/src

# Build with optimizations: parallel compilation, skip tests, no tail processing
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -B -ntp \
    -T 1C \
    -Dmaven.compiler.useIncrementalCompilation=true

# Reduce layer size - remove build artifacts not needed in final image
RUN find /root/.m2 -type d -name '*.git' -exec rm -rf {} + 2>/dev/null || true

# ----------------------------------------------------------------------
# Stage 2: Runner - execute the application (minimal JRE image)
# ----------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine

# Install dumb-init for proper signal handling (PID 1 problem)
RUN apk add --no-cache dumb-init

# Create non-root user first for better layer caching
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built application with correct permissions
COPY --from=builder --chown=appuser:appgroup /build/target/quarkus-app /app

USER appuser

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/q/health || exit 1

EXPOSE 8080

# Advanced JVM tuning for production containers
# G1GC is optimal for containers with 1-4GB heap
# String deduplication saves significant memory for long-running services
# UseContainerSupport enables cgroup-aware memory detection
# TieredCompilation balances startup time with long-term performance
ENV JAVA_OPTS="\
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=75 \
    -XX:InitiatingHeapOccupancyPercent=35 \
    -XX:G1HeapRegionSize=16m \
    -XX:+UseStringDeduplication \
    -XX:+UnlockDiagnosticVMOptions \
    -XX:G1SummarizeRSetStatsPeriod=86400 \
    -XX:+UseContainerSupport \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+PerfDisableSharedMem \
    -Dfile.encoding=UTF-8 \
    -Djava.awt.headless=true \
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Run with dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "sh", "-c", "java $JAVA_OPTS -jar /app/quarkus-run.jar"]
