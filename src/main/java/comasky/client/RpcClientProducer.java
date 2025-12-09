package comasky.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;

/**
 * Producer for the RpcClient REST client, with validation and robust configuration.
 */
@ApplicationScoped
public class RpcClientProducer {

    @ConfigProperty(name = "bitcoin.rpc.scheme")
    String scheme;

    @ConfigProperty(name = "bitcoin.rpc.host")
    String host;

    @ConfigProperty(name = "bitcoin.rpc.port")
    int port;

    @ConfigProperty(name = "bitcoin.rpc.user")
    String user;

    @ConfigProperty(name = "bitcoin.rpc.password")
    String password;

    /**
     * Creates and configures the RpcClient REST client.
     * @return configured RpcClient
     */
    @Produces
    @ApplicationScoped
    public RpcClient createRpcClient() {
        validateCredentials();
        if (isNullOrBlank(scheme) || isNullOrBlank(host) || isNullOrBlank(user) || isNullOrBlank(password)) {
            throw new IllegalStateException("All bitcoin.rpc.* properties must be set and non-blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("bitcoin.rpc.port must be between 1 and 65535");
        }
        String urlWithAuth = String.format("%s://%s:%s@%s:%d", scheme, user, password, host, port);
        URI baseUri = URI.create(urlWithAuth);
        return RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .build(RpcClient.class);
    }

    private void validateCredentials() {
        if (user != null && (user.contains(":") || user.contains("@"))) {
            throw new IllegalArgumentException("bitcoin.rpc.user contains invalid characters (: or @)");
        }
        if (password != null && password.contains("@")) {
            throw new IllegalArgumentException("bitcoin.rpc.password contains invalid character (@)");
        }
    }

    private boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }
}