package comasky.rpcClass;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GlobalResponse {
    private GeneralStats generalStats;
    private SubverDistribution subverDistribution;
    private List<PeerInfo> inboundPeer;
    private List<PeerInfo> outboundPeer;
    private BlockchainInfo blockchainInfo;
    private NodeInfo nodeInfo;
    private String upTime;
}
