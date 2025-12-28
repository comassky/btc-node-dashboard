package comasky.service;

import comasky.config.DashboardConfig;
import comasky.rpcClass.dto.GlobalResponse;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Provides a configured, high-performance, non-blocking cache for RPC data.
 * The cache duration is dynamically calculated based on the application configuration.
 */
@ApplicationScoped
public class CacheProvider {

    private final AsyncCache<String, GlobalResponse> cache;
    private static final String RPC_DATA_KEY = "rpc-data";

    @Inject
    public CacheProvider(DashboardConfig config) {
        // Calculate cache duration: polling interval minus the configured buffer.
        long pollingIntervalMs = config.pollingIntervalSeconds() * 1000L;
        long bufferMs = config.cache().validityBufferMs();
        long cacheDurationMs = Math.max(100, pollingIntervalMs - bufferMs); // Ensure at least 100ms

        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(cacheDurationMs))
                .buildAsync();
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
}
