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
        <IconUserGroup />
      </div>
      <div class="text-text-secondary text-xs font-medium uppercase">Total Peers</div>
    </div>
    <div class="mt-2 flex w-full items-center sm:mt-3">
      <Tooltip :text="'Total number of peers currently connected to your node.'">
        <div class="text-text-primary text-4xl font-light sm:text-5xl">
          {{ stats.totalPeers }}
        </div>
      </Tooltip>
      <div class="flex-1"></div>
      <Tooltip
        v-if="hasLowOutbound"
        text="Low outbound connections can reduce the security and reliability of your Bitcoin node. Make sure your firewall allows outbound connections."
        position="bottom"
      >
        <div
          class="bg-status-warning/10 border-status-warning/30 animate-breathe text-status-warning flex flex-shrink-0 cursor-help items-center gap-1 rounded border p-2"
        >
          <IconTriangleExclamation class="text-xs" />
          <span class="text-xs font-medium">Low outbound connections</span>
        </div>
      </Tooltip>
    </div>
    <div
      class="border-border-strong text-text-secondary mt-1 overflow-visible border-t pt-1 text-xs sm:mt-2 sm:pt-2 sm:text-sm"
    >
      <Tooltip :text="'Inbound connections: other nodes connecting to you.'">
        <p class="mb-0.5 sm:mb-1">
          <IconArrowRightToBracket class="mr-1" /> Inbound:
          {{ stats.inboundCount }}
        </p>
      </Tooltip>
      <div class="flex items-center justify-between gap-3">
        <Tooltip :text="'Outbound connections: your node connecting to others.'">
          <p class="mb-0.5 sm:mb-1">
            <IconArrowRightFromBracket class="mr-1" /> Outbound:
            <span :class="hasLowOutbound ? 'text-status-warning font-bold' : ''">{{
              stats.outboundCount
            }}</span>
          </p>
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
import {
  IconUserGroup,
  IconTriangleExclamation,
  IconArrowRightToBracket,
  IconArrowRightFromBracket,
} from '@/icons';

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
