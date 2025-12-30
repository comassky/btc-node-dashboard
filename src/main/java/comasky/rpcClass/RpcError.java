package comasky.rpcClass;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Represents a standard JSON-RPC error object.
 *
 * @param code    The error code indicating the type of error.
 * @param message A short description of the error.
 */
@RegisterForReflection
public record RpcError(int code, String message) {
    @Override
    public String toString() {
        return String.format("RpcError[code=%d, message='%s']", code, message);
    }
}
