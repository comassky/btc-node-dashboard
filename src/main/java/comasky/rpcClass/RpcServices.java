package comasky.rpcClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.exceptions.RpcException;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.dto.SubverStats;
import comasky.rpcClass.responses.*;
import comasky.shared.DashboardConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.tuples.Tuple6;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static comasky.shared.Tools.formatUptime;

/**
 * Service for executing RPC calls to the Bitcoin Core node using a reactive approach with Mutiny.
 * <p>
 * This service orchestrates multiple RPC calls in parallel to optimize response time for dashboard data retrieval.
 * It provides methods to fetch node, block, peer, and blockchain information.
 */
@ApplicationScoped
public class RpcServices {
    private static final Logger LOG = Logger.getLogger(RpcServices.class);

    
    private static final String GET_NETWORK_INFO = "getnetworkinfo";
    private static final String GET_BEST_BLOCK_HASH = "getbestblockhash";
    private static final String GET_BLOCK = "getblock";
    private static final String GET_BLOCKCHAIN_INFO = "getblockchaininfo";
    private static final String UPTIME = "uptime";
    private static final String GET_PEER_INFO = "getpeerinfo";
    private static final String GET_MEMPOOL_INFO = "getmempoolinfo";

    
    private static final TypeReference<List<PeerInfoResponse>> PEER_INFO_TYPE_REF = new TypeReference<>() {};


    @Inject
    DashboardConfig dashboardConfig;

    private final ObjectMapper objectMapper;
    private final RpcClient rpcClient;

    @Inject
    public RpcServices(ObjectMapper objectMapper, RpcClient rpcClient) {
        this.objectMapper = objectMapper;
        this.rpcClient = rpcClient;
    }

    /**
     * Retrieves network information about the Bitcoin node.
     *
     * @return a {@link Uni} emitting the node network information
     */
    public Uni<NetworkInfoResponse> getNetworkInfo() {
        return callRpcTyped(GET_NETWORK_INFO, Collections.emptyList(), NetworkInfoResponse.class);
    }

    /**
     * Retrieves the hash of the best (most recent) block.
     *
     * @return a {@link Uni} emitting the best block hash as a string
     */
    public Uni<String> getBestBlockHash() {
        return callRpcTyped(GET_BEST_BLOCK_HASH, Collections.emptyList(), String.class);
    }

    /**
     * Retrieves block information for a given block hash with verbosity 1.
     * Verbosity 1 returns block data without full transaction details, reducing response size.
     *
     * @param blockHash the block hash
     * @return a {@link Uni} emitting the block information
     */
    public Uni<BlockInfoResponse> getBlockInfo(String blockHash) {
        List<Object> params = List.of(blockHash, 1);
        return callRpcTyped(GET_BLOCK, params, BlockInfoResponse.class);
    }

    /**
     * Retrieves blockchain information for the Bitcoin node.
     *
     * @return a {@link Uni} emitting the blockchain information
     */
    public Uni<BlockchainInfoResponse> getBlockchainInfo() {
        return callRpcTyped(GET_BLOCKCHAIN_INFO, Collections.emptyList(), BlockchainInfoResponse.class);
    }

    /**
     * Retrieves mempool information from the Bitcoin node.
     *
     * @return a {@link Uni} emitting the mempool information
     */
    public Uni<comasky.rpcClass.responses.MempoolInfoResponse> getMempoolInfo() {
        return callRpcTyped(GET_MEMPOOL_INFO, Collections.emptyList(), comasky.rpcClass.responses.MempoolInfoResponse.class);
    }

    /**
     * Retrieves the node uptime in seconds.
     *
     * @return a {@link Uni} emitting the uptime in seconds
     */
    public Uni<Long> getUptimeSeconds() {
        return callRpcTyped(UPTIME, Collections.emptyList(), Long.class);
    }

    /**
     * Fetches all dashboard data by executing multiple RPC calls in parallel using Mutiny.
     * This method is fully non-blocking and composes multiple Unis to create a final aggregated response.
     *
     * @return a {@link Uni} emitting the aggregated global dashboard response
     */
    public Uni<GlobalResponse> getData() {
        Uni<List<PeerInfoResponse>> peerInfoUni = executePeerInfoRpcCall()
            .onFailure().invoke(e -> LOG.warnf("PeerInfo RPC failed: %s", e.getMessage()))
            .onFailure().recoverWithItem(Collections.emptyList());

        Uni<BlockchainInfoResponse> blockchainInfoUni = getBlockchainInfo()
            .onFailure().invoke(e -> LOG.warnf("BlockchainInfo RPC failed: %s", e.getMessage()))
            .onFailure().recoverWithItem(() -> null);

        Uni<NetworkInfoResponse> nodeInfoUni = getNetworkInfo()
            .onFailure().invoke(e -> LOG.warnf("Network info RPC failed: %s", e.getMessage()))
            .onFailure().recoverWithItem(() -> null);

        Uni<Long> uptimeUni = getUptimeSeconds()
            .onFailure().invoke(e -> LOG.warnf("Uptime RPC failed: %s", e.getMessage()))
            .onFailure().recoverWithItem(() -> 0L);

        Uni<BlockInfoResponse> blockInfoUni = getBestBlockHash()
            .onFailure().invoke(e -> LOG.warnf("BestBlockHash RPC failed: %s", e.getMessage()))
            .onFailure().recoverWithItem(() -> null)
            .onItem().transformToUni(hash -> {
                if (hash == null) return Uni.createFrom().item((BlockInfoResponse) null);
                return getBlockInfo(hash)
                .onFailure().invoke(e -> LOG.warnf("BlockInfo RPC failed: %s", e.getMessage()))
                .onFailure().recoverWithItem(() -> null);
            });

        Uni<MempoolInfoResponse> mempoolInfoResponse;
        if (dashboardConfig != null && dashboardConfig.disableMempool()) {
            mempoolInfoResponse = Uni.createFrom().item((MempoolInfoResponse) null);
        } else {
            mempoolInfoResponse = getMempoolInfo()
                .onFailure().invoke(e -> LOG.warnf("MempoolInfo RPC failed: %s", e.getMessage()))
                .onFailure().recoverWithItem(() -> null);
        }

        return Uni.combine().all().unis(peerInfoUni, blockchainInfoUni, nodeInfoUni, uptimeUni, blockInfoUni, mempoolInfoResponse)
            .asTuple()
            .onItem().transform(this::buildGlobalResponseFromTuple);
    }

