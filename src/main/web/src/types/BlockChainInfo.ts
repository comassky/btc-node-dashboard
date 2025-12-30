/**
 * Blockchain info returned by the Bitcoin node.
 */
export interface BlockChainInfo {
  /** Chain name (e.g., 'main', 'test') */
  chain: string;
  /** Current block height */
  blocks: number;
  /** Current header count */
  headers: number;
  /** Hash of the best block */
  bestblockhash: string;
  /** Current network difficulty */
  difficulty: number;
  /** Block time (Unix timestamp) */
  time: number;
  /** Median block time */
  mediantime: number;
  /** Verification progress (0-1) */
  verificationprogress: number;
  /** Whether node is in initial block download */
  initialblockdownload: boolean;
  /** Chainwork value */
  chainwork: string;
  /** Blockchain size on disk (bytes) */
  size_on_disk: number;
  /** Whether node is pruned */
  pruned: boolean;
  /** Prune height if pruned, else null */
  pruneheight: number | null;
}
