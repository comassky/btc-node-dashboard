package comasky.rpcClass.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * View object for Blockchain information, tailored for the dashboard frontend.
 */
@RegisterForReflection
public record BlockchainInfoView(
    String chain,
    int blocks,
    int headers,
    double difficulty,
    long time,
    long mediantime,
    double verificationprogress,
    boolean initialblockdownload,
    String chainwork,
    @JsonProperty("size_on_disk") long sizeOnDisk
) {
    public static BlockchainInfoView from(BlockchainInfoResponse rpc) {
        if (rpc == null) return null;
        return new BlockchainInfoView(
            rpc.chain(),
            rpc.blocks(),
            rpc.headers(),
            rpc.difficulty(),
            rpc.time(),
            rpc.mediantime(),
            rpc.verificationprogress(),
            rpc.initialblockdownload(),
            rpc.chainwork(),
            rpc.size_on_disk()
        );
    }
}
