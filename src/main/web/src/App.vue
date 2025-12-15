<script setup lang="ts">
import type { DashboardData, DashboardConfig, NetworkInfoResponse, BlockChainInfo, Peer } from '@types';
import Spinner from '@components/Spinner.vue';
import { reactive, computed, onBeforeUnmount, ref, onMounted, nextTick } from 'vue';
import Status from '@components/Status.vue';
import PeersCard from '@components/PeersCard.vue';
import BlockCard from '@components/BlockCard.vue';
import NodeCard from '@components/NodeCard.vue';
import PeerDistributionChart from '@components/PeerDistributionChart.vue';
import MempoolInfoCard from '@components/MempoolInfoCard.vue';
import PeerTable from '@components/PeerTable.vue';
import Footer from '@components/Footer.vue';
import { useWebSocket } from '@composables/useWebSocket';
import { useTheme } from '@composables/useTheme';
import { useMockData } from '@composables/useMockData';
import { setMinOutboundPeers } from '@utils/nodeHealth';

/**
 * Main Dashboard Application Component
 * Manages WebSocket connection, data state, and theme for the Bitcoin node dashboard.
 */

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
    localaddresses: []
  },
  upTime: 'N/A',
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
    total_fee: 0
  },
};

const dataState = reactive<DashboardData>(DEFAULT_DATA);

const configLoaded = ref(false);
const disableMempool = ref(false);


const inboundPeers = computed(() => dataState.inboundPeer);
const outboundPeers = computed(() => dataState.outboundPeer);
const subverInbound = computed(() => dataState.subverDistribution.inbound);
const subverOutbound = computed(() => dataState.subverDistribution.outbound);
const inboundCount = computed(() => dataState.generalStats.inboundCount);
const outboundCount = computed(() => dataState.generalStats.outboundCount);

const { isDarkMode, toggleDarkMode } = useTheme();

// Mock data composable for testing error/warning states
const { 
  MOCK_MODE, 
  mockScenario, 
  cycleMockScenario, 
  generateMockData, 
  getMockConnectionState,
  startAutoCycle 
} = useMockData();

// Auto-cycle interval id for mock mode (cleared on unmount)
const autoCycleId = ref<number | null>(null);

// Load configuration from backend

const loadConfig = async () => {
  try {
    const response = await fetch('/api/config');
    if (response.ok) {
      const config: DashboardConfig = await response.json();
      setMinOutboundPeers(config.minOutboundPeers);
      disableMempool.value = !!config.disableMempool;
      configLoaded.value = true;
    }
  } catch (error) {
    console.error('Failed to load dashboard configuration:', error);
    configLoaded.value = true; // Continue with default values
  }
};

onMounted(async () => {
  await loadConfig();
  
  // Apply mock data in dev mode
  if (MOCK_MODE.value) {
    Object.assign(dataState, generateMockData());
    // Auto-cycle scenarios every 8 seconds for demo
    autoCycleId.value = startAutoCycle(8000);
  }
});

