<template>
    <div class="bg-bg-card p-4 sm:p-6 rounded-xl shadow-lg border-l-4 border-status-warning hover:shadow-2xl hover:border-accent transition transform hover:-translate-y-0.5">
        <div class="flex justify-between items-center">
            <div class="text-2xl sm:text-3xl text-status-warning">
                <font-awesome-icon :icon="['fas', 'cubes']" />
            </div>
            <div class="text-xs uppercase text-text-secondary font-medium">Current Block</div>
        </div>
        <div class="text-4xl sm:text-5xl font-light text-text-primary mt-2 sm:mt-3"
             :title="'Current height of the ' + blockchain.chain + ' blockchain'">
            {{ blockchain.blocks }}
        </div>

        <div class="mt-1 sm:mt-2 pt-1 sm:pt-2 border-t border-border-strong text-xs sm:text-sm text-text-secondary">
            <p class="mb-0.5 sm:mb-1">
                <font-awesome-icon :icon="['fas', 'list-ol']" class="mr-1" /> Headers: 
                <span class="font-bold text-text-primary">{{ blockchain.headers }}</span>
            </p>
            <p class="mb-0.5 sm:mb-1">
                <font-awesome-icon :icon="['far', 'clock']" class="mr-1" /> Time: 
                <span class="font-bold text-text-primary">{{ formatTimeSince(block.time) }} ago</span>
            </p>
            <p>
                <font-awesome-icon :icon="['fas', 'exchange-alt']" class="mr-1" /> Tx Count: 
                <span class="font-bold text-text-primary">{{ block.nTx }}</span>
            </p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { BlockChainInfo, BlockInfo } from '../types';
import { formatTimeSince } from '../utils/formatting';

defineProps<{
    blockchain: BlockChainInfo;
    block: BlockInfo;
}>();
</script>
