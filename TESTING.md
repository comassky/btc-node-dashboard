# Testing Guide

**144 automated tests** covering backend and frontend for reliability and stability.

## 📊 Overview

| Component   | Tests | Technologies                        |
|------------ |-------|-------------------------------------|
| Backend     | 78    | JUnit 5, Mockito, Quarkus Test      |
| Frontend    | 66    | Vitest, Vue Test Utils, Happy DOM   |

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

## 🔍 Backend Tests (78)

**Test Classes**:
- `BtcControllerTest` - REST API endpoints
- `DashboardWebSocketTest` - WebSocket lifecycle
- `DashboardWebSocketAdvancedTest` - Concurrent connections, cache
- `RpcServicesTest` - Bitcoin RPC calls
- `RpcServicesAdvancedTest` - Multi-peer aggregation, errors
- `RpcServicesParallelTest` - Parallel execution, CompletableFuture
- `CachedMessageTest` - Cache validation, thread-safety
- `SubverStatsCalculationTest` - Version distribution
- `ToolsTest` - Utility functions
- `BtcApiAppTest` - Application lifecycle

## 🎨 Frontend Tests (66)

**Test Files**:
- `useWebSocket.test.ts` (9) - Connection, messaging, reconnection
- `useTheme.test.ts` (9) - Dark/light mode, localStorage
- `useMockData.test.ts` (9) - Mock scenarios, auto-cycle
- `Status.test.ts` (6) - Component rendering, states
- `types.test.ts` (15) - Interfaces, data validation
- `formatters.test.ts` (12) - Number/date formatting
- `logic.test.ts` (15) - Business logic, calculations

## 🔄 Continuous Integration

### GitHub Actions Workflows

#### 1. Tests Workflow (`.github/workflows/tests.yml`)

Runs on every push to `main` and `develop`:

```yaml
jobs:
  backend-tests:
    - Setup JDK 21
    - Run backend tests
    - Upload test results

  frontend-tests:
    - Setup Node.js 20
    - Install dependencies
    - Run frontend tests
    - Run coverage
    - Upload coverage reports

  integration-tests:
    - Full Maven build
    - Upload artifacts
```

#### 2. Docker Workflows

**Before** building Docker images:
- ✅ Run backend tests
- ✅ Run frontend tests
- ❌ Abort if any test fails

This ensures only tested code is deployed.

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

## 🔄 CI/CD

GitHub Actions runs all tests on every push. Docker images are only built if tests pass.

## 📝 Test Templates

**Backend**:
```java
@QuarkusTest
class MyTest {
    @InjectMock MyDep dep;
    @Inject MyService svc;
    
    @Test void shouldWork() {
        when(dep.method()).thenReturn(value);
        assertEquals(expected, svc.doSomething());
    }
}
```

**Frontend**:
```typescript
import { mount } from '@vue/test-utils';
describe('MyComponent', () => {
  it('renders', () => {
    const wrapper = mount(MyComponent, { props: { val: 'test' } });
    expect(wrapper.text()).toContain('test');
  });
});
```

## 🐛 Debugging

```bash
# Backend verbose
./mvnw test -X

# Frontend UI mode
cd src/main/web && npm run test:ui
```

---

**Test execution**: ~25s total (Backend: ~20s, Frontend: ~3s)