package comasky.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;

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
     * Produces an instance of RpcClient by building the URI dynamically at runtime.
     * This method is called once by Quarkus CDI.
     */
    @Produces
    @ApplicationScoped
    public RpcClient createRpcClient() {
        String urlWithAuth = String.format("%s://%s:%s@%s:%d", scheme, user, password, host, port);
        URI baseUri = URI.create(urlWithAuth);
        return RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .build(RpcClient.class);
    }
}