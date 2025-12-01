package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockchainInfo {
    private int blocks;
    private int headers;
    private String chain;
    @JsonProperty("verificationprogress")
    private double verificationProgress;
    private boolean initialBlockDownload;
}
