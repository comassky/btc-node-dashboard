package comasky;

import comasky.api.CachedMessage;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import comasky.rpcClass.responses.MempoolInfoResponse;
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
        comasky.rpcClass.responses.NetworkInfoResponse nodeInfo = new comasky.rpcClass.responses.NetworkInfoResponse(
            70016, // version
            "/Satoshi:27.0.0/", // subversion
            70016, // protocolversion
            "", // localservices
            Collections.emptyList(), // localservicesnames
            true, // localrelay
            0, // timeoffset
            0, // connections
            true, // networkactive
            Collections.emptyList(), // networks
            Collections.emptyList() // localaddresses
        );
        BlockchainInfoResponse blockchainInfoResponse = new BlockchainInfoResponse(
            "main", 870000, 870000, "", 0.99, 0L, 0L, 0.99, false, "", 0L, false, null
        );
        BlockInfoResponse blockInfoResponse = new BlockInfoResponse(null, 0, 0, 0, 0, 0, 0, null, null, 0, 0, 0, null, 0, null, 0, null, null);
        return new GlobalResponse(
            new GeneralStats(2, 8, 10),
            new SubverDistribution(Collections.emptyList(), Collections.emptyList()),
            Collections.emptyList(),
            Collections.emptyList(),
            blockchainInfoResponse,
            nodeInfo,
            "5d, 03:00:00",
            blockInfoResponse,
            new MempoolInfoResponse(
                true, 0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0
            ),
            Collections.emptyMap()
        );
    }
}
