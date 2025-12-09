package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
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

    
    private <T> String createSuccessRpcResponseJson(T result) throws Exception {
        RpcResponse<T> response = new RpcResponse<>();
        response.setResult(result);
        response.setId("1.0");
        return objectMapper.writeValueAsString(response);
    }

    
    private String createErrorRpcResponseJson(Object error) throws Exception {
        RpcResponse<Object> response = new RpcResponse<>();
        response.setError(error);
        response.setId("1.0");
        return objectMapper.writeValueAsString(response);
    }

    @Test
    void testGetNodeInfo_success() throws Exception {
        NodeInfo expectedNodeInfo = new NodeInfo(
            70016,
            270000,
            "/Satoshi:27.0.0/",
            10,
            true
        );
        
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedNodeInfo));

        NodeInfo nodeInfo = rpcServices.getNodeInfo().await().indefinitely();

        assertNotNull(nodeInfo);
        assertEquals(270000, nodeInfo.version());
        assertEquals(70016, nodeInfo.protocolVersion());
        assertEquals("/Satoshi:27.0.0/", nodeInfo.subversion());
    }

    @Test
    void testGetBestBlockHash_success() throws Exception {
        String expectedBlockHash = "00000000000000000001234567890abcdef";
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedBlockHash));

        String blockHash = rpcServices.getBestBlockHash().await().indefinitely();

        assertNotNull(blockHash);
        assertEquals("00000000000000000001234567890abcdef", blockHash);
    }

    @Test
    void testGetBlockchainInfo_success() throws Exception {
        BlockchainInfo expectedBlockchainInfo = new BlockchainInfo(
            870000,
            870000,
            "main",
            0.9999,
            false
        );
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedBlockchainInfo));

        BlockchainInfo blockchainInfo = rpcServices.getBlockchainInfo().await().indefinitely();

        assertNotNull(blockchainInfo);
        assertEquals(870000, blockchainInfo.blocks());
        assertEquals(870000, blockchainInfo.headers());
        assertEquals("main", blockchainInfo.chain());
        assertEquals(0.9999, blockchainInfo.verificationProgress(), 0.0001);
    }

    @Test
    void testGetUptimeSeconds_success() throws Exception {
        Long expectedUptime = 432000L;
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedUptime));

        long uptime = rpcServices.getUptimeSeconds().await().indefinitely();

        assertEquals(expectedUptime, uptime);
    }

    @Test
    void testGetBlockInfo_success() throws Exception {
            BlockInfo expectedBlockInfo = new BlockInfo(
                "00000000000000000001234567890abcdef",
                1,
                0,
                0,
                0,
                870000,
                1,
                "",
                "",
                1733443200L,
                0L,
                0L,
                "",
                1.0,
                "",
                2500,
                "",
                ""
            );
            when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedBlockInfo));

            BlockInfo blockInfo = rpcServices.getBlockInfo("00000000000000000001234567890abcdef").await().indefinitely();

            assertNotNull(blockInfo);
            assertEquals(1733443200, blockInfo.time());
            assertEquals(2500, blockInfo.ntx());
    }

    @Test
    void testRpcError_throwsException() throws Exception {
        
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createErrorRpcResponseJson("RPC Error: Verifying blocks..."));

        RpcException exception = assertThrows(RpcException.class,
            () -> rpcServices.getBlockchainInfo().await().indefinitely());
        
        
        assertTrue(exception.getMessage().contains("RPC Error for method getblockchaininfo: RPC Error: Verifying blocks..."));
    }

    @Test
    void testRpcConnectionFailure_throwsException() {
        
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenThrow(new RuntimeException("Connection refused"));

        RpcException exception = assertThrows(RpcException.class,
            () -> rpcServices.getNodeInfo().await().indefinitely());
        
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Connection refused"));
    }
}
