package comasky.shared;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dashboard")
public interface DashboardConfig {
    
    Polling polling();
    
    Health health();
    
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
}
