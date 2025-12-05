<script setup lang="ts">
import { reactive, computed, onBeforeUnmount } from 'vue';
import Status from './components/Status.vue';
import PeersCard from './components/PeersCard.vue';
import BlockCard from './components/BlockCard.vue';
import NodeCard from './components/NodeCard.vue';
import PeerDistributionChart from './components/PeerDistributionChart.vue';
import PeerTable from './components/PeerTable.vue';
import Footer from './components/Footer.vue';
import { BlockChainInfo, NodeInfo, Peer, type DashboardData } from './types';
import { useWebSocket } from './composables/useWebSocket';
import { useTheme } from './composables/useTheme';

const WS_URL = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/ws/dashboard`;

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
  nodeInfo: { version: 'N/A', protocolversion: 'N/A', subversion: 'N/A' },
  upTime: 'N/A',
  inboundPeer: [],
  outboundPeer: [],
  subverDistribution: { inbound: [], outbound: [] },
  block: { time: 0, nTx: 0 },
  rpcConnected: false,
};

const dataState = reactive<DashboardData>(DEFAULT_DATA);

const inboundPeers = computed(() => dataState.inboundPeer);
const outboundPeers = computed(() => dataState.outboundPeer);
const subverInbound = computed(() => dataState.subverDistribution.inbound);
const subverOutbound = computed(() => dataState.subverDistribution.outbound);
const inboundCount = computed(() => dataState.generalStats.inboundCount);
const outboundCount = computed(() => dataState.generalStats.outboundCount);

const { isDarkMode, toggleDarkMode } = useTheme();

const normalizeData = (rawData: Partial<DashboardData>) => {
  const nodeInfo = rawData.nodeInfo as NodeInfo || {};
  const blockchainInfo = rawData.blockchainInfo as BlockChainInfo || {};

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
      protocolversion: nodeInfo.protocolversion || 'N/A',
      subversion: nodeInfo.subversion || 'N/A',
    },
    upTime: rawData.upTime || 'N/A',
    inboundPeer: rawData.inboundPeer || [],
    outboundPeer: rawData.outboundPeer || [],
    subverDistribution: rawData.subverDistribution || { inbound: [], outbound: [] },
    block: rawData.block || { time: 0, nTx: 0 },
  });
};

const { isConnected, rpcConnected, errorMessage, connect, disconnect } = useWebSocket(WS_URL, normalizeData);

connect();

onBeforeUnmount(() => {
  disconnect();
});
</script>

<template>
    <div class="p-3 sm:p-4 md:p-6 bg-bg-app min-h-screen">
        <button @click="toggleDarkMode"
            class="fixed top-3 right-3 sm:top-4 sm:right-4 z-50 p-2.5 sm:p-3 rounded-full bg-bg-card border border-border-strong shadow-lg hover:bg-border-strong/50 transition-all"
            title="Toggle Dark/Light Mode">
            <font-awesome-icon :icon="isDarkMode ? ['fas', 'sun'] : ['fas', 'moon']" />
        </button>

        <div class="mb-6 sm:mb-8 md:mb-12 mt-12 sm:mt-4 text-center">
            <h1 class="text-accent text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-extralight tracking-wide sm:tracking-widest uppercase px-2">
                <font-awesome-icon :icon="['fab', 'bitcoin']" class="mr-1 sm:mr-2" /> 
                <span class="hidden sm:inline">Bitcoin Node Dashboard</span>
                <span class="sm:hidden">BTC Dashboard</span>
            </h1>
        </div>

        <Status
            :isConnected="isConnected"
            :rpcConnected="rpcConnected"
            :errorMessage="errorMessage"
        />

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6 md:gap-8">

            <div class="lg:col-span-2 mb-3 sm:mb-5" v-if="rpcConnected">
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">

                    <PeersCard :stats="dataState.generalStats" />

                    <BlockCard 
                        :blockchain="dataState.blockchainInfo"
                        :block="dataState.block"
                    />

                    <NodeCard 
                        :node="dataState.nodeInfo"
                        :blockchain="dataState.blockchainInfo"
                        :upTime="dataState.upTime"
                    />
                </div>
            </div>


            <div class="bg-bg-card p-6 rounded-xl shadow-2xl lg:col-span-2" v-else-if="isConnected && !rpcConnected">
                <p class="text-center text-text-secondary">
                    <font-awesome-icon :icon="['fas', 'spinner']" spin class="mr-2" /> Connecting to node RPC...
                </p>
            </div>


            <div class="bg-bg-card p-4 sm:p-6 rounded-xl shadow-2xl lg:col-span-2" v-if="rpcConnected">
                <h2 class="text-xl sm:text-2xl font-medium mb-4 sm:mb-6">
                    <font-awesome-icon :icon="['fas', 'chart-pie']" class="mr-2 text-accent" /> 
                    <span class="hidden sm:inline">Peer Software Distribution</span>
                    <span class="sm:hidden">Peers Distribution</span>
                </h2>

                <div class="flex flex-col md:flex-row gap-6 sm:gap-8 mt-3 sm:mt-4">

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

            <div class="bg-bg-card p-4 sm:p-6 rounded-xl shadow-2xl lg:col-span-2" v-if="rpcConnected">
                <h2 class="text-xl sm:text-2xl font-medium mb-4 sm:mb-6">
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

        <Footer />
    </div>
</template>