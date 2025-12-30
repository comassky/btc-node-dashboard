# Bitcoin Node Dashboard ₿

Monitor your Bitcoin Core node in real-time with a modern web interface.

![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-25-orange.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.30.5-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5.26-green.svg)

## 📸 Screenshots

<img width="3024" height="5752" alt="image" src="https://github.com/user-attachments/assets/e4cb9607-5fbc-4d2b-b091-530fe5108b3e" />

## ✨ Features

- **Reactive & Non-blocking Backend (Mutiny)**: Ultra-responsive, event-driven backend using Quarkus and Mutiny for maximum scalability.
- **Parallel & Monitored RPC Execution**: All Bitcoin Core RPC calls are executed in parallel, with DEBUG logs and latency measurement for each call.
- **Advanced Caching**: Prevents redundant RPC calls by caching ongoing requests, with fine-tuned expiry and buffer (see `dashboard.cache.*`).
- **Configurable & Nested Dashboard Settings**: All dashboard features are configurable via nested properties (see `dashboard.*`), compatible with Quarkus @ConfigMapping and @WithName.
- **Live Peer & Network Statistics**: Real-time display of inbound/outbound connections, peer details, version and geographic distribution.
- **Blockchain & Mempool Monitoring**: Track block height, sync progress, node uptime, mempool size, and network health in real time.
- **Modern UI/UX**: Dark/light/gray mode, responsive design, interactive charts, smooth animations, icon support.
- **WebSocket Streaming**: Instant dashboard updates, automatic reconnection, exponential backoff, and robust error handling.
- **Mock/Test Mode**: Simulate errors, low peer count, disconnected mode for testing and demos.
- **Comprehensive Error Handling**: Clear user messages, automatic recovery and reconnection.
- **Security & Privacy**: No tracking, no analytics, all data stays on your node.
- **Performance Optimized**: GraalVM Native (<50ms startup, ~30MB RAM), tree-shaking, code splitting, gzip/brotli compression.
- **Docker & CI/CD Ready**: Easy deployment, optimized images, automated builds and tests (GitHub Actions).

## 🛠️ Tech Stack

### Backend

| Technology                   | Version           | Description                         |
| ---------------------------- | ----------------- | ----------------------------------- |
| **Java**                     | 25                | Programming language                |
| **Quarkus**                  | 3.30.5            | Supersonic Subatomic Java Framework |
| **Mutiny**                   | (via Quarkus BOM) | Reactive programming library        |
| **Jakarta WebSocket**        | -                 | Real-time communication             |
| **MicroProfile REST Client** | -                 | HTTP client for Bitcoin RPC         |
| **Jackson**                  | -                 | JSON processing                     |
| **Maven Compiler Plugin**    | 3.14.1            | Java compilation                    |
| **Maven Surefire Plugin**    | 3.5.4             | Unit testing                        |
| **Maven Failsafe Plugin**    | 3.5.4             | Integration testing                 |
| **Frontend Maven Plugin**    | 2.0.0             | Frontend build integration          |
| **Node.js**                  | v24.12.0          | Frontend build (via Maven)          |
| **npm**                      | 11.6.2            | Frontend build (via Maven)          |

### Frontend

| Technology                              | Version | Description                      |
| --------------------------------------- | ------- | -------------------------------- |
| **Vue.js**                              | 3.5.26  | Progressive JavaScript framework |
| **TypeScript**                          | 5.9.3   | Type-safe JavaScript             |
| **Vite**                                | 7.3.0   | Next-generation frontend tooling |
| **Tailwind CSS**                        | 3.4.19  | Utility-first CSS framework      |
| **Chart.js**                            | 4.5.1   | Interactive charts               |
| **@fortawesome/fontawesome-svg-core**   | 7.1.0   | Font Awesome core                |
| **@fortawesome/free-brands-svg-icons**  | 7.1.0   | Font Awesome brands icons        |
| **@fortawesome/free-regular-svg-icons** | 7.1.0   | Font Awesome regular icons       |
| **@fortawesome/free-solid-svg-icons**   | 7.1.0   | Font Awesome solid icons         |
| **@fortawesome/vue-fontawesome**        | 3.1.2   | Font Awesome Vue component       |
| **ky**                                  | 1.14.2  | HTTP client                      |
| **reconnecting-websocket**              | 4.4.0   | WebSocket reconnect              |
| **@types/node**                         | 24.10.4 | Node.js types                    |
| **@vitejs/plugin-vue**                  | 6.0.3   | Vite Vue plugin                  |
| **@vitest/coverage-v8**                 | 4.0.16  | Coverage provider                |
| **@vitest/ui**                          | 4.0.16  | Vitest UI                        |
| **@vue/test-utils**                     | 2.4.6   | Vue test utilities               |
| **autoprefixer**                        | 10.4.23 | CSS vendor prefixer              |
| **happy-dom**                           | 20.0.11 | DOM environment for tests        |
| **postcss**                             | 8.5.6   | CSS processor                    |
| **rollup-plugin-visualizer**            | 6.0.5   | Bundle visualizer                |
| **sirv-cli**                            | 3.0.1   | Static server                    |
| **vite-plugin-compression**             | 0.5.1   | Compression plugin               |
| **vite-plugin-pwa**                     | 1.2.0   | PWA plugin                       |
| **vitest**                              | 4.0.16  | Unit testing framework           |
| **vue-tsc**                             | 3.2.1   | TypeScript type checker for Vue  |
| **workbox-window**                      | 7.4.0   | Service worker helper            |

