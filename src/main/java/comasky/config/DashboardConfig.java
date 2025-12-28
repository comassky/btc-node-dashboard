package comasky.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Min;

/**
 * Configuration properties for the dashboard's behavior.
 * The properties are prefixed with "dashboard" in application.properties.
 */
@ConfigMapping(prefix = "dashboard")
public interface DashboardConfig {

    /**
     * The interval in seconds for polling the Bitcoin node for new data.
     */
    @WithDefault("5")
    @Min(1)
    int pollingIntervalSeconds();

    /**
     * The minimum number of outbound peers to maintain.
     * If the number of outbound peers falls below this, a warning is shown.
     */
    @WithDefault("8")
    @Min(1)
    int minOutboundPeers();

    /**
     * Disables mempool data collection if true.
     * Useful for pruned nodes where mempool data can be unreliable.
     */
    @WithDefault("false")
    boolean disableMempool();

    /**
     * The maximum number of concurrent WebSocket sessions allowed.
     */
    @WithDefault("1000")
    @Min(1)
    int sessionsMax();

    /**
     * Configuration for the dashboard data cache.
     */
    CacheConfig cache();

    interface CacheConfig {
        /**
         * Buffer time in milliseconds to subtract from the polling interval
         * to ensure cache validity.
         */
        @WithDefault("100")
        @Min(0)
        int validityBufferMs();

        /**
         * The maximum number of items to hold in the cache.
         */
        @WithDefault("50")
        @Min(1)
        int maxItems();
    }
}