    /**
     * Builds a {@link GlobalResponse} object from a tuple of peer, blockchain, node, uptime, and block info.
     *
     * @param tuple a tuple containing all required data
     * @return the aggregated {@link GlobalResponse}
     */
    private GlobalResponse buildGlobalResponseFromTuple(Tuple6<List<PeerInfoResponse>, BlockchainInfoResponse, NetworkInfoResponse, Long, BlockInfoResponse, MempoolInfoResponse> tuple) {
        List<PeerInfoResponse> allPeers = tuple.getItem1();
        BlockchainInfoResponse blockchainInfoResponse = tuple.getItem2();
        NetworkInfoResponse nodeInfo = tuple.getItem3();
        long uptime = tuple.getItem4();
        BlockInfoResponse blockInfoResponse = tuple.getItem5();
        MempoolInfoResponse mempoolInfoResponse = tuple.getItem6();

        var peersByType = allPeers.stream()
                .map(e -> e)
                .collect(Collectors.partitioningBy(PeerInfoResponse::inbound));
        List<PeerInfoResponse> inboundPeers = peersByType.get(true);
        List<PeerInfoResponse> outboundPeers = peersByType.get(false);

        var stats = new SubverDistribution(calculateSubverStats(inboundPeers), calculateSubverStats(outboundPeers));
        var generalStat = new GeneralStats(inboundPeers.size(), outboundPeers.size(), inboundPeers.size() + outboundPeers.size());

        return new GlobalResponse(
                generalStat,
                stats,
                inboundPeers,
                outboundPeers,
                blockchainInfoResponse,
                nodeInfo,
                formatUptime(uptime),
                blockInfoResponse,
                mempoolInfoResponse
        );
    }

    
    /**
     * Executes the peer info RPC call and returns the result as a list of peers.
     *
     * @return a {@link Uni} emitting the list of peers
     */
    private Uni<List<PeerInfoResponse>> executePeerInfoRpcCall() {
        return callRpcTyped(GET_PEER_INFO, Collections.emptyList(), PEER_INFO_TYPE_REF);
    }
    
    /**
     * Calculates subversion distribution statistics with percentages for a list of peers.
     * Uses single-pass stream processing for optimal performance.
     *
     * @param peers the list of peers
     * @return a list of {@link SubverStats} representing the subversion distribution
     */
    private List<SubverStats> calculateSubverStats(List<PeerInfoResponse> peers) {
        if (peers.isEmpty()) {
            return Collections.emptyList();
        }
        final double totalPeers = peers.size();
        Stream<PeerInfoResponse> stream = peers.size() > 1000 ? peers.parallelStream() : peers.stream();
        return stream
                .filter(p -> p.subver() != null)
                .collect(Collectors.groupingBy(PeerInfoResponse::subver, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    double rawPercentage = (entry.getValue() / totalPeers) * 100.0;
                    double roundedPercentage = Math.round(rawPercentage * 100.0) / 100.0;
                    return new SubverStats(entry.getKey(), roundedPercentage);
                })
                .toList();
    }

    /**
     * Centralized method for deserializing and handling errors for RPC calls.
     *
     * @param method the RPC method name
     * @param params the parameters for the RPC call
     * @param type the expected result type (Class or TypeReference)
     * @return a {@link Uni} emitting the typed result or failing with an exception
     */
    private <T> Uni<T> callRpcTyped(String method, List<Object> params, Object type) {
        final var rpcRequest = new RpcRequestDto(
            "1.0",
            "quarkus-" + method,
            method,
            params
        );
        return Uni.createFrom().item(() -> {
            long start = System.nanoTime();
            try {
                final JavaType resultType;
                if (type instanceof Class<?> clazz) {
                    resultType = objectMapper.getTypeFactory().constructType(clazz);
                } else if (type instanceof TypeReference<?> typeRef) {
                    resultType = objectMapper.getTypeFactory().constructType(typeRef);
                } else {
                    throw new IllegalArgumentException("Type must be Class<T> or TypeReference<T>");
                }

                final var rpcResponseType = objectMapper.getTypeFactory().constructParametricType(RpcResponse.class, resultType);
                RpcResponse<T> rpcResponse = objectMapper.readValue(rpcClient.executeRpcCall(rpcRequest), rpcResponseType);

                
                if (rpcResponse.getError() != null) {
                    throw new RpcException("RPC Error for method " + method + ": " + rpcResponse.getError());
                }

                if (LOG.isDebugEnabled()) {
                    long durationMs = (System.nanoTime() - start) / 1_000_000;
                    LOG.debugf("RPC '%s' executed in %d ms", method, durationMs);
                }
                return rpcResponse.getResult();
            } catch (Exception e) {
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                LOG.debugf("RPC '%s' failed after %d ms: %s", method, durationMs, e.getMessage());
                throw new RpcException("Connection failed for method " + method + ": " + e.getMessage(), e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
