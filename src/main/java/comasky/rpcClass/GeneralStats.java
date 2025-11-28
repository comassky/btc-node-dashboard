package comasky.rpcClass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO pour le bloc des statistiques générales (stats).
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralStats {
    private  int inboundCount;
    private  int outboundCount;
    private  int totalPeers;
}