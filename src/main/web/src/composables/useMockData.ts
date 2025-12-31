import { ref } from 'vue';

/**
 * Type representing the available mock scenarios for dashboard simulation.
 */
export type MockScenario = 'normal' | 'disconnected' | 'lowPeers' | 'outOfSync';

import type { DashboardData } from '@types';

/**
 * Represents the available mock scenarios for dashboard simulation.
 * - 'normal': Healthy node
 * - 'disconnected': Simulates lost connection
 * - 'lowPeers': Simulates low peer count
 * - 'outOfSync': Simulates node out of sync
 */

/**
 * Composable for generating mock dashboard data and simulating node states for development/testing.
 * Provides utilities to cycle scenarios, generate mock data, and simulate connection states.
 *
 * Enable mock mode via:
 * - Setting VITE_MOCK_MODE=true in .env file
 * - Or modify MOCK_MODE.value directly in code
 *
 * @returns {Object} Mock data utilities and state
 */
export function useMockData() {
  /**
   * Indicates if mock mode is enabled (from VITE_MOCK_MODE env variable or manually).
   */
  const MOCK_MODE = ref(import.meta.env.VITE_MOCK_MODE === 'true' || false);

  /**
   * Current mock scenario in use.
   */
  const mockScenario = ref<MockScenario>('normal');

  /**
   * Cycles through available mock scenarios in sequence.
   * Useful for quickly testing all dashboard states.
   *
   * @returns {void}
   */
  const cycleMockScenario = (): void => {
    const scenarios: MockScenario[] = ['normal', 'disconnected', 'lowPeers', 'outOfSync'];
    const currentIndex = scenarios.indexOf(mockScenario.value);
    mockScenario.value = scenarios[(currentIndex + 1) % scenarios.length];
  };

  /**
   * Generates mock dashboard data based on the current scenario.
   *
   * Scenarios:
   * - 'normal': Healthy node with good peer count and sync status
   * - 'disconnected': Simulates WebSocket/RPC connection loss
   * - 'lowPeers': Low outbound peer count (triggers warning)
   * - 'outOfSync': Node out of sync with network (old block, headers ahead)
   *
   * @returns {DashboardData} Simulated dashboard data for the current scenario
   */
  const generateMockData = (): DashboardData => {
    const now = Math.floor(Date.now() / 1000);

    // Values for the outOfSync scenario
    const isOutOfSync = mockScenario.value === 'outOfSync';
    const blocks = isOutOfSync ? 875432 : 875432;
    const headers = isOutOfSync ? 875450 : 875432; // diff = 18 > 2
    const blockTime = isOutOfSync ? now - 4000 : now - 120; // 4000s > 1h
    const verificationprogress = isOutOfSync ? 0.9987 : 0.999998;

    return {
      generalStats: {
        inboundCount: mockScenario.value === 'lowPeers' ? 5 : 45,
        outboundCount: mockScenario.value === 'lowPeers' ? 3 : 8,
        totalPeers: mockScenario.value === 'lowPeers' ? 8 : 53,
      },
      blockchainInfoResponse: {
        blocks,
        headers,
        chain: 'main',
        verificationprogress,
        difficulty: 103919634711492.2,
        bestblockhash: '',
        time: 0,
        mediantime: 0,
        initialblockdownload: false,
        chainwork: '',
        size_on_disk: 0,
        pruned: false,
        pruneheight: null,
      },
      nodeInfo: {
        version: 270000,
        protocolversion: 70016,
        subversion: '/Satoshi:27.0.0/',
        localservices: '',
        localservicesnames: [],
        localrelay: false,
        timeoffset: 0,
        connections: 0,
        networkactive: false,
        networks: [],
        localaddresses: [],
      },
      upTime: 15 * 24 * 3600 + 7 * 3600 + 23 * 60, // 15 days, 7 hours, 23 minutes in seconds
      inboundPeer: Array.from({ length: mockScenario.value === 'lowPeers' ? 5 : 45 }, (_, i) => ({
        id: i,
        addr: `192.168.1.${i + 10}:8333`,
        subver:
          i % 3 === 0 ? '/Satoshi:27.0.0/' : i % 3 === 1 ? '/Satoshi:26.0.0/' : '/Satoshi:25.0.0/',
        version: 270000,
        timeoffset: 0,
        conntime: now - 3600 * (i + 1),
        network: 'ipv4',
        connection_type: 'inbound',
        minping: 0.05 + i * 0.01,
        bytesrecv: 1024000 + i * 5000,
        bytessent: 512000 + i * 2500,
      })),
      outboundPeer: Array.from({ length: mockScenario.value === 'lowPeers' ? 3 : 8 }, (_, i) => ({
        id: i + 100,
        addr: `172.16.0.${i + 10}:8333`,
        subver: '/Satoshi:27.0.0/',
        version: 270000,
        timeoffset: 0,
        conntime: now - 7200 * (i + 1),
        network: 'ipv4',
        connection_type: 'outbound-full-relay',
        minping: 0.08 + i * 0.02,
        bytesrecv: 2048000 + i * 10000,
        bytessent: 1024000 + i * 5000,
      })),
      subverDistribution: {
        inbound: [
          { server: '/Satoshi:27.0.0/', count: 27, percentage: 60 },
          { server: '/Satoshi:26.0.0/', count: 13, percentage: 30 },
          { server: '/Satoshi:25.0.0/', count: 5, percentage: 10 },
        ],
        outbound: [
          {
            server: '/Satoshi:27.0.0/',
            count: mockScenario.value === 'lowPeers' ? 3 : 8,
            percentage: 100,
          },
        ],
      },
      block: {
        time: blockTime,
        nTx: 2834,
        hash: '00000000000000000002a7c4c1e48d76c5a37902165a270156b7a8d72728a054',
      },
      rpcConnected: mockScenario.value !== 'disconnected',
      mempoolInfo: {
        loaded: true,
        size: 1234,
        bytes: 567890,
        usage: 234567,
        maxmempool: 300000000,
        mempoolminfee: 0.00001,
        minrelaytxfee: 0.00001,
        unbroadcastcount: 2,
        total_fee: 0.123456,
      },
    };
  };

  /**
   * Gets mock connection state based on current scenario.
   *
   * @returns {Object} Connection state with isConnected, rpcConnected, and errorMessage fields
   */
  const getMockConnectionState = () => ({
    isConnected: mockScenario.value !== 'disconnected',
    rpcConnected: mockScenario.value !== 'disconnected',
    errorMessage:
      mockScenario.value === 'disconnected'
        ? 'WebSocket connection lost. Attempting to reconnect...'
        : null,
  });

  /**
   * Starts auto-cycling through scenarios at a specified interval.
   *
   * @param intervalMs Milliseconds between scenario changes (default: 8000)
   * @returns {number} Interval ID (can be used to clearInterval)
   */
  const startAutoCycle = (intervalMs = 8000): number => {
    return window.setInterval(cycleMockScenario, intervalMs);
  };

  return {
    /**
     * Reactive ref indicating if mock mode is enabled.
     */
    MOCK_MODE,
    /**
     * Reactive ref for the current mock scenario.
     */
    mockScenario,
    /**
     * Function to cycle through mock scenarios.
     */
    cycleMockScenario,
    /**
     * Function to generate mock dashboard data for the current scenario.
     */
    generateMockData,
    /**
     * Function to get mock connection state for the current scenario.
     */
    getMockConnectionState,
    /**
     * Function to start auto-cycling scenarios at a given interval.
     */
    startAutoCycle,
  };
}
