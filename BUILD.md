# 🏗️ Build & Run Guide

This guide explains how to build and deploy the Bitcoin Node Dashboard.

## Prerequisites

**Required:** Java 25+, Maven 3.9.11+ (Maven Wrapper included), Bitcoin Core with RPC enabled

**Optional:** Node.js 24+ (v24.12.0 recommended), pnpm 11.6.2 (recommended), npm 11.6.2, Docker

## 🚀 Quick Start

```bash
# Clone repository
````

cd btc-node-dashboard

# Configure RPC

export BITCOIN_RPC_HOST=localhost
export BITCOIN_RPC_PORT=8332
export BITCOIN_RPC_USER=your_username
export BITCOIN_RPC_PASSWORD=your_password

# Start backend with hot reload

./mvnw quarkus:dev # http://localhost:8080

````

## 🔨 Build Options

```bash
# Standard build
./mvnw clean package

# Skip tests
./mvnw clean package -DskipTests

# GraalVM Native (fast startup, low memory)
./mvnw clean package -Pnative
./target/btc-node-dashboard-*-runner
````

## 🐳 Docker

See [DOCKER.md](DOCKER.md) for Docker build and run instructions.

### Profiles

Quarkus supports multiple profiles:

```bash
# Development profile (default in quarkus:dev)
./mvnw quarkus:dev

# Production profile (default in package)
java -jar target/quarkus-app/quarkus-run.jar

# Custom profile
java -Dquarkus.profile=staging -jar target/quarkus-app/quarkus-run.jar
```

## 🎨 Frontend Development

### Setup

```bash
cd src/main/web

# Install frontend dependencies (recommended: pnpm)
pnpm install
# or, if pnpm is not installed:
npm install -g pnpm@10.27.0
pnpm install
```

### Development Server

```bash
# Vite dev server with hot reload
pnpm dev
# or npm run dev
```

The Vite dev server will start on `http://localhost:5173` with proxy configured to forward API/WebSocket requests to `http://localhost:8080`.

**Note**: You still need to run the backend separately:

```bash
# In project root
./mvnw quarkus:dev
```

### Build Frontend Only

```bash
cd src/main/web

# Production build
pnpm build
# or npm run build

# Build output: dist/ directory
```


The Maven build automatically runs `pnpm install` (or `npm install` if pnpm is missing) and `pnpm build` to bundle the frontend into `target/classes/META-INF/resources/`.
## 🧩 Monorepo & pnpm workspace

This project uses a pnpm workspace for frontend dependency management. See `src/main/web/pnpm-workspace.yaml`.

To install all dependencies:

```bash
cd src/main/web
pnpm install
```

---

## 🚧 Troubleshooting

**Build fails**: Clear cache, rebuild

```bash
rm -rf node_modules target
./mvnw clean install
```

**Can't connect to Bitcoin Core**: Verify RPC settings, test connection

```bash
curl --user user:pass \
  --data-binary '{"method":"getblockchaininfo"}' \
  http://localhost:8332/
```

**Port 8080 in use**: Change port

```bash
export QUARKUS_HTTP_PORT=8888
./mvnw quarkus:dev
```

---

For more help, see [GitHub Issues](https://github.com/comassky/btc-node-dashboard/issues).
