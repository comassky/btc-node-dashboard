# Testing Guide

**162 unit tests** (79 backend and 83 frontend) passed in the last verified JVM Docker build. The native compile and runtime still require CI validation; these counts do not imply native integration-test coverage.

Full builds require JDK 25 and Maven. Quinoa installs Node.js and bundled npm locally, so no global frontend toolchain or live Bitcoin Core node is required for the unit tests. See [BUILD.md](BUILD.md).

## 📊 Overview

| Component | Tests | Technologies                                                                                                                                                                                |
| --------- | ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Backend   | 79    | JUnit 5, Mockito, Quarkus Test, Rest Assured                                                                                                                                                |
| Frontend  | 83    | Vitest (4.0.18), Vue Test Utils (2.4.6), Happy DOM (20.7.0) |

Test counts and duration may change as the suites evolve. [package.json](src/main/web/package.json) and [pom.xml](pom.xml) define the current tools and versions.

## 🧪 Running Tests

```bash
# Both unit-test suites, frontend build and JVM package
mvn -B --no-transfer-progress clean verify

# Backend only
mvn test
```

For standalone frontend testing, use the Node.js version and bundled npm from [BUILD.md](BUILD.md):

```bash
cd src/main/web
npm ci

# Frontend tests once, as in CI
npm run test -- --run

# Interactive watch mode
npm test

# Coverage report (not part of the default CI build)
npm run coverage
```

### Test Selection

Backend tests run in the `test` phase; frontend tests run during the Quinoa build in the `package` phase. `-DskipTests` skips backend tests but not frontend tests. To skip both for a local build only, use `mvn package -DskipTests -DskipFrontendTests=true`.

Failsafe integration tests are disabled by `skipITs=true` in the current Maven configuration, including the native profile. Neither `mvn verify` nor `-Dnative` currently validates a running native application through Failsafe.

## 🔍 Backend Tests (79)

**Test Classes:**

- `BtcControllerTest` - REST API endpoints
- `BitcoinApiControllerTest` - Bitcoin API endpoint responses
- `DashboardWebSocketTest` - WebSocket lifecycle
- `DashboardWebSocketAdvancedTest` - Concurrent connections, cache
- `RpcServicesTest` - Bitcoin RPC calls, logs, and latency
- `RpcServicesAdvancedTest` - Multi-peer aggregation, error handling
- `CachedMessageTest` - Cache validation, thread-safety
- `SubverStatsCalculationTest` - Version distribution
- `ToolsTest` - Utility functions
- `BtcApiAppTest` - Application lifecycle
- `CacheProviderTest` (7 tests) - Cache behavior, invalidation, expiration
- `DashboardConfigTest` (7 tests) - Configuration validation and defaults
- `RpcExceptionTest` (7 tests) - Exception creation, cause propagation, stack traces

## 🎨 Frontend Tests (83)

**Test Files**:

- `useWebSocket.test.ts` - Connection, messaging, reconnection
- `useTheme.test.ts` - Dark/light mode, localStorage
- `Status.test.ts` - Component rendering, states
- `types.test.ts` - Interfaces, data validation
- `formatters.test.ts` - Number/date formatting
- `logic.test.ts` - Business logic, calculations
- `nodeHealth.test.ts` - Node health checks
- `formatting.test.ts` - Text formatting utilities
- `Tooltip.test.ts` - Tooltip component
- `BaseCard.test.ts` - Base card component
- `PeersCard.test.ts` - Peers card component
- `BlockCard.test.ts` - Block card component
- `MempoolInfoCard.test.ts` - Mempool info card component


**Main tools and libraries:**

- **npm** bundled with the Quinoa-managed Node.js installation (frontend package manager)
- **Vitest** 4.0.18 (unit tests framework)
- **Vue Test Utils** 2.4.6, **Happy DOM** 20.7.0, **Vite** 7.3.1, **TypeScript** 5.9.3
- **VueUse** 14.2.1 (composition utilities with useFetch, useWebSocket)
- **Chart.js** 4.5.1 (tree-shaken with explicit imports), **unplugin-icons** 23.0.1, **Simple Icons** 1.2.71, **Floating UI** 1.1.10
- **Tailwind CSS** 4.2.1 with Lightning CSS, **vite-plugin-compression** 0.5.1
- **rollup-plugin-visualizer** 7.0.0, **sirv-cli** 3.0.1, **vue-tsc** 3.2.5

**Note**: PWA plugin, cssnano, and postcss have been removed as part of recent optimizations.

## 📝 Writing New Tests

### Backend Test Template

```java
@QuarkusTest
class MyServiceTest {

    @InjectMock
    MyDependency dependency;

    @Inject
    MyService service;

    @Test
    void shouldDoSomething() {
        // Arrange
        when(dependency.method()).thenReturn(expectedValue);

        // Act
        var result = service.doSomething();

        // Assert
        assertEquals(expectedValue, result);
        verify(dependency).method();
    }
}
```


### Frontend test scripts

- `npm test` : unit tests (Vitest)
- `npm run test -- --run` : run once without watch mode
- `npm run test:ui` : interactive test UI
- `npm run coverage` : coverage report

### Frontend Test Example

```typescript
import { describe, it, expect, vi } from "vitest";
import { mount } from "@vue/test-utils";
import MyComponent from "../MyComponent.vue";

describe("MyComponent", () => {
  it("should render correctly", () => {
    const wrapper = mount(MyComponent, {
      props: { value: "test" },
    });

    expect(wrapper.text()).toContain("test");
  });
});
```

## 🐛 Debugging Tests

### Backend

```bash
# Run with debug output
mvn test -X

# Run single test with debugging
mvn test -Dtest=MyTest -Dmaven.surefire.debug
```

### Frontend

```bash
# Frontend UI mode
cd src/main/web && npm run test:ui
```

## 🔄 Continuous Integration


### GitHub Actions Workflows

The [development](.github/workflows/docker.yml), [release](.github/workflows/release.yml) and [tag publication](.github/workflows/publish-image.yml) workflows use `mvn verify -Dnative -Dquarkus.native.container-build=true -Dquarkus.native.native-image-xmx=6g`. Quinoa installs Node.js and bundled npm during the Maven build, which runs the unit tests, builds the frontend, and compiles the native runner using Mandrel in Docker.

Before native image packaging:
- ✅ Backend tests are executed
- ✅ Frontend tests are executed (`npm run test -- --run`, during the Quinoa build)
- ❌ Build is cancelled if any test fails

This ensures only tested versions are deployed.

The JVM Dockerfile also runs `mvn verify`. For local builds only, `-DskipFrontendTests=true` skips the frontend tests; CI does not set this flag. Running `mvn test` alone only runs backend tests.

Pull requests never publish images, and documentation-only changes skip compilation in the development workflow. The native Dockerfile only copies the verified executable; it does not run the test suites again.

### Workflow and Dockerfile Checks

```bash
docker run --rm -v "$PWD:/repo:ro" -w /repo rhysd/actionlint:latest -color
docker build --check -f Dockerfile .
docker build --check -f Dockerfile.native .
npx --yes --package renovate renovate-config-validator --strict renovate.json
```

These are static checks, not build or runtime tests. For the full JVM build use `docker build -t btc-node-dashboard:jvm .`; for native builds use the command in [BUILD.md](BUILD.md), then package and run the image as described in [DOCKER.md](DOCKER.md).
