package comasky.rpcClass.responses;

/**
 * Factory for creating dummy MempoolInfoResponse objects for testing.
 */
public class DummyMempoolInfoResponse {
    /**
     * Creates a dummy MempoolInfoResponse with default values.
     * @return a new MempoolInfoResponse instance
     */
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
