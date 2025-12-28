package comasky.rpcClass.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import comasky.rpcClass.responses.PeerInfoResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * View object for Peer information, tailored for the dashboard frontend.
 */
@RegisterForReflection
public record PeerInfoView(
    int id,
    String addr,
    long conntime,
    long bytesrecv,
    long bytessent,
    double minping,
    long timeoffset,
    int version,
    String subver,
    boolean inbound,
    @JsonProperty("connection_type") String connectionType,
    String network
) {
    public static PeerInfoView from(PeerInfoResponse rpc) {
        if (rpc == null) return null;
        return new PeerInfoView(
            rpc.id(),
            rpc.addr(),
            rpc.conntime(),
            rpc.bytesrecv(),
            rpc.bytessent(),
            rpc.minping(),
            rpc.timeoffset(),
            rpc.version(),
            rpc.subver(),
            rpc.inbound(),
            rpc.connectionType(),
            rpc.network()
        );
    }
}
