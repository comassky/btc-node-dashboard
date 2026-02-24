package comasky.api;

import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import comasky.rpcClass.responses.MempoolInfoResponse;
import comasky.rpcClass.responses.NetworkInfoResponse;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API controller exposing endpoints to retrieve Bitcoin node and dashboard data.
 * <p>
 * Provides HTTP endpoints for dashboard, network, block, and blockchain information.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Bitcoin Node", description = "Bitcoin Core node monitoring and data retrieval endpoints")
public class BitcoinApiController {

    private static final String BLOCK_HASH_PATTERN = "^[0-9a-fA-F]{64}$";

    private final RpcServices rpcServices;

    @Inject
    public BitcoinApiController(RpcServices rpcServices) {
        this.rpcServices = rpcServices;
    }

    /**
     * Retrieves the complete dashboard data.
     *
     * @return a {@link Uni} emitting the global dashboard response
     */
    @GET
    @Path("dashboard")
    @Operation(
        summary = "Get complete dashboard data",
        description = "Retrieves comprehensive Bitcoin node information including peers, blockchain, network, and mempool data"
    )
    @APIResponse(
        responseCode = "200",
        description = "Dashboard data successfully retrieved",
        content = @Content(schema = @Schema(implementation = GlobalResponse.class))
    )
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
    @Operation(
        summary = "Get network information",
        description = "Retrieves detailed network information from the Bitcoin Core node"
    )
    @APIResponse(
        responseCode = "200",
        description = "Network information successfully retrieved",
        content = @Content(schema = @Schema(implementation = NetworkInfoResponse.class))
    )
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
    @Operation(
        summary = "Get block information",
        description = "Retrieves detailed information about a specific block by its hash"
    )
    @APIResponse(
        responseCode = "200",
        description = "Block information successfully retrieved",
        content = @Content(schema = @Schema(implementation = BlockInfoResponse.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid block hash format"
    )
    public Uni<BlockInfoResponse> getBlockInfo(
        @Parameter(description = "Block hash (64 hexadecimal characters)", required = true)
        @PathParam("hash") String hash
    ) {
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
    @Operation(
        summary = "Get best block hash",
        description = "Returns the hash of the most recent block in the blockchain"
    )
    @APIResponse(
        responseCode = "200",
        description = "Best block hash successfully retrieved",
        content = @Content(schema = @Schema(implementation = String.class))
    )
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
    @Operation(
        summary = "Get blockchain information",
        description = "Retrieves detailed information about the current state of the blockchain"
    )
    @APIResponse(
        responseCode = "200",
        description = "Blockchain information successfully retrieved",
        content = @Content(schema = @Schema(implementation = BlockchainInfoResponse.class))
    )
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
    @Operation(
        summary = "Get mempool information",
        description = "Retrieves current mempool statistics and transaction information"
    )
    @APIResponse(
        responseCode = "200",
        description = "Mempool information successfully retrieved",
        content = @Content(schema = @Schema(implementation = MempoolInfoResponse.class))
    )
    public Uni<MempoolInfoResponse> getMempoolInfo() {
        return rpcServices.getMempoolInfo();
    }
}
