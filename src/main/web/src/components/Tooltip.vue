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
          'pointer-events-auto visible fixed z-[99999] whitespace-nowrap rounded-lg border border-border-strong bg-bg-card px-3 py-2 text-sm text-text-primary opacity-100 shadow-xl',
        ]"
      >
        {{ text }}
        <div
          :class="[
            'absolute border-4 border-transparent',
            position === 'bottom'
              ? 'bottom-full left-1/2 -mb-1 -translate-x-1/2 border-b-bg-card'
              : position === 'top'
                ? 'left-1/2 top-full -mt-1 -translate-x-1/2 border-t-bg-card'
                : position === 'left'
                  ? 'left-full top-1/2 -ml-1 -translate-y-1/2 border-l-bg-card'
                  : position === 'right'
                    ? 'right-full top-1/2 -mr-1 -translate-y-1/2 border-r-bg-card'
                    : '',
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
  }>(),
  { position: 'top' }
);

const isHovered = ref(false);
// Expose pour les tests unitaires
defineExpose({ isHovered });
const triggerEl = ref<HTMLElement>();
const tooltipEl = ref<HTMLElement>();
const tooltipStyle = ref({ top: '0px', left: '0px', maxWidth: '100vw' });


function getTooltipCoords(
  position: 'top' | 'bottom' | 'left' | 'right',
  triggerRect: DOMRect,
  tooltipRect: DOMRect,
  padding: number
) {
  const centerX = triggerRect.left + triggerRect.width / 2 - tooltipRect.width / 2;
  const centerY = triggerRect.top + triggerRect.height / 2 - tooltipRect.height / 2;
  if (position === 'bottom') return { top: triggerRect.bottom + padding, left: centerX };
  if (position === 'top') return { top: triggerRect.top - tooltipRect.height - padding, left: centerX };
  if (position === 'left') return { top: centerY, left: triggerRect.left - tooltipRect.width - padding };
  if (position === 'right') return { top: centerY, left: triggerRect.right + padding };
  return { top: triggerRect.bottom + padding, left: centerX };
}


function updateTooltipPosition() {
  if (!triggerEl.value || !tooltipEl.value) return;
  const triggerRect = triggerEl.value.getBoundingClientRect();
  const tooltipRect = tooltipEl.value.getBoundingClientRect();
  const padding = 8;
  const vw = window.innerWidth;
  const vh = window.innerHeight;
  let position = props.position;
  let coords = getTooltipCoords(position, triggerRect, tooltipRect, padding);

  // Responsive: switch position if out of viewport (vertical only)
  if (position === 'top' && coords.top < padding) {
    position = 'bottom';
    coords = getTooltipCoords(position, triggerRect, tooltipRect, padding);
  } else if (position === 'bottom' && coords.top + tooltipRect.height > vh - padding) {
    position = 'top';
    coords = getTooltipCoords(position, triggerRect, tooltipRect, padding);
  }

  // Clamp to viewport
  const top = Math.max(padding, Math.min(coords.top, vh - tooltipRect.height - padding));
  const left = Math.max(padding, Math.min(coords.left, vw - tooltipRect.width - padding));

  tooltipStyle.value = {
    top: `${top}px`,
    left: `${left}px`,
    maxWidth: `calc(100vw - ${2 * padding}px)`,
  };
}


const handleMouseEnter = () => {
  isHovered.value = true;
};
const handleMouseLeave = () => {
  isHovered.value = false;
};

// Watch isHovered to update tooltip position when shown
watch(isHovered, (val) => {
  if (val) nextTick(updateTooltipPosition);
});

onUnmounted(() => {
  triggerEl.value = undefined;
  tooltipEl.value = undefined;
});
</script>
