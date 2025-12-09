package comasky;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Main application entry point for the Bitcoin Node Dashboard.
 * Handles configuration validation and logs startup configuration.
 */
@QuarkusMain
public class BtcApiApp implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(BtcApiApp.class);

    @ConfigProperty(name = "bitcoin.rpc.scheme")
    String rpcScheme;

    @ConfigProperty(name = "bitcoin.rpc.host")
    String rpcHost;

    @ConfigProperty(name = "bitcoin.rpc.port")
    int rpcPort;

    @ConfigProperty(name = "bitcoin.rpc.user")
    String rpcUser;

    @ConfigProperty(name = "bitcoin.rpc.password")
    String rpcPassword;

    @ConfigProperty(name = "dashboard.polling.interval.seconds")
    int pollingInterval;

    public static void main(String[] args) {
        Quarkus.run(BtcApiApp.class, args);
    }

    @Override
    public int run(String... args) {
        try {
            validateConfiguration();
            logConfiguration();
            Quarkus.waitForExit();
            return 0;
        } catch (Exception e) {
            LOG.error("Startup failed: " + e.getMessage(), e);
            return 1;
        }
    }

    private void validateConfiguration() {
        if (isNullOrBlank(rpcScheme) || (!"http".equals(rpcScheme) && !"https".equals(rpcScheme))) {
            throw new IllegalStateException("bitcoin.rpc.scheme is required and must be 'http' or 'https'");
        }
        if (isNullOrBlank(rpcHost)) {
            throw new IllegalStateException("bitcoin.rpc.host is required");
        }
        if (rpcPort < 1 || rpcPort > 65535) {
            throw new IllegalStateException("bitcoin.rpc.port must be between 1 and 65535");
        }
        if (isNullOrBlank(rpcUser) || isNullOrBlank(rpcPassword)) {
            throw new IllegalStateException("bitcoin.rpc.user and bitcoin.rpc.password are required");
        }
        if (pollingInterval < 1 || pollingInterval > 300) {
            throw new IllegalStateException("dashboard.polling.interval.seconds must be between 1 and 300");
        }
    }

    private void logConfiguration() {
        LOG.debugf("=== Application Configuration ===");
        LOG.debugf("Bitcoin RPC: %s://%s:%d", rpcScheme, rpcHost, rpcPort);
        LOG.debugf("RPC User: %s", rpcUser);
        LOG.debugf("RPC Password: %s", maskPassword(rpcPassword));
        LOG.debugf("WebSocket Polling Interval: %d seconds (WS_POLLING_INTERVAL)", pollingInterval);
        LOG.debugf("Min Outbound Peers: %s (MIN_OUTBOUND_PEERS)", System.getenv().getOrDefault("MIN_OUTBOUND_PEERS", "8"));
        LOG.debugf("Cache Validity Buffer (ms): %s (DASHBOARD_CACHE_VALIDITY_BUFFER_MS)", System.getenv().getOrDefault("DASHBOARD_CACHE_VALIDITY_BUFFER_MS", "200"));
        LOG.debugf("Log Level: %s (LOG_LEVEL)", System.getenv().getOrDefault("LOG_LEVEL", "INFO"));
        LOG.debugf("=================================");
    }

    private boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "[NOT SET]";
        }
        if (password.length() <= 4) {
            return "****";
        }
        return password.substring(0, 2) + "****" + password.substring(password.length() - 2);
    }
}