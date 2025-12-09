package comasky;

import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@QuarkusTest
class BtcControllerTest {

    @InjectMock
    RpcServices rpcServices;

    @Test
    void testGetDashboardData_restEndpoint() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        when(rpcServices.getData()).thenReturn(Uni.createFrom().item(mockResponse));

        given()
            .when().get("/data/dashboard")
            .then()
                .statusCode(200)
                .body("generalStats.totalPeers", is(10))
                .body("generalStats.inboundCount", is(2))
                .body("generalStats.outboundCount", is(8))
                .body("blockchainInfo.blocks", is(870000))
                .body("blockchainInfo.chain", is("main"))
                .body("nodeInfo.version", is(270000))
                .body("nodeInfo.subversion", is("/Satoshi:27.0.0/"))
                .body("upTime", is("5 days 3 hours"))
                .body("inboundPeer", hasSize(2))
                .body("outboundPeer", hasSize(8))
                .body("subverDistribution.inbound", hasSize(greaterThanOrEqualTo(0)))
                .body("subverDistribution.outbound", hasSize(greaterThanOrEqualTo(0)));
    }

    private GlobalResponse createMockGlobalResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfo blockchainInfo = new BlockchainInfo(870000, 870000, "main", 0.9999, false);

        NodeInfo nodeInfo = new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true);

        PeerInfo peer1 = new PeerInfo(1, "192.168.1.1:8333", null, null, 0, 0, 0, 2000000L, 1000000L, null, null, 0, 0, 0, 0, "/Satoshi:27.0.0/", true, null, 0, null, null, 0);
        PeerInfo peer2 = new PeerInfo(2, "192.168.1.2:8333", null, null, 0, 0, 0, 1500000L, 500000L, null, null, 0, 0, 0, 0, "/Satoshi:26.0.0/", false, null, 0, null, null, 0);

        List<PeerInfo> inboundPeers = List.of(peer1, peer1);
        List<PeerInfo> outboundPeers = List.of(peer2, peer2, peer2, peer2, peer2, peer2, peer2, peer2);

        SubverStats inboundStats = new SubverStats("/Satoshi:27.0.0/", 100.0);
        SubverStats outboundStats = new SubverStats("/Satoshi:26.0.0/", 100.0);

        SubverDistribution distribution = new SubverDistribution(
                List.of(inboundStats),
                List.of(outboundStats)
        );

        BlockInfo blockInfo = new BlockInfo(null, 0, 0, 0, 0, 0, 0, null, null, System.currentTimeMillis() / 1000, 0, 0, null, 0, null, 2500, null, null);

        return new GlobalResponse(
                generalStats,
                distribution,
                inboundPeers,
                outboundPeers,
                blockchainInfo,
                nodeInfo,
                "5 days 3 hours",
                blockInfo
        );
    }
}