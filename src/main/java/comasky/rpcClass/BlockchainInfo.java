package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockchainInfo {
    private int blocks;
    private int headers;
    private String chain;
    @JsonProperty("verificationprogress")
    private double verificationProgress;
    private boolean initialBlockDownload;
}
