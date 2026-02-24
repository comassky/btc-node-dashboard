<script setup lang="ts">
import { useDashboardStore } from '@/stores/dashboard';
import { useTheme } from '@composables/useTheme';
import { useMockData } from '@composables/useMockData';
import {
  IconBitcoin,
  IconHelmetSafety,
  IconSun,
  IconMoon,
  IconCloud,
  IconChartPie,
  IconTable,
  IconLayerGroup,
  IconDiagramProject,
} from '@/icons';

import { setMinOutboundPeers } from '@utils/nodeHealth';

// Async Components
const PeersCard = defineAsyncComponent(() => import('@components/cards/PeersCard.vue'));
const BlockCard = defineAsyncComponent(() => import('@components/cards/BlockCard.vue'));
const NodeCard = defineAsyncComponent(() => import('@components/cards/NodeCard.vue'));
const PeerDistributionChart = defineAsyncComponent(
  () => import('@components/PeerDistributionChart.vue')
);
const MempoolInfoCard = defineAsyncComponent(() => import('@components/cards/MempoolInfoCard.vue'));
const PeerTable = defineAsyncComponent(() => import('@components/PeerTable.vue'));

import Status from '@components/Status.vue';
import Footer from '@components/Footer.vue';
import BaseCardSkeleton from '@components/cards/BaseCardSkeleton.vue';

// Store
const dashboardStore = useDashboardStore();
const { dataState, disableMempool, isConnected, rpcConnected, errorMessage, isRetrying } =
  storeToRefs(dashboardStore);

// Derived state from dataState (all as computed for proper reactivity with mock data)
const inboundPeers = computed(() => dataState.value.inboundPeer);
const outboundPeers = computed(() => dataState.value.outboundPeer);
const subverInbound = computed(() => dataState.value.subverDistribution.inbound);
const subverOutbound = computed(() => dataState.value.subverDistribution.outbound);
const inboundCount = computed(() => dataState.value.generalStats.inboundCount);
const outboundCount = computed(() => dataState.value.generalStats.outboundCount);

// Theme
const { theme, isDarkMode, cycleTheme } = useTheme();

// Mock Data (for development/demo)
const { MOCK_MODE, mockScenario, cycleMockScenario, generateMockData, getMockConnectionState } =
  useMockData();

// Computed properties for cleaner template logic
const shouldShowContent = computed(() => MOCK_MODE.value || rpcConnected.value);

const connectionState = computed(() => {
  if (MOCK_MODE) {
    const mockState = getMockConnectionState();
    return {
      isConnected: mockState.isConnected,
      rpcConnected: mockState.rpcConnected,
      errorMessage: mockState.errorMessage,
      isRetrying: false,
    };
  }
  return {
    isConnected: isConnected.value,
    rpcConnected: rpcConnected.value,
    errorMessage: errorMessage.value,
    isRetrying: isRetrying.value,
  };
});

const themeIconsMap = {
  light: IconSun,
  dark: IconMoon,
  gray: IconCloud,
  auto: IconCloud,
} as const;
const themeIcon = computed(() => themeIconsMap[theme.value] ?? IconCloud);

onMounted(() => {
  if (MOCK_MODE.value) {
    // Apply the minimum peer threshold for mock (8 by default)
    setMinOutboundPeers(8);
    Object.assign(dataState.value, generateMockData());
    // Ne pas initialiser le dashboardStore en mode mock
    // No startAutoCycle: scenario changes only on click
  } else {
    dashboardStore.initialize();
  }
});

// Update mocked data on every scenario change
if (MOCK_MODE.value) {
  watch(
    () => mockScenario.value,
    () => {
      Object.assign(dataState.value, generateMockData());
    }
  );
}

onBeforeUnmount(() => {
  if (!MOCK_MODE) {
    dashboardStore.disconnect();
  }
});
</script>

