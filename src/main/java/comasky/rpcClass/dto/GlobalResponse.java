package comasky.rpcClass.dto;

import comasky.rpcClass.responses.*;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.Map;

/**
 * Aggregated response containing all dashboard data.
 * Using a record for immutability and conciseness.
 *
 * @param generalStats           General statistics about peers.
 * @param subverDistribution     Distribution of peer versions.
 * @param inboundPeer            List of inbound peers.
 * @param outboundPeer           List of outbound peers.
 * @param blockchainInfoResponse Information about the blockchain state.
 * @param nodeInfo               Information about the node and network.
 * @param upTime                 Node uptime formatted as a string.
 * @param block                  Information about the latest block.
 * @param mempoolInfo            Information about the mempool.
 * @param errors                 Map of partial errors (e.g., "mempool" -> "Connection refused").
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
    MempoolInfoResponse mempoolInfo,
    Map<String, String> errors
) {}
