<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount } from 'vue';
import { Chart, ArcElement, Tooltip, Legend, PieController } from 'chart.js';
import { type SubverDistribution } from '../types';

Chart.register(ArcElement, Tooltip, Legend, PieController);

// Désactiver toutes les animations par défaut pour optimiser CPU
Chart.defaults.animation = false;

// Données de base pour les couleurs
const BASE_COLORS = [
    '#06d6a0', '#ff9900', '#ef476f', '#118ab2', '#ffd166', '#00bcd4', '#4caf50', '#9c27b0',
    '#ff9800', '#03a9f4', '#8bc34a', '#e91e63', '#607d8b', '#009688', '#cddc39', '#795548'
];

// Utilitaires Chart.js adaptés de dashboard.js
const getCssVar = (name: string): string => getComputedStyle(document.documentElement).getPropertyValue(name).trim();

const generateColors = (numColors: number): string[] => {
    const colors: string[] = [];
    for (let i = 0; i < numColors; i++) {
        colors.push(BASE_COLORS[i % BASE_COLORS.length]);
    }
    return colors;
};

const updateChartDefaults = () => {
    Chart.defaults.color = getCssVar('--text-primary');
    Chart.defaults.borderColor = getCssVar('--border-strong');
};

const getChartOptions = () => {
    return {
        responsive: true,
        maintainAspectRatio: false,
        animation: false, // Désactiver toutes les animations
        plugins: {
            legend: {
                position: 'right',
                labels: {
                    boxWidth: 10,
                    color: getCssVar('--text-primary'),
                }
            },
            tooltip: {
                mode: 'point',
                intersect: true,
                backgroundColor: getCssVar('--bg-card'),
                bodyColor: getCssVar('--text-primary'),
                titleColor: getCssVar('--text-secondary'),
                cornerRadius: 6,
                animation: false,
                callbacks: {
                    label: function (context: any) {
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

// Logique du composant
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

const initPieChart = (chartData: SubverDistribution[]): Chart | null => {
    if (!canvasRef.value) return null;

    updateChartDefaults();

    const labels = chartData.map(d => d.server || '[Unknown]');
    const percentages = chartData.map(d => d.percentage);
    const backgroundColors = generateColors(labels.length);

    const ctx = canvasRef.value.getContext('2d');
    if (!ctx) return null;

    return new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: percentages,
                backgroundColor: backgroundColors,
                hoverOffset: 8,
                borderColor: 'transparent',
            }]
        },
        options: getChartOptions() as any
    });
};

const updateChartData = (chartInstance: Chart, chartData: SubverDistribution[]) => {
    if (!chartInstance) return;

    updateChartDefaults();

    const labels = chartData.map(d => d.server || '[Unknown]');
    const percentages = chartData.map(d => d.percentage);
    const backgroundColors = generateColors(labels.length);

    chartInstance.data.labels = labels;
    chartInstance.data.datasets[0].data = percentages;
    chartInstance.data.datasets[0].backgroundColor = backgroundColors;

    // Mise à jour sans animation pour optimiser CPU
    chartInstance.update('none');
};

// 1. Watcher sur les données (pour les mises à jour en temps réel)
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

// 2. 🚨 Watcher sur le Dark Mode (pour forcer la recréation du graphique)
watch(() => props.isDarkMode, () => {
    // Détruit et recrée le graphique pour appliquer les nouvelles couleurs du thème
    destroyChart();
    nextTick(() => {
        // Recrée le graphique avec les données actuelles
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
    <div class="sub-card flex-1 p-0 flex flex-col gap-4 border border-border-strong rounded-lg p-4">
        <h4
            class="text-lg font-bold uppercase text-center pb-2 border-b-2 tracking-wider"
            :class="[`border-${headerColor}`, `text-${headerColor}`]"
        >
            <i :class="headerIcon" class="mr-1"></i>
            {{ type === 'inbound' ? 'Inbound Peers' : 'Outbound Peers' }} ({{ count }})
        </h4>
        <div class="flex flex-col items-center justify-center h-full">
            <div class="chart-container">
                <canvas :ref="(el) => { canvasRef = el as HTMLCanvasElement }" :id="`${type}-chart`"></canvas>
            </div>
        </div>
    </div>
</template>