const { createApp, ref, onMounted, reactive, toRefs, watch, computed, nextTick, onBeforeUnmount } = Vue;

const DEFAULT_DATA = {
    stats: { inboundCount: 0, outboundCount: 0, totalPeers: 0 },
    blockchain: {
        blocks: 0,
        headers: 0,
        chain: 'Loading...',
        verificationProgress: 0,
        difficulty: 0,
        medianBlockSize: 0,
    },
    node: { version: 'N/A', protocolVersion: 'N/A', subversion: 'N/A' },
    upTime: 'N/A',
    inboundPeers: [],
    outboundPeers: [],
    subverDistribution: { inbound: [], outbound: [] },
    block: {},
};

const BASE_COLORS = [
    '#06d6a0', '#ff9900', '#ef476f', '#118ab2', '#ffd166', '#00bcd4', '#4caf50', '#9c27b0',
    '#ff9800', '#03a9f4', '#8bc34a', '#e91e63', '#607d8b', '#009688', '#cddc39', '#795548'
];

// --- Fonctions utilitaires ---

const getCssVar = (name) => getComputedStyle(document.documentElement).getPropertyValue(name).trim();

const generateColors = (numColors) => {
    const colors = [];
    for (let i = 0; i < numColors; i++) {
        colors.push(BASE_COLORS[i % BASE_COLORS.length]);
    }
    return colors;
};

const updateChartDefaults = () => {
    // Ces valeurs s'appliquent aux axes et aux ticks (si le graphique en avait).
    Chart.defaults.color = getCssVar('--text-primary');
    Chart.defaults.borderColor = getCssVar('--border-strong');
};

// NOUVEAU: Facteurs les options dynamiques de Chart.js
const getChartOptions = () => {
    return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'right',
                labels: {
                    boxWidth: 10,
                    color: getCssVar('--text-primary'), // Couleur dynamique
                }
            },
            tooltip: {
                mode: 'index',
                intersect: false,
                // Couleurs dynamiques pour le Tooltip
                backgroundColor: getCssVar('--bg-card'),
                bodyColor: getCssVar('--text-primary'),
                titleColor: getCssVar('--text-secondary'),
                cornerRadius: 6,
                callbacks: {
                    label: function (context) {
                        const label = context.label || '';
                        if (label) {
                            return label + ': ' + context.formattedValue + '%';
                        }
                        return null;
                    }
                }
            },
            title: {
                display: false,
            }
        }
    };
};

const initPieChart = (canvasRef, chartData) => {
    if (!canvasRef.value) return null;

    updateChartDefaults();

    const labels = chartData.map(d => d.server || '[Unknown]');
    const percentages = chartData.map(d => d.percentage);
    const backgroundColors = generateColors(labels.length);

    const ctx = canvasRef.value.getContext('2d');
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
        // Utilisation des options factorisées
        options: getChartOptions()
    });
};

const updateChartData = (chartInstance, chartData) => {
    if (!chartInstance) return;

    updateChartDefaults();

    const labels = chartData.map(d => d.server || '[Unknown]');
    const percentages = chartData.map(d => d.percentage);
    const backgroundColors = generateColors(labels.length);

    chartInstance.data.labels = labels;
    chartInstance.data.datasets[0].data = percentages;
    chartInstance.data.datasets[0].backgroundColor = backgroundColors;

    // Mise à jour explicite des options basées sur le thème actuel (pour les mises à jour de données)
    const options = getChartOptions();
    chartInstance.options.plugins.legend.labels.color = options.plugins.legend.labels.color;
    chartInstance.options.plugins.tooltip.backgroundColor = options.plugins.tooltip.backgroundColor;
    chartInstance.options.plugins.tooltip.bodyColor = options.plugins.tooltip.bodyColor;
    chartInstance.options.plugins.tooltip.titleColor = options.plugins.tooltip.titleColor;

    chartInstance.update();
};

// Fonctions de formatage (laissées ici pour la clarté)
const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

