import { BlockChainInfo, BlockInfoResponse } from '@types';

/**
 * Node health monitoring utilities.
 * Defines thresholds and checks for Bitcoin node synchronization status.
 */

let MIN_OUTBOUND_PEERS = 8;
export const MAX_BLOCK_AGE_SECONDS = 3600; // 1 hour
export const MAX_HEADER_BLOCK_DIFF = 2;
export const MIN_VERIFICATION_PROGRESS = 0.9999;



export function setMinOutboundPeers(value: number): void {
    MIN_OUTBOUND_PEERS = value;
}

export function hasLowOutboundPeers(outboundCount: number): boolean {
    return outboundCount < MIN_OUTBOUND_PEERS;
}

export function getHeaderBlockDiff(blockchain: BlockChainInfo): number {
    return blockchain.headers - blockchain.blocks;
}

export function isBlockTooOld(blockTime: number): boolean {
    // blockTime is in seconds (Unix timestamp)
    const now = Date.now();
    const then = blockTime * 1000;
    const diffSeconds = Math.floor((now - then) / 1000);
    return diffSeconds > MAX_BLOCK_AGE_SECONDS;
}


export function isSyncing(blockchain: BlockChainInfo): boolean {
    return getHeaderBlockDiff(blockchain) > MAX_HEADER_BLOCK_DIFF;
}

export function isNotFullySynced(blockchain: BlockChainInfo): boolean {
    return blockchain.verificationprogress < MIN_VERIFICATION_PROGRESS;
}

export function isNodeOutOfSync(blockchain: BlockChainInfo, block: BlockInfoResponse): boolean {
    return isBlockTooOld(block.time) || isSyncing(blockchain) || isNotFullySynced(blockchain);
}

export function getSyncWarningMessage(blockchain: BlockChainInfo, block: BlockInfoResponse, formatTimeSince: (timestamp: number) => string): string {
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
