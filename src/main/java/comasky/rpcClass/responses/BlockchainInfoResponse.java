
package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

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