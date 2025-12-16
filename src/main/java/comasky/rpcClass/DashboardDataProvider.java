package comasky.rpcClass;

import comasky.rpcClass.dto.GlobalResponse;
import io.smallrye.mutiny.Uni;

/**
 * Abstraction for providing dashboard data.
 * Implementations may fetch data from RPC, mocks, or caches.
 */
public interface DashboardDataProvider {
    Uni<GlobalResponse> getData();
}
