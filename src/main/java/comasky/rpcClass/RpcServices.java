package comasky.rpcClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.config.DashboardConfig;
import comasky.exceptions.RpcException;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.dto.SubverStats;
import comasky.rpcClass.responses.*;
import comasky.rpcClass.view.*;
import comasky.service.CacheProvider;
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

/**
 * Service for executing RPC calls to the Bitcoin Core node using a reactive approach with Mutiny.
 * <p>
 * This service orchestrates multiple RPC calls in parallel to optimize response time for dashboard data retrieval.
 * It provides methods to fetch node, block, peer, and blockchain information.
 */
@ApplicationScoped
public class RpcServices implements DashboardDataProvider {
    private static final Logger LOG = Logger.getLogger(RpcServices.class);

    // RPC method names
    private static final String GET_NETWORK_INFO = "getnetworkinfo";
    private static final String GET_BEST_BLOCK_HASH = "getbestblockhash";
    private static final String GET_BLOCK = "getblock";
    private static final String GET_BLOCKCHAIN_INFO = "getblockchaininfo";
    private static final String UPTIME = "uptime";
    private static final String GET_PEER_INFO = "getpeerinfo";
    private static final String GET_MEMPOOL_INFO = "getmempoolinfo";

    // RPC protocol constants
    private static final String JSON_RPC_VERSION = "1.0";
    private static final String REQUEST_ID_PREFIX = "quarkus-";

    // Performance tuning
    private static final int PARALLEL_STREAM_THRESHOLD = 100;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final long NANOS_TO_MILLIS = 1_000_000L;
    private static final long RETRY_DELAY_MS = 50L;
    
    // Pre-built RPC requests (immutable, can be reused)
    private static final List<Object> EMPTY_PARAMS = Collections.emptyList();

    private static final TypeReference<List<PeerInfoResponse>> PEER_INFO_TYPE_REF = new TypeReference<>() {};

    @Inject
    DashboardConfig dashboardConfig;

    @Inject
    CacheProvider cacheProvider;

    private final ObjectMapper objectMapper;
    private final RpcClient rpcClient;

    @Inject
    public RpcServices(ObjectMapper objectMapper, RpcClient rpcClient) {
        this.objectMapper = objectMapper;
        this.rpcClient = rpcClient;
    }

    @Override
    public Uni<GlobalResponse> getData() {
        return cacheProvider.getCachedData(this::fetchFreshData);
    }

    private Uni<GlobalResponse> fetchFreshData() {
        LOG.debug("Fetching fresh data from RPC...");
        final Map<String, String> errors = new HashMap<>();

        final Uni<List<PeerInfoResponse>> peerInfoUni = addErrorHandling(
                executePeerInfoRpcCall(), "peerInfo", errors, Collections::emptyList);

        final Uni<BlockchainInfoResponse> blockchainInfoUni = addErrorHandling(
                getBlockchainInfo(), "blockchainInfo", errors, () -> null);

        final Uni<NetworkInfoResponse> nodeInfoUni = addErrorHandling(
                getNetworkInfo(), "networkInfo", errors, () -> null);

        final Uni<Long> uptimeUni = addErrorHandling(
                getUptimeSeconds(), "uptime", errors, () -> 0L);

        final Uni<BlockInfoResponse> blockInfoUni = addErrorHandling(getBestBlockHash(), "bestBlockHash", errors, () -> null)
                .onItem().transformToUni(hash -> {
                    if (hash == null) {
                        return Uni.createFrom().nullItem();
                    }
                    return addErrorHandling(getBlockInfo(hash), "blockInfo", errors, () -> null);
                });

        final Uni<MempoolInfoResponse> mempoolInfoResponse;
        if (dashboardConfig != null && dashboardConfig.mempool().disable()) {
            mempoolInfoResponse = Uni.createFrom().nullItem();
        } else {
            mempoolInfoResponse = addErrorHandling(getMempoolInfo(), "mempoolInfo", errors, () -> null);
        }

        return Uni.combine().all().unis(peerInfoUni, blockchainInfoUni, nodeInfoUni, uptimeUni, blockInfoUni, mempoolInfoResponse)
                .asTuple()
                .onItem().transform(tuple -> buildGlobalResponseFromTuple(tuple, errors));
    }

    public Uni<NetworkInfoResponse> getNetworkInfo() {
        return callRpcNoParams(GET_NETWORK_INFO, NetworkInfoResponse.class);
    }

    public Uni<String> getBestBlockHash() {
        return callRpcNoParams(GET_BEST_BLOCK_HASH, String.class);
    }

    public Uni<BlockInfoResponse> getBlockInfo(String blockHash) {
        return callRpcTyped(GET_BLOCK, List.of(blockHash, 1), BlockInfoResponse.class);
    }

    public Uni<BlockchainInfoResponse> getBlockchainInfo() {
        return callRpcNoParams(GET_BLOCKCHAIN_INFO, BlockchainInfoResponse.class);
    }

    public Uni<MempoolInfoResponse> getMempoolInfo() {
        return callRpcNoParams(GET_MEMPOOL_INFO, MempoolInfoResponse.class);
    }

