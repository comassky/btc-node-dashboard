# Bitcoin Node Dashboard ₿

Monitor your Bitcoin Core node in real-time with a modern web interface.

![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.30.3-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5.25-green.svg)

## 📸 Screenshots

<img width="3024" height="5752" alt="image" src="https://github.com/user-attachments/assets/e4cb9607-5fbc-4d2b-b091-530fe5108b3e" />


## ✨ Features

- **Reactive Architecture (Mutiny)**: Event-driven, non-blocking backend for high responsiveness and efficient resource utilization.
- **Parallel & Monitored RPC Execution**: All Bitcoin Core RPC calls are executed in parallel, with DEBUG logs and latency measurement for each call.
- **Optimized Caching**: Prevents redundant RPC calls by caching ongoing requests, improving performance and reducing load on the Bitcoin node. Configurable via `dashboard.cache.validity-buffer-ms`.
- **Live Peer Statistics**: Real-time display of inbound/outbound connections, peer details, version and geographic distribution.
- **Blockchain Status**: Track block height, sync progress, node uptime, and network health.
- **Modern UI/UX**: Dark/light mode, responsive design, interactive charts, smooth animations, icon support.
- **WebSocket Streaming**: Instant dashboard updates, automatic reconnection, exponential backoff.
- **Mock/Test Mode**: Simulate errors, low peer count, disconnected mode for testing and demos.
- **Comprehensive Error Handling**: Clear user messages, automatic recovery and reconnection.
- **Performance Optimized**: GraalVM Native (<50ms startup, ~30MB RAM), tree-shaking, code splitting, gzip compression.
- **Docker & CI/CD Ready**: Easy deployment, optimized images, automated builds and tests.

## 🛠️ Tech Stack

### Backend
| Technology | Version | Description |
|------------|---------|-------------|
| **Java** | 21 | Programming language |
| **Quarkus** | 3.30.3 | Supersonic Subatomic Java Framework |
| **Mutiny** | 2.x | Reactive programming library |
| **Jakarta WebSocket** | - | Real-time communication |
| **MicroProfile REST Client** | - | HTTP client for Bitcoin RPC |
| **Jackson** | - | JSON processing |
| **Maven Compiler Plugin** | 3.14.1 | Java compilation |
| **Maven Surefire Plugin** | 3.5.4 | Unit testing |
| **Frontend Maven Plugin** | 1.15.4 | Frontend build integration |
| **Node.js** | v24.11.1 | Frontend build (via Maven) |
| **npm** | 11.6.2 | Frontend build (via Maven) |

### Frontend
| Technology | Version | Description |
|------------|---------|-------------|
| **Vue.js** | 3.5.25 | Progressive JavaScript framework |
| **TypeScript** | 5.9.3 | Type-safe JavaScript |
| **Vite** | 7.3.0 | Next-generation frontend tooling |
| **Tailwind CSS** | 3.4.19 | Utility-first CSS framework |
| **Chart.js** | 4.5.1 | Interactive charts |
| **Font Awesome** | 7.1.0 | Icon library |
| **@fortawesome/vue-fontawesome** | 3.1.2 | Font Awesome Vue component |
| **@fortawesome/fontawesome-svg-core** | 7.1.0 | Font Awesome core |
| **@fortawesome/free-brands-svg-icons** | 7.1.0 | Font Awesome brands icons |
| **@fortawesome/free-regular-svg-icons** | 7.1.0 | Font Awesome regular icons |
| **@fortawesome/free-solid-svg-icons** | 7.1.0 | Font Awesome solid icons |
| **@vue/test-utils** | 2.4.6 | Vue test utilities |
| **vitest** | 4.0.15 | Unit testing framework |
| **@vitest/ui** | 4.0.15 | Vitest UI |
| **@vitest/coverage-v8** | 4.0.15 | Coverage provider |
| **happy-dom** | 20.0.11 | DOM environment for tests |
| **workbox-window** | 7.4.0 | Service worker helper |
| **vite-plugin-pwa** | 1.2.0 | PWA plugin |
| **vite-plugin-compression** | 0.5.1 | Compression plugin |
| **rollup-plugin-visualizer** | 6.0.5 | Bundle visualizer |
| **sirv-cli** | 3.0.1 | Static server |
| **autoprefixer** | 10.4.22 | CSS vendor prefixer |
| **postcss** | 8.5.6 | CSS processor |
| **vue-tsc** | 3.1.8 | TypeScript type checker for Vue |
| **@types/node** | 24.10.3 | Node.js types |
| **reconnecting-websocket** | 4.4.0 | WebSocket reconnect |

### Build & Deploy
- **Maven** 3.9.11 (Backend build and dependency management)
- **Maven Compiler Plugin** 3.14.1
- **Maven Surefire Plugin** 3.5.4
- **Frontend Maven Plugin** 1.15.4
- **Node.js** v24.11.1 (via Maven)
- **npm** 11.6.2 (via Maven)
- **npm ci** (Optimized frontend dependency installation with --prefer-offline)
- **Docker** (JVM & GraalVM Native images)
- **GitHub Actions** (CI/CD with automated testing and native image builds)
- **GraalVM Native Image** (AOT compilation for ultra-fast startup)


## 🚀 Quick Start

### Prerequisites
- Java 21+ and Maven 3.9+
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

| Metric        | JVM         | Native      |
|-------------- |------------|------------ |
| **Startup**   | ~2-4s      | **~50-80ms**   |
| **Memory**    | ~180-250MB | **~30-60MB**|
| **Image Size**| ~400MB     | **~120MB**  |
| **CPU (Idle)**| ~1%        | **<0.5%**   |


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
dashboard.cache.validity-buffer-ms=200
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

| Currency | Address |
|----------|----------|
| **Bitcoin (BTC)** | `bc1qa7kcf6r9xemdmcs7wufufztfcl7rzravx9naz3` |
| **Ethereum / BSC (ETH)** | `0x0f26B8Bdc028F6bd0F79FF4959306065C36d5EAa` |
| **Solana (SOL)** | `FH7HPraEeSva72g5Cv2WTbP65tPxQiZc1GNCSk2ML7eN` |

#### Lightning Network ⚡

<img width="320" height="312" alt="Lightning Network QR Code" src="https://github.com/user-attachments/assets/6cfd0bfa-fb41-48eb-b429-3420e5cf63de" />

**Every satoshi helps keep this project maintained and growing!**

*Your support enables continuous improvements, new features, and better documentation.*

## 🙏 Acknowledgments

Built with [Quarkus](https://quarkus.io/), [Vue.js](https://vuejs.org/), [Tailwind CSS](https://tailwindcss.com/), [Chart.js](https://www.chartjs.org/), and [Bitcoin Core](https://bitcoincore.org/).

---

**Built with ❤️ for the Bitcoin community**

