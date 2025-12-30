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
          avgMinPing !== null ? formatPingSmart(avgMinPing) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Received</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          avgBytesRecv !== null ? formatBytesIEC(avgBytesRecv) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Sent</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          avgBytesSent !== null ? formatBytesIEC(avgBytesSent) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Time Offset</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          avgTimeOffset !== null ? formatSecondsWithSuffix(avgTimeOffset) : 'N/A'
        }}</span>
      </div>
      <div class="flex min-w-[120px] flex-col items-start">
        <span class="mb-1 text-xs text-text-secondary">Average Connection Time</span>
        <span class="text-lg font-semibold text-status-success text-text-primary">{{
          avgConnTime !== null ? formatRelativeTimeSince(avgConnTime) : 'N/A'
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
import { ref, computed } from 'vue';
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

function compare(a: unknown, b: unknown, order: 'asc' | 'desc') {
  if (a == null && b == null) return 0;
  if (a == null) return 1;
  if (b == null) return -1;
  if (typeof a === 'number' && typeof b === 'number') {
    return order === 'asc' ? a - b : b - a;
  }
  return order === 'asc' ? String(a).localeCompare(String(b)) : String(b).localeCompare(String(a));
}

const sortedPeers = computed(() => {
  const key = sortKey.value;
  const order = sortOrder.value;
  return [...props.peers].sort((a, b) => compare(a[key], b[key], order));
});

// Averages for relevant numeric columns

function average(arr: Array<number | null | undefined>): number | null {
  const nums = arr.filter((v): v is number => typeof v === 'number' && !isNaN(v));
  return nums.length ? nums.reduce((a, b) => a + b, 0) / nums.length : null;
}

const avgMinPing = computed(() => average(props.peers.map((p) => p.minping)));
const avgBytesRecv = computed(() => average(props.peers.map((p) => p.bytesrecv)));
const avgBytesSent = computed(() => average(props.peers.map((p) => p.bytessent)));
const avgTimeOffset = computed(() => average(props.peers.map((p) => p.timeoffset)));
const avgConnTime = computed(() => average(props.peers.map((p) => p.conntime)));
</script>
