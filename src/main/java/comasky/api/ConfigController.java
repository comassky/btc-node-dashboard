package comasky.api;

import comasky.config.DashboardConfig;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API controller for dashboard configuration.
 * <p>
 * Exposes runtime configuration values to the frontend for UI adaptation.
 */
@Path("/api/config")
@Tag(name = "Configuration", description = "Application configuration endpoints")
public class ConfigController {

    @Inject
    DashboardConfig config;

    /**
     * Retrieves dashboard configuration values for the frontend.
     *
     * @return a {@link Uni} emitting the dashboard configuration response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get dashboard configuration",
        description = "Retrieves runtime configuration values used by the dashboard UI"
    )
    @APIResponse(
        responseCode = "200",
        description = "Configuration successfully retrieved",
        content = @Content(schema = @Schema(implementation = DashboardConfigResponse.class))
    )
    public Uni<DashboardConfigResponse> getConfig() {
        int minPeers = 0;
        boolean disableMempool = false;
        if (config != null) {
                minPeers = config.peers().minOutbound();
                disableMempool = config.mempool().disable();
        }
        return Uni.createFrom().item(new DashboardConfigResponse(minPeers, disableMempool));
    }

    /**
     * DTO for dashboard configuration response.
     * @param minOutboundPeers minimum number of outbound peers required
     * @param disableMempool whether mempool display is disabled
     */
    public record DashboardConfigResponse(int minOutboundPeers, boolean disableMempool) {}
}
