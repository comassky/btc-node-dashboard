import { mount } from '@vue/test-utils';
import PeersCard from '../PeersCard.vue';
import { describe, it, expect } from 'vitest';
import { Icon } from '@iconify/vue';
import type { GeneralStats } from '../../../types';

describe('PeersCard.vue', () => {
  const slotStub = { template: '<div><slot /></div>' };
  const stats: GeneralStats = { totalPeers: 8, inboundCount: 3, outboundCount: 2 };

  it('renders peer stats', () => {
    const wrapper = mount(PeersCard, {
      props: { stats },
      global: {
        stubs: { Tooltip: slotStub, BaseCard: slotStub },
        components: { Icon },
      },
    });
    expect(wrapper.text()).toContain('Total Peers');
    expect(wrapper.text()).toContain('Inbound: 3');
    expect(wrapper.text()).toContain('Outbound: 2');
    expect(wrapper.findComponent(Icon).exists()).toBe(true);
  });

  it('shows low outbound warning if forceLowPeers is true', () => {
    const wrapper = mount(PeersCard, {
      props: { stats, forceLowPeers: true },
      global: {
        stubs: { Tooltip: slotStub, BaseCard: slotStub },
        components: { Icon },
      },
    });
    expect(wrapper.text()).toContain('Low outbound connections');
    // Check that Icon components are rendered
    const icons = wrapper.findAllComponents(Icon);
    expect(icons.length).toBeGreaterThan(0);
  });
});
