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
}
