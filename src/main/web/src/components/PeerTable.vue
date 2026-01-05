import { formatSecondsWithSuffix, formatPingSmart, formatRelativeTimeSince, formatBytesIEC,
formatTimestampToLocale } from '@utils/formatting';
<template>
  <div>
    <h4
      class="mt-8 border-b border-border-strong pb-2 text-xl font-medium"
      :class="[`text-${headerColor}`]"
    >
      {{ type === 'inbound' ? 'Inbound Peers' : 'Outbound Peers' }} ({{ peers.length }})
    </h4>

    <!-- Averages above the table -->
    <div
      class="bg-status-success/10 my-6 flex flex-wrap gap-8 rounded-lg border border-border-strong p-4 shadow-sm"
    >
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Ping</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          peerAverages.minping !== null ? formatPingSmart(peerAverages.minping) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Received</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          peerAverages.bytesrecv !== null ? formatBytesIEC(peerAverages.bytesrecv) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Sent</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          peerAverages.bytessent !== null ? formatBytesIEC(peerAverages.bytessent) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Time Offset</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          peerAverages.timeoffset !== null ? formatSecondsWithSuffix(peerAverages.timeoffset) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Connection Time</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          peerAverages.conntime !== null ? formatRelativeTimeSince(peerAverages.conntime) : 'N/A'
        }}</span>
      </div>
    </div>
    <div class="peer-table-wrapper mt-4 overflow-x-auto rounded-lg border border-border-strong">
      <table class="peer-table w-full text-sm">
        <thead>
          <tr class="bg-border-strong/50 whitespace-nowrap">
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('id')"
            >
              ID <span v-if="sortKey === 'id'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('addr')"
            >
              Address <span v-if="sortKey === 'addr'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('subver')"
            >
              Software (SubVer)
              <span v-if="sortKey === 'subver'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('version')"
            >
              Version
              <span v-if="sortKey === 'version'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('timeoffset')"
            >
              Time Offset
              <span v-if="sortKey === 'timeoffset'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('conntime')"
            >
              Connection Time
              <span v-if="sortKey === 'conntime'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('network')"
            >
              Network
              <span v-if="sortKey === 'network'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('connection_type')"
            >
              Type
              <span v-if="sortKey === 'connection_type'">{{
                sortOrder === 'asc' ? '↑' : '↓'
              }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('minping')"
            >
              Ping <span v-if="sortKey === 'minping'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('bytesrecv')"
            >
              <font-awesome-icon :icon="['fas', 'arrow-down']" class="text-status-success" />
              Received
              <span v-if="sortKey === 'bytesrecv'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="cursor-pointer p-4 text-left font-semibold uppercase text-text-secondary"
              @click="setSort('bytessent')"
            >
              <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-accent" /> Sent
              <span v-if="sortKey === 'bytessent'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
          </tr>
        </thead>
        <tbody>
          <!-- Peer Rows -->
          <PeerTableRow
            v-for="peer in sortedPeers"
            :key="type + '-' + peer.id"
            :peer="peer"
            :type="type"
          />
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, toRef } from 'vue';
import { useSorted } from '@vueuse/core';
import PeerTableRow from './PeerTableRow.vue';
import type { Peer } from '../types';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faArrowDown, faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import {
  formatBytesIEC,
  formatPingSmart,
  formatSecondsWithSuffix,
  formatRelativeTimeSince,
} from '@/utils/formatting';

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

// Use VueUse's useSorted for reactive sorting
const sortedPeers = useSorted(
  toRef(props, 'peers'),
  (a, b) => {
    const key = sortKey.value;
    const valA = a[key];
    const valB = b[key];
    
    if (valA == null && valB == null) return 0;
    if (valA == null) return 1;
    if (valB == null) return -1;
    
    let comparison = 0;
    if (typeof valA === 'number' && typeof valB === 'number') {
      comparison = valA - valB;
    } else {
      comparison = String(valA).localeCompare(String(valB));
    }
    
    return sortOrder.value === 'asc' ? comparison : -comparison;
  }
);

// Averages for relevant numeric columns - optimized single-pass calculation

function average(arr: number[]): number | null {
  return arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : null;
}

const peerAverages = computed(() => {
  const stats = { minping: [] as number[], bytesrecv: [] as number[], bytessent: [] as number[], timeoffset: [] as number[], conntime: [] as number[] };
  
  for (const peer of props.peers) {
    if (typeof peer.minping === 'number' && !isNaN(peer.minping)) stats.minping.push(peer.minping);
    if (typeof peer.bytesrecv === 'number' && !isNaN(peer.bytesrecv)) stats.bytesrecv.push(peer.bytesrecv);
    if (typeof peer.bytessent === 'number' && !isNaN(peer.bytessent)) stats.bytessent.push(peer.bytessent);
    if (typeof peer.timeoffset === 'number' && !isNaN(peer.timeoffset)) stats.timeoffset.push(peer.timeoffset);
    if (typeof peer.conntime === 'number' && !isNaN(peer.conntime)) stats.conntime.push(peer.conntime);
  }
  
  return {
    minping: average(stats.minping),
    bytesrecv: average(stats.bytesrecv),
    bytessent: average(stats.bytessent),
    timeoffset: average(stats.timeoffset),
    conntime: average(stats.conntime),
  };
});
</script>
