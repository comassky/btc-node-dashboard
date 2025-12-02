package comasky.rpcClass;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO pour le bloc de distribution des sous-versions (subver_distribution).
 * La map interne utilise la version du client comme clé et un ObjectNode (ou un DTO) comme valeur.
 */
@AllArgsConstructor
@Data
public class SubverDistribution {
    private List<SubverStats> inbound;
    private List<SubverStats> outbound;
}