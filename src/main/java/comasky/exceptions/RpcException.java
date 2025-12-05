package comasky.exceptions;

public class RpcException extends RuntimeException {
    // Lombok or simple constructor
    public RpcException(String message) {
        super(message);
    }
}