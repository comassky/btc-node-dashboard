package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response wrapper for the 'getnetworkinfo' RPC call.
 * Contains general information about the node and the P2P network.
 *
 * @param version             The version of the node software.
 * @param subversion          The user agent string (e.g., "/Satoshi:25.0.0/").
 * @param protocolversion     The protocol version of the P2P network.
 * @param localservices       The services supported by this node (in hex).
 * @param localservicesnames  The names of the services supported (e.g., ["WITNESS", "NETWORK"]).
 * @param localrelay          True if the node relays non-witness transactions (deprecated).
 * @param timeoffset          The time offset of the node relative to the network average (in seconds).
 * @param connections         The number of active P2P connections.
 * @param networkactive       True if P2P network activity is enabled.
 * @param networks            Information about the different networks (IPv4, IPv6, Tor, etc.).
 * @param localaddresses      List of local addresses.
 */
public record NetworkInfoResponse(
        @JsonProperty("version")
        int version,

        @JsonProperty("subversion")
        String subversion,

        @JsonProperty("protocolversion")
        int protocolversion,

        @JsonIgnore @JsonProperty("localservices")
        String localservices,

        @JsonIgnore @JsonProperty("localservicesnames")
        List<String> localservicesnames,

        @JsonIgnore @JsonProperty("localrelay")
        boolean localrelay,

        @JsonIgnore @JsonProperty("timeoffset")
        int timeoffset,

        @JsonIgnore @JsonProperty("connections")
        int connections,

        @JsonIgnore @JsonProperty("networkactive")
        boolean networkactive,

        @JsonProperty("networks")
        List<Network> networks,

        @JsonProperty("localaddresses")
        List<LocalAddress> localaddresses
) {}
