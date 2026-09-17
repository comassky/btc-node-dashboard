# Bitcoin Node Dashboard ₿

Monitor your Bitcoin Core node in real-time with a modern web interface.

![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-25-orange.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.32.0.CR1-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5.29-green.svg)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9.3-blue.svg)

## 📸 Screenshots

<img width="3024" height="6346" alt="image" src="https://github.com/user-attachments/assets/79395032-46d8-488d-86e8-8c1a696ef028" />


## ✨ Features

- **Reactive & Non-blocking Backend (Mutiny)**: Ultra-responsive, event-driven backend using Quarkus and Mutiny for maximum scalability.
- **Parallel & Monitored RPC Execution**: All Bitcoin Core RPC calls are executed in parallel, with DEBUG logs and latency measurement for each call.
- **Advanced Caching**: Prevents redundant RPC calls by caching ongoing requests, with fine-tuned expiry and buffer (see `dashboard.cache.*`).
- **Configurable & Nested Dashboard Settings**: All dashboard features are configurable via nested properties (see `dashboard.*`), compatible with Quarkus @ConfigMapping and @WithName.
- **Live Peer & Network Statistics**: Real-time display of inbound/outbound connections, peer details, version and geographic distribution.
- **Blockchain & Mempool Monitoring**: Track block height, sync progress, node uptime, mempool size, and network health in real time.
- **Modern UI/UX**: Dark/light/gray mode, responsive design, interactive charts, glassmorphism effects, optimized icons (official Tor logo, network-specific icons).
- **WebSocket Streaming**: Instant dashboard updates, automatic reconnection, exponential backoff, session cleanup.
- **Mock/Test Mode**: Simulate errors, low peer count, disconnected mode for testing and demos.
- **Comprehensive Error Handling**: Clear user messages, automatic recovery and reconnection.
- **Security & Privacy**: No tracking, no analytics, all data stays on your node.
- **Performance Optimized**: Native compilation, Chart.js tree-shaking, optimized component rendering, gzip/brotli compression, reusable composables.
- **Docker & CI/CD Ready**: Non-root Distroless images, automated development builds and tests, manually triggered releases (GitHub Actions).
- **Composable Architecture**: Reusable Vue composables (`useSortableTable`, `usePeerAnalytics`) for clean, maintainable code.

## 🛠️ Tech Stack

### Backend

| Technology                   | Version           | Description                         |
| ---------------------------- | ----------------- | ----------------------------------- |
| **Java**                     | 25                | Programming language                |
| **Quarkus**                  | 3.32.0.CR1         | Reactive Java framework             |
| **Mutiny**                   | (via Quarkus BOM) | Reactive programming library        |
| **Jakarta WebSocket**        | -                 | Real-time communication             |
| **MicroProfile REST Client** | -                 | HTTP client for Bitcoin RPC         |
| **Jackson**                  | -                 | JSON processing                     |
| **Caffeine Cache**           | -                 | High-performance async cache        |
| **SmallRye OpenAPI**         | -                 | OpenAPI/Swagger documentation       |

### Frontend

#### Runtime Dependencies

| Technology           | Version | Description                      |
| -------------------- | ------- | -------------------------------- |
| **Vue.js**           | 3.5.29  | Progressive JavaScript framework |
| **VueUse**           | 14.2.1  | Composition utilities            |
| **Pinia**            | 3.0.4   | State management                 |
| **Chart.js**         | 4.5.1   | Interactive charts (tree-shaken) |
| **unplugin-icons**   | 23.0.1  | Build-time Iconify integration    |
| **Simple Icons**     | 1.2.71  | Brand logos (Tor, etc.)          |
| **Floating UI**      | 1.1.10  | Tooltip positioning              |
| **date-fns**         | 4.1.0   | Date utilities                   |
| **filesize**         | 11.0.13 | File size formatting             |

#### Build Tools

| Technology           | Version | Description                      |
| -------------------- | ------- | -------------------------------- |
| **TypeScript**       | 5.9.3   | Type-safe JavaScript             |
| **Vite**             | 7.3.1   | Build tool & dev server          |
| **Tailwind CSS**     | 4.2.1   | Utility-first CSS framework      |
| **Vitest**           | 4.0.18  | Unit testing framework           |
| **Prettier**         | 3.8.1   | Code formatter                   |

### Build & Deploy

- **Maven** 3.9.11+ locally; 3.9.16 in the Docker builder
- **Maven Compiler Plugin** 3.15.0
- **Maven Surefire Plugin** 3.5.5
- **Maven Failsafe Plugin** 3.5.5 (integration tests currently skipped)
- **Quinoa** 2.9.0 (frontend integration with Quarkus)
- **Node.js** v24.21.0 (installed by Quinoa)
- **npm** bundled with Node.js (dependency installation, tests and frontend builds)
- **Docker** (Distroless Debian 13 JVM and native runtimes)
- **GitHub Actions** (development builds, release creation and tag publication)
- **Mandrel** (native compilation in Docker, no local GraalVM required)

