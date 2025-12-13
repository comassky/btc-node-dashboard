package comasky.rpcClass.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * DTO for subversion distribution.
 */
@RegisterForReflection
public record SubverDistribution(
    List<SubverStats> inbound,
    List<SubverStats> outbound
) {}