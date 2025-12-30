import { describe, it, expect } from 'vitest';
import type {
  GeneralStats,
  BlockChainInfo,
  NetworkInfoResponse,
  BlockInfoResponse,
  Peer,
  SubverDistribution,
} from '@types';

describe('Type Definitions', () => {
  describe('GeneralStats', () => {
    it('should have all general stats properties', () => {
      const stats: GeneralStats = {
        inboundCount: 50,
        outboundCount: 10,
        totalPeers: 60,
      };

      expect(stats.inboundCount).toBe(50);
      expect(stats.outboundCount).toBe(10);
      expect(stats.totalPeers).toBe(60);
    });

    it('should calculate total peers correctly', () => {
      const stats: GeneralStats = {
        inboundCount: 25,
        outboundCount: 15,
        totalPeers: 40,
      };

      expect(stats.totalPeers).toBe(stats.inboundCount + stats.outboundCount);
    });
  });

  describe('BlockChainInfo', () => {
    it('should have all blockchain properties', () => {
      const blockchain: BlockChainInfo = {
        blocks: 850000,
        headers: 850000,
        chain: 'main',
        verificationprogress: 0.9999,
        difficulty: 75000000000000,
        bestblockhash: '',
        time: 0,
        mediantime: 0,
        initialblockdownload: false,
        chainwork: '',
        size_on_disk: 0,
        pruned: false,
        pruneheight: null,
      };

      expect(blockchain.blocks).toBe(850000);
      expect(blockchain.chain).toBe('main');
      expect(blockchain.difficulty).toBeGreaterThan(0);
    });
  });

  describe('NetworkInfoResponse', () => {
    it('should have all node info properties', () => {
      const nodeInfo: NetworkInfoResponse = {
        version: 270000,
        subversion: '/Satoshi:27.0.0/',
        protocolversion: 70016,
        localservices: '',
        localservicesnames: [],
        localrelay: false,
        timeoffset: 0,
        connections: 0,
        networkactive: false,
        networks: [],
        localaddresses: [],
      };

      expect(nodeInfo.version).toBe(270000);
      expect(nodeInfo.subversion).toContain('Satoshi');
      expect(nodeInfo.protocolversion).toBe(70016);
    });
  });

  describe('BlockInfoResponse', () => {
    it('should have all block info properties', () => {
      const blockInfoResponse: BlockInfoResponse = {
        time: 1733443200,
        nTx: 2500,
        hash: '00000000000000000001a7b38faa4bdaa47a06fc0f12345abcdef1234567890a',
      };

      expect(blockInfoResponse.hash).toMatch(/^[0-9a-f]{64}$/);
      expect(blockInfoResponse.time).toBeGreaterThan(0);
      expect(blockInfoResponse.nTx).toBeGreaterThan(0);
    });

    it('should handle block without hash', () => {
      const blockInfoResponse: BlockInfoResponse = {
        time: 1733443200,
        nTx: 2500,
      };

      expect(blockInfoResponse.hash).toBeUndefined();
      expect(blockInfoResponse.nTx).toBe(2500);
    });
  });

  describe('Peer', () => {
    it('should have all peer properties', () => {
      const peer: Peer = {
        id: 1,
        addr: '192.168.1.100:8333',
        network: 'ipv4',
        connection_type: 'inbound',
        subver: '/Satoshi:27.0.0/',
        version: 270000,
        timeoffset: 0,
        conntime: 1733443200,
        minping: 0.025,
        bytesrecv: 2000000,
        bytessent: 1000000,
      };

      expect(peer.id).toBe(1);
      expect(peer.addr).toContain(':');
      expect(peer.subver).toContain('Satoshi');
      expect(peer.minping).toBeGreaterThan(0);
    });

    it('should handle peer with null minping', () => {
      const peer: Peer = {
        id: 2,
        addr: '192.168.1.101:8333',
        network: 'ipv4',
        connection_type: 'outbound',
        subver: '/Satoshi:26.0.0/',
        version: 260000,
        timeoffset: 0,
        conntime: 1733443200,
        minping: null,
        bytesrecv: 1000000,
        bytessent: 500000,
      };

      expect(peer.minping).toBeNull();
      expect(peer.connection_type).toBe('outbound');
    });

    it('should handle peer with null network', () => {
      const peer: Peer = {
        id: 3,
        addr: 'unknown:8333',
        network: null,
        connection_type: 'inbound',
        subver: '/Satoshi:25.0.0/',
        version: 250000,
        timeoffset: 0,
        conntime: 1733443200,
        minping: null,
        bytesrecv: 500000,
        bytessent: 250000,
      };

      expect(peer.network).toBeNull();
    });
  });

  describe('SubverDistribution', () => {
    it('should have correct structure', () => {
      const distribution: SubverDistribution = {
        server: '/Satoshi:27.0.0/',
        count: 10,
        percentage: 50.5,
      };

      expect(distribution.server).toBe('/Satoshi:27.0.0/');
      expect(distribution.count).toBe(10);
      expect(distribution.percentage).toBe(50.5);
    });

    it('should handle array of distributions', () => {
      const distributions: SubverDistribution[] = [
        { server: '/Satoshi:27.0.0/', count: 10, percentage: 50 },
        { server: '/Satoshi:26.0.0/', count: 6, percentage: 30 },
        { server: '/Satoshi:25.0.0/', count: 4, percentage: 20 },
      ];

      expect(distributions.length).toBe(3);
      expect(
        distributions.reduce((sum: number, item: SubverDistribution) => sum + item.count, 0)
      ).toBe(20);
    });

    it('should validate percentages sum to 100', () => {
      const distributions: SubverDistribution[] = [
        { server: '/Satoshi:27.0.0/', count: 10, percentage: 66.67 },
        { server: '/Satoshi:26.0.0/', count: 5, percentage: 33.33 },
      ];

      const totalPercentage = distributions.reduce((sum, dist) => sum + dist.percentage, 0);
      expect(Math.round(totalPercentage)).toBe(100);
    });
  });
});

describe('Data Normalization', () => {
  it('should provide default values for missing data', () => {
    const defaultStats: GeneralStats = {
      inboundCount: 0,
      outboundCount: 0,
      totalPeers: 0,
    };

    expect(defaultStats.inboundCount).toBe(0);
    expect(defaultStats.outboundCount).toBe(0);
    expect(defaultStats.totalPeers).toBe(0);
  });

  it('should handle large numbers', () => {
    const blockchain: BlockChainInfo = {
      blocks: 1000000,
      headers: 1000000,
      chain: 'main',
      verificationprogress: 1.0,
      difficulty: 100000000000000,
      bestblockhash: '',
      time: 0,
      mediantime: 0,
      initialblockdownload: false,
      chainwork: '',
      size_on_disk: 0,
      pruned: false,
      pruneheight: null,
    };

    expect(blockchain.blocks).toBeGreaterThan(0);
    expect(blockchain.difficulty).toBeGreaterThan(0);
  });

  it('should handle edge cases for peer data', () => {
    const peer: Peer = {
      id: 0,
      addr: '',
      network: null,
      connection_type: '',
      subver: '',
      version: 0,
      timeoffset: 0,
      conntime: 0,
      minping: null,
      bytesrecv: 0,
      bytessent: 0,
    };

    expect(peer.id).toBe(0);
    expect(peer.network).toBeNull();
    expect(peer.minping).toBeNull();
  });
});
