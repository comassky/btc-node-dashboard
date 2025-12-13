package comasky.rpcClass.responses;

public class DummyMempoolInfoResponse {
    public static MempoolInfoResponse create() {
        return new MempoolInfoResponse(
            true, // loaded
            0,    // size
            0L,   // bytes
            0L,   // usage
            0L,   // maxmempool
            0.0,  // mempoolminfee
            0.0,  // minrelaytxfee
            0,    // unbroadcastcount
            0.0   // totalFee
        );
    }
}
