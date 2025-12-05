# Build and Run Guide

This guide covers all methods to build and run the Bitcoin Node Dashboard, from local development to production Docker deployments.

## 📋 Prerequisites

### Required
- **Java Development Kit (JDK) 21** or higher
- **Apache Maven 3.9+** (or use the included wrapper `./mvnw`)
- **Bitcoin Core** node with RPC access enabled

### Optional (for frontend development)
- **Node.js 24+** and **npm 11+**

### Docker Deployment Only
- **Docker** or **Docker Compose**

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard
```

### 2. Configure Bitcoin Core RPC

#### Option A: Application Properties File

Create `src/main/resources/application-local.properties`:

```properties
# Bitcoin RPC Configuration
bitcoin.rpc.scheme=http
bitcoin.rpc.host=localhost
bitcoin.rpc.port=8332
bitcoin.rpc.user=your_rpc_username
bitcoin.rpc.password=your_rpc_password

# WebSocket polling interval (seconds)
dashboard.polling.interval.seconds=5
```

#### Option B: Environment Variables

```bash
export BITCOIN_RPC_HOST=localhost
export BITCOIN_RPC_PORT=8332
export BITCOIN_RPC_USER=your_rpc_username
export BITCOIN_RPC_PASSWORD=your_rpc_password
export BITCOIN_RPC_SCHEME=http
export WS_POLLING_INTERVAL=5
export LOG_LEVEL=INFO  # Default: INFO. Use DEBUG to see detailed configuration at startup
```

### 3. Build and Run

#### Development Mode (Recommended for Development)

```bash
# Backend + Frontend with hot reload
./mvnw quarkus:dev
```

Features:
- ✅ Automatic restart on code changes
- ✅ Live reload for frontend
- ✅ Dev UI available at http://localhost:8080/q/dev
- ✅ Continuous testing mode
- ✅ Debug port 5005 enabled

Access: `http://localhost:8080`

#### Production Build

```bash
# Build the application
./mvnw clean package

# Run the JAR
java -jar target/quarkus-app/quarkus-run.jar
```

Access: `http://localhost:8080`

## 🔨 Build Options

### Standard JVM Build

```bash
# Full build with tests
./mvnw clean package

# Skip tests (faster)
./mvnw clean package -DskipTests

# Build without running frontend build
./mvnw clean package -DskipTests -Dfrontend.skip=true
```

Output: `target/quarkus-app/quarkus-run.jar`

### GraalVM Native Build

**Ultra-fast startup (~50ms) and minimal memory (~30MB)**

#### Prerequisites

- **GraalVM JDK 21** or higher
- **Native Image** tool installed:
  ```bash
  gu install native-image
  ```
- Platform-specific tools:
  - **macOS**: Xcode Command Line Tools
  - **Linux**: gcc, glibc-devel, zlib-devel
  - **Windows**: Visual Studio with C++ tools

#### Build Native Executable

```bash
# Build native executable
./mvnw clean package -Pnative

# Skip tests (faster)
./mvnw clean package -Pnative -DskipTests
```

Output: `target/btc-node-dashboard-1.0.0-SNAPSHOT-runner`

#### Run Native Executable

```bash
# macOS/Linux
./target/btc-node-dashboard-*-runner

# Windows
target\btc-node-dashboard-*-runner.exe
```

#### Performance Comparison

| Metric | JVM | Native |
|--------|-----|--------|
| **Startup Time** | ~3-5s | **~50ms** |
| **Memory (RSS)** | ~200-300MB | **~30-50MB** |
| **Build Time** | ~30s | **~3-5min** |
| **Image Size** | ~50MB | **~80MB** |

## 🐳 Docker Deployment

### Pull Pre-built Images

