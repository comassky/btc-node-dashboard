# ----------------------------------------------------------------------
# Stage 1: Builder - compile and create the Quarkus fast-jar archive
# ----------------------------------------------------------------------
# Uses a full Java/Maven image for the build. This image contains 'mvn'.
FROM maven:3-eclipse-temurin-25-alpine AS builder

# Set the working directory in the container
WORKDIR /build

# Copy Maven wrapper to use the project's configured version
COPY mvnw .
COPY .mvn .mvn

# Copy POM file and dependencies (to leverage Docker cache)
COPY pom.xml .
# Download all Maven dependencies with mounted cache to speed up builds
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Copy source code
COPY src /build/src

# Package the Quarkus application as fast-jar with Maven cache
# Quarkus generates necessary files in /target/quarkus-app
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -B -ntp

# ----------------------------------------------------------------------
# Stage 2: Runner - execute the application (minimal JRE image)
# ----------------------------------------------------------------------
# Uses a minimal JRE/JDK image for execution (smaller than full JDK)
FROM eclipse-temurin:25-jre-alpine AS runner

# Create non-root directory for execution (first for better caching)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy artifacts from builder (fast-jar) with correct ownership
# The 'quarkus-app' folder contains the main jar and libraries
COPY --from=builder --chown=appuser:appgroup /build/target/quarkus-app /app

# Switch to non-root user
USER appuser

# Define the port exposed by the application
EXPOSE 8080

# Modern JVM options for better performance (G1GC, heap size, tuning)
ENV JAVA_OPTS="-XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -XX:InitialRAMPercentage=50.0 \
    -XX:MaxRAMPercentage=80.0 \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+UseContainerSupport \
    -Dfile.encoding=UTF-8 \
    -Djava.awt.headless=true"

# Execution command with JVM options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/quarkus-run.jar"]
