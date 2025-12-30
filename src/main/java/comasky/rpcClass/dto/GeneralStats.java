package comasky.rpcClass.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO for general peer statistics.
 *
 * @param inboundCount  Number of inbound connections.
 * @param outboundCount Number of outbound connections.
 * @param totalPeers    Total number of connected peers.
 */
@RegisterForReflection
public record GeneralStats(
    int inboundCount,
    int outboundCount,
    int totalPeers
) {}
