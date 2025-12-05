package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record NodeInfo(
    @JsonProperty("protocolversion") int protocolVersion,
    int version,
    String subversion,
    int connections,
    boolean networkActive
) {}