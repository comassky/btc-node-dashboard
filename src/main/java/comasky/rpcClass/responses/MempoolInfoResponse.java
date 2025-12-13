package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Java record representing the response of the RPC command 'getmempoolinfo'.
 * Provides statistics on the current state of the mempool of unconfirmed transactions of the node.
 */
public record MempoolInfoResponse(
        // Information about transactions in the mempool
        @JsonIgnore @JsonProperty("loaded")
        boolean loaded, // True if the mempool is fully loaded after startup/restart

        @JsonProperty("size")
        int size,       // Total number of transactions in the mempool

        @JsonProperty("bytes")
        long bytes,     // Total size (in bytes) of all transactions in the mempool

        @JsonProperty("usage")
        long usage,     // Memory (in bytes) used by the mempool (including indexes)

        @JsonProperty("maxmempool")
        long maxmempool, // Maximum configured size of the mempool (in bytes)

        @JsonProperty("mempoolminfee")
        double mempoolminfee, // Minimum fee rate (in BTC/kB) for transactions to be accepted in the mempool

        @JsonProperty("minrelaytxfee")
        double minrelaytxfee, // Minimum fee rate to relay a transaction (in BTC/kB)

        // Replace-by-fee (RBF) counters
        @JsonProperty("unbroadcastcount")
        int unbroadcastcount, // Number of transactions in the mempool not yet broadcast to peers

        @JsonProperty("total_fee")
        double totalFee // Total fees (in BTC) of all transactions in the mempool
) {}