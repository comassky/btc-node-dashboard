package comasky.rpcClass.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO for subversion distribution statistics.
 * Using a record for immutability and conciseness.
 */
@RegisterForReflection
public record SubverStats(
    String server,
    double percentage
) {}