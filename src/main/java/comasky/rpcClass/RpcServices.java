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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
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
public class RpcServices implements DashboardDataProvider {
    private static final Logger LOG = Logger.getLogger(RpcServices.class);

    private static final String GET_NETWORK_INFO = "getnetworkinfo";
    private static final String GET_BEST_BLOCK_HASH = "getbestblockhash";
    private static final String GET_BLOCK = "getblock";
    private static final String GET_BLOCKCHAIN_INFO = "getblockchaininfo";
    private static final String UPTIME = "uptime";
    private static final String GET_PEER_INFO = "getpeerinfo";
    private static final String GET_MEMPOOL_INFO = "getmempoolinfo";

    private static final String JSON_RPC_VERSION = "1.0";
    private static final String REQUEST_ID_PREFIX = "quarkus-";

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

    public Uni<NetworkInfoResponse> getNetworkInfo() {
        return callRpcTyped(GET_NETWORK_INFO, Collections.emptyList(), NetworkInfoResponse.class);
    }

    public Uni<String> getBestBlockHash() {
        return callRpcTyped(GET_BEST_BLOCK_HASH, Collections.emptyList(), String.class);
    }

    public Uni<BlockInfoResponse> getBlockInfo(String blockHash) {
        List<Object> params = List.of(blockHash, 1);
        return callRpcTyped(GET_BLOCK, params, BlockInfoResponse.class);
    }

    public Uni<BlockchainInfoResponse> getBlockchainInfo() {
        return callRpcTyped(GET_BLOCKCHAIN_INFO, Collections.emptyList(), BlockchainInfoResponse.class);
    }

    public Uni<MempoolInfoResponse> getMempoolInfo() {
        return callRpcTyped(GET_MEMPOOL_INFO, Collections.emptyList(), MempoolInfoResponse.class);
    }

    public Uni<Long> getUptimeSeconds() {
        return callRpcTyped(UPTIME, Collections.emptyList(), Long.class);
    }

    @Override
    public Uni<GlobalResponse> getData() {
        Map<String, String> errors = new HashMap<>();

        Uni<List<PeerInfoResponse>> peerInfoUni = addErrorHandling(
                executePeerInfoRpcCall(), "peerInfo", errors, Collections::emptyList);

        Uni<BlockchainInfoResponse> blockchainInfoUni = addErrorHandling(
                getBlockchainInfo(), "blockchainInfo", errors, () -> null);

        Uni<NetworkInfoResponse> nodeInfoUni = addErrorHandling(
                getNetworkInfo(), "networkInfo", errors, () -> null);

        Uni<Long> uptimeUni = addErrorHandling(
                getUptimeSeconds(), "uptime", errors, () -> 0L);

        Uni<BlockInfoResponse> blockInfoUni = addErrorHandling(getBestBlockHash(), "bestBlockHash", errors, () -> null)
                .onItem().transformToUni(hash -> {
                    if (hash == null) {
                        return Uni.createFrom().nullItem();
                    }
                    return addErrorHandling(getBlockInfo(hash), "blockInfo", errors, () -> null);
                });

        Uni<MempoolInfoResponse> mempoolInfoResponse;
        if (dashboardConfig != null && dashboardConfig.disableMempool()) {
            mempoolInfoResponse = Uni.createFrom().nullItem();
        } else {
            mempoolInfoResponse = addErrorHandling(getMempoolInfo(), "mempoolInfo", errors, () -> null);
        }

        return Uni.combine().all().unis(peerInfoUni, blockchainInfoUni, nodeInfoUni, uptimeUni, blockInfoUni, mempoolInfoResponse)
                .asTuple()
                .onItem().transform(tuple -> buildGlobalResponseFromTuple(tuple, errors));
    }

    private <T> Uni<T> addErrorHandling(Uni<T> uni, String callName, Map<String, String> errors, Supplier<T> defaultValueSupplier) {
        return uni.onFailure().retry().atMost(3)
                .onFailure().invoke(e -> {
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    LOG.warnf("%s RPC failed: %s", callName, errorMessage);
                    errors.put(callName, errorMessage);
                })
                .onFailure().recoverWithItem(defaultValueSupplier);
    }

    private GlobalResponse buildGlobalResponseFromTuple(Tuple6<List<PeerInfoResponse>, BlockchainInfoResponse, NetworkInfoResponse, Long, BlockInfoResponse, MempoolInfoResponse> tuple, Map<String, String> errors) {
        List<PeerInfoResponse> allPeers = tuple.getItem1();
        BlockchainInfoResponse blockchainInfoResponse = tuple.getItem2();
        NetworkInfoResponse nodeInfo = tuple.getItem3();
        long uptime = tuple.getItem4();
        BlockInfoResponse blockInfoResponse = tuple.getItem5();
        MempoolInfoResponse mempoolInfoResponse = tuple.getItem6();

        var peersByType = allPeers.stream()
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
                mempoolInfoResponse,
                errors
        );
    }

    private Uni<List<PeerInfoResponse>> executePeerInfoRpcCall() {
        return callRpcTyped(GET_PEER_INFO, Collections.emptyList(), PEER_INFO_TYPE_REF);
    }

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

    private <T> Uni<T> callRpcTyped(String method, List<Object> params, Class<T> type) {
        JavaType resultType = objectMapper.getTypeFactory().constructType(type);
        return callRpcInternal(method, params, resultType);
    }

    private <T> Uni<T> callRpcTyped(String method, List<Object> params, TypeReference<T> type) {
        JavaType resultType = objectMapper.getTypeFactory().constructType(type);
        return callRpcInternal(method, params, resultType);
    }

    private <T> Uni<T> callRpcInternal(String method, List<Object> params, JavaType resultType) {
        final var rpcRequest = new RpcRequestDto(JSON_RPC_VERSION, REQUEST_ID_PREFIX + method, method, params);

        return Uni.createFrom().item(() -> {
            long start = System.nanoTime();
            try {
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
                LOG.errorf(e, "RPC '%s' failed after %d ms: %s", method, durationMs, e.getMessage());
                throw new RpcException("Connection failed for method " + method + ": " + e.getMessage(), e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
