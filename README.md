# Bitcoin Node Dashboard ₿

Monitor your Bitcoin Core node in real-time with a modern web interface.

![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-25-orange.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.18.1-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5.25-green.svg)
![Tests](https://img.shields.io/badge/tests-144%20passing-brightgreen.svg)

## 📸 Screenshots

<img width="3016" height="1010" alt="Cards" src="https://github.com/user-attachments/assets/8e952912-645d-4f48-aa6c-9610cf2c93a5" />
<img width="3024" height="1174" alt="Peer Statistics" src="https://github.com/user-attachments/assets/ffe5f913-408c-48f3-9d2f-4c3da98c4f39" />
<img width="3022" height="1202" alt="image" src="https://github.com/user-attachments/assets/4bf9aa26-6d41-4ede-bb9e-c4dd4002dd37" />
<img width="3024" height="1394" alt="image" src="https://github.com/user-attachments/assets/2dd16738-c6b4-49b5-93b5-93b694e94690" />


## ✨ Features

- **Live Peer Statistics**: Real-time display of inbound/outbound connections, peer details, version and geographic distribution.
- **Blockchain Status**: Track block height, sync progress, node uptime, and network health.
- **Modern UI/UX**: Dark/light mode, responsive design, interactive charts, smooth animations, icon support.
- **WebSocket Streaming**: Instant dashboard updates, automatic reconnection, exponential backoff.
- **Parallel RPC Execution**: Up to 6 simultaneous Bitcoin Core requests for fast refresh.
- **Mock/Test Mode**: Simulate errors, low peer count, disconnected mode for testing and demos.
- **Comprehensive Error Handling**: Clear user messages, automatic recovery and reconnection.
- **Performance Optimized**: GraalVM Native (<50ms startup, ~30MB RAM), tree-shaking, code splitting, gzip compression.
- **Full Test Suite**: 144 automated tests (backend + frontend) for reliability.
- **Docker & CI/CD Ready**: Easy deployment, optimized images, automated builds and tests.

## 🛠️ Tech Stack

### Backend
| Technology | Version | Description |
|------------|---------|-------------|
| **Java** | 21 | Programming language |
| **Quarkus** | 3.30.2 | Supersonic Subatomic Java Framework |
| **Jakarta WebSocket** | - | Real-time communication |
| **MicroProfile REST Client** | - | HTTP client for Bitcoin RPC |
| **Jackson** | - | JSON processing |
| **Lombok** | 1.18.42 | Boilerplate reduction |

### Frontend
| Technology | Version | Description |
|------------|---------|-------------|
| **Vue.js** | 3.5.25 | Progressive JavaScript framework |
| **TypeScript** | 5.9.3 | Type-safe JavaScript |
| **Vite** | 7.2.6 | Next-generation frontend tooling |
| **Tailwind CSS** | 3.4.18 | Utility-first CSS framework |
| **Chart.js** | 4.5.1 | Interactive charts |
| **Font Awesome** | 7.1 | Icon library |

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

### Testing
| Technology | Version | Description |
|------------|---------|-------------|
| **JUnit** | 5.10.5 | Backend unit & integration tests |
| **Mockito** | 5.15.2 | Java mocking framework |
| **Vitest** | 2.1.9 | Frontend unit test framework |
| **Vue Test Utils** | 2.4.6 | Vue component testing |
| **Happy DOM** | 15.11.7 | Lightweight DOM implementation |

## 🚀 Quick Start

### Prerequisites
- Java 25+, Maven 3.9+, Bitcoin Core with RPC enabled
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
  ghcr.io/comassky/btc-node-dashboard:native
```

See [BUILD.md](BUILD.md) for detailed instructions.

## 📊 API Endpoints

### REST API

- **GET** `/api/config` — Dashboard config (minOutboundPeers)
- **GET** `/data/dashboard` — Get dashboard data (GlobalResponse)
- **GET** `/data/getnetworkinfo` — Get node info (NodeInfo)
- **GET** `/data/getblock/{hash}` — Get block info by hash (BlockInfo)
- **GET** `/data/getbestblockhash` — Get best block hash (plain text)
- **GET** `/data/getblockchainInfo` — Get blockchain info (BlockchainInfo)

### WebSocket

- **WS** `/ws/dashboard` - Real-time dashboard updates

## 🧪 Testing

![Tests](https://img.shields.io/badge/tests-144%20passing-brightgreen.svg)

The project includes a comprehensive test suite with **144 tests** covering both backend and frontend.

**For detailed testing documentation, see [TESTING.md](TESTING.md)**

## 📊 API

**REST**: `/api/config`, `/data/peers`  
**WebSocket**: `/ws/dashboard` (real-time updates)

## 🧪 Testing

**144 tests** (78 backend + 66 frontend)

```bash
./mvnw clean test           # All tests
./mvnw test                 # Backend only
cd src/main/web && npm test # Frontend only
```

See [TESTING.md](TESTING.md) for details.
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
    
## 🤝 Contributing

Contributions welcome! Fork, branch, commit, push, PR.