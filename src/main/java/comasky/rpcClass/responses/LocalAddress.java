package comasky.rpcClass.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LocalAddress(
        @JsonProperty("address")
        String address,     // L'adresse IP ou le nom d'hôte local
        @JsonProperty("port")
        int port,           // Le port d'écoute
        @JsonProperty("score")
        int score           // Score de fiabilité de l'adresse (plus le score est élevé, plus l'adresse est préférée)
) {}
