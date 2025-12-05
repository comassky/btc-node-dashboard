package comasky.rpcClass;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO for general peer statistics.
 */
@RegisterForReflection
public record GeneralStats(
    int inboundCount,
    int outboundCount,
    int totalPeers
) {}