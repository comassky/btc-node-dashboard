# 🚦 Build & Continuous Integration

- The Maven build (`mvn package`) runs all tests and produces the final artifact (backend + frontend bundled).
- Docker and native workflows use this artifact: no build or tests are repeated in those steps.
- CI (GitHub Actions) blocks any deployment if a test fails.
- See [BUILD.md](BUILD.md) and [TESTING.md](TESTING.md) for more details.
# Bitcoin Node Dashboard ₿

Monitor your Bitcoin Core node in real-time with a modern web interface.

![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.30.2-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5.25-green.svg)

## 📸 Screenshots

<img width="3024" height="5236" alt="node hjacquot xyz_" src="https://github.com/user-attachments/assets/9d4ac01d-47cc-4bd1-996c-31deb6b7609a" />


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
| **Quarkus** | 3.30.2 | Supersonic Subatomic Java Framework |
| **Mutiny** | 2.x | Reactive programming library |
| **Jakarta WebSocket** | - | Real-time communication |
| **MicroProfile REST Client** | - | HTTP client for Bitcoin RPC |
| **Jackson** | - | JSON processing |

### Frontend
| Technology | Version | Description |
|------------|---------|-------------|
| **Vue.js** | 3.5.25 | Progressive JavaScript framework |
| **TypeScript** | 5.9.3 | Type-safe JavaScript |
| **Vite** | 7.2.6 | Next-generation frontend tooling |
| **Tailwind CSS** | 3.4.18 | Utility-first CSS framework |
| **Chart.js** | 4.5.1 | Interactive charts |
| **Font Awesome** | 7.1.0 | Icon library |

### Build & Deploy
- **Maven** 3.9.11 (Backend build and dependency management)
- **Maven Compiler Plugin** 3.14.1
- **Maven Surefire Plugin** 3.5.4
- **Frontend Maven Plugin** 1.15.4
- **npm** 11.6.2 (with Node.js v24.11.1)
- **npm ci** (Optimized frontend dependency installation with --prefer-offline)
- **Docker** (JVM & GraalVM Native images)
- **GitHub Actions** (CI/CD with automated testing and native image builds)
- **GraalVM Native Image** (AOT compilation for ultra-fast startup)

## 🚀 Quick Start

### Prerequisites
- Java 21+, Maven 3.9+, Bitcoin Core with RPC enabled
- Node.js 24+ (optional, for frontend dev)

### Development
```bash
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard

# Configure RPC (env vars or application-local.properties)
export RPC_HOST=localhost
export RPC_PORT=8332
export RPC_USER=your_user
export RPC_PASS=your_password

# Run with hot reload
./mvnw quarkus:dev  # → http://localhost:8080
```

### Docker (Recommended)
```bash
# GraalVM Native (50ms startup, 30MB memory)
docker run -d -p 8080:8080 \
  -e RPC_HOST=<HOST> -e RPC_PORT=<PORT> \
  -e RPC_USER=<USER> -e RPC_PASS=<PASSWORD> \
  -e WS_POLLING_INTERVAL=5 \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  -e DASHBOARD_CACHE_VALIDITY_BUFFER_MS=200 \
  ghcr.io/comassky/btc-node-dashboard:native
```

See [BUILD.md](BUILD.md) for detailed instructions.

## 📊 API Endpoints

### REST API

- **GET** `/api/config` — Dashboard configuration (minOutboundPeers)
- **GET** `/data/dashboard` — Get aggregated dashboard data (GlobalResponse)
- **GET** `/data/getnetworkinfo` — Get node network information (NodeInfo)
- **GET** `/data/getblock/{hash}` — Get block information by hash (BlockInfo)
- **GET** `/data/getbestblockhash` — Get the hash of the best block (plain text)
- **GET** `/data/getblockchainInfo` — Get blockchain information (BlockchainInfo)

### WebSocket

- **WS** `/ws/dashboard` - Real-time dashboard updates

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

## 🎨 Frontend Development

See [BUILD.md](BUILD.md) for detailed instructions on setting up and running the frontend development server, as well as building the frontend.

## 🤝 Contributing

Contributions welcome! Fork, branch, commit, push, PR.

<div align="center">

### Cryptocurrency Donations

| Currency | Address |
|----------|----------|
| **Bitcoin (BTC)** | `bc1qa7kcf6r9xemdmcs7wufufztfcl7rzravx9naz3` |
| **Ethereum / BSC (ETH)** | `0x0f26B8Bdc028F6bd0F79FF4959306065C36d5EAa` |
| **Solana (SOL)** | `FH7HPraEeSva72g5Cv2WTbP65tPxQiZc1GNCSk2ML7eN` |

### Lightning Network ⚡

<img width="320" height="312" alt="Lightning Network QR Code" src="https://github.com/user-attachments/assets/6cfd0bfa-fb41-48eb-b429-3420e5cf63de" />

<br>

**Every satoshi helps keep this project maintained and growing!** 🚀

*Your support enables continuous improvements, new features, and better documentation.*

</div>

## 🙏 Acknowledgments

Built with [Quarkus](https://quarkus.io/), [Vue.js](https://vuejs.org/), [Tailwind CSS](https://tailwindcss.com/), [Chart.js](https://www.chartjs.org/), and [Bitcoin Core](https://bitcoincore.org/).

---

**Built with ❤️ for the Bitcoin community**
    