[pom.xml](pom.xml) and [package.json](src/main/web/package.json) are the version sources of truth. [renovate.json](renovate.json) maintains Java, frontend, Docker and GitHub Actions dependencies in separate groups, with no automerge.

## 🧪 Automated Tests

| Suite    | Tests |
| -------- | ----- |
| Backend  | 79    |
| Frontend | 83    |

**Total: 162 unit tests** in the last verified build. Run `mvn verify` to execute both suites. Failsafe integration tests are currently skipped; see [TESTING.md](TESTING.md).

## 🏎️ Recommended Native Build (GraalVM)

Build the native executable with JDK 25, Maven and Docker. Maven runs the backend and frontend unit tests, bundles the frontend, and invokes Mandrel in a container:

```bash
mvn -B --no-transfer-progress clean verify -Dnative \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.native-image-xmx=6g
```

Package the executable with [Dockerfile.native](Dockerfile.native):

```bash
docker build -f Dockerfile.native -t btc-node-dashboard-native .
```

Supply the RPC configuration when running the image, as in the Docker example below. Startup time, memory usage and image size depend on the build and workload; the new native runtime has not yet been benchmarked. See [BUILD.md](BUILD.md) for memory and platform requirements.

## 🚀 Quick Start

### Prerequisites

- JDK 25 and Maven 3.9.11+ for local builds
- Bitcoin Core with RPC enabled
- Node.js v24.21.0 and bundled npm for standalone frontend development; Quinoa installs them for full builds
- Docker for native compilation or container image builds


### Development

```bash
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard

# Configure and start the backend in this terminal
export RPC_HOST=localhost
export RPC_PORT=8332
export RPC_USER=your_user
export RPC_PASS=your_password
mvn quarkus:dev # http://localhost:8080
```

In another terminal, from the repository root:

```bash
cd src/main/web
npm ci
npm run dev # http://localhost:5173
```

Vite proxies API and WebSocket requests to the backend on port 8080. To build a single deployable JVM application instead, run `mvn -B --no-transfer-progress verify` at the repository root.

## 🐳 Docker (Recommended)

