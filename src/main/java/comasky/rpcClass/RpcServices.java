package comasky.rpcClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.exceptions.RpcException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.tuples.Tuple5;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static comasky.shared.Tools.formatUptime;

/**
 * Service for executing RPC calls to Bitcoin Core node using a reactive approach with Mutiny.
 * This service orchestrates multiple RPC calls in parallel to optimize response time for dashboard data retrieval.
 */
@ApplicationScoped
public class RpcServices {
    private static final Logger LOG = Logger.getLogger(RpcServices.class);

    // RPC Method Constants
    private static final String GET_NETWORK_INFO = "getnetworkinfo";
    private static final String GET_BEST_BLOCK_HASH = "getbestblockhash";
    private static final String GET_BLOCK = "getblock";
    private static final String GET_BLOCKCHAIN_INFO = "getblockchaininfo";
    private static final String UPTIME = "uptime";
    private static final String GET_PEER_INFO = "getpeerinfo";

    // Updated TypeReference to represent the actual result type, not the RpcResponse wrapper
    private static final TypeReference<List<PeerInfo>> PEER_INFO_TYPE_REF = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final RpcClient rpcClient;

    @Inject
    public RpcServices(ObjectMapper objectMapper, RpcClient rpcClient) {
        this.objectMapper = objectMapper;
        this.rpcClient = rpcClient;
    }

    public Uni<NodeInfo> getNodeInfo() {
        return callRpcTyped(GET_NETWORK_INFO, Collections.emptyList(), NodeInfo.class);
    }

    public Uni<String> getBestBlockHash() {
        return callRpcTyped(GET_BEST_BLOCK_HASH, Collections.emptyList(), String.class);
    }

    /**
     * Retrieves block information by hash with verbosity 1.
     * Verbosity 1 returns block data without full transaction details, reducing response size.
     */
    public Uni<BlockInfo> getBlockInfo(String blockHash) {
        List<Object> params = List.of(blockHash, 1);
        return callRpcTyped(GET_BLOCK, params, BlockInfo.class);
    }

    public Uni<BlockchainInfo> getBlockchainInfo() {
        return callRpcTyped(GET_BLOCKCHAIN_INFO, Collections.emptyList(), BlockchainInfo.class);
    }

    public Uni<Long> getUptimeSeconds() {
        return callRpcTyped(UPTIME, Collections.emptyList(), Long.class); // Changed from long.class to Long.class
    }

    /**
     * Fetches all dashboard data by executing multiple RPC calls in parallel using Mutiny.
     * This method is fully non-blocking and composes multiple Unis to create a final aggregated response.
     *
     * @return Uni<GlobalResponse> containing all aggregated node data.
     */
    public Uni<GlobalResponse> getData() {
        // Launch parallel independent RPC calls
        Uni<List<PeerInfo>> peerInfoUni = executePeerInfoRpcCall();
        Uni<BlockchainInfo> blockchainInfoUni = getBlockchainInfo();
        Uni<NodeInfo> nodeInfoUni = getNodeInfo();
        Uni<Long> uptimeUni = getUptimeSeconds();

        // Chain block hash and block info calls
        Uni<BlockInfo> blockInfoUni = getBestBlockHash()
                .onItem().transformToUni(this::getBlockInfo);

        // Combine all Unis and transform the results into the final GlobalResponse
        return Uni.combine().all().unis(peerInfoUni, blockchainInfoUni, nodeInfoUni, uptimeUni, blockInfoUni)
                .asTuple()
                .onItem().transform(this::buildGlobalResponseFromTuple);
    }

