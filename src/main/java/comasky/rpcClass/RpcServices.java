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
import java.util.stream.Collectors;

import static comasky.shared.Tools.formatUptime;

@ApplicationScoped
public class RpcServices {

    private final ObjectMapper objectMapper;
    private final RpcClient rpcClient;

    @Inject
    public RpcServices(ObjectMapper objectMapper, RpcClient rpcClient) {
        this.objectMapper = objectMapper;
        this.rpcClient = rpcClient;
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

    public GlobalResponse getData() {
        String jsonResponse = callRpc("getpeerinfo", Collections.emptyList());
        List<PeerInfo> allPeers;
        try {
            TypeReference<RpcResponse<List<PeerInfo>>> typeRef = new TypeReference<>() {};
            RpcResponse<List<PeerInfo>> response = objectMapper.readValue(jsonResponse, typeRef);
            if (response.getError() != null) {
                throw new RpcException("RPC Error: " + response.getError());
            }
            allPeers = response.getResult();
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException("Parsing error for peer info: " + e.getMessage());
        }

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
                .blockchainInfo(this.getBlockchainInfo())
                .nodeInfo(this.getNodeInfo())
                .upTime(formatUptime(this.getUptimeSeconds()))
                .block(this.getBlockInfo(this.getBestBlockHash()))
                .build();
    }


    /**
     * Calculates subversion distribution statistics with percentages.
     */
    private List<SubverStats> calculateSubverStats(List<PeerInfo> peers) {
        if (peers.isEmpty()) {
            return Collections.emptyList();
        }
        double totalPeers = peers.size();
        return peers.stream()
                .filter(p -> p.getSubver() != null)
                .collect(Collectors.groupingBy(PeerInfo::getSubver, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> SubverStats.builder()
                        .server(entry.getKey())
                        .percentage(Math.round(entry.getValue() * 10000.0 / totalPeers) / 100.0)
                        .build())
                .toList();
    }

    private String callRpc(String method, List<Object> params) {
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