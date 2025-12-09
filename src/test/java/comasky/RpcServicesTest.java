
package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.exceptions.RpcException;
import comasky.rpcClass.BlockInfo;
import comasky.rpcClass.BlockchainInfo;
import comasky.rpcClass.NodeInfo;
import comasky.rpcClass.RpcServices;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class RpcServicesTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testGetNodeInfo_success() {
        String mockResponse = """
            {
                "result": {
                    "version": 270000,
                    "protocolversion": 70016,
                    "subversion": "/Satoshi:27.0.0/",
                    "connections": 10,
                    "networkactive": true
                },
                "error": null,
                "id": "quarkus-getnetworkinfo"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        NodeInfo nodeInfo = rpcServices.getNodeInfo().await().indefinitely();

        assertNotNull(nodeInfo);
        assertEquals(270000, nodeInfo.version());
        assertEquals(70016, nodeInfo.protocolVersion());
        assertEquals("/Satoshi:27.0.0/", nodeInfo.subversion());
    }

    @Test
    void testGetBestBlockHash_success() {
        String mockResponse = """
            {
                "result": "00000000000000000001234567890abcdef",
                "error": null,
                "id": "quarkus-getbestblockhash"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        String blockHash = rpcServices.getBestBlockHash().await().indefinitely();

        assertNotNull(blockHash);
        assertEquals("00000000000000000001234567890abcdef", blockHash);
    }

    @Test
    void testGetBlockchainInfo_success() {
        String mockResponse = """
            {
                "result": {
                    "blocks": 870000,
                    "headers": 870000,
                    "chain": "main",
                    "verificationprogress": 0.9999,
                    "difficulty": 95000000000000.0
                },
                "error": null,
                "id": "quarkus-getblockchaininfo"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        BlockchainInfo blockchainInfo = rpcServices.getBlockchainInfo().await().indefinitely();

        assertNotNull(blockchainInfo);
        assertEquals(870000, blockchainInfo.blocks());
        assertEquals(870000, blockchainInfo.headers());
        assertEquals("main", blockchainInfo.chain());
        assertEquals(0.9999, blockchainInfo.verificationProgress(), 0.0001);
    }

    @Test
    void testGetUptimeSeconds_success() {
        String mockResponse = """
            {
                "result": 432000,
                "error": null,
                "id": "quarkus-uptime"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        long uptime = rpcServices.getUptimeSeconds().await().indefinitely();

        assertEquals(432000, uptime);
    }

    @Test
    void testGetBlockInfo_success() {
        String mockResponse = """
            {
                "result": {
                    "time": 1733443200,
                    "nTx": 2500
                },
                "error": null,
                "id": "quarkus-getblock"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        BlockInfo blockInfo = rpcServices.getBlockInfo("00000000000000000001234567890abcdef").await().indefinitely();

        assertNotNull(blockInfo);
        assertEquals(1733443200, blockInfo.time());
        assertEquals(2500, blockInfo.ntx());
    }

    @Test
    void testRpcError_throwsException() {
        String mockResponse = """
            {
                "result": null,
                "error": {
                    "code": -28,
                    "message": "Verifying blocks..."
                },
                "id": "quarkus-getblockchaininfo"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        RpcException exception = assertThrows(RpcException.class, () -> {
            rpcServices.getBlockchainInfo().await().indefinitely();
        });
        
        assertTrue(exception.getMessage().contains("Verifying blocks..."));
    }

    @Test
    void testRpcConnectionFailure_throwsException() {
        when(rpcClient.executeRpcCall(any())).thenThrow(new RuntimeException("Connection refused"));

        RpcException exception = assertThrows(RpcException.class, () -> {
            rpcServices.getNodeInfo().await().indefinitely();
        });
        
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Connection refused"));
    }

    @Test
    void testInstantiationWithMocks() {
        final ObjectMapper objectMapper = Mockito.mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        RpcClient rpcClient = Mockito.mock(comasky.client.RpcClient.class);
        RpcServices rpcServices = new RpcServices(objectMapper, rpcClient);
        assertNotNull(rpcServices);
    }
}