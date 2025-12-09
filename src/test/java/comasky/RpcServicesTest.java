package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.exceptions.RpcException;
import comasky.rpcClass.BlockInfo;
import comasky.rpcClass.BlockchainInfo;
import comasky.rpcClass.NodeInfo;
import comasky.rpcClass.RpcResponse; // Import RpcResponse
import comasky.rpcClass.RpcServices;
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

    @Test
    void testGetNodeInfo_success() throws Exception {
        NodeInfo expectedNodeInfo = new NodeInfo(
            70016, // protocolVersion
            270000, // version
            "/Satoshi:27.0.0/", // subversion
            10, // connections
            true // networkActive
        );
        // Mock RpcClient to return the full JSON string
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
            870000, // blocks
            870000, // headers
            "main", // chain
            0.9999, // verificationProgress
            false // initialBlockDownload
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
                "00000000000000000001234567890abcdef", // hash
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

            BlockInfo blockInfo = rpcServices.getBlockInfo("00000000000000000001234567890abcdef").await().indefinitely();

            assertNotNull(blockInfo);
            assertEquals(1733443200, blockInfo.time());
            assertEquals(2500, blockInfo.ntx());
    }

    @Test
    void testRpcError_throwsException() throws Exception {
        // Mock RpcClient to return an error JSON string
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createErrorRpcResponseJson("RPC Error: Verifying blocks..."));

        RpcException exception = assertThrows(RpcException.class,
            () -> rpcServices.getBlockchainInfo().await().indefinitely());
        
        // The error message now includes the method name from RpcServices
        assertTrue(exception.getMessage().contains("RPC Error for method getblockchaininfo: RPC Error: Verifying blocks..."));
    }

    @Test
    void testRpcConnectionFailure_throwsException() {
        // This test case still throws a RuntimeException directly from the mock,
        // which RpcServices will catch and wrap in an RpcException.
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenThrow(new RuntimeException("Connection refused"));

        RpcException exception = assertThrows(RpcException.class,
            () -> rpcServices.getNodeInfo().await().indefinitely());
        
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Connection refused"));
    }
}
