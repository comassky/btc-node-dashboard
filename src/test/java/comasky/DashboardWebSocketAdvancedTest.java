package comasky;

import comasky.api.DashboardWebSocket;
import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.responses.BlockInfoResponse;
import comasky.rpcClass.responses.BlockchainInfoResponse;
import comasky.rpcClass.responses.MempoolInfoResponse;
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
            new BlockchainInfoResponse(
                "", // chain
                0,  // blocks
                0,  // headers
                "", // bestblockhash
                0.0, // difficulty
                0L, // time
                0L, // mediantime
                0.0, // verificationprogress
                false, // initialblockdownload
                "", // chainwork
                0L, // size_on_disk
                false, // pruned
                null // pruneheight
            ),
            new comasky.rpcClass.responses.NetworkInfoResponse(
                0, // version
                "", // subversion
                0, // protocolversion
                "", // localservices
                Collections.emptyList(), // localservicesnames
                false, // localrelay
                0, // timeoffset
                0, // connections
                false, // networkactive
                Collections.emptyList(), // networks
                Collections.emptyList() // localaddresses
            ),
            "0s",
            new BlockInfoResponse(null, 0, 0, 0, 0, 0, 0, null, null, 0, 0, 0, null, 0, null, 0, null, null),
            new MempoolInfoResponse(
                true, 0, 0L, 0L, 0L, 0.0, 0.0, 0, 0.0
            ),
            Collections.emptyMap()
        );
    }
}
