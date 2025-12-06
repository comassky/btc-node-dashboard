package comasky;

import comasky.api.DashboardWebSocket;
import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        when(rpcServices.getData()).thenReturn(mockResponse);

        webSocket.onOpen(mockSession);

        verify(mockSession, atLeastOnce()).getId();
    }

    @Test
    void testOnClose_removesSession() {
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("test-session-456");

        webSocket.onClose(mockSession);

        verify(mockSession).getId();
    }

    private GlobalResponse createMockResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfo blockchainInfo = new BlockchainInfo();
        blockchainInfo.setBlocks(870000);
        blockchainInfo.setChain("main");

        NodeInfo nodeInfo = new NodeInfo(70016, 270000, "/Satoshi:27.0.0/", 10, true);

        SubverDistribution distribution = new SubverDistribution(List.of(), List.of());

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setTime(System.currentTimeMillis() / 1000);
        blockInfo.setNtx(2500);

        return GlobalResponse.builder()
                .generalStats(generalStats)
                .blockchainInfo(blockchainInfo)
                .nodeInfo(nodeInfo)
                .upTime("5 days")
                .inboundPeer(List.of())
                .outboundPeer(List.of())
                .subverDistribution(distribution)
                .block(blockInfo)
                .build();
    }
}
