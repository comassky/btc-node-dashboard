package comasky.rpcClass;

import java.util.List;

/**
 * DTO for subversion distribution.
 */
public record SubverDistribution(
    List<SubverStats> inbound,
    List<SubverStats> outbound
) {}