    public Uni<Long> getUptimeSeconds() {
        return callRpcNoParams(UPTIME, Long.class);
    }

    /**
     * Helper method for RPC calls without parameters.
     */
    private <T> Uni<T> callRpcNoParams(String method, Class<T> type) {
        return callRpcTyped(method, EMPTY_PARAMS, type);
    }

    private <T> Uni<T> addErrorHandling(Uni<T> uni, String callName, Map<String, String> errors, Supplier<T> defaultValueSupplier) {
        return uni.onFailure().retry()
                .withBackOff(java.time.Duration.ofMillis(RETRY_DELAY_MS))
                .atMost(MAX_RETRY_ATTEMPTS)
                .onFailure().invoke(e -> recordError(callName, e, errors))
                .onFailure().recoverWithItem(defaultValueSupplier);
    }

    /**
     * Records an error in the errors map with appropriate logging.
     */
    private void recordError(String callName, Throwable error, Map<String, String> errors) {
        String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
        LOG.warnf("%s RPC failed: %s", callName, errorMessage);
        errors.put(callName, errorMessage);
    }

    private GlobalResponse buildGlobalResponseFromTuple(Tuple6<List<PeerInfoResponse>, BlockchainInfoResponse, NetworkInfoResponse, Long, BlockInfoResponse, MempoolInfoResponse> tuple, Map<String, String> errors) {
        List<PeerInfoResponse> allPeers = tuple.getItem1();
        var peersByType = partitionPeersByDirection(allPeers);
        List<PeerInfoResponse> inboundPeers = peersByType.get(true);
        List<PeerInfoResponse> outboundPeers = peersByType.get(false);

        var stats = new SubverDistribution(calculateSubverStats(inboundPeers), calculateSubverStats(outboundPeers));
        var generalStat = new GeneralStats(inboundPeers.size(), outboundPeers.size(), allPeers.size());

        // Map RPC responses to View objects - use parallel streams for larger datasets
        return new GlobalResponse(
            generalStat,
            stats,
            mapPeersToView(inboundPeers),
            mapPeersToView(outboundPeers),
            BlockchainInfoView.from(tuple.getItem2()),
            NetworkInfoView.from(tuple.getItem3()),
            tuple.getItem4(),
            BlockInfoView.from(tuple.getItem5()),
            MempoolInfoView.from(tuple.getItem6()),
            errors
        );
    }

    /**
     * Partitions peers into inbound and outbound lists.
     */
    private Map<Boolean, List<PeerInfoResponse>> partitionPeersByDirection(List<PeerInfoResponse> peers) {
        return peers.stream().collect(Collectors.partitioningBy(PeerInfoResponse::inbound));
    }

    private Uni<List<PeerInfoResponse>> executePeerInfoRpcCall() {
        return callRpcTyped(GET_PEER_INFO, EMPTY_PARAMS, PEER_INFO_TYPE_REF);
    }

    private List<PeerInfoView> mapPeersToView(List<PeerInfoResponse> peers) {
        return (peers.size() > PARALLEL_STREAM_THRESHOLD ? peers.parallelStream() : peers.stream())
                .map(PeerInfoView::from)
                .toList();
    }

    private List<SubverStats> calculateSubverStats(List<PeerInfoResponse> peers) {
        if (peers.isEmpty()) {
            return Collections.emptyList();
        }
        final double totalPeers = peers.size();
        final var stream = peers.size() > PARALLEL_STREAM_THRESHOLD ? peers.parallelStream() : peers.stream();
        
        // Pre-filter to avoid processing nulls in grouping
        return stream
                .filter(p -> p.subver() != null)
                .collect(Collectors.groupingByConcurrent(
                    PeerInfoResponse::subver, 
                    Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> new SubverStats(entry.getKey(), calculatePercentage(entry.getValue(), totalPeers)))
                .toList();
    }

    /**
     * Calculates percentage rounded to 2 decimal places.
     */
    private double calculatePercentage(long count, double total) {
        return Math.round((count / total) * 10000.0) / 100.0;
    }

    private <T> Uni<T> callRpcTyped(String method, List<Object> params, Class<T> type) {
        var resultType = objectMapper.getTypeFactory().constructType(type);
        return callRpcInternal(method, params, resultType);
    }

    private <T> Uni<T> callRpcTyped(String method, List<Object> params, TypeReference<T> type) {
        var resultType = objectMapper.getTypeFactory().constructType(type);
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
                    long durationMs = (System.nanoTime() - start) / NANOS_TO_MILLIS;
                    LOG.debugf("RPC '%s' executed in %d ms", method, durationMs);
                }
                return rpcResponse.getResult();
            } catch (RpcException e) {
                throw e; // Re-throw RPC exceptions as-is
            } catch (Exception e) {
                long durationMs = (System.nanoTime() - start) / NANOS_TO_MILLIS;
                LOG.errorf(e, "RPC '%s' failed after %d ms", method, durationMs);
                throw new RpcException("Connection failed for method " + method + ": " + e.getMessage(), e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
