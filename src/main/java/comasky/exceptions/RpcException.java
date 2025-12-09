package comasky.exceptions;

/**
 * Exception for errors during Bitcoin Core RPC calls.
 */
public class RpcException extends RuntimeException {
    /**
     * Constructs a new RpcException with the specified detail message.
     * @param message the detail message
     */
    public RpcException(String message) {
        super(message);
    }

    /**
     * Constructs a new RpcException with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
}