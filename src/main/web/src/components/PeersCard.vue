<template>
    <div class="dashboard-card-interactive"
         :class="hasLowOutbound ? 'border-status-warning hover:border-status-warning hover:shadow-2xl' : 'border-status-success hover:border-status-success hover:shadow-2xl'">
        <div class="flex justify-between items-center">
            <div class="text-2xl sm:text-3xl"
                 :class="hasLowOutbound ? 'text-status-warning' : 'text-status-success'">
                <font-awesome-icon :icon="['fas', 'user-friends']" />
            </div>
            <div class="text-xs uppercase text-text-secondary font-medium">Total Peers</div>
        </div>
        <div class="text-4xl sm:text-5xl font-light text-text-primary mt-2 sm:mt-3">{{ stats.totalPeers }}</div>
        <div class="mt-1 sm:mt-2 pt-1 sm:pt-2 border-t border-border-strong text-xs sm:text-sm text-text-secondary overflow-visible">
            <p class="mb-0.5 sm:mb-1">
                <font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-1" /> Inbound: {{ stats.inboundCount }}
            </p>
            <div class="flex items-center justify-between gap-3">
                <p class="mb-0.5 sm:mb-1">
                    <font-awesome-icon :icon="['fas', 'sign-out-alt']" class="mr-1" /> Outbound: 
                    <span :class="hasLowOutbound ? 'text-status-warning font-bold' : ''">{{ stats.outboundCount }}</span>
                </p>
                <Tooltip v-if="hasLowOutbound" text="Low outbound connections can reduce the security and reliability of your Bitcoin node. Make sure your firewall allows outbound connections." position="bottom" horizontal="right">
                    <div class="p-2 bg-status-warning/10 border border-status-warning/30 rounded text-status-warning flex items-center gap-1 cursor-help animate-breathe flex-shrink-0">
                        <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-xs" />
                        <span class="text-xs font-medium">Low outbound connections</span>
                    </div>
                </Tooltip>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { GeneralStats } from '@types';
import Tooltip from '@components/Tooltip.vue';
import { hasLowOutboundPeers } from '@utils/nodeHealth';

const props = withDefaults(defineProps<{
    stats: GeneralStats;
    forceLowPeers?: boolean;
}>(), {
    forceLowPeers: false
});

const hasLowOutbound = computed(() => props.forceLowPeers || hasLowOutboundPeers(props.stats.outboundCount));
</script>
