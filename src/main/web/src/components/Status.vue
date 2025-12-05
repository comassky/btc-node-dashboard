<script setup lang="ts">
import { computed } from 'vue';
import { BlockChainInfo, BlockInfo } from '@types';
import { hasLowOutboundPeers, isNodeOutOfSync } from '@utils/nodeHealth';

const props = defineProps<{
    isConnected: boolean;
    rpcConnected: boolean;
    errorMessage: string | null;
    outboundPeers: number;
    blockchain: BlockChainInfo;
    block: BlockInfo;
}>();

const hasLowOutbound = computed(() => hasLowOutboundPeers(props.outboundPeers));

const isOutOfSync = computed(() => isNodeOutOfSync(props.blockchain, props.block));

const hasWarnings = computed(() => hasLowOutbound.value || isOutOfSync.value);

const statusClass = computed(() => ({
    'bg-status-success/10 border border-status-success text-status-success': props.isConnected && props.rpcConnected && !hasWarnings.value,
    'bg-status-warning/10 border border-status-warning text-status-warning': props.isConnected && props.rpcConnected && hasWarnings.value,
    'bg-status-error/10 border border-status-error text-status-error pulse-error': !props.isConnected || !props.rpcConnected
}));
</script>

<template>
    <div
        class="status-bar is-mobile flex justify-center items-center gap-4 sm:gap-10 p-5 md:p-6 mb-10 rounded-xl font-medium shadow-2xl transition-all duration-300 ease-in-out"
        :class="statusClass"
    >
        <span
            :title="props.isConnected ? 'WebSocket link is active and open' : 'WebSocket disconnected. Retrying connection...'"
            class="flex items-center"
        >
            <font-awesome-icon :icon="['fas', 'network-wired']" class="mr-2 text-xl" /> WebSocket: {{ props.isConnected ? 'CONNECTED' : 'DISCONNECTED' }}
        </span>
        <span
            :title="props.rpcConnected ? 'Node is responding to RPC commands' : 'RPC connection lost or not yet established'"
            class="flex items-center"
        >
            <font-awesome-icon :icon="['fas', 'server']" class="mr-2 text-xl" /> Node RPC: {{ props.rpcConnected ? 'ONLINE' : 'OFFLINE' }}
        </span>
        <p v-if="props.errorMessage" class="text-sm font-light mt-1 pt-1 sm:border-t-0 sm:pt-0">
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