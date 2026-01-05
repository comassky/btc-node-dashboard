import { defineStore } from 'pinia';
import { reactive, ref } from 'vue';
import type { DashboardData, DashboardConfig } from '@types';
import { useWebSocket } from '@composables/useWebSocket';
import { deepMerge } from '@utils/deepMerge';
import { setMinOutboundPeers } from '@utils/nodeHealth';
import ky from 'ky';

const WS_URL = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/ws/dashboard`;

const DEFAULT_DATA: DashboardData = {
  generalStats: { inboundCount: 0, outboundCount: 0, totalPeers: 0 },
  blockchainInfoResponse: {
    blocks: 0,
    headers: 0,
    chain: 'Loading...',
    verificationprogress: 0,
    difficulty: 0,
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
    version: 0,
    protocolversion: 0,
    subversion: 'N/A',
    localservices: '',
    localservicesnames: [],
    localrelay: false,
    timeoffset: 0,
    connections: 0,
    networkactive: false,
    networks: [],
    localaddresses: [],
  },
  upTime: 0,
  inboundPeer: [],
  outboundPeer: [],
  subverDistribution: { inbound: [], outbound: [] },
  block: { time: 0, nTx: 0 },
  rpcConnected: false,
  mempoolInfo: {
    loaded: false,
    size: 0,
    bytes: 0,
    usage: 0,
    maxmempool: 0,
    mempoolminfee: 0,
    minrelaytxfee: 0,
    unbroadcastcount: 0,
    total_fee: 0,
  },
};

export const useDashboardStore = defineStore('dashboard', () => {
  // --- State ---
  const dataState = reactive<DashboardData>(deepMerge({}, DEFAULT_DATA));
  const configLoaded = ref(false);
  const disableMempool = ref(false);

  // --- WebSocket Connection ---
  const onDataReceived = (rawData: Partial<DashboardData>) => {
    deepMerge(dataState, rawData);
  };
  const { isConnected, rpcConnected, errorMessage, isRetrying, connect, disconnect } = useWebSocket(
    WS_URL,
    onDataReceived
  );

  // --- Actions ---
  const loadConfig = async () => {
    if (configLoaded.value) return; // Prevent double loading
    
    try {
      const config: DashboardConfig = await ky.get('/api/config').json();
      setMinOutboundPeers(config.minOutboundPeers);
      disableMempool.value = !!config.disableMempool;
      configLoaded.value = true;
    } catch (error) {
      console.error('Failed to load dashboard configuration:', error);
      configLoaded.value = true; // Continue with default values
    }
  };

  const initialize = async () => {
    await loadConfig();
    connect();
  };

  return {
    // State
    dataState,
    configLoaded,
    disableMempool,
    isConnected,
    rpcConnected,
    errorMessage,
    isRetrying,

    // Actions
    initialize,
    disconnect,
  };
});
