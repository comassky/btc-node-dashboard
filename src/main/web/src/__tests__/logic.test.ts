import { describe, it, expect } from 'vitest';
import { filter, map, reduce, groupBy } from 'lodash-es';

describe('WebSocket Reconnection Logic', () => {
  const WS_RECONNECT_BASE_DELAY = 1000;
  const WS_RECONNECT_MAX_DELAY = 30000;
  const WS_RECONNECT_MULTIPLIER = 2;

  const calculateReconnectDelay = (attempts: number): number => {
    return Math.min(
      WS_RECONNECT_BASE_DELAY * Math.pow(WS_RECONNECT_MULTIPLIER, attempts),
      WS_RECONNECT_MAX_DELAY
    );
  };

  it('should start with base delay', () => {
    expect(calculateReconnectDelay(0)).toBe(1000);
  });

  it('should double delay on each attempt', () => {
    expect(calculateReconnectDelay(1)).toBe(2000);
    expect(calculateReconnectDelay(2)).toBe(4000);
    expect(calculateReconnectDelay(3)).toBe(8000);
  });

  it('should cap at maximum delay', () => {
    expect(calculateReconnectDelay(10)).toBe(30000);
    expect(calculateReconnectDelay(20)).toBe(30000);
  });

  it('should use exponential backoff', () => {
    const delays = map([0, 1, 2, 3, 4], calculateReconnectDelay);
    
    for (let i = 1; i < delays.length; i++) {
      expect(delays[i]).toBeGreaterThanOrEqual(delays[i - 1]);
    }
  });
});

describe('Data Validation', () => {
  const isValidBlockHeight = (height: number): boolean => {
    return height >= 0 && Number.isInteger(height);
  };

  const isValidDifficulty = (difficulty: number): boolean => {
    return difficulty >= 0 && Number.isFinite(difficulty);
  };

  const isValidPercentage = (value: number): boolean => {
    return value >= 0 && value <= 1;
  };

  it('should validate block height', () => {
    expect(isValidBlockHeight(870000)).toBe(true);
    expect(isValidBlockHeight(0)).toBe(true);
    expect(isValidBlockHeight(-1)).toBe(false);
    expect(isValidBlockHeight(1.5)).toBe(false);
  });

  it('should validate difficulty', () => {
    expect(isValidDifficulty(95000000000000)).toBe(true);
    expect(isValidDifficulty(0)).toBe(true);
    expect(isValidDifficulty(-1)).toBe(false);
    expect(isValidDifficulty(Infinity)).toBe(false);
  });

  it('should validate verification progress percentage', () => {
    expect(isValidPercentage(0.9999)).toBe(true);
    expect(isValidPercentage(0)).toBe(true);
    expect(isValidPercentage(1)).toBe(true);
    expect(isValidPercentage(1.5)).toBe(false);
    expect(isValidPercentage(-0.1)).toBe(false);
  });
});

describe('Peer Filtering Logic', () => {
  interface TestPeer {
    id: number;
    inbound: boolean;
    subver?: string;
  }

  const filterInboundPeers = (peers: TestPeer[]): TestPeer[] => filter(peers, { inbound: true });
  const filterOutboundPeers = (peers: TestPeer[]): TestPeer[] => filter(peers, { inbound: false });
  const groupBySubversion = (peers: TestPeer[]): Record<string, TestPeer[]> => groupBy(peers, p => p.subver || 'Unknown');

  it('should filter inbound peers correctly', () => {
    const peers: TestPeer[] = [
      { id: 1, inbound: true },
      { id: 2, inbound: false },
      { id: 3, inbound: true },
    ];

    const inbound = filterInboundPeers(peers);
    expect(inbound).toHaveLength(2);
    expect(inbound.every(p => p.inbound)).toBe(true);
  });

  it('should filter outbound peers correctly', () => {
    const peers: TestPeer[] = [
      { id: 1, inbound: true },
      { id: 2, inbound: false },
      { id: 3, inbound: false },
    ];

    const outbound = filterOutboundPeers(peers);
    expect(outbound).toHaveLength(2);
    expect(outbound.every(p => !p.inbound)).toBe(true);
  });

  it('should group peers by subversion', () => {
    const peers: TestPeer[] = [
      { id: 1, inbound: true, subver: '/Satoshi:27.0.0/' },
      { id: 2, inbound: true, subver: '/Satoshi:27.0.0/' },
      { id: 3, inbound: true, subver: '/Satoshi:26.0.0/' },
    ];

    const groups = groupBySubversion(peers);
    expect(groups.size).toBe(2);
    expect(groups.get('/Satoshi:27.0.0/')).toHaveLength(2);
    expect(groups.get('/Satoshi:26.0.0/')).toHaveLength(1);
  });

  it('should handle peers without subversion', () => {
    const peers: TestPeer[] = [
      { id: 1, inbound: true },
      { id: 2, inbound: true, subver: '/Satoshi:27.0.0/' },
    ];

    const groups = groupBySubversion(peers);
    expect(groups.has('Unknown')).toBe(true);
    expect(groups.get('Unknown')).toHaveLength(1);
  });
});

describe('Chart Data Preparation', () => {
  interface SubverStats {
    server: string;
    percentage: number;
  }

  const prepareChartData = (stats: SubverStats[]) => {
    return {
      labels: map(stats, s => s.server),
      data: map(stats, s => s.percentage),
    };
  };

  const calculatePercentages = (counts: Map<string, number>): SubverStats[] => {
    const total = reduce(Array.from(counts.values()), (sum, count) => sum + count, 0);
    if (total === 0) return [];

    return map(Array.from(counts.entries()), ([server, count]) => ({
      server,
      percentage: Math.round((count / total) * 10000) / 100,
    }));
  };

  it('should prepare data for chart rendering', () => {
    const stats: SubverStats[] = [
      { server: '/Satoshi:27.0.0/', percentage: 60 },
      { server: '/Satoshi:26.0.0/', percentage: 40 },
    ];

    const chartData = prepareChartData(stats);
    expect(chartData.labels).toHaveLength(2);
    expect(chartData.data).toHaveLength(2);
    expect(chartData.labels[0]).toBe('/Satoshi:27.0.0/');
    expect(chartData.data[0]).toBe(60);
  });

  it('should handle empty stats', () => {
    const chartData = prepareChartData([]);
    expect(chartData.labels).toHaveLength(0);
    expect(chartData.data).toHaveLength(0);
  });

  it('should calculate percentages correctly', () => {
    const counts = new Map([
      ['/Satoshi:27.0.0/', 6],
      ['/Satoshi:26.0.0/', 4],
    ]);

    const percentages = calculatePercentages(counts);
    expect(percentages).toHaveLength(2);
    expect(percentages[0].percentage).toBe(60);
    expect(percentages[1].percentage).toBe(40);
  });

  it('should round percentages to 2 decimal places', () => {
    const counts = new Map([
      ['/Satoshi:27.0.0/', 2],
      ['/Satoshi:26.0.0/', 1],
    ]);

    const percentages = calculatePercentages(counts);
    const total = reduce(percentages, (sum: number, p: { percentage: number }) => sum + p.percentage, 0);
    expect(total).toBeCloseTo(100, 1);
  });
});
