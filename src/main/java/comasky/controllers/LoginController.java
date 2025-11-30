package comasky.controllers;

import comasky.rpcClass.GlobalResponse;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static comasky.shared.Tools.formatUptime;

@Path("")
public class LoginController {

    @Inject
    Template login;

    @GET
    @Path("login.html")
    @Produces(MediaType.TEXT_HTML) // Doit renvoyer du HTML
    public TemplateInstance getLogin() {
        return this.login.data(null);
    }
}
