<template>
  <BaseCard :status="hasLowOutbound ? 'warning' : 'success'" interactive>
    <div class="flex items-center justify-between">
      <div
        :class="[
          'text-2xl',
          'sm:text-3xl',
          hasLowOutbound ? 'text-status-warning' : 'text-status-success',
        ]"
      >
        <font-awesome-icon :icon="['fas', 'user-friends']" />
      </div>
      <div class="text-xs font-medium uppercase text-text-secondary">Total Peers</div>
    </div>
    <Tooltip :text="'Total number of peers currently connected to your node.'">
      <div class="mt-2 text-4xl font-light text-text-primary sm:mt-3 sm:text-5xl">
        {{ stats.totalPeers }}
      </div>
    </Tooltip>
    <div
      class="mt-1 overflow-visible border-t border-border-strong pt-1 text-xs text-text-secondary sm:mt-2 sm:pt-2 sm:text-sm"
    >
      <Tooltip :text="'Inbound connections: other nodes connecting to you.'">
        <p class="mb-0.5 sm:mb-1">
          <font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-1" /> Inbound:
          {{ stats.inboundCount }}
        </p>
      </Tooltip>
      <div class="flex items-center justify-between gap-3">
        <Tooltip :text="'Outbound connections: your node connecting to others.'">
          <p class="mb-0.5 sm:mb-1">
            <font-awesome-icon :icon="['fas', 'sign-out-alt']" class="mr-1" /> Outbound:
            <span :class="hasLowOutbound ? 'font-bold text-status-warning' : ''">{{
              stats.outboundCount
            }}</span>
          </p>
        </Tooltip>
        <Tooltip
          v-if="hasLowOutbound"
          text="Low outbound connections can reduce the security and reliability of your Bitcoin node. Make sure your firewall allows outbound connections."
          position="bottom"
          horizontal="right"
        >
          <div
            class="bg-status-warning/10 border-status-warning/30 animate-breathe flex flex-shrink-0 cursor-help items-center gap-1 rounded border p-2 text-status-warning"
          >
            <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-xs" />
            <span class="text-xs font-medium">Low outbound connections</span>
          </div>
        </Tooltip>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Tooltip from '@components/Tooltip.vue';
import BaseCard from '@components/BaseCard.vue';
import { hasLowOutboundPeers } from '@utils/nodeHealth';
import type { GeneralStats } from '../../types';

const props = withDefaults(
  defineProps<{
    stats: GeneralStats;
    forceLowPeers?: boolean;
  }>(),
  { forceLowPeers: false }
);

const hasLowOutbound = computed(
  () => props.forceLowPeers || hasLowOutboundPeers(props.stats.outboundCount)
);
</script>
