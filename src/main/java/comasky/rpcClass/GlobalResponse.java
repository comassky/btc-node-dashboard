package comasky.rpcClass;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * Aggregated response containing all dashboard data.
 * Using a record for immutability and conciseness.
 */
@RegisterForReflection
public record GlobalResponse(
    GeneralStats generalStats,
    SubverDistribution subverDistribution,
    List<PeerInfo> inboundPeer,
    List<PeerInfo> outboundPeer,
    BlockchainInfo blockchainInfo,
    NodeInfo nodeInfo,
    String upTime,
    BlockInfo block
) {}