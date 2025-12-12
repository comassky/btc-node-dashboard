<template>
    <div class="dashboard-card border-accent hover:shadow-2xl hover:border-accent lg:col-span-2">
        <div class="flex justify-between items-center">
            <div class="text-2xl sm:text-3xl text-accent">
                <font-awesome-icon :icon="['fas', 'hard-hat']" />
            </div>
            <div class="text-xs uppercase text-text-secondary font-medium">Node Details</div>
        </div>

        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-end mt-3 gap-3 sm:gap-0">
            <div class="min-w-0 w-full sm:w-auto">
                <Tooltip :text="'Node software version. This identifies the implementation and version your node is running.'" position="bottom" horizontal="left">
                    <div class="text-xl sm:text-2xl font-bold text-text-primary truncate">
                        {{ cleanedSubversion }}
                    </div>
                </Tooltip>
                <div class="text-xs sm:text-sm text-text-secondary">Protocol v{{ node.protocolversion }}</div>
            </div>

            <div class="w-full sm:w-auto">
                <Tooltip :text="'Uptime: Time elapsed since the node started.'" position="bottom" horizontal="left">
                    <div class="text-xl sm:text-2xl font-bold text-text-primary">
                        {{ upTime }}
                    </div>
                </Tooltip>
                <div class="text-xs sm:text-sm text-text-secondary">Uptime</div>
            </div>
        </div>
        
        <div class="mt-2 sm:mt-3 pt-2 sm:pt-3 border-t border-border-strong text-xs sm:text-sm text-text-secondary">
            <Tooltip :text="'Verification progress: How much of the blockchain has been verified by your node.'" position="bottom" horizontal="left">
                <p class="flex flex-wrap items-center gap-1">
                    <font-awesome-icon :icon="['fas', 'shield-alt']" class="text-status-success" /> 
                    <span>Verification Progress:</span>
                    <span class="font-bold text-text-primary">{{ (blockchain.verificationprogress * 100).toFixed(4) }}%</span>
                </p>
            </Tooltip>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Tooltip from '@components/Tooltip.vue';

const props = defineProps<{ node: any; blockchain: any; upTime: string }>();

const cleanedSubversion = computed(() => {
    const subver = props.node.subversion;
    return (!subver || subver === 'N/A') ? 'N/A' : subver.replace(/^\/|\/$/g, '').trim();
});
</script>
