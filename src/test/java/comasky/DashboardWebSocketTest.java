package comasky;

import comasky.api.DashboardWebSocket;
import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

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

        // Verify that the session was added and data was sent asynchronously
        verify(mockSession, atLeastOnce()).getId();
        verify(mockAsync, timeout(1000).times(1)).sendText(anyString());
    }

    @Test
    void testOnClose_removesSession() {
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("test-session-456");

        // Add and then remove the session to test the close logic
        webSocket.onOpen(mockSession);
        webSocket.onClose(mockSession);

        verify(mockSession, atLeastOnce()).getId();
    }

    private GlobalResponse createMockResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfo blockchainInfo = new BlockchainInfo(870000, 870000, "main", 0.9999, false);

        NodeInfo nodeInfo = new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true);

        SubverDistribution distribution = new SubverDistribution(Collections.emptyList(), Collections.emptyList());

        BlockInfo blockInfo = new BlockInfo(null, 0, 0, 0, 0, 0, 0, null, null, System.currentTimeMillis() / 1000, 0, 0, null, 0, null, 2500, null, null);

        return new GlobalResponse(
                generalStats,
                distribution,
                Collections.emptyList(),
                Collections.emptyList(),
                blockchainInfo,
                nodeInfo,
                "5 days",
                blockInfo
        );
    }
}