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

        BlockchainInfoView blockchainInfoView = new BlockchainInfoView(
            "main", // chain
            870000,  // blocks
            870000,  // headers
            0.9999,  // difficulty
            1700000000L, // time
            1700000000L, // mediantime
            0.9999,  // verificationprogress
            false,   // initialblockdownload
            "0000000000000000000000000000000000000000000000000000000000000000", // chainwork
            1000000000L // size_on_disk
        );

        NetworkInfoView nodeInfoView = new NetworkInfoView(
            70016, // version
            "/Satoshi:27.0.0/", // subversion
            270000, // protocolversion
            java.util.Collections.emptyList(), // networks
            java.util.Collections.emptyList() // localaddresses
        );

        SubverDistribution distribution = new SubverDistribution(Collections.emptyList(), Collections.emptyList());

        BlockInfoView blockInfoView = new BlockInfoView(
            System.currentTimeMillis() / 1000, // time
            2500 // nTx
        );

        MempoolInfoView mempoolInfoView = new MempoolInfoView(
            5000, // size
            1000000L, // bytes
            2000000L, // usage
            300000000L, // maxmempool
            0.00001, // mempoolminfee
            0.00001, // minrelaytxfee
            0, // unbroadcastcount
            0.5 // totalFee
        );

        return new GlobalResponse(
            generalStats,
            distribution,
            Collections.emptyList(),
            Collections.emptyList(),
            blockchainInfoView,
            nodeInfoView,
            "5 days",
            blockInfoView,
            mempoolInfoView,
            Collections.emptyMap()
        );
    }
}
