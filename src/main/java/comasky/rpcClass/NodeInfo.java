package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Data
public class NodeInfo {
    @JsonProperty("protocolversion")
    private int protocolVersion;
    private int version;
    private String subversion;
    private int connections;
    private boolean networkActive;
}