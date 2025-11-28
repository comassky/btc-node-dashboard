package comasky.exceptions;

public class RpcException extends RuntimeException {
    // Lombok ou simple constructeur
    public RpcException(String message) {
        super(message);
    }
}