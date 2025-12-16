
import { ref } from 'vue';
import type { DashboardData } from '@types';
import ReconnectingWebSocket from 'reconnecting-websocket';

/**
 * WebSocket composable for real-time dashboard updates.
 * Handles connection lifecycle, automatic reconnection with exponential backoff,
 * and data parsing from the server.
 *
 * @param wsUrl WebSocket endpoint URL
 * @param onDataReceived Callback invoked when dashboard data is received
 * @param wsClass Optional WebSocket class (for testing/mocking)
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

    /**
     * Establishes a new WebSocket connection and sets up event listeners.
     */
    const connect = () => {
        if (ws) {
            ws.close();
            ws = null;
        }
        ws = new wsClass(wsUrl);

        if (ws) {
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
        }
    };

    /**
     * Closes the WebSocket connection if open.
     */
    const disconnect = () => {
        if (ws) {
            ws.close();
            ws = null;
        }
        isRetrying.value = false;
    };

    return {
        /** Reactive connection state */
        isConnected,
        /** Reactive RPC connection state */
        rpcConnected,
        /** Error message if any */
        errorMessage,
        /** Whether the connection is retrying */
        isRetrying,
        /** Function to connect WebSocket */
        connect,
        /** Function to disconnect WebSocket */
        disconnect,
    };
}
