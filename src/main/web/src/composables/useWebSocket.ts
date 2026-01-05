import { ref, watch } from 'vue';
import { useWebSocket as useVueUseWebSocket } from '@vueuse/core';
import type { DashboardData } from '@types';

/**
 * WebSocket composable for real-time dashboard updates.
 * Handles connection lifecycle, automatic reconnection with exponential backoff,
 * and data parsing from the server.
 *
 * @param wsUrl WebSocket endpoint URL
 * @param onDataReceived Callback invoked when dashboard data is received
 * @returns Reactive connection state and control functions
 */
export function useWebSocket(
  wsUrl: string,
  onDataReceived: (data: Partial<DashboardData>) => void
) {
  const rpcConnected = ref(false);
  const errorMessage = ref<string | null>(null);
  const isRetrying = ref(false);

  const { status, data, open, close } = useVueUseWebSocket(wsUrl, {
    autoReconnect: {
      retries: Infinity,
      delay: (retries) => Math.min(1000 * 2 ** (retries - 1), 30000), // Exponential backoff: 1s, 2s, 4s...30s max
    },
    immediate: false,
    heartbeat: {
      message: 'ping',
      interval: 30000,
      pongTimeout: 10000,
    },
  });

  // Watch for data changes
  watch(data, (newData) => {
    if (!newData) return;

    try {
      const json = JSON.parse(newData) as Partial<DashboardData>;

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
        const preview = typeof newData === 'string' ? newData.slice(0, 200) : String(newData);
        console.warn('WebSocket parse error:', error, preview);
      }
    }
  });

  // Watch status changes
  watch(status, (newStatus) => {
    if (newStatus === 'OPEN') {
      errorMessage.value = null;
      isRetrying.value = false;
    } else if (newStatus === 'CLOSED') {
      rpcConnected.value = false;
      errorMessage.value = 'WebSocket disconnected. Retrying...';
      isRetrying.value = true;
    } else if (newStatus === 'CONNECTING') {
      isRetrying.value = true;
    }
  });

  const isConnected = ref(false);
  watch(status, (newStatus) => {
    isConnected.value = newStatus === 'OPEN';
  });

  /**
   * Closes the WebSocket connection if open.
   */
  const disconnect = () => {
    close();
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
    connect: open,
    /** Function to disconnect WebSocket */
    disconnect,
  };
}
