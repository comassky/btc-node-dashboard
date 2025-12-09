
package comasky.api;

import comasky.shared.DashboardConfig;
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
        if (config != null && config.health() != null && config.health().min() != null && config.health().min().outbound() != null) {
            minPeers = config.health().min().outbound().peers();
        }
        return Uni.createFrom().item(new DashboardConfigResponse(minPeers));
    }

    /**
     * DTO for dashboard configuration response.
     * @param minOutboundPeers minimum number of outbound peers required
     */
    public record DashboardConfigResponse(int minOutboundPeers) {}
}
