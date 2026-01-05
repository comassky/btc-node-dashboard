package comasky;

import comasky.exceptions.RpcException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RpcException.
 * Tests exception creation, message handling, and cause propagation.
 */
class RpcExceptionTest {

    @Test
    void testExceptionWithMessage() {
        // Arrange
        final String message = "RPC call failed";

        // Act
        final RpcException exception = new RpcException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionWithMessageAndCause() {
        // Arrange
        final String message = "RPC call failed";
        final RuntimeException cause = new RuntimeException("Connection timeout");

        // Act
        final RpcException exception = new RpcException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
        assertEquals("Connection timeout", exception.getCause().getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        // Arrange & Act
        final RpcException exception = new RpcException("Test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionCanBeThrown() {
        // Act & Assert
        assertThrows(RpcException.class, () -> {
            throw new RpcException("Test exception");
        });
    }

    @Test
    void testExceptionWithNullMessage() {
        // Act
        final RpcException exception = new RpcException(null);

        // Assert
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionStackTraceIsPreserved() {
        // Arrange
        final RuntimeException cause = new RuntimeException("Original error");

        // Act
        final RpcException exception = new RpcException("Wrapped error", cause);

        // Assert
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
        assertNotNull(exception.getCause().getStackTrace());
    }

    @Test
    void testExceptionMessageFormattingInRealScenario() {
        // Arrange - Simulate real-world usage
        final String method = "getblockchaininfo";
        final String errorCode = "-28";
        final String errorMessage = "Loading block index...";
        final String formattedMessage = String.format(
            "RPC Error for method %s: RpcError[code=%s, message='%s']",
            method, errorCode, errorMessage
        );

        // Act
        final RpcException exception = new RpcException(formattedMessage);

        // Assert
        assertTrue(exception.getMessage().contains(method));
        assertTrue(exception.getMessage().contains(errorCode));
        assertTrue(exception.getMessage().contains(errorMessage));
    }
}
