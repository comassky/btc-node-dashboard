<template>
    <div class="dashboard-card-interactive"
         :class="isOutOfSync ? 'border-status-error hover:border-status-error hover:shadow-2xl' : 'border-status-success hover:border-status-success hover:shadow-2xl'">
        <div class="flex justify-between items-center">
            <div class="text-2xl sm:text-3xl"
                 :class="isOutOfSync ? 'text-status-error' : 'text-status-success'">
                <font-awesome-icon :icon="['fas', 'cubes']" />
            </div>
            <div class="text-xs uppercase text-text-secondary font-medium">Current Block</div>
        </div>
        <Tooltip v-if="block.hash" :text="'View block ' + blockchain.blocks + ' on mempool.space'">
            <a 
                :href="getMempoolUrl()" 
                target="_blank" 
                rel="noopener noreferrer"
                class="text-4xl sm:text-5xl font-light text-text-primary mt-2 sm:mt-3 block hover:text-accent transition-colors cursor-pointer">
                {{ blockchain.blocks }}
            </a>
        </Tooltip>
        <div 
            v-else
            class="text-4xl sm:text-5xl font-light text-text-primary mt-2 sm:mt-3"
            :title="'Current height of the ' + blockchain.chain + ' blockchain'">
            {{ blockchain.blocks }}
        </div>

        <div class="mt-1 sm:mt-2 pt-1 sm:pt-2 border-t border-border-strong text-xs sm:text-sm text-text-secondary overflow-visible">
            <div class="flex items-center justify-between gap-3">
                <div class="flex-1">
                    <p class="mb-0.5 sm:mb-1">
                        <font-awesome-icon :icon="['fas', 'list-ol']" class="mr-1" /> Headers: 
                        <span class="font-bold text-text-primary" :class="isSyncing ? 'text-status-warning' : ''">
                            {{ blockchain.headers }}
                            <span v-if="isSyncing" class="text-status-warning"> (+{{ headerBlockDiff }})</span>
                        </span>
                    </p>
                    <p class="mb-0.5 sm:mb-1">
                        <font-awesome-icon :icon="['far', 'clock']" class="mr-1" /> Time: 
                        <span class="font-bold text-text-primary" :class="isBlockTooOld ? 'text-status-error' : ''">
                            {{ formatTimeSince(block.time) }} ago
                        </span>
                    </p>
                    <p>
                        <font-awesome-icon :icon="['fas', 'exchange-alt']" class="mr-1" /> Tx Count: 
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
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { BlockChainInfo, BlockInfo } from '@types';
import { formatTimeSince } from '@utils/formatting';
import Tooltip from '@components/Tooltip.vue';
import { 
    getHeaderBlockDiff, 
    isBlockTooOld as checkBlockTooOld, 
    isSyncing as checkSyncing,
    isNodeOutOfSync,
    getSyncWarningMessage
} from '@utils/nodeHealth';

const props = withDefaults(defineProps<{
    blockchain: BlockChainInfo;
    block: BlockInfo;
    forceOutOfSync?: boolean;
}>(), {
    forceOutOfSync: false
});

const getMempoolUrl = () => {
    const baseUrl = props.blockchain.chain === 'test' 
        ? 'https://mempool.space/testnet' 
        : 'https://mempool.space';
    return `${baseUrl}/block/${props.block.hash}`;
};

const headerBlockDiff = computed(() => getHeaderBlockDiff(props.blockchain));

const isBlockTooOld = computed(() => checkBlockTooOld(props.block.time));

const isSyncing = computed(() => checkSyncing(props.blockchain));

const isOutOfSync = computed(() => props.forceOutOfSync || isNodeOutOfSync(props.blockchain, props.block));

const syncWarningMessage = computed(() => getSyncWarningMessage(props.blockchain, props.block, formatTimeSince));
</script>
