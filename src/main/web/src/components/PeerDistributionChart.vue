<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount } from 'vue';
import { Chart, ArcElement, Tooltip, Legend, PieController, type TooltipItem, type ChartOptions } from 'chart.js';
import { type SubverDistribution } from '@types';

/**
 * Peer Distribution Chart Component
 * Displays a pie chart showing the distribution of peer software versions.
 * Optimized with CSS variable caching and efficient data extraction.
 */

Chart.register(ArcElement, Tooltip, Legend, PieController);

Chart.defaults.animation = false;


// Génère une palette HSL optimisée pour le contraste et la différenciation
const generatePalette = (numColors: number): string[] => {
    const palette: string[] = [];
    // Pour beaucoup de couleurs, alterne la luminosité et la saturation pour éviter les teintes trop proches
    const baseSaturation = 70;
    const baseLightness = 50;
    for (let i = 0; i < numColors; i++) {
        // Décale la teinte pour maximiser l'écart entre couleurs voisines
        const hue = Math.round((360 / numColors) * i + (i % 2 === 0 ? 0 : 180 / numColors));
        // Alterne la luminosité et la saturation pour plus de contraste
        const sat = baseSaturation + (i % 3 === 0 ? 10 : i % 3 === 1 ? -10 : 0);
        const light = baseLightness + (i % 2 === 0 ? 8 : -8);
        palette.push(`hsl(${hue}, ${sat}%, ${light}%)`);
    }
    return palette;
};

let cssVarCache: Map<string, string> | null = null;

const getCssVar = (name: string): string => {
    if (!cssVarCache) {
        cssVarCache = new Map();
    }
    if (!cssVarCache.has(name)) {
        cssVarCache.set(name, getComputedStyle(document.documentElement).getPropertyValue(name).trim());
    }
    return cssVarCache.get(name)!;
};

const invalidateCssCache = () => {
    cssVarCache = null;
};

const generateColors = (numColors: number): string[] => {
    // Accentue les 4 premières couleurs avec le thème, puis palette HSL pour le reste
    const accent = getCssVar('--accent') || '#ff9900';
    const success = getCssVar('--status-success') || '#06d6a0';
    const warning = getCssVar('--status-warning') || '#ffd166';
    const error = getCssVar('--status-error') || '#ef476f';
    const base = [accent, success, warning, error];
    if (numColors <= base.length) {
        return base.slice(0, numColors);
    }
    return [...base, ...generatePalette(numColors - base.length)];
};

const updateChartDefaults = () => {
    Chart.defaults.color = getCssVar('--text-primary');
    Chart.defaults.borderColor = getCssVar('--border-strong');
    Chart.defaults.backgroundColor = getCssVar('--bg-card');
    Chart.defaults.plugins.legend.labels.color = getCssVar('--text-primary');
    Chart.defaults.plugins.tooltip.backgroundColor = getCssVar('--bg-card');
    Chart.defaults.plugins.tooltip.bodyColor = getCssVar('--text-primary');
    Chart.defaults.plugins.tooltip.titleColor = getCssVar('--text-secondary');
};

const getChartOptions = (): ChartOptions<'doughnut'> => {
    return {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        layout: {
            padding: 0
        },
        plugins: {
            legend: {
                display: false // Désactive la légende native
            },
            tooltip: {
                mode: 'point',
                intersect: true,
                backgroundColor: getCssVar('--bg-card'),
                bodyColor: getCssVar('--text-primary'),
                titleColor: getCssVar('--text-secondary'),
                borderColor: getCssVar('--border-strong'),
                borderWidth: 1,
                cornerRadius: 6,
                caretSize: 6,
                padding: 8,
                animation: false,
                callbacks: {
                    label: (context: TooltipItem<'pie'>) => {
                        const label = context.label || '';
                        const value = context.parsed || 0;
                        return `${label}: ${value}%`;
                    }
                }
            },
            title: {
                display: false,
            }
        }
    };
};
// Pour la légende custom et le hover
import { computed } from 'vue';
const hoveredIndex = ref<number|null>(null);

