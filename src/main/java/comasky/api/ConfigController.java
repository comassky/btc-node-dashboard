package comasky.api;

import comasky.shared.DashboardConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST API controller for dashboard configuration.
 * Exposes runtime configuration values to the frontend.
 */
@Path("/api/config")
public class ConfigController {

    @Inject
    DashboardConfig config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DashboardConfigResponse getConfig() {
        return new DashboardConfigResponse(
            config.health().min().outbound().peers()
        );
    }

    public record DashboardConfigResponse(int minOutboundPeers) {}
}
