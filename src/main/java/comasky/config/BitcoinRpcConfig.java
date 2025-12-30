package comasky.config;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Range;

/**
 * Configuration properties for the Bitcoin Core RPC connection.
 * The properties are prefixed with "bitcoin.rpc" in application.properties.
 */
@ConfigMapping(prefix = "bitcoin.rpc")
public interface BitcoinRpcConfig {

    /**
     * The scheme for the RPC connection (http or https).
     */
    @NotBlank
    @Pattern(regexp = "http|https", message = "must be 'http' or 'https'")
    String scheme();

    /**
     * The hostname or IP address of the Bitcoin Core RPC server.
     */
    @NotBlank
    String host();

    /**
     * The port of the Bitcoin Core RPC server.
     */
    @Range(min = 1, max = 65535)
    int port();

    /**
     * The username for RPC authentication.
     */
    @NotBlank
    String user();

    /**
     * The password for RPC authentication.
     */
    @NotBlank
    String password();
}
