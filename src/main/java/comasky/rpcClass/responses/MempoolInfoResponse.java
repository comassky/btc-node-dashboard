package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Java record representing the response of the RPC command 'getmempoolinfo'.
 * Provides statistics about the current state of the node's mempool (unconfirmed transactions).
 *
 * @param loaded            True if the mempool is fully loaded after startup/restart.
 * @param size              Total number of transactions in the mempool.
 * @param bytes             Total size (in bytes) of all transactions in the mempool.
 * @param usage             Memory (in bytes) used by the mempool (including indexes).
 * @param maxmempool        Maximum configured size of the mempool (in bytes).
 * @param mempoolminfee     Minimum fee rate (in BTC/kB) for transactions to be accepted into the mempool.
 * @param minrelaytxfee     Minimum fee rate to relay a transaction (in BTC/kB).
 * @param unbroadcastcount  Number of transactions in the mempool not yet broadcast to peers.
 * @param totalFee          Total fees (in BTC) of all transactions in the mempool.
 */
public record MempoolInfoResponse(
        @JsonIgnore @JsonProperty("loaded")
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
