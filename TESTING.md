# 🔄 Continuous Integration

- CI runs all tests (backend and frontend) during the Maven build.
- Docker/native images are only built and published if all tests pass.
- No tests are re-run during Docker or native build steps.

# Testing Guide

**164 automated tests** covering backend and frontend for reliability and stability.

## 📊 Overview

| Component | Tests | Technologies                                                                                                                                                                                |
| --------- | ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Backend   | 79    | JUnit 5, Mockito, Quarkus Test, Rest Assured                                                                                                                                                |
| Frontend  | 83    | Vitest (4.0.16), Vue Test Utils (2.4.6), Happy DOM (20.0.11), Vite (7.3.1), TypeScript (5.9.3), VueUse (14.1.0), Chart.js (4.5.1), Tailwind CSS (4.1.18), Iconify (5.0.0), Simple Icons (1.2.66), Floating UI (1.1.9) |

**Test execution**: ~25s total (Backend: ~20s, Frontend: ~3.5s)

## 🧪 Running Tests

```bash
# All tests
./mvnw clean test

# Backend only
./mvnw test

# Frontend only
cd src/main/web && pnpm test

# With coverage
cd src/main/web && pnpm coverage
```

## 🔍 Backend Tests (79)

**Test Classes:**

- `BtcControllerTest` - REST API endpoints
- `DashboardWebSocketTest` - WebSocket lifecycle
- `DashboardWebSocketAdvancedTest` - Concurrent connections, cache
- `RpcServicesTest` - Bitcoin RPC calls, logs, and latency
- `RpcServicesAdvancedTest` - Multi-peer aggregation, error handling
- `RpcServicesParallelTest` - Parallel execution, CompletableFuture
- `CachedMessageTest` - Cache validation, thread-safety
- `SubverStatsCalculationTest` - Version distribution
- `ToolsTest` - Utility functions
- `BtcApiAppTest` - Application lifecycle
- `CacheProviderTest` (7 tests) - Cache behavior, invalidation, expiration
- `DashboardConfigTest` (7 tests) - Configuration validation and defaults
- `RpcExceptionTest` (7 tests) - Exception creation, cause propagation, stack traces

## 🎨 Frontend Tests (67)

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

- **pnpm** 10.28.0 (frontend package manager, monorepo workspace)
- **Vitest** 4.0.16 (unit tests framework)
- **Vue Test Utils** 2.4.6, **Happy DOM** 20.0.11, **Vite** 7.3.0, **TypeScript** 5.9.3
- **VueUse** 14.1.0 (composition utilities with useFetch, useWebSocket)
- **Chart.js** 4.5.1 (tree-shaken with explicit imports), **Iconify** 5.0.0, **Simple Icons** 1.2.65, **Floating UI** 1.1.9
- **Tailwind CSS** 4.1.18 with Lightning CSS, **vite-plugin-compression** 0.5.1
- **rollup-plugin-visualizer** 6.0.5, **sirv-cli** 3.0.1, **vue-tsc** 3.2.1

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

- `pnpm test` : unit tests (Vitest)
- `pnpm test:ui` : interactive test UI
- `pnpm coverage` : coverage report

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
./mvnw test -X

# Run single test with debugging
./mvnw test -Dtest=MyTest -Dmaven.surefire.debug
```

### Frontend

```bash
# Frontend UI mode
cd src/main/web && npm run test:ui
```

## 🔄 Continuous Integration


### GitHub Actions Workflows

CI workflows use pnpm to install frontend dependencies and run tests (see `.github/workflows/docker.yml`, `docker-native.yml`, `docker-dev-native.yml`).

Before each Docker image build:
- ✅ Backend tests are executed
- ✅ Frontend tests are executed (pnpm test, pnpm coverage)
- ❌ Build is cancelled if any test fails

This ensures only tested versions are deployed.
