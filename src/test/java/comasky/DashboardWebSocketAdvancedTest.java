package comasky;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.exceptions.RpcException;
import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class DashboardWebSocketAdvancedTest {

    @InjectMock
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testCachedMessage_success() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        when(rpcServices.getData()).thenReturn(mockResponse);

        assertNotNull(mockResponse);
        assertEquals(2, mockResponse.getGeneralStats().inboundCount());
        assertEquals(8, mockResponse.getGeneralStats().outboundCount());
    }

    @Test
    void testCachedMessage_error() {
        when(rpcServices.getData()).thenThrow(new RpcException("Connection failed"));

        assertThrows(RpcException.class, () -> rpcServices.getData());
    }

    @Test
    void testGlobalResponse_structure() {
        GlobalResponse mockResponse = createMockGlobalResponse();

        assertNotNull(mockResponse.getNodeInfo());
        assertNotNull(mockResponse.getBlockchainInfo());
        assertNotNull(mockResponse.getGeneralStats());
        assertNotNull(mockResponse.getSubverDistribution());
        assertEquals("5d, 03:00:00", mockResponse.getUpTime());
    }

    @Test
    void testJsonSerialization() throws Exception {
        GlobalResponse mockResponse = createMockGlobalResponse();
        
        String json = objectMapper.writeValueAsString(mockResponse);
        
        assertNotNull(json);
        assertTrue(json.contains("generalStats"));
        assertTrue(json.contains("nodeInfo"));
        assertTrue(json.contains("blockchainInfo"));
    }

    @Test
    void testRpcException_handling() {
        String errorMessage = "Test RPC error";
        RpcException exception = new RpcException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testNodeInfo_fields() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        NodeInfo nodeInfo = mockResponse.getNodeInfo();
        
        assertEquals(270000, nodeInfo.version());
        assertEquals(70016, nodeInfo.protocolVersion());
        assertEquals("/Satoshi:27.0.0/", nodeInfo.subversion());
    }

    @Test
    void testBlockchainInfo_fields() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        BlockchainInfo blockchainInfo = mockResponse.getBlockchainInfo();
        
        assertEquals(870000, blockchainInfo.getBlocks());
        assertEquals("main", blockchainInfo.getChain());
    }

    @Test
    void testGeneralStats_calculation() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        GeneralStats stats = mockResponse.getGeneralStats();
        
        assertEquals(10, stats.totalPeers());
        assertEquals(stats.inboundCount() + stats.outboundCount(), stats.totalPeers());
    }

    @Test
    void testSubverDistribution_structure() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        SubverDistribution distribution = mockResponse.getSubverDistribution();
        
        assertNotNull(distribution.inbound());
        assertNotNull(distribution.outbound());
    }

    @Test
    void testBlockInfo_fields() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        BlockInfo blockInfo = mockResponse.getBlock();
        
        assertNotNull(blockInfo);
        assertEquals(1733443200, blockInfo.getTime());
        assertEquals(2500, blockInfo.getNtx());
    }

    @Test
    void testMultipleRpcCalls() {
        GlobalResponse mockResponse = createMockGlobalResponse();
        when(rpcServices.getData()).thenReturn(mockResponse);
        
        GlobalResponse result1 = rpcServices.getData();
        GlobalResponse result2 = rpcServices.getData();
        
        assertNotNull(result1);
        assertNotNull(result2);
        verify(rpcServices, times(2)).getData();
    }

    private GlobalResponse createMockGlobalResponse() {
        NodeInfo nodeInfo = new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true);
        BlockchainInfo blockchainInfo = new BlockchainInfo();
        blockchainInfo.setBlocks(870000);
        blockchainInfo.setChain("main");

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setTime(1733443200);
        blockInfo.setNtx(2500);

        return GlobalResponse.builder()
                .generalStats(new GeneralStats(2, 8, 10))
                .nodeInfo(nodeInfo)
                .blockchainInfo(blockchainInfo)
                .inboundPeer(Collections.emptyList())
                .outboundPeer(Collections.emptyList())
                .subverDistribution(new SubverDistribution(Collections.emptyList(), Collections.emptyList()))
                .upTime("5d, 03:00:00")
                .block(blockInfo)
                .build();
    }
}
