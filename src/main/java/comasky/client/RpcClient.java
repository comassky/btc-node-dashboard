package comasky.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * REST client interface for Bitcoin Core RPC endpoint.
 */
public interface RpcClient {
    /**
     * Executes a Bitcoin Core RPC call and returns the raw JSON response.
     * @param rpcRequest the RPC request as a DTO
     * @return the raw JSON response as a String
     */
    @POST
    @Path("/")
    String executeRpcCall(RpcRequestDto rpcRequest);
}
