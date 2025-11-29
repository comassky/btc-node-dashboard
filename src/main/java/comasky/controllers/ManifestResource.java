package comasky.controllers;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class ManifestResource {

    @Inject
    @Location("manifest.json")
    Template manifest;

    @GET
    @Path("/manifest.json") // L'URL doit correspondre à ce que le HTML appelle
    @Produces(MediaType.APPLICATION_JSON) // ⚠️ Indique au navigateur que c'est du JSON
    public TemplateInstance getManifest() {
        return manifest.instance();
    }
}
