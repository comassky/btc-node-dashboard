<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted } from 'vue';
import { useDashboardStore } from '@/stores/dashboard';
import { useTheme } from '@composables/useTheme';
import { useMockData } from '@composables/useMockData';
import { storeToRefs } from 'pinia';

import { setMinOutboundPeers } from '@utils/nodeHealth';
import { watch } from 'vue';

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

const themeIcons: { [key: string]: string[] } = {
  light: ['fas', 'sun'],
  dark: ['fas', 'moon'],
  gray: ['fas', 'cloud'],
};
const themeIcon = computed(() => themeIcons[theme.value] || themeIcons.gray);

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
  <div class="min-h-screen bg-bg-app p-3 sm:p-4 md:p-6">
    <button
      @click="cycleTheme"
      class="btn btn-secondary fixed right-3 top-3 z-50 sm:right-4 sm:top-4"
      title="Cycle theme (light, dark, gray)"
      aria-label="Cycle theme"
      :aria-pressed="theme === 'dark' || theme === 'gray'"
    >
      <font-awesome-icon :icon="themeIcon" />
    </button>

    <div
      v-if="MOCK_MODE"
      class="dashboard-card fixed left-3 top-3 z-50 border-accent text-xs sm:left-4 sm:top-4"
    >
      <div class="mb-2 flex items-center gap-2 font-bold text-accent">
        <font-awesome-icon :icon="['fas', 'hard-hat']" /> MOCK MODE
      </div>
      <button @click="cycleMockScenario" class="btn btn-accent">Cycle Scenario</button>
      <div class="mt-2 text-text-secondary">
        Current: <span class="font-bold text-text-primary">{{ mockScenario }}</span>
      </div>
    </div>

    <transition name="fade" mode="out-in">
      <div class="mb-6 mt-12 text-center sm:mb-8 sm:mt-4 md:mb-12" key="header">
        <h1
          class="card-title px-2 text-2xl font-extralight uppercase tracking-wide text-accent sm:text-3xl sm:tracking-widest md:text-4xl lg:text-5xl"
        >
          <font-awesome-icon :icon="['fab', 'bitcoin']" class="mr-1 sm:mr-2" />
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
      class="grid grid-cols-1 gap-2 gap-y-4 sm:gap-4 md:grid-cols-2 md:gap-6 md:gap-y-6 lg:grid-cols-2 lg:gap-8"
    >
      <!-- Main Cards -->
      <transition name="fade" mode="out-in">
        <div v-if="shouldShowContent" class="lg:col-span-2" key="cards">
          <div
            class="xs:grid-cols-2 xs:gap-3 grid grid-cols-1 gap-2 sm:gap-4 md:grid-cols-2 md:gap-6 lg:grid-cols-4"
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
            class="xs:grid-cols-2 xs:gap-3 grid grid-cols-1 gap-2 sm:gap-4 md:grid-cols-2 md:gap-6 lg:grid-cols-4"
          >
            <BaseCardSkeleton class="col-span-1" />
            <BaseCardSkeleton class="col-span-1" />
            <BaseCardSkeleton class="col-span-1 w-full max-w-full lg:col-span-2" />
          </div>
        </div>
      </transition>

      <!-- Mempool Info -->
      <MempoolInfoCard
        v-if="shouldShowContent && !disableMempool"
        v-motion
        :initial="{ opacity: 0 }"
        :enter="{ opacity: 1, transition: { duration: 20 } }"
        :leave="{ opacity: 0, transition: { duration: 15 } }"
        :mempool-info="dataState.mempoolInfo"
        class="dashboard-card lg:col-span-2"
      />

      <!-- Peer Software Distribution -->
      <div
        v-if="shouldShowContent"
        v-motion
        :initial="{ opacity: 0 }"
        :enter="{ opacity: 1, transition: { duration: 20 } }"
        :leave="{ opacity: 0, transition: { duration: 15 } }"
        class="dashboard-card lg:col-span-2"
      >
        <h2 class="xs:text-xl mb-3 break-words text-lg font-medium sm:mb-4 sm:text-2xl md:mb-6">
          <font-awesome-icon :icon="['fas', 'chart-pie']" class="mr-2 text-accent" />
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
        v-if="shouldShowContent"
        v-motion
        :initial="{ opacity: 0 }"
        :enter="{ opacity: 1, transition: { duration: 20 } }"
        :leave="{ opacity: 0, transition: { duration: 15 } }"
        class="dashboard-card overflow-x-auto lg:col-span-2"
        key="table"
      >
        <h2 class="xs:text-xl mb-3 break-words text-lg font-medium sm:mb-4 sm:text-2xl md:mb-6">
          <font-awesome-icon :icon="['fas', 'table']" class="mr-2 text-accent" /> Connection
          Details
        </h2>
        <PeerTable :peers="inboundPeers" type="inbound" />
        <PeerTable :peers="outboundPeers" type="outbound" />
      </div>
    </div>

    <Footer />
  </div>
</template>
