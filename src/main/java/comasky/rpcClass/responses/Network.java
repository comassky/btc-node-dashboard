package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents information about a network interface (IPv4, IPv6, Tor, etc.).
 *
 * @param name                      Name of the network (e.g., "ipv4", "ipv6", "onion", "i2p").
 * @param limited                   True if the node is limited to this network.
 * @param reachable                 True if the network is reachable.
 * @param proxy                     Address of the proxy used (if applicable).
 * @param proxyRandomizeCredentials True if proxy credentials are randomized.
 */
public record Network(
        @JsonProperty("name")
        String name,

        @JsonProperty("limited")
        boolean limited,

        @JsonProperty("reachable")
        boolean reachable,

        @JsonProperty("proxy")
        String proxy,

        @JsonProperty("proxy_randomize_credentials")
        boolean proxyRandomizeCredentials
) {}
