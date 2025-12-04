package comasky.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.RpcServices;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
public class DashboardWebSocket {

    private static final Logger LOG = Logger.getLogger(DashboardWebSocket.class);

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private ScheduledExecutorService scheduler;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "dashboard.polling.interval.seconds", defaultValue = "5")
    int pollingIntervalSeconds;

    @PostConstruct
    void startScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "websocket-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::sendData, 0, pollingIntervalSeconds, TimeUnit.SECONDS);
        LOG.infof("WebSocket scheduler started with %d seconds interval", pollingIntervalSeconds);
    }

    @PreDestroy
    void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOG.info("WebSocket scheduler stopped");
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOG.infof("WebSocket opened: %s (total: %d)", session.getId(), sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        LOG.infof("WebSocket closed: %s (remaining: %d)", session.getId(), sessions.size());
    }

    private void sendData() {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            GlobalResponse data = rpcServices.getData();
            String jsonMessage = objectMapper.writeValueAsString(data);
            broadcastMessage(jsonMessage);
        } catch (Exception e) {
            LOG.errorf("RPC call failed: %s", e.getMessage());
            String errorJson = String.format(
                    "{\"rpcConnected\": false, \"errorMessage\": \"%s\"}",
                    e.getMessage().replace("\"", "'")
            );
            broadcastMessage(errorJson);
        }
    }

    private void broadcastMessage(String message) {
        sessions.removeIf(session -> {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(message);
                    return false;
                } catch (Exception e) {
                    LOG.warnf("Failed to send message to session %s: %s", session.getId(), e.getMessage());
                    return true;
                }
            }
            return true;
        });
    }
}