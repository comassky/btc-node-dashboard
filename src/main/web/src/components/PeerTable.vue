import { formatSecondsWithSuffix, formatPingSmart, formatRelativeTimeSince, formatBytesIEC, formatTimestampToLocale } from '@utils/formatting';
<template>
    <div>
        <h4
            class="text-xl font-medium mt-8 border-b border-border-strong pb-2"
            :class="[`text-${headerColor}`]"
        >
            {{ type === 'inbound' ? 'Inbound Peers' : 'Outbound Peers' }} ({{ peers.length }})
        </h4>

        <div class="peer-table-wrapper border border-border-strong rounded-lg mt-4 overflow-x-auto">
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
                    <PeerTableRow v-for="peer in sortedPeers" :key="type + '-' + peer.id" :peer="peer" :type="type" />
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import PeerTableRow from './PeerTableRow.vue';
import type { Peer } from '../types';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faArrowDown, faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

library.add(faArrowDown, faArrowUp);

const props = defineProps<{ peers: Peer[]; type: 'inbound' | 'outbound' }>();
const headerColor = props.type === 'inbound' ? 'status-success' : 'accent';
const sortKey = ref<keyof Peer>('id');
const sortOrder = ref<'asc' | 'desc'>('asc');

function setSort(key: keyof Peer) {
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