    private GlobalResponse buildGlobalResponseFromTuple(Tuple5<List<PeerInfo>, BlockchainInfo, NodeInfo, Long, BlockInfo> tuple) {
        List<PeerInfo> allPeers = tuple.getItem1();
        BlockchainInfo blockchainInfo = tuple.getItem2();
        NodeInfo nodeInfo = tuple.getItem3();
        long uptime = tuple.getItem4();
        BlockInfo blockInfo = tuple.getItem5();

        var peersByType = allPeers.stream()
                .collect(Collectors.partitioningBy(PeerInfo::isInbound));
        List<PeerInfo> inboundPeers = peersByType.get(true);
        List<PeerInfo> outboundPeers = peersByType.get(false);

        var stats = new SubverDistribution(calculateSubverStats(inboundPeers), calculateSubverStats(outboundPeers));
        var generalStat = new GeneralStats(inboundPeers.size(), outboundPeers.size(), inboundPeers.size() + outboundPeers.size());

        return new GlobalResponse(
                generalStat,
                stats,
                inboundPeers,
                outboundPeers,
                blockchainInfo,
                nodeInfo,
                formatUptime(uptime),
                blockInfo
        );
    }

    // Simplified to directly return Uni<List<PeerInfo>> as callRpcTyped now handles RpcResponse unwrapping
    private Uni<List<PeerInfo>> executePeerInfoRpcCall() {
        return callRpcTyped(GET_PEER_INFO, Collections.emptyList(), PEER_INFO_TYPE_REF);
    }
    
    /**
     * Calculates subversion distribution statistics with percentages.
     * Uses single-pass stream processing for optimal performance.
     */
    private List<SubverStats> calculateSubverStats(List<PeerInfo> peers) {
        if (peers.isEmpty()) {
            return Collections.emptyList();
        }
        final double totalPeers = peers.size();
        // Utilise un stream parallèle pour accélérer le traitement sur de grandes listes
        return peers.parallelStream()
                .filter(p -> p.subver() != null)
                .collect(Collectors.groupingBy(PeerInfo::subver, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    double rawPercentage = (entry.getValue() / totalPeers) * 100.0;
                    double roundedPercentage = Math.round(rawPercentage * 100.0) / 100.0;
                    return new SubverStats(entry.getKey(), roundedPercentage);
                })
                .toList();
    }

    // Refactored to centralize deserialization and error handling
    private <T> Uni<T> callRpcTyped(String method, List<Object> params, Object type) {
        var rpcRequest = new RpcRequestDto(
            "1.0",
            "quarkus-" + method,
            method,
            params
        );
        return Uni.createFrom().item(() -> {
            long start = System.nanoTime();
            try {
                // Execute RPC call to get raw JSON string
                String jsonResponse = rpcClient.executeRpcCall(rpcRequest);

                // Determine the actual type for deserialization (Class<T> or TypeReference<T>)
                JavaType resultType;
                if (type instanceof Class<?> clazz) {
                    resultType = objectMapper.getTypeFactory().constructType(clazz);
                } else if (type instanceof TypeReference<?> typeRef) {
                    resultType = objectMapper.getTypeFactory().constructType(typeRef);
                } else {
                    throw new IllegalArgumentException("Type must be Class<T> or TypeReference<T>");
                }

                // Construct the RpcResponse<T> type and deserialize
                JavaType rpcResponseType = objectMapper.getTypeFactory().constructParametricType(RpcResponse.class, resultType);
                RpcResponse<T> rpcResponse = objectMapper.readValue(jsonResponse, rpcResponseType);

                // Check for RPC errors
                if (rpcResponse.getError() != null) {
                    throw new RpcException("RPC Error for method " + method + ": " + rpcResponse.getError());
                }

                T result = rpcResponse.getResult();

                if (LOG.isDebugEnabled()) {
                    long durationMs = (System.nanoTime() - start) / 1_000_000;
                    LOG.debugf("RPC '%s' executed in %d ms", method, durationMs);
                }
                return result;
            } catch (Exception e) {
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                LOG.debugf("RPC '%s' failed after %d ms: %s", method, durationMs, e.getMessage());
                // Wrap any exception in RpcException for consistent error handling
                throw new RpcException("Connection failed for method " + method + ": " + e.getMessage(), e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
