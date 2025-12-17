# 🔄 Continuous Integration

- CI runs all tests (backend and frontend) during the Maven build.
- Docker/native images are only built and published if all tests pass.
- No tests are re-run during Docker or native build steps.
# Testing Guide

**146 automated tests** covering backend and frontend for reliability and stability.

## 📊 Overview

| Component   | Tests | Technologies                        |
|------------ |-------|-------------------------------------|
| Backend     | 79    | JUnit 5, Mockito, Quarkus Test      |
| Frontend    | 67    | Vitest (4.0.16), Vue Test Utils (2.4.6), Happy DOM (20.0.11)   |

**Test execution**: ~25s total (Backend: ~20s, Frontend: ~3s)

## 🧪 Running Tests

```bash
# All tests
./mvnw clean test

# Backend only
./mvnw test

# Frontend only
cd src/main/web && npm test

# With coverage
cd src/main/web && npm run coverage
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

## 🎨 Frontend Tests (67)

**Test Files**:
- `useWebSocket.test.ts` (9) - Connection, messaging, reconnection
- `useTheme.test.ts` (9) - Dark/light mode, localStorage
- `useMockData.test.ts` (9) - Mock scenarios, auto-cycle
- `Status.test.ts` (6) - Component rendering, states
- `types.test.ts` (15) - Interfaces, data validation
- `formatters.test.ts` (12) - Number/date formatting
- `logic.test.ts` (16) - Business logic, calculations

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

### Frontend Test Template

```typescript
import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import MyComponent from '../MyComponent.vue';

describe('MyComponent', () => {
  it('should render correctly', () => {
    const wrapper = mount(MyComponent, {
      props: { value: 'test' }
    });
    
    expect(wrapper.text()).toContain('test');
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

#### 1. Tests Workflow (`.github/workflows/tests.yml`)

Runs on every push to `main` and `develop`:

- Setup JDK 21 and run backend tests
- Setup Node.js 20, install dependencies, run frontend tests and coverage
- Perform a full Maven build for integration tests

#### 2. Docker Workflows

**Before** building Docker images:
- ✅ Run backend tests
- ✅ Run frontend tests
- ❌ Abort if any test fails

This ensures only tested code is deployed.