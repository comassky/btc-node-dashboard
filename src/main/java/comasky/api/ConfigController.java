package comasky.api;

import comasky.config.DashboardConfig;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST API controller for dashboard configuration.
 * <p>
 * Exposes runtime configuration values to the frontend for UI adaptation.
 */
@Path("/api/config")
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
