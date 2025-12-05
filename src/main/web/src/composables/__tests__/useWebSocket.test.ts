import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useWebSocket } from '../useWebSocket';
import type { DashboardData } from '../../types';

describe('useWebSocket', () => {
  let wsInstances: any[] = [];
  let mockWebSocket: any;

  beforeEach(() => {
    vi.useFakeTimers();
    wsInstances = [];
    
    mockWebSocket = vi.fn().mockImplementation((url: string) => {
      const ws = {
        url,
        readyState: WebSocket.CONNECTING,
        onopen: null as ((event: Event) => void) | null,
        onclose: null as ((event: CloseEvent) => void) | null,
        onerror: null as ((event: Event) => void) | null,
        onmessage: null as ((event: MessageEvent) => void) | null,
        close: vi.fn(),
        send: vi.fn(),
      };
      
      wsInstances.push(ws);
      return ws;
    });

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

    const ws = wsInstances[0];
    ws.readyState = WebSocket.OPEN;
    ws.onopen?.(new Event('open'));

    expect(isConnected.value).toBe(true);
    expect(mockWebSocket).toHaveBeenCalledWith('ws://test');
  });

  it('should handle incoming dashboard data', async () => {
    const onDataReceived = vi.fn();
    const { connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();

    const ws = wsInstances[0];
    const mockData: Partial<DashboardData> = {
      generalStats: {
        inboundCount: 5,
        outboundCount: 10,
        totalPeers: 15,
      },
    };

    ws.onmessage?.(new MessageEvent('message', { data: JSON.stringify(mockData) }));

    expect(onDataReceived).toHaveBeenCalledWith(mockData);
  });

  it('should handle RPC error message', async () => {
    const onDataReceived = vi.fn();
    const { rpcConnected, errorMessage, connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();

    const ws = wsInstances[0];
    const errorData = {
      rpcConnected: false,
      errorMessage: 'RPC connection failed',
    };

    ws.onmessage?.(new MessageEvent('message', { data: JSON.stringify(errorData) }));

    expect(rpcConnected.value).toBe(false);
    expect(errorMessage.value).toBe('RPC connection failed');
  });

  it('should handle WebSocket close and reconnect', async () => {
    const onDataReceived = vi.fn();
    const { isConnected, connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();

    const ws = wsInstances[0];
    ws.onclose?.(new CloseEvent('close'));

    expect(isConnected.value).toBe(false);
    
    await vi.runAllTimersAsync();
    
    expect(wsInstances.length).toBeGreaterThan(1);
  });

  it('should handle WebSocket error', async () => {
    const onDataReceived = vi.fn();
    const { isConnected, errorMessage, connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();

    const ws = wsInstances[0];
    ws.onerror?.(new Event('error'));

    expect(isConnected.value).toBe(false);
    expect(errorMessage.value).toBe('WebSocket connection error.');
  });

  it('should disconnect cleanly', async () => {
    const onDataReceived = vi.fn();
    const { isConnected, connect, disconnect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();

    const ws = wsInstances[0];
    ws.readyState = WebSocket.OPEN;
    ws.onopen?.(new Event('open'));

    expect(isConnected.value).toBe(true);

    disconnect();

    expect(ws.close).toHaveBeenCalled();
  });

  it('should handle invalid JSON gracefully', async () => {
    const onDataReceived = vi.fn();
    const { connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();

    const ws = wsInstances[0];
    ws.onmessage?.(new MessageEvent('message', { data: 'invalid json' }));

    expect(onDataReceived).not.toHaveBeenCalled();
  });

  it('should implement exponential backoff for reconnection', async () => {
    const onDataReceived = vi.fn();
    const { connect } = useWebSocket('ws://test', onDataReceived);

    connect();
    await vi.runAllTimersAsync();

    const ws1 = wsInstances[0];
    ws1.onclose?.(new CloseEvent('close'));
    
    await vi.advanceTimersByTimeAsync(1000);
    
    const ws2 = wsInstances[1];
    ws2.onclose?.(new CloseEvent('close'));
    
    await vi.advanceTimersByTimeAsync(2000);

    expect(wsInstances.length).toBeGreaterThan(2);
  });
});
