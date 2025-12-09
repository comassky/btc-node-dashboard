
package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Generic DTO for Bitcoin Core RPC responses.
 * @param <T> the type of the result field
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class RpcResponse<T> {
    private T result;
    private Object error;
    private String id;

    public RpcResponse() {}

    public T getResult() {
        return result;
    }
    public void setResult(T result) {
        this.result = result;
    }
    public Object getError() {
        return error;
    }
    public void setError(Object error) {
        this.error = error;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}