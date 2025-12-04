<template>
    <div class="lg:col-span-2 mb-5">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">

            <StatCard title="Total Peers" :value="stats.totalPeers" :icon="['fas', 'user-friends']" color="status-success">
                <p class="mb-1"><font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-1" /> Inbound: {{ stats.inboundCount }}</p>
                <p class="mb-1"><font-awesome-icon :icon="['fas', 'sign-out-alt']" class="mr-1" /> Outbound: {{ stats.outboundCount }}</p>
            </StatCard>

            <StatCard title="Current Block" :value="blockchain.blocks" :icon="['fas', 'cubes']" color="status-warning">
                <p class="mb-1"><font-awesome-icon :icon="['fas', 'list-ol']" class="mr-1" /> Headers: <span
                        class="font-bold text-text-primary">{{ blockchain.headers }}</span></p>
                <p class="mb-1"><font-awesome-icon :icon="['far', 'clock']" class="mr-1" /> Time: <span
                        class="font-bold text-text-primary">{{ formatTimeSince(block.time) }} ago</span></p>
                <p class="mb-1"><font-awesome-icon :icon="['fas', 'exchange-alt']" class="mr-1" /> Tx Count: <span
                        class="font-bold text-text-primary">{{ block.ntx }}</span></p>
            </StatCard>

            <StatCard title="Node Details" :value="cleanedSubversion" :icon="['fas', 'hard-hat']" color="accent"
                      class="lg:col-span-2">
                <template #default>
                    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-end -mt-2 mb-3">
                        <div class="min-w-0 flex-shrink">
                            <div class="value text-2xl font-bold text-text-primary truncate" :title="cleanedSubversion">
                                {{ cleanedSubversion }}
                            </div>
                            <div class="detail text-sm text-text-secondary font-light">Protocol v{{ node.protocolVersion }}</div>
                        </div>
                        <div class="text-left sm:text-right mt-3 sm:mt-0 flex-shrink-0">
                            <div class="value text-2xl font-bold text-text-primary">{{ upTime }}</div>
                            <div class="detail text-sm text-text-secondary font-light">Uptime</div>
                        </div>
                    </div>
                    <div class="mt-3 pt-3 border-t border-border-strong text-sm text-text-secondary">
                        <p class="mb-1">
                            <font-awesome-icon :icon="['fas', 'shield-alt']" class="mr-1 text-status-success" /> Verification Progress:
                            <span class="font-bold text-text-primary ml-1">{{ (blockchain.verificationprogress * 100).toFixed(4) }}%</span>
                        </p>
                    </div>
                </template>
            </StatCard>
        </div>
    </div>
</template>

<script setup lang="ts">
import StatCard from './StatCard.vue';
import { computed } from 'vue';
import { GeneralStats, BlockChainInfo, NodeInfo, BlockInfo } from '../types';
import { formatTimeSince } from '../utils/formatting';

const props = defineProps<{
    stats: GeneralStats;
    blockchain: BlockChainInfo;
    node: NodeInfo;
    block: BlockInfo;
    upTime: string;
}>();

const cleanedSubversion = computed(() => {
    const subver = props.node.subversion;
    if (!subver || subver === 'N/A') return 'N/A';
    return subver.replace(/^\/|\/$/g, '').trim();
});
</script>