<template>
  <div class="relative inline-flex group">
    <slot></slot>
    <div
      v-if="text"
      :class="[
        'absolute px-3 py-2 bg-bg-card text-text-primary text-sm rounded-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 whitespace-nowrap z-[9999] pointer-events-none shadow-xl border border-border-strong backdrop-blur-sm',
        position === 'bottom' ? 'top-full mt-2' : 'bottom-full mb-2',
        horizontal === 'left' ? 'right-0' : horizontal === 'right' ? 'left-0' : 'left-1/2 -translate-x-1/2'
      ]"
    >
      {{ text }}
      <div 
        :class="[
          'absolute border-4 border-transparent',
          position === 'bottom' ? 'bottom-full -mb-1 border-b-bg-card' : 'top-full -mt-1 border-t-bg-card',
          horizontal === 'left' ? 'right-3' : horizontal === 'right' ? 'left-3' : 'left-1/2 -translate-x-1/2'
        ]"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    text: string;
    position?: 'top' | 'bottom';
    horizontal?: 'left' | 'right' | 'center';
  }>(),
  {
    position: 'top',
    horizontal: 'center'
  }
);
</script>
