package comasky.api;

import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.RpcServices;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class BtcController {

    @Inject
    private RpcServices rpcServices;

    @GET
    @Path("peers")
    public GlobalResponse peers() {
        return this.rpcServices.getData();
    }
}