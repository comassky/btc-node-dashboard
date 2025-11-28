package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeInfo {
    @JsonProperty("protocolversion")
    private int protocolVersion;
    private int version;
    private String subversion;
    private int connections;
    private boolean networkActive;
}