<template>
  <tr class="peer-table-row">
    <td class="td-cell">{{ peer.id }}</td>
    <td class="td-cell td-overflow">
      <Tooltip
        :text="`Show Bitnodes page for this node: ${peer.addr}`"
        position="bottom"
       
      >
        <a
          class="peer-link"
          :href="`https://bitnodes.io/nodes/${peer.addr.replace(':', '-')}/`"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ peer.addr }}
        </a>
      </Tooltip>
    </td>
    <td class="td-cell">
      <Tooltip :text="peer.subver || '[Empty]'" position="bottom">
        <span class="peer-subver">{{ peer.subver || '[Empty]' }}</span>
      </Tooltip>
    </td>
    <td class="td-cell">
      <Tooltip :text="`Full version: ${peer.version}`" position="bottom">
        <span>{{ peer.version }}</span>
      </Tooltip>
    </td>
    <td class="td-cell">{{ formatSecondsWithSuffix(peer.timeoffset) }}</td>
    <td class="td-cell">
      <Tooltip
        :text="peer.conntime ? `Connected at: ${formatTimestampToLocale(peer.conntime)}` : 'N/A'"
        position="bottom"
       
      >
        <span>{{ formatRelativeTimeSince(peer.conntime) }}</span>
      </Tooltip>
    </td>
    <td class="td-cell">
      <Tooltip :text="`Network type: ${peer.network || 'N/A'}`" position="bottom">
        <span>{{ peer.network || 'N/A' }}</span>
      </Tooltip>
    </td>
    <td
      class="td-cell font-medium"
      :class="[`text-${type === 'inbound' ? 'status-success' : 'accent'}`]"
      :title="'Connection type: ' + peer.connection_type"
    >
      {{ peer.connection_type }}
    </td>
    <td class="td-cell">
      <Tooltip :text="`Raw ping: ${peer.minping ?? 'N/A'} s`" position="bottom">
        <span>{{ formatPingSmart(peer.minping) }}</span>
      </Tooltip>
    </td>
    <td class="td-cell">
      <Tooltip
        :text="formatBytesLocale(peer.bytesrecv) + ' Bytes'"
        position="bottom"
       
      >
        <span>{{ formatBytesIEC(peer.bytesrecv) }}</span>
      </Tooltip>
    </td>
    <td class="td-cell">
      <Tooltip
        :text="formatBytesLocale(peer.bytessent) + ' Bytes'"
        position="bottom"
       
      >
        <span>{{ formatBytesIEC(peer.bytessent) }}</span>
      </Tooltip>
    </td>
  </tr>
</template>

<script setup lang="ts">
import {
  formatSecondsWithSuffix,
  formatPingSmart,
  formatRelativeTimeSince,
  formatBytesIEC,
  formatTimestampToLocale,
  formatBytesLocale,
} from '@utils/formatting';
import Tooltip from '@components/Tooltip.vue';
import type { Peer } from '../types';
const props = defineProps<{ peer: Peer; type: 'inbound' | 'outbound' }>();
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
