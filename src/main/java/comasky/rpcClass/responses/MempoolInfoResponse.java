package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Java record representing the response of the RPC command 'getmempoolinfo'.
 * Provides statistics about the current state of the node's mempool (unconfirmed transactions).
 */
public record MempoolInfoResponse(
        @JsonProperty("loaded")
        boolean loaded,

        @JsonProperty("size")
        int size,

        @JsonProperty("bytes")
        long bytes,

        @JsonProperty("usage")
        long usage,

        @JsonProperty("maxmempool")
        long maxmempool,

        @JsonProperty("mempoolminfee")
        double mempoolminfee,

        @JsonProperty("minrelaytxfee")
        double minrelaytxfee,

        @JsonProperty("unbroadcastcount")
        int unbroadcastcount,

        @JsonProperty("total_fee")
        double totalFee
) {}
