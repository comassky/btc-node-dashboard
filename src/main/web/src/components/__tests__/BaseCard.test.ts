import { mount } from '@vue/test-utils';
import BaseCard from '../BaseCard.vue';
import { describe, it, expect } from 'vitest';

describe('BaseCard.vue', () => {
  it('renders slot content', () => {
    const wrapper = mount(BaseCard, {
      slots: { default: '<div>Card content</div>' }
    });
    expect(wrapper.text()).toContain('Card content');
  });

  it('applies interactive class when prop is set', () => {
    const wrapper = mount(BaseCard, {
      props: { interactive: true },
      slots: { default: '<div>Card</div>' }
    });
    expect(wrapper.classes()).toContain('dashboard-card-interactive');
  });

  it('applies correct status class', () => {
    const wrapper = mount(BaseCard, {
      props: { status: 'error' },
      slots: { default: '<div>Card</div>' }
    });
    expect(wrapper.classes().some(c => c.includes('status-error'))).toBe(true);
  });
});