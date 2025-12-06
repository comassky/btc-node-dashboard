package comasky;

import comasky.api.CachedMessage;
import comasky.rpcClass.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CachedMessageTest {

    @Test
    void testSuccessMessage_createsValidMessage() {
        GlobalResponse data = createMockGlobalResponse();
        String json = "{\"data\":\"test\"}";
        
        CachedMessage message = CachedMessage.success(data, json);
        
        assertNotNull(message);
        assertFalse(message.isError());
        assertEquals(data, message.data());
        assertNull(message.errorMessage());
        assertEquals(json, message.serializedJson());
        assertTrue(message.timestamp() > 0);
    }

    @Test
    void testErrorMessage_createsValidMessage() {
        String errorMsg = "Connection failed";
        String errorJson = "{\"rpcConnected\": false}";
        
        CachedMessage message = CachedMessage.error(errorMsg, errorJson);
        
        assertNotNull(message);
        assertTrue(message.isError());
        assertNull(message.data());
        assertEquals(errorMsg, message.errorMessage());
        assertEquals(errorJson, message.serializedJson());
        assertTrue(message.timestamp() > 0);
    }

    @Test
    void testIsValid_recentMessage() {
        GlobalResponse data = createMockGlobalResponse();
        CachedMessage message = CachedMessage.success(data, "{}");
        
        assertTrue(message.isValid(5000));
    }

    @Test
    void testIsValid_expiredMessage() throws InterruptedException {
        GlobalResponse data = createMockGlobalResponse();
        CachedMessage message = CachedMessage.success(data, "{}");
        
        Thread.sleep(100);
        
        assertFalse(message.isValid(50));
    }

    @Test
    void testIsValid_zeroValidity() {
        GlobalResponse data = createMockGlobalResponse();
        CachedMessage message = CachedMessage.success(data, "{}");
        
        assertFalse(message.isValid(0));
    }

    @Test
    void testIsValid_negativeValidity() {
        GlobalResponse data = createMockGlobalResponse();
        CachedMessage message = CachedMessage.success(data, "{}");
        
        assertFalse(message.isValid(-100));
    }

    @Test
    void testTimestamp_isAccurate() {
        long beforeCreate = System.currentTimeMillis();
        GlobalResponse data = createMockGlobalResponse();
        CachedMessage message = CachedMessage.success(data, "{}");
        long afterCreate = System.currentTimeMillis();
        
        assertTrue(message.timestamp() >= beforeCreate);
        assertTrue(message.timestamp() <= afterCreate);
    }

    private GlobalResponse createMockGlobalResponse() {
        NodeInfo nodeInfo = new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true);
        BlockchainInfo blockchainInfo = new BlockchainInfo();
        blockchainInfo.setBlocks(870000);
        blockchainInfo.setChain("main");
        
        return GlobalResponse.builder()
                .generalStats(new GeneralStats(2, 8, 10))
                .nodeInfo(nodeInfo)
                .blockchainInfo(blockchainInfo)
                .inboundPeer(Collections.emptyList())
                .outboundPeer(Collections.emptyList())
                .subverDistribution(new SubverDistribution(Collections.emptyList(), Collections.emptyList()))
                .upTime("5d, 03:00:00")
                .build();
    }
}
