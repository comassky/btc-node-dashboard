package comasky.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import comasky.config.DashboardConfig;
import comasky.rpcClass.dto.GlobalResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Provides a configured, high-performance, non-blocking cache for RPC data.
 * The cache duration is dynamically calculated based on the application configuration.
 */
@ApplicationScoped
public class CacheProvider {

    private static final String RPC_DATA_KEY = "rpc-data";
    private static final long MIN_CACHE_DURATION_MS = 100L;
    private static final long MILLIS_PER_SECOND = 1000L;

    private final AsyncCache<String, GlobalResponse> cache;

    @Inject
    public CacheProvider(DashboardConfig config) {
        // Calculate cache duration: polling interval minus the configured buffer
        long pollingIntervalMs = config.polling().seconds() * MILLIS_PER_SECOND;
        long bufferMs = config.cache().validityBufferMs();
        long cacheDurationMs = Math.max(MIN_CACHE_DURATION_MS, pollingIntervalMs - bufferMs);

        // Note: recordStats() is not compatible with buildAsync(), so we build sync cache and wrap it
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(cacheDurationMs))
                .maximumSize(config.cache().maxItems())
                .build()
                .asAsync();
    }

    /**
     * Retrieves data from the cache. If the data is not present, it will be fetched
     * using the provided data supplier, populated into the cache, and then returned.
     *
     * @param dataSupplier A supplier providing a Uni<GlobalResponse> to fetch fresh data.
     * @return A Uni<GlobalResponse> containing either cached or fresh data.
     */
    public Uni<GlobalResponse> getCachedData(Supplier<Uni<GlobalResponse>> dataSupplier) {
        CompletableFuture<GlobalResponse> future = cache.get(RPC_DATA_KEY, (key, executor) ->
                dataSupplier.get().subscribeAsCompletionStage()
        );
        return Uni.createFrom().completionStage(future);
    }

    /**
     * Invalidates all entries in the cache.
     * Useful for testing or forcing a refresh.
     */
    public void invalidateAll() {
        cache.synchronous().invalidateAll();
    }

    /**
     * Returns cache statistics.
     * Note: Stats are not available when using asAsync() without recordStats().
     * This method returns empty stats.
     * @return Uni containing a Map with cache stats
     */
    public Uni<Map<String, Object>> getCacheStats() {
        return Uni.createFrom().item(() -> {
            Map<String, Object> result = new HashMap<>();
            result.put("hitRate", 0.0);
            result.put("missRate", 0.0);
            result.put("hitCount", 0L);
            result.put("missCount", 0L);
            result.put("loadSuccessCount", 0L);
            result.put("loadFailureCount", 0L);
            result.put("totalLoadTime", 0L);
            result.put("averageLoadPenalty", 0.0);
            result.put("evictionCount", 0L);
            return result;
        });
    }
}
