package comasky;

import comasky.api.BitcoinApiController;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.dto.SubverStats;
import comasky.rpcClass.responses.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@QuarkusTest
class BitcoinApiControllerTest {

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
        String hash = "0000000000000000000dummyhash";
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

    @InjectMock
    RpcServices rpcServices;


    @Test
    void testInstantiationWithMock() {
        comasky.rpcClass.RpcServices rpcServices = org.mockito.Mockito.mock(comasky.rpcClass.RpcServices.class);
        BitcoinApiController controller = new BitcoinApiController(rpcServices);
        assertNotNull(controller);
    }

    private GlobalResponse createMockGlobalResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfoResponse blockchainInfoResponse = new BlockchainInfoResponse(
            "main", // chain
            870000,  // blocks
            870000,  // headers
            "0000000000000000000dummyhash", // bestblockhash
            0.9999,  // difficulty
            1700000000L, // time
            1700000000L, // mediantime
            0.9999,  // verificationprogress
            false,   // initialblockdownload
            "0000000000000000000000000000000000000000000000000000000000000000", // chainwork
            1000000000L, // size_on_disk
            false,   // pruned
            null     // pruneheight
        );

        NetworkInfoResponse nodeInfo = new NetworkInfoResponse(
            70016, // version
            "/Satoshi:27.0.0/", // subversion
            270000, // protocolversion
            "0000000000000000", // localservices
            java.util.Collections.emptyList(), // localservicesnames
            true, // localrelay
            0, // timeoffset
            10, // connections
            true, // networkactive
            java.util.Collections.emptyList(), // networks
            java.util.Collections.emptyList() // localaddresses
        );

        PeerInfoResponse peer1 = new PeerInfoResponse(1, "192.168.1.1:8333", null, null, 0, 0, 0, 2000000L, 1000000L, null, null, 0, 0, 0, 0, "/Satoshi:27.0.0/", true, null, 0, null, null, 0);
        PeerInfoResponse peer2 = new PeerInfoResponse(2, "192.168.1.2:8333", null, null, 0, 0, 0, 1500000L, 500000L, null, null, 0, 0, 0, 0, "/Satoshi:26.0.0/", false, null, 0, null, null, 0);

        List<PeerInfoResponse> inboundPeers = List.of(peer1, peer1);
        List<PeerInfoResponse> outboundPeers = List.of(peer2, peer2, peer2, peer2, peer2, peer2, peer2, peer2);

        SubverStats inboundStats = new SubverStats("/Satoshi:27.0.0/", 100.0);
        SubverStats outboundStats = new SubverStats("/Satoshi:26.0.0/", 100.0);

        SubverDistribution distribution = new SubverDistribution(
                List.of(inboundStats),
                List.of(outboundStats)
        );

        BlockInfoResponse blockInfoResponse = new BlockInfoResponse(null, 0, 0, 0, 0, 0, 0, null, null, System.currentTimeMillis() / 1000, 0, 0, null, 0, null, 2500, null, null);

        return new GlobalResponse(
                generalStats,
                distribution,
                inboundPeers,
                outboundPeers,
                blockchainInfoResponse,
                nodeInfo,
                "5 days 3 hours",
                blockInfoResponse,
                new MempoolInfoResponse(
                        true, 0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0
                )
        );
    }
}