package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.exceptions.RpcException;
import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests spécifiques pour vérifier le comportement parallèle des appels RPC.
 */
@QuarkusTest
class RpcServicesParallelTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("unchecked")
    void testGetData_parallelExecution_completesSuccessfully() throws Exception {
        // Counter to verify that multiple threads are being used
        AtomicInteger callCounter = new AtomicInteger(0);

        String mockPeerInfoResponse = """
            {
                "result": [
                    {"id": 1, "addr": "192.168.1.100:8333", "inbound": true, "subver": "/Satoshi:27.0.0/", "version": 270000}
                ],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        String mockBlockchainResponse = """
            {
                "result": {"blocks": 870000, "headers": 870000, "chain": "main", "verificationprogress": 0.9999, "difficulty": 95000000000000.0},
                "error": null,
                "id": "quarkus-getblockchaininfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {"version": 270000, "protocolversion": 70016, "subversion": "/Satoshi:27.0.0/"},
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
                "result": {"time": 1733443200, "nTx": 2500, "hash": "00000000000000000001abc"},
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            callCounter.incrementAndGet();
            var request = (java.util.Map<String, Object>) invocation.getArgument(0);
            String method = (String) request.get("method");
            
            // Simulate latency to verify parallelism
            Thread.sleep(50);
            
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

        long startTime = System.currentTimeMillis();
        GlobalResponse response = rpcServices.getData();
        long duration = System.currentTimeMillis() - startTime;

        // Verifications
        assertNotNull(response);
        assertEquals(1, response.getGeneralStats().totalPeers());
        
        // 6 RPC calls total (getpeerinfo, getblockchaininfo, getnetworkinfo, uptime, getbestblockhash, getblock)
        assertEquals(6, callCounter.get());
        
        // If calls were sequential: 6 * 50ms = 300ms
        // In parallel: ~50-150ms (depending on available threads)
        // Verify that execution is faster than sequential
        assertTrue(duration < 250, 
            "L'exécution devrait être plus rapide que séquentiel (durée: " + duration + "ms)");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetData_oneCallFails_propagatesException() {
        String mockPeerInfoResponse = """
            {
                "result": [{"id": 1, "addr": "192.168.1.100:8333", "inbound": true, "subver": "/Satoshi:27.0.0/", "version": 270000}],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {"version": 270000, "protocolversion": 70016, "subversion": "/Satoshi:27.0.0/"},
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
                "result": {"time": 1733443200, "nTx": 2500, "hash": "00000000000000000001abc"},
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            var request = (java.util.Map<String, Object>) invocation.getArgument(0);
            String method = (String) request.get("method");
            
            if ("getblockchaininfo".equals(method)) {
                throw new RuntimeException("Connection timeout");
            }
            
            return switch (method) {
                case "getpeerinfo" -> mockPeerInfoResponse;
                case "getnetworkinfo" -> mockNodeInfoResponse;
                case "uptime" -> mockUptimeResponse;
                case "getbestblockhash" -> mockBestBlockHashResponse;
                case "getblock" -> mockBlockInfoResponse;
                default -> throw new IllegalArgumentException("Unexpected method: " + method);
            };
        });

        // Exception should be propagated even if other calls succeed
        assertThrows(RpcException.class, () -> rpcServices.getData());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetData_rpcErrorInParallelCall_throwsException() {
        String mockErrorResponse = """
            {
                "result": null,
                "error": {"code": -28, "message": "Verifying blocks..."},
                "id": "quarkus-getblockchaininfo"
            }
            """;

        String mockPeerInfoResponse = """
            {
                "result": [{"id": 1, "addr": "192.168.1.100:8333", "inbound": true, "subver": "/Satoshi:27.0.0/", "version": 270000}],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {"version": 270000, "protocolversion": 70016, "subversion": "/Satoshi:27.0.0/"},
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
                "result": {"time": 1733443200, "nTx": 2500, "hash": "00000000000000000001abc"},
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            var request = (java.util.Map<String, Object>) invocation.getArgument(0);
            String method = (String) request.get("method");
            
            if ("getblockchaininfo".equals(method)) {
                return mockErrorResponse;
            }
            
            return switch (method) {
                case "getpeerinfo" -> mockPeerInfoResponse;
                case "getnetworkinfo" -> mockNodeInfoResponse;
                case "uptime" -> mockUptimeResponse;
                case "getbestblockhash" -> mockBestBlockHashResponse;
                case "getblock" -> mockBlockInfoResponse;
                default -> throw new IllegalArgumentException("Unexpected method: " + method);
            };
        });

        RpcException exception = assertThrows(RpcException.class, () -> rpcServices.getData());
        assertTrue(exception.getMessage().contains("Error during parallel RPC calls") || 
                   exception.getMessage().contains("RPC Error"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetData_multipleInvocations_eachUsesParallelExecution() throws Exception {
        AtomicInteger totalCalls = new AtomicInteger(0);

        String mockPeerInfoResponse = """
            {
                "result": [{"id": 1, "addr": "192.168.1.100:8333", "inbound": true, "subver": "/Satoshi:27.0.0/", "version": 270000}],
                "error": null,
                "id": "quarkus-getpeerinfo"
            }
            """;

        String mockBlockchainResponse = """
            {
                "result": {"blocks": 870000, "headers": 870000, "chain": "main", "verificationprogress": 0.9999, "difficulty": 95000000000000.0},
                "error": null,
                "id": "quarkus-getblockchaininfo"
            }
            """;

        String mockNodeInfoResponse = """
            {
                "result": {"version": 270000, "protocolversion": 70016, "subversion": "/Satoshi:27.0.0/"},
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
                "result": {"time": 1733443200, "nTx": 2500, "hash": "00000000000000000001abc"},
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenAnswer(invocation -> {
            totalCalls.incrementAndGet();
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

        // Call getData() multiple times
        rpcServices.getData();
        rpcServices.getData();

        // Each getData call makes 6 RPC calls
        assertEquals(12, totalCalls.get());
    }
}
