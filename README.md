# Bitcoin Node Dashboard ₿

A modern, real-time Bitcoin network monitoring application with a sleek web interface. Monitor your Bitcoin Core node's peers, blockchain status, and network statistics through an interactive dashboard.

![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.18.1-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5.25-green.svg)
![GraalVM](https://img.shields.io/badge/GraalVM-Native-blueviolet.svg)
![Tests](https://img.shields.io/badge/tests-130%20passing-brightgreen.svg)

## 📸 Screenshots

<img width="3016" height="1010" alt="Cards" src="https://github.com/user-attachments/assets/8e952912-645d-4f48-aa6c-9610cf2c93a5" />
<img width="3024" height="1174" alt="Peer Statistics" src="https://github.com/user-attachments/assets/ffe5f913-408c-48f3-9d2f-4c3da98c4f39" />
<img width="3022" height="1202" alt="image" src="https://github.com/user-attachments/assets/4bf9aa26-6d41-4ede-bb9e-c4dd4002dd37" />
<img width="3024" height="1394" alt="image" src="https://github.com/user-attachments/assets/2dd16738-c6b4-49b5-93b5-93b694e94690" />


## ✨ Features

### Real-time Monitoring
- **Live Peer Statistics**: Track inbound and outbound peer connections in real-time
- **Network Distribution**: Visualize Bitcoin client versions across the network with interactive charts
- **Blockchain Information**: Monitor current blockchain height, verification progress, and sync status
- **Node Metrics**: Display node uptime, protocol version, and connection statistics

### Modern UI/UX
- **Dark/Light Mode**: Toggle between themes for comfortable viewing
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices
- **Real-time Updates**: WebSocket-based live data streaming (configurable interval, default 5s)
- **Interactive Charts**: Beautiful pie charts showing peer distribution
- **Exponential Backoff**: Smart reconnection strategy with 1s → 30s retry delays

### Technical Features
- **WebSocket Communication**: Efficient real-time data push with message caching
- **Error Handling**: Comprehensive error management with user-friendly messages
- **Auto-reconnection**: Automatic WebSocket reconnection on connection loss
- **Optimized Performance**: Tree-shaking, code splitting, Terser minification, and gzip compression
- **GraalVM Native**: Ultra-fast startup (<50ms) and minimal memory footprint (~30MB)

## 🛠️ Tech Stack

### Backend
| Technology | Version | Description |
|------------|---------|-------------|
| **Java** | 21 | Programming language |
| **Quarkus** | 3.18.1 | Supersonic Subatomic Java Framework |
| **Jakarta WebSocket** | - | Real-time communication |
| **MicroProfile REST Client** | - | HTTP client for Bitcoin RPC |
| **Jackson** | - | JSON processing |
| **Lombok** | 1.18.36 | Boilerplate reduction |

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
- **Maven** (Backend build and dependency management)
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

**For detailed build and deployment instructions, see [BUILD.md](BUILD.md)**

### Prerequisites

- Java 21+, Maven 3.9+, Bitcoin Core with RPC enabled
- Node.js 24+ and npm 11+ (for frontend development)

### Run Locally

```bash
# Clone repository
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard

# Configure Bitcoin RPC (create application-local.properties or use env vars)
# See BUILD.md for configuration details

# Development mode with hot reload
./mvnw quarkus:dev

# Access at http://localhost:8080
```

### Docker Deployment

```bash
# GraalVM Native (⚡ recommended - 50ms startup, 30MB memory)
docker run -d -p 8080:8080 \
  -e RPC_HOST=<HOST> \
  -e RPC_PORT=<PORT> \
  -e RPC_USER=<USER> \
  -e RPC_PASS=<PASS> \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  ghcr.io/comassky/btc-node-dashboard:native

# JVM Image
docker run -d -p 8080:8080 \
  -e RPC_HOST=<HOST> \
  -e RPC_PORT=<PORT> \
  -e RPC_USER=<USER> \
  -e RPC_PASS=<PASS> \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  ghcr.io/comassky/btc-node-dashboard:main
```

Browse all images: [GitHub Packages](https://github.com/comassky/btc-node-dashboard/pkgs/container/btc-node-dashboard)

## 📊 API Endpoints

### REST API

- **GET** `/api/config` - Get dashboard configuration (min outbound peers threshold)
- **GET** `/data/peers` - Get current peer statistics and blockchain info

### WebSocket

- **WS** `/ws/dashboard` - Real-time dashboard updates

## 🧪 Testing

![Tests](https://img.shields.io/badge/tests-130%20passing-brightgreen.svg)

The project includes a comprehensive test suite with **130 tests** covering both backend and frontend.

**For detailed testing documentation, see [TESTING.md](TESTING.md)**

### Quick Start

```bash
# Run all tests (backend + frontend)
./mvnw clean test

# Backend only
./mvnw test

# Frontend only
cd src/main/web && npm test
```

### Test Coverage

| Component | Tests | Technologies |
|-----------|-------|--------------|
| **Backend** | 64 | JUnit 5.10.5, Mockito 5.15.2, Quarkus Test |
| **Frontend** | 66 | Vitest 2.1.9, Vue Test Utils 2.4.6, Happy DOM |

## 🚀 Performance Optimizations

### Backend
- ✅ **WebSocket Message Caching**: Single RPC call shared across multiple concurrent connections
- ✅ **Thread-safe Caching**: Synchronized access with `CachedMessage` record pattern
  - Smart cache invalidation: `validity = max(100ms, pollingInterval - 100ms)`
  - Prevents concurrent RPC calls even if Bitcoin Core RPC takes >200ms
  - Lock-based synchronization ensures only one thread fetches data at a time
- ✅ **Non-blocking I/O**: RPC calls executed on worker threads to avoid blocking event loop
- ✅ **Efficient Broadcasting**: Single JSON serialization per broadcast cycle
- ✅ **Stream API**: Optimal peer statistics calculation with parallel processing
- ✅ **Immutable Records** (Java 21): Thread-safe DTOs with zero boilerplate
- ✅ **GraalVM Native Optimizations**:
  - `@RegisterForReflection` on all DTOs for Jackson compatibility
  - SLF4J initialized at build-time for faster startup
  - Native image reports enabled for debugging
  - AOT compilation: <50ms startup, ~30MB memory footprint

### Frontend
- ✅ **Tailwind CSS v3**: Utility-first CSS framework with PostCSS optimization
- ✅ **Vite Production Build**: Terser minification (2 passes), code splitting, tree-shaking
- ✅ **Gzip Compression**: ~70% size reduction on JS/CSS assets
- ✅ **Modular TypeScript**: Individual type files with barrel exports
- ✅ **Chart.js Optimizations**: Animations disabled for better performance
- ✅ **WebSocket Exponential Backoff**: Smart reconnection (1s → 30s max delay)
- ✅ **Version Sync**: Automatic version injection from `pom.xml` → `package.json` → runtime
- ✅ **PWA Support**: Service Worker for offline capability and app-like experience

### Build Optimizations
- ✅ **Maven Resource Filtering**: Binary-safe handling of compressed assets
- ✅ **Frontend Cache**: Node.js modules cached in GitHub Actions
- ✅ **Docker Multi-stage**: Optimized image layers with build cache

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## ☕ Support the Project

If you find this project useful and want to support its development, consider making a donation!

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

- [Quarkus](https://quarkus.io/) - Supersonic Subatomic Java
- [Vue.js](https://vuejs.org/) - The Progressive JavaScript Framework
- [Tailwind CSS](https://tailwindcss.com/) - Utility-first CSS framework
- [Chart.js](https://www.chartjs.org/) - Simple yet flexible JavaScript charting
- [Bitcoin Core](https://bitcoincore.org/) - Bitcoin reference implementation

## 📧 Contact

Project Link: [https://github.com/comassky/btc-node-dashboard](https://github.com/comassky/btc-node-dashboard)

---

**Built with ❤️ for the Bitcoin community**
    
