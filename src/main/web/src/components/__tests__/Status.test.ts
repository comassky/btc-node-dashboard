import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Status from '@components/Status.vue';
import type { BlockChainInfo, BlockInfo } from '@types';

const mockBlockchain: BlockChainInfo = {
  blocks: 800000,
  headers: 800000,
  chain: 'main',
  verificationprogress: 0.9999,
  difficulty: 50000000000000,
  medianBlockSize: 1000000,
};

const mockBlock: BlockInfo = {
  time: Math.floor(Date.now() / 1000) - 300, // 5 minutes ago
  nTx: 2500,
  hash: '00000000000000000001234567890abcdef',
};

describe('Status.vue', () => {
  it('should render correctly', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: true,
        rpcConnected: true,
        errorMessage: null,
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
    });

    expect(wrapper.exists()).toBe(true);
  });

  it('should display connected status when both connections are active', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: true,
        rpcConnected: true,
        errorMessage: null,
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
    });

    const text = wrapper.text();
    expect(text.toLowerCase()).toContain('connected');
  });

  it('should display disconnected status when WebSocket is not connected', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: false,
        rpcConnected: false,
        errorMessage: null,
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
    });

    const text = wrapper.text();
    expect(text.toLowerCase()).toContain('disconnected');
  });

  it('should display error message when provided', () => {
    const errorMsg = 'Connection failed';
    const wrapper = mount(Status, {
      props: {
        isConnected: false,
        rpcConnected: false,
        errorMessage: errorMsg,
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
    });

    expect(wrapper.text()).toContain(errorMsg);
  });

  it('should show RPC disconnected status when RPC is not connected', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: true,
        rpcConnected: false,
        errorMessage: 'RPC unavailable',
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
    });

    const text = wrapper.text().toLowerCase();
    expect(text).toContain('rpc');
  });

  it('should update when props change', async () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: false,
        rpcConnected: false,
        errorMessage: null,
        outboundPeers: 10,
        blockchain: mockBlockchain,
        block: mockBlock,
      },
    });

    await wrapper.setProps({
      isConnected: true,
      rpcConnected: true,
      errorMessage: null,
    });

    const text = wrapper.text().toLowerCase();
    expect(text).toContain('connected');
  });
});
