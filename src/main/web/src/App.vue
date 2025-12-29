<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted } from 'vue';
import { useDashboardStore } from '@/stores/dashboard';
import { useTheme } from '@composables/useTheme';
import { useMockData } from '@composables/useMockData';
import { storeToRefs } from 'pinia';

// Async Components
const Status = defineAsyncComponent(() => import('@components/Status.vue'));
const PeersCard = defineAsyncComponent(() => import('@components/cards/PeersCard.vue'));
const BlockCard = defineAsyncComponent(() => import('@components/cards/BlockCard.vue'));
const NodeCard = defineAsyncComponent(() => import('@components/cards/NodeCard.vue'));
const PeerDistributionChart = defineAsyncComponent(() => import('@components/PeerDistributionChart.vue'));
const MempoolInfoCard = defineAsyncComponent(() => import('@components/cards/MempoolInfoCard.vue'));
const PeerTable = defineAsyncComponent(() => import('@components/PeerTable.vue'));
const Footer = defineAsyncComponent(() => import('@components/Footer.vue'));
const BaseCardSkeleton = defineAsyncComponent(() => import('@components/cards/BaseCardSkeleton.vue'));

// Store
const dashboardStore = useDashboardStore();
const {
  dataState,
  disableMempool,
  isConnected,
  rpcConnected,
  errorMessage,
  isRetrying,
} = storeToRefs(dashboardStore);

// Derived state from dataState
const inboundPeers = computed(() => dataState.value.inboundPeer);
const outboundPeers = computed(() => dataState.value.outboundPeer);
const subverInbound = computed(() => dataState.value.subverDistribution.inbound);
const subverOutbound = computed(() => dataState.value.subverDistribution.outbound);
const inboundCount = computed(() => dataState.value.generalStats.inboundCount);
const outboundCount = computed(() => dataState.value.generalStats.outboundCount);


// Theme
const { theme, isDarkMode, cycleTheme } = useTheme();

// Mock Data (for development/demo)
const { MOCK_MODE, mockScenario, cycleMockScenario, generateMockData, getMockConnectionState, startAutoCycle } = useMockData();

// Computed properties for cleaner template logic
const shouldShowContent = computed(() => MOCK_MODE.value || rpcConnected.value);

const themeIcons: { [key: string]: string[] } = {
  light: ['fas', 'sun'],
  dark: ['fas', 'moon'],
  gray: ['fas', 'cloud'],
};
const themeIcon = computed(() => themeIcons[theme.value] || themeIcons.gray);

onMounted(() => {
  if (MOCK_MODE.value) {
    Object.assign(dataState.value, generateMockData());
    startAutoCycle(8000);
  } else {
    dashboardStore.initialize();
  }
});

onBeforeUnmount(() => {
  if (!MOCK_MODE.value) {
    dashboardStore.disconnect();
  }
});
</script>

<template>
    <div class="p-3 sm:p-4 md:p-6 bg-bg-app min-h-screen">
        <button @click="cycleTheme"
          class="fixed top-3 right-3 sm:top-4 sm:right-4 z-50 btn btn-secondary"
          title="Cycle theme (light, dark, gray)"
          aria-label="Cycle theme"
          :aria-pressed="theme === 'dark' || theme === 'gray'">
          <font-awesome-icon :icon="themeIcon" />
        </button>

        <div v-if="MOCK_MODE" class="fixed top-3 left-3 sm:top-4 sm:left-4 z-50 dashboard-card border-accent text-xs">
            <div class="font-bold text-accent mb-2 flex items-center gap-2">
                <font-awesome-icon :icon="['fas', 'hard-hat']" /> MOCK MODE
            </div>
            <button 
              @click="cycleMockScenario"
              class="btn btn-accent">
                Cycle Scenario
            </button>
            <div class="mt-2 text-text-secondary">
                Current: <span class="font-bold text-text-primary">{{ mockScenario }}</span>
            </div>
        </div>

        <transition name="fade" mode="out-in">
          <div class="mb-6 sm:mb-8 md:mb-12 mt-12 sm:mt-4 text-center" key="header">
            <h1 class="text-accent text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-extralight tracking-wide sm:tracking-widest uppercase px-2 card-title">
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
          <!-- Main Cards -->
          <transition name="fade" mode="out-in">
            <div v-if="shouldShowContent" class="lg:col-span-2" key="cards">
              <div class="grid grid-cols-1 xs:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-2 xs:gap-3 sm:gap-4 md:gap-6">
                <PeersCard :stats="dataState.generalStats" class="col-span-1" />
                <BlockCard :blockchain="dataState.blockchainInfoResponse" :block="dataState.block" class="col-span-1" />
                <NodeCard :node="dataState.nodeInfo" :blockchain="dataState.blockchainInfoResponse" :upTime="dataState.upTime" class="col-span-1 lg:col-span-2 w-full max-w-full" />
              </div>
            </div>
            <div v-else class="lg:col-span-2" key="skeletons">
              <div class="grid grid-cols-1 xs:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-2 xs:gap-3 sm:gap-4 md:gap-6">
                <BaseCardSkeleton class="col-span-1" />
                <BaseCardSkeleton class="col-span-1" />
                <BaseCardSkeleton class="col-span-1 lg:col-span-2 w-full max-w-full" />
              </div>
            </div>
          </transition>

          <!-- Mempool Info -->
          <transition name="fade" mode="out-in">
            <MempoolInfoCard
              v-if="shouldShowContent && !disableMempool"
              :mempool-info="dataState.mempoolInfo"
              class="dashboard-card lg:col-span-2"
            />
          </transition>

          <!-- Peer Software Distribution -->
          <transition name="fade" mode="out-in">
            <div v-if="shouldShowContent" class="dashboard-card lg:col-span-2">
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
          </transition>

          <!-- Peer Table -->
          <transition name="fade" mode="out-in">
            <div v-if="shouldShowContent" class="dashboard-card lg:col-span-2 overflow-x-auto" key="table">
              <h2 class="text-lg xs:text-xl sm:text-2xl font-medium mb-3 sm:mb-4 md:mb-6 break-words">
                <font-awesome-icon :icon="['fas', 'table']" class="mr-2 text-accent" /> Connection Details
              </h2>
              <PeerTable :peers="inboundPeers" type="inbound" />
              <PeerTable :peers="outboundPeers" type="outbound" />
            </div>
          </transition>
        </div>

        <Footer />
    </div>
</template>
