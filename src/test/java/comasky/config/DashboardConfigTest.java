package comasky.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DashboardConfig.
 * Tests configuration injection and value validation.
 */
@QuarkusTest
class DashboardConfigTest {

    @Inject
    DashboardConfig config;

    @Test
    void testPollingConfigurationIsValid() {
        // Assert
        assertNotNull(config.polling());
        assertTrue(config.polling().seconds() > 0, "Polling interval must be positive");
        assertTrue(config.polling().seconds() <= 60, "Polling interval should be reasonable (<=60s)");
    }

    @Test
    void testCacheConfigurationIsValid() {
        // Assert
        assertNotNull(config.cache());
        assertTrue(config.cache().validityBufferMs() >= 0, "Cache buffer must be non-negative");
        assertTrue(config.cache().maxItems() > 0, "Max cache items must be positive");
        assertTrue(config.cache().maxItems() <= 1000, "Max cache items should be reasonable");
    }

    @Test
    void testMempoolConfigurationIsValid() {
        // Assert
        assertNotNull(config.mempool());
        // Mempool can be disabled (true/false), just verify it's accessible
        assertNotNull(config.mempool().disable());
    }

    @Test
    void testCacheDurationCalculationIsPositive() {
        // Arrange
        long pollingIntervalMs = config.polling().seconds() * 1000L;
        long bufferMs = config.cache().validityBufferMs();

        // Act
        long cacheDurationMs = Math.max(100, pollingIntervalMs - bufferMs);

        // Assert
        assertTrue(cacheDurationMs >= 100, "Cache duration should be at least 100ms");
        assertTrue(cacheDurationMs <= pollingIntervalMs, "Cache duration should not exceed polling interval");
    }

    @Test
    void testConfigurationInterfacesAreAccessible() {
        // Assert - Test nested configuration interfaces
        assertNotNull(config.polling());
        assertNotNull(config.cache());
        assertNotNull(config.mempool());
    }

    @Test
    void testPollingSecondsConversionToMilliseconds() {
        // Act
        long milliseconds = config.polling().seconds() * 1000L;

        // Assert
        assertTrue(milliseconds >= 1000, "Should be at least 1 second in milliseconds");
    }

    @Test
    void testDefaultConfigurationValues() {
        // Assert - Verify sensible defaults are loaded
        assertTrue(config.polling().seconds() >= 1, "Default polling should be at least 1 second");
        assertTrue(config.cache().validityBufferMs() < 5000, "Buffer should be less than 5 seconds");
        assertTrue(config.cache().maxItems() >= 1, "Should cache at least 1 item");
    }
}
