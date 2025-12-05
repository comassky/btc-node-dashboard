import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Status from '../Status.vue';

describe('Status.vue', () => {
  it('should render correctly', () => {
    const wrapper = mount(Status, {
      props: {
        isConnected: true,
        rpcConnected: true,
        errorMessage: null,
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
