import { mount } from '@vue/test-utils';
import PeersCard from '../PeersCard.vue';
import { describe, it, expect } from 'vitest';

const slotStub = { template: '<slot />' };

describe('PeersCard.vue', () => {
  const stats = { totalPeers: 8, inboundCount: 3, outboundCount: 2 };

  it('renders peer stats', () => {
    const wrapper = mount(PeersCard, {
      props: { stats },
      global: { stubs: { 'font-awesome-icon': slotStub, Tooltip: slotStub, BaseCard: slotStub } }
    });
    expect(wrapper.text()).toContain('Total Peers');
    expect(wrapper.text()).toContain('Inbound: 3');
    expect(wrapper.text()).toContain('Outbound:');
  });

  it('shows low outbound warning if forceLowPeers is true', () => {
    const wrapper = mount(PeersCard, {
      props: { stats, forceLowPeers: true },
      global: { stubs: { 'font-awesome-icon': slotStub, Tooltip: slotStub, BaseCard: slotStub } }
    });
    expect(wrapper.text()).toContain('Low outbound connections');
  });
});