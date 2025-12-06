package comasky.rpcClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.exceptions.RpcException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static comasky.shared.Tools.formatUptime;

/**
 * Service for executing RPC calls to Bitcoin Core node.
 * Uses parallel execution via CompletableFuture to optimize response time.
 * Thread pool of 4 daemon threads handles concurrent RPC operations.
 */
@ApplicationScoped
public class RpcServices {

    private static final TypeReference<RpcResponse<List<PeerInfo>>> PEER_INFO_TYPE_REF = new TypeReference<>() {};
    private static final int PARALLEL_RPC_THREADS = 4;

    private final ObjectMapper objectMapper;
    private final RpcClient rpcClient;
    private final ExecutorService executorService;

    @Inject
    public RpcServices(ObjectMapper objectMapper, RpcClient rpcClient) {
        this.objectMapper = objectMapper;
        this.rpcClient = rpcClient;
        this.executorService = Executors.newFixedThreadPool(PARALLEL_RPC_THREADS, r -> {
            Thread thread = new Thread(r, "rpc-parallel-call");
            thread.setDaemon(true);
            return thread;
        });
    }

    public NodeInfo getNodeInfo() {
        return this.executeRpcCall("getnetworkinfo", Collections.emptyList(), NodeInfo.class);
    }

    public String getBestBlockHash() {
        return this.executeRpcCall("getbestblockhash", Collections.emptyList(), String.class);
    }

    /**
     * Retrieves block information by hash with verbosity 1.
     * Verbosity 1 returns block data without full transaction details, reducing response size.
     */
    public BlockInfo getBlockInfo(String blockHash) {
        List<Object> params = List.of(blockHash, 1);
        return this.executeRpcCall("getblock", params, BlockInfo.class);
    }

    /**
     * Returns Unix timestamp (seconds) of the most recent block.
     */
    public long getLastBlockTimestamp() {
        String bestHash = this.getBestBlockHash();
        BlockInfo blockInfo = this.getBlockInfo(bestHash);
        return blockInfo.getTime();
    }

    public BlockchainInfo getBlockchainInfo() {
        return this.executeRpcCall("getblockchaininfo", Collections.emptyList(), BlockchainInfo.class);
    }

    public long getUptimeSeconds() {
        return this.executeRpcCall("uptime", Collections.emptyList(), long.class);
    }

    /**
     * Fetches all dashboard data by executing multiple RPC calls in parallel.
     * Executes 6 independent calls concurrently (getpeerinfo, getblockchaininfo, 
     * getnetworkinfo, uptime, getbestblockhash, getblock) to minimize total latency.
     * Uses functional composition for optimal performance and error handling.
     * 
     * @return GlobalResponse containing all aggregated node data
     * @throws RpcException if any RPC call fails or returns an error
     */
    public GlobalResponse getData() {
        // Launch parallel independent RPC calls
        CompletableFuture<List<PeerInfo>> peerInfoFuture = CompletableFuture.supplyAsync(() -> {
            String jsonResponse = callRpc("getpeerinfo", Collections.emptyList());
            try {
                RpcResponse<List<PeerInfo>> response = objectMapper.readValue(jsonResponse, PEER_INFO_TYPE_REF);
                if (response.getError() != null) {
                    throw new RpcException("RPC Error: " + response.getError());
                }
                return response.getResult();
            } catch (RpcException e) {
                throw e;
            } catch (Exception e) {
                throw new RpcException("Parsing error for peer info: " + e.getMessage());
            }
        }, executorService);

        CompletableFuture<BlockchainInfo> blockchainInfoFuture = 
            CompletableFuture.supplyAsync(this::getBlockchainInfo, executorService);
        
        CompletableFuture<NodeInfo> nodeInfoFuture = 
            CompletableFuture.supplyAsync(this::getNodeInfo, executorService);
        
        CompletableFuture<Long> uptimeFuture = 
            CompletableFuture.supplyAsync(this::getUptimeSeconds, executorService);

        // Chain block hash and block info calls using thenCompose for optimal composition
        CompletableFuture<BlockInfo> blockInfoFuture = 
            CompletableFuture.supplyAsync(this::getBestBlockHash, executorService)
                .thenCompose(bestHash -> 
                    CompletableFuture.supplyAsync(() -> this.getBlockInfo(bestHash), executorService)
                );

        // Combine all futures using functional composition to avoid blocking .get() calls
        CompletableFuture<GlobalResponse> combined = CompletableFuture.allOf(peerInfoFuture, blockchainInfoFuture, nodeInfoFuture, uptimeFuture, blockInfoFuture)
            .thenApply(ignored -> {
                // All futures are guaranteed to be complete here, .join() won't block
                List<PeerInfo> allPeers = peerInfoFuture.join();
                BlockchainInfo blockchainInfo = blockchainInfoFuture.join();
                NodeInfo nodeInfo = nodeInfoFuture.join();
                long uptime = uptimeFuture.join();
                BlockInfo blockInfo = blockInfoFuture.join();

                // Process peer data by type
                var peersByType = allPeers.stream()
                        .collect(Collectors.partitioningBy(PeerInfo::isInbound));
                List<PeerInfo> inboundPeers = peersByType.get(true);
                List<PeerInfo> outboundPeers = peersByType.get(false);

                var stats = new SubverDistribution(calculateSubverStats(inboundPeers), calculateSubverStats(outboundPeers));
                var generalStat = new GeneralStats(inboundPeers.size(), outboundPeers.size(), inboundPeers.size() + outboundPeers.size());

                return GlobalResponse.builder()
                        .generalStats(generalStat)
                        .inboundPeer(inboundPeers)
                        .outboundPeer(outboundPeers)
                        .subverDistribution(stats)
                        .blockchainInfo(blockchainInfo)
                        .nodeInfo(nodeInfo)
                        .upTime(formatUptime(uptime))
                        .block(blockInfo)
                        .build();
            });

        // Unwrap CompletionException to expose the original RpcException
        try {
            return combined.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RpcException) {
                throw (RpcException) cause;
            }
            throw new RpcException("Unexpected error during parallel RPC calls: " + e.getMessage(), cause);
        }
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
        final double scaleFactor = 10000.0 / totalPeers;
        return peers.stream()
                .filter(p -> p.getSubver() != null)
                .collect(Collectors.groupingBy(PeerInfo::getSubver, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> SubverStats.builder()
                        .server(entry.getKey())
                        .percentage(Math.round(entry.getValue() * scaleFactor) / 100.0)
                        .build())
                .toList();
    }

    private String callRpc(String method, List<Object> params) {
        // Map.of is optimized for small maps (< 10 entries)
        var rpcRequest = java.util.Map.of(
            "jsonrpc", "1.0",
            "id", "quarkus-" + method,
            "method", method,
            "params", params
        );

        try {
            return rpcClient.executeRpcCall(rpcRequest);
        } catch (Exception e) {
            throw new RpcException("Connection failed for method " + method + ": " + e.getMessage());
        }
    }

    private <T> T executeRpcCall(String rpcMethod, List<Object> params, Class<T> resultClass) {
        String jsonResponse = callRpc(rpcMethod, params);
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
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException("Parsing error for '" + rpcMethod + "': " + e.getMessage());
        }
    }
}