# syntax=docker/dockerfile:1@sha256:4edf897a3ffa55b89f906fc8cc78afdb3f1834cc9c7083565e611a8a7d5fe99e
FROM maven:3.10.0-eclipse-temurin-25@sha256:0396dcd8cd0d46a0d2026449b714b2a5bbe53cce030975b5a41f8ffa3f5f0525 AS builder
WORKDIR /build

COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress verify

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:28a3986989d7d74cb5cfd6ba369e64d3d634f83ac9fa562dac8db2d20827f90e AS runner
WORKDIR /app

COPY --from=builder --chown=65532:65532 /build/target/quarkus-app/lib/ ./lib/
COPY --from=builder --chown=65532:65532 /build/target/quarkus-app/*.jar ./
COPY --from=builder --chown=65532:65532 /build/target/quarkus-app/app/ ./app/
COPY --from=builder --chown=65532:65532 /build/target/quarkus-app/quarkus/ ./quarkus/

USER 65532:65532

EXPOSE 8080

ENV QUARKUS_HTTP_HOST=0.0.0.0

ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+UseContainerSupport \
    -Dfile.encoding=UTF-8 \
    -Djava.awt.headless=true"

CMD ["quarkus-run.jar"]
