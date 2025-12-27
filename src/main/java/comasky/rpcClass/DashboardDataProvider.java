package comasky.rpcClass;

import comasky.rpcClass.dto.GlobalResponse;
import io.smallrye.mutiny.Uni;

/**
 * Abstraction for providing dashboard data.
 * Implementations may fetch data from RPC, mocks, or caches.
 */
public interface DashboardDataProvider {
    /**
     * Retrieves the global dashboard data.
     * @return a Uni emitting the GlobalResponse
     */
    Uni<GlobalResponse> getData();
}
