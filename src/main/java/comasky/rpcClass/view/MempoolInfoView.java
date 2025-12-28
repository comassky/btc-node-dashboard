package comasky.rpcClass.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import comasky.rpcClass.responses.MempoolInfoResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * View object for Mempool information, tailored for the dashboard frontend.
 */
@RegisterForReflection
public record MempoolInfoView(
    int size,
    long bytes,
    long usage,
    long maxmempool,
    double mempoolminfee,
    double minrelaytxfee,
    int unbroadcastcount,
    @JsonProperty("total_fee") double totalFee
) {
    public static MempoolInfoView from(MempoolInfoResponse rpc) {
        if (rpc == null) return null;
        return new MempoolInfoView(
            rpc.size(),
            rpc.bytes(),
            rpc.usage(),
            rpc.maxmempool(),
            rpc.mempoolminfee(),
            rpc.minrelaytxfee(),
            rpc.unbroadcastcount(),
            rpc.totalFee()
        );
    }
}
