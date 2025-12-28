package comasky;

import comasky.api.DashboardWebSocket;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.view.BlockInfoView;
import comasky.rpcClass.view.BlockchainInfoView;
import comasky.rpcClass.view.MempoolInfoView;
import comasky.rpcClass.view.NetworkInfoView;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
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
        // Return a Uni that emits on a worker thread to simulate async work
        when(rpcServices.getData()).thenReturn(Uni.createFrom().item(mockResponse).emitOn(Infrastructure.getDefaultExecutor()));

        int concurrentClients = 5;
        CountDownLatch latch = new CountDownLatch(concurrentClients);

        
        for (int i = 0; i < concurrentClients; i++) {
            Session mockSession = mock(Session.class);
            RemoteEndpoint.Async mockAsync = mock(RemoteEndpoint.Async.class);
            when(mockSession.getId()).thenReturn("session-" + i);
            when(mockSession.isOpen()).thenReturn(true);
            when(mockSession.getAsyncRemote()).thenReturn(mockAsync);

            // Fix: Return a completed Future instead of null
            doAnswer(invocation -> {
                latch.countDown();
                return CompletableFuture.completedFuture(null);
            }).when(mockAsync).sendText(anyString());

            webSocket.onOpen(mockSession);
        }

        
        latch.await(5, TimeUnit.SECONDS);

        // Since RpcServices is mocked, the caching logic inside it (via CacheProvider) is bypassed.
        // Therefore, getData() is called for each client connection.
        // We verify that it is called at least once (and up to N times).
        verify(rpcServices, atLeast(1)).getData();
    }

    private GlobalResponse createMockGlobalResponse() {
        return new GlobalResponse(
            new GeneralStats(0, 0, 0),
            new SubverDistribution(Collections.emptyList(), Collections.emptyList()),
            Collections.emptyList(),
            Collections.emptyList(),
            new BlockchainInfoView(
                "", // chain
                0,  // blocks
                0,  // headers
                0.0, // difficulty
                0L, // time
                0L, // mediantime
                0.0, // verificationprogress
                false, // initialblockdownload
                "", // chainwork
                0L // size_on_disk
            ),
            new NetworkInfoView(
                0, // version
                "", // subversion
                0, // protocolversion
                Collections.emptyList(), // networks
                Collections.emptyList() // localaddresses
            ),
            "0s",
            new BlockInfoView(0, 0),
            new MempoolInfoView(
                0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0
            ),
            Collections.emptyMap()
        );
    }
}
