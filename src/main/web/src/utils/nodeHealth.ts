import { BlockChainInfo, BlockInfoResponse } from '@types';


/**
 * Node health monitoring utilities.
 * Defines thresholds and checks for Bitcoin node synchronization status.
 */

let MIN_OUTBOUND_PEERS = 8;
export const MAX_BLOCK_AGE_SECONDS = 3600; // 1 hour
export const MAX_HEADER_BLOCK_DIFF = 2;
export const MIN_VERIFICATION_PROGRESS = 0.9999;




/**
 * Sets the minimum number of outbound peers required for healthy status.
 * @param value Minimum outbound peers
 */
export function setMinOutboundPeers(value: number): void {
    MIN_OUTBOUND_PEERS = value;
}


/**
 * Checks if the outbound peer count is below the healthy threshold.
 * @param outboundCount Number of outbound peers
 * @returns True if outbound peers are low
 */
export function hasLowOutboundPeers(outboundCount: number): boolean {
    return outboundCount < MIN_OUTBOUND_PEERS;
}


/**
 * Returns the difference between headers and blocks.
 * @param blockchain Blockchain info
 * @returns Number of headers ahead of blocks
 */
export function getHeaderBlockDiff(blockchain: BlockChainInfo): number {
    return blockchain.headers - blockchain.blocks;
}


/**
 * Checks if the latest block is too old (stale) compared to the current time.
 * @param blockTime Block time (Unix timestamp)
 * @returns True if block is too old
 */
export function isBlockTooOld(blockTime: number): boolean {
    const now = Date.now();
    const then = blockTime * 1000;
    const diffSeconds = Math.floor((now - then) / 1000);
    return diffSeconds > MAX_BLOCK_AGE_SECONDS;
}



/**
 * Checks if the node is currently syncing (headers ahead of blocks).
 * @param blockchain Blockchain info
 * @returns True if node is syncing
 */
export function isSyncing(blockchain: BlockChainInfo): boolean {
    return getHeaderBlockDiff(blockchain) > MAX_HEADER_BLOCK_DIFF;
}


/**
 * Checks if the node's verification progress is below the fully synced threshold.
 * @param blockchain Blockchain info
 * @returns True if not fully synced
 */
export function isNotFullySynced(blockchain: BlockChainInfo): boolean {
    return blockchain.verificationprogress < MIN_VERIFICATION_PROGRESS;
}


/**
 * Checks if the node is out of sync (block too old, syncing, or not fully synced).
 * @param blockchain Blockchain info
 * @param block Block info
 * @returns True if node is out of sync
 */
export function isNodeOutOfSync(blockchain: BlockChainInfo, block: BlockInfoResponse): boolean {
    return isBlockTooOld(block.time) || isSyncing(blockchain) || isNotFullySynced(blockchain);
}

/**
 * Returns a warning message describing the node's sync status.
 * @param blockchain Blockchain info
 * @param block Block info
 * @param formatTimeSince Function to format time since block
 * @returns Warning message string
 */
export function getSyncWarningMessage(
    blockchain: BlockChainInfo,
    block: BlockInfoResponse,
    formatTimeSince: (timestamp: number) => string
): string {
    const progress = (blockchain.verificationprogress * 100).toFixed(2);
    
    if (isBlockTooOld(block.time)) {
        return `Last block is ${formatTimeSince(block.time)} old. Your node may have lost connection to the network or stopped syncing.`;
    }
    if (isSyncing(blockchain)) {
        const headerBlockDiff = getHeaderBlockDiff(blockchain);
        return `Node is syncing: ${headerBlockDiff} blocks behind. Verification progress: ${progress}%`;
    }
    if (isNotFullySynced(blockchain)) {
        return `Node is still syncing. Verification progress: ${progress}%`;
    }
    return 'Node is out of sync with the blockchain.';
}