### Build & Deploy

- **Maven** 3.9.11 (Backend build and dependency management)
- **Maven Compiler Plugin** 3.14.1
- **Maven Surefire Plugin** 3.5.4
- **Maven Failsafe Plugin** 3.5.4
- **Frontend Maven Plugin** 2.0.0
- **Node.js** v24.12.0 (via Maven)
- **npm** 11.6.2 (via Maven)
- **npm ci** (Optimized frontend dependency installation with --prefer-offline)
- **Docker** (JVM & GraalVM Native images)
- **GitHub Actions** (CI/CD with automated testing and native image builds)
- **GraalVM Native Image** (AOT compilation for ultra-fast startup)

## 🧪 Automated Tests

| Suite    | Tests |
| -------- | ----- |
| Backend  | 58    |
| Frontend | 67    |

## 🏎️ Recommended Native Build (GraalVM)

For maximum performance (startup <50ms, RAM ~30MB), use the Quarkus native build:

```bash
mvn package -Pnative -DskipTests
# or with Docker
mvn package -Pnative -Dquarkus.native.container-build=true -DskipTests
```

The native Docker image is generated with `Dockerfile.native`:

```bash
docker build -f Dockerfile.native -t btc-node-dashboard-native .
docker run -p 8080:8080 btc-node-dashboard-native
```

The native binary starts instantly and uses very little memory, making it ideal for production.

## 🚀 Quick Start

### Prerequisites

- Java 25+ and Maven 3.9+
- Bitcoin Core with RPC enabled
- Node.js 24+ (optional, for frontend development)

### Development

```bash
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard

# Configure RPC (environment variables or application-local.properties)
export RPC_HOST=localhost
export RPC_PORT=8332
export RPC_USER=your_user
export RPC_PASS=your_password

# Start backend with hot reload
./mvnw quarkus:dev  # http://localhost:8080
```

## 🐳 Docker (Recommended)

```bash
# Run GraalVM Native image (fast startup, low memory)
docker run -d -p 8080:8080 \
  -e RPC_HOST=<HOST> \
  -e RPC_PORT=<PORT> \
  -e RPC_USER=<USER> \
  -e RPC_PASS=<PASSWORD> \
  -e WS_POLLING_INTERVAL=5 \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  -e DASHBOARD_CACHE_VALIDITY_BUFFER_MS=200 \
  ghcr.io/comassky/btc-node-dashboard:native
```

### Image Performance

| Metric         | JVM        | Native       |
| -------------- | ---------- | ------------ |
| **Startup**    | ~2-4s      | **~50-80ms** |
| **Memory**     | ~180-250MB | **~30-60MB** |
| **Image Size** | ~400MB     | **~120MB**   |
| **CPU (Idle)** | ~1%        | **<0.5%**    |

See [DOCKER.md](DOCKER.md) for more details.

### Available Image Tags

A short summary of the Docker image tags produced by the GitHub Actions workflows (full list in `DOCKER.md`):

- `jvm` and `jvm-<version>`: JVM-based images (examples: `jvm`, `jvm-1.4.0`).
- `latest` and semantic version tags (`<version>`, `<major>.<minor>`, `<major>`): native (GraalVM) images built from `main` and from git tags.
- `develop`: images built from the `develop` branch.

## 📊 API Endpoints

### REST API

- **GET** `/api/config` — Get dashboard configuration (e.g., minOutboundPeers)
- **GET** `/api/dashboard` — Get aggregated dashboard data (GlobalResponse)
- **GET** `/api/networkinfo` — Get node network information (NodeInfo)
- **GET** `/api/block/{hash}` — Get block information by hash (BlockInfo)
- **GET** `/api/bestblockhash` — Get the hash of the best block (plain text)
- **GET** `/api/blockchaininfo` — Get blockchain information (BlockchainInfo)

### WebSocket

- **WS** `/ws/dashboard` — Real-time dashboard updates

## 🔧 Configuration

For details on reactive programming, non-blocking guarantees, and contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

Main user-configurable environment variables:

- `QUARKUS_IO_THREADS`: Number of IO threads for the backend (recommended: 2 × number of CPU cores). Set this environment variable to control backend concurrency. Example: `QUARKUS_IO_THREADS=16 java -jar ...`. Defaults to 8 if not set.

- `RPC_HOST`, `RPC_PORT`, `RPC_USER`, `RPC_PASS`: Bitcoin node connection
- `WS_POLLING_INTERVAL`: dashboard refresh interval (seconds)
- `MIN_OUTBOUND_PEERS`: minimum outbound peers
- `DASHBOARD_CACHE_VALIDITY_BUFFER_MS`: cache validity buffer (ms)
- `LOG_LEVEL`: log level (INFO, DEBUG, ...)

For the full list and default values, see [BUILD.md](BUILD.md).

Example in `application-local.properties`:

```properties
bitcoin.rpc.host=localhost
bitcoin.rpc.port=8332
bitcoin.rpc.user=your_user
bitcoin.rpc.password=your_password
dashboard.polling.interval.seconds=5
dashboard.cache.validity.buffer.ms=200
```

# 🚦 Build & Continuous Integration

- The Maven build (`mvn package`) runs all tests and produces the final artifact (backend + frontend bundled).
- Docker and native workflows use this artifact: no build or tests are repeated in those steps.
- CI (GitHub Actions) blocks any deployment if a test fails.
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
