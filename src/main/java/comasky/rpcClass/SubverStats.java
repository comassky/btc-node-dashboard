package comasky.rpcClass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SubverStats {
    private final String server;
    private final double percentage;
}