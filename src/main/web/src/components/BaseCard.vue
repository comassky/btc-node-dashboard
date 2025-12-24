<template>
  <div
    class="dashboard-card"
    :class="[
      interactive ? 'dashboard-card-interactive' : '',
      statusClass
    ]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  status?: 'success' | 'warning' | 'error';
  interactive?: boolean;
}>(), {
  status: 'success',
  interactive: false
});

const statusClass = computed(() => {
  switch (props.status) {
    case 'error':
      return 'border-status-error hover:border-status-error hover:shadow-2xl';
    case 'warning':
      return 'border-status-warning hover:border-status-warning hover:shadow-2xl';
    case 'success':
    default:
      return 'border-status-success hover:border-status-success hover:shadow-2xl';
  }
});
</script>
