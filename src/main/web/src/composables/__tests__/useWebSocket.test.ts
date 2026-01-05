import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { useWebSocket } from '@composables/useWebSocket';
import type { DashboardData } from '@types';
import { nextTick } from 'vue';

let wsInstances: any[] = [];
let mockWebSocket: any;

describe('useWebSocket', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    wsInstances = [];

    class MockWebSocket {
      url: string;
      readyState: number = 0;
      onopen: ((event: Event) => void) | null = null;
      onclose: ((event: CloseEvent) => void) | null = null;
      onerror: ((event: Event) => void) | null = null;
      onmessage: ((event: MessageEvent) => void) | null = null;
      close = vi.fn();
      send = vi.fn();

      static CONNECTING = 0;
      static OPEN = 1;
      static CLOSING = 2;
      static CLOSED = 3;

      constructor(url: string) {
        this.url = url;
        wsInstances.push(this);
        // Auto-open for VueUse behavior
        setTimeout(() => {
          this.readyState = 1;
          this.onopen?.(new Event('open'));
        }, 0);
      }

      addEventListener(event: string, handler: any) {
        if (event === 'open') this.onopen = handler;
        if (event === 'close') this.onclose = handler;
        if (event === 'error') this.onerror = handler;
        if (event === 'message') this.onmessage = handler;
      }
    }

    mockWebSocket = MockWebSocket;
    global.WebSocket = mockWebSocket as any;
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('should initialize with disconnected state', () => {
    const onDataReceived = vi.fn();
    const { isConnected, rpcConnected } = useWebSocket('ws://test', onDataReceived);

    expect(isConnected.value).toBe(false);
    expect(rpcConnected.value).toBe(false);
  });

  it('should connect successfully', async () => {
    const onDataReceived = vi.fn();
    const { isConnected, connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();
    await nextTick();

    expect(isConnected.value).toBe(true);
    expect(wsInstances[0].url).toBe('ws://test');
  });

  it('should handle incoming dashboard data', async () => {
    const onDataReceived = vi.fn();
    const { connect, rpcConnected } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();
    await nextTick();

    const ws = wsInstances[0];
    const mockData: Partial<DashboardData> = {
      generalStats: {
        inboundCount: 5,
        outboundCount: 10,
        totalPeers: 15,
      },
    };

    ws.onmessage?.(new MessageEvent('message', { data: JSON.stringify(mockData) }));
    await nextTick();

    expect(onDataReceived).toHaveBeenCalledWith(mockData);
    expect(rpcConnected.value).toBe(true);
  });

  it('should handle RPC error message', async () => {
    const onDataReceived = vi.fn();
    const { rpcConnected, errorMessage, connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();
    await nextTick();

    const ws = wsInstances[0];
    const errorData = {
      rpcConnected: false,
      errorMessage: 'RPC connection failed',
    };

    ws.onmessage?.(new MessageEvent('message', { data: JSON.stringify(errorData) }));
    await nextTick();

    expect(rpcConnected.value).toBe(false);
    expect(errorMessage.value).toBe('RPC connection failed');
  });

  it('should handle WebSocket close and set isRetrying', async () => {
    const onDataReceived = vi.fn();
    const { isConnected, rpcConnected, errorMessage, isRetrying, connect } = useWebSocket(
      'ws://test',
      onDataReceived
    );

    connect();
    await vi.runAllTimersAsync();
    await nextTick();

    const ws = wsInstances[0];
    ws.readyState = 3; // CLOSED
    ws.onclose?.(new CloseEvent('close'));
    await nextTick();

    expect(isConnected.value).toBe(false);
    expect(rpcConnected.value).toBe(false);
    expect(isRetrying.value).toBe(true);
    expect(errorMessage.value).toBe('WebSocket disconnected. Retrying...');
  });

  it('should disconnect cleanly', async () => {
    const onDataReceived = vi.fn();
    const { isConnected, connect, disconnect, isRetrying } = useWebSocket(
      'ws://test',
      onDataReceived
    );

    connect();
    await vi.runAllTimersAsync();
    await nextTick();

    expect(isConnected.value).toBe(true);

    disconnect();
    await nextTick();

    expect(isRetrying.value).toBe(false);
  });

  it('should handle invalid JSON gracefully', async () => {
    const onDataReceived = vi.fn();
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

    const { connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();
    await nextTick();

    const ws = wsInstances[0];
    ws.onmessage?.(new MessageEvent('message', { data: 'invalid json' }));
    await nextTick();

    expect(onDataReceived).not.toHaveBeenCalled();

    consoleWarnSpy.mockRestore();
  });

  it('should reset isRetrying on disconnect', async () => {
    const onDataReceived = vi.fn();
    const { connect, disconnect, isRetrying } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();
    await nextTick();

    const ws = wsInstances[0];
    ws.readyState = 3; // CLOSED
    ws.onclose?.(new CloseEvent('close'));
    await nextTick();

    expect(isRetrying.value).toBe(true);

    disconnect();
    await nextTick();

    expect(isRetrying.value).toBe(false);
  });
});
