# Bitcoin Node Dashboard ₿

A modern, real-time Bitcoin network monitoring application with a sleek web interface. Monitor your Bitcoin Core node's peers, blockchain status, and network statistics through an interactive dashboard.

![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.30.1-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5-green.svg)

## 📸 Screenshots

<img width="3024" height="914" alt="Dashboard Overview" src="https://github.com/user-attachments/assets/0267a62a-a914-4273-92c2-fb1b6886868e" />
<img width="3024" height="1054" alt="Peer Statistics" src="https://github.com/user-attachments/assets/49209d3a-857d-4076-a82c-ab1d38a4b876" />
<img width="2988" height="1198" alt="Network Distribution" src="https://github.com/user-attachments/assets/20d3a60d-b2ec-47c3-900b-3f099eddca17" />
<img width="3000" height="1162" alt="Connection Details" src="https://github.com/user-attachments/assets/8e4a43d6-788c-4a50-88aa-61761d10f365" />

## ✨ Features

### Real-time Monitoring
- **Live Peer Statistics**: Track inbound and outbound peer connections in real-time
- **Network Distribution**: Visualize Bitcoin client versions across the network with interactive charts
- **Blockchain Information**: Monitor current blockchain height, verification progress, and sync status
- **Node Metrics**: Display node uptime, protocol version, and connection statistics

### Modern UI/UX
- **Dark/Light Mode**: Toggle between themes for comfortable viewing
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices
- **Real-time Updates**: WebSocket-based live data streaming (5-second intervals)
- **Interactive Charts**: Beautiful pie charts showing peer distribution

### Technical Features
- **WebSocket Communication**: Efficient real-time data push to clients
- **Error Handling**: Comprehensive error management with user-friendly messages
- **Auto-reconnection**: Automatic WebSocket reconnection on connection loss
- **Optimized Performance**: Tree-shaking, code splitting, and CSS purging

## 🛠️ Tech Stack

### Backend
| Technology | Version | Description |
|------------|---------|-------------|
| **Java** | 21 | Programming language |
| **Quarkus** | 3.30.1 | Supersonic Subatomic Java Framework |
| **Jakarta WebSocket** | - | Real-time communication |
| **MicroProfile REST Client** | - | HTTP client for Bitcoin RPC |
| **Jackson** | - | JSON processing |
| **Lombok** | 1.18.34 | Boilerplate reduction |

### Frontend
| Technology | Version | Description |
|------------|---------|-------------|
| **Vue.js** | 3.5 | Progressive JavaScript framework |
| **TypeScript** | 5.7 | Type-safe JavaScript |
| **Vite** | 6.0 | Next-generation frontend tooling |
| **Tailwind CSS** | 3.4 | Utility-first CSS framework |
| **Chart.js** | 4.4 | Interactive charts |
| **Font Awesome** | 7.1 | Icon library |

### Build & Deploy
- **Maven** (Backend build and dependency management)
- **Docker** (Containerization)
- **GitHub Actions** (CI/CD ready)

## 📋 Prerequisites

- **Java Development Kit (JDK) 21** or higher
- **Apache Maven 3.9+** (or use the included wrapper `./mvnw`)
- **Bitcoin Core** node with RPC access enabled
- **Node.js 24+** and **npm 11+** (for frontend development)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/comassky/btc-node-dashboard.git
cd btc-node-dashboard
```

### 2. Configure Bitcoin Core RPC

Create a configuration file at `src/main/resources/application-local.properties`:

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

### 3. Build and Run

#### Development Mode (with hot reload)

```bash
# Backend + Frontend
./mvnw quarkus:dev

# Access the dashboard at http://localhost:8080
```

#### Production Build

```bash
# Build the application
./mvnw clean package

# Run the JAR
java -jar target/quarkus-app/quarkus-run.jar
```

### 4. Access the Dashboard

Open your browser and navigate to:
```
http://localhost:8080
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -f src/main/docker/Dockerfile.jvm -t btc-node-dashboard:latest .
```

### Run with Docker

```bash
docker run -p 8080:8080 \
  -e BITCOIN_RPC_HOST=your-bitcoin-node \
  -e BITCOIN_RPC_USER=your_username \
  -e BITCOIN_RPC_PASSWORD=your_password \
  btc-node-dashboard:latest
```

### Docker Compose

```yaml
version: '3.8'

services:
  btc-dashboard:
    image: btc-node-dashboard:latest
    ports:
      - "8080:8080"
    environment:
      BITCOIN_RPC_SCHEME: http
      BITCOIN_RPC_HOST: bitcoin-core
      BITCOIN_RPC_PORT: 8332
      BITCOIN_RPC_USER: your_username
      BITCOIN_RPC_PASSWORD: your_password
      DASHBOARD_POLLING_INTERVAL_SECONDS: 5
    depends_on:
      - bitcoin-core

  bitcoin-core:
    image: btcsuite/bitcoind:latest
    # ... your Bitcoin Core configuration
```

## 🔧 Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `bitcoin.rpc.scheme` | `http` | RPC protocol (http/https) |
| `bitcoin.rpc.host` | `localhost` | Bitcoin node hostname |
| `bitcoin.rpc.port` | `8332` | Bitcoin RPC port |
| `bitcoin.rpc.user` | - | RPC username |
| `bitcoin.rpc.password` | - | RPC password |
| `dashboard.polling.interval.seconds` | `5` | WebSocket update interval |

### Bitcoin Core Setup

Ensure your `bitcoin.conf` includes:

```conf
# Enable RPC server
server=1

# RPC credentials
rpcuser=your_username
rpcpassword=your_secure_password

# Allow connections from localhost
rpcallowip=127.0.0.1

