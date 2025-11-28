    package comasky.controllers;

    import comasky.rpcClass.GlobalResponse;
    import comasky.rpcClass.RpcServices;
    import io.quarkus.qute.Template;
    import io.quarkus.qute.TemplateInstance;
    import jakarta.inject.Inject;
    import jakarta.ws.rs.GET;
    import jakarta.ws.rs.Path;
    import jakarta.ws.rs.Produces;
    import jakarta.ws.rs.core.MediaType;

    import static comasky.shared.Tools.formatUptime;

    /**
     * Contrôleur pour l'affichage des statistiques des pairs en HTML,
     * utilisant le moteur de templates Qute de Quarkus.
     */
    @Path("")
    public class PeerHtmlController {

        @Inject
        RpcServices rpcServices;

        @Inject
        Template peers;

        @GET
        @Path("")
        @Produces(MediaType.TEXT_HTML) // Doit renvoyer du HTML
        public TemplateInstance getPeerReport() {
            GlobalResponse dataNode = rpcServices.getData();
            return peers.data("stats", dataNode.getGeneralStats()) // ObjectNode des stats générales
                    .data("subver_distribution", dataNode.getSubverDistribution())
                    .data("inbound_peers", dataNode.getInboundPeer()) // ArrayNode des pairs entrants
                    .data("outbound_peers", dataNode.getOutboundPeer())
                    .data("time", formatUptime(rpcServices.getUptimeSeconds()))
                    .data("blockchain", dataNode.getBlockchainInfo())
                    .data("node", dataNode.getNodeInfo()); //
        }
    }