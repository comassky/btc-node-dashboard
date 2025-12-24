import { describe, it, expect, beforeEach } from 'vitest';
import * as nodeHealth from '../nodeHealth';

describe('nodeHealth utils', () => {
  beforeEach(() => {
    nodeHealth.setMinOutboundPeers(8);
  });

  it('detects low outbound peers', () => {
    expect(nodeHealth.hasLowOutboundPeers(5)).toBe(true);
    expect(nodeHealth.hasLowOutboundPeers(10)).toBe(false);
  });

  it('computes header-block diff', () => {
    expect(nodeHealth.getHeaderBlockDiff({ headers: 100, blocks: 95 })).toBe(5);
  });

  it('detects block too old', () => {
    const now = Math.floor(Date.now() / 1000);
    expect(nodeHealth.isBlockTooOld(now - 4000)).toBe(true);
    expect(nodeHealth.isBlockTooOld(now)).toBe(false);
  });

  it('detects syncing', () => {
    expect(nodeHealth.isSyncing({ headers: 100, blocks: 90 })).toBe(true);
    expect(nodeHealth.isSyncing({ headers: 100, blocks: 99 })).toBe(false);
  });

  it('detects not fully synced', () => {
    expect(nodeHealth.isNotFullySynced({ verificationprogress: 0.9 })).toBe(true);
    expect(nodeHealth.isNotFullySynced({ verificationprogress: 0.99999 })).toBe(false);
  });

  it('detects node out of sync', () => {
    const blockchain = { headers: 100, blocks: 90, verificationprogress: 0.9 };
    const block = { time: Math.floor(Date.now() / 1000) - 4000 };
    expect(nodeHealth.isNodeOutOfSync(blockchain, block)).toBe(true);
  });
});