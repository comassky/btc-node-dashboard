<template>
	<BaseCard status="success" interactive>
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
			<div class="w-full sm:w-auto flex flex-col items-end">
				<Tooltip :text="'Uptime: Time elapsed since the node started.'" position="bottom" horizontal="left">
					<div class="text-xl sm:text-2xl font-bold text-text-primary">
						{{ upTime }}
					</div>
				</Tooltip>
				<div class="text-xs sm:text-sm text-text-secondary">Uptime</div>
			</div>
		</div>
		<div class="mt-2 sm:mt-3 pt-2 sm:pt-3 border-t border-border-strong text-xs sm:text-sm text-text-secondary">
			<div class="flex flex-col sm:flex-row justify-between items-start sm:items-center w-full">
				<div class="flex-1">
					<Tooltip :text="'Verification progress: How much of the blockchain has been verified by your node.'" position="bottom" horizontal="left">
						<p class="flex flex-wrap items-center gap-1">
							<font-awesome-icon :icon="['fas', 'shield-alt']" class="text-status-success" />
							<span>Verification Progress:</span>
							<span class="font-bold text-text-primary">{{ (blockchain.verificationprogress * 100).toFixed(4) }}%</span>
						</p>
					</Tooltip>
					<div class="mt-2">
						<Tooltip :text="'Disk space used by the local blockchain (size_on_disk).'" position="bottom" horizontal="left">
							<p class="flex items-center gap-1">
								<span class="inline-flex items-center justify-center w-5 h-5">
									<font-awesome-icon :icon="['fas', 'hdd']" class="text-text-secondary text-base" />
								</span>
								<span>Size on disk:</span>
								<span class="font-bold text-text-primary">{{ formatSizeOnDisk(blockchain.size_on_disk) }}</span>
							</p>
						</Tooltip>
					</div>
				</div>
				<div class="flex gap-2 mt-4 sm:mt-0 sm:ml-6">
					<template v-for="net in node.networks" :key="net.name">
						<Tooltip :text="netLabel(net)" position="bottom" horizontal="left">
							<font-awesome-icon :icon="networkIcon(net.name)" :class="net.reachable ? 'text-status-success' : 'text-status-error'" />
						</Tooltip>
					</template>
				</div>
			</div>
		</div>
	</BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Tooltip from '@components/Tooltip.vue';
import BaseCard from '@components/BaseCard.vue';
import type { BlockChainInfo, NetworkInfoResponse } from '@types';
import { library } from '@fortawesome/fontawesome-svg-core';
import {
    faHardHat, faShieldAlt, faHdd, faNetworkWired, faProjectDiagram,
    faMask, faLayerGroup, faCloud, faQuestionCircle
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

library.add(
    faHardHat, faShieldAlt, faHdd, faNetworkWired, faProjectDiagram,
    faMask, faLayerGroup, faCloud, faQuestionCircle
);

const props = defineProps<{ node: NetworkInfoResponse; blockchain: BlockChainInfo; upTime: string }>();


function networkIcon(name: string) {
	switch (name) {
		case 'ipv4': return ['fas', 'network-wired'];
		case 'ipv6': return ['fas', 'project-diagram'];
		case 'onion': return ['fas', 'mask'];
		case 'i2p': return ['fas', 'layer-group'];
		case 'cloud': return ['fas', 'cloud'];
		default: return ['fas', 'question-circle'];
	}
}

function netLabel(net: { name: string; reachable: boolean }) {
	const labelMap: Record<string, string> = { ipv4: 'IPv4', ipv6: 'IPv6', onion: 'Tor', i2p: 'I2P' };
	const label = labelMap[net.name] || net.name;
	if (net.name === 'onion' && net.reachable && Array.isArray(props.node.localaddresses)) {
		const onionAddr = props.node.localaddresses.find(addr => addr.address.endsWith('.onion'));
		if (onionAddr) {
			return `${label} enabled : ${onionAddr.address}:${onionAddr.port}`;
		}
	}
	return `${label} ${net.reachable ? 'enabled' : 'disabled'}`;
}

const cleanedSubversion = computed(() => {
	const subver = props.node.subversion;
	return (!subver || subver === 'N/A') ? 'N/A' : subver.replace(/^\/|\/$/g, '').trim();
});
function formatSizeOnDisk(size?: number): string {
	if (!size && size !== 0) return 'N/A';
	if (size >= 1e12) return (size / 1e12).toFixed(2) + ' TB';
	if (size >= 1e9) return (size / 1e9).toFixed(2) + ' GB';
	if (size >= 1e6) return (size / 1e6).toFixed(2) + ' MB';
	if (size >= 1e3) return (size / 1e3).toFixed(2) + ' KB';
	return size + ' B';
}
</script>