// Normalizes incoming WebSocket data and updates reactive state
const normalizeData = (rawData: Partial<DashboardData>) => {
  const nodeInfo = rawData.nodeInfo as NetworkInfoResponse || {};
  const blockchainInfoResponse = rawData.blockchainInfoResponse as BlockChainInfo || {};

  Object.assign(dataState, {
    generalStats: rawData.generalStats || dataState.generalStats,
    blockchainInfoResponse: {
      blocks: blockchainInfoResponse.blocks ?? 0,
      headers: blockchainInfoResponse.headers ?? 0,
      chain: blockchainInfoResponse.chain || 'N/A',
      verificationprogress: blockchainInfoResponse.verificationprogress ?? 0,
      difficulty: blockchainInfoResponse.difficulty ?? 0,
      bestblockhash: blockchainInfoResponse.bestblockhash || '',
      time: blockchainInfoResponse.time ?? 0,
      mediantime: blockchainInfoResponse.mediantime ?? 0,
      initialblockdownload: blockchainInfoResponse.initialblockdownload ?? false,
      chainwork: blockchainInfoResponse.chainwork || '',
      size_on_disk: blockchainInfoResponse.size_on_disk ?? 0,
      pruned: blockchainInfoResponse.pruned ?? false,
      pruneheight: blockchainInfoResponse.pruneheight ?? null,
    },
    nodeInfo: {
      version: nodeInfo.version ?? 0,
      protocolversion: nodeInfo.protocolversion ?? 0,
      subversion: nodeInfo.subversion || 'N/A',
      localservices: nodeInfo.localservices || '',
      localservicesnames: nodeInfo.localservicesnames || [],
      localrelay: nodeInfo.localrelay ?? false,
      timeoffset: nodeInfo.timeoffset ?? 0,
      connections: nodeInfo.connections ?? 0,
      networkactive: nodeInfo.networkactive ?? false,
      networks: nodeInfo.networks || [],
      localaddresses: nodeInfo.localaddresses || [],
    },
    upTime: rawData.upTime || 'N/A',
    inboundPeer: rawData.inboundPeer || [],
    outboundPeer: rawData.outboundPeer || [],
    subverDistribution: rawData.subverDistribution || { inbound: [], outbound: [] },
    block: rawData.block || { time: 0, nTx: 0 },
    mempoolInfo: rawData.mempoolInfo || dataState.mempoolInfo,
  });
};

const { isConnected, rpcConnected, errorMessage, isRetrying, connect, disconnect } = useWebSocket(WS_URL, normalizeData);

if (!MOCK_MODE.value) {
  connect();
}

// Always clean resources on unmount: disconnect websocket and clear mock interval
onBeforeUnmount(() => {
  try { disconnect(); } catch (e) {}
  if (autoCycleId.value) {
    clearInterval(autoCycleId.value);
    autoCycleId.value = null;
  }
});

function handleCycleScenario() {
  cycleMockScenario();
  nextTick(() => {
    Object.assign(dataState, generateMockData());
  });
}
</script>

