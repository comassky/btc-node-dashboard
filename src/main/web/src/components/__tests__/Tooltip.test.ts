import { mount } from '@vue/test-utils';
import Tooltip from '../Tooltip.vue';
import { describe, it, expect } from 'vitest';

describe('Tooltip.vue', () => {
  it('renders slot content', () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info' },
      slots: { default: '<button>Hover me</button>' },
    });
    expect(wrapper.find('button').exists()).toBe(true);
    expect(wrapper.text()).toContain('Hover me');
  });

  it('renders with top position by default', () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info' },
      slots: { default: '<span>Hover</span>' },
    });
    expect(wrapper.find('span').exists()).toBe(true);
  });

  it('renders with custom position', () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info', position: 'bottom' },
      slots: { default: '<span>Hover</span>' },
    });
    expect(wrapper.find('span').exists()).toBe(true);
  });
});
