package comasky;

import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class BtcControllerTest {

    @InjectMock
    RpcServices rpcServices;

    @Test
    void testGetDashboardData_success() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        when(rpcServices.getData()).thenReturn(mockResponse);

        assertNotNull(mockResponse);
        assertEquals(2, mockResponse.getGeneralStats().inboundCount());
        assertEquals(8, mockResponse.getGeneralStats().outboundCount());
        assertEquals(10, mockResponse.getGeneralStats().totalPeers());
        assertEquals(870000, mockResponse.getBlockchainInfo().getBlocks());
        assertEquals("main", mockResponse.getBlockchainInfo().getChain());
    }

    @Test
    void testGetDashboardData_restEndpoint() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        when(rpcServices.getData()).thenReturn(mockResponse);

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

        BlockchainInfo blockchainInfo = new BlockchainInfo();
        blockchainInfo.setBlocks(870000);
        blockchainInfo.setHeaders(870000);
        blockchainInfo.setChain("main");
        blockchainInfo.setVerificationProgress(0.9999);

        NodeInfo nodeInfo = new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true);

        PeerInfo peer1 = new PeerInfo();
        peer1.setAddr("192.168.1.1:8333");
        peer1.setSubver("/Satoshi:27.0.0/");
        peer1.setInbound(true);
        peer1.setBytessent(1000000L);
        peer1.setBytesrecv(2000000L);

        PeerInfo peer2 = new PeerInfo();
        peer2.setAddr("192.168.1.2:8333");
        peer2.setSubver("/Satoshi:26.0.0/");
        peer2.setInbound(false);
        peer2.setBytessent(500000L);
        peer2.setBytesrecv(1500000L);

        List<PeerInfo> inboundPeers = List.of(peer1, peer1);
        List<PeerInfo> outboundPeers = List.of(peer2, peer2, peer2, peer2, peer2, peer2, peer2, peer2);

        SubverStats inboundStats = SubverStats.builder()
                .server("/Satoshi:27.0.0/")
                .percentage(100.0)
                .build();

        SubverStats outboundStats = SubverStats.builder()
                .server("/Satoshi:26.0.0/")
                .percentage(100.0)
                .build();

        SubverDistribution distribution = new SubverDistribution(
                List.of(inboundStats),
                List.of(outboundStats)
        );

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setTime(System.currentTimeMillis() / 1000);
        blockInfo.setNtx(2500);

        return GlobalResponse.builder()
                .generalStats(generalStats)
                .blockchainInfo(blockchainInfo)
                .nodeInfo(nodeInfo)
                .upTime("5 days 3 hours")
                .inboundPeer(inboundPeers)
                .outboundPeer(outboundPeers)
                .subverDistribution(distribution)
                .block(blockInfo)
                .build();
    }
}