const formatTimeOffset = (timeoffset) => {
    if (timeoffset === undefined || timeoffset === null) return (0).toFixed(1) + ' s';
    return (timeoffset * 1).toFixed(1) + ' s';
};

const formatTimeSince = (timestampOrTotalSeconds) => {
    if (!timestampOrTotalSeconds) return 'N/A';

    let totalSeconds;
    if (timestampOrTotalSeconds > 1000000000) {
        const now = Date.now() / 1000;
        totalSeconds = Math.max(0, now - timestampOrTotalSeconds);
    } else {
        totalSeconds = timestampOrTotalSeconds;
    }

    if (totalSeconds < 1) return '<1s';

    const s = Math.floor(totalSeconds % 60);
    const m = Math.floor((totalSeconds / 60) % 60);
    const h = Math.floor((totalSeconds / (60 * 60)) % 24);
    const d = Math.floor(totalSeconds / (60 * 60 * 24));

    const parts = [];
    if (d > 0) parts.push(d + "d");
    if (h > 0) parts.push(h + "h");
    if (m > 0) parts.push(m + "m");
    if (parts.length === 0 || totalSeconds < 60) parts.push(s + "s");

    return parts.slice(0, 2).join(' ');
};

const formatPing = (minping) => {
    if (minping === undefined || minping === null || minping === 0) return 'N/A';
    return minping.toFixed(3) + ' s';
};

// ---------------------------------------------


