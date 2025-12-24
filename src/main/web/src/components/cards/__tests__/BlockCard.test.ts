import { mount } from '@vue/test-utils';
import BlockCard from '../BlockCard.vue';
import { describe, it, expect } from 'vitest';

const slotStub = { template: '<slot />' };

describe('BlockCard.vue', () => {
  const blockchain = { blocks: 123456, headers: 123460 };
  const block = { time: Math.floor(Date.now() / 1000), nTx: 2000 };

  it('renders block count and headers', () => {
    const wrapper = mount(BlockCard, {
      props: { blockchain, block },
      global: { stubs: { 'font-awesome-icon': slotStub, Tooltip: slotStub, BaseCard: slotStub } }
    });
    // Le nombre de blocs est formaté avec un espace
    expect(wrapper.text().replace(/\s/g, '')).toContain('123456');
    expect(wrapper.text()).toContain('Headers:');
  });

  it('shows out of sync warning if forceOutOfSync is true', () => {
    const wrapper = mount(BlockCard, {
      props: { blockchain, block, forceOutOfSync: true },
      global: { stubs: { 'font-awesome-icon': slotStub, Tooltip: slotStub, BaseCard: slotStub } }
    });
    expect(wrapper.text()).toContain('Node out of sync');
  });
});