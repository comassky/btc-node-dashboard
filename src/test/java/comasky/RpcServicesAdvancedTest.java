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
    void testGetData_withMultiplePeers() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [
                    {
                        "id": 1,
                        "addr": "192.168.1.100:8333",
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/",
                        "version": 270000
                    },
                    {
                        "id": 2,
                        "addr": "192.168.1.101:8333",
                        "inbound": false,
                        "subver": "/Satoshi:26.0.0/",
                        "version": 260000
                    },
                    {
                        "id": 3,
                        "addr": "192.168.1.102:8333",
                        "inbound": true,
                        "subver": "/Satoshi:27.0.0/",
                        "version": 270000
                    }
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        String mockBlockchainResponse = """
            {
                "result": {
                    "blocks": 870000,
                    "chain": "main"
                },
                "error": null,
                "id": "quarkus-getblockchaininfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {
                    "version": 270000,
                    "subversion": "/Satoshi:27.0.0/"
                },
                "error": null,
                "id": "quarkus-getnetworkinfo"
            }
            """;

        String mockUptimeResponse = """
            {
                "result": 432000,
                "error": null,
                "id": "quarkus-uptime"
            }
            """;

        String mockBestBlockHashResponse = """
            {
                "result": "00000000000000000001abc",
                "error": null,
                "id": "quarkus-getbestblockhash"
            }
            """;

        String mockBlockInfoResponse = """
            {
                "result": {
                    "time": 1733443200,
                    "nTx": 2500
                },
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

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

        GlobalResponse response = rpcServices.getData();

        assertNotNull(response);
        assertEquals(2, response.getGeneralStats().inboundCount());
        assertEquals(1, response.getGeneralStats().outboundCount());
        assertEquals(3, response.getGeneralStats().totalPeers());
        assertEquals(2, response.getInboundPeer().size());
        assertEquals(1, response.getOutboundPeer().size());
        
        assertNotNull(response.getSubverDistribution());
        assertNotNull(response.getSubverDistribution().inbound());
        assertNotNull(response.getSubverDistribution().outbound());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetData_emptyPeerList() throws Exception {
        String mockPeerInfoResponse = """
            {
                "result": [],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        String mockBlockchainResponse = """
            {
                "result": {"blocks": 870000, "chain": "main"},
                "error": null,
                "id": "quarkus-getblockchaininfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {"version": 270000, "subversion": "/Satoshi:27.0.0/"},
                "error": null,
                "id": "quarkus-getnetworkinfo"
            }
            """;

        String mockUptimeResponse = """
            {
                "result": 432000,
                "error": null,
                "id": "quarkus-uptime"
            }
            """;

        String mockBestBlockHashResponse = """
            {
                "result": "00000000000000000001abc",
                "error": null,
                "id": "quarkus-getbestblockhash"
            }
            """;

        String mockBlockInfoResponse = """
            {
                "result": {"time": 1733443200, "nTx": 2500},
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

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

        GlobalResponse response = rpcServices.getData();

        assertNotNull(response);
        assertEquals(0, response.getGeneralStats().inboundCount());
        assertEquals(0, response.getGeneralStats().outboundCount());
        assertEquals(0, response.getGeneralStats().totalPeers());
        assertTrue(response.getInboundPeer().isEmpty());
        assertTrue(response.getOutboundPeer().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetLastBlockTimestamp_success() throws Exception {
        String mockBestBlockHashResponse = """
            {
                "result": "00000000000000000001abc",
                "error": null,
                "id": "quarkus-getbestblockhash"
            }
            """;

        String mockBlockInfoResponse = """
            {
                "result": {
                    "time": 1733443200,
                    "nTx": 2500
                },
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            var request = (java.util.Map<String, Object>) invocation.getArgument(0);
            String method = (String) request.get("method");
            
            return switch (method) {
                case "getbestblockhash" -> mockBestBlockHashResponse;
                case "getblock" -> mockBlockInfoResponse;
                default -> throw new IllegalArgumentException("Unexpected method: " + method);
            };
        });

        long timestamp = rpcServices.getLastBlockTimestamp();

        assertEquals(1733443200, timestamp);
    }

    @Test
    void testGetBlockInfo_withVerbosity() throws Exception {
        String mockResponse = """
            {
                "result": {
                    "time": 1733443200,
                    "nTx": 2500,
                    "height": 870000,
                    "hash": "00000000000000000001abc"
                },
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        BlockInfo blockInfo = rpcServices.getBlockInfo("00000000000000000001abc");

        assertNotNull(blockInfo);
        assertEquals(1733443200, blockInfo.getTime());
        assertEquals(2500, blockInfo.getNtx());
    }

    @Test
    void testRpcError_withDetailedMessage() {
        String mockResponse = """
            {
                "result": null,
                "error": {
                    "code": -28,
                    "message": "Loading block index..."
                },
                "id": "quarkus-getblockchaininfo"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        RpcException exception = assertThrows(RpcException.class, 
            () -> rpcServices.getBlockchainInfo());
        
        assertTrue(exception.getMessage().contains("Loading block index"));
    }

    @Test
    void testRpcError_nullResult() {
        String mockResponse = """
            {
                "result": null,
                "error": null,
                "id": "quarkus-getnetworkinfo"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        assertThrows(RpcException.class, () -> rpcServices.getNodeInfo());
    }

    @Test
    void testJsonParsingError_invalidResponse() {
        String invalidJson = "{ invalid json }";

        when(rpcClient.executeRpcCall(any())).thenReturn(invalidJson);

        assertThrows(RpcException.class, () -> rpcServices.getNodeInfo());
    }

    @Test
    void testGetData_parsingError() {
        String invalidPeerResponse = "not valid json";

        when(rpcClient.executeRpcCall(any())).thenReturn(invalidPeerResponse);

        assertThrows(RpcException.class, () -> rpcServices.getData());
    }

    @Test
    void testGetData_rpcErrorInPeerInfo() {
        String mockPeerInfoResponse = """
            {
                "result": null,
                "error": {
                    "code": -1,
                    "message": "Peer info unavailable"
                },
                "id": "quarkus-getpeerinfo"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockPeerInfoResponse);

        RpcException exception = assertThrows(RpcException.class, 
            () -> rpcServices.getData());
        
        assertTrue(exception.getMessage().contains("RPC Error"));
    }

    @Test
    void testConnectionFailure_withNetworkError() {
        when(rpcClient.executeRpcCall(any()))
            .thenThrow(new RuntimeException("Network unreachable"));

        RpcException exception = assertThrows(RpcException.class, 
            () -> rpcServices.getBlockchainInfo());
        
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertTrue(exception.getMessage().contains("Network unreachable"));
    }

    @Test
    void testConnectionFailure_timeout() {
        when(rpcClient.executeRpcCall(any()))
            .thenThrow(new RuntimeException("Request timeout"));

        RpcException exception = assertThrows(RpcException.class, 
            () -> rpcServices.getUptimeSeconds());
        
        assertTrue(exception.getMessage().contains("timeout"));
    }
}
