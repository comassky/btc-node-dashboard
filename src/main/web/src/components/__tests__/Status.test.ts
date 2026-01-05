import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Status from '@components/Status.vue';
import type { BlockChainInfo, BlockInfoResponse } from '@types';

const mockBlockchain: BlockChainInfo = {
  blocks: 100,
  headers: 100,
  chain: 'main',
  verificationprogress: 1,
  difficulty: 1,
  bestblockhash: '',
  time: 0,
  mediantime: 0,
  initialblockdownload: false,
  chainwork: '',
  size_on_disk: 0,
  pruned: false,
  pruneheight: null,
};

const mockBlock: BlockInfoResponse = {
  time: Math.floor(Date.now() / 1000) - 300,
  nTx: 2500,
  hash: '00000000000000000001234567890abcdef',
};

const mountOptions = {
  global: {
    stubs: {
      Tooltip: {
        template: '<div><slot /></div>',
      },
      IconNetworkWired: true,
      IconSpinner: true,
      IconServer: true,
      IconCircleExclamation: true,
      IconTriangleExclamation: true,
    },
  },
};

describe('Status.vue', () => {
  it('should display connected statuses', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: true,
        rpcConnected: true,
        errorMessage: null,
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
      ...mountOptions,
    });

    const text = wrapper.text();
    expect(text).toContain('WebSocket: CONNECTED');
    expect(text).toContain('Node RPC: ONLINE');
  });

  it('should display disconnected statuses', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: false,
        rpcConnected: false,
        errorMessage: 'Disconnected',
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
      ...mountOptions,
    });

    const text = wrapper.text();
    expect(text).toContain('WebSocket: DISCONNECTED');
    expect(text).toContain('Node RPC: OFFLINE');
  });

  it('should display retrying status and spinner when retrying', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: false,
        rpcConnected: false,
        errorMessage: 'Retrying...',
        isRetrying: true,
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
      ...mountOptions,
    });

    expect(wrapper.text()).toContain('Reconnecting...');
  });

  it('should display warning for low outbound peers', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: true,
        rpcConnected: true,
        errorMessage: null,
        outboundPeers: 2, // Low number of peers
        blockchain: mockBlockchain,
        block: mockBlock,
      },
      ...mountOptions,
    });

    expect(wrapper.text()).toContain('Low outbound peers');
  });
});
