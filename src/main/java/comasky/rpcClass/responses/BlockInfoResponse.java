package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Response wrapper for the 'getblock' RPC call.
 * Contains information about a specific block.
 *
 * @param hash              The block hash.
 * @param confirmations     The number of confirmations.
 * @param strippedsize      The size of the block excluding witness data.
 * @param size              The size of the block.
 * @param weight            The weight of the block (BIP 141).
 * @param height            The block height or index.
 * @param version           The block version.
 * @param versionHex        The block version formatted in hexadecimal.
 * @param merkleroot        The merkle root.
 * @param time              The block time in seconds since epoch (Jan 1 1970 GMT).
 * @param mediantime        The median block time in seconds since epoch.
 * @param nonce             The nonce.
 * @param bits              The bits.
 * @param difficulty        The difficulty.
 * @param chainwork         Expected number of hashes required to produce the chain up to this block (in hex).
 * @param ntx               The number of transactions in the block.
 * @param previousblockhash The hash of the previous block.
 * @param nextblockhash     The hash of the next block.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record BlockInfoResponse(
    @JsonIgnore String hash,
    @JsonIgnore int confirmations,
    @JsonIgnore int strippedsize,
    @JsonIgnore int size,
    @JsonIgnore int weight,
    @JsonIgnore int height,
    @JsonIgnore int version,
    @JsonIgnore String versionHex,
    @JsonIgnore String merkleroot,
    long time,
    @JsonIgnore long mediantime,
    @JsonIgnore long nonce,
    @JsonIgnore String bits,
    @JsonIgnore double difficulty,
    @JsonIgnore String chainwork,
    @JsonProperty("nTx") int ntx,
    @JsonIgnore String previousblockhash,
    @JsonIgnore String nextblockhash
) {}
