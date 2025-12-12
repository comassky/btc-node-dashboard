
import { ref } from 'vue';
import type { DashboardData } from '@types';
import ReconnectingWebSocket from 'reconnecting-websocket';

/**
 * WebSocket composable for real-time dashboard updates.
 * Handles connection lifecycle, automatic reconnection with exponential backoff,
 * and data parsing from the server.
 * 
 * @param wsUrl - WebSocket endpoint URL
 * @param onDataReceived - Callback invoked when dashboard data is received
 * @returns Reactive connection state and control functions
 */
export function useWebSocket(
    wsUrl: string,
    onDataReceived: (data: Partial<DashboardData>) => void,
    wsClass: any = ReconnectingWebSocket
) {
    const isConnected = ref(false);
    const rpcConnected = ref(false);
    const errorMessage = ref<string | null>(null);
    const isRetrying = ref(false);
    let ws: ReconnectingWebSocket | null = null;

    const connect = () => {
        if (ws) {
            ws.close();
            ws = null;
        }
        ws = new wsClass(wsUrl);

        ws.addEventListener('open', () => {
            isConnected.value = true;
            errorMessage.value = null;
            isRetrying.value = false;
        });

        ws.addEventListener('message', (event: MessageEvent) => {
            try {
                const json = JSON.parse(event.data) as Partial<DashboardData>;
                if ('rpcConnected' in json) {
                    rpcConnected.value = json.rpcConnected ?? false;
                    errorMessage.value = json.errorMessage || null;
                } else if ('generalStats' in json) {
                    rpcConnected.value = true;
                    errorMessage.value = null;
                    onDataReceived(json);
                }
            } catch (e) {
                console.warn('WebSocket message parse error', e, event.data);
            }
        });

        ws.addEventListener('close', () => {
            isConnected.value = false;
            rpcConnected.value = false;
            errorMessage.value = 'WebSocket disconnected. Retrying...';
            isRetrying.value = true;
        });

        ws.addEventListener('error', () => {
            isConnected.value = false;
            rpcConnected.value = false;
            errorMessage.value = 'WebSocket connection error.';
        });
    };

    const disconnect = () => {
        if (ws) {
            ws.close();
            ws = null;
        }
        isRetrying.value = false;
    };

    return {
        isConnected,
        rpcConnected,
        errorMessage,
        isRetrying,
        connect,
        disconnect,
    };
}
