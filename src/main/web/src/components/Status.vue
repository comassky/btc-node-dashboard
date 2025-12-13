<script setup lang="ts">
import { computed } from 'vue';
import { BlockChainInfo, BlockInfoResponse } from '@types';
import { hasLowOutboundPeers, isNodeOutOfSync } from '@utils/nodeHealth';

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

const isHealthy = computed(() => props.isConnected && props.rpcConnected && !hasWarnings.value);
// Removed unused variable hasIssue

const statusClass = computed(() =>
    isHealthy.value
        ? 'bg-status-success/10 border border-status-success text-status-success'
        : props.isConnected && props.rpcConnected && hasWarnings.value
            ? 'bg-status-warning/10 border border-status-warning text-status-warning'
            : 'bg-status-error/10 border border-status-error text-status-error pulse-error'
);

const badgeTextClass = computed(() =>
    isHealthy.value
        ? 'text-status-success'
        : props.isConnected && props.rpcConnected && hasWarnings.value
            ? 'text-status-warning'
            : 'text-status-error'
);
</script>

<template>
    <div
        class="status-bar is-mobile flex justify-center items-center gap-4 sm:gap-10 p-5 md:p-6 mb-10 rounded-xl font-medium shadow-2xl transition-all duration-300 ease-in-out"
        :class="statusClass"
    >
        <Tooltip :text="'WebSocket: Connection between dashboard and backend. Shows if the dashboard is receiving live updates.'" position="bottom" horizontal="left">
            <span class="flex items-center">
                <font-awesome-icon :icon="['fas', 'network-wired']" class="mr-2 text-xl" /> WebSocket: {{ props.isConnected ? 'CONNECTED' : 'DISCONNECTED' }}
                <span v-if="props.isRetrying" :class="['ml-3 flex items-center text-sm', badgeTextClass]" aria-live="polite">
                    <font-awesome-icon :icon="['fas','sync-alt']" class="mr-2 animate-spin" /> Reconnecting...
                </span>
            </span>
        </Tooltip>
        <Tooltip :text="'Node RPC: Connection between backend and Bitcoin node. Shows if the node is responding to commands.'" position="bottom" horizontal="left">
            <span class="flex items-center">
                <font-awesome-icon :icon="['fas', 'server']" class="mr-2 text-xl" /> Node RPC: {{ props.rpcConnected ? 'ONLINE' : 'OFFLINE' }}
            </span>
        </Tooltip>
        <p v-if="props.errorMessage && !props.isRetrying" class="text-sm font-light mt-1 pt-1 sm:border-t-0 sm:pt-0">
            <font-awesome-icon :icon="['fas', 'exclamation-circle']" class="mr-2" /> {{ props.errorMessage }}
        </p>
        <div v-if="props.rpcConnected && hasWarnings" class="flex flex-wrap items-center gap-3 text-sm">
            <span v-if="hasLowOutbound" class="flex items-center">
                <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="mr-2" /> Low outbound peers ({{ props.outboundPeers }})
            </span>
            <span v-if="isOutOfSync" class="flex items-center">
                <font-awesome-icon :icon="['fas', 'exclamation-circle']" class="mr-2" /> Node out of sync
            </span>
        </div>
    </div>
</template>