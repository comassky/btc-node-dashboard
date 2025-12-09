package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public record BlockchainInfo(
    int blocks,
    int headers,
    String chain,
    @JsonProperty("verificationprogress") double verificationProgress,
    @JsonProperty("initialblockdownload") boolean initialBlockDownload
) {}