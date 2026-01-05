<template>
  <tr class="peer-table-row">
    <td class="td-cell" v-once>{{ peer.id }}</td>
    <td class="td-cell td-overflow">
      <a
        class="peer-link"
        :href="`https://bitnodes.io/nodes/${peer.addr.replace(':', '-')}/`"
        target="_blank"
        rel="noopener noreferrer"
        :title="`Show Bitnodes page for this node: ${peer.addr}`"
      >
        {{ peer.addr }}
      </a>
    </td>
    <td class="td-cell" v-once>
      <span class="peer-subver" :title="peer.subver || '[Empty]'">{{
        peer.subver || '[Empty]'
      }}</span>
    </td>
    <td class="td-cell" v-once :title="`Full version: ${peer.version}`">{{ peer.version }}</td>
    <td class="td-cell">{{ formattedTimeOffset }}</td>
    <td class="td-cell" :title="peer.conntime ? `Connected at: ${formattedTimestamp}` : 'N/A'">
      {{ formattedConnTime }}
    </td>
    <td class="td-cell" :title="`Network type: ${peer.network || 'N/A'}`">
      {{ peer.network || 'N/A' }}
    </td>
    <td
      class="td-cell font-medium"
      :class="[`text-${type === 'inbound' ? 'status-success' : 'accent'}`]"
      :title="'Connection type: ' + peer.connection_type"
    >
      {{ peer.connection_type }}
    </td>
    <td class="td-cell" :title="`Raw ping: ${peer.minping ?? 'N/A'} s`">
      {{ formattedPing }}
    </td>
    <td class="td-cell" :title="formattedBytesRecvLocale + ' Bytes'">
      {{ formattedBytesRecv }}
    </td>
    <td class="td-cell" :title="formattedBytesSentLocale + ' Bytes'">
      {{ formattedBytesSent }}
    </td>
  </tr>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  formatSecondsWithSuffix,
  formatPingSmart,
  formatRelativeTimeSince,
  formatBytesIEC,
  formatTimestampToLocale,
  formatBytesLocale,
} from '@utils/formatting';
import type { Peer } from '../types';

const props = defineProps<{ peer: Peer; type: 'inbound' | 'outbound' }>();

// Memoize formatted values to avoid recalculating on every render
const formattedTimeOffset = computed(() => formatSecondsWithSuffix(props.peer.timeoffset));
const formattedConnTime = computed(() => formatRelativeTimeSince(props.peer.conntime));
const formattedTimestamp = computed(() => formatTimestampToLocale(props.peer.conntime));
const formattedPing = computed(() => formatPingSmart(props.peer.minping));
const formattedBytesRecv = computed(() => formatBytesIEC(props.peer.bytesrecv));
const formattedBytesSent = computed(() => formatBytesIEC(props.peer.bytessent));
const formattedBytesRecvLocale = computed(() => formatBytesLocale(props.peer.bytesrecv));
const formattedBytesSentLocale = computed(() => formatBytesLocale(props.peer.bytessent));
</script>

<style scoped>
/* Util classes for table row and cells */
.peer-table-row {
  border-bottom: 1px solid var(--border-strong);
  transition: background 0.15s;
  white-space: nowrap;
}
.peer-table-row:hover {
  background: var(--bg-card);
}
.td-cell {
  padding: 1rem;
  font-weight: 300;
}
.td-overflow {
  overflow: visible;
}
.peer-link {
  max-width: 150px;
  display: inline-block;
  color: var(--text-primary);
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
  transition: color 0.2s;
}
.peer-link:hover {
  color: var(--accent);
}
.peer-subver {
  max-width: 150px;
  display: inline-block;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}
</style>