<template>
  <div class="bg-bg-app min-h-screen p-3 pb-16 sm:p-4 sm:pb-4 md:p-6 md:pb-6">
    <!-- Navigation Menu -->
    <nav
      class="bg-bg-card/95 border-border-strong/50 fixed right-0 bottom-0 left-0 z-40 flex flex-row items-center justify-around gap-1 rounded-t-lg rounded-b-none border-t p-2 shadow-xl backdrop-blur-lg sm:top-4 sm:right-4 sm:bottom-auto sm:left-auto sm:w-auto sm:justify-center sm:gap-2 sm:rounded-lg sm:border sm:border-t sm:p-1.5"
    >
      <a href="#overview" class="btn-nav" title="Overview" aria-label="Go to overview">
        <IconDiagramProject />
      </a>
      <a href="#mempool" class="btn-nav" title="Mempool" aria-label="Go to mempool info">
        <IconLayerGroup />
      </a>
      <a
        href="#distribution"
        class="btn-nav"
        title="Peer Distribution"
        aria-label="Go to peer distribution"
      >
        <IconChartPie />
      </a>
      <a
        href="#connections"
        class="btn-nav"
        title="Connection Details"
        aria-label="Go to connection details"
      >
        <IconTable />
      </a>

      <div class="bg-border-strong h-6 w-px"></div>

      <button
        @click="cycleTheme"
        class="btn-nav"
        title="Cycle theme (light, dark, gray)"
        aria-label="Cycle theme"
        :aria-pressed="theme === 'dark' || theme === 'gray'"
      >
        <component :is="themeIcon" />
      </button>
    </nav>

    <div
      v-if="MOCK_MODE"
      class="dashboard-card border-accent fixed top-3 left-3 z-50 text-xs sm:top-4 sm:left-4"
    >
      <div class="text-accent mb-2 flex items-center gap-2 font-bold">
        <IconHelmetSafety /> MOCK MODE
      </div>
      <button @click="cycleMockScenario" class="btn btn-accent">Cycle Scenario</button>
      <div class="text-text-secondary mt-2">
        Current: <span class="text-text-primary font-bold">{{ mockScenario }}</span>
      </div>
    </div>

    <transition name="fade" mode="out-in">
      <div class="mt-4 mb-6 text-center sm:mb-8 md:mb-10" key="header">
        <h1
          class="text-accent flex items-center justify-center gap-2 px-2 text-2xl font-extralight tracking-wide uppercase sm:gap-3 sm:text-3xl sm:tracking-widest md:text-4xl lg:text-5xl"
        >
          <IconBitcoin class="text-3xl sm:text-4xl md:text-5xl lg:text-6xl" />
          <span class="hidden sm:inline">Bitcoin Node Dashboard</span>
          <span class="sm:hidden">BTC Dashboard</span>
        </h1>
      </div>
    </transition>

    <transition name="fade" mode="out-in">
      <Status
        v-if="MOCK_MODE || isConnected || isRetrying"
        :isConnected="connectionState.isConnected"
        :rpcConnected="connectionState.rpcConnected"
        :errorMessage="connectionState.errorMessage"
        :outboundPeers="dataState.generalStats.outboundCount"
        :blockchain="dataState.blockchainInfoResponse"
        :block="dataState.block"
        :isRetrying="connectionState.isRetrying"
        key="status"
      />
    </transition>

    <div
      class="grid grid-cols-1 gap-5 gap-y-5 sm:gap-5 md:grid-cols-2 md:gap-5 md:gap-y-5 lg:grid-cols-2 lg:gap-5"
    >
      <!-- Main Cards -->
      <transition name="fade" mode="out-in">
        <div id="overview" v-if="shouldShowContent" class="lg:col-span-2" key="cards">
          <div
            class="xs:grid-cols-2 xs:gap-3 grid grid-cols-1 gap-3 sm:gap-4 md:grid-cols-2 md:gap-5 lg:grid-cols-4"
          >
            <PeersCard
              :stats="dataState.generalStats"
              :forceLowPeers="MOCK_MODE && mockScenario === 'lowPeers'"
              class="col-span-1"
            />
            <BlockCard
              :blockchain="dataState.blockchainInfoResponse"
              :block="dataState.block"
              :forceOutOfSync="MOCK_MODE && mockScenario === 'outOfSync'"
              class="col-span-1"
            />
            <NodeCard
              :node="dataState.nodeInfo"
              :blockchain="dataState.blockchainInfoResponse"
              :upTime="dataState.upTime"
              class="col-span-1 w-full max-w-full lg:col-span-2"
            />
          </div>
        </div>
        <div v-else class="lg:col-span-2" key="skeletons">
          <div
            class="xs:grid-cols-2 xs:gap-3 grid grid-cols-1 gap-3 sm:gap-4 md:grid-cols-2 md:gap-5 lg:grid-cols-4"
          >
            <BaseCardSkeleton class="shimmer col-span-1" />
            <BaseCardSkeleton class="shimmer col-span-1" />
            <BaseCardSkeleton class="shimmer col-span-1 w-full max-w-full lg:col-span-2" />
          </div>
        </div>
      </transition>

      <!-- Mempool Info -->
      <MempoolInfoCard
        id="mempool"
        v-if="shouldShowContent && !disableMempool"
        class="dashboard-card lg:col-span-2"
        :mempool-info="dataState.mempoolInfo"
      />

      <!-- Peer Software Distribution -->
      <div id="distribution" v-if="shouldShowContent" class="dashboard-card lg:col-span-2">
        <h2
          class="xs:text-xl mb-3 flex items-center text-lg font-medium break-words sm:mb-4 sm:text-2xl md:mb-6"
        >
          <IconChartPie class="text-accent mr-2" />
          <span class="hidden sm:inline">Peer Software Distribution</span>
          <span class="sm:hidden">Peers Distribution</span>
        </h2>
        <div class="mt-2 flex flex-col gap-4 sm:mt-3 sm:gap-6 md:mt-4 md:flex-row md:gap-8">
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

      <!-- Peer Table -->
      <div
        id="connections"
        v-if="shouldShowContent"
        class="dashboard-card overflow-x-auto lg:col-span-2"
        key="table"
      >
        <h2
          class="xs:text-xl mb-3 flex items-center text-lg font-medium break-words sm:mb-4 sm:text-2xl md:mb-6"
        >
          <IconTable class="text-accent mr-2" /> Connection Details
        </h2>
        <PeerTable :peers="inboundPeers" type="inbound" />
        <PeerTable :peers="outboundPeers" type="outbound" />
      </div>
    </div>

    <Footer />
  </div>
</template>
