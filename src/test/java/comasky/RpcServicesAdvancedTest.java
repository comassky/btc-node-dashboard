package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.exceptions.RpcException;
import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class RpcServicesAdvancedTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("unchecked")
    void testGetData_withMultiplePeers() {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {"id": 1, "addr": "192.168.1.100:8333", "inbound": true, "subver": "/Satoshi:27.0.0/", "version": 270000},
                    {"id": 2, "addr": "192.168.1.101:8333", "inbound": false, "subver": "/Satoshi:26.0.0/", "version": 260000},
                    {"id": 3, "addr": "192.168.1.102:8333", "inbound": true, "subver": "/Satoshi:27.0.0/", "version": 270000}
                ], "error": null, "id": "quarkus-getpeerinfo"
            }""";
        String mockBlockchainResponse = """
            {"result": {"blocks": 870000, "chain": "main", "verificationprogress": 0.99, "initialblockdownload": false}, "error": null, "id": "quarkus-getblockchaininfo"}""";
        String mockNodeInfoResponse = """
            {"result": {"version": 270000, "subversion": "/Satoshi:27.0.0/", "protocolversion": 70016, "connections": 10, "networkactive": true}, "error": null, "id": "quarkus-getnetworkinfo"}""";
        String mockUptimeResponse = """
            {"result": 432000, "error": null, "id": "quarkus-uptime"}""";
        String mockBestBlockHashResponse = """
            {"result": "00000000000000000001abc", "error": null, "id": "quarkus-getbestblockhash"}""";
        String mockBlockInfoResponse = """
            {"result": {"hash": "00000000000000000001abc", "time": 1733443200, "nTx": 2500}, "error": null, "id": "quarkus-getblock"}""";

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            var request = (java.util.Map<String, Object>) invocation.getArgument(0);
            String method = (String) request.get("method");
            return switch (method) {
                case "getpeerinfo" -> mockPeerInfoResponse;
                case "getblockchaininfo" -> mockBlockchainResponse;
                case "getnetworkinfo" -> mockNodeInfoResponse;
                case "uptime" -> mockUptimeResponse;
                case "getbestblockhash" -> mockBestBlockHashResponse;
                case "getblock" -> mockBlockInfoResponse;
                default -> throw new IllegalArgumentException("Unexpected method: " + method);
            };
        });

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        assertNotNull(response);
        assertEquals(2, response.generalStats().inboundCount());
        assertEquals(1, response.generalStats().outboundCount());
        assertEquals(3, response.generalStats().totalPeers());
        assertEquals(2, response.inboundPeer().size());
        assertEquals(1, response.outboundPeer().size());
        assertNotNull(response.subverDistribution());
        assertNotNull(response.subverDistribution().inbound());
        assertNotNull(response.subverDistribution().outbound());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetData_emptyPeerList() {
        String mockPeerInfoResponse = """
            {"result": [], "error": null, "id": "quarkus-getpeerinfo"}""";
        String mockBlockchainResponse = """
            {"result": {"blocks": 870000, "chain": "main", "verificationprogress": 0.99, "initialblockdownload": false}, "error": null, "id": "quarkus-getblockchaininfo"}""";
        String mockNodeInfoResponse = """
            {"result": {"version": 270000, "subversion": "/Satoshi:27.0.0/", "protocolversion": 70016, "connections": 10, "networkactive": true}, "error": null, "id": "quarkus-getnetworkinfo"}""";
        String mockUptimeResponse = """
            {"result": 432000, "error": null, "id": "quarkus-uptime"}""";
        String mockBestBlockHashResponse = """
            {"result": "00000000000000000001abc", "error": null, "id": "quarkus-getbestblockhash"}""";
        String mockBlockInfoResponse = """
            {"result": {"hash": "00000000000000000001abc", "time": 1733443200, "nTx": 2500}, "error": null, "id": "quarkus-getblock"}""";

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            var request = (java.util.Map<String, Object>) invocation.getArgument(0);
            String method = (String) request.get("method");
            return switch (method) {
                case "getpeerinfo" -> mockPeerInfoResponse;
                case "getblockchaininfo" -> mockBlockchainResponse;
                case "getnetworkinfo" -> mockNodeInfoResponse;
                case "uptime" -> mockUptimeResponse;
                case "getbestblockhash" -> mockBestBlockHashResponse;
                case "getblock" -> mockBlockInfoResponse;
                default -> throw new IllegalArgumentException("Unexpected method: " + method);
            };
        });

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        assertNotNull(response);
        assertEquals(0, response.generalStats().inboundCount());
        assertEquals(0, response.generalStats().outboundCount());
        assertEquals(0, response.generalStats().totalPeers());
        assertTrue(response.inboundPeer().isEmpty());
        assertTrue(response.outboundPeer().isEmpty());
    }

    @Test
    void testGetBlockInfo_withVerbosity() {
        String mockResponse = """
            {"result": {"time": 1733443200, "nTx": 2500, "height": 870000, "hash": "00000000000000000001abc"}, "error": null, "id": "quarkus-getblock"}""";

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        BlockInfo blockInfo = rpcServices.getBlockInfo("00000000000000000001abc").await().indefinitely();

        assertNotNull(blockInfo);
        assertEquals(1733443200, blockInfo.time());
        assertEquals(2500, blockInfo.ntx());
    }

    @Test
    void testRpcError_withDetailedMessage() {
        String mockResponse = """
            {"result": null, "error": {"code": -28, "message": "Loading block index..."}, "id": "quarkus-getblockchaininfo"}""";

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getBlockchainInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("Loading block index"));
    }

    @Test
    void testRpcError_nullResult() {
        String mockResponse = """
            {"result": null, "error": null, "id": "quarkus-getnetworkinfo"}""";

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        assertThrows(RpcException.class, () -> rpcServices.getNodeInfo().await().indefinitely());
    }

    @Test
    void testJsonParsingError_invalidResponse() {
        String invalidJson = "{ invalid json }";
        when(rpcClient.executeRpcCall(any())).thenReturn(invalidJson);
        assertThrows(RpcException.class, () -> rpcServices.getNodeInfo().await().indefinitely());
    }

    @Test
    void testGetData_parsingError() {
        String invalidPeerResponse = "not valid json";
        when(rpcClient.executeRpcCall(any())).thenReturn(invalidPeerResponse);
        assertThrows(RpcException.class, () -> rpcServices.getData().await().indefinitely());
    }

    @Test
    void testGetData_rpcErrorInPeerInfo() {
        String mockPeerInfoResponse = """
            {"result": null, "error": {"code": -1, "message": "Peer info unavailable"}, "id": "quarkus-getpeerinfo"}""";
        when(rpcClient.executeRpcCall(any())).thenReturn(mockPeerInfoResponse);
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getData().await().indefinitely());
        assertTrue(exception.getMessage().contains("RPC Error"));
    }

    @Test
    void testConnectionFailure_withNetworkError() {
        when(rpcClient.executeRpcCall(any())).thenThrow(new RuntimeException("Network unreachable"));
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getBlockchainInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertTrue(exception.getCause().getMessage().contains("Network unreachable"));
    }

    @Test
    void testConnectionFailure_timeout() {
        when(rpcClient.executeRpcCall(any())).thenThrow(new RuntimeException("Request timeout"));
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getUptimeSeconds().await().indefinitely());
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertTrue(exception.getCause().getMessage().contains("timeout"));
    }
}