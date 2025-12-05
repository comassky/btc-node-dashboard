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
class RpcServicesTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testGetNodeInfo_success() throws Exception {
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

        NodeInfo nodeInfo = rpcServices.getNodeInfo();

        assertNotNull(nodeInfo);
        assertEquals(270000, nodeInfo.version());
        assertEquals(70016, nodeInfo.protocolVersion());
        assertEquals("/Satoshi:27.0.0/", nodeInfo.subversion());
    }

    @Test
    void testGetBestBlockHash_success() throws Exception {
        String mockResponse = """
            {
                "result": "00000000000000000001234567890abcdef",
                "error": null,
                "id": "quarkus-getbestblockhash"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        String blockHash = rpcServices.getBestBlockHash();

        assertNotNull(blockHash);
        assertEquals("00000000000000000001234567890abcdef", blockHash);
    }

    @Test
    void testGetBlockchainInfo_success() throws Exception {
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

        BlockchainInfo blockchainInfo = rpcServices.getBlockchainInfo();

        assertNotNull(blockchainInfo);
        assertEquals(870000, blockchainInfo.getBlocks());
        assertEquals(870000, blockchainInfo.getHeaders());
        assertEquals("main", blockchainInfo.getChain());
        assertEquals(0.9999, blockchainInfo.getVerificationProgress(), 0.0001);
    }

    @Test
    void testGetUptimeSeconds_success() throws Exception {
        String mockResponse = """
            {
                "result": 432000,
                "error": null,
                "id": "quarkus-uptime"
            }
            """;

        when(rpcClient.executeRpcCall(any())).thenReturn(mockResponse);

        long uptime = rpcServices.getUptimeSeconds();

        assertEquals(432000, uptime);
    }

    @Test
    void testGetBlockInfo_success() throws Exception {
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

        BlockInfo blockInfo = rpcServices.getBlockInfo("00000000000000000001234567890abcdef");

        assertNotNull(blockInfo);
        assertEquals(1733443200, blockInfo.getTime());
        assertEquals(2500, blockInfo.getNtx());
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

        assertThrows(RpcException.class, () -> rpcServices.getBlockchainInfo());
    }

    @Test
    void testRpcConnectionFailure_throwsException() {
        when(rpcClient.executeRpcCall(any())).thenThrow(new RuntimeException("Connection refused"));

        assertThrows(RpcException.class, () -> rpcServices.getNodeInfo());
    }
}
