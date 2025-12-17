# 🏗️ Single Build (CI/CD)

In CI, the Maven build (`./mvnw clean package`) runs all tests and produces the artifact used for Docker and native images. No build or tests are repeated in Docker/native steps: speed and consistency guaranteed.

- To build and test locally:
  ```bash
  ./mvnw clean package
  ```
- To build native (tests already passed):
  ```bash
  ./mvnw clean package -Pnative -DskipTests
  ```
git clone https://github.com/comassky/btc-node-dashboard.git
# Build and Run Guide

Complete guide for building and deploying the Bitcoin Node Dashboard.

## Prerequisites

**Required**: Java 21+, Maven 3.9+, Bitcoin Core with RPC enabled  
**Optional**: Node.js v24.12.0 (for frontend development), npm 11.6.2, Docker

## 🚀 Quick Start

```bash
# Clone
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard

```bash

# 🏗️ Build & Run Guide

This guide explains how to build and deploy the Bitcoin Node Dashboard.

## Prerequisites

**Required:** Java 21+, Maven 3.9+, Bitcoin Core with RPC enabled  
**Optional:** Node.js 24+ (v24.12.0 recommandé), npm 11.6.2, Docker

## 🚀 Quick Start

```bash
# Clone repository
```
cd btc-node-dashboard

# Configure RPC
export RPC_HOST=localhost
export RPC_PORT=8332
export RPC_USER=your_username
export RPC_PASS=your_password

# Start backend with hot reload
./mvnw quarkus:dev  # http://localhost:8080
```

## 🔨 Build Options

```bash
# Standard build
./mvnw clean package

# Skip tests
./mvnw clean package -DskipTests

# GraalVM Native (fast startup, low memory)
./mvnw clean package -Pnative
./target/btc-node-dashboard-*-runner
```

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

# Install dependencies
npm install
```

### Development Server

```bash
# Vite dev server with hot reload
npm run dev
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
npm run build

# Build output: dist/ directory
```

The Maven build automatically runs `npm install` and `npm run build` to bundle the frontend into `target/classes/META-INF/resources/`.

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