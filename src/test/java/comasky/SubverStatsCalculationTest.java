package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.exceptions.RpcException;
import comasky.rpcClass.BlockInfo;
import comasky.rpcClass.BlockchainInfo;
import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.NodeInfo;
import comasky.rpcClass.PeerInfo;
import comasky.rpcClass.RpcResponse;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.SubverStats;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    ObjectMapper objectMapper; // Inject ObjectMapper to help with JSON creation

    // Helper to create a successful RpcResponse JSON string
    private <T> String createSuccessRpcResponseJson(T result) throws Exception {
        RpcResponse<T> response = new RpcResponse<>();
        response.setResult(result);
        response.setId("1.0"); // Assuming a default ID
        return objectMapper.writeValueAsString(response);
    }

    // Helper to create an error RpcResponse JSON string
    private String createErrorRpcResponseJson(Object error) throws Exception {
        RpcResponse<Object> response = new RpcResponse<>();
        response.setError(error);
        response.setId("1.0"); // Assuming a default ID
        return objectMapper.writeValueAsString(response);
    }

    // Helper method to create a PeerInfo instance with default values for less relevant fields
    private PeerInfo createPeerInfo(String id, String addr, boolean inbound, String subver, int version) {
        return new PeerInfo(
                Integer.parseInt(id), // id
                addr, // addr
                "127.0.0.1:8333", // addrlocal
                "00000001", // services
                System.currentTimeMillis() / 1000 - 3600, // conntime (1 hour ago)
                System.currentTimeMillis() / 1000, // lastsend
                System.currentTimeMillis() / 1000, // lastrecv
                1024 * 1024, // bytesrecv
                512 * 1024, // bytessent
                Collections.emptyMap(), // bytesRecvPerMsg
                Collections.emptyMap(), // bytesSentPerMsg
                0.1, // pingtime
                0.05, // minping
                0, // timeoffset
                version, // version
                subver, // subver
                inbound, // inbound
                "V2", // transportProtocol
                0, // permission
                "inbound", // connectionType
                "mainnet", // network
                0 // unshippedTxs
        );
    }

    @Test
    void testSubverDistribution_singleVersion() throws Exception {
        List<PeerInfo> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        assertNotNull(response.subverDistribution()); // Use record accessor
        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server()); // Use record accessor
        assertEquals(100.0, inboundStats.get(0).percentage()); // Use record accessor
    }

    @Test
    void testSubverDistribution_multipleVersions() throws Exception {
        List<PeerInfo> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:26.0.0/", 260000),
                createPeerInfo("4", "192.168.1.103:8333", true, "/Satoshi:25.0.0/", 250000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        assertEquals(3, inboundStats.size());

        SubverStats v27Stats = inboundStats.stream()
                .filter(s -> s.server().equals("/Satoshi:27.0.0/")) // Use record accessor
                .findFirst()
                .orElseThrow();

        assertEquals(50.0, v27Stats.percentage()); // Use record accessor
    }

    @Test
    void testSubverDistribution_withNullSubver() throws Exception {
        List<PeerInfo> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, null, 0), // Null subver
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        // Only 1 distinct subversion, but percentage is calculated on total peers (including null)
        // 2 peers with /Satoshi:27.0.0/ out of 3 total = 66.67%
        assertEquals(1, inboundStats.size());
        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server()); // Use record accessor
        assertEquals(66.67, inboundStats.get(0).percentage(), 0.01); // Use record accessor
    }

    @Test
    void testSubverDistribution_inboundVsOutbound() throws Exception {
        List<PeerInfo> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", false, "/Satoshi:26.0.0/", 260000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("4", "192.168.1.103:8333", false, "/Satoshi:26.0.0/", 260000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor
        List<SubverStats> outboundStats = response.subverDistribution().outbound(); // Use record accessor

        assertEquals(1, inboundStats.size());
        assertEquals(1, outboundStats.size());

        assertEquals("/Satoshi:27.0.0/", inboundStats.get(0).server()); // Use record accessor
        assertEquals(100.0, inboundStats.get(0).percentage()); // Use record accessor

        assertEquals("/Satoshi:26.0.0/", outboundStats.get(0).server()); // Use record accessor
        assertEquals(100.0, outboundStats.get(0).percentage()); // Use record accessor
    }

    @Test
    void testSubverDistribution_percentageCalculation() throws Exception {
        List<PeerInfo> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("4", "192.168.1.103:8333", true, "/Satoshi:26.0.0/", 260000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        double totalPercentage = inboundStats.stream()
                .mapToDouble(SubverStats::percentage) // Use record accessor
                .sum();

        assertEquals(100.0, totalPercentage, 0.01);
    }

    @Test
    void testSubverDistribution_emptyPeers() throws Exception {
        mockAllRpcCalls(createSuccessRpcResponseJson(List.of())); // Empty list

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        assertTrue(response.generalStats().inboundCount() == 0);
        assertTrue(response.generalStats().outboundCount() == 0);
        assertTrue(response.subverDistribution().inbound().isEmpty()); // Use record accessor
        assertTrue(response.subverDistribution().outbound().isEmpty()); // Use record accessor
    }

    @Test
    void testSubverDistribution_roundingPrecision() throws Exception {
        List<PeerInfo> mockPeers = List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", true, "/Satoshi:26.0.0/", 260000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:26.0.0/", 260000)
        );
        mockAllRpcCalls(createSuccessRpcResponseJson(mockPeers));

        GlobalResponse response = rpcServices.getData().await().indefinitely(); // Await the Uni

        List<SubverStats> inboundStats = response.subverDistribution().inbound(); // Use record accessor

        SubverStats v27 = inboundStats.stream()
                .filter(s -> s.server().equals("/Satoshi:27.0.0/")) // Use record accessor
                .findFirst()
                .orElseThrow();

        assertEquals(33.33, v27.percentage(), 0.01); // Use record accessor
    }

    @SuppressWarnings("unchecked")
    private void mockAllRpcCalls(String peerInfoResponse) throws Exception {
        // These should be full RpcResponse JSON strings
        String mockBlockchainResponse = createSuccessRpcResponseJson(new BlockchainInfo(870000, 870000, "main", 0.99, false));
        String mockNodeInfoResponse = createSuccessRpcResponseJson(new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true));
        String mockUptimeResponse = createSuccessRpcResponseJson(432000L);
        String mockBestBlockHashResponse = createSuccessRpcResponseJson("00000000000000000001abc");
        String mockBlockInfoResponse = createSuccessRpcResponseJson(new BlockInfo(
                "00000000000000000001abc", 1, 0, 0, 0, 870000, 1, "", "", 1733443200L, 0L, 0L, "", 1.0, "", 2500, "", ""
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
                default:
                    throw new IllegalArgumentException("Unexpected method: " + method);
            }
        });
    }
}