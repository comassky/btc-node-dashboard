package comasky.rpcClass;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RegisterForReflection
public class SubverStats {
    private final String server;
    private final double percentage;
}