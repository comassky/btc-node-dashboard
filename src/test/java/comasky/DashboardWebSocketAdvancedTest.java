package comasky;

import comasky.api.DashboardWebSocket;
import comasky.rpcClass.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class DashboardWebSocketAdvancedTest {

    @InjectMock
    RpcServices rpcServices;

    @Inject
    DashboardWebSocket webSocket;

    @Test
    void testConcurrentAccess_singleRpcCall() throws InterruptedException {
        GlobalResponse mockResponse = createMockGlobalResponse();
        when(rpcServices.getData()).thenReturn(Uni.createFrom().item(mockResponse).emitOn(Infrastructure.getDefaultExecutor()));

        int concurrentClients = 5;
        CountDownLatch latch = new CountDownLatch(concurrentClients);

        
        for (int i = 0; i < concurrentClients; i++) {
            Session mockSession = mock(Session.class);
            RemoteEndpoint.Async mockAsync = mock(RemoteEndpoint.Async.class);
            when(mockSession.getId()).thenReturn("session-" + i);
            when(mockSession.isOpen()).thenReturn(true);
            when(mockSession.getAsyncRemote()).thenReturn(mockAsync);

            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(mockAsync).sendText(anyString());

            webSocket.onOpen(mockSession);
        }

        
        latch.await(5, TimeUnit.SECONDS);

        
        verify(rpcServices, times(1)).getData();
    }

    private GlobalResponse createMockGlobalResponse() {
        return new GlobalResponse(
                new GeneralStats(0, 0, 0),
                new SubverDistribution(Collections.emptyList(), Collections.emptyList()),
                Collections.emptyList(),
                Collections.emptyList(),
                new BlockchainInfo(0, 0, "", 0.0, false),
                new NodeInfo(0, 0, "", 0, false),
                "0s",
                new BlockInfo(null, 0, 0, 0, 0, 0, 0, null, null, 0, 0, 0, null, 0, null, 0, null, null)
        );
    }
}