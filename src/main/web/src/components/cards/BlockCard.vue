
<template>
    <BaseCard :status="isOutOfSync ? 'error' : 'success'" interactive>
        <div class="flex justify-between items-center">
            <div :class="['text-2xl', 'sm:text-3xl', isOutOfSync ? 'text-status-error' : 'text-status-success']">
                <font-awesome-icon :icon="['fas', 'cubes']" />
            </div>
            <div class="text-xs uppercase text-text-secondary font-medium">Current Block</div>
        </div>
        <Tooltip :text="'Current height of the blockchain. This is the number of blocks in the chain. Click to view on mempool.org.'" position="bottom" horizontal="center">
            <a
                :href="`https://mempool.space/block/${blockchain.blocks}`"
                target="_blank"
                rel="noopener noreferrer"
                :class="['text-4xl', 'sm:text-5xl', 'font-light', 'mt-2', 'sm:mt-3', 'block', 'focus:text-status-warning', 'transition-colors', 'duration-150', 'outline-none', isOutOfSync ? 'text-status-error hover:text-accent' : 'text-status-success hover:text-accent']"
            >
                {{ formattedBlockCount }}
            </a>
        </Tooltip>
        <div class="mt-1 sm:mt-2 pt-1 sm:pt-2 border-t border-border-strong text-xs sm:text-sm text-text-secondary overflow-visible">
            <div class="flex items-center justify-between gap-3">
                <div class="flex-1">
                    <p class="mb-0.5 sm:mb-1">
                        <Tooltip :text="'Number of block headers known to the node.'" position="bottom" horizontal="left">
                            <font-awesome-icon :icon="['fas', 'list-ol']" class="mr-1" />
                        </Tooltip>
                        Headers:
                        <span class="font-bold text-text-primary" :class="isSyncingComputed ? 'text-status-warning' : ''">
                            {{ blockchain.headers }}
                            <span v-if="isSyncingComputed && headerBlockDiff !== 0" class="text-status-warning"> (+{{ headerBlockDiff }})</span>
                        </span>
                    </p>
                    <p class="mb-0.5 sm:mb-1">
                        <Tooltip :text="'Time since the last block was found.'" position="bottom" horizontal="left">
                            <font-awesome-icon :icon="['far', 'clock']" class="mr-1" />
                        </Tooltip>
                        Time:
                        <span class="font-bold text-text-primary">
                            {{ formatTimeSince(block.time).replace(/ ago$/, '') }} ago
                        </span>
                    </p>
                    <p class="mb-0.5 sm:mb-1">
                        <Tooltip :text="'Number of transactions in the current block.'" position="bottom" horizontal="left">
                            <font-awesome-icon :icon="['fas', 'exchange-alt']" class="mr-1" />
                        </Tooltip>
                        Tx Count:
                        <span class="font-bold text-text-primary">{{ block.nTx }}</span>
                    </p>
                </div>
                <Tooltip v-if="isOutOfSync" :text="syncWarningMessage" position="bottom" horizontal="right">
                    <div class="p-2 bg-status-error/10 border border-status-error/30 rounded text-status-error flex items-center gap-1 cursor-help animate-breathe flex-shrink-0">
                        <font-awesome-icon :icon="['fas', 'exclamation-circle']" class="text-xs" />
                        <span class="text-xs font-medium">Node out of sync</span>
                    </div>
                </Tooltip>
            </div>
        </div>
    </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatTimeSince } from '@utils/formatting';
import Tooltip from '@components/Tooltip.vue';
import BaseCard from '@components/BaseCard.vue';
import { getHeaderBlockDiff, isSyncing, getSyncWarningMessage } from '@utils/nodeHealth';
import type { BlockChainInfo, BlockInfoResponse } from '@types';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faCubes, faListOl, faExchangeAlt, faExclamationCircle } from '@fortawesome/free-solid-svg-icons';
import { faClock } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

library.add(faCubes, faListOl, faExchangeAlt, faExclamationCircle, faClock);

const props = withDefaults(defineProps<{
    blockchain: BlockChainInfo;
    block: BlockInfoResponse;
    forceOutOfSync?: boolean;
}>(), { forceOutOfSync: false });

// Format the number with a normal space as thousands separator, regardless of locale.
// Regex explanation:
//   - /\B(?=(\d{3})+(?!\d))/g matches positions in the string that are not at a word boundary (\B)
//     and are followed by one or more groups of three digits ((\d{3})+), but not followed by another digit (?!\d).
//   - This inserts a space between every group of three digits from the right, except at the start.
//   - Example: 1234567 => 1 234 567
const formattedBlockCount = computed(() => {
    return String(props.blockchain.blocks).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
});
const headerBlockDiff = computed(() => getHeaderBlockDiff(props.blockchain));
const isSyncingComputed = computed(() => isSyncing(props.blockchain));
const isOutOfSync = computed(() => props.forceOutOfSync || (props.blockchain.headers - props.blockchain.blocks > 2));
const syncWarningMessage = computed(() => getSyncWarningMessage(props.blockchain, props.block, formatTimeSince));

</script>
