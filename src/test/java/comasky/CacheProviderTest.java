package comasky;

import comasky.config.DashboardConfig;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.service.CacheProvider;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CacheProvider service.
 * Tests caching behavior, invalidation, and data freshness.
 */
@QuarkusTest
class CacheProviderTest {

    @Inject
    CacheProvider cacheProvider;

    @Inject
    DashboardConfig config;

    @BeforeEach
    void setup() {
        cacheProvider.invalidateAll();
    }

    @Test
    void testCacheReturnsDataFromSupplierOnFirstCall() {
        // Arrange
        final GlobalResponse expected = createMockGlobalResponse();

        // Act
        final GlobalResponse result = cacheProvider.getCachedData(() -> Uni.createFrom().item(expected))
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(expected.generalStats().totalPeers(), result.generalStats().totalPeers());
    }

    @Test
    void testCacheReturnsDataFromCacheOnSubsequentCalls() {
        // Arrange
        final AtomicInteger callCount = new AtomicInteger(0);
        final GlobalResponse mockData = createMockGlobalResponse();

        // Act - First call
        cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            return Uni.createFrom().item(mockData);
        }).await().indefinitely();

        // Act - Second call (should be cached)
        cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            return Uni.createFrom().item(mockData);
        }).await().indefinitely();

        // Assert - Supplier should only be called once
        assertEquals(1, callCount.get(), "Supplier should only be called once, second call should use cache");
    }

    @Test
    void testInvalidateAllClearsCacheAndForcesRefresh() {
        // Arrange
        final AtomicInteger callCount = new AtomicInteger(0);
        final GlobalResponse mockData = createMockGlobalResponse();

        // Act - First call
        cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            return Uni.createFrom().item(mockData);
        }).await().indefinitely();

        // Invalidate cache
        cacheProvider.invalidateAll();

        // Act - Second call after invalidation (should call supplier again)
        cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            return Uni.createFrom().item(mockData);
        }).await().indefinitely();

        // Assert - Supplier should be called twice
        assertEquals(2, callCount.get(), "Supplier should be called twice after cache invalidation");
    }

    @Test
    void testCacheHandlesNullResponseGracefully() {
        // Act
        final GlobalResponse result = cacheProvider.getCachedData(() -> Uni.createFrom().nullItem())
                .await().indefinitely();

        // Assert
        assertNull(result, "Cache should handle null responses");
    }

    @Test
    void testCacheExpirationAfterConfiguredDuration() throws InterruptedException {
        // Arrange
        final AtomicInteger callCount = new AtomicInteger(0);
        final GlobalResponse mockData = createMockGlobalResponse();
        
        // Get cache duration from config
        long pollingIntervalMs = config.polling().seconds() * 1000L;
        long bufferMs = config.cache().validityBufferMs();
        long cacheDurationMs = Math.max(100, pollingIntervalMs - bufferMs);

        // Act - First call
        cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            return Uni.createFrom().item(mockData);
        }).await().indefinitely();

        // Wait for cache to expire
        Thread.sleep(cacheDurationMs + 100);

        // Act - Second call after expiration
        cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            return Uni.createFrom().item(mockData);
        }).await().indefinitely();

        // Assert - Supplier should be called twice (once initially, once after expiration)
        assertEquals(2, callCount.get(), "Supplier should be called twice after cache expiration");
    }

    @Test
    void testCacheHandlesSupplierFailureGracefully() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cacheProvider.getCachedData(() -> Uni.createFrom().failure(new RuntimeException("Test failure")))
                    .await().indefinitely();
        }, "Cache should propagate supplier failures");
    }

    @Test
    void testMultipleConcurrentCallsOnlyTriggerOneSupplierInvocation() throws InterruptedException {
        // Arrange
        final AtomicInteger callCount = new AtomicInteger(0);
        final GlobalResponse mockData = createMockGlobalResponse();

        // Act - Multiple concurrent calls
        final Uni<GlobalResponse> call1 = cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            try {
                Thread.sleep(100); // Simulate slow operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Uni.createFrom().item(mockData);
        });

        final Uni<GlobalResponse> call2 = cacheProvider.getCachedData(() -> {
            callCount.incrementAndGet();
            return Uni.createFrom().item(mockData);
        });

        // Wait for both to complete
        call1.await().indefinitely();
        call2.await().indefinitely();

        // Assert - Supplier should only be called once despite concurrent calls
        assertEquals(1, callCount.get(), "Concurrent calls should only trigger one supplier invocation");
    }

    private GlobalResponse createMockGlobalResponse() {
        final GeneralStats stats = new GeneralStats(5, 10, 15);
        final SubverDistribution distribution = new SubverDistribution(Collections.emptyList(), Collections.emptyList());
        
        return new GlobalResponse(
            stats,
            distribution,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            null,
            100L,
            null,
            null,
            new HashMap<>()
        );
    }
}
