import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useTheme } from '../useTheme';
import { defineComponent, h } from 'vue';
import { mount } from '@vue/test-utils';

describe('useTheme', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark');
    document.documentElement.style.removeProperty('--status-error-rgb');
    
    // Mock matchMedia
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation(query => ({
        matches: query === '(prefers-color-scheme: dark)',
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  const createWrapper = () => {
    let composableResult: any;
    
    const TestComponent = defineComponent({
      setup() {
        composableResult = useTheme();
        return () => h('div');
      }
    });
    
    const wrapper = mount(TestComponent);
    return { wrapper, result: composableResult };
  };

  it('should initialize with dark mode by default', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    expect(result.isDarkMode.value).toBe(true);
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should toggle dark mode', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    const initialMode = result.isDarkMode.value;
    result.toggleDarkMode();
    
    expect(result.isDarkMode.value).toBe(!initialMode);
    expect(localStorage.getItem('theme')).toBe(initialMode ? 'light' : 'dark');
  });

  it('should save theme preference to localStorage', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    result.toggleDarkMode();
    const theme1 = localStorage.getItem('theme');
    
    result.toggleDarkMode();
    const theme2 = localStorage.getItem('theme');
    
    expect(theme1).not.toBe(theme2);
  });

  it('should add dark class to document when dark mode is enabled', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    // Ensure we're in dark mode
    if (!result.isDarkMode.value) {
      result.toggleDarkMode();
    }
    
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should remove dark class when dark mode is disabled', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    // Ensure we're in light mode
    if (result.isDarkMode.value) {
      result.toggleDarkMode();
    }
    
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('should update error pulse RGB color based on theme', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    result.toggleDarkMode();
    
    const errorColor = document.documentElement.style.getPropertyValue('--status-error-rgb');
    expect(errorColor).toBeTruthy();
    expect(errorColor).toMatch(/\d+,\s*\d+,\s*\d+/);
  });

  it('should load saved theme from localStorage', async () => {
    localStorage.setItem('theme', 'light');
    
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    expect(result.isDarkMode.value).toBe(false);
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('should respect prefers-color-scheme when no saved theme', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    // No saved theme + matchMedia returns true for dark mode
    expect(result.isDarkMode.value).toBe(true);
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should persist theme across multiple toggles', async () => {
    const { result, wrapper } = createWrapper();
    await wrapper.vm.$nextTick();
    
    result.toggleDarkMode();
    const theme1 = localStorage.getItem('theme');
    
    result.toggleDarkMode();
    const theme2 = localStorage.getItem('theme');
    
    expect(theme1).not.toBe(theme2);
  });
});
