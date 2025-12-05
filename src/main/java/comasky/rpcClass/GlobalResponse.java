package comasky.rpcClass;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@RegisterForReflection
public class GlobalResponse {
    private GeneralStats generalStats;
    private SubverDistribution subverDistribution;
    private List<PeerInfo> inboundPeer;
    private List<PeerInfo> outboundPeer;
    private BlockchainInfo blockchainInfo;
    private NodeInfo nodeInfo;
    private String upTime;
    private BlockInfo block;
}
