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
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">ID</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Address</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Software (SubVer)</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Version</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Time Offset</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Connection Time</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Network</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Type</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">Ping</th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">
                            <font-awesome-icon :icon="['fas', 'arrow-down']" class="text-status-success" /> Received
                        </th>
                        <th class="p-4 text-left font-semibold text-text-secondary uppercase">
                            <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-accent" /> Sent
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="peer in peers" :key="type + '-' + peer.id"
                        class="border-b border-border-strong/70 hover:bg-bg-card/70 transition duration-150 whitespace-nowrap">
                        <td class="p-4 font-light">{{ peer.id }}</td>
                        <td class="p-4 font-light" :title="peer.addr">{{ peer.addr }}</td>
                        <td class="p-4 font-light max-w-[150px] truncate" :title="peer.subver">{{ peer.subver || '[Empty]' }}</td>
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
import { type Peer } from '../types';
import { formatBytes, formatTimeOffset, formatTimeSince, formatPing } from '../utils/formatting';

const props = defineProps<{
    peers: Peer[];
    type: 'inbound' | 'outbound';
}>();

const headerColor = props.type === 'inbound' ? 'status-success' : 'accent';
</script>