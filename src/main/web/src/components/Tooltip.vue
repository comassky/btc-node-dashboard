<template>
  <div class="relative inline-flex group">
    <slot></slot>
    <div
      v-if="text"
      class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-bg-card text-text-primary text-sm rounded-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 whitespace-nowrap z-50 pointer-events-none shadow-xl border border-border-strong backdrop-blur-sm"
    >
      {{ text }}
      <div class="absolute top-full left-1/2 -translate-x-1/2 -mt-1 border-4 border-transparent border-t-bg-card"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  text: string;
}>();
</script>
