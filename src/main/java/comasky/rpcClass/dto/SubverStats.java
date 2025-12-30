package comasky.rpcClass.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO for subversion distribution statistics.
 * Using a record for immutability and conciseness.
 *
 * @param server     The subversion string (e.g., "/Satoshi:25.0.0/").
 * @param percentage The percentage of peers with this subversion.
 */
@RegisterForReflection
public record SubverStats(
    String server,
    double percentage
) {}
