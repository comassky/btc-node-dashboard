package comasky.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Centralized configuration for Bitcoin RPC connection and dashboard polling.
 */
@ApplicationScoped
public class BitcoinRpcConfig {
    @ConfigProperty(name = "bitcoin.rpc.scheme")
    public String rpcScheme;

    @ConfigProperty(name = "bitcoin.rpc.host")
    public String rpcHost;

    @ConfigProperty(name = "bitcoin.rpc.port")
    public int rpcPort;

    @ConfigProperty(name = "bitcoin.rpc.user")
    public String rpcUser;

    @ConfigProperty(name = "bitcoin.rpc.password")
    public String rpcPassword;

    @ConfigProperty(name = "dashboard.polling.interval.seconds")
    public int pollingInterval;
}
