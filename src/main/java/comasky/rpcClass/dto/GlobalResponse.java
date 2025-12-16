package comasky.rpcClass.dto;

import comasky.rpcClass.responses.*;
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
    List<PeerInfoResponse> inboundPeer,
    List<PeerInfoResponse> outboundPeer,
    BlockchainInfoResponse blockchainInfoResponse,
    NetworkInfoResponse nodeInfo,
    String upTime,
    BlockInfoResponse block,
    MempoolInfoResponse mempoolInfo
) {}