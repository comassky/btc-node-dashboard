package comasky.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WebSocket endpoint for real-time dashboard updates.
 * <p>
 * Broadcasts Bitcoin node data to all connected clients at configured intervals using a reactive approach.
 * Implements caching to avoid redundant RPC calls when data is fresh.
 * Handles connection, disconnection, and error reporting for WebSocket clients.
 */
@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
public class DashboardWebSocket {

    private static final Logger LOG = Logger.getLogger(DashboardWebSocket.class);

    private final Set<Session> sessions = ConcurrentHashMap.<Session>newKeySet();

    // Cache for RPC data
    private final AtomicReference<CachedMessage> cachedMessage = new AtomicReference<>();
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

    /**
     * Initializes the cache validity duration after construction.
     */
    @PostConstruct
    void init() {
        // Pre-calculate cache validity to avoid recalculation on each fetch
        long pollingIntervalMs = pollingIntervalSeconds * 1000L;
        this.cacheValidityMs = Math.max(100, pollingIntervalMs - cacheValidityBufferMs);
    }

    /**
     * Periodically broadcasts the latest dashboard data to all connected WebSocket clients.
     */
    @Scheduled(every = "${dashboard.polling.interval.seconds}s", identity = "dashboard-broadcast")
    void scheduledBroadcast() {
        if (sessions.isEmpty()) {
            return;
        }
        fetchAndCacheMessage()
                .subscribe().with(
                        message -> {
                            String json = message.serializedJson();
                            if (json != null) {
                                broadcastMessage(json); // diffuse la même chaîne à tous
                            } else {
                                LOG.error("No JSON to broadcast");
                            }
                        },
                        failure -> LOG.errorf("Failed to fetch and broadcast message: %s", failure.getMessage())
                );
    }

    /**
     * Handles a new WebSocket connection.
     *
     * @param session the new WebSocket session
     */
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOG.debugf("WebSocket opened: %s (total: %d)", session.getId(), sessions.size());
        sendDataToSession(session);
    }

    /**
     * Handles the closure of a WebSocket connection.
     *
     * @param session the closed WebSocket session
     */
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        LOG.debugf("WebSocket closed: %s (remaining: %d)", session.getId(), sessions.size());
    }

    /**
     * Sends the latest dashboard data to a specific WebSocket session.
     *
     * @param session the WebSocket session to send data to
     */
    private void sendDataToSession(Session session) {
        if (session == null || !session.isOpen()) {
            LOG.warn("Attempted to send data to a null or closed session.");
            return;
        }

        fetchAndCacheMessage()
                .subscribe().with(
                        message -> safeSend(session, message),
                        failure -> handleSendError(session, failure)
                );
    }

    /**
     * Safely sends a message to a WebSocket session, handling errors and nulls.
     *
     * @param session the WebSocket session
     * @param message the cached message to send
     */
    private void safeSend(Session session, CachedMessage message) {
        try {
            if (message == null || message.serializedJson() == null) {
                LOG.errorf("Fetched message or its JSON is null for session %s", session.getId());
                sendErrorJson(session, new ErrorResponse("No data available"));
            } else {
                session.getAsyncRemote().sendText(message.serializedJson());
            }
        } catch (Exception e) {
            LOG.errorf(e, "Exception while sending data to session %s", session.getId());
            sendErrorJson(session, new ErrorResponse("Internal error while sending data"));
        }
    }

    /**
     * Handles errors that occur when sending data to a WebSocket session.
     *
     * @param session the WebSocket session
     * @param failure the error that occurred
     */
    private void handleSendError(Session session, Throwable failure) {
        LOG.errorf(failure, "Failed to send initial data to session %s: %s", session.getId(), failure.getMessage());
        String msg = (failure.getMessage() != null) ? failure.getMessage() : "unknown error";
        sendErrorJson(session, new ErrorResponse(String.format("Failed to fetch data: %s", msg)));
    }

    /**
     * Sends an error message as JSON to a WebSocket session.
     *
     * @param session the WebSocket session
     * @param errorResponse the error response to send
     */
    private void sendErrorJson(Session session, ErrorResponse errorResponse) {
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(objectMapper.writeValueAsString(errorResponse));
            } catch (Exception e) {
                LOG.errorf(e, "Exception while sending error message to session %s", session.getId());
            }
        }
    }

    /**
     * Fetches the latest dashboard data, using the cache if valid.
     *
     * @return a {@link Uni} emitting the cached or fresh message
     */
    private Uni<CachedMessage> fetchAndCacheMessage() {
        CachedMessage currentCache = cachedMessage.get();
        if (currentCache != null && currentCache.isValid(cacheValidityMs)) {
            return Uni.createFrom().item(currentCache);
        }
        return getFreshMessage();
    }

    /**
     * Fetches a fresh dashboard message from the backend and updates the cache.
     *
     * @return a {@link Uni} emitting the fresh message
     */
    private Uni<CachedMessage> getFreshMessage() {
        synchronized (this) {
            if (inFlightRequest != null) {
                return inFlightRequest;
            }
            inFlightRequest = rpcServices.getData()
                .onItem().transform(data -> {
                    try {
                        String json = objectMapper.writeValueAsString(data);
                        return CachedMessage.success(data, json);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .onFailure().recoverWithItem(e -> {
                    LOG.warnf("RPC call failed: %s", e.getMessage());
                    String msg = (e.getCause() != null && e.getCause().getMessage() != null) ? e.getCause().getMessage() : e.getMessage();
                    if (msg == null) msg = "Unknown error";
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("rpcConnected", false);
                    errorMap.put("errorMessage", msg);
                    String errorJson;
                    try {
                        errorJson = objectMapper.writeValueAsString(errorMap);
                    } catch (Exception jsonEx) {
                        LOG.errorf(jsonEx, "Failed to serialize error map to JSON for WebSocket client");
                        errorJson = "{\"rpcConnected\": false, \"errorMessage\": \"Internal server error during error serialization\"}";
                    }
                    return CachedMessage.error(msg, errorJson);
                })
                .onItem().invoke(message -> {
                    cachedMessage.set(message);
                })
                .onTermination().invoke(() -> {
                    synchronized (this) {
                        inFlightRequest = null;
                    }
                })
                .memoize().indefinitely();
            return inFlightRequest;
        }
    }

    /**
     * Broadcasts a message to all connected WebSocket sessions.
     *
     * @param message the message to broadcast
     */
    private void broadcastMessage(String message) {
        sessions.removeIf(session -> {
            if (session == null || !session.isOpen()) {
                return true;
            }
            try {
                session.getAsyncRemote().sendText(message);
                return false;
            } catch (Exception e) {
                LOG.warnf(e, "Failed to send message to session %s", session.getId());
                return true;
            }
        });
    }
}
