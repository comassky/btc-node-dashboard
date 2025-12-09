package comasky.api;

import comasky.rpcClass.*;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
    public Uni<GlobalResponse> getDashboardData() {
        return rpcServices.getData();
    }

    @GET
    @Path("getnetworkinfo")
    public Uni<NodeInfo> getNetworkInfo() {
        return rpcServices.getNodeInfo();
    }

    @GET
    @Path("getblock/{hash}")
    public Uni<BlockInfo> getBlockkInfo(@PathParam("hash") String hash) {
        return rpcServices.getBlockInfo(hash);
    }

    @GET
    @Path("getbestblockhash")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getBestBlockHash() {
        return rpcServices.getBestBlockHash();
    }

    @GET
    @Path("getBlockchainInfo")
    public Uni<BlockchainInfo> getBlockchainInfo() {
        return rpcServices.getBlockchainInfo();
    }
}