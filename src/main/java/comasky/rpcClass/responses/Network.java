package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Network(
        @JsonProperty("name")
        String name,        // Nom du réseau (e.g., "ipv4", "ipv6", "onion", "i2p")
        @JsonProperty("limited")
        boolean limited,    // True si le nœud est limité à ce réseau (ex: Tor seulement)
        @JsonProperty("reachable")
        boolean reachable,  // True si le réseau est accessible
        @JsonProperty("proxy")
        String proxy,       // Adresse du proxy utilisé (si applicable)
        @JsonProperty("proxy_randomize_credentials")
        boolean proxyRandomizeCredentials // True si les identifiants du proxy sont randomisés
) {}