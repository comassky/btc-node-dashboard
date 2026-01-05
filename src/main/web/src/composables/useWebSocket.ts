import { ref } from 'vue';
import type { DashboardData } from '@types';
import ReconnectingWebSocket from 'reconnecting-websocket';

/**
 * WebSocket composable for real-time dashboard updates.
 * Handles connection lifecycle, automatic reconnection with exponential backoff,
 * and data parsing from the server.

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
  const attachListeners = (socket: ReconnectingWebSocket) => {
    socket.addEventListener('open', () => {
      isConnected.value = true;
      errorMessage.value = null;
      isRetrying.value = false;
    });

    socket.addEventListener('message', (event: MessageEvent) => {
      try {
        const json = JSON.parse(event.data) as Partial<DashboardData>;

        // Optimize: check rpcConnected first (more common case)
        if ('generalStats' in json) {
          rpcConnected.value = true;
          errorMessage.value = null;
          onDataReceived(json);
        } else if ('rpcConnected' in json) {
          rpcConnected.value = json.rpcConnected ?? false;
          errorMessage.value = json.errorMessage || null;
        }
      } catch (error) {
        if (import.meta.env.DEV) {
          const preview =
            typeof event.data === 'string' ? event.data.slice(0, 200) : String(event.data);
          console.warn('WebSocket parse error:', error, preview);
        }
      }
    });

    socket.addEventListener('close', () => {
      isConnected.value = false;
      rpcConnected.value = false;
      errorMessage.value = 'WebSocket disconnected. Retrying...';
      isRetrying.value = true;
    });

    socket.addEventListener('error', () => {
      isConnected.value = false;
      rpcConnected.value = false;
      errorMessage.value = 'WebSocket connection error.';
    });
  };

  const connect = () => {
    if (ws) {
      try {
        // readyState constants may not be on the wrapper, defensively check
        const ready = (ws as any).readyState;
        const OPEN = (ws as any).OPEN ?? WebSocket.OPEN;
        const CONNECTING = (ws as any).CONNECTING ?? WebSocket.CONNECTING;
        if (ready === OPEN || ready === CONNECTING) {
          return;
        }
      } catch (e) {}
      try {
        ws.close();
      } catch (e) {}
      ws = null;
    }

    ws = new wsClass(wsUrl);
    if (ws) {
      attachListeners(ws as ReconnectingWebSocket);
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
