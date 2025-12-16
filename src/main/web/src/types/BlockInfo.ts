
/**
 * Information about a specific block.
 */
export interface BlockInfoResponse {
  /** Block time (Unix timestamp) */
  time: number;
  /** Number of transactions in the block */
  nTx: number;
  /** Block hash (optional) */
  hash?: string;
}
