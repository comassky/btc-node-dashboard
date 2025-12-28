package comasky.api;

import comasky.rpcClass.DashboardDataProvider;
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

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for real-time dashboard updates.
 * <p>
 * Broadcasts Bitcoin node data to all connected clients at configured intervals using a reactive approach.
 * It leverages Quarkus's built-in caching and a custom JSON encoder for serialization.
 */
@ServerEndpoint(value = "/ws/dashboard", encoders = {JsonEncoder.class})
@ApplicationScoped
public class DashboardWebSocket {

    private static final Logger LOG = Logger.getLogger(DashboardWebSocket.class);

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @Inject
    DashboardDataProvider dataProvider;

    /**
     * Creates a Uni that emits a unified Object payload.
     * On success, it emits the GlobalResponse.
     * On failure, it emits a Map representing the error.
     * This robustly handles type differences between success and failure paths.
     *
     * @return A Uni<Object> ready to be sent via WebSocket.
     */
    private Uni<Object> getUnifiedDashboardData() {
        return Uni.createFrom().emitter(emitter ->
                dataProvider.getData().subscribe().with(
                        emitter::complete,
                        failure -> emitter.complete(createErrorPayload(failure))
                )
        );
    }

    /**
     * Periodically fetches and broadcasts the latest dashboard data to all connected WebSocket clients.
     */
    @Scheduled(every = "${dashboard.polling.interval.seconds}s", identity = "dashboard-broadcast")
    void scheduledBroadcast() {
        if (sessions.isEmpty()) {
            return;
        }
        getUnifiedDashboardData()
                .subscribe().with(
                        this::broadcastMessage,
                        failure -> LOG.error("Failed to subscribe for broadcast.", failure)
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

    /**
     * Sends the latest dashboard data to a specific WebSocket session.
     */
    private void sendDataToSession(Session session) {
        if (session == null || !session.isOpen()) {
            LOG.warn("Attempted to send data to a null or closed session.");
            return;
        }

        getUnifiedDashboardData()
                .onItem().transformToUni(data -> sendMessage(session, data))
                .subscribe().with(
                        _ -> LOG.debugf("Initial data sent to session %s", session.getId()),
                        failure -> LOG.errorf(failure, "Failed to send initial data to session %s", session.getId())
                );
    }

    /**
     * Broadcasts a message object to all connected and open WebSocket sessions concurrently.
     */
    private void broadcastMessage(Object message) {
        if (message == null) return;
        LOG.debugf("Broadcasting to %d sessions", sessions.size());
        Multi.createFrom().iterable(sessions)
                .filter(Session::isOpen)
                .onItem().transformToUniAndMerge(session ->
                        sendMessage(session, message)
                                .onFailure().recoverWithNull()
                )
                .collect().asList()
                .subscribe().with(
                        _ -> LOG.debug("Broadcast complete."),
                        failure -> LOG.error("An unexpected error occurred during broadcast.", failure)
                );
    }

    /**
     * Sends a message object to a single WebSocket session using a callback-based approach
     * wrapped in a Mutiny Uni. This is the robust way to bridge the imperative WebSocket API
     * with the reactive world, avoiding threading issues.
     *
     * @param session the session to send the message to
     * @param message the object to send
     * @return a Uni<Void> that completes on success or fails
     */
    private Uni<Void> sendMessage(Session session, Object message) {
        // Create the Uni and explicitly type it to Uni<Void> to resolve compiler inference issues.
        Uni<Void> sendUni = Uni.createFrom().emitter(emitter -> {
            session.getAsyncRemote().sendObject(message, result -> {
                if (result.isOK()) {
                    emitter.complete(null);
                } else {
                    emitter.fail(result.getException());
                }
            });
        });

        // Apply the side-effect on failure to the correctly typed Uni.
        return sendUni.onFailure().invoke(failure -> {
            if (failure instanceof IOException) {
                LOG.debugf("Client %s disconnected, removing session.", session.getId());
            } else {
                LOG.warnf(failure, "Failed to send message to session %s, removing.", session.getId());
            }
            sessions.remove(session);
        });
    }

    /**
     * Creates a standard error payload map when data fetching fails.
     */
    private Map<String, Object> createErrorPayload(Throwable throwable) {
        LOG.error("An error occurred while fetching data for WebSocket", throwable);
        String errorMessage = throwable.getMessage() != null ? throwable.getMessage() : "An unknown error occurred.";
        return Map.of(
                "rpcConnected", false,
                "errorMessage", "Failed to fetch data: " + errorMessage
        );
    }
}
