package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record PeerInfo(
    int id,
    String addr,
    String addrlocal,
    String services,
    long conntime,
    long lastsend,
    long lastrecv,
    long bytesrecv,
    long bytessent,
    @JsonProperty("bytesrecv_per_msg") Map<String, Long> bytesRecvPerMsg,
    @JsonProperty("bytessent_per_msg") Map<String, Long> bytesSentPerMsg,
    double pingtime,
    double minping,
    long timeoffset,
    int version,
    String subver,
    boolean inbound,
    @JsonProperty("transport_protocol") String transportProtocol,
    int permission,
    @JsonProperty("connection_type") String connectionType,
    String network,
    @JsonProperty("unshipped_txs") int unshippedTxs
) {
    
    public boolean isInbound() {
        return inbound;
    }
}