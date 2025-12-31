<template>
  <div class="flex items-center justify-center">
    <svg
      :class="[`animate-spin`, sizeClass, colorClass]"
      :width="sizePx"
      :height="sizePx"
      viewBox="0 0 128 128"
      xmlns="http://www.w3.org/2000/svg"
      role="status"
      aria-label="Loading..."
    >
      <circle cx="64" cy="64" r="60" :fill="bgColor" :stroke="accentColor" stroke-width="8" />
      <g>
        <circle cx="64" cy="64" r="48" :fill="accentColor" />
        <text
          x="64"
          y="80"
          text-anchor="middle"
          font-size="56"
          font-family="Arial, Helvetica, sans-serif"
          :fill="bgColor"
          font-weight="bold"
        >
          ₿
        </text>
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    size?: number | string; // px or tailwind class
    accentColor?: string;
    bgColor?: string;
    colorClass?: string;
  }>(),
  {
    size: 48,
    accentColor: '#f7931a',
    bgColor: '#fff',
    colorClass: 'text-accent',
  }
);

const sizePx = computed(() => (typeof props.size === 'number' ? props.size : 48));
const sizeClass = computed(() => (typeof props.size === 'string' ? props.size : `h-12 w-12`));
</script>

<style scoped>
svg[role='status'] {
  display: inline-block;
}
</style>
