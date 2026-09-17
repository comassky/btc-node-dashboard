# 🐳 Docker Guide

This document explains how to build, run, and configure the Bitcoin Node Dashboard using Docker.

## Pre-built Images


Official images are available on GitHub Packages:
https://github.com/comassky/btc-node-dashboard/pkgs/container/btc-node-dashboard

---

**CI Note:** Quinoa installs Node.js and bundled npm during the Maven build, which runs backend and frontend unit tests and builds the frontend. Native workflows use Mandrel in a container, with no local GraalVM installation required. Failsafe integration tests remain disabled in the current Maven configuration.

Both Dockerfiles use the same pinned Distroless Debian 13 bases as wallet-viewer:
- [Dockerfile](Dockerfile): `gcr.io/distroless/java25-debian13:nonroot`, with a `maven:3.9.16-eclipse-temurin-25` builder running `verify`.
- [Dockerfile.native](Dockerfile.native): `gcr.io/distroless/base-debian13:nonroot`, using a previously compiled `target/*-runner`.

Both runtimes use non-root UID/GID `65532:65532`, with no shell. Use `JAVA_TOOL_OPTIONS` (not `JAVA_OPTS`) to override JVM options.

The base image digests are maintained by [renovate.json](renovate.json). The Maven builder stays on Java 25; image migrations require coordinated changes. The repository's own Compose image is not treated as a third-party dependency.

```bash
docker build -t btc-node-dashboard:jvm .
mvn -B --no-transfer-progress verify -Dnative -Dquarkus.native.container-build=true -Dquarkus.native.native-image-xmx=6g
docker build -f Dockerfile.native -t btc-node-dashboard:native .
```

The JVM Docker build only requires Docker on the host. The native build command requires JDK 25, Maven and Docker, with enough memory for the 6 GiB compiler heap plus overhead. Native workflows target `linux/amd64`; the copied executable must match the target platform. Tests must pass before either image is published.


### Run Native Image (Recommended)

```bash
docker run -d -p 8080:8080 \
  -e RPC_HOST=bitcoin-host \
  -e RPC_PORT=8332 \
  -e RPC_USER=your_user \
  -e RPC_PASS=your_password \
  -e WS_POLLING_INTERVAL=5 \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  -e DASHBOARD_CACHE_VALIDITY_BUFFER_MS=200 \
  ghcr.io/comassky/btc-node-dashboard:latest
```

Replace the example host and credentials with your node's values. The RPC host must be reachable from inside the container; `localhost` refers to the container, not the Docker host. Configure Bitcoin Core's RPC access rules accordingly.

For the locally built JVM image, use the same environment variables and replace the image name with `btc-node-dashboard:jvm`. The published workflow images are native; there is no published JVM tag in these workflows.


## Docker Compose

See [compose.yml](compose.yml) for the dashboard service, currently using the `main` image tag. It does not start Bitcoin Core. Replace its RPC placeholders with an existing node's connection details before running `docker compose up -d`. Use `latest` for stable releases or an explicit release tag for a fixed application version.

## Configuration

### Environment Variables

| Variable                             | Default     | Description                                                                                                           |
| ------------------------------------ | ----------- | --------------------------------------------------------------------------------------------------------------------- |
| `RPC_HOST`                          | Required   | Bitcoin node hostname or IP address                                                                                   |
| `QUARKUS_IO_THREADS`                 | `8`         | Number of IO threads for backend concurrency (recommended: 2 × number of CPU cores). Example: `QUARKUS_IO_THREADS=16` |
| `RPC_PORT`                          | Required   | Bitcoin RPC port, usually `8332` for mainnet                                                                           |
| `RPC_USER`                          | Required   | RPC username for authentication                                                                                       |
| `RPC_PASS`                          | Required   | RPC password for authentication                                                                                       |
| `BITCOIN_RPC_SCHEME`                 | `http`      | RPC protocol (`http` or `https`)                                                                                      |
| `WS_POLLING_INTERVAL`                | `5`         | Dashboard polling interval in seconds                                                                                 |
| `MIN_OUTBOUND_PEERS`                 | `8`         | Minimum number of outbound peers for dashboard health                                                                 |
| `DASHBOARD_CACHE_VALIDITY_BUFFER_MS` | `100`       | Cache validity buffer in ms for dashboard cache                                                                       |
| `DASHBOARD_SESSIONS_MAX`             | `1000`      | Maximum number of dashboard sessions                                                                                  |
| `DASHBOARD_CACHE_MAX_ITEMS`          | `1`         | Maximum number of items in dashboard cache                                                                            |
| `DASHBOARD_DISABLE_MEMPOOL`          | `false`     | Set to `true` to disable mempool info retrieval in the dashboard                                                      |
| `LOG_LEVEL`                          | `INFO`      | Application log level (`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`). Use `DEBUG` to see detailed startup configuration  |

