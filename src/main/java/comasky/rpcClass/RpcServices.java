package comasky.rpcClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.exceptions.RpcException;
import org.jboss.logging.Logger;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple5;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import static comasky.shared.Tools.formatUptime;

/**
 * Service for executing RPC calls to Bitcoin Core node using a reactive approach with Mutiny.
 * This service orchestrates multiple RPC calls in parallel to optimize response time for dashboard data retrieval.
 */
@ApplicationScoped
public class RpcServices {
    private static final Logger LOG = Logger.getLogger(RpcServices.class);

    private static final TypeReference<RpcResponse<List<PeerInfo>>> PEER_INFO_TYPE_REF = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final RpcClient rpcClient;

    @Inject
    public RpcServices(ObjectMapper objectMapper, RpcClient rpcClient) {
        this.objectMapper = objectMapper;
        this.rpcClient = rpcClient;
    }

    public Uni<NodeInfo> getNodeInfo() {
        return this.executeRpcCall("getnetworkinfo", Collections.emptyList(), NodeInfo.class);
    }

    public Uni<String> getBestBlockHash() {
        return this.executeRpcCall("getbestblockhash", Collections.emptyList(), String.class);
    }

    /**
     * Retrieves block information by hash with verbosity 1.
     * Verbosity 1 returns block data without full transaction details, reducing response size.
     */
    public Uni<BlockInfo> getBlockInfo(String blockHash) {
        List<Object> params = List.of(blockHash, 1);
        return this.executeRpcCall("getblock", params, BlockInfo.class);
    }

    public Uni<BlockchainInfo> getBlockchainInfo() {
        return this.executeRpcCall("getblockchaininfo", Collections.emptyList(), BlockchainInfo.class);
    }

    public Uni<Long> getUptimeSeconds() {
        return this.executeRpcCall("uptime", Collections.emptyList(), long.class);
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

    private Uni<List<PeerInfo>> executePeerInfoRpcCall() {
        return callRpc("getpeerinfo", Collections.emptyList())
                .onItem().transform(jsonResponse -> {
                    try {
                        RpcResponse<List<PeerInfo>> response = objectMapper.readValue(jsonResponse, PEER_INFO_TYPE_REF);
                        if (response.getError() != null) {
                            throw new RpcException("RPC Error: " + response.getError());
                        }
                        return response.getResult();
                    } catch (Exception e) {
                        throw new RpcException("Parsing error for peer info: " + e.getMessage(), e);
                    }
                });
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

    private Uni<String> callRpc(String method, List<Object> params) {
        var rpcRequest = java.util.Map.of(
                "jsonrpc", "1.0",
                "id", "quarkus-" + method,
                "method", method,
                "params", params
        );

        return Uni.createFrom().item(() -> {
            long start = System.nanoTime();
            try {
                String result = rpcClient.executeRpcCall(rpcRequest);
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                if (LOG.isDebugEnabled()) {
                    LOG.debugf("RPC '%s' executed in %d ms", method, durationMs);
                }
                return result;
            } catch (Exception e) {
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                LOG.debugf("RPC '%s' failed after %d ms: %s", method, durationMs, e.getMessage());
                throw new RpcException("Connection failed for method " + method + ": " + e.getMessage(), e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private <T> Uni<T> executeRpcCall(String rpcMethod, List<Object> params, Class<T> resultClass) {
        return callRpc(rpcMethod, params)
                .onItem().transform(jsonResponse -> {
                    try {
                        JavaType type = objectMapper.getTypeFactory()
                                .constructParametricType(RpcResponse.class, resultClass);
                        RpcResponse<T> response = objectMapper.readValue(jsonResponse, type);

                        if (response.getError() != null) {
                            throw new RpcException("RPC Error calling '" + rpcMethod + "': " + response.getError());
                        }

                        T result = response.getResult();
                        if (result == null) {
                            throw new RpcException("Result of '" + rpcMethod + "' is null.");
                        }
                        return result;
                    } catch (Exception e) {
                        throw new RpcException("Parsing error for '" + rpcMethod + "': " + e.getMessage(), e);
                    }
                });
    }
}