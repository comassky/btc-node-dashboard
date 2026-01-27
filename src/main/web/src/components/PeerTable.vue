<template>
  <div>
    <h4
      class="border-border-strong mt-8 border-b pb-2 text-xl font-medium"
      :class="[`text-${headerColor}`]"
    >
      {{ type === 'inbound' ? 'Inbound Peers' : 'Outbound Peers' }} ({{ peers.length }})
    </h4>

    <!-- Averages above the table -->
    <div
      class="border-border-strong bg-bg-card my-6 flex flex-wrap gap-8 rounded-lg border p-4 shadow-sm"
    >
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="text-text-secondary mb-1 text-xs">Average Ping</span>
        <span class="text-status-success text-text-primary text-lg font-semibold">{{
          peerAverages.minping !== null ? formatPingSmart(peerAverages.minping) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="text-text-secondary mb-1 text-xs">Average Received</span>
        <span class="text-status-success text-text-primary text-lg font-semibold">{{
          peerAverages.bytesrecv !== null ? formatBytesIEC(peerAverages.bytesrecv) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="text-text-secondary mb-1 text-xs">Average Sent</span>
        <span class="text-status-success text-text-primary text-lg font-semibold">{{
          peerAverages.bytessent !== null ? formatBytesIEC(peerAverages.bytessent) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="text-text-secondary mb-1 text-xs">Average Time Offset</span>
        <span class="text-status-success text-text-primary text-lg font-semibold">{{
          peerAverages.timeoffset !== null
            ? formatSecondsWithSuffix(peerAverages.timeoffset)
            : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="text-text-secondary mb-1 text-xs">Average Connection Time</span>
        <span class="text-status-success text-text-primary text-lg font-semibold">{{
          peerAverages.conntime !== null ? formatRelativeTimeSince(peerAverages.conntime) : 'N/A'
        }}</span>
      </div>
    </div>
    <div class="peer-table-wrapper border-border-strong mt-4 overflow-x-auto rounded-lg border">
      <table class="peer-table w-full text-sm">
        <thead>
          <tr class="bg-border-strong/50 whitespace-nowrap">
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('id')"
            >
              ID <span v-if="sortKey === 'id'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('addr')"
            >
              Address <span v-if="sortKey === 'addr'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('subver')"
            >
              Software (SubVer)
              <span v-if="sortKey === 'subver'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('version')"
            >
              Version
              <span v-if="sortKey === 'version'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('timeoffset')"
            >
              Time Offset
              <span v-if="sortKey === 'timeoffset'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('conntime')"
            >
              Connection Time
              <span v-if="sortKey === 'conntime'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('network')"
            >
              Network
              <span v-if="sortKey === 'network'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('connection_type')"
            >
              Type
              <span v-if="sortKey === 'connection_type'">{{
                sortOrder === 'asc' ? '↑' : '↓'
              }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('minping')"
            >
              Ping <span v-if="sortKey === 'minping'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('bytesrecv')"
            >
              <span class="inline-flex items-center gap-1">
                <IconArrowDown class="text-status-success" />
                Received
              </span>
              <span v-if="sortKey === 'bytesrecv'">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th
              class="text-text-secondary cursor-pointer p-4 text-left font-semibold uppercase"
              @click="setSort('bytessent')"
            >
              <span class="inline-flex items-center gap-1">
                <IconArrowUp class="text-accent" />
                Sent
              </span>
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
import PeerTableRow from './PeerTableRow.vue';
import type { Peer } from '../types';
import {
  formatBytesIEC,
  formatPingSmart,
  formatSecondsWithSuffix,
  formatRelativeTimeSince,
} from '@/utils/formatting';
import { IconArrowDown, IconArrowUp } from '@/icons';

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
const sortedPeers = useSorted(toRef(props, 'peers'), (a, b) => {
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
});

// Averages for relevant numeric columns - optimized single-pass calculation

interface PeerStats {
  minping: number[];
  bytesrecv: number[];
  bytessent: number[];
  timeoffset: number[];
  conntime: number[];
}

type StatKey = keyof PeerStats;

function average(arr: number[]): number | null {
  return arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : null;
}

function isValidNumber(value: any): value is number {
  return typeof value === 'number' && !isNaN(value);
}

const peerAverages = computed(() => {
  const stats: PeerStats = {
    minping: [],
    bytesrecv: [],
    bytessent: [],
    timeoffset: [],
    conntime: [],
  };

  const keys: StatKey[] = ['minping', 'bytesrecv', 'bytessent', 'timeoffset', 'conntime'];

  // Single-pass collection
  for (const peer of props.peers) {
    for (const key of keys) {
      const value = peer[key];
      if (isValidNumber(value)) {
        stats[key].push(value);
      }
    }
  }

  // Calculate all averages
  return {
    minping: average(stats.minping),
    bytesrecv: average(stats.bytesrecv),
    bytessent: average(stats.bytessent),
    timeoffset: average(stats.timeoffset),
    conntime: average(stats.conntime),
  };
});
</script>
