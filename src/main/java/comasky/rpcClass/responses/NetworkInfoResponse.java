package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response wrapper for the 'getnetworkinfo' RPC call.
 * Contains general information about the node and the P2P network.
 */
public record NetworkInfoResponse(
        @JsonProperty("version")
        int version,

        @JsonProperty("subversion")
        String subversion,

        @JsonProperty("protocolversion")
        int protocolversion,

        @JsonProperty("localservices")
        String localservices,

        @JsonProperty("localservicesnames")
        List<String> localservicesnames,

        @JsonProperty("localrelay")
        boolean localrelay,

        @JsonProperty("timeoffset")
        int timeoffset,

        @JsonProperty("connections")
        int connections,

        @JsonProperty("networkactive")
        boolean networkactive,

        @JsonProperty("networks")
        List<Network> networks,

        @JsonProperty("localaddresses")
        List<LocalAddress> localaddresses
) {}
