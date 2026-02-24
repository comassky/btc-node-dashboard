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
        <IconCubes />
      </div>
      <div class="text-text-secondary text-xs font-medium uppercase">Current Block</div>
    </div>
    <div class="mt-2 flex w-full items-center sm:mt-3">
      <div class="min-w-0 flex-1">
        <Tooltip
          :text="
            isOutOfSync
              ? syncWarningMessage
              : 'Current height of the blockchain. This is the number of blocks in the chain. Click to view on mempool.org.'
          "
          position="bottom"
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
              'hover:text-accent',
              'focus:text-accent',
              'transition-colors',
              'duration-150',
              'outline-none',
              isOutOfSync ? 'animate-breathe text-status-error' : 'text-status-success',
            ]"
          >
            {{ formattedBlockCount }}
          </a>
        </Tooltip>
      </div>
      <!-- Icon removed as requested -->
    </div>
    <div
      class="border-border-strong text-text-secondary mt-1 overflow-visible border-t pt-1 text-xs sm:mt-2 sm:pt-2 sm:text-sm"
    >
      <div class="flex items-center justify-between gap-3">
        <div class="flex-1">
          <div class="mb-0.5 sm:mb-1">
            <div class="flex w-full min-w-0 items-center">
              <Tooltip :text="'Number of block headers known to the node.'" position="bottom">
                <IconListOl class="mr-1 inline-flex items-center" />
              </Tooltip>
              <span class="truncate">
                Headers:
                <span
                  class="text-text-primary font-bold"
                  :class="isSyncingComputed ? 'text-status-warning' : ''"
                >
                  {{ blockchain.headers }}
                  <span
                    v-show="isSyncingComputed && headerBlockDiff !== 0"
                    class="animate-breathe text-status-error ml-1 text-xs font-bold whitespace-nowrap sm:text-sm"
                  >
                    (+{{ headerBlockDiff }})
                  </span>
                </span>
              </span>
            </div>
          </div>
          <p class="mb-0.5 flex items-center sm:mb-1">
            <Tooltip :text="'Time since the last block was found.'" position="bottom">
              <IconClock class="mr-1 inline-flex items-center" />
            </Tooltip>
            <span class="truncate">
              Time:
              <span class="text-text-primary font-bold">
                {{ formatDistanceToNow(new Date(block.time * 1000), { addSuffix: true }) }}
              </span>
            </span>
          </p>
          <p class="mb-0.5 flex items-center sm:mb-1">
            <Tooltip :text="'Number of transactions in the current block.'" position="bottom">
              <IconArrowRightArrowLeft class="mr-1 inline-flex items-center" />
            </Tooltip>
            <span class="truncate">
              Tx Count:
              <span class="text-text-primary font-bold">{{ block.nTx }}</span>
            </span>
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
import { IconCubes, IconListOl, IconClock, IconArrowRightArrowLeft } from '@/icons';

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