These defaults come from [application.properties](src/main/resources/application.properties). Direct Quarkus overrides `BITCOIN_RPC_HOST`, `BITCOIN_RPC_PORT`, `BITCOIN_RPC_USER` and `BITCOIN_RPC_PASSWORD` are also supported, as used by Compose; they override the corresponding properties instead of their `RPC_*` placeholders. Use one convention consistently.

Both images bind HTTP to `0.0.0.0:8080`. The JVM image's default `JAVA_TOOL_OPTIONS` enables G1GC, string deduplication, exit-on-OOM and UTF-8; setting that variable replaces those defaults. Native images do not use JVM options.
### Application Properties

For local source builds, an alternative is `src/main/resources/application-local.properties` with the `local` profile explicitly selected:

```properties
bitcoin.rpc.scheme=http
bitcoin.rpc.host=localhost
bitcoin.rpc.port=8332
bitcoin.rpc.user=your_rpc_username
bitcoin.rpc.password=your_rpc_password
dashboard.polling.interval.seconds=5
dashboard.cache.validity.buffer.ms=200
```

Run with `mvn quarkus:dev -Dquarkus.profile=local`, or select `QUARKUS_PROFILE=local` when running an artifact built with this file. This file is not included in the published images; adding it to a local checkout does not configure an already pulled image. Use environment variables for pre-built images, and never commit credentials or bake them into distributable images.

Distroless images do not support `docker exec ... sh`. Use `docker logs <container>` for application output and `docker inspect <container>` for container configuration; avoid sharing inspect output containing credentials.


## Available Image Tags

Images are published by the following GitHub Actions workflows:
- [docker.yml](.github/workflows/docker.yml): validates pull requests without publishing; pushes to `main` publish `dev` and `main`, and pushes to `develop` publish `develop`. Documentation-only changes skip the build.
- [release.yml](.github/workflows/release.yml): manually takes a version `X.Y.Z`, verifies the native image, commits the Maven/frontend versions, creates the tag, prepares the next `-SNAPSHOT`, and publishes `<version>`, `latest`, and a GitHub Release.
- [publish-image.yml](.github/workflows/publish-image.yml): manually rebuilds an existing tag, or runs via `workflow_call`. Prerelease tags never update `latest`.

A tag push alone no longer starts a release. Use **Release - Create and Publish** for new releases or **Release - Publish Tag** to publish an existing tag.

No `<major>`, `<major>.<minor>`, `native` or `jvm` aliases are published. Pull requests never push images. The release workflow creates the next `<major>.<minor>.<patch+1>-SNAPSHOT` commit after tagging the verified release; this is a Maven/frontend development version, not a separately published `-SNAPSHOT` image tag.

For releases, allow workflow write permissions and ensure branch rules permit the release commits. If needed, configure `RELEASE_TOKEN` with the required repository permissions; otherwise `GITHUB_TOKEN` is used. Set **Settings > Pages > Source** to **GitHub Actions** for the released OpenAPI documentation. As in wallet-viewer, these workflows do not sign images with Cosign.

Only the release-creation workflow deploys GitHub Pages; the existing-tag workflow uploads schemas as an artifact without deploying the site. See [OPENAPI.md](OPENAPI.md) for deployment details. The release-creation workflow always checks out `main` for the release build.

Examples:

```bash
docker pull ghcr.io/comassky/btc-node-dashboard:latest
docker pull ghcr.io/comassky/btc-node-dashboard:dev
docker pull ghcr.io/comassky/btc-node-dashboard:develop
```


Notes:

- Development tags are generated by `docker/metadata-action`; release workflows explicitly construct their version and `latest` tags.
- Native images use the Distroless runtime in `Dockerfile.native`; release workflows publish versioned images only after verification.
