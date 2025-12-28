package comasky;

import comasky.config.BitcoinRpcConfig;
import comasky.config.DashboardConfig;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

/**
 * Main application entry point for the Bitcoin Node Dashboard.
 * Handles configuration validation and logs startup configuration.
 */
@QuarkusMain
public class BtcApiApp implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(BtcApiApp.class);

    @Inject
    BitcoinRpcConfig rpcConfig;

    @Inject
    DashboardConfig dashboardConfig;

    @Inject
    Config config; // Keep for quarkus.http.io-threads

    /**
     * Main entry point for the Quarkus application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Quarkus.run(BtcApiApp.class, args);
    }

    @Override
    public int run(String... args) {
        try {
            logConfiguration();
            Quarkus.waitForExit();
            return 0;
        } catch (Exception e) {
            LOG.error("Startup failed: " + e.getMessage(), e);
            return 1;
        }
    }

    /**
     * Logs the current application configuration for debugging purposes.
     */
    private void logConfiguration() {
        LOG.info("+================ Bitcoin Node Dashboard Config ================");
        LOG.infof("Java: %s   | Log level: %s   | Quarkus: %s",
                System.getProperty("java.version"),
                config.getOptionalValue("quarkus.log.level", String.class).orElse("INFO"),
                getQuarkusVersion());
        LOG.info("---------------------------------------------------------------");
        LOG.infof("RPC: %s://%s:%d  [user: %s | pass: %s]",
                rpcConfig.scheme(), rpcConfig.host(), rpcConfig.port(), rpcConfig.user(), maskPassword(rpcConfig.password()));
        LOG.infof("Polling Interval: %ds", dashboardConfig.pollingIntervalSeconds());
        LOG.infof("Disable Mempool: %s", dashboardConfig.disableMempool());
        LOG.info("---------------------------------------------------------------");
        
        long pollingIntervalMs = dashboardConfig.pollingIntervalSeconds() * 1000L;
        long bufferMs = dashboardConfig.cache().validityBufferMs();
        long cacheValidityMs = Math.max(100, pollingIntervalMs - bufferMs);

        LOG.infof("Min Outbound Peers: %d | Cache Buffer: %dms | Cache Validity: %dms",
                dashboardConfig.minOutboundPeers(),
                bufferMs,
                cacheValidityMs);
                
        LOG.infof("Max Cache Items: %d | Max Sessions: %d | Quarkus IO Threads: %s",
                dashboardConfig.cache().maxItems(),
                dashboardConfig.sessionsMax(),
                config.getOptionalValue("quarkus.http.io-threads", String.class).orElse("N/A"));
        LOG.info("===============================================================\n");
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

    /**
     * Attempts to retrieve the Quarkus version from the package metadata.
     * @return the Quarkus version as a String, or "unknown" if not found
     */
    private String getQuarkusVersion() {
        Package pkg = io.quarkus.runtime.Quarkus.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        return "unknown";
    }
}
