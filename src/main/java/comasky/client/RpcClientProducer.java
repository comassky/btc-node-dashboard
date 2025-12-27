package comasky.client;

import comasky.config.BitcoinRpcConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Producer for the RpcClient REST client, configured via typesafe properties.
 */
@ApplicationScoped
public class RpcClientProducer {

    @Inject
    BitcoinRpcConfig config;

    /**
     * Creates and configures the RpcClient REST client using typesafe configuration.
     * This method leverages the RestClientBuilder to construct the client with a base URI
     * and registers a BasicAuthentication filter for handling RPC credentials securely.
     *
     * @return A configured RpcClient instance.
     * @throws URISyntaxException if the configured RPC URL is invalid.
     */
    @Produces
    @ApplicationScoped
    public RpcClient createRpcClient() throws URISyntaxException {
        URI baseUri = new URI(String.format("%s://%s:%d", config.scheme(), config.host(), config.port()));

        // Use our own BasicAuthentication class
        BasicAuthentication authFilter = new BasicAuthentication(config.user(), config.password());

        return RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .register(authFilter)
                .build(RpcClient.class);
    }
}