Browse all available images and tags:
- **GitHub Packages**: [https://github.com/comassky/btc-node-dashboard/pkgs/container/btc-node-dashboard](https://github.com/comassky/btc-node-dashboard/pkgs/container/btc-node-dashboard)

**Available Tags:**
- `main` - Latest JVM build from main branch
- `native` - Latest GraalVM native build (⚡ recommended)
- `native-main` - GraalVM native build from main branch
- `native-<sha>` - GraalVM native build from specific commit
- `X.Y.Z-native` - GraalVM native build for version (e.g., `1.3.0-native`)

### JVM Docker Image

```bash
docker run -d \
  -p 8080:8080 \
  --name btc-dashboard \
  -e BITCOIN_RPC_HOST=<HOST> \
  -e BITCOIN_RPC_PORT=<PORT> \
  -e BITCOIN_RPC_USER=<RPC_USER> \
  -e BITCOIN_RPC_PASSWORD=<RPC_PASSWORD> \
  -e BITCOIN_RPC_SCHEME=http \
  -e WS_POLLING_INTERVAL=5 \
  -e LOG_LEVEL=INFO \
  ghcr.io/comassky/btc-node-dashboard:main
```

### GraalVM Native Docker Image (⚡ Recommended)

```bash
docker run -d \
  -p 8080:8080 \
  --name btc-dashboard-native \
  -e BITCOIN_RPC_HOST=<HOST> \
  -e BITCOIN_RPC_PORT=<PORT> \
  -e BITCOIN_RPC_USER=<RPC_USER> \
  -e BITCOIN_RPC_PASSWORD=<RPC_PASSWORD> \
  -e BITCOIN_RPC_SCHEME=http \
  -e WS_POLLING_INTERVAL=5 \
  -e LOG_LEVEL=INFO \
  ghcr.io/comassky/btc-node-dashboard:native
```

**Image Performance:**

| Metric | JVM Image | Native Image |
|--------|-----------|--------------|
| **Startup Time** | ~3-5s | **~50ms** |
| **Memory (RSS)** | ~200-300MB | **~30-50MB** |
| **Image Size** | ~500MB | **~150MB** |
| **CPU (Idle)** | ~1-2% | **<0.5%** |

### Build Docker Images Locally

#### JVM Image

```bash
docker build -f Dockerfile -t btc-node-dashboard:jvm .
```

#### Native Image

```bash
# Build native executable first
./mvnw clean package -Pnative -DskipTests

# Build Docker image
docker build -f Dockerfile.native -t btc-node-dashboard:native .
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  btc-dashboard:
    image: ghcr.io/comassky/btc-node-dashboard:native
    container_name: btc-dashboard
    ports:
      - "8080:8080"
    environment:
      BITCOIN_RPC_SCHEME: http
      BITCOIN_RPC_HOST: bitcoin-core
      BITCOIN_RPC_PORT: 8332
      BITCOIN_RPC_USER: your_username
      BITCOIN_RPC_PASSWORD: your_password
      WS_POLLING_INTERVAL: 5
      LOG_LEVEL: WARN  # Default: INFO. Use DEBUG to see detailed startup configuration
    depends_on:
      - bitcoin-core
    restart: unless-stopped

  bitcoin-core:
    image: btcsuite/bitcoind:latest
    container_name: bitcoin-core
    ports:
      - "8332:8332"
      - "8333:8333"
    volumes:
      - bitcoin-data:/bitcoin/.bitcoin
    command:
      - "-server"
      - "-rpcallowip=0.0.0.0/0"
      - "-rpcbind=0.0.0.0"
      - "-rpcuser=your_username"
      - "-rpcpassword=your_password"
    restart: unless-stopped

volumes:
  bitcoin-data:
```

Run with:
```bash
docker-compose up -d
```

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
# Development server
npm run dev

# Production build
npm run build

# Preview production build
npm run preview

# Type checking
npm run type-check

# Linting
npm run lint

# Run tests
npm test

# Coverage report
npm run coverage

# Test UI mode
npm run test:ui
```

## 🔍 Verification

### Health Check

```bash
# Check application health
curl http://localhost:8080/q/health

# Expected response:
# {
#   "status": "UP",
#   "checks": [...]
# }
```

### Metrics

```bash
# Prometheus metrics
curl http://localhost:8080/q/metrics
```

### Dev UI (Development Mode Only)

Access the Quarkus Dev UI at:
```
http://localhost:8080/q/dev
```

Features:
- Configuration editor
- Health checks
- Metrics visualization
- Arc CDI inspector
- Continuous testing

## 🚧 Troubleshooting

### Build Issues

#### Frontend build fails
```bash
# Clear npm cache and reinstall
cd src/main/web
rm -rf node_modules package-lock.json
npm install
```

#### Maven build fails
```bash
# Clean and rebuild
./mvnw clean install -U

# Skip tests
./mvnw clean package -DskipTests
```

#### Native build fails on macOS
```bash
# Install Xcode Command Line Tools
xcode-select --install

# Verify GraalVM installation
gu list
gu install native-image
```

### Runtime Issues

#### Cannot connect to Bitcoin Core
- Verify Bitcoin Core is running
- Check RPC credentials in `bitcoin.conf`:
  ```
  rpcuser=your_username
  rpcpassword=your_password
  rpcallowip=127.0.0.1
  server=1
  ```
- Test RPC connection:
  ```bash
  curl --user your_username:your_password \
       --data-binary '{"jsonrpc":"1.0","id":"test","method":"getblockchaininfo","params":[]}' \
       -H 'content-type: text/plain;' \
       http://localhost:8332/
  ```

#### WebSocket disconnects frequently
- Increase polling interval: `WS_POLLING_INTERVAL=10`
- Check network stability
- Review browser console for errors

#### Port 8080 already in use
```bash
# Change port via environment variable
export QUARKUS_HTTP_PORT=8888
./mvnw quarkus:dev

# Or in application.properties
quarkus.http.port=8888
```

### Performance Issues

#### High memory usage (JVM)
```bash
# Limit JVM heap
java -Xmx256m -jar target/quarkus-app/quarkus-run.jar
```

#### Slow startup (JVM)
Consider using GraalVM native image for ~50ms startup time.

## 📚 Additional Resources

- **Quarkus Documentation**: https://quarkus.io/guides/
- **Vue.js Guide**: https://vuejs.org/guide/
- **GraalVM Native Image**: https://www.graalvm.org/native-image/
- **Bitcoin Core RPC**: https://developer.bitcoin.org/reference/rpc/
- **Testing Guide**: [TESTING.md](TESTING.md)

## 🆘 Getting Help

If you encounter issues:

1. Check existing [GitHub Issues](https://github.com/comassky/btc-node-dashboard/issues)
2. Review [TESTING.md](TESTING.md) for test-related problems
3. Open a new issue with:
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details (OS, Java version, Docker version)
   - Relevant logs

---

**Ready to deploy?** See the main [README.md](README.md) for feature overview and architecture details.
