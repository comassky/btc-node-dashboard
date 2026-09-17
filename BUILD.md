# 🏗️ Build & Run Guide

This guide explains how to build and deploy the Bitcoin Node Dashboard.

## Prerequisites

**Required for local builds:** JDK 25 and Maven 3.9.11 or newer. Bitcoin Core with RPC enabled is needed to run the dashboard against a node, not for unit tests.

**Frontend toolchain:** Maven installs Node.js and its bundled npm, plus pnpm, under `src/main/web/node/`. Versions are defined in [pom.xml](pom.xml), currently Node.js v24.13.0 and pnpm 10.28.2. Global Node.js/pnpm installations are only needed for standalone frontend development.

**Docker:** Required for containerized native compilation and image builds. Building the JVM image with Docker does not require Java, Maven or Node.js on the host.

## 🚀 Quick Start

```bash
cd btc-node-dashboard

# Configure RPC
export RPC_HOST=localhost
export RPC_PORT=8332
export RPC_USER=your_username
export RPC_PASS=your_password

# Start backend with hot reload
mvn quarkus:dev # http://localhost:8080
```

Run the Vite frontend separately during development as described below.

## 🔨 Build Options

```bash
# Standard build: backend tests, frontend tests, frontend assets and JVM package
mvn -B --no-transfer-progress clean verify

# Skip both test suites for a local build only
mvn -B --no-transfer-progress clean package -DskipTests -DskipFrontendTests=true

# Native build using Mandrel in Docker; no local GraalVM required
mvn -B --no-transfer-progress clean verify -Dnative \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.native-image-xmx=6g
./target/btc-node-dashboard-*-runner
```

`mvn test` runs backend unit tests only. Frontend tests run during `prepare-package`, after Maven installs the frontend toolchain and dependencies. `-DskipTests` alone does not skip frontend tests. Failsafe integration tests remain disabled by the current `skipITs` setting; see [TESTING.md](TESTING.md).

The native command produces a Linux executable. Run it directly only on a compatible Linux host, or package it with [Dockerfile.native](Dockerfile.native). Allocate enough Docker memory for the 6 GiB native compiler heap plus build overhead.

## 🐳 Docker

See [DOCKER.md](DOCKER.md) for Docker build and run instructions.

### Profiles

Quarkus supports multiple profiles:

```bash
# Development profile (default in quarkus:dev)
mvn quarkus:dev

# Production profile (default in package)
java -jar target/quarkus-app/quarkus-run.jar

# Custom profile
java -Dquarkus.profile=staging -jar target/quarkus-app/quarkus-run.jar
```

## 🎨 Frontend Development

### Setup

```bash
cd src/main/web

# Use the Node.js and pnpm versions declared in pom.xml
npm install -g pnpm@10.28.2
pnpm install --frozen-lockfile
```

### Development Server

```bash
# Vite dev server with hot reload
pnpm dev
```

The Vite dev server will start on `http://localhost:5173` with proxy configured to forward API/WebSocket requests to `http://localhost:8080`.

**Note**: You still need to run the backend separately:

```bash
# In project root
mvn quarkus:dev
```

### Build Frontend Only

```bash
cd src/main/web

# Production build
pnpm build

# Build output: dist/ directory
```


The Maven build installs Node.js, npm and pnpm, runs `pnpm install --frozen-lockfile`, executes `pnpm run test --run`, synchronizes the frontend version with Maven using npm, and runs `pnpm build`. The resulting assets are copied into `target/classes/META-INF/resources/`. There is no fallback to `npm install`; keep [pnpm-lock.yaml](src/main/web/pnpm-lock.yaml) committed.

## 🧩 Monorepo & pnpm workspace

This project uses a pnpm workspace for frontend dependency management. See `src/main/web/pnpm-workspace.yaml`.

To install all dependencies:

```bash
cd src/main/web
pnpm install --frozen-lockfile
```

---

## 🚧 Troubleshooting

**Build fails**: Check `java -version` and `mvn -version`, then rebuild from a clean Maven output directory.

```bash
mvn -B --no-transfer-progress clean verify
```

If dependency installation reports an outdated lockfile after an intentional dependency change, run `pnpm install` in `src/main/web`, review the lockfile diff, and commit it with the manifest change. CI uses the frozen lockfile.

**Can't connect to Bitcoin Core**: Verify RPC settings, test connection

```bash
curl --user user:pass \
  --data-binary '{"method":"getblockchaininfo"}' \
  http://localhost:8332/
```

**Port 8080 in use**: Change port

```bash
export QUARKUS_HTTP_PORT=8888
mvn quarkus:dev
```

---

## CI and Releases

[docker.yml](.github/workflows/docker.yml) runs the native verification command for build-relevant changes, validates pull requests without publishing, and publishes development images on `main` and `develop`.

New releases are started manually with [release.yml](.github/workflows/release.yml); an existing tag can be published with [publish-image.yml](.github/workflows/publish-image.yml). A tag push alone does not publish an image. See [DOCKER.md](DOCKER.md) for tags, permissions and GitHub Pages setup.

For more help, see [GitHub Issues](https://github.com/comassky/btc-node-dashboard/issues).
