# syntax=docker/dockerfile:1@sha256:4edf897a3ffa55b89f906fc8cc78afdb3f1834cc9c7083565e611a8a7d5fe99e
FROM maven:3.10.0-eclipse-temurin-25@sha256:047a82fc82c613266cfb1b66fd920be9c8ba51fd67edc68245e4542768f45e36 AS builder
WORKDIR /build

COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress verify

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:8ac5525549d14b8ddd34562f9fc8e6ba4840c995363a1507b17cadf8bb7ab57e AS runner
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
