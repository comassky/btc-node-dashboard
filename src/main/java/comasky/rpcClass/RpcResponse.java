package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class RpcResponse<T> {

    private T result;
    private Object error;
    private String id;
}