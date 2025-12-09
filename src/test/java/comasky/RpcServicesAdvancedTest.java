package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.exceptions.RpcException;
import comasky.rpcClass.BlockInfo;
import comasky.rpcClass.BlockchainInfo;
import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.NodeInfo;
import comasky.rpcClass.PeerInfo; // Import PeerInfo
import comasky.rpcClass.RpcResponse; // Import RpcResponse
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

    // Helper method to extract the RPC method name from the request object
    private String extractMethodName(RpcRequestDto request) {
        return request.method();
    }

    // Helper method to set up common RPC client mocks
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
                    // Pour uptime et getbestblockhash, renvoyer le type simple attendu
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
    void testGetData_withMultiplePeers() throws Exception {
        Map<String, Object> mockResponses = Map.of(
            "getpeerinfo", List.of( // Direct list of PeerInfo objects
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
            "getpeerinfo", List.of(), // Empty list
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
        // Simule une erreur RPC en retournant une réponse JSON d'erreur
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createErrorRpcResponseJson("Loading block index..."));

        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getBlockchainInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("RPC Error for method getblockchaininfo: Loading block index..."));
    }

    @Test
    void testRpcError_nullResult() throws Exception {
        // Simule une erreur RPC en retournant une réponse JSON d'erreur
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createErrorRpcResponseJson("Null result error"));

        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getNodeInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("RPC Error for method getnetworkinfo: Null result error"));
    }

    @Test
    void testJsonParsingError_invalidResponse() {
        // Simule une erreur de parsing en retournant une chaîne JSON invalide
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn("this is not valid json");
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getNodeInfo().await().indefinitely());
        assertTrue(exception.getMessage().contains("Connection failed for method getnetworkinfo: Unrecognized token 'this'"));
    }

    @Test
    void testGetData_parsingError() {
        // Simule une erreur de parsing pour getpeerinfo
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenAnswer(invocation -> {
            RpcRequestDto request = invocation.getArgument(0);
            if ("getpeerinfo".equals(request.method())) {
                return "not valid json";
            }
            // Pour les autres méthodes, renvoyer un type simple compatible (ex: Long ou String)
            if ("uptime".equals(request.method())) {
                return createSuccessRpcResponseJson(432000L);
            }
            if ("getbestblockhash".equals(request.method())) {
                return createSuccessRpcResponseJson("00000000000000000001abc");
            }
            // Pour les autres, renvoyer un objet factice
            try {
                return createSuccessRpcResponseJson(Map.of("status", "ok"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertThrows(RpcException.class, () -> rpcServices.getData().await().indefinitely());
    }

    @Test
    void testGetData_rpcErrorInPeerInfo() throws Exception {
        String mockPeerInfoResponse = createErrorRpcResponseJson(Map.of("code", -1, "message", "Peer info unavailable"));
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenAnswer(invocation -> {
            RpcRequestDto request = invocation.getArgument(0);
            if ("getpeerinfo".equals(request.method())) {
                return mockPeerInfoResponse;
            }
            // Pour les autres méthodes, renvoyer un type simple compatible (ex: Long ou String)
            if ("uptime".equals(request.method())) {
                return createSuccessRpcResponseJson(432000L);
            }
            if ("getbestblockhash".equals(request.method())) {
                return createSuccessRpcResponseJson("00000000000000000001abc");
            }
            // Pour les autres, renvoyer un objet factice
            try {
                return createSuccessRpcResponseJson(Map.of("status", "ok"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getData().await().indefinitely());
        assertTrue(exception.getMessage().contains("RPC Error for method getpeerinfo: {code=-1, message=Peer info unavailable}"));
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
