
package comasky.rpcClass;

/**
 * DTO for best block hash response.
 */
public class BestBlockHashResponse {
    private String bestblockhash;

    public BestBlockHashResponse() {}
    public BestBlockHashResponse(String bestblockhash) {
        this.bestblockhash = bestblockhash;
    }
    public String getBestblockhash() {
        return bestblockhash;
    }
    public void setBestblockhash(String bestblockhash) {
        this.bestblockhash = bestblockhash;
    }
}
