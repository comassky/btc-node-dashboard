<template>
  <BaseCard status="success" interactive>
    <h2 class="mb-4 text-xl font-medium sm:text-2xl">
      <font-awesome-icon :icon="['fas', 'layer-group']" class="text-accent mr-2" />
      Mempool Info
    </h2>
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-4">
      <div>
        <div class="text-text-secondary mb-1 text-xs">Transactions</div>
        <div class="text-lg font-bold">{{ mempoolInfo.size }}</div>
      </div>
      <div>
        <div class="text-text-secondary mb-1 text-xs">Total Bytes</div>
        <div class="text-lg font-bold">{{ formatBytesIEC(mempoolInfo.bytes) }}</div>
      </div>
      <div>
        <div class="text-text-secondary mb-1 text-xs">Memory Usage</div>
        <div class="text-lg font-bold">{{ formatBytesIEC(mempoolInfo.usage) }}</div>
      </div>
      <div>
        <div class="text-text-secondary mb-1 text-xs">Max Mempool</div>
        <div class="text-lg font-bold">{{ formatBytesIEC(mempoolInfo.maxmempool) }}</div>
      </div>
      <div>
        <div class="text-text-secondary mb-1 text-xs">Min Fee (mempool)</div>
        <div class="text-lg font-bold">{{ mempoolInfo.mempoolminfee }} BTC/kB</div>
      </div>
      <div>
        <div class="text-text-secondary mb-1 text-xs">Min Relay Fee</div>
        <div class="text-lg font-bold">{{ mempoolInfo.minrelaytxfee }} BTC/kB</div>
      </div>
      <div>
        <div class="text-text-secondary mb-1 text-xs">Unbroadcast Count</div>
        <div class="text-lg font-bold">{{ mempoolInfo.unbroadcastcount }}</div>
      </div>
      <div>
        <div class="text-text-secondary mb-1 text-xs">Total Fees</div>
        <div class="text-lg font-bold">{{ mempoolInfo.total_fee }} BTC</div>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { formatBytesIEC } from '@utils/formatting';
import BaseCard from '@components/BaseCard.vue';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faLayerGroup } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import type { MempoolInfoResponse } from '@types';

library.add(faLayerGroup);

const props = defineProps<{ mempoolInfo: MempoolInfoResponse }>();
</script>
