# Build and Run Guide

Complete guide for building and deploying the Bitcoin Node Dashboard.

## Prerequisites

**Required**: Java 21+, Maven 3.9+, Bitcoin Core with RPC enabled  
**Optional**: Node.js 24+ (for frontend dev), Docker

## 🚀 Quick Start

```bash
# Clone
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard

# Configure RPC
export RPC_HOST=localhost
export RPC_PORT=8332
export RPC_USER=your_username
export RPC_PASS=your_password

# Run
./mvnw quarkus:dev  # → http://localhost:8080
```

## 🔨 Build Options

```bash
# Standard build
./mvnw clean package

# Skip tests
./mvnw clean package -DskipTests

# GraalVM Native (50ms startup, 30MB memory)
./mvnw clean package -Pnative
./target/btc-node-dashboard-*-runner
```

## 🐳 Docker

**Pre-built images**: [GitHub Packages](https://github.com/comassky/btc-node-dashboard/pkgs/container/btc-node-dashboard)

```bash
# Native (recommended - 50ms startup, 30MB memory)
docker run -d -p 8080:8080 \
  -e RPC_HOST=<HOST> \
  -e RPC_PORT=<PORT> \
  -e RPC_USER=<USER> \
  -e RPC_PASS=<PASSWORD> \
  -e WS_POLLING_INTERVAL=5 \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  ghcr.io/comassky/btc-node-dashboard:native

# JVM
docker run -d -p 8080:8080 \
  -e RPC_HOST=<HOST> \
  -e RPC_PORT=<PORT> \
  -e RPC_USER=<USER> \
  -e RPC_PASS=<PASSWORD> \
  -e WS_POLLING_INTERVAL=5 \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  ghcr.io/comassky/btc-node-dashboard:main
```

**Image Performance**:

| Metric        | JVM         | Native      |
|-------------- |------------|------------ |
| **Startup**   | ~2-4s      | **~80ms**   |
| **Memory**    | ~180-250MB | **~35-60MB**|
| **Image Size**| ~400MB     | **~120MB**  |
| **CPU (Idle)**| ~1%        | **<0.5%**   |

**Docker Compose**: See [compose.yml](compose.yml) for full setup with Bitcoin Core.

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `BITCOIN_RPC_HOST` | `127.0.0.1` | Bitcoin node hostname or IP address |
| `BITCOIN_RPC_PORT` | `8332` | Bitcoin RPC port |
| `BITCOIN_RPC_USER` | - | RPC username for authentication |
| `BITCOIN_RPC_PASSWORD` | - | RPC password for authentication |
| `BITCOIN_RPC_SCHEME` | `http` | RPC protocol (`http` or `https`) |
| `WS_POLLING_INTERVAL` | `5` | Dashboard refresh interval in seconds |
| `LOG_LEVEL` | `INFO` | Application log level (`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`). Use `DEBUG` to see detailed startup configuration |

### Application Properties

Alternative to environment variables, create `src/main/resources/application-local.properties`:

```properties
bitcoin.rpc.scheme=http
bitcoin.rpc.host=localhost
bitcoin.rpc.port=8332
bitcoin.rpc.user=your_rpc_username
bitcoin.rpc.password=your_rpc_password
dashboard.polling.interval.seconds=5
```

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

### Frontend Commands

```bash
# Install dependencies (production)
npm ci --prefer-offline --no-audit

# Install dependencies (development)
npm install

# Development server
npm run dev

# Development server with mock mode
VITE_MOCK_MODE=true npm run dev

# Production build
npm run build

## 🔧 Configuration

**Environment Variables**:
- `RPC_HOST` or `BITCOIN_RPC_HOST` (default: `127.0.0.1`)
- `RPC_PORT` or `BITCOIN_RPC_PORT` (default: `8332`)
- `RPC_USER` or `BITCOIN_RPC_USER` (required)
- `RPC_PASS` or `BITCOIN_RPC_PASSWORD` (required)
- `BITCOIN_RPC_SCHEME` (default: `http`)
- `WS_POLLING_INTERVAL` (default: `5` seconds, interval for dashboard updates)
- `MIN_OUTBOUND_PEERS` (default: `8`, minimum peers before warning)
- `LOG_LEVEL` (default: `INFO`, options: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`)

**Or use** `application-local.properties` in `src/main/resources/`.

## 🎨 Frontend Development

```bash
cd src/main/web

# Install
## 🔍 Verification

```bash
# Health
curl http://localhost:8080/q/health

# Metrics
curl http://localhost:8080/q/metrics

# Dev UI (quarkus:dev only)
http://localhost:8080/q/dev
```

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