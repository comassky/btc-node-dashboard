<template>
  <span
    ref="triggerEl"
    class="relative inline-flex"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <slot></slot>
    <teleport to="body">
      <div
        v-if="text && isHovered"
        ref="tooltipEl"
        :style="tooltipStyle"
        :class="[
          'fixed px-3 py-2 bg-bg-card text-text-primary text-sm rounded-lg opacity-100 visible transition-all duration-200 whitespace-nowrap z-[99999] pointer-events-auto shadow-xl border border-border-strong backdrop-blur-sm',
        ]"
      >
        {{ text }}
        <div 
          :class="[
            'absolute border-4 border-transparent',
            position === 'bottom' ? 'bottom-full -mb-1 border-b-bg-card left-1/2 -translate-x-1/2' : position === 'top' ? 'top-full -mt-1 border-t-bg-card left-1/2 -translate-x-1/2' : position === 'left' ? 'left-full -ml-1 border-l-bg-card top-1/2 -translate-y-1/2' : position === 'right' ? 'right-full -mr-1 border-r-bg-card top-1/2 -translate-y-1/2' : ''
          ]"
        ></div>
      </div>
    </teleport>
  </span>
</template>

<script setup lang="ts">
import { ref, onUnmounted, watch, nextTick } from 'vue';

const props = withDefaults(
  defineProps<{
    text: string;
    position?: 'top' | 'bottom' | 'left' | 'right';
    horizontal?: 'left' | 'right' | 'center';
  }>(),
  {
    position: 'top',
    horizontal: 'center'
  }
);

const isHovered = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const tooltipEl = ref<HTMLElement | null>(null);
const tooltipStyle = ref<Record<string, string>>({});

function updateTooltipPosition() {
  if (!triggerEl.value || !tooltipEl.value) return;
  const triggerRect = triggerEl.value.getBoundingClientRect();
  const tooltipRect = tooltipEl.value.getBoundingClientRect();
  let top = 0, left = 0;

  switch (props.position) {
    case 'bottom':
      top = triggerRect.bottom + 8;
      left = triggerRect.left + triggerRect.width / 2 - tooltipRect.width / 2;
      break;
    case 'top':
      top = triggerRect.top - tooltipRect.height - 8;
      left = triggerRect.left + triggerRect.width / 2 - tooltipRect.width / 2;
      break;
    case 'left':
      top = triggerRect.top + triggerRect.height / 2 - tooltipRect.height / 2;
      left = triggerRect.left - tooltipRect.width - 8;
      break;
    case 'right':
      top = triggerRect.top + triggerRect.height / 2 - tooltipRect.height / 2;
      left = triggerRect.right + 8;
      break;
    default:
      top = triggerRect.bottom + 8;
      left = triggerRect.left + triggerRect.width / 2 - tooltipRect.width / 2;
  }

  tooltipStyle.value = {
    top: `${top}px`,
    left: `${left}px`,
  };
}

function handleMouseEnter() {
  isHovered.value = true;
  nextTick(updateTooltipPosition);
}
function handleMouseLeave() {
  isHovered.value = false;
}

watch(isHovered, (val) => {
  if (val) nextTick(updateTooltipPosition);
});

onUnmounted(() => {
  triggerEl.value = null;
  tooltipEl.value = null;
});
</script>
