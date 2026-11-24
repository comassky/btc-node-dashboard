# syntax=docker/dockerfile:1@sha256:572cebbc2b15e6f84c077742952674889cb63cd5e9b80abdffcbe587b6f19183
FROM maven:3.10.2-eclipse-temurin-25@sha256:fdb6138e3d3600784c9e5f540d34d89c5c7c6074f80f611abd2c6025d9b81ef7 AS builder
RUN apt-get update && apt-get install -y --no-install-recommends libatomic1 \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /build
RUN apt-get update \
    && apt-get install -y --no-install-recommends libatomic1 \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress verify

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:59bbb349c9752fc57b75744b41b1614c9dabe8bb4869523aabd5a4288f6c09a0 AS runner
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
