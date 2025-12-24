import { mount } from '@vue/test-utils';
import MempoolInfoCard from '../MempoolInfoCard.vue';
import { describe, it, expect } from 'vitest';

const slotStub = { template: '<slot />' };

describe('MempoolInfoCard.vue', () => {
  const mempoolInfo = {
    loaded: true,
    size: 1000,
    bytes: 500000,
    usage: 400000,
    maxmempool: 1000000,
    mempoolminfee: 0.00001,
    minrelaytxfee: 0.00001,
    unbroadcastcount: 5,
    total_fee: 0.5
  };

  it('renders mempool info fields', () => {
    const wrapper = mount(MempoolInfoCard, {
      props: { mempoolInfo },
      global: { stubs: { 'font-awesome-icon': slotStub, BaseCard: slotStub } }
    });
    expect(wrapper.text()).toContain('Transactions');
    expect(wrapper.text()).toContain('1000');
    expect(wrapper.text()).toContain('Total Bytes');
    // 500000 bytes = 488.28 KB
    expect(wrapper.text()).toMatch(/488(.|,)?28\s?KB/);
    expect(wrapper.text()).toContain('Total Fees');
    expect(wrapper.text()).toContain('0.5 BTC');
  });
});