package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record PeerInfoResponse(
    int id,
    String addr,
    @JsonIgnore String addrlocal,
    @JsonIgnore String services,
    long conntime,
    @JsonIgnore long lastsend,
    @JsonIgnore long lastrecv,
    long bytesrecv,
    long bytessent,
    @JsonIgnore @JsonProperty("bytesrecv_per_msg") Map<String, Long> bytesRecvPerMsg,
    @JsonIgnore @JsonProperty("bytessent_per_msg") Map<String, Long> bytesSentPerMsg,
    @JsonIgnore double pingtime,
    double minping,
    long timeoffset,
    int version,
    String subver,
    boolean inbound,
    @JsonIgnore @JsonProperty("transport_protocol") String transportProtocol,
    @JsonIgnore int permission,
    @JsonProperty("connection_type") String connectionType,
    String network,
    @JsonIgnore @JsonProperty("unshipped_txs") int unshippedTxs
) {}