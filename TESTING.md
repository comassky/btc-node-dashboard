# Testing Guide

This document describes the comprehensive test suite for the Bitcoin Node Dashboard project.

## 📊 Test Coverage Overview

The project includes **130 passing tests** across backend and frontend:

| Component | Framework | Tests | Coverage |
|-----------|-----------|-------|----------|
| **Backend** | JUnit 5 + Mockito | 64 tests | Controllers, WebSocket, RPC, Caching |
| **Frontend** | Vitest + Vue Test Utils | 66 tests | Composables, Components, Types, Utils |

## 🧪 Running Tests

### Full Test Suite

Run all tests (backend + frontend) during the Maven build:

```bash
./mvnw clean test
```

This will:
1. Execute 64 backend tests (JUnit)
2. Execute 66 frontend tests (Vitest)
3. Generate test reports

### Backend Tests Only

```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# Specific test class
./mvnw test -Dtest=BtcControllerTest
```

### Frontend Tests Only

```bash
cd src/main/web

# Run all tests once
npm test

# Run tests with coverage
npm run coverage

# Watch mode (re-run on changes)
npm test -- --watch

# UI mode (interactive browser interface)
npm run test:ui
```

## 🔍 Backend Test Suite (64 tests)

### Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **JUnit** | 5.10.5 | Test framework |
| **Mockito** | 5.15.2 | Mocking framework |
| **Quarkus Test** | 3.18.1 | Integration testing |
| **REST Assured** | - | API testing |

### Test Categories

#### 1. REST API Controllers
- **BtcControllerTest** - API endpoints validation
  - Peer data endpoint
  - Error handling
  - Response format validation

#### 2. WebSocket Communication
- **DashboardWebSocketTest** - WebSocket lifecycle
  - Connection management
  - Message broadcasting
  - Error scenarios

- **DashboardWebSocketAdvancedTest** - Complex scenarios
  - Concurrent connections
  - Cache invalidation
  - RPC error propagation

#### 3. RPC Client Integration
- **RpcServicesAdvancedTest** - Bitcoin Core RPC
  - Multi-peer data aggregation
  - Network statistics calculation
  - Error handling (connection failures, invalid responses)

#### 4. Data Transformation & Caching
- **CachedMessageTest** - Cache validation
  - Message validity checks
  - Expiration logic
  - Thread-safety

- **SubverStatsCalculationTest** - Version distribution
  - Percentage calculations
  - Peer grouping by version
  - Edge cases (null values, empty lists)

#### 5. Utilities & Tools
- **ToolsTest** - Helper functions
  - Uptime formatting
  - Hash rate calculations
  - Data transformations

- **BtcApiAppTest** - Application lifecycle
  - Password masking in logs
  - Configuration validation

### Running Specific Backend Tests

```bash
# Single test class
./mvnw test -Dtest=BtcControllerTest

# Single test method
./mvnw test -Dtest=BtcControllerTest#testGetPeers

# Pattern matching
./mvnw test -Dtest=*WebSocket*

# With specific profile
./mvnw test -Dquarkus.test.profile=test
```

## 🎨 Frontend Test Suite (66 tests)

### Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Vitest** | 2.1.9 | Test runner |
| **Vue Test Utils** | 2.4.6 | Component testing |
| **Happy DOM** | 15.11.7 | DOM simulation |
| **@vitest/ui** | 2.1.9 | Interactive test UI |
| **@vitest/coverage-v8** | 2.1.9 | Code coverage |

### Test Categories

#### 1. Composables (18 tests)

**useWebSocket.test.ts** (9 tests)
- ✅ Connection lifecycle (connect, disconnect)
- ✅ Message handling (dashboard data, RPC errors)
- ✅ WebSocket events (close, error)
- ✅ Reconnection with exponential backoff
- ✅ Invalid JSON handling

**useTheme.test.ts** (9 tests)
- ✅ Dark/light mode toggle
- ✅ localStorage persistence
- ✅ DOM class manipulation
- ✅ CSS variable updates
- ✅ prefers-color-scheme detection

#### 2. Components (6 tests)

**Status.test.ts** (6 tests)
- ✅ Component rendering
- ✅ Connected/disconnected states
- ✅ Error message display
- ✅ RPC connection status
- ✅ Props reactivity

#### 3. Types & Validation (15 tests)

**types.test.ts** (15 tests)
- ✅ GeneralStats interface
- ✅ BlockChainInfo interface
- ✅ NodeInfo interface
- ✅ BlockInfo interface
- ✅ Peer interface (with null handling)
- ✅ SubverDistribution interface
- ✅ Data normalization
- ✅ Edge cases

#### 4. Utilities (12 tests)

**formatters.test.ts** (12 tests)
- ✅ Number formatting (commas, decimals)
- ✅ Hash rate calculation (EH/s, PH/s)
- ✅ Date/timestamp formatting
- ✅ Percentage formatting
- ✅ Large number handling

#### 5. Business Logic (15 tests)

**logic.test.ts** (15 tests)
- ✅ WebSocket reconnection logic
- ✅ Data validation functions
- ✅ Peer filtering and grouping
- ✅ Chart data preparation
- ✅ Statistics calculations

### Frontend Test Configuration

**vitest.config.ts**
```typescript
export default defineConfig({
  test: {
    environment: 'happy-dom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: ['node_modules/', 'dist/', '**/*.test.ts']
    }
  }
})
```

### Coverage Report

After running `npm run coverage`, view the detailed HTML report:

```bash
open src/main/web/coverage/index.html
```

Coverage targets:
- **Statements**: >80%
- **Branches**: >80%
- **Functions**: >80%
- **Lines**: >80%

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

### Frontend

```bash
# Run with verbose output
npm test -- --reporter=verbose

# Debug in browser
npm run test:ui

# Run specific test file
npm test -- useWebSocket.test.ts
```

## 📚 Additional Resources

- **Frontend Testing Guide**: [src/main/web/TESTS.md](src/main/web/TESTS.md)
- **JUnit 5 Documentation**: https://junit.org/junit5/docs/current/user-guide/
- **Vitest Documentation**: https://vitest.dev/
- **Vue Test Utils**: https://test-utils.vuejs.org/

## 🎯 Best Practices

### General
- ✅ Write tests before fixing bugs (TDD)
- ✅ Keep tests isolated and independent
- ✅ Use descriptive test names
- ✅ Test edge cases and error scenarios
- ✅ Mock external dependencies

### Backend
- ✅ Use `@InjectMock` for CDI beans
- ✅ Test both success and failure paths
- ✅ Verify interactions with `verify()`
- ✅ Clean up resources in `@AfterEach`

### Frontend
- ✅ Test user interactions, not implementation
- ✅ Use `mount()` for integration tests
- ✅ Mock external APIs and WebSockets
- ✅ Test accessibility (ARIA attributes)
- ✅ Use fake timers for async operations

## 🚀 Performance

### Test Execution Times

| Suite | Tests | Duration |
|-------|-------|----------|
| Backend | 64 | ~15-20s |
| Frontend | 66 | ~2-3s |
| **Total** | **130** | **~20s** |

### Optimization Tips

- Use `@QuarkusTest` instead of `@QuarkusIntegrationTest` when possible
- Mock expensive operations (RPC calls, database queries)
- Run tests in parallel: `./mvnw test -T 1C`
- Use `vi.useFakeTimers()` for time-dependent tests

---

**Questions or issues with tests?** Open an issue on [GitHub](https://github.com/comassky/btc-node-dashboard/issues)
