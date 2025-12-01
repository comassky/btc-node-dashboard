package comasky.rpcClass;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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
