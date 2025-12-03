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
        return this.executeRpcCall("getnetworkinfo", Collections.emptyList(), NodeInfo.class);
    }

    public String getBestBlockHash() {
        return this.executeRpcCall("getbestblockhash", Collections.emptyList(), String.class);
    }

    /**
     * Récupère les informations détaillées d'un bloc par son hash.
     * La verbosité 1 est utilisée pour obtenir l'objet JSON du bloc.
     * C'est suffisant pour le timestamp (time) et réduit la taille de la réponse
     * par rapport à la verbosité 2 (qui inclut les détails de toutes les transactions).
     * @param blockHash Le hash du bloc.
     * @return Les informations du bloc.
     */
    public BlockInfo getBlockInfo(String blockHash) {
        // Changement de la verbosité de 2 à 1 pour réduire la taille de la réponse RPC.
        List<Object> params = List.of(blockHash, 1);
        return this.executeRpcCall("getblock", params, BlockInfo.class);
    }

    /**
     * Récupère uniquement le timestamp Unix du dernier bloc validé.
     * Ceci combine getbestblockhash et getblock.
     * @return Le timestamp Unix (en secondes) du dernier bloc.
     */
    public long getLastBlockTimestamp() {
        // 1. Obtenir le hash du meilleur bloc
        String bestHash = this.getBestBlockHash();

        // 2. Obtenir toutes les informations du bloc, y compris le timestamp
        BlockInfo blockInfo = this.getBlockInfo(bestHash);

        // 3. Retourner uniquement le timestamp
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
                // Cette ligne utilise la méthode getBlockInfo corrigée
                .blockInfo(this.getBlockInfo(this.getBestBlockHash()))
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

    /**
     * NOUVELLE VERSION : Appelle le client RPC avec une méthode et une liste de paramètres.
     */
    private String callRpc(String method, List<Object> params) {
        java.util.Map<String, Object> rpcRequest = new java.util.HashMap<>();
        rpcRequest.put("jsonrpc", "1.0");
        rpcRequest.put("id", "quarkus-" + method);
        rpcRequest.put("method", method);
        rpcRequest.put("params", params); // Utilise la liste des paramètres

        try {
            return this.rpcClient.executeRpcCall(rpcRequest);
        } catch (Exception e) {
            throw new RpcException("Connection failed for method " + method + ": " + e.getMessage());
        }
    }

    /**
     * NOUVELLE VERSION : Exécute l'appel RPC et désérialise la réponse.
     */
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