/**
 * Dashboard configuration options.
 */
export interface DashboardConfig {
  /** Minimum number of outbound peers for healthy status */
  minOutboundPeers: number;
  /** Optionally disable mempool display */
  disableMempool?: boolean;
}
