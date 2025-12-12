
<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount, computed } from 'vue';
import { Chart, ArcElement, Tooltip, Legend, PieController, type TooltipItem, type ChartOptions } from 'chart.js';
import { type SubverDistribution } from '@types';

Chart.register(ArcElement, Tooltip, Legend, PieController);
Chart.defaults.animation = false;

// --- Color utilities ---

const cssVarCache = new Map<string, string>();
const getCssVar = (name: string): string => {
    if (!cssVarCache.has(name)) {
        cssVarCache.set(name, getComputedStyle(document.documentElement).getPropertyValue(name).trim());
    }
    return cssVarCache.get(name) || '';
};
const invalidateCssCache = () => { cssVarCache.clear(); };

const generatePalette = (num: number): string[] => Array.from({ length: num }, (_, i) => {
    const baseS = 70, baseL = 50;
    const hue = Math.round((360 / num) * i + (i % 2 === 0 ? 0 : 180 / num));
    const sat = baseS + (i % 3 === 0 ? 10 : i % 3 === 1 ? -10 : 0);
    const light = baseL + (i % 2 === 0 ? 8 : -8);
    return `hsl(${hue}, ${sat}%, ${light}%)`;
});
const generateColors = (num: number): string[] => {
    const base = [
        getCssVar('--accent') || '#ff9900',
        getCssVar('--status-success') || '#06d6a0',
        getCssVar('--status-warning') || '#ffd166',
        getCssVar('--status-error') || '#ef476f'
    ];
    return num <= base.length ? base.slice(0, num) : base.concat(generatePalette(num - base.length));
};

// --- Chart.js config ---
const updateChartDefaults = () => {
    const textPrimary = getCssVar('--text-primary');
    const borderStrong = getCssVar('--border-strong');
    const bgCard = getCssVar('--bg-card');
    const textSecondary = getCssVar('--text-secondary');
    Chart.defaults.color = textPrimary;
    Chart.defaults.borderColor = borderStrong;
    Chart.defaults.backgroundColor = bgCard;
    Chart.defaults.plugins.legend.labels.color = textPrimary;
    Chart.defaults.plugins.tooltip.backgroundColor = bgCard;
    Chart.defaults.plugins.tooltip.bodyColor = textPrimary;
    Chart.defaults.plugins.tooltip.titleColor = textSecondary;
};
const getChartOptions = (): ChartOptions<'doughnut'> => {
    const bgCard = getCssVar('--bg-card');
    const textPrimary = getCssVar('--text-primary');
    const textSecondary = getCssVar('--text-secondary');
    const borderStrong = getCssVar('--border-strong');
    return {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        layout: { padding: 0 },
        plugins: {
            legend: { display: false },
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
                    label: (context: TooltipItem<'doughnut'>) => `${context.label || ''}: ${context.parsed || 0}%`
                }
            },
            title: { display: false }
        }
    };
};

// --- Props & Reactivity ---
const props = defineProps<{ peers: SubverDistribution[]; type: 'inbound' | 'outbound'; count: number; isDarkMode: boolean }>();
const canvasRef = ref<HTMLCanvasElement | null>(null);
let chartInstance: Chart | null = null;
const hoveredIndex = ref<number|null>(null);
const chartLabels = computed(() => props.peers.map(p => p.server || '[Unknown]'));
const chartColors = computed(() => generateColors(props.peers.length));

// --- Chart.js helpers ---
const extractChartData = (data: SubverDistribution[]) => ({
    labels: data.map(d => d.server || '[Unknown]'),
    percentages: data.map(d => d.percentage),
    backgroundColors: generateColors(data.length)
});
const destroyChart = () => {
    chartInstance?.destroy();
    chartInstance = null;
};
const initChart = (data: SubverDistribution[]): Chart | null => {
    if (!canvasRef.value) return null;
    updateChartDefaults();
    const { labels, percentages, backgroundColors } = extractChartData(data);
    const ctx = canvasRef.value.getContext('2d');
    if (!ctx) return null;
    return new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{ data: percentages, backgroundColor: backgroundColors, hoverOffset: 8, borderColor: 'transparent', borderWidth: 0 }]
        },
        options: getChartOptions()
    });
};
const updateChartData = (chart: Chart, data: SubverDistribution[]) => {
    updateChartDefaults();
    const { labels, percentages, backgroundColors } = extractChartData(data);
    chart.data.labels = labels;
    chart.data.datasets[0].data = percentages;
    chart.data.datasets[0].backgroundColor = backgroundColors;
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
        if (chartInstance) updateChartData(chartInstance, val);
        else nextTick(() => { if (!chartInstance) chartInstance = initChart(val); });
    },
    { deep: true, immediate: true }
);
watch(
    () => props.isDarkMode,
    () => {
        invalidateCssCache();
        destroyChart();
        nextTick(() => { chartInstance = initChart(props.peers); });
    }
);
onBeforeUnmount(destroyChart);

const headerColor = props.type === 'inbound' ? 'status-success' : 'accent';
const headerIcon = props.type === 'inbound' ? 'fas fa-arrow-alt-circle-down' : 'fas fa-arrow-alt-circle-up';
</script>

<template>
    <div class="sub-card flex-1 p-0 flex flex-col gap-4 rounded-lg p-4">
        <h4
            class="text-lg font-bold uppercase text-center pb-2 border-b-2 tracking-wider"
            :class="[`border-${headerColor}`, `text-${headerColor}`]"
        >
            <i :class="headerIcon" class="mr-1"></i>
            {{ type === 'inbound' ? 'Inbound Peers' : 'Outbound Peers' }} ({{ count }})
        </h4>
        <div class="flex flex-col items-center justify-center h-full w-full">
            <div class="chart-container">
                <canvas ref="canvasRef" :id="`${type}-chart`" width="300" height="300"></canvas>
            </div>
            <ul class="legend-list">
                <li v-for="(label, idx) in chartLabels" :key="label"
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