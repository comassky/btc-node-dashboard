<script setup lang="ts">
import {
  Chart,
  ArcElement,
  Tooltip,
  Legend,
  PieController,
  type TooltipItem,
  type ChartOptions,
} from 'chart.js';
import { type SubverDistribution } from '@types';
import { IconArrowDown, IconArrowUp } from '@/icons';

Chart.register(ArcElement, Tooltip, Legend, PieController);

// --- Color utilities ---

const cssVarCache = new Map<string, string>();
const paletteCache = new Map<number, readonly string[]>();

const getCssVar = (name: string) => {
  if (!cssVarCache.has(name)) {
    cssVarCache.set(name, getComputedStyle(document.documentElement).getPropertyValue(name).trim());
  }
  return cssVarCache.get(name) || '';
};
const invalidateCssCache = () => cssVarCache.clear();

const generatePalette = (num: number): readonly string[] => {
  if (!paletteCache.has(num)) {
    paletteCache.set(
      num,
      Array.from({ length: num }, (_, i) => {
        const baseS = 70,
          baseL = 50;
        const hue = Math.round((360 / num) * i + (i % 2 === 0 ? 0 : 180 / num));
        const sat = baseS + (i % 3 === 0 ? 10 : i % 3 === 1 ? -10 : 0);
        const light = baseL + (i % 2 === 0 ? 8 : -8);
        return `hsl(${hue}, ${sat}%, ${light}%)`;
      })
    );
  }
  return paletteCache.get(num)!;
};

const generateColors = (num: number): string[] => {
  const base = [
    getCssVar('--accent') || '#ff9900',
    getCssVar('--status-success') || '#06d6a0',
    getCssVar('--status-warning') || '#ffd166',
    getCssVar('--status-error') || '#ef476f',
  ];
  return num <= base.length ? base.slice(0, num) : [...base, ...generatePalette(num - base.length)];
};

// --- Chart.js config ---
const getChartOptions = (): ChartOptions<'doughnut'> => {
  const bgCard = getCssVar('--bg-card');
  const textPrimary = getCssVar('--text-primary');
  const textSecondary = getCssVar('--text-secondary');
  const borderStrong = getCssVar('--border-strong');
  return {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    color: textPrimary,
    borderColor: borderStrong,
    layout: { padding: 0 },
    plugins: {
      legend: {
        display: false,
        labels: { color: textPrimary },
      },
      tooltip: {
        mode: 'point',
        intersect: true,
        backgroundColor: bgCard,
        bodyColor: textPrimary,
        titleColor: textSecondary,
        borderColor: borderStrong,
        borderWidth: 1,
        cornerRadius: 6,
        caretSize: 6,
        padding: 8,
        animation: false,
        callbacks: {
          label: (context: TooltipItem<'doughnut'>) =>
            `${context.label || ''}: ${context.parsed || 0}%`,
        },
      },
      title: { display: false },
    },
  };
};

// --- Props & Reactivity ---
const props = defineProps<{
  peers: SubverDistribution[];
  type: 'inbound' | 'outbound';
  count: number;
  isDarkMode: boolean;
}>();
const canvasRef = ref<HTMLCanvasElement | null>(null);
let chartInstance: Chart | null = null;
const hoveredIndex = ref<number | null>(null);
const styleVersion = ref(0);
const chartLabels = computed(() => props.peers.map((p) => p.server || '[Unknown]'));
const chartColors = computed(() => {
  styleVersion.value; // Dependency to force update on theme change
  return generateColors(props.peers.length);
});

