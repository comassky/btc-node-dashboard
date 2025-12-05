import { ref } from 'vue';
import type { DashboardData } from '../types';

const WS_RECONNECT_BASE_DELAY = 1000;
const WS_RECONNECT_MAX_DELAY = 30000;
const WS_RECONNECT_MULTIPLIER = 2;

export function useWebSocket(wsUrl: string, onDataReceived: (data: Partial<DashboardData>) => void) {
    const isConnected = ref(false);
    const rpcConnected = ref(false);
    const errorMessage = ref<string | null>(null);

    let ws: WebSocket | null = null;
    let reconnectTimeout: ReturnType<typeof setTimeout> | null = null;
    let reconnectAttempts = 0;

    const scheduleReconnect = () => {
        if (reconnectTimeout) clearTimeout(reconnectTimeout);
        
        const delay = Math.min(
            WS_RECONNECT_BASE_DELAY * Math.pow(WS_RECONNECT_MULTIPLIER, reconnectAttempts),
            WS_RECONNECT_MAX_DELAY
        );
        
        reconnectAttempts++;
        reconnectTimeout = setTimeout(connect, delay);
    };

    const connect = () => {
        if (ws) {
            ws.onclose = null;
            ws.close();
        }

        ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            isConnected.value = true;
            errorMessage.value = null;
            reconnectAttempts = 0;
        };

        ws.onmessage = (event) => {
            try {
                const json = JSON.parse(event.data) as Partial<DashboardData>;

                if ('rpcConnected' in json) {
                    rpcConnected.value = json.rpcConnected ?? false;
                    errorMessage.value = json.errorMessage || null;
                    return;
                }

                if ('generalStats' in json) {
                    rpcConnected.value = true;
                    errorMessage.value = null;
                    onDataReceived(json);
                }
            } catch (e) {}
        };

        ws.onclose = () => {
            isConnected.value = false;
            rpcConnected.value = false;
            errorMessage.value = 'WebSocket disconnected. Retrying...';
            scheduleReconnect();
        };

        ws.onerror = () => {
            isConnected.value = false;
            rpcConnected.value = false;
            errorMessage.value = 'WebSocket connection error.';
        };
    };

    const disconnect = () => {
        if (reconnectTimeout) clearTimeout(reconnectTimeout);
        if (ws) {
            ws.onclose = null;
            ws.close();
        }
    };

    return {
        isConnected,
        rpcConnected,
        errorMessage,
        connect,
        disconnect,
    };
}
