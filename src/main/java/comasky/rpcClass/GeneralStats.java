package comasky.rpcClass;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO pour le bloc des statistiques générales (stats).
 */
@Data
@AllArgsConstructor
public class GeneralStats {
    private  int inboundCount;
    private  int outboundCount;
    private  int totalPeers;
}