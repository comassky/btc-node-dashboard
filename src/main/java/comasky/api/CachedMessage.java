
package comasky.api;

import comasky.rpcClass.GlobalResponse;
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
    public static CachedMessage success(GlobalResponse data, String serializedJson) {
        return new CachedMessage(data, null, serializedJson, System.currentTimeMillis());
    }

    public static CachedMessage error(String errorMessage, String serializedJson) {
        return new CachedMessage(null, errorMessage, serializedJson, System.currentTimeMillis());
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public boolean isValid(long cacheValidityMs) {
        return System.currentTimeMillis() - timestamp < cacheValidityMs;
    }
}
