
package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

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