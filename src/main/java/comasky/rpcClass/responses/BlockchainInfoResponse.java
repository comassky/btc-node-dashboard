package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Response wrapper for the 'getblockchaininfo' RPC call.
 * Provides information about the current state of the blockchain.
 *
 * @param chain                 The name of the chain (e.g., "main", "test", "regtest").
 * @param blocks                The current number of blocks in the chain.
 * @param headers               The current number of headers validated.
 * @param bestblockhash         The hash of the best (tip) block in the chain.
 * @param difficulty            The current difficulty.
 * @param time                  The median time of the most recent block.
 * @param mediantime            The median time of the last 11 blocks.
 * @param verificationprogress  Estimate of verification progress [0..1].
 * @param initialblockdownload  True if the node is in Initial Block Download mode.
 * @param chainwork             Total amount of work in active chain, in hexadecimal.
 * @param size_on_disk          Estimated size of the block and undo files on disk.
 * @param pruned                True if the blocks are pruned.
 * @param pruneheight           The lowest block height available if pruned.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record BlockchainInfoResponse(
        @JsonProperty("chain")
        String chain,

        @JsonProperty("blocks")
        int blocks,

        @JsonProperty("headers")
        int headers,

        @JsonIgnore @JsonProperty("bestblockhash")
        String bestblockhash,

        @JsonProperty("difficulty")
        double difficulty,

        @JsonProperty("time")
        long time,

        @JsonProperty("mediantime")
        long mediantime,

        @JsonProperty("verificationprogress")
        double verificationprogress,

        @JsonProperty("initialblockdownload")
        boolean initialblockdownload,

        @JsonProperty("chainwork")
        String chainwork,

        @JsonProperty("size_on_disk")
        long size_on_disk,

        @JsonIgnore @JsonProperty("pruned")
        boolean pruned,

        @JsonIgnore @JsonProperty("pruneheight")
        Long pruneheight
) {}
