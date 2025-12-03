package comasky.rpcClass;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockInfo {

    // Informations d'identification et de structure
    private String hash;
    private int confirmations;
    private int strippedsize;
    private int size;
    private int weight;
    private int height;
    private int version;
    private String versionHex;
    private String merkleroot;

    private long time; // Timestamp Unix en secondes
    private long mediantime;

    // Informations techniques et de consensus
    private long nonce;
    private String bits;
    private double difficulty;
    private String chainwork;
    private int nTx;

    // Structure de la chaîne
    private String previousblockhash;
    private String nextblockhash;
}