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
  const isConnected = ref(false);
  const rpcConnected = ref(false);
  const errorMessage = ref<string | null>(null);
  const isRetrying = ref(false);
  const isManualDisconnect = ref(false);

  const { status, data, open, close } = useVueUseWebSocket(wsUrl, {
    autoReconnect: {
      retries: Infinity,
      // VueUse can call delay with an undefined retry count depending on runtime path.
      // Default to first retry to avoid NaN -> immediate reconnect.
      delay: (retries = 1) => Math.min(1000 * 2 ** (retries - 1), 30000), // Exponential backoff: 1s, 2s, 4s...30s max
    },
    immediate: false,
    heartbeat: {
      message: 'ping',
      interval: 30000,
      pongTimeout: 10000,
    },
    onConnected: () => {
      isConnected.value = true;
      errorMessage.value = null;
      isRetrying.value = false;
    },
    onDisconnected: () => {
      isConnected.value = false;
      rpcConnected.value = false;

      if (isManualDisconnect.value) {
        isRetrying.value = false;
        isManualDisconnect.value = false;
        return;
      }

      errorMessage.value = 'WebSocket disconnected. Retrying...';
      isRetrying.value = true;
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
      isConnected.value = true;
      errorMessage.value = null;
      isRetrying.value = false;
    } else if (newStatus === 'CLOSED' || newStatus === 'CONNECTING') {
      isConnected.value = false;
      rpcConnected.value = false;
      if (!isManualDisconnect.value) {
        errorMessage.value = 'WebSocket disconnected. Retrying...';
        isRetrying.value = true;
      }
    }
  });

  /**
   * Closes the WebSocket connection if open.
   */
  const disconnect = () => {
    isManualDisconnect.value = true;
    isConnected.value = false;
    close();
    isRetrying.value = false;
  };

  const connect = () => {
    isManualDisconnect.value = false;
    open();
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
