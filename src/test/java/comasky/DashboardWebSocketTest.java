package comasky;

import comasky.api.DashboardWebSocket;
import comasky.rpcClass.*;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import comasky.rpcClass.responses.DummyMempoolInfoResponse;
import comasky.rpcClass.responses.NetworkInfoResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class DashboardWebSocketTest {

    @InjectMock
    RpcServices rpcServices;

    @Inject
    DashboardWebSocket webSocket;

    @Test
    void testOnOpen_addsSessionAndSendsData() {
        Session mockSession = mock(Session.class);
        RemoteEndpoint.Async mockAsync = mock(RemoteEndpoint.Async.class);
        when(mockSession.getId()).thenReturn("test-session-123");
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsync);

        GlobalResponse mockResponse = createMockResponse();
        when(rpcServices.getData()).thenReturn(Uni.createFrom().item(mockResponse));

        webSocket.onOpen(mockSession);

        
        verify(mockSession, atLeastOnce()).getId();
        verify(mockAsync, timeout(1000).times(1)).sendText(anyString());
    }

    @Test
    void testOnClose_removesSession() {
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("test-session-456");

        
        webSocket.onOpen(mockSession);
        webSocket.onClose(mockSession);

        verify(mockSession, atLeastOnce()).getId();
    }

    private GlobalResponse createMockResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfoResponse blockchainInfoResponse = new BlockchainInfoResponse(
            "main", // chain
            870000,  // blocks
            870000,  // headers
            "0000000000000000000dummyhash", // bestblockhash
            0.9999,  // difficulty
            1700000000L, // time
            1700000000L, // mediantime
            0.9999,  // verificationprogress
            false,   // initialblockdownload
            "0000000000000000000000000000000000000000000000000000000000000000", // chainwork
            1000000000L, // size_on_disk
            false,   // pruned
            null     // pruneheight
        );

        NetworkInfoResponse nodeInfo = new NetworkInfoResponse(
            70016, // version
            "/Satoshi:27.0.0/", // subversion
            270000, // protocolversion
            "0000000000000000", // localservices
            java.util.Collections.emptyList(), // localservicesnames
            true, // localrelay
            0, // timeoffset
            10, // connections
            true, // networkactive
            java.util.Collections.emptyList(), // networks
            java.util.Collections.emptyList() // localaddresses
        );

        SubverDistribution distribution = new SubverDistribution(Collections.emptyList(), Collections.emptyList());

        BlockInfoResponse blockInfoResponse = new BlockInfoResponse(null, 0, 0, 0, 0, 0, 0, null, null, System.currentTimeMillis() / 1000, 0, 0, null, 0, null, 2500, null, null);

        return new GlobalResponse(
            generalStats,
            distribution,
            Collections.emptyList(),
            Collections.emptyList(),
            blockchainInfoResponse,
            nodeInfo,
            "5 days",
            blockInfoResponse,
            DummyMempoolInfoResponse.create()
        );
    }
}