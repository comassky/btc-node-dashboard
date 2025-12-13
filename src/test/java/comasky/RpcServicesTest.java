package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.exceptions.RpcException;
import comasky.rpcClass.*;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
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
        comasky.rpcClass.responses.NetworkInfoResponse expectedNodeInfo = new comasky.rpcClass.responses.NetworkInfoResponse(
            70016, // version
            "/Satoshi:27.0.0/", // subversion
            70016, // protocolversion
            "", // localservices
            java.util.Collections.emptyList(), // localservicesnames
            true, // localrelay
            0, // timeoffset
            0, // connections
            true, // networkactive
            java.util.Collections.emptyList(), // networks
            java.util.Collections.emptyList() // localaddresses
        );
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedNodeInfo));

        comasky.rpcClass.responses.NetworkInfoResponse nodeInfo = rpcServices.getNetworkInfo().await().indefinitely();

        assertNotNull(nodeInfo);
        assertEquals(70016, nodeInfo.version());
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
        BlockchainInfoResponse expectedBlockchainInfoResponse = new BlockchainInfoResponse(
            "main", 870000, 870000, "", 0.9999, 0L, 0L, 0.9999, false, "", 0L, false, null
        );
        when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedBlockchainInfoResponse));

        BlockchainInfoResponse blockchainInfoResponse = rpcServices.getBlockchainInfo().await().indefinitely();

        assertNotNull(blockchainInfoResponse);
        assertEquals(870000, blockchainInfoResponse.blocks());
        assertEquals(870000, blockchainInfoResponse.headers());
        assertEquals("main", blockchainInfoResponse.chain());
        assertEquals(0.9999, blockchainInfoResponse.verificationprogress(), 0.0001);
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
            BlockInfoResponse expectedBlockInfoResponse = new BlockInfoResponse(
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
            when(rpcClient.executeRpcCall(any(RpcRequestDto.class))).thenReturn(createSuccessRpcResponseJson(expectedBlockInfoResponse));

            BlockInfoResponse blockInfoResponse = rpcServices.getBlockInfo("00000000000000000001234567890abcdef").await().indefinitely();

            assertNotNull(blockInfoResponse);
            assertEquals(1733443200, blockInfoResponse.time());
            assertEquals(2500, blockInfoResponse.ntx());
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
            () -> rpcServices.getNetworkInfo().await().indefinitely());
        
        assertTrue(exception.getMessage().contains("Connection failed"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Connection refused"));
    }
}
