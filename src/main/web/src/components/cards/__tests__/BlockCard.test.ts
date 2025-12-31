import { mount } from '@vue/test-utils';
import BlockCard from '../BlockCard.vue';
import { describe, it, expect } from 'vitest';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import Tooltip from '../../Tooltip.vue';
import type { BlockChainInfo } from '../../../types';
import type { BlockInfoResponse } from '../../../types';

describe('BlockCard.vue', () => {
  const slotStub = { template: '<div><slot /></div>' };

  const blockchain: BlockChainInfo = {
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
  };

  const block: BlockInfoResponse = {
    time: Math.floor(Date.now() / 1000),
    nTx: 2000,
    hash: 'somehash',
  };

  it('renders block count and headers', () => {
    const wrapper = mount(BlockCard, {
      props: { blockchain, block },
      global: {
        stubs: { BaseCard: slotStub },
        components: { FontAwesomeIcon, Tooltip },
      },
    });
    expect(wrapper.text().replace(/\s/g, '')).toContain('123456');
    expect(wrapper.text()).toContain('Headers:');
    expect(wrapper.findComponent(FontAwesomeIcon).exists()).toBe(true);
  });

  it('shows out of sync warning if forceOutOfSync is true', () => {
    const wrapper = mount(BlockCard, {
      props: { blockchain, block, forceOutOfSync: true },
      global: {
        stubs: { BaseCard: slotStub },
        components: { FontAwesomeIcon, Tooltip },
      },
    });
    // The out-of-sync warning is now in the tooltip, not in the visible text
    const tooltip = wrapper.findComponent({ name: 'Tooltip' });
    expect(tooltip.exists()).toBe(true);
    // The tooltip text should contain the out-of-sync warning message
    const tooltipText = tooltip.props('text');
    expect(tooltipText).toMatch(/out of sync|syncing|blocks behind|Verification progress/i);
  });
});
