package comasky.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

public interface RpcClient {
    @POST
    @Path("/")
    String executeRpcCall(java.util.Map<String, Object> rpcRequest);

}