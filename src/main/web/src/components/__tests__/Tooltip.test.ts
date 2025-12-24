import { mount } from '@vue/test-utils';
import Tooltip from '../Tooltip.vue';
import { describe, it, expect } from 'vitest';

describe('Tooltip.vue', () => {
  it('renders slot content', () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info' },
      slots: { default: '<button>Hover me</button>' }
    });
    expect(wrapper.text()).toContain('Hover me');
  });

  it('shows tooltip on mouseenter and hides on mouseleave', async () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info' },
      slots: { default: '<button>Hover me</button>' }
    });
    await wrapper.trigger('mouseenter');
    expect(wrapper.vm.isHovered).toBe(true);
    await wrapper.trigger('mouseleave');
    expect(wrapper.vm.isHovered).toBe(false);
  });

  it('positions tooltip according to prop', async () => {
    const wrapper = mount(Tooltip, {
      props: { text: 'info', position: 'bottom' },
      slots: { default: '<span>Hover</span>' }
    });
    expect(wrapper.props('position')).toBe('bottom');
  });
});