package comasky.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A client request filter for adding Basic Authentication credentials to outgoing requests.
 * This class provides the BasicAuthentication functionality for the REST client.
 */
public class BasicAuthentication implements ClientRequestFilter {

    private final String username;
    private final String password;

    /**
     * Constructs a new BasicAuthentication filter.
     * @param username The username for authentication.
     * @param password The password for authentication.
     */
    public BasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String credentials = this.username + ":" + this.password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
    }
}