```bash
# Run native GraalVM image (fast startup, low memory)
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


Replace `bitcoin-host` and the example credentials with values reachable from the container; `localhost` would refer to the container itself.

Docker images use non-root Distroless runtimes and the following GitHub Actions workflows:
- [docker.yml](.github/workflows/docker.yml): PR checks and development images (`dev`, `main`, `develop`).
- [release.yml](.github/workflows/release.yml): manually create, verify and publish a release (`<version>`, `latest`).
- [publish-image.yml](.github/workflows/publish-image.yml): publish an existing tag.

The JVM and native runtimes use UID/GID `65532:65532` and contain no shell. Configure Java with `JAVA_TOOL_OPTIONS`, not `JAVA_OPTS`. See [DOCKER.md](DOCKER.md) for builds, runtime configuration and release setup.

### Available Image Tags

A short summary of the Docker image tags produced by the GitHub Actions workflows (full list in [DOCKER.md](DOCKER.md)):

- `latest` and `<version>`: native images published by a successful stable release workflow.
- Prerelease tags: published by the existing-tag workflow without updating `latest`.
- `dev` and `main`: images built from the `main` branch.
- `develop`: images built from the `develop` branch.

No `<major>` or `<major>.<minor>` aliases are generated. Tag pushes alone do not publish images. New releases are started manually with a version `X.Y.Z`; the release workflow then prepares the next development `-SNAPSHOT`.

## 📊 API Endpoints

### REST API

The backend exposes RESTful endpoints to retrieve Bitcoin node data.

📖 **OpenAPI Specification**: Available at project root as `openapi.json` and `openapi.yaml` (versioned)

➡️ [OpenAPI YAML documentation](./openapi.yaml)
➡️ [OpenAPI JSON documentation](./openapi.json)

The API specification follows the OpenAPI 3.0 standard and documents all available endpoints, request/response schemas, and parameters.

The schema version follows the Maven application version. Release workflows upload the generated schemas as artifacts, and the release-creation workflow publishes Redoc to GitHub Pages after the release succeeds. See [OPENAPI.md](OPENAPI.md) for setup and runtime schema access.

#### Available Endpoints

- **GET** `/api/config` — Get dashboard configuration (e.g., minOutboundPeers)
- **GET** `/api/dashboard` — Get aggregated dashboard data (GlobalResponse)
- **GET** `/api/getnetworkinfo` — Get node network information
- **GET** `/api/getblock/{hash}` — Get block information by hash
- **GET** `/api/getbestblockhash` — Get the hash of the best block (plain text)
- **GET** `/api/getBlockchainInfo` — Get blockchain information
- **GET** `/api/getmempoolinfo` — Get mempool information
- **GET** `/api/blockchaininfo` — Get blockchain information (BlockchainInfo)
- **GET** `/api/cache/stats` — Get cache performance statistics (reactive)
- **GET** `/api/getmempoolinfo` — Get mempool information

### WebSocket

- **WS** `/ws/dashboard` — Real-time dashboard updates

## 🔧 Configuration


For details on reactive programming, non-blocking guarantees and contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

### Environment Variables

- `QUARKUS_IO_THREADS`: Number of IO threads for the backend (recommended: 2 × number of CPU cores). Set this environment variable to control backend concurrency. Example: `QUARKUS_IO_THREADS=16 java -jar ...`. Defaults to 8 if not set.

- `RPC_HOST`, `RPC_PORT`, `RPC_USER`, `RPC_PASS`: required Bitcoin node connection settings. Direct Quarkus overrides (`BITCOIN_RPC_HOST`, `BITCOIN_RPC_PORT`, `BITCOIN_RPC_USER`, `BITCOIN_RPC_PASSWORD`) are also supported.
- `WS_POLLING_INTERVAL`: dashboard refresh interval (seconds)
- `MIN_OUTBOUND_PEERS`: minimum outbound peers
- `DASHBOARD_CACHE_VALIDITY_BUFFER_MS`: cache validity buffer (ms)
- `LOG_LEVEL`: log level (INFO, DEBUG, ...)


For the complete list and default values, see [DOCKER.md](DOCKER.md).

---

## 🧩 Frontend Dependencies

The project uses npm for frontend dependency management. Quinoa runs `npm ci`, tests and builds during Maven packaging using [package-lock.json](src/main/web/package-lock.json).

To install all dependencies:

```bash
cd src/main/web
npm ci
```

---

For a source build, an alternative is `src/main/resources/application-local.properties` with the `local` profile enabled (`mvn quarkus:dev -Dquarkus.profile=local`):

```properties
bitcoin.rpc.host=localhost
bitcoin.rpc.port=8332
bitcoin.rpc.user=your_user
bitcoin.rpc.password=your_password
dashboard.polling.interval.seconds=5
dashboard.cache.validity.buffer.ms=200
```

# 🚦 Build & Continuous Integration

- `mvn verify` runs backend unit tests and, during `prepare-package`, frontend unit tests before bundling the frontend. `mvn test` alone only runs backend tests.
- Native workflows compile with Mandrel in Docker, then package the resulting executable using the native Dockerfile. The packaging step does not re-run tests.
- The JVM Dockerfile runs `mvn verify` in its Maven builder stage.
- Build-relevant pull requests are verified without publishing; documentation-only changes skip compilation. Test failures prevent image publication.
- GitHub Pages must use the **GitHub Actions** source for release documentation. Release commits require suitable branch permissions; see [DOCKER.md](DOCKER.md).
- See [BUILD.md](BUILD.md) and [TESTING.md](TESTING.md) for more details.

## 🎨 Frontend Development

See [BUILD.md](BUILD.md) for detailed instructions on setting up and running the frontend development server, as well as building the frontend.

## 🤝 Contributing

Contributions are welcome! Fork, branch, commit, push, and open a pull request.

---

### Cryptocurrency Donations

| Currency                 | Address                                        |
| ------------------------ | ---------------------------------------------- |
| **Bitcoin (BTC)**        | `bc1qa7kcf6r9xemdmcs7wufufztfcl7rzravx9naz3`   |
| **Ethereum / BSC (ETH)** | `0x0f26B8Bdc028F6bd0F79FF4959306065C36d5EAa`   |
| **Solana (SOL)**         | `FH7HPraEeSva72g5Cv2WTbP65tPxQiZc1GNCSk2ML7eN` |

#### Lightning Network ⚡

<img width="320" height="312" alt="Lightning Network QR Code" src="https://github.com/user-attachments/assets/6cfd0bfa-fb41-48eb-b429-3420e5cf63de" />

**Every satoshi helps keep this project maintained and growing!**

_Your support enables continuous improvements, new features, and better documentation._

## 🙏 Acknowledgments

Built with [Quarkus](https://quarkus.io/), [Vue.js](https://vuejs.org/), [Tailwind CSS](https://tailwindcss.com/), [Chart.js](https://www.chartjs.org/), and [Bitcoin Core](https://bitcoincore.org/).

---

**Built with ❤️ for the Bitcoin community**
