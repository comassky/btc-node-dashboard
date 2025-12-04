package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NodeInfo(
    @JsonProperty("protocolversion") int protocolVersion,
    int version,
    String subversion,
    int connections,
    boolean networkActive
) {}