// --- Chart.js helpers ---
const extractChartData = (data: SubverDistribution[], colors: string[]) => {
  const labels = data.map((d) => d.server || '[Unknown]');
  const percentages = data.map((d) => d.percentage);
  return { labels, percentages, backgroundColors: colors };
};
const destroyChart = () => {
  chartInstance?.destroy();
  chartInstance = null;
};
const initChart = (data: SubverDistribution[], colors: string[]): Chart | null => {
  if (!canvasRef.value) return null;
  const { labels, percentages, backgroundColors } = extractChartData(data, colors);
  const ctx = canvasRef.value.getContext('2d');
  if (!ctx) return null;
  return new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [
        {
          data: percentages,
          backgroundColor: backgroundColors,
          hoverOffset: 8,
          borderColor: 'transparent',
          borderWidth: 0,
        },
      ],
    },
    options: getChartOptions(),
  });
};
const updateChartData = (chart: Chart, data: SubverDistribution[], colors: string[]) => {
  const { labels, percentages, backgroundColors } = extractChartData(data, colors);
  chart.data.labels = labels;
  const dataset = chart.data.datasets[0];
  if (dataset) {
    dataset.data = percentages;
    dataset.backgroundColor = backgroundColors;
  }
  chart.update('none');
};

// --- Interactive legend ---
const handleLegendEnter = (idx: number) => {
  hoveredIndex.value = idx;
  if (chartInstance) {
    const el = { datasetIndex: 0, index: idx };
    chartInstance.setActiveElements([el]);
    chartInstance.tooltip?.setActiveElements([el], { x: 0, y: 0 });
    chartInstance.update();
  }
};
const handleLegendLeave = () => {
  hoveredIndex.value = null;
  if (chartInstance) {
    chartInstance.setActiveElements([]);
    chartInstance.tooltip?.setActiveElements([], { x: 0, y: 0 });
    chartInstance.update();
  }
};

// --- Watchers & Lifecycle ---

watch(
  () => props.peers,
  (val) => {
    if (chartInstance) updateChartData(chartInstance, val, chartColors.value);
    else
      nextTick(() => {
        if (!chartInstance) chartInstance = initChart(val, chartColors.value);
      });
  },
  { deep: true, immediate: true, flush: 'post' }
);
watch(
  () => props.isDarkMode,
  async () => {
    await nextTick(); // Wait for DOM/CSS updates
    invalidateCssCache();
    styleVersion.value++;
    if (chartInstance) {
      chartInstance.options = getChartOptions();
      if (chartInstance.data.datasets[0]) {
        chartInstance.data.datasets[0].backgroundColor = chartColors.value;
      }
      chartInstance.update('none');
    }
  },
  { flush: 'post' }
);
onBeforeUnmount(destroyChart);

const headerColor = props.type === 'inbound' ? 'status-success' : 'accent';
</script>

<template>
  <div class="sub-card flex flex-1 flex-col gap-4 rounded-lg p-0 p-4">
    <h4
      class="border-b-2 pb-2 text-center text-lg font-bold tracking-wider uppercase"
      :class="[`border-${headerColor}`, `text-${headerColor}`]"
    >
      <span class="inline-flex items-center justify-center">
        <component :is="type === 'inbound' ? IconArrowDown : IconArrowUp" class="mr-1" />
        {{ type === 'inbound' ? 'Inbound Peers' : 'Outbound Peers' }} ({{ count }})
      </span>
    </h4>
    <div class="flex h-full w-full flex-col items-center justify-center">
      <div class="chart-container">
        <canvas ref="canvasRef" :id="`${type}-chart`" width="300" height="300"></canvas>
      </div>
      <ul class="legend-list">
        <li
          v-for="(label, idx) in chartLabels"
          :key="label"
          class="legend-item"
          @mouseenter="() => handleLegendEnter(idx)"
          @mouseleave="handleLegendLeave"
        >
          <span class="legend-dot" :style="{ background: chartColors[idx] }"></span>
          <span>{{ label }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.chart-container {
  width: 300px;
  height: 300px;
  max-width: 100%;
  margin: auto;
}
.legend-list {
  margin-top: 0.5rem;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1.2rem;
  align-items: center;
  justify-content: center;
  padding: 0;
  list-style: none;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 0.4em;
  cursor: pointer;
  font-size: 0.95em;
  user-select: none;
  min-width: 0;
  padding: 0;
  margin: 0;
}
.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
  margin-right: 0.4em;
}
</style>
