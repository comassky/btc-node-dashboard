package comasky.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import comasky.config.DashboardConfig;
import comasky.rpcClass.dto.GlobalResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Provides a configured, high-performance cache for RPC data.
 *
 * Stores results as {@link CompletableFuture} so callers can easily consume the cached
 * response asynchronously without requiring Caffeine's AsyncCache implementation.
 */
@ApplicationScoped
public class CacheProvider {

    private static final String RPC_DATA_KEY = "rpc-data";
    private static final long MIN_CACHE_DURATION_MS = 100L;
    private static final long MILLIS_PER_SECOND = 1000L;
    

    private final Cache<String, CompletableFuture<GlobalResponse>> cache;

    @Inject
    public CacheProvider(DashboardConfig config) {
        // Calculate cache duration: polling interval minus the configured buffer
        long pollingIntervalMs = config.polling().seconds() * MILLIS_PER_SECOND;
        long bufferMs = config.cache().validityBufferMs();
        long cacheDurationMs = Math.max(MIN_CACHE_DURATION_MS, pollingIntervalMs - bufferMs);

        // Optimized Caffeine cache configuration
        this.cache = Caffeine.newBuilder()
            // Automatic expiration after write
            .expireAfterWrite(Duration.ofMillis(cacheDurationMs))
            // Limit maximum size to prevent unbounded growth
            .maximumSize(config.cache().maxItems())
            // Record cache statistics for monitoring
            .recordStats()
            // Build a synchronous cache (native-image friendly)
            .build();
    }

    /**
     * Retrieves data from the cache. If the data is not present, it will be fetched
     * using the provided data supplier, populated into the cache, and then returned.
     *
     * @param dataSupplier A supplier providing a Uni<GlobalResponse> to fetch fresh data.
     * @return A Uni<GlobalResponse> containing either cached or fresh data.
     */
    public Uni<GlobalResponse> getCachedData(Supplier<Uni<GlobalResponse>> dataSupplier) {
        // Get or compute from cache - efficient non-blocking operation
        CompletableFuture<GlobalResponse> future = cache.get(RPC_DATA_KEY, key ->
            dataSupplier.get().subscribeAsCompletionStage()
        );
        return Uni.createFrom().completionStage(future);
    }

    /**
     * Invalidates all entries in the cache.
     * Useful for testing or forcing a refresh.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * Returns the estimated number of entries in the cache.
     */
    public long estimatedSize() {
        return cache.estimatedSize();
    }

    /**
     * Manually invalidates the RPC data cache entry.
     */
    public void invalidateRpcData() {
        cache.invalidate(RPC_DATA_KEY);
    }
}
