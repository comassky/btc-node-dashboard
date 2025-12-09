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

    /**
     * Returns aggregated dashboard data.
     */
    @GET
    @Path("dashboard")
    public Uni<GlobalResponse> getDashboardData() {
        return rpcServices.getData();
    }

    /**
     * Returns node network information.
     */
    @GET
    @Path("getnetworkinfo")
    public Uni<NodeInfo> getNetworkInfo() {
        return rpcServices.getNodeInfo();
    }

    /**
     * Returns block information by hash.
     * @param hash block hash (required)
     */
    @GET
    @Path("getblock/{hash}")
    public Uni<BlockInfo> getBlockInfo(@PathParam("hash") String hash) {
        if (hash == null || hash.isBlank()) {
            return Uni.createFrom().failure(new IllegalArgumentException("Block hash must not be null or blank"));
        }
        return rpcServices.getBlockInfo(hash);
    }

    /**
     * Returns the hash of the best block (plain text).
     */
    @GET
    @Path("getbestblockhash")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getBestBlockHash() {
        return rpcServices.getBestBlockHash();
    }

    /**
     * Returns blockchain information.
     */
    @GET
    @Path("getBlockchainInfo")
    public Uni<BlockchainInfo> getBlockchainInfo() {
        return rpcServices.getBlockchainInfo();
    }
}