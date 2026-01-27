package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.rpcClass.RpcResponse;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverStats;
import comasky.rpcClass.responses.*;
import comasky.service.CacheProvider;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class SubverStatsCalculationTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    RpcServices rpcServices;

    @Inject
    CacheProvider cacheProvider;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // Invalidate cache before each test to ensure isolation
        cacheProvider.invalidateAll();
    }

    private <T> String createSuccessRpcResponseJson(T result) throws Exception {
        RpcResponse<T> response = new RpcResponse<>();
        response.setResult(result);
        response.setId("1.0");
        return objectMapper.writeValueAsString(response);
    }

    private PeerInfoResponse createPeerInfo(String id, String addr, boolean inbound, String subver, int version) {
        return new PeerInfoResponse(
            Integer.parseInt(id),
            addr,
            "127.0.0.1:8333",
            "00000001",
            System.currentTimeMillis() / 1000 - 3600,
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000,
            1024 * 1024,
            512 * 1024,
            Collections.emptyMap(),
            Collections.emptyMap(),
            0.1,
            0.05,
            0,
            version,
            subver,
            inbound,
            "V2",
            0,
            "inbound",
            "mainnet",
            0
        );
    }

    @Test
    void testSubverDistribution_singleVersion() throws Exception {
        List<PeerInfoResponse> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        assertNotNull(response.subverDistribution());
        List<SubverStats> inboundStats = response.subverDistribution().inbound();

        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server());
        assertEquals(100.0, inboundStats.get(0).percentage());
    }

    @Test
    void testSubverDistribution_multipleVersions() throws Exception {
        List<PeerInfoResponse> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:26.0.0/", 260000),
                createPeerInfo("4", "192.168.1.103:8333", true, "/Satoshi:25.0.0/", 250000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        List<SubverStats> inboundStats = response.subverDistribution().inbound();

        assertEquals(3, inboundStats.size());

        SubverStats v27Stats = inboundStats.stream()
            .filter(s -> s.server().equals("/Satoshi:27.0.0/"))
            .findFirst()
            .orElseThrow();

        assertEquals(50.0, v27Stats.percentage());
    }

    @Test
    void testSubverDistribution_withNullSubver() throws Exception {
        List<PeerInfoResponse> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, null, 0), // Null subver
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        List<SubverStats> inboundStats = response.subverDistribution().inbound();

        
        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server());
        assertEquals(66.67, inboundStats.get(0).percentage(), 0.01);
    }

    @Test
    void testSubverDistribution_inboundVsOutbound() throws Exception {
        List<PeerInfoResponse> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", false, "/Satoshi:26.0.0/", 260000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("4", "192.168.1.103:8333", false, "/Satoshi:26.0.0/", 260000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        List<SubverStats> inboundStats = response.subverDistribution().inbound();
        List<SubverStats> outboundStats = response.subverDistribution().outbound();

        assertEquals(1, inboundStats.size());
        assertEquals(1, outboundStats.size());

        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server());
        assertEquals(100.0, inboundStats.get(0).percentage());

        assertEquals("/Satoshi:26.0.0/", outboundStats.get(0).server());
        assertEquals(100.0, outboundStats.get(0).percentage());
    }

    @Test
    void testSubverDistribution_percentageCalculation() throws Exception {
        List<PeerInfoResponse> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("4", "192.168.1.103:8333", true, "/Satoshi:26.0.0/", 260000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        List<SubverStats> inboundStats = response.subverDistribution().inbound();

        double totalPercentage = inboundStats.stream()
            .mapToDouble(SubverStats::percentage)
            .sum();

        assertEquals(100.0, totalPercentage, 0.01);
    }

    @Test
    void testSubverDistribution_emptyPeers() throws Exception {
        mockAllRpcCalls(createSuccessRpcResponseJson(List.of()));

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        assertTrue(response.generalStats().inboundCount() == 0);
        assertTrue(response.generalStats().outboundCount() == 0);
        assertTrue(response.subverDistribution().inbound().isEmpty());
        assertTrue(response.subverDistribution().outbound().isEmpty());
    }

    @Test
    void testSubverDistribution_roundingPrecision() throws Exception {
        List<PeerInfoResponse> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:26.0.0/", 260000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:26.0.0/", 260000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        List<SubverStats> inboundStats = response.subverDistribution().inbound();

        SubverStats v27 = inboundStats.stream()
            .filter(s -> s.server().equals("/Satoshi:27.0.0/"))
            .findFirst()
            .orElseThrow();

        assertEquals(33.33, v27.percentage(), 0.01);
    }

    private void mockAllRpcCalls(String peerInfoResponse) throws Exception {
        String mockBlockchainResponse = createSuccessRpcResponseJson(new BlockchainInfoResponse(
            "main", // chain
            870000,  // blocks
            870000,  // headers
            "0000000000000000000dummyhash", // bestblockhash
            0.99,    // difficulty
            1700000000L, // time
            1700000000L, // mediantime
            0.99,    // verificationprogress
            false,   // initialblockdownload
            "0000000000000000000000000000000000000000000000000000000000000000", // chainwork
            1000000000L, // size_on_disk
            false,   // pruned
            null     // pruneheight
        ));
        String mockNodeInfoResponse = createSuccessRpcResponseJson(new NetworkInfoResponse(
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
        ));
        String mockUptimeResponse = createSuccessRpcResponseJson(432000L);
        String mockBestBlockHashResponse = createSuccessRpcResponseJson("00000000000000000001abc");
        String mockBlockInfoResponse = createSuccessRpcResponseJson(new BlockInfoResponse(
                "00000000000000000001abc", 1, 0, 0, 0, 870000, 1, "", "", 1733443200L, 0L, 0L, "", 1.0, "", 2500, "", ""
        ));
        String mockMempoolInfoResponse = createSuccessRpcResponseJson(new MempoolInfoResponse(
                true, 0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0
        ));

        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenAnswer(invocation -> {
            RpcRequestDto request = invocation.getArgument(0);
            String method = request.method();

            switch (method) {
                case "getpeerinfo":
                    return peerInfoResponse;
                case "getblockchaininfo":
                    return mockBlockchainResponse;
                case "getnetworkinfo":
                    return mockNodeInfoResponse;
                case "uptime":
                    return mockUptimeResponse;
                case "getbestblockhash":
                    return mockBestBlockHashResponse;
                case "getblock":
                    return mockBlockInfoResponse;
                case "getmempoolinfo":
                    return mockMempoolInfoResponse;
                default:
                    throw new IllegalArgumentException("Unexpected method: " + method);
            }
        });
    }
}