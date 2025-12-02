package comasky.api; // Assurez-vous d'avoir le bon package


import com.fasterxml.jackson.databind.ObjectMapper;
import comasky.rpcClass.GlobalResponse;
import comasky.rpcClass.RpcServices;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
public class DashboardWebSocket {

    @Inject
    RpcServices rpcServices;
    @Inject
    ObjectMapper objectMapper;

    // NOUVEAU : Injection de la propriété de configuration
    @ConfigProperty(name = "dashboard.polling.interval.seconds", defaultValue = "5")
    int pollingIntervalSeconds;

    private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @PostConstruct
    void startScheduler() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::sendData, 0, pollingIntervalSeconds, TimeUnit.SECONDS);
    }


    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session); // Ajoute la nouvelle session
        System.out.println("WebSocket opened: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session); // Retire la session fermée
        System.out.println("WebSocket closed: " + session.getId());
    }

    private void sendData() {
        try {
            GlobalResponse data = rpcServices.getData();
            String jsonMessage = objectMapper.writeValueAsString(data);

            sessions.forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(jsonMessage);
                }
            });

        } catch (Exception rpcException) {
            System.err.println("RPC Call Failed (no data sent): " + rpcException.getMessage());

            String errorJson = String.format(
                    "{\"rpcConnected\": false, \"errorMessage\": \"%s\"}",
                    rpcException.getMessage().replace("\"", "'")
            );

            sessions.forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(errorJson);
                }
            });
        }
    }
}