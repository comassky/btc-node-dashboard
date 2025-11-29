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

    // NOUVEAU : Gérer plusieurs sessions
    private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // NOUVEAU : Démarrer le scheduler une seule fois (via CDI/LifeCycle)
    @PostConstruct
    // Démarre au lancement de l'application
    void startScheduler() {
        // Planifie l'envoi des données toutes les 5 secondes
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::sendData, 0, pollingIntervalSeconds, TimeUnit.SECONDS);
    }

    // --- Gestion du Cycle de Vie WebSocket ---

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session); // Ajoute la nouvelle session
        System.out.println("WebSocket opened: " + session.getId());
        // Pas besoin d'appeler startSendingData ici
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session); // Retire la session fermée
        System.out.println("WebSocket closed: " + session.getId());
        // NE PAS DÉTRUIRE LE SCHEDULER ICI
    }

    // ... (Votre @OnError reste le même) ...

    // --- Logique d'envoi de données (Modifiée) ---

    private void sendData() {
        try {
            // Tente l'appel RPC une seule fois
            GlobalResponse data = rpcServices.getData();
            String jsonMessage = objectMapper.writeValueAsString(data);

            // Si l'appel RPC a réussi, on envoie à TOUTES les sessions
            sessions.forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(jsonMessage);
                }
            });

        } catch (Exception rpcException) {
            // L'erreur RPC se produit, mais cela ne fait pas planter le scheduler.
            System.err.println("RPC Call Failed (no data sent): " + rpcException.getMessage());

            // Si l'appel RPC échoue, envoyez un message d'erreur/statut à TOUTES les sessions
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