<template>
    <div class="bg-bg-card p-4 sm:p-6 rounded-xl shadow-lg border-l-4 border-accent hover:shadow-2xl hover:border-accent transition transform hover:-translate-y-0.5 lg:col-span-2">
        <div class="flex justify-between items-center">
            <div class="text-2xl sm:text-3xl text-accent">
                <font-awesome-icon :icon="['fas', 'hard-hat']" />
            </div>
            <div class="text-xs uppercase text-text-secondary font-medium">Node Details</div>
        </div>

        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-end mt-3 gap-3 sm:gap-0">
            <div class="min-w-0 w-full sm:w-auto">
                <Tooltip :text="cleanedSubversion">
                    <div class="text-xl sm:text-2xl font-bold text-text-primary truncate">
                        {{ cleanedSubversion }}
                    </div>
                </Tooltip>
                <div class="text-xs sm:text-sm text-text-secondary">Protocol v{{ node.protocolversion }}</div>
            </div>

            <div class="w-full sm:w-auto">
                <div class="text-xl sm:text-2xl font-bold text-text-primary" title="Time elapsed since node started">
                    {{ upTime }}
                </div>
                <div class="text-xs sm:text-sm text-text-secondary">Uptime</div>
            </div>
        </div>
        
        <div class="mt-2 sm:mt-3 pt-2 sm:pt-3 border-t border-border-strong text-xs sm:text-sm text-text-secondary">
            <p class="flex flex-wrap items-center gap-1">
                <font-awesome-icon :icon="['fas', 'shield-alt']" class="text-status-success" /> 
                <span>Verification Progress:</span>
                <span class="font-bold text-text-primary">{{ (blockchain.verificationprogress * 100).toFixed(4) }}%</span>
            </p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { type NodeInfo, type BlockChainInfo } from '@types';
import Tooltip from '@components/Tooltip.vue';

const props = defineProps<{
    node: NodeInfo;
    blockchain: BlockChainInfo;
    upTime: string;
}>();

const cleanedSubversion = computed(() => {
    const subver = props.node.subversion;
    return (!subver || subver === 'N/A') ? 'N/A' : subver.replace(/^\/|\/$/g, '').trim();
});
</script>
