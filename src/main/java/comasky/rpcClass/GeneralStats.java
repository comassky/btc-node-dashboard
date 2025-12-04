package comasky.rpcClass;

/**
 * DTO for general peer statistics.
 */
public record GeneralStats(
    int inboundCount,
    int outboundCount,
    int totalPeers
) {}