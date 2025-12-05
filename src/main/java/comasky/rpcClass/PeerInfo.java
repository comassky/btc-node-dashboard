package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class PeerInfo {

    // Unique peer identifier
    private int id;

    // Peer IP:Port address
    private String addr;

    // Local peer IP:Port address
    private String addrlocal;

    // Services supported by the peer (e.g., "0000000000000001" for NODE_NETWORK)
    private String services;

    // Connection start Unix epoch timestamp
    private long conntime;

    // Seconds since last send activity
    private long lastsend;

    // Seconds since last receive activity
    private long lastrecv;

    // Bytes sent since last (re)connection
    private long bytesrecv;

    // Bytes received since last (re)connection
    private long bytessent;

    // Bytes sent to this peer (Total)
    @JsonProperty("bytesrecv_per_msg")
    private java.util.Map<String, Long> bytesRecvPerMsg;

    // Bytes received from this peer (Total)
    @JsonProperty("bytessent_per_msg")
    private java.util.Map<String, Long> bytesSentPerMsg;

    // Latency (ping) in seconds
    private double pingtime;

    // Minimum latency (ping) in seconds
    private double minping;

    // Time since last successful ping in seconds
    private long timeoffset;

    // Peer protocol version
    private int version;

    // Subversion (descriptive string)
    private String subver;

    // Indicates if the peer is inbound (true) or outbound (false)
    private boolean inbound;

    // Indicates if the connection is encrypted (e.g., "v2")
    private String transport_protocol;

    // Permission level (0 = default)
    private int permission;

    // Connection status (e.g., "in_flight")
    private String connection_type;

    // Network name (e.g., "ipv4", "ipv6", "onion")
    private String network;

    // Transactions not transmitted by the peer
    @JsonProperty("unshipped_txs")
    private int unshippedTxs;
}