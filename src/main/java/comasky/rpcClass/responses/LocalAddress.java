package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a local address of the node.
 *
 * @param address The IP address or hostname.
 * @param port    The listening port.
 * @param score   Reliability score of the address (higher is better).
 */
public record LocalAddress(
        @JsonProperty("address")
        String address,

        @JsonProperty("port")
        int port,

        @JsonProperty("score")
        int score
) {}
