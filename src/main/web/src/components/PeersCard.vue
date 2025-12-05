<template>
    <div class="bg-bg-card p-4 sm:p-6 rounded-xl shadow-lg border-l-4 border-status-success hover:shadow-2xl hover:border-accent transition transform hover:-translate-y-0.5">
        <div class="flex justify-between items-center">
            <div class="text-2xl sm:text-3xl text-status-success">
                <font-awesome-icon :icon="['fas', 'user-friends']" />
            </div>
            <div class="text-xs uppercase text-text-secondary font-medium">Total Peers</div>
        </div>
        <div class="text-4xl sm:text-5xl font-light text-text-primary mt-2 sm:mt-3">{{ stats.totalPeers }}</div>
        <div class="mt-1 sm:mt-2 pt-1 sm:pt-2 border-t border-border-strong text-xs sm:text-sm text-text-secondary">
            <p class="mb-0.5 sm:mb-1">
                <font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-1" /> Inbound: {{ stats.inboundCount }}
            </p>
            <p>
                <font-awesome-icon :icon="['fas', 'sign-out-alt']" class="mr-1" /> Outbound: {{ stats.outboundCount }}
            </p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { GeneralStats } from '@types';

defineProps<{
    stats: GeneralStats;
}>();
</script>
