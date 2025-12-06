package comasky.api;

import comasky.rpcClass.*;
import jakarta.inject.Inject;
import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST API controller for Bitcoin node data.
 * Provides HTTP endpoints to retrieve dashboard information.
 */
@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class BtcController {

    private final RpcServices rpcServices;

    @Inject
    public BtcController(RpcServices rpcServices) {
        this.rpcServices = rpcServices;
    }

    @GET
    @Path("dashboard")
    public GlobalResponse getDashboardData() {
        return rpcServices.getData();
    }

    @GET
    @Path("getnetworkinfo")
    public NodeInfo getNetworkInfo() {
        return rpcServices.getNodeInfo();
    }

    @GET
    @Path("getblock/{hash}")
    public BlockInfo getBlockkInfo(@PathParam("hash") String hash) {
        return rpcServices.getBlockInfo(hash);
    }

    @GET
    @Path("getbestblockhash")
    @Produces(MediaType.TEXT_PLAIN)
    public String getBestBlockHash() {
        return rpcServices.getBestBlockHash();
    }

    @GET
    @Path("getBlockchainInfo")
    public BlockchainInfo getBlockchainInfo() {
        return rpcServices.getBlockchainInfo();
    }
}