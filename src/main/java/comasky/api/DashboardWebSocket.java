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

/**
 * WebSocket endpoint for real-time dashboard updates.
 * Broadcasts Bitcoin node data to all connected clients at configured intervals.
 * Implements caching to avoid redundant RPC calls when data is fresh.
 */
@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
public class DashboardWebSocket {

    private static final Logger LOG = Logger.getLogger(DashboardWebSocket.class);

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private ScheduledExecutorService scheduler;
    
    // Cache for RPC data - thread-safe with synchronized access
    private final Object cacheLock = new Object();
    private CachedMessage cachedMessage;

    @Inject
    RpcServices rpcServices;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "dashboard.polling.interval.seconds", defaultValue = "5")
    int pollingIntervalSeconds;
    
    private long cacheValidityMs;

    @PostConstruct
    void startScheduler() {
        // Pre-calculate cache validity to avoid recalculation on each fetch
        cacheValidityMs = Math.max(100, (pollingIntervalSeconds * 1000L) - 100);
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "websocket-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        // Schedule on worker thread to avoid blocking event loop
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendData();
            } catch (Exception e) {
                LOG.errorf("Error in scheduled task: %s", e.getMessage());
            }
        }, 0, pollingIntervalSeconds, TimeUnit.SECONDS);
        LOG.debugf("WebSocket scheduler started with %d seconds interval", pollingIntervalSeconds);
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
        LOG.debugf("WebSocket opened: %s (total: %d)", session.getId(), sessions.size());
        scheduler.execute(() -> sendDataToSession(session));
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        LOG.debugf("WebSocket closed: %s (remaining: %d)", session.getId(), sessions.size());
    }

    private void sendData() {
        if (sessions.isEmpty()) {
            return;
        }

        CachedMessage message = fetchAndCacheMessage();
        if (message != null) {
            broadcastMessage(message.serializedJson());
        }
    }

    private void sendDataToSession(Session session) {
        if (!session.isOpen()) {
            return;
        }

        CachedMessage message = fetchAndCacheMessage();
        if (message != null) {
            session.getAsyncRemote().sendText(message.serializedJson());
        }
    }

    private CachedMessage fetchAndCacheMessage() {
        synchronized (cacheLock) {
            if (cachedMessage != null && cachedMessage.isValid(cacheValidityMs)) {
                return cachedMessage;
            }
            
            try {
                GlobalResponse data = rpcServices.getData();
                String json = objectMapper.writeValueAsString(data);
                cachedMessage = CachedMessage.success(data, json);
                return cachedMessage;
            } catch (Exception e) {
                LOG.warnf("RPC call failed: %s", e.getMessage());
                String errorMsg = e.getMessage().replace("\"", "'");
                String errorJson = new StringBuilder(64)
                    .append("{\"rpcConnected\": false, \"errorMessage\": \"")
                    .append(errorMsg)
                    .append("\"}")
                    .toString();
                cachedMessage = CachedMessage.error(e.getMessage(), errorJson);
                return cachedMessage;
            }
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