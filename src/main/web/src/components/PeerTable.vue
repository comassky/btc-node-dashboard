<template>
    <div>
        <h4
            class="text-xl font-medium mt-8 border-b border-border-strong pb-2"
            :class="[`text-${headerColor}`]"
        >
            {{ type === 'inbound' ? 'Inbound Peers' : 'Outbound Peers' }} ({{ peers.length }})
        </h4>

        <div class="peer-table-wrapper border border-border-strong rounded-lg mt-4 shadow-inner">
            <table class="peer-table w-full text-sm">
                <thead>
                    <tr class="bg-border-strong/50 whitespace-nowrap">
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('id')">
                            ID <span v-if="sortKey==='id'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('addr')">
                            Address <span v-if="sortKey==='addr'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('subver')">
                            Software (SubVer) <span v-if="sortKey==='subver'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('version')">
                            Version <span v-if="sortKey==='version'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('timeoffset')">
                            Time Offset <span v-if="sortKey==='timeoffset'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('conntime')">
                            Connection Time <span v-if="sortKey==='conntime'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('network')">
                            Network <span v-if="sortKey==='network'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('connection_type')">
                            Type <span v-if="sortKey==='connection_type'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('minping')">
                            Ping <span v-if="sortKey==='minping'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('bytesrecv')">
                            <font-awesome-icon :icon="['fas', 'arrow-down']" class="text-status-success" /> Received <span v-if="sortKey==='bytesrecv'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase cursor-pointer" @click="setSort('bytessent')">
                            <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-accent" /> Sent <span v-if="sortKey==='bytessent'">{{ sortOrder==='asc' ? '↑' : '↓' }}</span>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="peer in sortedPeers" :key="type + '-' + peer.id"
                        class="border-b border-border-strong/70 hover:bg-bg-card/70 transition duration-150 whitespace-nowrap">
                        <td class="p-4 font-light">{{ peer.id }}</td>
                        <td class="p-4 font-light overflow-visible">
                            <Tooltip :text="`Show Bitnodes page for this node: ${peer.addr}`" position="bottom" horizontal="left">
                                <a
                                    class="max-w-[150px] truncate inline-block text-white hover:text-orange-500 transition-colors"
                                    :href="`https://bitnodes.io/nodes/${peer.addr.replace(':', '-')}/`"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                >
                                    {{ peer.addr }}
                                </a>
                            </Tooltip>
                        </td>
                        <td class="p-4 font-light">
                            <Tooltip :text="peer.subver || '[Empty]'" position="bottom" horizontal="left">
                                <span class="max-w-[150px] truncate inline-block">{{ peer.subver || '[Empty]' }}</span>
                            </Tooltip>
                        </td>
                        <td class="p-4 font-light">{{ peer.version }}</td>
                        <td class="p-4 font-light">{{ formatTimeOffset(peer.timeoffset) }}</td>
                        <td class="p-4 font-light">{{ formatTimeSince(peer.conntime) }} ago</td>
                        <td class="p-4 font-light">{{ peer.network || 'N/A' }}</td>
                        <td class="p-4 font-medium" :class="[`text-${type === 'inbound' ? 'status-success' : 'accent'}`]"
                            :title="'Connection type: ' + peer.connection_type">{{ peer.connection_type }}</td>
                        <td class="p-4 font-light">{{ formatPing(peer.minping) }}</td>
                        <td class="p-4 font-light" :title="peer.bytesrecv + ' Bytes'">{{ formatBytes(peer.bytesrecv) }}
                        </td>
                        <td class="p-4 font-light" :title="peer.bytessent + ' Bytes'">{{ formatBytes(peer.bytessent) }}
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { type Peer } from '@types';
import { formatBytes, formatTimeOffset, formatTimeSince, formatPing } from '@utils/formatting';
import Tooltip from '@components/Tooltip.vue';

const props = defineProps<{
        peers: Peer[];
        type: 'inbound' | 'outbound';
}>();

const headerColor = props.type === 'inbound' ? 'status-success' : 'accent';

const sortKey = ref<'id'|'addr'|'subver'|'version'|'timeoffset'|'conntime'|'network'|'connection_type'|'minping'|'bytesrecv'|'bytessent'>('id');
const sortOrder = ref<'asc' | 'desc'>('asc');

function setSort(key: typeof sortKey.value) {
    if (sortKey.value === key) {
        sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
    } else {
        sortKey.value = key;
        sortOrder.value = 'asc';
    }
}

const sortedPeers = computed(() => {
    const key = sortKey.value;
    return [...props.peers].sort((a, b) => {
        const aVal = a[key];
        const bVal = b[key];
        if (aVal == null && bVal == null) return 0;
        if (aVal == null) return 1;
        if (bVal == null) return -1;
        if (typeof aVal === 'number' && typeof bVal === 'number') {
            return sortOrder.value === 'asc' ? aVal - bVal : bVal - aVal;
        }
        return sortOrder.value === 'asc'
            ? String(aVal).localeCompare(String(bVal))
            : String(bVal).localeCompare(String(aVal));
    });
});
</script>