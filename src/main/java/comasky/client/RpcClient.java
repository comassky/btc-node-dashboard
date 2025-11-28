package comasky.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "bitcoin")
public interface RpcClient {

    // Suppression de la généricité. Le client renvoie la réponse JSON brute sous forme de chaîne.
    @POST
    @Path("/")
    String executeRpcCall(java.util.Map<String, Object> rpcRequest);

}