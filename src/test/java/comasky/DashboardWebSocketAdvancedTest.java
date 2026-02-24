package comasky;

import comasky.client.RpcClient;
import comasky.client.RpcRequestDto;
import comasky.service.CacheProvider;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@QuarkusTest
class DashboardWebSocketAdvancedTest {

    @InjectMock
    RpcClient rpcClient;

    @Inject
    CacheProvider cacheProvider;

    @TestHTTPResource("/ws/dashboard")
    URI uri;

    private static CountDownLatch latch;

    /**
     * A custom Mockito ArgumentMatcher to safely match RPC requests by method name.
     * This avoids NullPointerExceptions that can occur with lambda matchers.
     */
    private static class RpcMethodMatcher implements ArgumentMatcher<RpcRequestDto> {
        private final String methodName;

        private RpcMethodMatcher(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public boolean matches(RpcRequestDto request) {
            // Guard against nulls that Mockito might pass internally during setup
            return request != null && methodName.equals(request.method());
        }

        @Override
        public String toString() {
            return "A RpcRequestDto with method: " + methodName;
        }
    }

    @BeforeEach
    public void setup() {
        reset(rpcClient);
        cacheProvider.invalidateAll();
        setupRpcClientMocks();
    }

    private void setupRpcClientMocks() {
        try {
            when(rpcClient.executeRpcCall(argThat(new RpcMethodMatcher("getblockchaininfo"))))
                .thenReturn("{\"result\": {\"chain\": \"main\", \"blocks\": 123}, \"error\": null, \"id\": \"1\"}");

            when(rpcClient.executeRpcCall(argThat(new RpcMethodMatcher("getnetworkinfo"))))
                .thenReturn("{\"result\": {\"version\": 70016, \"subversion\": \"/Satoshi:27.0.0/\"}, \"error\": null, \"id\": \"1\"}");

            when(rpcClient.executeRpcCall(argThat(new RpcMethodMatcher("getpeerinfo"))))
                .thenReturn("{\"result\": [], \"error\": null, \"id\": \"1\"}");

            when(rpcClient.executeRpcCall(argThat(new RpcMethodMatcher("uptime"))))
                .thenReturn("{\"result\": 1000, \"error\": null, \"id\": \"1\"}");

            when(rpcClient.executeRpcCall(argThat(new RpcMethodMatcher("getbestblockhash"))))
                .thenReturn("{\"result\": \"some_hash\", \"error\": null, \"id\": \"1\"}");

            when(rpcClient.executeRpcCall(argThat(new RpcMethodMatcher("getblock"))))
                .thenReturn("{\"result\": {\"time\": 12345, \"nTx\": 10}, \"error\": null, \"id\": \"1\"}");

            when(rpcClient.executeRpcCall(argThat(new RpcMethodMatcher("getmempoolinfo"))))
                .thenReturn("{\"result\": {\"size\": 1}, \"error\": null, \"id\": \"1\"}");

        } catch (Exception e) {
            throw new RuntimeException("Failed to setup mocks", e);
        }
    }

    @Test
    void testConcurrentConnections_triggersSingleRpcCallDueToCache() throws Exception {
        int concurrentClients = 5;
        latch = new CountDownLatch(concurrentClients);

        ExecutorService executor = Executors.newFixedThreadPool(concurrentClients);
        List<Session> sessions = new ArrayList<>();

        try {
            for (int i = 0; i < concurrentClients; i++) {
                executor.submit(() -> {
                    try {
                        sessions.add(ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            boolean allMessagesReceived = latch.await(10, TimeUnit.SECONDS);
            assertTrue(allMessagesReceived, "All clients should have received a message");

            // Verify that each RPC method was called only ONCE, proving the cache works.
            verify(rpcClient, times(1)).executeRpcCall(argThat(new RpcMethodMatcher("getblockchaininfo")));
            verify(rpcClient, times(1)).executeRpcCall(argThat(new RpcMethodMatcher("getnetworkinfo")));
            verify(rpcClient, times(1)).executeRpcCall(argThat(new RpcMethodMatcher("getpeerinfo")));
            verify(rpcClient, times(1)).executeRpcCall(argThat(new RpcMethodMatcher("uptime")));
            verify(rpcClient, times(1)).executeRpcCall(argThat(new RpcMethodMatcher("getbestblockhash")));
            verify(rpcClient, times(1)).executeRpcCall(argThat(new RpcMethodMatcher("getblock")));
            verify(rpcClient, atMost(1)).executeRpcCall(argThat(new RpcMethodMatcher("getmempoolinfo")));

        } finally {
            sessions.forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.close();
                    } catch (Exception e) { /* ignore */ }
                }
            });
            executor.shutdownNow();
        }
    }

    @ClientEndpoint
    public static class Client {
        @OnMessage
        void message(String msg) {
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
