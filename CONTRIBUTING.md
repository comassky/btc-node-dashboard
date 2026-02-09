# 🧩 Monorepo & Frontend Development

The project uses a pnpm workspace for frontend dependency management (see `src/main/web/pnpm-workspace.yaml`).

## Installing frontend dependencies

```bash
cd src/main/web
pnpm install
# or, if pnpm is not installed:
npm install -g pnpm@10.27.0
pnpm install
```

## Useful Scripts

- `pnpm dev` : Vite development server with hot reload
- `pnpm build` : production build with optimizations
- `pnpm test` : frontend unit tests (Vitest)
- `pnpm test:ui` : interactive test UI
- `pnpm coverage` : test coverage report
- `pnpm prettier` : format code with Prettier

CI workflows use pnpm to ensure dependency consistency.

## Performance Optimizations

The frontend is highly optimized for performance:

- **Bundle Size**: 144KB Brotli (~10% reduction from previous 160KB)
- **Chart.js Tree-Shaking**: Explicit imports instead of 'chart.js/auto' (-16KB)
- **Component Optimization**: Removed 1375+ redundant Tooltip instances from peer tables
- **Lazy Loading**: Async component imports with defineAsyncComponent
- **CSS Optimization**: Tailwind CSS 4 with Lightning CSS for faster builds
- **Compression**: Brotli and Gzip compression enabled

### Key Optimizations Made

1. **Chart.js**: Switched from 'chart.js/auto' to explicit imports (ArcElement, DoughnutController, etc.)
2. **Tooltips**: Replaced Vue Tooltip components with native HTML title attributes in peer tables
3. **Icons**: Using unplugin-icons with tree-shaking, added @iconify-json/simple-icons for official brand logos
4. **Removed Dependencies**: Eliminated PWA plugin, cssnano (replaced by Lightning CSS), postcss
5. **Component Memoization**: Added v-once for static cells, computed properties for formatted values
6. **Animations**: Disabled unnecessary Chart.js animations for instant rendering

When contributing, please maintain these optimizations and avoid re-introducing removed dependencies.

---
# Versions used

- Java 25
- Quarkus 3.31.2
- Node.js v24.12.0
- pnpm 10.28.2
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
