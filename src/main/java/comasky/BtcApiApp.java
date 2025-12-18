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

    /**
     * Main entry point for the Quarkus application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Quarkus.run(BtcApiApp.class, args);
    }

    @Override
    /**
     * Runs the application, validating configuration and starting Quarkus.
     *
     * @param args command-line arguments
     * @return 0 if successful, 1 if startup failed
     */
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

    /**
     * Validates application configuration properties.
     *
     * @throws IllegalStateException if any required property is missing or invalid
     */
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

    /**
     * Logs the current application configuration for debugging purposes.
     */
    private void logConfiguration() {
        LOG.info("=================  💻 Bitcoin Node Dashboard Configuration  =================");
        LOG.info(String.format("%-30s : %-30s", "Java Version", System.getProperty("java.version")));
        LOG.info(String.format("%-30s : %-30s", "Log Level", System.getenv().getOrDefault("LOG_LEVEL", "INFO")));
        LOG.info("----------------------------------------------------------------------");
        LOG.info(String.format("%-30s : %-30s [env: RPC_SCHEME]", "Bitcoin RPC Scheme", rpcScheme));
        LOG.info(String.format("%-30s : %-30s [env: RPC_HOST]", "Bitcoin RPC Host", rpcHost));
        LOG.info(String.format("%-30s : %-30d [env: RPC_PORT]", "Bitcoin RPC Port", rpcPort));
        LOG.info(String.format("%-30s : %-30s [env: RPC_USER]", "Bitcoin RPC User", rpcUser));
        LOG.info(String.format("%-30s : %-30s [env: RPC_PASS]", "Bitcoin RPC Password", maskPassword(rpcPassword)));
        LOG.info("  (can be overridden by env: RPC_HOST, RPC_PORT, RPC_USER, RPC_PASS, RPC_SCHEME)");
        LOG.info("----------------------------------------------------------------------");
        LOG.info(String.format("%-40s : %-10d", "Dashboard Polling Interval (s)", pollingInterval));
        LOG.info(String.format("%-40s : %-10s", "Min Outbound Peers", System.getenv().getOrDefault("MIN_OUTBOUND_PEERS", "8")));
        LOG.info(String.format("%-40s : %-10s", "Cache Validity Buffer (ms)", System.getenv().getOrDefault("DASHBOARD_CACHE_VALIDITY_BUFFER_MS", "200")));
        LOG.info(String.format("%-40s : %-10s", "Dashboard Cache Validity (ms)", System.getenv().getOrDefault("DASHBOARD_CACHE_VALIDITY_MS", "1000")));
        LOG.info(String.format("%-40s : %-10s", "Dashboard Max Cache Size", System.getenv().getOrDefault("DASHBOARD_MAX_CACHE_SIZE", "1000")));
        LOG.info(String.format("%-40s : %-10s", "Dashboard Max Message Size", System.getenv().getOrDefault("DASHBOARD_MAX_MESSAGE_SIZE", "1048576")));
        LOG.info(String.format("%-40s : %-10s", "Dashboard Max Connections", System.getenv().getOrDefault("DASHBOARD_MAX_CONNECTIONS", "100")));
        LOG.info(String.format("%-40s : %-10s", "Dashboard Max Subscriptions", System.getenv().getOrDefault("DASHBOARD_MAX_SUBSCRIPTIONS", "10")));
        LOG.info("----------------------------------------------------------------------");
        LOG.info(String.format("%-30s : %-30s", "WebSocket Polling Int", System.getenv().getOrDefault("WS_POLLING_INTERVAL", String.valueOf(pollingInterval))));
        LOG.info("============================================================================");
    }

    /**
     * Checks if a string is null or blank.
     *
     * @param s the string to check
     * @return true if the string is null or blank, false otherwise
     */
    private boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Masks a password for logging, showing only the first and last two characters.
     *
     * @param password the password to mask
     * @return the masked password string
     */
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