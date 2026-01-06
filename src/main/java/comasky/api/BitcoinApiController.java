package comasky.api;

import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import comasky.rpcClass.responses.MempoolInfoResponse;
import comasky.rpcClass.responses.NetworkInfoResponse;
import comasky.service.CacheProvider;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

/**
 * REST API controller exposing endpoints to retrieve Bitcoin node and dashboard data.
 * <p>
 * Provides HTTP endpoints for dashboard, network, block, and blockchain information.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class BitcoinApiController {

    private static final String BLOCK_HASH_PATTERN = "^[0-9a-fA-F]{64}$";

    private final RpcServices rpcServices;
    private final CacheProvider cacheProvider;

    @Inject
    public BitcoinApiController(RpcServices rpcServices, CacheProvider cacheProvider) {
        this.rpcServices = rpcServices;
        this.cacheProvider = cacheProvider;
    }

    /**
     * Retrieves the complete dashboard data.
     *
     * @return a {@link Uni} emitting the global dashboard response
     */
    @GET
    @Path("dashboard")
    public Uni<GlobalResponse> getDashboardData() {
        return rpcServices.getData();
    }

    /**
     * Retrieves network information about the Bitcoin node.
     *
     * @return a {@link Uni} emitting the node network information
     */
    @GET
    @Path("getnetworkinfo")
    public Uni<NetworkInfoResponse> getNetworkInfo() {
        return rpcServices.getNetworkInfo();
    }

    /**
     * Retrieves block information for a given block hash.
     *
     * @param hash the block hash (must not be null or blank)
     * @return a {@link Uni} emitting the block information, or a failed Uni if the hash is invalid
     */
    @GET
    @Path("getblock/{hash}")
    public Uni<BlockInfoResponse> getBlockInfo(@PathParam("hash") String hash) {
        if (hash == null || hash.isBlank() || !hash.matches(BLOCK_HASH_PATTERN)) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("Invalid block hash format. Expected 64 hexadecimal characters.")
            );
        }
        return rpcServices.getBlockInfo(hash);
    }

    /**
     * Retrieves the hash of the best (most recent) block as plain text.
     *
     * @return a {@link Uni} emitting the best block hash as a string
     */
    @GET
    @Path("getbestblockhash")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getBestBlockHash() {
        return rpcServices.getBestBlockHash();
    }

    /**
     * Retrieves blockchain information for the Bitcoin node.
     *
     * @return a {@link Uni} emitting the blockchain information
     */
    @GET
    @Path("getBlockchainInfo")
    public Uni<BlockchainInfoResponse> getBlockchainInfo() {
        return rpcServices.getBlockchainInfo();
    }


    /**
     * Retrieves mempool information for the Bitcoin node.
     *
     * @return a {@link Uni} emitting the mempool information
     */
    @GET
    @Path("getmempoolinfo")
    public Uni<MempoolInfoResponse> getMempoolInfo() {
        return rpcServices.getMempoolInfo();
    }
}
