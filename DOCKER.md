# 🐳 Docker Guide

This document explains how to build, run, and configure the Bitcoin Node Dashboard using Docker.

## Pre-built Images

Official images are available on GitHub Packages:
https://github.com/comassky/btc-node-dashboard/pkgs/container/btc-node-dashboard

### Run Native Image (Recommended)

```bash
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

### Run JVM Image

```bash
docker run -d -p 8080:8080 \
  -e RPC_HOST=<HOST> \
  -e RPC_PORT=<PORT> \
  -e RPC_USER=<USER> \
  -e RPC_PASS=<PASSWORD> \
  -e WS_POLLING_INTERVAL=5 \
  -e MIN_OUTBOUND_PEERS=8 \
  -e LOG_LEVEL=INFO \
  -e DASHBOARD_CACHE_VALIDITY_BUFFER_MS=200 \
  ghcr.io/comassky/btc-node-dashboard:main
```

## Docker Compose

See [compose.yml](compose.yml) for a full setup with Bitcoin Core.

## Configuration

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
| `DASHBOARD_CACHE_VALIDITY_BUFFER_MS` | `200` | Time in milliseconds to subtract from the cache entry's expiry to ensure data freshness. Useful for preventing stale data in highly dynamic environments. |

### Application Properties

As an alternative to environment variables, create `src/main/resources/application-local.properties`:

```properties
bitcoin.rpc.scheme=http
bitcoin.rpc.host=localhost
bitcoin.rpc.port=8332
bitcoin.rpc.user=your_rpc_username
bitcoin.rpc.password=your_rpc_password
dashboard.polling.interval.seconds=5
dashboard.cache.validity-buffer-ms=200
```
