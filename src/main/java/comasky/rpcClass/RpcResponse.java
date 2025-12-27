package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Generic DTO for Bitcoin Core RPC responses.
 *
 * @param <T> the type of the result field
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class RpcResponse<T> {
    private T result;
    private Object error;
    private String id;

    /**
     * Default constructor for Jackson deserialization.
     */
    public RpcResponse() {}

    /**
     * Gets the result of the RPC call.
     * @return the result object of type T
     */
    public T getResult() {
        return result;
    }

    /**
     * Sets the result of the RPC call.
     * @param result the result object
     */
    public void setResult(T result) {
        this.result = result;
    }

    /**
     * Gets the error object if the RPC call failed.
     * @return the error object, or null if successful
     */
    public Object getError() {
        return error;
    }

    /**
     * Sets the error object.
     * @param error the error object
     */
    public void setError(Object error) {
        this.error = error;
    }

    /**
     * Gets the request ID.
     * @return the request ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the request ID.
     * @param id the request ID
     */
    public void setId(String id) {
        this.id = id;
    }
}
