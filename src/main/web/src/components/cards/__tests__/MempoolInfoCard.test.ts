import { mount } from '@vue/test-utils';
import MempoolInfoCard from '../MempoolInfoCard.vue';
import { describe, it, expect } from 'vitest';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import type { MempoolInfoResponse } from '../../../types/MempoolInfoResponse';

describe('MempoolInfoCard.vue', () => {

  const slotStub = { template: '<div><slot /></div>' };

  const mempoolInfo: MempoolInfoResponse = {
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
      global: {
        stubs: { BaseCard: slotStub },
        components: { FontAwesomeIcon }
      }
    });
    expect(wrapper.text()).toContain('Transactions');
    expect(wrapper.text()).toContain('1000');
    expect(wrapper.text()).toContain('Total Bytes');
    // 500000 bytes = 488.28 KB
    expect(wrapper.text()).toMatch(/488(.|,)?28\s?KB/);
    expect(wrapper.text()).toContain('Total Fees');
    expect(wrapper.text()).toContain('0.5 BTC');
    expect(wrapper.findComponent(FontAwesomeIcon).exists()).toBe(true);
  });
});
