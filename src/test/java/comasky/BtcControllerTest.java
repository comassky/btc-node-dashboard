package comasky;

import comasky.api.BitcoinApiController;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.dto.SubverStats;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import comasky.rpcClass.responses.MempoolInfoResponse;
import comasky.rpcClass.responses.PeerInfoResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class BtcControllerTest {

    @InjectMock
    RpcServices rpcServices;


    @Test
    void testInstantiationWithMock() {
        RpcServices rpcServices = org.mockito.Mockito.mock(comasky.rpcClass.RpcServices.class);
        BitcoinApiController controller = new BitcoinApiController(rpcServices);
        assertNotNull(controller);
    }

    private GlobalResponse createMockGlobalResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfoResponse blockchainInfoResponse = new BlockchainInfoResponse(
            "main", 870000, 870000, "", 0.9999, 0L, 0L, 0.9999, false, "", 0L, false, null
        );

        comasky.rpcClass.responses.NetworkInfoResponse nodeInfo = new comasky.rpcClass.responses.NetworkInfoResponse(
            70016, // version
            "/Satoshi:27.0.0/", // subversion
            70016, // protocolversion
            "", // localservices
            java.util.Collections.emptyList(), // localservicesnames
            true, // localrelay
            0, // timeoffset
            0, // connections
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

            MempoolInfoResponse mempoolInfo = new comasky.rpcClass.responses.MempoolInfoResponse(
            true, 0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0
        );

        return new GlobalResponse(
                generalStats,
                distribution,
                inboundPeers,
                outboundPeers,
                blockchainInfoResponse,
                nodeInfo,
                "5 days 3 hours",
                blockInfoResponse,
                mempoolInfo
        );
    }
}