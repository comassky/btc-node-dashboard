package comasky.rpcClass.view;

import comasky.rpcClass.responses.BlockInfoResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * View object for Block information, tailored for the dashboard frontend.
 */
@RegisterForReflection
public record BlockInfoView(
    long time,
    int nTx
) {
    public static BlockInfoView from(BlockInfoResponse rpc) {
        if (rpc == null) return null;
        return new BlockInfoView(rpc.time(), rpc.ntx());
    }
}
