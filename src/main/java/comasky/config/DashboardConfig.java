
package comasky.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import jakarta.validation.constraints.Min;

/**
 * Configuration properties for the dashboard's behavior.
 * The properties are prefixed with "dashboard" in application.properties.
 */
@ConfigMapping(prefix = "dashboard")
public interface DashboardConfig {

    PollingConfig polling();
    PeersConfig peers();
    MempoolConfig mempool();
    SessionsConfig sessions();
    CacheConfig cache();

    interface PollingConfig {
        @WithName("interval.seconds")
        @WithDefault("5")
        @Min(1)
        int seconds();
    }

    interface PeersConfig {
        @WithName("min.outbound")
        @WithDefault("8")
        @Min(1)
        int minOutbound();
    }

    interface MempoolConfig {
        @WithName("disable")
        @WithDefault("false")
        boolean disable();
    }

    interface SessionsConfig {
        @WithName("max")
        @WithDefault("1000")
        @Min(1)
        int max();
    }

    interface CacheConfig {
        @WithName("validity.buffer.ms")
        @WithDefault("100")
        @Min(0)
        int validityBufferMs();

        @WithName("max.items")
        @WithDefault("50")
        @Min(1)
        int maxItems();
    }
}
