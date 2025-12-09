package comasky.shared;

import io.smallrye.config.ConfigMapping;

/**
 * Configuration mapping for dashboard properties.
 */
@ConfigMapping(prefix = "dashboard")
public interface DashboardConfig {
    Polling polling();
    Health health();
    Cache cache();
    int sessionsMax(); // dashboard.sessions.max

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
        int maxItems(); // dashboard.cache.max-items
    }
}