# RPC port (mainnet: 8332, testnet: 18332)
rpcport=8332
```

## 🎨 Frontend Development

### Development Server with Hot Reload

```bash
cd src/main/web
npm install
npm run dev
```

The Vite dev server will start on `http://localhost:5173` with proxy configured to forward API/WebSocket requests to `http://localhost:8080`.

### Build for Production

```bash
cd src/main/web
npm run build
```

The optimized build will be output to `dist/` and automatically copied to `target/classes/META-INF/resources/` by Maven.

## 📊 API Endpoints

### REST API

- **GET** `/data/peers` - Get current peer statistics and blockchain info

### WebSocket

- **WS** `/ws/dashboard` - Real-time dashboard updates

## 🧪 Testing

### Run Tests

```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify
```

## 🚀 Performance Optimizations

### Backend
- ✅ Efficient WebSocket broadcasting with session management
- ✅ Single RPC call batching for multiple data points
- ✅ Stream API for optimal peer statistics calculation
- ✅ Immutable records (Java 21) for thread-safe DTOs

### Frontend
- ✅ Tailwind CSS purging (~95% size reduction)
- ✅ Vite code splitting and tree-shaking
- ✅ Terser minification for production
- ✅ Lazy component loading
- ✅ Efficient WebSocket reconnection strategy

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

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

The API uses the RPC information of your Bitcoin Core node to communicate. These parameters must be configured in your environment or via the `application.properties` file.

| Variable | Description | Default |
| :--- | :--- | :--- |
| `BITCOIN_RPC_HOST` | Hostname or IP address of the Bitcoin Core node. | `127.0.0.1` |
| `BITCOIN_RPC_PORT` | RPC port of the Bitcoin Core node. | `8332` |
| `BITCOIN_RPC_USER` | RPC username for authentication. | N/A |
| `BITCOIN_RPC_PASSWORD` | RPC password for authentication. | N/A |
| `BITCOIN_RPC_SCHEME` | Scheme to use (`http` or `https`). | `http` |
| `WS_POLLING_INTERVAL` | Dashboard refresh interval in seconds. | `5` |

Example environment variables (to be adapted):

```bash
# Bitcoin Core RPC interface address and port
BITCOIN_RPC_HOST=127.0.0.1
BITCOIN_RPC_PORT=8332
BITCOIN_RPC_SCHEME=http
# Refresh interval for Dashboard (e.g., 5 seconds)
WS_POLLING_INTERVAL=5
# RPC credentials
BITCOIN_RPC_USER=yourrpcuser
BITCOIN_RPC_PASSWORD=yourrpcpassword
````

-----

## Docker Deployment 🐳

The easiest way to run the API in production is by using the official container image from GitHub Container Registry.

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## ☕ Support the Project

If you find this project useful and want to support its development, consider buying me a coffee!

<div align="center">

### Bitcoin (BTC)
```
bc1qwhateveryouraddressishere
```

### Lightning Network ⚡
```
your-lightning-address@getalby.com
```

<br>

**Every satoshi helps keep this project maintained and growing!** 🚀

*Your support enables continuous improvements, new features, and better documentation.*

</div>

## 🙏 Acknowledgments

### 2\. Run the Container

You must provide the necessary Bitcoin RPC credentials and configuration using environment variables.

Replace `<HOST>`, `<PORT>`, `<RPC_USER>`, `<RPC_PASSWORD>`, and `<RPC_SCHEME>` with your actual values.

```bash
docker run -d \
  -p 8080:8080 \
  --name btc-dashboard-api \
  -e BITCOIN_RPC_HOST=<HOST> \
  -e BITCOIN_RPC_PORT=<PORT> \
  -e BITCOIN_RPC_USER=<RPC_USER> \
  -e BITCOIN_RPC_PASSWORD=<RPC_PASSWORD> \
  -e BITCOIN_RPC_SCHEME=<RPC_SCHEME> \
  -e WS_POLLING_INTERVAL=1 \
  ghcr.io/comassky/btc-node-dashboard:main
```

> **Note:** The example above sets `WS_POLLING_INTERVAL` to `1` second for a rapid refresh rate. The API will be accessible on port `8080` of your host machine.

-----

## Getting Started (Source Code)

### 1\. Clone the Repository

```bash
git clone [https://github.com/comassky/btc-node-dashboard.git](https://github.com/comassky/btc-node-dashboard.git)
cd btc-node-dashboard
```

### 2\. Run in Development Mode

Development mode enables live coding (hot reloading) for rapid development:

```bash
./mvnw quarkus:dev
```

> **NOTE:** Quarkus now ships with a Dev UI, which is available in dev mode only at [http://localhost:8080/q/dev/](https://www.google.com/search?q=http://localhost:8080/q/dev/).

-----

## Packaging and Running

### 1\. Standard JAR Packaging

The application can be packaged for production using:

```bash
./mvnw package
```

The application is now runnable using:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### 2\. Creating a Native Executable (Optimized Performance/Memory)

You can create a native executable (requires GraalVM or a containerized build):

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with:

```bash
./target/bitcoin-api-1.0.0-SNAPSHOT-runner
```

-----

If you enjoy my work and would like to support me, you can buy me a coffee using the following crypto addresses:

- **BTC:**     bc1qa7kcf6r9xemdmcs7wufufztfcl7rzravx9naz3

- **ETH/BSC:**    0x0f26B8Bdc028F6bd0F79FF4959306065C36d5EAa

- **SOL:**    FH7HPraEeSva72g5Cv2WTbP65tPxQiZc1GNCSk2ML7eN

-  **Lightning**

  <img width="320" height="312" alt="image" src="https://github.com/user-attachments/assets/6cfd0bfa-fb41-48eb-b429-3420e5cf63de" />


## License

This project is distributed under the [GPLV3](https://www.gnu.org/licenses/gpl-3.0.html#license-text).
    
