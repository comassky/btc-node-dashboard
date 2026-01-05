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
    const nextScenario = scenarios[(currentIndex + 1) % scenarios.length];
    if (nextScenario) {
      mockScenario.value = nextScenario;
    }
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
        networks: [
          {
            name: 'ipv4',
            limited: false,
            reachable: true,
            proxy: '',
            proxy_randomize_credentials: false,
          },
          {
            name: 'ipv6',
            limited: false,
            reachable: true,
            proxy: '',
            proxy_randomize_credentials: false,
          },
          {
            name: 'onion',
            limited: false,
            reachable: true,
            proxy: '127.0.0.1:9050',
            proxy_randomize_credentials: true,
          },
          {
            name: 'i2p',
            limited: false,
            reachable: false,
            proxy: '',
            proxy_randomize_credentials: false,
          },
        ],
        localaddresses: [
          { address: '2a01:e0a:123:4567:89ab:cdef:1234:5678', port: 8333, score: 5 },
          { address: 'abcdefghijklmnop.onion', port: 8333, score: 4 },
        ],
      },
      upTime: 15 * 24 * 3600 + 7 * 3600 + 23 * 60, // 15 days, 7 hours, 23 minutes in seconds
      inboundPeer: Array.from({ length: mockScenario.value === 'lowPeers' ? 5 : 45 }, (_, i) => {
        // Pseudo-random but consistent based on index
        const seed = (i * 17 + 13) % 100;
        const randomMinutes = (seed * 7) % 45;
        const randomHours = Math.floor((seed * 3) % 72);
        const randomDays = Math.floor((seed * 2) % 7);
        const connOffset = randomDays * 86400 + randomHours * 3600 + randomMinutes * 60 + seed * 10;
        
        const timeoffset = ((seed * 17) % 11) - 5;
        
        return {
          id: i,
          addr: `192.168.1.${i + 10}:8333`,
          subver:
            i % 3 === 0 ? '/Satoshi:27.0.0/' : i % 3 === 1 ? '/Satoshi:26.0.0/' : '/Satoshi:25.0.0/',
          version: 270000,
          timeoffset,
          conntime: now - connOffset,
          network: i % 7 === 0 ? 'onion' : i % 11 === 0 ? 'ipv6' : 'ipv4',
          connection_type: 'inbound',
          minping: 0.02 + (seed % 30) * 0.01,
          bytesrecv: 512000 + seed * 12345 + i * 8765,
          bytessent: 256000 + seed * 6789 + i * 4321,
        };
      }),
      outboundPeer: Array.from({ length: mockScenario.value === 'lowPeers' ? 3 : 8 }, (_, i) => {
        const seed = (i * 23 + 19) % 100;
        const randomMinutes = (seed * 11) % 55;
        const randomHours = Math.floor((seed * 5) % 96);
        const randomDays = Math.floor((seed * 3) % 14);
        const connOffset = randomDays * 86400 + randomHours * 3600 + randomMinutes * 60 + seed * 15;
        const timeoffset = ((seed * 13) % 11) - 5;
        
        return {
          id: i + 100,
          addr: `172.16.0.${i + 10}:8333`,
          subver: i % 4 === 0 ? '/Satoshi:26.0.0/' : '/Satoshi:27.0.0/',
          version: 270000,
          timeoffset,
          conntime: now - connOffset,
          network: i % 5 === 0 ? 'onion' : i % 7 === 0 ? 'ipv6' : 'ipv4',
          connection_type: 'outbound-full-relay',
          minping: 0.03 + (seed % 25) * 0.015,
          bytesrecv: 1024000 + seed * 23456 + i * 15432,
          bytessent: 768000 + seed * 13579 + i * 9876,
        };
      }),
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
