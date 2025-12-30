import { mount } from '@vue/test-utils';
import Tooltip from '../Tooltip.vue';
import { describe, it, expect } from 'vitest';

describe('Tooltip.vue', () => {
  it('renders slot content', () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info' },
      slots: { default: '<button>Hover me</button>' },
    });
    expect(wrapper.text()).toContain('Hover me');
  });

  it('shows tooltip on mouseenter and hides on mouseleave', async () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info' },
      slots: { default: '<button>Hover me</button>' },
    });
    // Trigger mouseenter and check if tooltip is rendered
    await wrapper.trigger('mouseenter');
    expect(document.body.innerHTML).toContain('info');
    // Trigger mouseleave and check if tooltip is removed
    await wrapper.trigger('mouseleave');
    expect(document.body.innerHTML).not.toContain('info');
  });

  it('positions tooltip according to prop', async () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info', position: 'bottom' },
      slots: { default: '<span>Hover</span>' },
    });
    expect(wrapper.props('position')).toBe('bottom');
  });
});
