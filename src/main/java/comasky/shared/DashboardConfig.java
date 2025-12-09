package comasky.shared;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dashboard")
public interface DashboardConfig {
    
    Polling polling();
    
    Health health();

    Cache cache(); // Add this line
    
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

    interface Cache { // Add this interface
        int validityBufferMs();
    }
}
