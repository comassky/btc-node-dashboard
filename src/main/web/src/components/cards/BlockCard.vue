<template>
  <BaseCard :status="isOutOfSync ? 'error' : 'success'" interactive>
    <div class="flex items-center justify-between">
      <div
        :class="[
          'text-2xl',
          'sm:text-3xl',
          isOutOfSync ? 'text-status-error' : 'text-status-success',
        ]"
      >
        <font-awesome-icon :icon="['fas', 'cubes']" />
      </div>
      <div class="text-xs font-medium uppercase text-text-secondary">Current Block</div>
    </div>
    <div class="flex items-center mt-2 sm:mt-3 w-full">
      <div class="flex-1 min-w-0">
        <Tooltip
          :text="isOutOfSync ? syncWarningMessage : 'Current height of the blockchain. This is the number of blocks in the chain. Click to view on mempool.org.'"
          position="bottom"
          horizontal="center"
        >
          <a
            :href="`https://mempool.space/block/${blockchain.blocks}`"
            target="_blank"
            rel="noopener noreferrer"
            :class="[
              'text-4xl',
              'sm:text-5xl',
              'font-light',
              'block',
              'focus:text-status-warning',
              'transition-colors',
              'duration-150',
              'outline-none',
              isOutOfSync ? 'text-status-error animate-breathe' : 'text-status-success',
            ]"
          >
            {{ formattedBlockCount }}
          </a>
        </Tooltip>
      </div>
      <!-- Icon removed as requested -->
    </div>
    <div
      class="mt-1 overflow-visible border-t border-border-strong pt-1 text-xs text-text-secondary sm:mt-2 sm:pt-2 sm:text-sm"
    >
      <div class="flex items-center justify-between gap-3">
        <div class="flex-1">
          <div class="mb-0.5 sm:mb-1">
            <div class="flex items-center w-full min-w-0">
              <Tooltip
                :text="'Number of block headers known to the node.'"
                position="bottom"
                horizontal="left"
              >
                <font-awesome-icon :icon="['fas', 'list-ol']" class="mr-1" />
              </Tooltip>
              <span class="truncate">
                Headers:
                <span
                  class="font-bold text-text-primary"
                  :class="isSyncingComputed ? 'text-status-warning' : ''"
                >
                  {{ blockchain.headers }}
                  <span v-show="isSyncingComputed && headerBlockDiff !== 0" class="ml-1 text-status-error font-bold whitespace-nowrap text-xs sm:text-sm animate-breathe">
                    (+{{ headerBlockDiff }})
                  </span>
                </span>
              </span>
            </div>
          </div>
          <p class="mb-0.5 sm:mb-1">
            <Tooltip
              :text="'Time since the last block was found.'"
              position="bottom"
              horizontal="left"
            >
              <font-awesome-icon :icon="['far', 'clock']" class="mr-1" />
            </Tooltip>
            Time:
            <span class="font-bold text-text-primary">
              {{ formatDistanceToNow(new Date(block.time * 1000), { addSuffix: true }) }}
            </span>
          </p>
          <p class="mb-0.5 sm:mb-1">
            <Tooltip
              :text="'Number of transactions in the current block.'"
              position="bottom"
              horizontal="left"
            >
              <font-awesome-icon :icon="['fas', 'exchange-alt']" class="mr-1" />
            </Tooltip>
            Tx Count:
            <span class="font-bold text-text-primary">{{ block.nTx }}</span>
          </p>
        </div>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatRelativeTimeSince } from '@utils/formatting';
import { formatDistanceToNow } from 'date-fns';
import Tooltip from '@components/Tooltip.vue';
import BaseCard from '@components/BaseCard.vue';
import { getHeaderBlockDiff, isSyncing, getSyncWarningMessage } from '@utils/nodeHealth';
import type { BlockChainInfo, BlockInfoResponse } from '@types';
import { formatNumberWithSpace } from '@/utils/formatting';

const props = withDefaults(
  defineProps<{
    blockchain: BlockChainInfo;
    block: BlockInfoResponse;
    forceOutOfSync?: boolean;
  }>(),
  { forceOutOfSync: false }
);

const formattedBlockCount = computed(() => formatNumberWithSpace(props.blockchain.blocks));
const headerBlockDiff = computed(() => getHeaderBlockDiff(props.blockchain));
const isSyncingComputed = computed(() => isSyncing(props.blockchain));
const isOutOfSync = computed(
  () => props.forceOutOfSync || props.blockchain.headers - props.blockchain.blocks > 2
);
const syncWarningMessage = computed(() =>
  getSyncWarningMessage(props.blockchain, props.block, formatRelativeTimeSince)
);
</script>
