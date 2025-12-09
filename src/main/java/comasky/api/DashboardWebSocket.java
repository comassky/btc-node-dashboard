package comasky.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.RpcServices;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for real-time dashboard updates.
 * Broadcasts Bitcoin node data to all connected clients at configured intervals using a reactive approach.
 * Implements caching to avoid redundant RPC calls when data is fresh.
 */
@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
public class DashboardWebSocket {

    private static final Logger LOG = Logger.getLogger(DashboardWebSocket.class);

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    // Cache for RPC data
    private final Object cacheLock = new Object();
    private volatile CachedMessage cachedMessage;
    private volatile Uni<CachedMessage> inFlightRequest;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "dashboard.polling.interval.seconds", defaultValue = "5")
    int pollingIntervalSeconds;

    @ConfigProperty(name = "dashboard.cache.validity-buffer-ms", defaultValue = "100")
    int cacheValidityBufferMs;

    private long cacheValidityMs;

    @PostConstruct
    void init() {
        // Pre-calculate cache validity to avoid recalculation on each fetch
        long pollingIntervalMs = pollingIntervalSeconds * 1000L;
        this.cacheValidityMs = Math.max(100, pollingIntervalMs - cacheValidityBufferMs);
    }

    @Scheduled(every = "${dashboard.polling.interval.seconds}s", identity = "dashboard-broadcast")
    void scheduledBroadcast() {
        if (sessions.isEmpty()) {
            return;
        }
        fetchAndCacheMessage()
                .subscribe().with(
                        message -> broadcastMessage(message.serializedJson()),
                        failure -> LOG.errorf("Failed to fetch and broadcast message: %s", failure.getMessage())
                );
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOG.debugf("WebSocket opened: %s (total: %d)", session.getId(), sessions.size());
        sendDataToSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        LOG.debugf("WebSocket closed: %s (remaining: %d)", session.getId(), sessions.size());
    }

    private void sendDataToSession(Session session) {
        if (!session.isOpen()) {
            return;
        }

        fetchAndCacheMessage()
                .subscribe().with(
                        message -> session.getAsyncRemote().sendText(message.serializedJson()),
                        failure -> LOG.errorf("Failed to send initial data to session %s: %s", session.getId(), failure.getMessage())
                );
    }

    private Uni<CachedMessage> fetchAndCacheMessage() {
        CachedMessage currentCache;
        synchronized (cacheLock) {
            currentCache = this.cachedMessage;
        }

        if (currentCache != null && currentCache.isValid(cacheValidityMs)) {
            return Uni.createFrom().item(currentCache);
        }

        // Cache is stale or absent, get a fresh message, ensuring only one fetch happens at a time.
        return getFreshMessage();
    }

    private synchronized Uni<CachedMessage> getFreshMessage() {
        // Check if a request is already in flight
        if (inFlightRequest == null) {
            // No request in flight, create a new one.
            inFlightRequest = rpcServices.getData()
                    .onItem().transform(data -> {
                        try {
                            String json = objectMapper.writeValueAsString(data);
                            return CachedMessage.success(data, json);
                        } catch (Exception e) {
                            // This will be caught by onFailure and transformed into an error message
                            throw new RuntimeException(e);
                        }
                    })
                    .onFailure().recoverWithItem(e -> {
                        LOG.warnf("RPC call failed: %s", e.getMessage());
                        String errorMsg = e.getCause() != null ? e.getCause().getMessage().replace("\"", "'") : e.getMessage().replace("\"", "'");
                        String errorJson = "{\"rpcConnected\": false, \"errorMessage\": \"" + errorMsg + "\"}";
                        return CachedMessage.error(errorMsg, errorJson);
                    })
                    .onItem().invoke(message -> {
                        // Cache the new result
                        synchronized (cacheLock) {
                            cachedMessage = message;
                        }
                    })
                    // When the Uni completes (on item or failure), clear the in-flight request.
                    .onTermination().invoke(() -> {
                        synchronized (this) {
                            inFlightRequest = null;
                        }
                    })
                    // Cache the Uni's result so that concurrent subscribers get the same result without re-triggering the RPC call.
                    .memoize().indefinitely();
        }
        return inFlightRequest;
    }

    private void broadcastMessage(String message) {
        sessions.removeIf(session -> {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(message);
                    return false;
                } catch (Exception e) {
                    LOG.warnf("Failed to send message to session %s: %s", session.getId(), e.getMessage());
                    return true; // Remove session if sending fails
                }
            }
            return true; // Remove closed sessions
        });
    }
}