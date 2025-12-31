<template>
  <div
    class="dashboard-card"
    :class="[interactive ? 'dashboard-card-interactive' : '', statusClass]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    status?: 'success' | 'warning' | 'error';
    interactive?: boolean;
  }>(),
  {
    status: 'success',
    interactive: false,
  }
);

const statusClasses = {
  error: 'border-status-error bg-status-error/20 hover:border-status-error hover:shadow-2xl',
  warning: 'border-status-warning bg-status-warning/20 hover:border-status-warning hover:shadow-2xl',
  success: 'border-status-success bg-status-success/20 hover:border-status-success hover:shadow-2xl',
};

const statusClass = computed(() => statusClasses[props.status] || statusClasses.success);
</script>
