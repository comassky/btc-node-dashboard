package comasky;

import comasky.api.BitcoinApiController;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.dto.SubverStats;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import comasky.rpcClass.responses.NetworkInfoResponse;
import comasky.rpcClass.view.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@QuarkusTest
class BitcoinApiControllerTest {

    @InjectMock
    RpcServices rpcServices;

    @Test
    void testGetNetworkInfo() {
        NetworkInfoResponse mockResponse = new NetworkInfoResponse(
            70016, "/Satoshi:27.0.0/", 270000, "0000000000000000", java.util.Collections.emptyList(), true, 0, 10, true, java.util.Collections.emptyList(), java.util.Collections.emptyList()
        );
        when(rpcServices.getNetworkInfo()).thenReturn(Uni.createFrom().item(mockResponse));

        given()
            .when().get("/api/getnetworkinfo")
            .then()
            .statusCode(200)
            .body("version", is(70016));
    }

    @Test
    void testGetBlockInfo() {
        String hash = "0000000000000000000a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e";
        BlockInfoResponse mockBlock = new BlockInfoResponse(hash, 1, 0, 0, 0, 870000, 1, "", "", 1733443200L, 0L, 0L, "", 1.0, "", 2500, "", "");
        when(rpcServices.getBlockInfo(hash)).thenReturn(Uni.createFrom().item(mockBlock));

        given()
            .when().get("/api/getblock/" + hash)
            .then()
            .statusCode(200)
            .body("nTx", is(2500));
    }

    @Test
    void testGetBestBlockHash() {
        String bestHash = "0000000000000000000besthash";
        when(rpcServices.getBestBlockHash()).thenReturn(Uni.createFrom().item(bestHash));

        given()
            .when().get("/api/getbestblockhash")
            .then()
            .statusCode(200)
            .body(is(bestHash));
    }

    @Test
    void testGetBlockchainInfo() {
        BlockchainInfoResponse mockResponse = new BlockchainInfoResponse(
            "main", 870000, 870000, "0000000000000000000dummyhash", 0.9999, 1700000000L, 1700000000L, 0.9999, false,
            "0000000000000000000000000000000000000000000000000000000000000000", 1000000000L, false, null
        );
        when(rpcServices.getBlockchainInfo()).thenReturn(Uni.createFrom().item(mockResponse));

        given()
            .when().get("/api/getBlockchainInfo")
            .then()
            .statusCode(200)
            .body("chain", is("main"));
    }

    @Test
    void testInstantiationWithMock() {
        RpcServices mockRpcServices = org.mockito.Mockito.mock(RpcServices.class);
        BitcoinApiController controller = new BitcoinApiController(mockRpcServices);
        assertNotNull(controller);
    }

    @Test
    void testGetDashboardData() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        when(rpcServices.getData()).thenReturn(Uni.createFrom().item(mockResponse));

        given()
            .when().get("/api/dashboard")
            .then()
            .statusCode(200)
            .body("generalStats.totalPeers", is(10));
    }

    private GlobalResponse createMockGlobalResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfoView blockchainInfoView = new BlockchainInfoView(
            "main", 870000, 870000, 0.9999, 1700000000L, 1700000000L, 0.9999, false,
            "0000000000000000000000000000000000000000000000000000000000000000", 1000000000L
        );

        NetworkInfoView nodeInfoView = new NetworkInfoView(
            70016, "/Satoshi:27.0.0/", 270000, java.util.Collections.emptyList(), java.util.Collections.emptyList()
        );

        PeerInfoView peer1 = new PeerInfoView(1, "192.168.1.1:8333", 0, 2000000L, 1000000L, 0, 0, 0, "/Satoshi:27.0.0/", true, null, null);
        PeerInfoView peer2 = new PeerInfoView(2, "192.168.1.2:8333", 0, 1500000L, 500000L, 0, 0, 0, "/Satoshi:26.0.0/", false, null, null);

        List<PeerInfoView> inboundPeers = List.of(peer1, peer1);
        List<PeerInfoView> outboundPeers = List.of(peer2, peer2, peer2, peer2, peer2, peer2, peer2, peer2);

        SubverStats inboundStats = new SubverStats("/Satoshi:27.0.0/", 100.0);
        SubverStats outboundStats = new SubverStats("/Satoshi:26.0.0/", 100.0);

        SubverDistribution distribution = new SubverDistribution(List.of(inboundStats), List.of(outboundStats));

        BlockInfoView blockInfoView = new BlockInfoView(System.currentTimeMillis() / 1000, 2500);

        MempoolInfoView mempoolInfoView = new MempoolInfoView(0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0);

        return new GlobalResponse(
                generalStats,
                distribution,
                inboundPeers,
                outboundPeers,
                blockchainInfoView,
                nodeInfoView,
                446400L,
                blockInfoView,
                mempoolInfoView,
                Collections.emptyMap()
        );
    }
}
