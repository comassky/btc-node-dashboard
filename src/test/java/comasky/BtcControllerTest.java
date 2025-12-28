package comasky;

import comasky.api.BitcoinApiController;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.dto.SubverStats;
import comasky.rpcClass.view.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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

        BlockchainInfoView blockchainInfoView = new BlockchainInfoView(
            "main", 870000, 870000, 0.9999, 0L, 0L, 0.9999, false, "", 0L
        );

        NetworkInfoView nodeInfoView = new NetworkInfoView(
            70016, "/Satoshi:27.0.0/", 70016, java.util.Collections.emptyList(), java.util.Collections.emptyList()
        );

        PeerInfoView peer1 = new PeerInfoView(1, "192.168.1.1:8333", 0, 2000000L, 1000000L, 0, 0, 0, "/Satoshi:27.0.0/", true, null, null);
        PeerInfoView peer2 = new PeerInfoView(2, "192.168.1.2:8333", 0, 1500000L, 500000L, 0, 0, 0, "/Satoshi:26.0.0/", false, null, null);

        List<PeerInfoView> inboundPeers = List.of(peer1, peer1);
        List<PeerInfoView> outboundPeers = List.of(peer2, peer2, peer2, peer2, peer2, peer2, peer2, peer2);

        SubverStats inboundStats = new SubverStats("/Satoshi:27.0.0/", 100.0);
        SubverStats outboundStats = new SubverStats("/Satoshi:26.0.0/", 100.0);

        SubverDistribution distribution = new SubverDistribution(
                List.of(inboundStats),
                List.of(outboundStats)
        );

        BlockInfoView blockInfoView = new BlockInfoView(System.currentTimeMillis() / 1000, 2500);

        MempoolInfoView mempoolInfoView = new MempoolInfoView(
            0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0
        );

        return new GlobalResponse(
                generalStats,
                distribution,
                inboundPeers,
                outboundPeers,
                blockchainInfoView,
                nodeInfoView,
                "5 days 3 hours",
                blockInfoView,
                mempoolInfoView,
                Collections.emptyMap()
        );
    }
}
