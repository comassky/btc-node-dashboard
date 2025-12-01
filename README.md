# BTC Node Dashboard - API Backend ₿

This project is the backend API for a **Bitcoin node monitoring dashboard** (BTC Node Dashboard). It is designed to interface with a **Bitcoin Core** node and expose its status and performance data via a **RESTful interface**, intended to be consumed by a frontend application.

The API is built using **Quarkus**, the Supersonic Subatomic Java Framework, offering fast startup times and low memory consumption.

---

<img width="2970" height="1504" alt="image" src="https://github.com/user-attachments/assets/08926806-690a-4981-8b66-57d733a2ea50" />
<img width="2908" height="1480" alt="image" src="https://github.com/user-attachments/assets/ade7b4a8-54cb-4e6c-b4c4-d6fb5c6b8dc0" />


## Features (Inferred)

* **Node Status:** Retrieval of key data such as current block height, sync status, and node version.
* **Network Information:** Monitoring of peer connections (number of peers, addresses, latency).
* **REST API:** All data points are exposed via easy-to-consume REST endpoints.

## Technologies Used

| Technology | Description |
| :--- | :--- |
| **Framework** | [Quarkus](https://quarkus.io/) |
| **Langage** | Java |
| **Outil de Build** | Maven |
| **Conteneurisation** | Docker |
| **Licence** | MIT |


## Prerequisites

To run and develop this project, you will need:

* **JDK 21** or higher
* **Apache Maven**
* A running **Bitcoin Core** node accessible via RPC.

---

## Bitcoin Node Configuration

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

### 1\. Pull the Image

```bash
docker pull ghcr.io/comassky/btc-node-dashboard:main
```

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

- **SOL:**    64NUvVYMwwnchsTYwbWboGfyez4j7dGsJnB1eiWZhbkm

## License

This project is distributed under the [MIT License](https://www.google.com/search?q=LICENSE).
    
