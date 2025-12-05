package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// Lombok handles getters and setters
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RpcResponse<T> {

    // The result of the RPC command (will be a List<PeerInfo>)
    private T result;

    // The error field in case of failure
    private Object error;

    // The request ID (not used here)
    private String id;
}