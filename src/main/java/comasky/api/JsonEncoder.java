package comasky.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

/**
 * A generic WebSocket encoder that serializes any Java object into a JSON string.
 * <p>
 * This encoder leverages Quarkus's CDI container to get a pre-configured
 * {@link ObjectMapper} instance, ensuring consistent JSON serialization across the application.
 */
public class JsonEncoder implements Encoder.Text<Object> {

    private ObjectMapper objectMapper;

    /**
     * Encodes the given object into its JSON string representation.
     *
     * @param object the object to encode
     * @return the JSON string
     * @throws EncodeException if serialization fails
     */
    @Override
    public String encode(Object object) throws EncodeException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new EncodeException(object, "Failed to encode object to JSON", e);
        }
    }

    /**
     * Initializes the encoder by programmatically looking up the ObjectMapper
     * from the Quarkus CDI container. This is the standard way to enable
     * dependency injection in non-CDI-managed classes like Encoders.
     *
     * @param config the endpoint configuration
     */
    @Override
    public void init(EndpointConfig config) {
        this.objectMapper = Arc.container().instance(ObjectMapper.class).get();
    }

    /**
     * Cleans up resources used by the encoder.
     */
    @Override
    public void destroy() {
        // No resources to clean up
    }
}