createApp({
    setup() {
        const isConnected = ref(false);
        const rpcConnected = ref(false);
        const errorMessage = ref(null);
        const isDarkMode = ref(true);

        const dataState = reactive(DEFAULT_DATA);
        let ws = null;

        const inboundChartCanvas = ref(null);
        const outboundChartCanvas = ref(null);
        let inboundPieChart = null;
        let outboundPieChart = null;


        const updateErrorPulseRgb = () => {
            const errorColor = isDarkMode.value ? '239, 71, 111' : '239, 68, 68';
            document.documentElement.style.setProperty('--status-error-rgb', errorColor);
        };


        const toggleDarkMode = () => {
            isDarkMode.value = !isDarkMode.value;
            localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light');

            // IMPORTANT: Appliquer la classe 'dark' sur <html> pour garantir la résolution CSS des variables.
            document.documentElement.classList.toggle('dark', isDarkMode.value);

            updateErrorPulseRgb();

            // Détruire et recréer les graphiques pour appliquer les nouvelles couleurs du thème
            destroyCharts();
            nextTick(() => {
                if (rpcConnected.value) {
                    inboundPieChart = initPieChart(inboundChartCanvas, dataState.subverDistribution.inbound);
                    outboundPieChart = initPieChart(outboundChartCanvas, dataState.subverDistribution.outbound);
                }
            });
        };

        const loadTheme = () => {
            const savedTheme = localStorage.getItem('theme');
            if (savedTheme) {
                isDarkMode.value = savedTheme === 'dark';
            } else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
                isDarkMode.value = true;
            } else {
                isDarkMode.value = false;
            }

            // IMPORTANT: Appliquer la classe 'dark' sur <html> au chargement
            if (isDarkMode.value) {
                document.documentElement.classList.add('dark');
            } else {
                document.documentElement.classList.remove('dark');
            }

            updateErrorPulseRgb();
        };


        const normalizeData = (rawData) => {
            const nodeInfo = rawData.nodeInfo || {};
            const blockchainInfo = rawData.blockchainInfo || {};

            Object.assign(dataState, {
                stats: rawData.generalStats || {},
                blockchain: {
                    blocks: blockchainInfo.blocks || 0,
                    headers: blockchainInfo.headers || 0,
                    chain: blockchainInfo.chain || 'N/A',
                    verificationProgress: blockchainInfo.verificationprogress || 0,
                    difficulty: blockchainInfo.difficulty || 0,
                    medianBlockSize: blockchainInfo.medianblocksize || 0,
                },
                node: {
                    version: nodeInfo.version,
                    protocolVersion: nodeInfo.protocolversion,
                    subversion: nodeInfo.subversion,
                },
                upTime: rawData.upTime || 'N/A',
                inboundPeers: rawData.inboundPeer || [],
                outboundPeers: rawData.outboundPeer || [],
                subverDistribution: rawData.subverDistribution || { inbound: [], outbound: [] },
                block: rawData.blockInfo || {},
            });
        };

        const cleanedSubversion = computed(() => {
            const subver = dataState.node.subversion;
            if (!subver || subver === 'N/A') return 'N/A';
            return subver.replace(/^\/|\/$/g, '').trim();
        });

        const destroyCharts = () => {
            if (inboundPieChart) {
                inboundPieChart.destroy();
                inboundPieChart = null;
            }
            if (outboundPieChart) {
                outboundPieChart.destroy();
                outboundPieChart = null;
            }
        };


        const connectWebSocket = () => {
            if (ws) {
                ws.onclose = null;
                ws.close();
            }

            const wsProtocol = location.protocol === "https:" ? "wss:" : "ws:";
            const hostAndPort = location.host;

            ws = new WebSocket(wsProtocol + '//' + hostAndPort + '/ws/dashboard');

            ws.onopen = () => {
                isConnected.value = true;
            };

            ws.onmessage = (event) => {
                try {
                    const json = JSON.parse(event.data);

                    if (json.hasOwnProperty('rpcConnected')) {
                        rpcConnected.value = json.rpcConnected;
                        errorMessage.value = json.errorMessage || null;
                        return;
                    }

                    if (json.hasOwnProperty('generalStats')) {
                        rpcConnected.value = true;
                        errorMessage.value = null;
                        normalizeData(json);
                    }

                } catch (e) {
                    console.error("Error parsing JSON message from WebSocket:", e, event.data);
                }
            };

            ws.onclose = () => {
                isConnected.value = false;
                rpcConnected.value = false;
                errorMessage.value = 'WebSocket disconnected from server. Retrying...';
                setTimeout(connectWebSocket, 3000);
            };

            ws.onerror = (error) => {
                isConnected.value = false;
                rpcConnected.value = false;
                errorMessage.value = 'WebSocket connection error.';
            };
        };

        onMounted(() => {
            loadTheme();
            updateChartDefaults();
            connectWebSocket();

            // L'enregistrement du Service Worker (pour PWA) a été supprimé ici.
        });

        onBeforeUnmount(() => {
            if (ws) {
                ws.onclose = null;
                ws.close();
            }
            destroyCharts();
        });

        watch(rpcConnected, (newVal) => {
            if (newVal) {
                nextTick(() => {
                    destroyCharts();
                    inboundPieChart = initPieChart(inboundChartCanvas, dataState.subverDistribution.inbound);
                    outboundPieChart = initPieChart(outboundChartCanvas, dataState.subverDistribution.outbound);
                });
            } else {
                destroyCharts();
            }
        });

        // Les watchers appellent maintenant la version optimisée d'updateChartData
        watch(() => dataState.subverDistribution.inbound, (newVal) => {
            if (inboundPieChart) { updateChartData(inboundPieChart, newVal); }
        }, { deep: true });

        watch(() => dataState.subverDistribution.outbound, (newVal) => {
            if (outboundPieChart) { updateChartData(outboundPieChart, newVal); }
            }, { deep: true });

        return {
            isConnected,
            rpcConnected,
            errorMessage,
            isDarkMode,
            toggleDarkMode,
            ...toRefs(dataState),
            inbound_peers: computed(() => dataState.inboundPeers),
            outbound_peers: computed(() => dataState.outboundPeers),
            subver_distribution: computed(() => dataState.subverDistribution),
            inboundChartCanvas,
            outboundChartCanvas,
            cleanedSubversion,
            // Fonctions de formatage exposées pour le template
            formatBytes,
            formatTimeOffset,
            formatTimeSince,
            formatPing,
        };
    }
}).mount('#app');