<template>
    <div class="p-3 sm:p-4 md:p-6 bg-bg-app min-h-screen">
        <button @click="toggleDarkMode"
          class="fixed top-3 right-3 sm:top-4 sm:right-4 z-50 p-2.5 sm:p-3 rounded-full bg-bg-card border border-border-strong shadow-lg hover:bg-border-strong/50 transition-all"
          title="Toggle Dark/Light Mode"
          aria-label="Toggle dark mode"
          :aria-pressed="isDarkMode">
          <font-awesome-icon :icon="isDarkMode ? ['fas', 'sun'] : ['fas', 'moon']" />
        </button>

        <div v-if="MOCK_MODE" class="fixed top-3 left-3 sm:top-4 sm:left-4 z-50 bg-bg-card border border-accent shadow-lg rounded-lg p-3 text-xs">
            <div class="font-bold text-accent mb-2 flex items-center gap-2">
                <font-awesome-icon :icon="['fas', 'hard-hat']" /> MOCK MODE
            </div>
            <button 
                @click="handleCycleScenario"
                class="px-3 py-1.5 bg-accent text-bg-app rounded hover:opacity-80 transition-all font-medium">
                Cycle Scenario
            </button>
            <div class="mt-2 text-text-secondary">
                Current: <span class="font-bold text-text-primary">{{ mockScenario }}</span>
            </div>
        </div>

        <transition name="fade" mode="out-in">
          <div class="mb-6 sm:mb-8 md:mb-12 mt-12 sm:mt-4 text-center" key="header">
            <h1 class="text-accent text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-extralight tracking-wide sm:tracking-widest uppercase px-2">
              <font-awesome-icon :icon="['fab', 'bitcoin']" class="mr-1 sm:mr-2" /> 
              <span class="hidden sm:inline">Bitcoin Node Dashboard</span>
              <span class="sm:hidden">BTC Dashboard</span>
            </h1>
          </div>
        </transition>

        <transition name="fade" mode="out-in">
          <Status
            v-if="MOCK_MODE || isConnected || isRetrying"
            :isConnected="MOCK_MODE ? getMockConnectionState().isConnected : isConnected"
            :rpcConnected="MOCK_MODE ? getMockConnectionState().rpcConnected : rpcConnected"
            :errorMessage="MOCK_MODE ? getMockConnectionState().errorMessage : errorMessage"
            :outboundPeers="dataState.generalStats.outboundCount"
            :blockchain="dataState.blockchainInfoResponse"
            :block="dataState.block"
            :isRetrying="MOCK_MODE ? false : isRetrying"
            key="status"
          />
        </transition>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-2 sm:gap-4 md:gap-6 lg:gap-8 gap-y-4 md:gap-y-6">
          <!-- Cartes principales -->
          <transition name="fade" mode="out-in">
            <template v-if="(MOCK_MODE && dataState.rpcConnected) || rpcConnected">
              <div class="lg:col-span-2" key="cards">
                <div class="grid grid-cols-1 xs:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-2 xs:gap-3 sm:gap-4 md:gap-6">
                  <PeersCard :stats="dataState.generalStats" />
                  <BlockCard :blockchain="dataState.blockchainInfoResponse" :block="dataState.block" />
                  <NodeCard :node="dataState.nodeInfo" :blockchain="dataState.blockchainInfoResponse" :upTime="dataState.upTime" />
                </div>
              </div>
            </template>
            <template v-else-if="isConnected && !rpcConnected">
              <div class="bg-bg-card p-6 rounded-xl shadow-2xl lg:col-span-2 flex flex-col items-center justify-center min-h-[120px]" key="spinner">
                <Spinner />
                <p class="text-center text-text-secondary mt-2">Connecting to node RPC...</p>
              </div>
            </template>
          </transition>

          <!-- Mempool Info -->
          <transition name="fade" mode="out-in">
            <MempoolInfoCard
              v-if="((MOCK_MODE && dataState.rpcConnected) || rpcConnected) && !disableMempool"
              :mempool-info="dataState.mempoolInfo"
              class="lg:col-span-2"
            />
          </transition>

          <!-- Peer Software Distribution -->
          <transition name="fade" mode="out-in">
            <template v-if="(MOCK_MODE && dataState.rpcConnected) || rpcConnected">
              <div class="bg-bg-card p-2 xs:p-3 sm:p-4 md:p-6 rounded-xl shadow-2xl lg:col-span-2">
                <h2 class="text-lg xs:text-xl sm:text-2xl font-medium mb-3 sm:mb-4 md:mb-6 break-words">
                  <font-awesome-icon :icon="['fas', 'chart-pie']" class="mr-2 text-accent" />
                  <span class="hidden sm:inline">Peer Software Distribution</span>
                  <span class="sm:hidden">Peers Distribution</span>
                </h2>
                <div class="flex flex-col md:flex-row gap-4 sm:gap-6 md:gap-8 mt-2 sm:mt-3 md:mt-4">
                  <PeerDistributionChart :peers="subverInbound" type="inbound" :count="inboundCount" :isDarkMode="isDarkMode" />
                  <PeerDistributionChart :peers="subverOutbound" type="outbound" :count="outboundCount" :isDarkMode="isDarkMode" />
                </div>
              </div>
            </template>
          </transition>

          <!-- Peer Table -->
          <transition name="fade" mode="out-in">
            <template v-if="(MOCK_MODE && dataState.rpcConnected) || rpcConnected">
              <div class="bg-bg-card p-2 xs:p-3 sm:p-4 md:p-6 rounded-xl shadow-2xl lg:col-span-2 overflow-x-auto" key="table">
                <h2 class="text-lg xs:text-xl sm:text-2xl font-medium mb-3 sm:mb-4 md:mb-6 break-words">
                  <font-awesome-icon :icon="['fas', 'table']" class="mr-2 text-accent" /> Connection Details
                </h2>
                <PeerTable :peers="inboundPeers as Peer[]" type="inbound" />
                <PeerTable :peers="outboundPeers as Peer[]" type="outbound" />
              </div>
            </template>
          </transition>
        </div>

        <Footer />
    </div>
</template>