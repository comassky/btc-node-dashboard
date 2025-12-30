/**
 * Provides statistics about the current state of the node's mempool (unconfirmed transactions).
 */
export interface MempoolInfoResponse {
  /** True if the mempool is fully loaded after startup/restart */
  loaded: boolean;
  /** Total number of transactions in the mempool */
  size: number;
  /** Total size (in bytes) of all transactions in the mempool */
  bytes: number;
  /** Memory (in bytes) used by the mempool (including indexes) */
  usage: number;
  /** Configured maximum mempool size (in bytes) */
  maxmempool: number;
  /** Minimum fee rate (in BTC/kB) for transactions to be accepted into the mempool */
  mempoolminfee: number;
  /** Minimum fee rate for relaying a transaction (in BTC/kB) */
  minrelaytxfee: number;
  /** Number of transactions in the mempool not yet broadcast to peers */
  unbroadcastcount: number;
  /** Total fees (in BTC) of all transactions in the mempool */
  total_fee: number;
}
