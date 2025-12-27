package comasky.shared;

import io.smallrye.config.ConfigMapping;

/**
 * Configuration mapping for dashboard properties.
 */
@ConfigMapping(prefix = "dashboard")
public interface DashboardConfig {
    /**
     * Polling configuration.
     */
    Polling polling();

    /**
     * Health check configuration.
     */
    Health health();

    /**
     * Cache configuration.
     */
    Cache cache();

    /**
     * Maximum number of concurrent sessions.
     */
    int sessionsMax();

    /**
     * If true, disables mempool info retrieval in the backend.
     */
    boolean disableMempool();

    interface Polling {
        Interval interval();
        interface Interval {
            int seconds();
        }
    }

    interface Health {
        Min min();
        interface Min {
            Outbound outbound();
            interface Outbound {
                int peers();
            }
        }
    }

    interface Cache {
        int validityBufferMs();
        int maxItems();
    }
}