const chartLabels = computed(() => props.peers.map(p => p.server || '[Unknown]'));
const chartColors = computed(() => generateColors(props.peers.length));

const handleLegendEnter = (idx: number) => {
    hoveredIndex.value = idx;
    if (pieChartInstance) {
        // Simule le hover sur le segment
        pieChartInstance.setActiveElements([
            { datasetIndex: 0, index: idx }
        ]);
        pieChartInstance.tooltip.setActiveElements([
            { datasetIndex: 0, index: idx }
        ], {x: 0, y: 0});
        pieChartInstance.update();
    }
};

const handleLegendLeave = () => {
    hoveredIndex.value = null;
    if (pieChartInstance) {
        pieChartInstance.setActiveElements([]);
        pieChartInstance.tooltip.setActiveElements([], {x: 0, y: 0});
        pieChartInstance.update();
    }
};

const props = defineProps<{
    peers: SubverDistribution[];
    type: 'inbound' | 'outbound';
    count: number;
    isDarkMode: boolean;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
let pieChartInstance: Chart | null = null;

const destroyChart = () => {
    if (pieChartInstance) {
        pieChartInstance.destroy();
        pieChartInstance = null;
    }
};

const extractChartData = (chartData: SubverDistribution[]) => {
    const count = chartData.length;
    const labels = new Array(count);
    const percentages = new Array(count);
    
    for (let i = 0; i < count; i++) {
        labels[i] = chartData[i].server || '[Unknown]';
        percentages[i] = chartData[i].percentage;
    }
    
    return { labels, percentages, backgroundColors: generateColors(count) };
};

const initPieChart = (chartData: SubverDistribution[]): Chart | null => {
    if (!canvasRef.value) return null;

    updateChartDefaults();
    const { labels, percentages, backgroundColors } = extractChartData(chartData);

    const ctx = canvasRef.value.getContext('2d');
    if (!ctx) return null;

    return new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: percentages,
                backgroundColor: backgroundColors,
                hoverOffset: 8,
                borderColor: 'transparent',
                borderWidth: 0
            }]
        },
        options: getChartOptions()
    });
};

const updateChartData = (chartInstance: Chart, chartData: SubverDistribution[]) => {
    if (!chartInstance) return;

    updateChartDefaults();
    const { labels, percentages, backgroundColors } = extractChartData(chartData);

    chartInstance.data.labels = labels;
    chartInstance.data.datasets[0].data = percentages;
    chartInstance.data.datasets[0].backgroundColor = backgroundColors;

    chartInstance.update('none');
};

watch(() => props.peers, (newVal) => {
    if (pieChartInstance) {
        updateChartData(pieChartInstance, newVal);
    } else {
        nextTick(() => {
            if (!pieChartInstance) {
                pieChartInstance = initPieChart(newVal);
            }
        });
    }
}, { deep: true, immediate: true });

watch(() => props.isDarkMode, () => {
    invalidateCssCache();
    destroyChart();
    nextTick(() => {
        pieChartInstance = initPieChart(props.peers);
    });
});


onBeforeUnmount(() => {
    destroyChart();
});

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
            <div class="chart-container" style="width:300px; height:300px; max-width:100%; margin:auto;">
                <canvas :ref="(el) => { canvasRef = el as HTMLCanvasElement }" :id="`${type}-chart`" width="300" height="300"></canvas>
            </div>
            <!-- Légende custom -->
            <ul class="mt-2 w-full flex flex-row flex-wrap gap-x-4 gap-y-2 items-center justify-center p-0">
                <li v-for="(label, idx) in chartLabels" :key="label"
                    class="flex items-center gap-1 cursor-pointer text-sm select-none min-w-0"
                    style="padding: 0; margin: 0;"
                    @mouseenter="() => handleLegendEnter(idx)"
                    @mouseleave="handleLegendLeave"
                >
                    <span :style="{ background: chartColors[idx], width: '12px', height: '12px', borderRadius: '50%', display: 'inline-block', marginRight: '0.4em', flexShrink: 0 }"></span>
                    <span>{{ label }}</span>
                </li>
            </ul>
        </div>
    </div>
</template>