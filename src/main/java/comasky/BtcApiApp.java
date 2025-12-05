package comasky;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@QuarkusMain
public class BtcApiApp implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(BtcApiApp.class);

    @Inject
    @ConfigProperty(name = "bitcoin.rpc.scheme")
    String rpcScheme;

    @Inject
    @ConfigProperty(name = "bitcoin.rpc.host")
    String rpcHost;

    @Inject
    @ConfigProperty(name = "bitcoin.rpc.port")
    int rpcPort;

    @Inject
    @ConfigProperty(name = "bitcoin.rpc.user")
    String rpcUser;

    @Inject
    @ConfigProperty(name = "bitcoin.rpc.password")
    String rpcPassword;

    @Inject
    @ConfigProperty(name = "dashboard.polling.interval.seconds")
    int pollingInterval;

    public static void main(String[] args) {
        Quarkus.run(BtcApiApp.class, args);
    }

    @Override
    public int run(String... args) {
        validateConfiguration();
        
        LOG.infof("=== Application Configuration ===");
        LOG.infof("Bitcoin RPC: %s://%s:%d", rpcScheme, rpcHost, rpcPort);
        LOG.infof("RPC User: %s", rpcUser);
        LOG.infof("RPC Password: %s", maskPassword(rpcPassword));
        LOG.infof("WebSocket Polling Interval: %d seconds", pollingInterval);
        LOG.infof("=================================");

        Quarkus.waitForExit();
        return 0;
    }

    private void validateConfiguration() {
        if (rpcScheme == null || rpcScheme.isBlank()) {
            throw new IllegalStateException("bitcoin.rpc.scheme is required");
        }
        if (!rpcScheme.equals("http") && !rpcScheme.equals("https")) {
            throw new IllegalStateException("bitcoin.rpc.scheme must be 'http' or 'https'");
        }
        if (rpcHost == null || rpcHost.isBlank()) {
            throw new IllegalStateException("bitcoin.rpc.host is required");
        }
        if (rpcPort < 1 || rpcPort > 65535) {
            throw new IllegalStateException("bitcoin.rpc.port must be between 1 and 65535");
        }
        if (rpcUser == null || rpcUser.isBlank()) {
            throw new IllegalStateException("bitcoin.rpc.user is required");
        }
        if (rpcPassword == null || rpcPassword.isBlank()) {
            throw new IllegalStateException("bitcoin.rpc.password is required");
        }
        if (pollingInterval < 1 || pollingInterval > 300) {
            throw new IllegalStateException("dashboard.polling.interval.seconds must be between 1 and 300");
        }
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