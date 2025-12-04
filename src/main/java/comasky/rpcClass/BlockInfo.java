package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BlockInfo {

    // Identification and structure information
    private String hash;
    private int confirmations;
    private int strippedsize;
    private int size;
    private int weight;
    private int height;
    private int version;
    private String versionHex;
    private String merkleroot;

    private long time; // Unix timestamp in seconds
    private long mediantime;

    // Technical and consensus information
    private long nonce;
    private String bits;
    private double difficulty;
    private String chainwork;
    
    @JsonProperty("nTx")
    private int ntx;

    // Chain structure
    private String previousblockhash;
    private String nextblockhash;
}