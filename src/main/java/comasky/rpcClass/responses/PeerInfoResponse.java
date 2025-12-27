package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

/**
 * Response wrapper for the 'getpeerinfo' RPC call.
 * Contains detailed information about each connected peer.
 *
 * @param id                 Peer index.
 * @param addr               The IP address and port of the peer.
 * @param addrlocal          Local address as reported by the peer.
 * @param services           The services offered by the peer.
 * @param conntime           The time the connection was established.
 * @param lastsend           The time of the last send.
 * @param lastrecv           The time of the last receive.
 * @param bytesrecv          The total bytes received.
 * @param bytessent          The total bytes sent.
 * @param bytesRecvPerMsg    Bytes received per message type.
 * @param bytesSentPerMsg    Bytes sent per message type.
 * @param pingtime           The last ping time.
 * @param minping            The minimum observed ping time.
 * @param timeoffset         The time offset in seconds.
 * @param version            The peer's version.
 * @param subver             The string version.
 * @param inbound            True if the connection is inbound.
 * @param transportProtocol  The transport protocol used (e.g., "tcp").
 * @param permission         Permission flags.
 * @param connectionType     Type of connection (e.g., "inbound", "outbound-full-relay").
 * @param network            The network used (e.g., "ipv4", "ipv6", "onion").
 * @param unshippedTxs       Number of transactions not yet sent to this peer.
 */
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
