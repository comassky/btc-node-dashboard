import { mount } from '@vue/test-utils';
import BlockCard from '../BlockCard.vue';
import { describe, it, expect } from 'vitest';

describe('BlockCard.vue', () => {

const slotStub = { template: '<slot />' };

// Mock complet pour BlockChainInfo
const blockchain = {
  chain: 'main',
  blocks: 123456,
  headers: 123460,
  bestblockhash: '0000000000000000000',
  difficulty: 1,
  time: Math.floor(Date.now() / 1000),
  mediantime: Math.floor(Date.now() / 1000),
  verificationprogress: 1,
  initialblockdownload: false,
  chainwork: '00',
  size_on_disk: 0,
  pruned: false,
  pruneheight: null,
} as import('../../../types/BlockChainInfo').BlockChainInfo;
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