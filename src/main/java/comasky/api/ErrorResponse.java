package comasky.api;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Standardized error response format for API and WebSocket communications.
 *
 * @param message A human-readable error message.
 * @param code An optional error code.
 */
@RegisterForReflection
public record ErrorResponse(String message, String code) {
    public ErrorResponse(String message) {
        this(message, null);
    }
}
