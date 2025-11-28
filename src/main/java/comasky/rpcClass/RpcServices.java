package comasky.rpcClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.exceptions.RpcException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RpcServices {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @RestClient
    private RpcClient rpcClient;

    public long getUptimeSeconds() {

        // 1. Appel RPC pour la commande "uptime"
        String jsonResponse = callRpc("uptime");

        try {
            // Désérialisation complète de l'objet RpcResponse<Integer>
            // La commande 'uptime' retourne directement un entier (le nombre de secondes).
            TypeReference<RpcResponse<Integer>> typeRef = new TypeReference<>() {};

            RpcResponse<Integer> response = objectMapper.readValue(jsonResponse, typeRef);

            if (response.getError() != null) {
                // Erreur RPC lors de l'exécution de la commande
                throw new RpcException("RPC Error calling uptime: " + response.getError().toString());
            }

            if (response.getResult() == null) {
                // Cas où le résultat est null, ce qui ne devrait pas arriver pour 'uptime'
                throw new RpcException("Result of uptime is null.");
            }

            // Le résultat est l'uptime en secondes (casté en long pour une manipulation sûre)
            return response.getResult().longValue();

        } catch (RpcException e) {
            // Relancer l'exception RPC
            throw e;
        } catch (Exception e) {
            // Erreur de parsing ou autre exception inattendue
            throw new RpcException("Parsing error for uptime: " + e.getMessage());
        }
    }

    public GlobalResponse getData() {
        String jsonResponse = callRpc("getpeerinfo");
        List<PeerInfo> allPeers;
        try {
            // Désérialisation complète de l'objet RpcResponse<List<PeerInfo>>
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
                        .build();
    }


    /**
     * Méthode utilitaire pour calculer la distribution des sous-versions (subver) en pourcentages.
     * @param peers La liste de pairs (inbound ou outbound).
     * @return ObjectNode avec les pourcentages par subver et le total_peers_count.
     */
    private List<SubverStats> calculateSubverStats(List<PeerInfo> peers) {

        final long totalPeers = peers.size();

        java.util.Map<String, Long> subverCounts = peers.stream()
                .filter(p -> p.getSubver() != null)
                .collect(Collectors.groupingBy(PeerInfo::getSubver, Collectors.counting()));

        final List<SubverStats> result = new ArrayList<>();
        subverCounts.forEach((subver, count) -> {
            double percentage = (double) count * 100.0 / totalPeers;
            final var stat = SubverStats.builder()
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .server(subver)
                    .build();
            result.add(stat);
        });
        return result;
    }

    // --- Méthode d'Abstraction RPC ---

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
}