package comasky.service;

import comasky.config.DashboardConfig;
import comasky.rpcClass.dto.GlobalResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Provides a native-image compatible cache for RPC data.
 * 
 * Uses a simple concurrent map with TTL instead of Caffeine (which generates
 * dynamic classes incompatible with GraalVM native image).
 */
@ApplicationScoped
public class CacheProvider {

    private static final String RPC_DATA_KEY = "rpc-data";
    private static final long MIN_CACHE_DURATION_MS = 100L;
    private static final long MILLIS_PER_SECOND = 1000L;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long cacheDurationMs;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Inject
    public CacheProvider(DashboardConfig config) {
        // Calculate cache duration: polling interval minus the configured buffer
        long pollingIntervalMs = config.polling().seconds() * MILLIS_PER_SECOND;
        long bufferMs = config.cache().validityBufferMs();
        this.cacheDurationMs = Math.max(MIN_CACHE_DURATION_MS, pollingIntervalMs - bufferMs);
    }

    /**
     * Retrieves data from the cache. If the data is not present or expired, 
     * it will be fetched using the provided data supplier.
     *
     * @param dataSupplier A supplier providing a Uni<GlobalResponse> to fetch fresh data.
     * @return A Uni<GlobalResponse> containing either cached or fresh data.
     */
    public Uni<GlobalResponse> getCachedData(Supplier<Uni<GlobalResponse>> dataSupplier) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(RPC_DATA_KEY);
            if (entry != null && !entry.isExpired()) {
                // Return cached future
                return Uni.createFrom().completionStage(entry.future);
            }
        } finally {
            lock.readLock().unlock();
        }

        // Cache miss or expired - fetch new data
        lock.writeLock().lock();
        try {
            // Double-check after acquiring write lock
            CacheEntry entry = cache.get(RPC_DATA_KEY);
            if (entry != null && !entry.isExpired()) {
                return Uni.createFrom().completionStage(entry.future);
            }

            // Compute and cache the result
            CompletableFuture<GlobalResponse> future = dataSupplier.get()
                .subscribeAsCompletionStage();
            cache.put(RPC_DATA_KEY, new CacheEntry(future));
            return Uni.createFrom().completionStage(future);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Invalidates all entries in the cache.
     */
    public void invalidateAll() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the estimated number of entries in the cache.
     */
    public long estimatedSize() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Manually invalidates the RPC data cache entry.
     */
    public void invalidateRpcData() {
        lock.writeLock().lock();
        try {
            cache.remove(RPC_DATA_KEY);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Internal cache entry wrapper with expiration timestamp.
     */
    private class CacheEntry {
        final CompletableFuture<GlobalResponse> future;
        final long expirationTime;

        CacheEntry(CompletableFuture<GlobalResponse> future) {
            this.future = future;
            this.expirationTime = System.currentTimeMillis() + cacheDurationMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
