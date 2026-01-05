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
    return 'bg-status-success/10 border border-status-success text-status-success';
  if (isConnectedAndRpc.value && isOutOfSync.value)
    return 'bg-status-error/10 border border-status-error text-status-error pulse-error';
  if (isConnectedAndRpc.value && hasLowOutbound.value)
    return 'bg-status-warning/10 border border-status-warning text-status-warning';
  return 'bg-status-error/10 border border-status-error text-status-error pulse-error';
});

const badgeTextClass = computed(() => {
  if (isHealthy.value) return 'text-status-success';
  if (isConnectedAndRpc.value && isOutOfSync.value) return 'text-status-error';
  if (isConnectedAndRpc.value && hasLowOutbound.value) return 'text-status-warning';
  return 'text-status-error';
});
</script>

<template>
  <div
    class="mb-10 flex items-center justify-center gap-4 rounded-xl p-5 font-medium shadow-2xl transition-all duration-300 ease-in-out sm:gap-10 md:p-6"
    :class="statusClass"
  >
    <Tooltip
      text="WebSocket: Connection between dashboard and backend. Shows if the dashboard is receiving live updates."
      position="bottom"
    >
      <span class="flex items-center">
        <IconNetworkWired class="mr-2 text-xl" /> WebSocket:
        {{ props.isConnected ? 'CONNECTED' : 'DISCONNECTED' }}
        <span
          v-if="props.isRetrying"
          :class="['ml-3 flex items-center text-sm whitespace-nowrap', badgeTextClass]"
          aria-live="polite"
        >
          <IconSpinner class="mr-2 flex-shrink-0 animate-spin" /> Reconnecting...
        </span>
      </span>
    </Tooltip>
    <Tooltip
      text="Node RPC: Connection between backend and Bitcoin node. Shows if the node is responding to commands."
      position="bottom"
    >
      <span class="flex items-center">
        <IconServer class="mr-2 text-xl" /> Node RPC:
        {{ props.rpcConnected ? 'ONLINE' : 'OFFLINE' }}
      </span>
    </Tooltip>
    <p
      v-if="props.errorMessage && !props.isRetrying"
      class="flex items-center text-sm font-light"
    >
      <IconCircleExclamation class="mr-2 flex-shrink-0" />
      {{ props.errorMessage }}
    </p>
    <div v-if="props.rpcConnected && hasWarnings" class="flex flex-wrap items-center gap-3 text-sm">
      <span v-if="hasLowOutbound" class="flex items-center">
        <IconTriangleExclamation class="mr-2" /> Low outbound peers ({{ props.outboundPeers }})
      </span>
      <span v-if="isOutOfSync" class="flex items-center">
        <IconCircleExclamation class="mr-2" /> Node out of sync
      </span>
    </div>
  </div>
</template>
