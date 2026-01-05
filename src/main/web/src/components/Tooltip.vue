<template>
  <span ref="reference" class="relative inline-flex">
    <slot></slot>
  </span>
  <teleport to="body">
    <div
      v-if="text && isOpen"
      ref="floating"
      :style="floatingStyles"
      class="pointer-events-auto visible fixed z-[99999] whitespace-nowrap rounded-lg border border-border-strong bg-bg-card px-3 py-2 text-sm text-text-primary opacity-100 shadow-xl"
    >
      {{ text }}
      <div
        ref="arrowRef"
        :style="arrowStyles"
        class="absolute h-2 w-2 rotate-45 border border-transparent bg-bg-card"
      ></div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useFloating, autoUpdate, offset, flip, shift, arrow } from '@floating-ui/vue';
import { useElementHover } from '@vueuse/core';

const props = withDefaults(
  defineProps<{
    text: string;
    position?: 'top' | 'bottom' | 'left' | 'right';
  }>(),
  { position: 'top' }
);

const reference = ref<HTMLElement | null>(null);
const floating = ref<HTMLElement | null>(null);
const arrowRef = ref<HTMLElement | null>(null);

const isOpen = useElementHover(reference);

// Expose for unit tests
defineExpose({ isHovered: isOpen });

const { floatingStyles, middlewareData } = useFloating(reference, floating, {
  placement: props.position,
  middleware: [
    offset(8),
    flip(),
    shift({ padding: 8 }),
    arrow({ element: arrowRef }),
  ],
  whileElementsMounted: autoUpdate,
});

const arrowStyles = computed(() => {
  if (!middlewareData.value.arrow) return {};
  
  const { x, y } = middlewareData.value.arrow;
  const staticSide = {
    top: 'bottom',
    right: 'left',
    bottom: 'top',
    left: 'right',
  }[props.position];

  return {
    left: x != null ? `${x}px` : '',
    top: y != null ? `${y}px` : '',
    [staticSide as string]: '-4px',
  };
});
</script>
