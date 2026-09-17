# 🧩 Monorepo & Frontend Development

The project uses a pnpm workspace for frontend dependency management (see `src/main/web/pnpm-workspace.yaml`).

## Installing frontend dependencies

```bash
cd src/main/web
# Use Node.js v24.13.0; versions are defined in pom.xml
npm install -g pnpm@10.28.2
pnpm install --frozen-lockfile
```

For a full application build, run `mvn -B --no-transfer-progress verify` from the repository root with JDK 17 or newer. Maven installs Node.js, bundled npm and pnpm locally; global frontend tools are not required for that path. See [BUILD.md](BUILD.md).

## Useful Scripts

- `pnpm dev` : Vite development server with hot reload
- `pnpm build` : production build with optimizations
- `pnpm test` : frontend unit tests (Vitest)
- `pnpm run test --run` : frontend unit tests once, as in CI
- `pnpm test:ui` : interactive test UI
- `pnpm coverage` : test coverage report
- `pnpm prettier` : format code with Prettier

Maven and CI use `pnpm install --frozen-lockfile`. For intentional dependency updates, run `pnpm install` and commit [package.json](src/main/web/package.json) together with [pnpm-lock.yaml](src/main/web/pnpm-lock.yaml); do not introduce an npm lockfile.

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

- Java 17+
- Quarkus 3.32.0.CR1
- Node.js v24.13.0
- pnpm 10.28.2
- npm bundled with the Maven-managed Node.js installation

[pom.xml](pom.xml) is the source of truth for the Java/frontend toolchain. [renovate.json](renovate.json) groups Java, frontend, Docker and GitHub Actions updates separately. Docker digests are pinned, automerge is disabled, and the Compose image published by this repository is excluded from dependency updates.

# 🛠️ CI & Quality

- Build-relevant pull requests run `mvn verify` with native compilation through Mandrel in Docker. This runs backend and frontend unit tests; Failsafe integration tests are disabled by the current Maven configuration.
- Documentation-only changes skip compilation and image builds. Pull requests never publish images.
- Maven test failures stop both the JVM Docker build and the native publication workflows. Do not add test-skip flags to CI.
- The runtime images are Distroless Debian 13, run as UID/GID `65532:65532`, and contain no shell. Use `JAVA_TOOL_OPTIONS` for JVM options.

Before submitting build or workflow changes:

```bash
mvn -B --no-transfer-progress verify
docker build --check -f Dockerfile .
docker build --check -f Dockerfile.native .
docker run --rm -v "$PWD:/repo:ro" -w /repo rhysd/actionlint:latest -color
npx --yes --package renovate renovate-config-validator --strict renovate.json
```

Build checks validate Dockerfile configuration, not a native executable's runtime compatibility. Verify native changes with the containerized build command in [BUILD.md](BUILD.md).

Maintainers use [release.yml](.github/workflows/release.yml) to verify a release before committing versions, creating its tag and publishing it. Use [publish-image.yml](.github/workflows/publish-image.yml) to rebuild an existing tag. See [DOCKER.md](DOCKER.md) for required permissions and [OPENAPI.md](OPENAPI.md) for release documentation deployment.

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
