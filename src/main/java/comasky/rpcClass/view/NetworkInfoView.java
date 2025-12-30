package comasky.rpcClass.view;

import comasky.rpcClass.responses.LocalAddress;
import comasky.rpcClass.responses.Network;
import comasky.rpcClass.responses.NetworkInfoResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * View object for Network information, tailored for the dashboard frontend.
 */
@RegisterForReflection
public record NetworkInfoView(
    int version,
    String subversion,
    int protocolversion,
    List<Network> networks,
    List<LocalAddress> localaddresses
) {
    public static NetworkInfoView from(NetworkInfoResponse rpc) {
        if (rpc == null) return null;
        return new NetworkInfoView(
            rpc.version(),
            rpc.subversion(),
            rpc.protocolversion(),
            rpc.networks(),
            rpc.localaddresses()
        );
    }
}
