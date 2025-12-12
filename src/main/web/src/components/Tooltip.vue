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
          'fixed px-3 py-2 bg-bg-card text-text-primary text-sm rounded-lg opacity-100 visible whitespace-nowrap z-[99999] pointer-events-auto shadow-xl border border-border-strong backdrop-blur-sm',
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



const props = withDefaults(defineProps<{
  text: string;
  position?: 'top' | 'bottom' | 'left' | 'right';
}>(), { position: 'top' });



const isHovered = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const tooltipEl = ref<HTMLElement | null>(null);
const tooltipStyle = ref<{ top: string; left: string; maxWidth: string }>({ top: '0px', left: '0px', maxWidth: '100vw' });



function getTooltipCoords(position: string, triggerRect: DOMRect, tooltipRect: DOMRect, padding: number) {
  const centerX = triggerRect.left + triggerRect.width / 2 - tooltipRect.width / 2;
  const centerY = triggerRect.top + triggerRect.height / 2 - tooltipRect.height / 2;
  switch (position) {
    case 'bottom':
      return { top: triggerRect.bottom + padding, left: centerX };
    case 'top':
      return { top: triggerRect.top - tooltipRect.height - padding, left: centerX };
    case 'left':
      return { top: centerY, left: triggerRect.left - tooltipRect.width - padding };
    case 'right':
      return { top: centerY, left: triggerRect.right + padding };
    default:
      return { top: triggerRect.bottom + padding, left: centerX };
  }
}

function updateTooltipPosition() {
  if (!triggerEl.value || !tooltipEl.value) return;
  const triggerRect = triggerEl.value.getBoundingClientRect();
  const tooltipRect = tooltipEl.value.getBoundingClientRect();
  const padding = 8;
  const vw = window.innerWidth;
  const vh = window.innerHeight;
  let position = props.position;
  let { top, left } = getTooltipCoords(position, triggerRect, tooltipRect, padding);

  // Responsive: switch position if out of viewport
  if (top < padding && position === 'top') {
    position = 'bottom';
    ({ top, left } = getTooltipCoords(position, triggerRect, tooltipRect, padding));
  } else if (top + tooltipRect.height > vh - padding && position === 'bottom') {
    position = 'top';
    ({ top, left } = getTooltipCoords(position, triggerRect, tooltipRect, padding));
  }

  // Clamp to viewport
  top = Math.max(padding, Math.min(top, vh - tooltipRect.height - padding));
  left = Math.max(padding, Math.min(left, vw - tooltipRect.width - padding));

  tooltipStyle.value = {
    top: `${top}px`,
    left: `${left}px`,
    maxWidth: `calc(100vw - ${2 * padding}px)`
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
