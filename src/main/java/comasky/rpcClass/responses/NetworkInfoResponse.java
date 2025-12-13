package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NetworkInfoResponse(
        // Informations Générales
        @JsonProperty("version")
        int version,                       // La version du protocole de votre nœud
        @JsonProperty("subversion")
        String subversion,                 // La chaîne de sous-version (ex: "/Satoshi:25.0.0/")
        @JsonProperty("protocolversion")
        int protocolversion,               // La version du protocole P2P
        @JsonIgnore @JsonProperty("localservices")
        String localservices,              // Les services supportés par ce nœud (en hexadécimal)
        @JsonIgnore @JsonProperty("localservicesnames")
        List<String> localservicesnames,   // Les noms des services supportés (ex: ["WITNESS", "NETWORK"])
        @JsonIgnore @JsonProperty("localrelay")
        boolean localrelay,                // Si le nœud relaie les transactions non-witness (déprécié)
        @JsonIgnore @JsonProperty("timeoffset")
        int timeoffset,                    // Décalage temporel moyen des pairs (en secondes)
        @JsonIgnore @JsonProperty("connections")
        int connections,                   // Le nombre de connexions P2P actives
        @JsonIgnore @JsonProperty("networkactive")
        boolean networkactive,             // True si le nœud est actif sur le réseau P2P

        // Informations sur les différents réseaux (IPv4, IPv6, Tor, etc.)
        @JsonProperty("networks")
        List<Network> networks,

        // Adresses locales
        @JsonProperty("localaddresses")
        List<LocalAddress> localaddresses
) {}