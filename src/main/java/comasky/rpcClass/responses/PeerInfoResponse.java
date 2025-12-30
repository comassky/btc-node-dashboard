package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

/**
 * Response wrapper for the 'getpeerinfo' RPC call.
 * Contains detailed information about each connected peer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record PeerInfoResponse(
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
) {}
