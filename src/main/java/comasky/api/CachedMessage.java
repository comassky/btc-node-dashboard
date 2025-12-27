    package comasky.api;

import comasky.rpcClass.dto.GlobalResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Cached message holder for WebSocket broadcasts.
 * Stores either successful data or error state with pre-serialized JSON
 * to avoid redundant serialization on each broadcast.
 *
 * @param data           The RPC response data (null if error)
 * @param errorMessage   The error message (null if success)
 * @param serializedJson Pre-serialized JSON to avoid repeated serialization
 * @param timestamp      Creation timestamp for cache validation
 */
@RegisterForReflection
public record CachedMessage(
        GlobalResponse data,
        String errorMessage,
        String serializedJson,
        long timestamp
) {
    /**
     * Creates a successful cached message.
     * @param data the global response data
     * @param serializedJson the JSON representation of the data
     * @return a new CachedMessage instance
     */
    public static CachedMessage success(GlobalResponse data, String serializedJson) {
        return new CachedMessage(data, null, serializedJson, System.currentTimeMillis());
    }

    /**
     * Creates an error cached message.
     * @param errorMessage the error message
     * @param serializedJson the JSON representation of the error
     * @return a new CachedMessage instance
     */
    public static CachedMessage error(String errorMessage, String serializedJson) {
        return new CachedMessage(null, errorMessage, serializedJson, System.currentTimeMillis());
    }

    /**
     * Checks if this message represents an error.
     * @return true if it is an error message, false otherwise
     */
    public boolean isError() {
        return errorMessage != null;
    }

    /**
     * Checks if the cached message is still valid based on the given validity duration.
     * @param cacheValidityMs the validity duration in milliseconds
     * @return true if the message is valid, false otherwise
     */
    public boolean isValid(long cacheValidityMs) {
        return System.currentTimeMillis() - timestamp < cacheValidityMs;
    }
}
