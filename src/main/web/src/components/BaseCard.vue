<template>
  <div
    class="dashboard-card"
    :class="[interactive ? 'dashboard-card-interactive' : '', statusClass]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
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
  error: 'border-status-error bg-status-error/10 hover:border-status-error hover:shadow-2xl',
  warning:
    'border-status-warning bg-status-warning/10 hover:border-status-warning hover:shadow-2xl',
  success: 'border-l-4 hover:shadow-2xl',
};

const statusClass = computed(() => statusClasses[props.status] || statusClasses.success);
</script>
