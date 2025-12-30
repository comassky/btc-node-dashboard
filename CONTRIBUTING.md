# 🧩 Monorepo & Développement Frontend

Le projet utilise un workspace pnpm pour la gestion des dépendances frontend (voir `src/main/web/pnpm-workspace.yaml`).

## Installation des dépendances frontend

```bash
cd src/main/web
pnpm install
# ou, si pnpm n'est pas installé :
npm install -g pnpm@10.26.2
pnpm install
```

## Scripts utiles

- `pnpm dev` : serveur de développement Vite
- `pnpm build` : build de production
- `pnpm test` : tests unitaires frontend (Vitest)
- `pnpm test:ui` : UI de tests
- `pnpm coverage` : couverture de tests

Les workflows CI utilisent pnpm pour garantir la cohérence des dépendances.

---
# Versions used

- Java 25
- Quarkus 3.30.5
- Node.js v24.12.0
- npm 11.6.2

# 🛠️ CI & Quality

- Every Pull Request must pass all tests to be merged.
- CI workflows ensure that no untested code is deployed.

# Contributing to btc-node-dashboard

Thank you for considering contributing to this project! Please read the following guidelines to help us maintain a robust, reactive, and high-quality codebase.

## ⚡ Reactive Programming & Non-Blocking Guarantees

- All API endpoints (REST and WebSocket) must be implemented using Mutiny (`Uni`/`Multi`), ensuring non-blocking I/O.
- All RPC calls to Bitcoin Core are performed asynchronously, composed reactively, and executed in parallel with DEBUG logs and latency measurement for each call.
- No blocking calls (e.g. `Thread.sleep`, synchronous I/O, blocking DB access) are allowed in the critical path.
- WebSocket push and scheduled broadcast use non-blocking data fetch and asynchronous serialization.

### Reactive Entry Points

- `BtcController` (REST): All methods return `Uni<T>`
- `ConfigController` (REST): All methods return `Uni<T>`
- `DashboardWebSocket`: All data sent to clients is fetched and pushed reactively
- `RpcServices`: All services are parallel, non-blocking, logged and measured

### Contribution Guidelines

- Never introduce blocking calls in the critical path.
- Always use Mutiny (`Uni`/`Multi`) for async operations and data composition.
- If you add a new endpoint or service, ensure it is non-blocking and returns `Uni` or `Multi`.
- Use `.onFailure()` and `.ifNoItem().after(Duration)` for robust error and timeout handling.
- Add tests for any new reactive code (see existing tests for patterns).

For more details, see the comments in `RpcServices`, `BtcController`, and `DashboardWebSocket`.

---

## 🧪 Mock/Test Mode for Frontend Development

The dashboard includes a mock mode to help you develop and test the frontend without requiring a live Bitcoin node. This is especially useful for simulating error states, warnings, and demo scenarios.

### How to Enable

- Add `VITE_MOCK_MODE=true` to your frontend `.env` file.
- Start the application as usual.

### How to Use

- A "MOCK MODE" panel will appear in the top left corner of the dashboard UI.
- Use the "Cycle Scenario" button to switch between different simulated states:
  - **normal**: healthy node, all systems operational
  - **lowPeers**: low outbound peer count, triggers warning
  - **outOfSync**: node is behind on block sync, triggers warning
  - **disconnected**: simulates WebSocket/RPC connection loss
- The dashboard components will automatically react to these scenarios, allowing you to visually verify warning and error handling.

### Customization

Mock logic and scenarios are managed in `src/main/web/src/composables/useMockData.ts`. You can extend or modify the mock data to fit your testing needs.

Feel free to open issues or pull requests if you have questions or suggestions!
