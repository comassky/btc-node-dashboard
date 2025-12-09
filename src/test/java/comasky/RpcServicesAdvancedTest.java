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
class RpcServicesAdvancedTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    // Helper to create a successful RpcResponse JSON string
    private <T> String createSuccessRpcResponseJson(T result) throws Exception {
        RpcResponse<T> response = new RpcResponse<>();
        response.setResult(result);
        response.setId("1.0");
        return objectMapper.writeValueAsString(response);
    }

    // Helper to create an error RpcResponse JSON string
    private String createErrorRpcResponseJson(Object error) throws Exception {
        RpcResponse<Object> response = new RpcResponse<>();
        response.setError(error);
        response.setId("1.0");
        return objectMapper.writeValueAsString(response);
    }

    // Helper to extract the RPC method name
    private String extractMethodName(RpcRequestDto request) {
        return request.method();
    }

    // Helper to set up common RPC client mocks
    private void setupRpcClientMock(Map<String, Object> responses) {
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                RpcRequestDto request = invocation.getArgument(0);
                String method = extractMethodName(request);
                if (method == null) {
                    throw new IllegalArgumentException("Method cannot be null or extracted from request");
                }
                Object responseResult = responses.get(method);
                if (responseResult != null) {
                    
                    if ("uptime".equals(method) && !(responseResult instanceof Long)) {
                        return createSuccessRpcResponseJson(Long.valueOf(responseResult.toString()));
                    }
                    if ("getbestblockhash".equals(method) && !(responseResult instanceof String)) {
                        return createSuccessRpcResponseJson(responseResult.toString());
                    }
                    return createSuccessRpcResponseJson(responseResult);
                }
                throw new IllegalArgumentException("Unexpected method: " + method);
            }
        });
    }

    // Helper to create a PeerInfo instance
    private PeerInfo createPeerInfo(String id, String addr, boolean inbound, String subver, int version) {
        return new PeerInfo(
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
    void testGetData_withMultiplePeers() throws Exception {
        Map<String, Object> mockResponses = Map.of(
            "getpeerinfo", List.of(
                createPeerInfo("1", "192.168.1.100:8333", true, "/Satoshi:27.0.0/", 270000),
                createPeerInfo("2", "192.168.1.101:8333", false, "/Satoshi:26.0.0/", 260000),
                createPeerInfo("3", "192.168.1.102:8333", true, "/Satoshi:27.0.0/", 270000)
            ),
            "getblockchaininfo", new BlockchainInfo(870000, 870000, "main", 0.99, false),
            "getnetworkinfo", new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true),
            "uptime", 432000L,
            "getbestblockhash", "00000000000000000001abc", // This is a String object, will be wrapped
            "getblock", new BlockInfo(
                "00000000000000000001abc", 1, 0, 0, 0, 870000, 1, "", "", 1733443200L, 0L, 0L, "", 1.0, "", 2500, "", ""
            )
        );
        setupRpcClientMock(mockResponses);

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
    void testGetData_emptyPeerList() throws Exception {
        Map<String, Object> mockResponses = Map.of(
            "getpeerinfo", List.of(),
            "getblockchaininfo", new BlockchainInfo(870000, 870000, "main", 0.99, false),
            "getnetworkinfo", new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true),
            "uptime", 432000L,
            "getbestblockhash", "00000000000000000001abc",
            "getblock", new BlockInfo(
                "00000000000000000001abc", 1, 0, 0, 0, 870000, 1, "", "", 1733443200L, 0L, 0L, "", 1.0, "", 2500, "", ""
            )
        );
        setupRpcClientMock(mockResponses);

        GlobalResponse response = rpcServices.getData().await().indefinitely();

        assertNotNull(response);
        assertEquals(0, response.generalStats().inboundCount());
        assertEquals(0, response.generalStats().outboundCount());
        assertEquals(0, response.generalStats().totalPeers());
        assertTrue(response.inboundPeer().isEmpty());
        assertTrue(response.outboundPeer().isEmpty());
    }

    @Test
    void testGetBlockInfo_withVerbosity() throws Exception {
        BlockInfo expectedBlockInfo = new BlockInfo(
              "00000000000000000001abc", // hash
              1, // confirmations
              0, // strippedsize
              0, // size
              0, // weight
              870000, // height
              1, // version
              "", // versionHex
              "", // merkleroot
              1733443200L, // time
              0L, // mediantime
              0L, // nonce
              "", // bits
              1.0, // difficulty
              "", // chainwork
              2500, // ntx
              "", // previousblockhash
              "" // nextblockhash
        );
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedBlockInfo));

        BlockInfo blockInfo = rpcServices.getBlockInfo("00000000000000000001abc").await().indefinitely();

        assertNotNull(blockInfo);
        assertEquals(1733443200, blockInfo.time());
        assertEquals(2500, blockInfo.ntx());
    }

    @Test
    void testRpcError_withDetailedMessage() throws Exception {
        // Simulate an RPC error by returning an error JSON response
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createErrorRpcResponseJson("Loading block index..."));

        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getBlockchainInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("RPC Error for method getblockchaininfo: Loading block index..."));
    }

    @Test
    void testRpcError_nullResult() throws Exception {
        // Simulate an RPC error by returning an error JSON response
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createErrorRpcResponseJson("Null result error"));

        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getNodeInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("RPC Error for method getnetworkinfo: Null result error"));
    }

    @Test
    void testJsonParsingError_invalidResponse() {
        // Simulate a parsing error by returning an invalid JSON string
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn("this is not valid json");
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getNodeInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("Connection failed for method getnetworkinfo: Unrecognized token 'this'"));
    }

    @Test
    void testGetData_parsingError() {
        // Simulate a parsing error for getpeerinfo
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenAnswer(invocation -> {
            RpcRequestDto request = invocation.getArgument(0);
            if ("getpeerinfo".equals(request.method())) {
                return "not valid json";
            }
            // For other methods, return a compatible simple type (e.g., Long or String)
            if ("uptime".equals(request.method())) {
                return createSuccessRpcResponseJson(432000L);
            }
            if ("getbestblockhash".equals(request.method())) {
                return createSuccessRpcResponseJson("00000000000000000001abc");
            }
            // For others, return a dummy object
            try {
                return createSuccessRpcResponseJson(Map.of("status", "ok"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        GlobalResponse response = rpcServices.getData().await().indefinitely();
        assertNotNull(response);
        // Fallback: peers should be empty due to parsing error
        assertEquals(0, response.generalStats().inboundCount());
        assertEquals(0, response.generalStats().outboundCount());
        assertEquals(0, response.generalStats().totalPeers());
        assertTrue(response.inboundPeer().isEmpty());
        assertTrue(response.outboundPeer().isEmpty());
    }

    @Test
    void testGetData_rpcErrorInPeerInfo() throws Exception {
        String mockPeerInfoResponse = createErrorRpcResponseJson(Map.of("code", -1, "message", "Peer info unavailable"));
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenAnswer(invocation -> {
            RpcRequestDto request = invocation.getArgument(0);
            if ("getpeerinfo".equals(request.method())) {
                return mockPeerInfoResponse;
            }
            if ("uptime".equals(request.method())) {
                return createSuccessRpcResponseJson(432000L);
            }
            if ("getbestblockhash".equals(request.method())) {
                return createSuccessRpcResponseJson("00000000000000000001abc");
            }
            try {
                return createSuccessRpcResponseJson(Map.of("status", "ok"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        GlobalResponse response = rpcServices.getData().await().indefinitely();
        assertNotNull(response);
        // Fallback: peers should be empty due to RPC error
        assertEquals(0, response.generalStats().inboundCount());
        assertEquals(0, response.generalStats().outboundCount());
        assertEquals(0, response.generalStats().totalPeers());
        assertTrue(response.inboundPeer().isEmpty());
        assertTrue(response.outboundPeer().isEmpty());
    }

    @Test
    void testConnectionFailure_withNetworkError() {
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenThrow(new RuntimeException("Network unreachable"));
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getBlockchainInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertTrue(exception.getCause().getMessage().contains("Network unreachable"));
    }

    @Test
    void testConnectionFailure_timeout() {
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenThrow(new RuntimeException("Request timeout"));
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getUptimeSeconds().await().indefinitely());
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertTrue(exception.getCause().getMessage().contains("timeout"));
    }
}
