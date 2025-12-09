
package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record BlockInfo(
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