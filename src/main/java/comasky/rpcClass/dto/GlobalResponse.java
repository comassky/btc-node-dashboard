package comasky.rpcClass.dto;

import comasky.rpcClass.view.*;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.Map;

/**
 * Aggregated response containing all dashboard data, tailored for the frontend.
 * Using a record for immutability and conciseness.
 */
@RegisterForReflection
public record GlobalResponse(
    GeneralStats generalStats,
    SubverDistribution subverDistribution,
    List<PeerInfoView> inboundPeer,
    List<PeerInfoView> outboundPeer,
    BlockchainInfoView blockchainInfoResponse,
    NetworkInfoView nodeInfo,
    long upTime,
    BlockInfoView block,
    MempoolInfoView mempoolInfo,
    Map<String, String> errors
) {}
