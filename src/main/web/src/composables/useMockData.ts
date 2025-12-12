import { ref } from 'vue';
import { range } from 'lodash-es';
import type { DashboardData } from '@types';

export type MockScenario = 'normal' | 'disconnected' | 'lowPeers' | 'outOfSync';

/**
 * Mock data generator for testing error and warning states in development.
 * Provides simulated dashboard data for different scenarios without requiring a live Bitcoin node.
 * 
 * Enable mock mode via:
 * - Setting VITE_MOCK_MODE=true in .env file
 * - Or modify MOCK_MODE.value directly in code
 */
export function useMockData() {
  const MOCK_MODE = ref(import.meta.env.VITE_MOCK_MODE === 'true' || false);
  const mockScenario = ref<MockScenario>('normal');

  /**
   * Cycles through available mock scenarios in sequence.
   */
  const cycleMockScenario = () => {
    const scenarios: MockScenario[] = ['normal', 'disconnected', 'lowPeers', 'outOfSync'];
    const currentIndex = scenarios.indexOf(mockScenario.value);
    mockScenario.value = scenarios[(currentIndex + 1) % scenarios.length];
  };

  /**
   * Generates mock dashboard data based on the current scenario.
   * 
   * Scenarios:
   * - normal: Healthy node with good peer count and sync status
   * - disconnected: Simulates WebSocket/RPC connection loss
   * - lowPeers: Low outbound peer count (triggers warning)
   * - outOfSync: Node out of sync with network (old block, headers ahead)
   */
  const generateMockData = (): DashboardData => {
    const now = Math.floor(Date.now() / 1000);
    
    return {
      generalStats: { 
        inboundCount: mockScenario.value === 'lowPeers' ? 5 : 45,
        outboundCount: mockScenario.value === 'lowPeers' ? 3 : 8,
        totalPeers: mockScenario.value === 'lowPeers' ? 8 : 53
      },
      blockchainInfo: {
        blocks: 875432,
        headers: mockScenario.value === 'outOfSync' ? 875532 : 875432,
        chain: 'main',
        verificationprogress: mockScenario.value === 'outOfSync' ? 0.9987 : 0.999998,
        difficulty: 103919634711492.2,
        medianBlockSize: 0,
      },
      nodeInfo: { 
        version: '270000', 
        protocolversion: '70016', 
        subversion: '/Satoshi:27.0.0/' 
      },
      upTime: '15d 7h 23m',
      inboundPeer: range(mockScenario.value === 'lowPeers' ? 5 : 45).map((i: number) => ({
        id: i,
        addr: `192.168.1.${i + 10}:8333`,
        subver: i % 3 === 0 ? '/Satoshi:27.0.0/' : i % 3 === 1 ? '/Satoshi:26.0.0/' : '/Satoshi:25.0.0/',
        version: 270000,
        timeoffset: 0,
        conntime: now - (3600 * (i + 1)),
        network: 'ipv4',
        connection_type: 'inbound',
        minping: 0.05 + (i * 0.01),
        bytesrecv: 1024000 + (i * 5000),
        bytessent: 512000 + (i * 2500),
      })),
      outboundPeer: range(mockScenario.value === 'lowPeers' ? 3 : 8).map((i: number) => ({
        id: i + 100,
        addr: `172.16.0.${i + 10}:8333`,
        subver: '/Satoshi:27.0.0/',
        version: 270000,
        timeoffset: 0,
        conntime: now - (7200 * (i + 1)),
        network: 'ipv4',
        connection_type: 'outbound-full-relay',
        minping: 0.08 + (i * 0.02),
        bytesrecv: 2048000 + (i * 10000),
        bytessent: 1024000 + (i * 5000),
      })),
      subverDistribution: {
        inbound: [
          { server: '/Satoshi:27.0.0/', count: 27, percentage: 60 },
          { server: '/Satoshi:26.0.0/', count: 13, percentage: 30 },
          { server: '/Satoshi:25.0.0/', count: 5, percentage: 10 },
        ],
        outbound: [
          { server: '/Satoshi:27.0.0/', count: mockScenario.value === 'lowPeers' ? 3 : 8, percentage: 100 },
        ],
      },
      block: { 
        time: mockScenario.value === 'outOfSync' ? now - 3600 : now - 120,
        nTx: 2834,
        hash: '00000000000000000002a7c4c1e48d76c5a37902165a270156b7a8d72728a054'
      },
      rpcConnected: mockScenario.value !== 'disconnected',
    };
  };

  /**
   * Gets mock connection state based on current scenario.
   */
  const getMockConnectionState = () => ({
    isConnected: mockScenario.value !== 'disconnected',
    rpcConnected: mockScenario.value !== 'disconnected',
    errorMessage: mockScenario.value === 'disconnected' 
      ? 'WebSocket connection lost. Attempting to reconnect...' 
      : null
  });

  /**
   * Starts auto-cycling through scenarios at specified interval.
   * @param intervalMs - Milliseconds between scenario changes (default: 8000)
   */
  const startAutoCycle = (intervalMs = 8000): number => {
    return window.setInterval(cycleMockScenario, intervalMs);
  };

  return {
    MOCK_MODE,
    mockScenario,
    cycleMockScenario,
    generateMockData,
    getMockConnectionState,
    startAutoCycle,
  };
}
