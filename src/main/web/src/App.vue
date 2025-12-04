<script setup lang="ts">
import { ref, onMounted, reactive, computed, onBeforeUnmount } from 'vue';
import Status from './components/Status.vue';
import PeerDistributionChart from './components/PeerDistributionChart.vue';
import PeerTable from './components/PeerTable.vue';
import { BlockChainInfo, NodeInfo, Peer, type DashboardData } from './types';
import { formatTimeSince } from './utils/formatting';

// Constants
const WS_RECONNECT_DELAY = 3000;
const WS_URL = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/ws/dashboard`;

// Optimized initial state
const DEFAULT_DATA: DashboardData = {
  generalStats: { inboundCount: 0, outboundCount: 0, totalPeers: 0 },
  blockchainInfo: {
    blocks: 0,
    headers: 0,
    chain: 'Loading...',
    verificationprogress: 0,
    difficulty: 0,
    medianBlockSize: 0,
  },
  nodeInfo: { version: 'N/A', protocolVersion: 'N/A', subversion: 'N/A' },
  upTime: 'N/A',
  inboundPeer: [],
  outboundPeer: [],
  subverDistribution: { inbound: [], outbound: [] },
  block: { time: 0, ntx: 0 },
  rpcConnected: false,
};

// Reactive state
const isConnected = ref(false);
const rpcConnected = ref(false);
const errorMessage = ref<string | null>(null);
const isDarkMode = ref(true);
const dataState = reactive<DashboardData>(DEFAULT_DATA);

let ws: WebSocket | null = null;
let reconnectTimeout: ReturnType<typeof setTimeout> | null = null;

// Computed properties
const inboundPeers = computed(() => dataState.inboundPeer);
const outboundPeers = computed(() => dataState.outboundPeer);
const subverInbound = computed(() => dataState.subverDistribution.inbound);
const subverOutbound = computed(() => dataState.subverDistribution.outbound);
const inboundCount = computed(() => dataState.generalStats.inboundCount);
const outboundCount = computed(() => dataState.generalStats.outboundCount);
const cleanedSubversion = computed(() => {
  const subver = dataState.nodeInfo.subversion;
  return (!subver || subver === 'N/A') ? 'N/A' : subver.replace(/^\/|\/$/g, '').trim();
});

// Theme management
const updateErrorPulseRgb = () => {
  const errorColor = isDarkMode.value ? '239, 71, 111' : '239, 68, 68';
  document.documentElement.style.setProperty('--status-error-rgb', errorColor);
};

const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value;
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light');
  document.documentElement.classList.toggle('dark', isDarkMode.value);
  updateErrorPulseRgb();
};

const loadTheme = () => {
  const savedTheme = localStorage.getItem('theme');
  isDarkMode.value = savedTheme 
    ? savedTheme === 'dark' 
    : window.matchMedia('(prefers-color-scheme: dark)').matches;
  
  document.documentElement.classList.toggle('dark', isDarkMode.value);
  updateErrorPulseRgb();
};

// WebSocket
const normalizeData = (rawData: Partial<DashboardData>) => {
  const nodeInfo = rawData.nodeInfo as NodeInfo|| {};
  const blockchainInfo= rawData.blockchainInfo as BlockChainInfo|| {};

  Object.assign(dataState, {
    generalStats: rawData.generalStats || dataState.generalStats,
    blockchainInfo: {
      blocks: blockchainInfo.blocks ?? 0,
      headers: blockchainInfo.headers ?? 0,
      chain: blockchainInfo.chain || 'N/A',
      verificationprogress: blockchainInfo.verificationprogress ?? 0,
      difficulty: blockchainInfo.difficulty ?? 0,
      medianBlockSize: blockchainInfo.medianBlockSize ?? 0,
    },
    nodeInfo: {
      version: nodeInfo.version || 'N/A',
      protocolVersion: nodeInfo.protocolVersion || 'N/A',
      subversion: nodeInfo.subversion || 'N/A',
    },
    upTime: rawData.upTime || 'N/A',
    inboundPeer: rawData.inboundPeer || [],
    outboundPeer: rawData.outboundPeer || [],
    subverDistribution: rawData.subverDistribution || { inbound: [], outbound: [] },
    block: rawData.block || { time: 0, ntx: 0 },
  });
};

const scheduleReconnect = () => {
  if (reconnectTimeout) clearTimeout(reconnectTimeout);
  reconnectTimeout = setTimeout(connectWebSocket, WS_RECONNECT_DELAY);
};

const connectWebSocket = () => {
  if (ws) {
    ws.onclose = null;
    ws.close();
  }

  ws = new WebSocket(WS_URL);

  ws.onopen = () => {
    isConnected.value = true;
    errorMessage.value = null;
  };

  ws.onmessage = (event) => {
    try {
      const json = JSON.parse(event.data) as Partial<DashboardData>;

      if ('rpcConnected' in json) {
        rpcConnected.value = json.rpcConnected ?? false;
        errorMessage.value = json.errorMessage || null;
        return;
      }

      if ('generalStats' in json) {
        rpcConnected.value = true;
        errorMessage.value = null;
        normalizeData(json);
      }
    } catch (e) {
      console.error('WebSocket JSON parse error:', e);
    }
  };

  ws.onclose = () => {
    isConnected.value = false;
    rpcConnected.value = false;
    errorMessage.value = 'WebSocket disconnected. Retrying...';
    scheduleReconnect();
  };

  ws.onerror = () => {
    isConnected.value = false;
    rpcConnected.value = false;
    errorMessage.value = 'WebSocket connection error.';
  };
};

onMounted(() => {
  loadTheme();
  connectWebSocket();
});

onBeforeUnmount(() => {
  if (reconnectTimeout) clearTimeout(reconnectTimeout);
  if (ws) {
    ws.onclose = null;
    ws.close();
  }
});
</script>

<template>
    <div class="p-2 md:p-6 bg-bg-app min-h-screen">
        <button @click="toggleDarkMode"
            class="fixed top-4 right-4 z-50 p-3 rounded-full bg-bg-card border border-border-strong text-text-primary text-xl shadow-lg hover:bg-border-strong/50 transition-all duration-200"
            title="Toggle Dark/Light Mode">
            <font-awesome-icon :icon="isDarkMode ? ['fas', 'sun'] : ['fas', 'moon']" />
        </button>

        <div class="flex justify-center mb-12 mt-4 sm:mt-0">
<h1 class="text-accent text-3xl md:text-5xl font-extralight tracking-widest uppercase text-center">
    <font-awesome-icon :icon="['fab', 'bitcoin']" class="mr-2" /> Bitcoin Node Dashboard
</h1>
        </div>

        <Status
            :isConnected="isConnected"
            :rpcConnected="rpcConnected"
            :errorMessage="errorMessage"
        />

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">

            <div class="lg:col-span-2 mb-5" v-if="rpcConnected">
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">

                    <div
                            class="stat-card bg-bg-card p-6 rounded-xl shadow-lg border-l-4 border-status-success hover:shadow-2xl hover:border-accent transition duration-300 transform hover:-translate-y-0.5 flex flex-col gap-3">
                        <div class="flex justify-between items-center">
                            <div class="icon text-3xl text-status-success"><font-awesome-icon :icon="['fas', 'user-friends']" /></div>
                            <div class="label text-xs uppercase text-text-secondary font-medium">Total Peers</div>
                        </div>
                        <div class="value text-5xl font-light text-text-primary">{{ dataState.generalStats.totalPeers }}</div>
                        <div class="detail mt-2 pt-2 border-t border-border-strong text-sm text-text-secondary font-light">
                            <p class="mb-1"><font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-1" /> Inbound: {{ dataState.generalStats.inboundCount }}</p>
                            <p class="mb-1"><font-awesome-icon :icon="['fas', 'sign-out-alt']" class="mr-1" /> Outbound: {{ dataState.generalStats.outboundCount }}</p>
                        </div>
                    </div>

                    <div
                            class="stat-card bg-bg-card p-6 rounded-xl shadow-lg border-l-4 border-status-warning hover:shadow-2xl hover:border-accent transition duration-300 transform hover:-translate-y-0.5 flex flex-col gap-3">
                        <div class="flex justify-between items-center">
                            <div class="icon text-3xl text-status-warning"><font-awesome-icon :icon="['fas', 'cubes']" /></div>
                            <div class="label text-xs uppercase text-text-secondary font-medium">Current Block</div>
                        </div>
                        <div class="value text-5xl font-light text-text-primary"
                             :title="'Current height of the ' + dataState.blockchainInfo.chain + ' blockchain'">{{ dataState.blockchainInfo.blocks }}
                        </div>

                        <div class="detail mt-2 pt-2 border-t border-border-strong text-sm text-text-secondary font-light">
                            <p class="mb-1"><font-awesome-icon :icon="['fas', 'list-ol']" class="mr-1" /> Headers: <span
                                    class="font-bold text-text-primary">{{ dataState.blockchainInfo.headers }}</span></p>
                            <p class="mb-1"><font-awesome-icon :icon="['far', 'clock']" class="mr-1" /> Time: <span
                                    class="font-bold text-text-primary">{{ formatTimeSince(dataState.block.time) }} ago</span></p>
                            <p class="mb-1"><font-awesome-icon :icon="['fas', 'exchange-alt']" class="mr-1" /> Tx Count: <span
                                    class="font-bold text-text-primary">{{ dataState.block.ntx }}</span></p>
                        </div>
                    </div>

                    <div
                            class="stat-card bg-bg-card p-6 rounded-xl shadow-lg border-l-4 border-accent hover:shadow-2xl hover:border-accent transition duration-300 transform hover:-translate-y-0.5 flex flex-col gap-3 lg:col-span-2">
                        <div class="flex justify-between items-center">
                            <div class="icon text-3xl text-accent"><font-awesome-icon :icon="['fas', 'hard-hat']" /></div>
                            <div class="label text-xs uppercase text-text-secondary font-medium">Node Details</div>
                        </div>

                        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-end mt-2">
                            <div class="min-w-0 flex-shrink">
                                <div class="value text-2xl font-bold text-text-primary truncate"
                                    :title="cleanedSubversion">{{ cleanedSubversion }}</div>
                                <div class="detail text-sm text-text-secondary font-light">Protocol v{{ dataState.nodeInfo.protocolVersion }}
                                </div>
                            </div>

                            <div class="text-left sm:text-right mt-3 sm:mt-0 flex-shrink-0">
                                <div class="value text-2xl font-bold text-text-primary"
                                    title="Time elapsed since node started">{{ dataState.upTime }}</div>
                                <div class="detail text-sm text-text-secondary font-light">Uptime</div>
                            </div>
                        </div>
                        <div class="detail mt-3 pt-3 border-t border-border-strong text-sm text-text-secondary">
                            <p class="mb-1">
                                <font-awesome-icon :icon="['fas', 'shield-alt']" class="mr-1 text-status-success" /> Verification Progress:
                                <span class="font-bold text-text-primary ml-1">{{ (dataState.blockchainInfo.verificationprogress *
                                        100).toFixed(4) }}%</span>
                            </p>
                        </div>
                    </div>
                </div>
            </div>


            <div class="bg-bg-card p-6 rounded-xl shadow-2xl lg:col-span-2" v-else-if="isConnected && !rpcConnected">
                <p class="text-center text-text-secondary text-lg">
                    <font-awesome-icon :icon="['fas', 'spinner']" spin class="mr-2" /> Connecting to node RPC...
                </p>
            </div>


            <div class="data-section bg-bg-card p-6 rounded-xl shadow-2xl lg:col-span-2" v-if="rpcConnected">
                <h2 class="text-2xl font-medium mb-6">
                    <font-awesome-icon :icon="['fas', 'chart-pie']" class="mr-2 text-accent" /> Peer Software Distribution
                </h2>

                <div class="flex flex-col md:flex-row gap-8 mt-4">

                    <PeerDistributionChart
                        :peers="subverInbound"
                        type="inbound"
                        :count="inboundCount"
                        :isDarkMode="isDarkMode"
                    />

                    <PeerDistributionChart
                        :peers="subverOutbound"
                        type="outbound"
                        :count="outboundCount"
                        :isDarkMode="isDarkMode"
                    />
                </div>
            </div>

            <div class="data-section bg-bg-card p-6 rounded-xl shadow-2xl lg:col-span-2" v-if="rpcConnected">
                <h2 class="text-2xl font-medium mb-6">
                    <font-awesome-icon :icon="['fas', 'table']" class="mr-2 text-accent" /> Connection Details
                </h2>

                <PeerTable
                    :peers="inboundPeers as Peer[]"
                    type="inbound"
                />

                <PeerTable
                    :peers="outboundPeers as Peer[]"
                    type="outbound"
                />
            </div>
        </div>
    </div>
</template>