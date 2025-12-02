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

    @Inject
    ObjectMapper objectMapper;

    @Inject
    private RpcClient rpcClient;

    public NodeInfo getNodeInfo() {
        return this.executeRpcCall("getnetworkinfo", NodeInfo.class);
    }

    public BlockchainInfo getBlockchainInfo() {
        return this.executeRpcCall("getblockchaininfo", BlockchainInfo.class);
    }

    public long getUptimeSeconds() {
        return this.executeRpcCall("uptime", long.class);
    }

    public GlobalResponse getData() {
        String jsonResponse = callRpc("getpeerinfo");
        List<PeerInfo> allPeers;
        try {
            TypeReference<RpcResponse<List<PeerInfo>>> typeRef = new TypeReference<>() {};
            RpcResponse<List<PeerInfo>> response = objectMapper.readValue(jsonResponse, typeRef);
            if (response.getError() != null) {
                throw new RpcException("RPC Error: " + response.getError());
            }
            allPeers = response.getResult();

        } catch (Exception e) {
            throw new RpcException("Parsing error for peer info: " + e.getMessage());
        }

        // 2. Séparation des pairs en Inbound et Outbound
        List<PeerInfo> inboundPeers = allPeers.stream()
                .filter(PeerInfo::isInbound)
                .toList();

        List<PeerInfo> outboundPeers = allPeers.stream()
                .filter(p -> !p.isInbound())
                .toList();

        // 3. Calcul des statistiques pour chaque groupe
        final var stats = new SubverDistribution(this.calculateSubverStats(inboundPeers), this.calculateSubverStats(outboundPeers));
        final var generalStat = new GeneralStats(inboundPeers.size(),outboundPeers.size(), inboundPeers.size()+ outboundPeers.size());
        return GlobalResponse.builder()
                .generalStats(generalStat)
                .inboundPeer(inboundPeers)
                .outboundPeer(outboundPeers)
                .subverDistribution(stats)
                .blockchainInfo(this.getBlockchainInfo())
                .nodeInfo(this.getNodeInfo())
                .upTime(formatUptime(this.getUptimeSeconds()))
                .build();
    }


    /**
     * Méthode utilitaire pour calculer la distribution des sous-versions (subver) en pourcentages.
     * @param peers La liste de pairs (inbound ou outbound).
     * @return ObjectNode avec les pourcentages par subver et le total_peers_count.
     */
    private List<SubverStats> calculateSubverStats(List<PeerInfo> peers) {
        if (peers.isEmpty()) {
            return Collections.emptyList();
        }
        final double totalPeers = peers.size();
        return peers.stream()
                .filter(p -> p.getSubver() != null)
                .collect(Collectors.groupingBy(PeerInfo::getSubver, Collectors.counting()))
                .entrySet().stream()
                .map(e -> SubverStats.builder()
                        .server(e.getKey())
                        .percentage(Math.round(e.getValue() * 10000.0 / totalPeers) / 100.0)
                        .build())
                .collect(Collectors.toList());
    }

    private String callRpc(String method) {
        java.util.Map<String, Object> rpcRequest = new java.util.HashMap<>();
        rpcRequest.put("jsonrpc", "1.0");
        rpcRequest.put("id", "quarkus-" + method);
        rpcRequest.put("method", method);
        rpcRequest.put("params", Collections.emptyList());

        try {
            return this.rpcClient.executeRpcCall(rpcRequest);
        } catch (Exception e) {
            throw new RpcException("Connection failed for method " + method + ": " + e.getMessage());
        }
    }

    private <T> T executeRpcCall(String rpcCommand, Class<T> resultClass) {
        String jsonResponse = callRpc(rpcCommand);
        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(RpcResponse.class, resultClass);
            RpcResponse<T> response = objectMapper.readValue(jsonResponse, type);

            if (response.getError() != null) {
                throw new RpcException("RPC Error calling '" + rpcCommand + "': " + response.getError());
            }

            T result = response.getResult();
            if (result == null) {
                throw new RpcException("Result of '" + rpcCommand + "' is null.");
            }

            return result;
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException("Parsing error for '" + rpcCommand + "': " + e.getMessage());
        }
    }
}