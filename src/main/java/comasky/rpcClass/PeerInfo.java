package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeerInfo {

    // Identifiant unique du pair
    private int id;

    // Adresse IP:Port du pair
    private String addr;

    // Adresse IP locale:Port du pair
    private String addrlocal;

    // Services supportés par le pair (ex: "0000000000000001" pour NODE_NETWORK)
    private String services;

    // Début de l'horodatage de la connexion Unix epoch
    private long conntime;

    // Durée en secondes depuis la dernière activité
    private long lastsend;

    // Durée en secondes depuis la dernière activité reçue
    private long lastrecv;

    // Bytes envoyés depuis la dernière (re)connexion
    private long bytesrecv;

    // Bytes reçus depuis la dernière (re)connexion
    private long bytessent;

    // Bytes envoyés à ce pair (Total)
    @JsonProperty("bytesrecv_per_msg")
    private java.util.Map<String, Long> bytesRecvPerMsg;

    // Bytes reçus de ce pair (Total)
    @JsonProperty("bytessent_per_msg")
    private java.util.Map<String, Long> bytesSentPerMsg;

    // Temps de latence (ping) en secondes
    private double pingtime;

    // Temps de latence (ping) minimal en secondes
    private double minping;

    // Temps depuis le dernier ping réussi en secondes
    private long timeoffset;

    // Version du protocole du pair
    private int version;

    // Sous-version (chaîne de caractères descriptive)
    private String subver;

    // Indique si le pair est un pair entrant (true) ou sortant (false)
    private boolean inbound;

    // Indique si la connexion est chiffrée (ex: "v2")
    private String transport_protocol;

    // Niveau de permission (0 = par défaut)
    private int permission;

    // Statut de la connexion (ex: "in_flight")
    private String connection_type;

    // Nom du réseau (ex: "ipv4", "ipv6", "onion")
    private String network;

    // Blocs non transmis par le pair
    @JsonProperty("unshipped_txs")
    private int unshippedTxs;
}