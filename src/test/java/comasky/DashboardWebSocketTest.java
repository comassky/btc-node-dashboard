package comasky;

import comasky.rpcClass.RpcServices;
import comasky.rpcClass.dto.GeneralStats;
import comasky.rpcClass.dto.GlobalResponse;
import comasky.rpcClass.dto.SubverDistribution;
import comasky.rpcClass.view.BlockInfoView;
import comasky.rpcClass.view.BlockchainInfoView;
import comasky.rpcClass.view.MempoolInfoView;
import comasky.rpcClass.view.NetworkInfoView;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class DashboardWebSocketTest {

    private static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();

    @TestHTTPResource("/ws/dashboard")
    URI uri;

    @InjectMock
    RpcServices rpcServices;

    @BeforeEach
    public void setup() {
        MESSAGES.clear();
    }

    @Test
    void testOnOpen_sendsDataOnConnect() throws Exception {
        // Mock the backend service to return a predictable response
        GlobalResponse mockResponse = createMockResponse();
        when(rpcServices.getData()).thenReturn(Uni.createFrom().item(mockResponse));

        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            // Wait for a message to arrive, with a timeout
            String message = MESSAGES.poll(5, TimeUnit.SECONDS);

            // Assert that we received a message and it contains some expected data
            assertNotNull(message, "Should have received a message on connect");
            assertTrue(message.contains("\"chain\":\"main\""), "Message should contain blockchain info");
            assertTrue(message.contains("\"totalPeers\":10"), "Message should contain general stats");
        }
    }

    // A simple client endpoint for the test
    @ClientEndpoint
    public static class Client {
        @OnOpen
        public void open(Session session) {
            // Connection opened
        }

        @OnMessage
        void message(String msg) {
            MESSAGES.add(msg);
        }
    }

    private GlobalResponse createMockResponse() {
        GeneralStats generalStats = new GeneralStats(2, 8, 10);

        BlockchainInfoView blockchainInfoView = new BlockchainInfoView(
            "main", 870000, 870000, 0.9999, 1700000000L, 1700000000L, 0.9999, false, "chainwork", 1000000000L
        );

        NetworkInfoView nodeInfoView = new NetworkInfoView(70016, "/Satoshi:27.0.0/", 270000, Collections.emptyList(), Collections.emptyList());
        SubverDistribution distribution = new SubverDistribution(Collections.emptyList(), Collections.emptyList());
        BlockInfoView blockInfoView = new BlockInfoView(System.currentTimeMillis() / 1000, 2500);
        MempoolInfoView mempoolInfoView = new MempoolInfoView(5000, 1000000L, 2000000L, 300000000L, 0.00001, 0.00001, 0, 0.5);

        return new GlobalResponse(
            generalStats, distribution, Collections.emptyList(), Collections.emptyList(),
            blockchainInfoView, nodeInfoView, "5 days", blockInfoView, mempoolInfoView, Collections.emptyMap()
        );
    }
}
