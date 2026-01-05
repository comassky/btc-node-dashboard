<script setup lang="ts">
import type { BlockChainInfo, BlockInfoResponse } from '@types';
import { hasLowOutboundPeers, isNodeOutOfSync } from '@utils/nodeHealth';
import {
  IconNetworkWired,
  IconSpinner,
  IconServer,
  IconCircleExclamation,
  IconTriangleExclamation,
} from '@/icons';

import Tooltip from '@components/Tooltip.vue';

const props = defineProps<{
  isConnected: boolean;
  rpcConnected: boolean;
  errorMessage: string | null;
  isRetrying?: boolean;
  outboundPeers: number;
  blockchain: BlockChainInfo;
  block: BlockInfoResponse;
}>();

const hasLowOutbound = computed(() => hasLowOutboundPeers(props.outboundPeers));
const isOutOfSync = computed(() => isNodeOutOfSync(props.blockchain, props.block));
const hasWarnings = computed(() => hasLowOutbound.value || isOutOfSync.value);
const isConnectedAndRpc = computed(() => props.isConnected && props.rpcConnected);
const isHealthy = computed(() => isConnectedAndRpc.value && !hasWarnings.value);

const statusClass = computed(() => {
  if (isHealthy.value)
    return 'bg-status-success/15 border-2 border-status-success/30 text-status-success shadow-lg shadow-status-success/20';
  if (isConnectedAndRpc.value && isOutOfSync.value)
    return 'bg-status-error/15 border-2 border-status-error/30 text-status-error pulse-error shadow-lg shadow-status-error/20';
  if (isConnectedAndRpc.value && hasLowOutbound.value)
    return 'bg-status-warning/15 border-2 border-status-warning/30 text-status-warning shadow-lg shadow-status-warning/20';
  return 'bg-status-error/15 border-2 border-status-error/30 text-status-error pulse-error shadow-lg shadow-status-error/20';
});

const badgeTextClass = computed(() => {
  if (isHealthy.value) return 'text-status-success';
  if (isConnectedAndRpc.value && isOutOfSync.value) return 'text-status-error';
  if (isConnectedAndRpc.value && hasLowOutbound.value) return 'text-status-warning';
  return 'text-status-error';
});

const wsIndicatorClass = computed(() => {
  if (!props.isConnected) return 'bg-status-error';
  return 'bg-status-success animate-pulse';
});

const rpcIndicatorClass = computed(() => {
  if (!props.rpcConnected) return 'bg-status-error';
  if (hasWarnings.value) return 'bg-status-warning animate-pulse';
  return 'bg-status-success animate-pulse';
});
</script>

<template>
  <div
    class="mb-6 flex flex-wrap items-center justify-center gap-3 rounded-xl px-4 py-3 text-sm font-medium backdrop-blur-sm transition-all duration-300 ease-in-out sm:gap-6 sm:text-base md:px-5 md:py-3.5"
    :class="statusClass"
  >
    <div class="flex items-center gap-2">
      <div class="flex h-1.5 w-1.5 rounded-full" :class="wsIndicatorClass"></div>
      <Tooltip
        text="WebSocket: Connection between dashboard and backend. Shows if the dashboard is receiving live updates."
        position="bottom"
      >
        <span class="flex items-center gap-2">
          <IconNetworkWired class="text-lg" />
          <span class="hidden sm:inline text-xs opacity-70">WebSocket:</span>
          <span class="font-semibold">{{ props.isConnected ? 'CONNECTED' : 'DISCONNECTED' }}</span>
          <span
            v-if="props.isRetrying"
            :class="['ml-2 flex items-center text-xs whitespace-nowrap', badgeTextClass]"
            aria-live="polite"
          >
            <IconSpinner class="mr-1 flex-shrink-0 animate-spin" /> Reconnecting...
          </span>
        </span>
      </Tooltip>
    </div>

    <div class="hidden sm:block h-6 w-px bg-current opacity-20"></div>

    <div class="flex items-center gap-2">
      <div class="flex h-1.5 w-1.5 rounded-full" :class="rpcIndicatorClass"></div>
      <Tooltip
        text="Node RPC: Connection between backend and Bitcoin node. Shows if the node is responding to commands."
        position="bottom"
      >
        <span class="flex items-center gap-2">
          <IconServer class="text-lg" />
          <span class="hidden sm:inline text-xs opacity-70">Node RPC:</span>
          <span class="font-semibold">{{ props.rpcConnected ? 'ONLINE' : 'OFFLINE' }}</span>
        </span>
      </Tooltip>
    </div>

    <p
      v-if="props.errorMessage && !props.isRetrying"
      class="flex w-full items-center justify-center text-xs sm:w-auto"
    >
      <IconCircleExclamation class="mr-1.5 flex-shrink-0 text-sm" />
      {{ props.errorMessage }}
    </p>
    <div v-if="props.rpcConnected && hasWarnings" class="flex w-full flex-wrap items-center justify-center gap-2 text-xs sm:w-auto">
      <span v-if="hasLowOutbound" class="flex items-center gap-1.5 rounded-md bg-status-warning/20 px-2.5 py-1">
        <IconTriangleExclamation class="text-sm" /> Low outbound peers ({{ props.outboundPeers }})
      </span>
      <span v-if="isOutOfSync" class="flex items-center gap-1.5 rounded-md bg-status-error/20 px-2.5 py-1">
        <IconCircleExclamation class="text-sm" /> Node out of sync
      </span>
    </div>
  </div>
</template>
