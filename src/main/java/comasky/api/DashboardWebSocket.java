package comasky.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.rpcClass.DashboardDataProvider;
import comasky.rpcClass.dto.GlobalResponse;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for real-time dashboard updates.
 * <p>
 * Broadcasts Bitcoin node data to all connected clients at configured intervals using a reactive approach.
 * It leverages Quarkus's built-in caching to avoid redundant RPC calls.
 */
@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
public class DashboardWebSocket {

    private static final Logger LOG = Logger.getLogger(DashboardWebSocket.class);

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @Inject
    DashboardDataProvider dataProvider;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Periodically fetches and broadcasts the latest dashboard data to all connected WebSocket clients.
     * The data fetching is cached via {@link DashboardDataProvider}.
     */
    @Scheduled(every = "${dashboard.polling.interval.seconds}s", identity = "dashboard-broadcast")
    void scheduledBroadcast() {
        if (sessions.isEmpty()) {
            return;
        }

        dataProvider.getData()
                .onItem().transform(this::serializeResponse)
                .onFailure().recoverWithItem(this::serializeError)
                .subscribe().with(
                        this::broadcastMessage,
                        failure -> LOG.error("Failed to serialize and broadcast message.", failure)
                );
    }

    /**
     * Handles a new WebSocket connection by adding it to the session pool and sending the current data.
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
     * Handles the closure of a WebSocket connection by removing it from the session pool.
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

        dataProvider.getData()
                .onItem().transform(this::serializeResponse)
                .onFailure().recoverWithItem(this::serializeError)
                .subscribe().with(
                        json -> sendMessage(session, json),
                        failure -> LOG.errorf(failure, "Failed to send initial data to session %s", session.getId())
                );
    }

    /**
     * Broadcasts a JSON message to all connected and open WebSocket sessions.
     *
     * @param jsonMessage the JSON message to broadcast
     */
    private void broadcastMessage(String jsonMessage) {
        Multi.createFrom().iterable(sessions)
                .filter(Session::isOpen)
                .subscribe().with(session -> sendMessage(session, jsonMessage));
    }

    /**
     * Sends a JSON message to a single WebSocket session.
     *
     * @param session     the session to send the message to
     * @param jsonMessage the JSON message to send
     */
    private void sendMessage(Session session, String jsonMessage) {
        Uni.createFrom().future(session.getAsyncRemote().sendText(jsonMessage))
                .onFailure().invoke(failure -> {
                    LOG.warnf(failure, "Failed to send message to session %s. Removing.", session.getId());
                    sessions.remove(session); // Remove session on send failure
                })
                .subscribe().with(
                        v -> {}, // Success is ignored
                        failure -> {} // Failure is already logged
                );
    }

    /**
     * Serializes a successful GlobalResponse to its JSON string representation.
     *
     * @param response the GlobalResponse object
     * @return a JSON string
     */
    private String serializeResponse(GlobalResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize dashboard data", e);
            // This becomes the new item in the Uni, which will be handled by serializeError downstream.
            throw new RuntimeException("Failed to serialize dashboard data", e);
        }
    }

    /**
     * Serializes an error into a standard JSON error message.
     *
     * @param throwable the error that occurred
     * @return a JSON string representing the error
     */
    private String serializeError(Throwable throwable) {
        LOG.error("An error occurred while fetching data for WebSocket", throwable);
        String errorMessage = throwable.getMessage() != null ? throwable.getMessage() : "An unknown error occurred.";
        Map<String, Object> errorMap = Map.of(
                "rpcConnected", false,
                "errorMessage", "Failed to fetch data: " + errorMessage
        );
        try {
            return objectMapper.writeValueAsString(errorMap);
        } catch (JsonProcessingException e) {
            LOG.fatal("Failed to serialize an error message.", e);
            return "{\"rpcConnected\":false,\"errorMessage\":\"Internal server error: failed to serialize error message.\"}";
        }
    }
}
