package comasky.api;

import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.RpcServices;
import jakarta.inject.Inject;
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
}