package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Response wrapper for the 'getblock' RPC call.
 * Contains information about a specific block.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record BlockInfoResponse(
    String hash,
    int confirmations,
    int strippedsize,
    int size,
    int weight,
    int height,
    int version,
    String versionHex,
    String merkleroot,
    long time,
    long mediantime,
    long nonce,
    String bits,
    double difficulty,
    String chainwork,
    @JsonProperty("nTx") int ntx,
    String previousblockhash,
    String nextblockhash
) {}
