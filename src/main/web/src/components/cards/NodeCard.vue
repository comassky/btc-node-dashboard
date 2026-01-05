<template>
  <BaseCard status="success" interactive>
    <div class="flex items-center justify-between">
      <div class="text-accent text-2xl sm:text-3xl">
        <IconHelmetSafety />
      </div>
      <div class="text-text-secondary text-xs font-medium uppercase">Node Details</div>
    </div>
    <div
      class="mt-3 flex flex-col items-start justify-between gap-3 sm:flex-row sm:items-end sm:gap-0"
    >
      <div class="w-full min-w-0 sm:w-auto">
        <Tooltip
          :text="'Node software version. This identifies the implementation and version your node is running.'"
          position="bottom"
        >
          <div class="text-text-primary truncate text-xl font-bold sm:text-2xl">
            {{ cleanedSubversion }}
          </div>
        </Tooltip>
        <div class="text-text-secondary text-xs sm:text-sm">
          Protocol v{{ node.protocolversion }}
        </div>
      </div>
      <div class="flex w-full flex-col items-end sm:w-auto">
        <Tooltip :text="'Uptime: Time elapsed since the node started.'" position="bottom">
          <div class="text-text-primary text-xl font-bold sm:text-2xl">
            {{ formatUptime(upTime) }}
          </div>
        </Tooltip>
        <div class="text-text-secondary text-xs sm:text-sm">Uptime</div>
      </div>
    </div>
    <div
      class="border-border-strong text-text-secondary mt-2 border-t pt-2 text-xs sm:mt-3 sm:pt-3 sm:text-sm"
    >
      <div class="flex w-full flex-col items-start justify-between sm:flex-row sm:items-center">
        <div class="flex-1">
          <Tooltip
            :text="'Verification progress: How much of the blockchain has been verified by your node.'"
            position="bottom"
          >
            <p class="flex flex-wrap items-center gap-1">
              <IconShieldHalved class="text-status-success" />
              <span>Verification Progress:</span>
              <span class="text-text-primary font-bold"
                >{{ (blockchain.verificationprogress * 100).toFixed(4) }}%</span
              >
            </p>
          </Tooltip>
          <div class="mt-2">
            <Tooltip
              :text="'Disk space used by the local blockchain (size_on_disk).'"
              position="bottom"
            >
              <p class="flex items-center gap-1">
                <span class="inline-flex h-5 w-5 items-center justify-center">
                  <IconHardDrive class="text-text-secondary text-base" />
                </span>
                <span>Size on disk:</span>
                <span class="text-text-primary font-bold">{{
                  filesize(blockchain.size_on_disk, { standard: 'iec', base: 2 })
                }}</span>
              </p>
            </Tooltip>
          </div>
        </div>
        <div class="mt-4 flex gap-2 sm:mt-0 sm:ml-6">
          <template v-for="net in node.networks" :key="net.name">
            <Tooltip :text="netLabel(net)" position="bottom">
              <component
                :is="networkIconComponent(net.name)"
                :class="net.reachable ? 'text-status-success' : 'text-status-error'"
              />
            </Tooltip>
          </template>
        </div>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import Tooltip from '@components/Tooltip.vue';
import BaseCard from '@components/BaseCard.vue';

import { computed } from 'vue';
import { filesize } from 'filesize';
import { intervalToDuration } from 'date-fns';
import type { BlockChainInfo, NetworkInfoResponse } from '@/types';
import {
  IconHelmetSafety,
  IconShieldHalved,
  IconHardDrive,
  IconNetworkWired,
  IconEthernet,
  IconRoute,
  IconDiagramProject,
  IconTorProject,
  IconLayerGroup,
  IconCloud,
  IconCircleQuestion,
} from '@/icons';

const props = defineProps<{
  node: NetworkInfoResponse;
  blockchain: BlockChainInfo;
  upTime: number;
}>();

function formatUptime(totalSeconds: number): string {
  if (typeof totalSeconds !== 'number' || isNaN(totalSeconds) || totalSeconds < 0) return 'N/A';
  const durationObj = intervalToDuration({ start: 0, end: totalSeconds * 1000 });
  // Format: "Xd, HH:mm:ss" (jours, heures, minutes, secondes)
  const days = durationObj.days ? `${durationObj.days}d, ` : '';
  const hours = String(durationObj.hours ?? 0).padStart(2, '0');
  const minutes = String(durationObj.minutes ?? 0).padStart(2, '0');
  const seconds = String(durationObj.seconds ?? 0).padStart(2, '0');
  return `${days}${hours}:${minutes}:${seconds}`;
}

const networkIconsMap: Record<string, any> = {
  ipv4: IconEthernet,
  ipv6: IconRoute,
  onion: IconTorProject,
  i2p: IconLayerGroup,
  cjdns: IconCloud,
};
const defaultNetworkIconComponent = IconCircleQuestion;

function networkIconComponent(name: string) {
  return networkIconsMap[name] || defaultNetworkIconComponent;
}

function netLabel(net: { name: string; reachable: boolean }) {
  const labelMap: Record<string, string> = { ipv4: 'IPv4', ipv6: 'IPv6', onion: 'Tor', i2p: 'I2P' };
  const label = labelMap[net.name] || net.name;
  if (net.name === 'onion' && net.reachable && Array.isArray(props.node.localaddresses)) {
    const onionAddr = props.node.localaddresses.find((addr) => addr.address.endsWith('.onion'));
    if (onionAddr) {
      return `${label} enabled : ${onionAddr.address}:${onionAddr.port}`;
    }
  }
  return `${label} ${net.reachable ? 'enabled' : 'disabled'}`;
}

const cleanedSubversion = computed(() => {
  const subver = props.node.subversion;
  return !subver || subver === 'N/A' ? 'N/A' : subver.replace(/^\/|\/$/g, '').trim();
});
</script>
