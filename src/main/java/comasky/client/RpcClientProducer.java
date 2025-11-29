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
     * Produit une instance du RpcClient en construisant l'URI dynamique au runtime.
     * Cette méthode est appelée une seule fois par le CDI de Quarkus.
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