import { describe, it, expect, beforeEach } from 'vitest';
import * as nodeHealth from '../nodeHealth';

describe('nodeHealth utils', () => {
  // Mock complet pour BlockChainInfo
  const baseBlockChainInfo = {
    chain: 'main',
    blocks: 100,
    headers: 100,
    bestblockhash: '0000000000000000000',
    difficulty: 1,
    time: Math.floor(Date.now() / 1000),
    mediantime: Math.floor(Date.now() / 1000),
    verificationprogress: 1,
    initialblockdownload: false,
    chainwork: '00',
    size_on_disk: 0,
    pruned: false,
    pruneheight: null,
  };

  beforeEach(() => {
    nodeHealth.setMinOutboundPeers(8);
  });

  it('detects low outbound peers', () => {
    expect(nodeHealth.hasLowOutboundPeers(5)).toBe(true);
    expect(nodeHealth.hasLowOutboundPeers(10)).toBe(false);
  });

  it('computes header-block diff', () => {
    expect(nodeHealth.getHeaderBlockDiff({ ...baseBlockChainInfo, headers: 100, blocks: 95 })).toBe(
      5
    );
  });

  it('detects block too old', () => {
    const now = Math.floor(Date.now() / 1000);
    expect(nodeHealth.isBlockTooOld(now - 4000)).toBe(true);
    expect(nodeHealth.isBlockTooOld(now)).toBe(false);
  });

  it('detects syncing', () => {
    expect(nodeHealth.isSyncing({ ...baseBlockChainInfo, headers: 100, blocks: 90 })).toBe(true);
    expect(nodeHealth.isSyncing({ ...baseBlockChainInfo, headers: 100, blocks: 99 })).toBe(false);
  });

  it('detects not fully synced', () => {
    expect(nodeHealth.isNotFullySynced({ ...baseBlockChainInfo, verificationprogress: 0.9 })).toBe(
      true
    );
    expect(
      nodeHealth.isNotFullySynced({ ...baseBlockChainInfo, verificationprogress: 0.99999 })
    ).toBe(false);
  });

  it('detects node out of sync', () => {
    const blockchain = {
      ...baseBlockChainInfo,
      headers: 100,
      blocks: 90,
      verificationprogress: 0.9,
    };
    const block = { time: Math.floor(Date.now() / 1000) - 4000, nTx: 2000 };
    expect(nodeHealth.isNodeOutOfSync(blockchain, block)).toBe(true);
  });
});
