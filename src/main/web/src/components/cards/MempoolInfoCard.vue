<template>
  <BaseCard status="success" interactive>
    <h2 class="text-xl sm:text-2xl font-medium mb-4">
      <font-awesome-icon :icon="['fas', 'layer-group']" class="mr-2 text-accent" />
      Mempool Info
    </h2>
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
      <div>
        <div class="text-xs text-text-secondary mb-1">Transactions</div>
        <div class="text-lg font-bold">{{ mempoolInfo.size }}</div>
      </div>
      <div>
        <div class="text-xs text-text-secondary mb-1">Total Bytes</div>
        <div class="text-lg font-bold">{{ formatBytes(mempoolInfo.bytes) }}</div>
      </div>
      <div>
        <div class="text-xs text-text-secondary mb-1">Memory Usage</div>
        <div class="text-lg font-bold">{{ formatBytes(mempoolInfo.usage) }}</div>
      </div>
      <div>
        <div class="text-xs text-text-secondary mb-1">Max Mempool</div>
        <div class="text-lg font-bold">{{ formatBytes(mempoolInfo.maxmempool) }}</div>
      </div>
      <div>
        <div class="text-xs text-text-secondary mb-1">Min Fee (mempool)</div>
        <div class="text-lg font-bold">{{ mempoolInfo.mempoolminfee }} BTC/kB</div>
      </div>
      <div>
        <div class="text-xs text-text-secondary mb-1">Min Relay Fee</div>
        <div class="text-lg font-bold">{{ mempoolInfo.minrelaytxfee }} BTC/kB</div>
      </div>
      <div>
        <div class="text-xs text-text-secondary mb-1">Unbroadcast Count</div>
        <div class="text-lg font-bold">{{ mempoolInfo.unbroadcastcount }}</div>
      </div>
      <div>
        <div class="text-xs text-text-secondary mb-1">Total Fees</div>
        <div class="text-lg font-bold">{{ mempoolInfo.total_fee }} BTC</div>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import type { MempoolInfoResponse } from '../../types/MempoolInfoResponse';
import { formatBytes } from '../../utils/formatting';
import BaseCard from '@components/BaseCard.vue';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faLayerGroup } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

library.add(faLayerGroup);

const props = defineProps<{ mempoolInfo: MempoolInfoResponse }>();
</script>
