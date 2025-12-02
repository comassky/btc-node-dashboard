package comasky.rpcClass;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubverStats {
    private final String server;
    private final double percentage;
}