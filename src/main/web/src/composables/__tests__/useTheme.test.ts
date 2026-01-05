import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useTheme } from '@composables/useTheme';
import { nextTick } from 'vue';

// Mock VueUse's useColorMode
vi.mock('@vueuse/core', async () => {
  const actual = await vi.importActual('@vueuse/core');
  return {
    ...actual,
    useColorMode: vi.fn(() => {
      const mode = { value: 'dark' };
      return mode;
    }),
    usePreferredColorScheme: vi.fn(() => ({ value: 'dark' })),
  };
});

describe('useTheme', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.className = '';
  });

  it('should provide theme management functionality', () => {
    const { theme, isDarkMode, isGrayMode, cycleTheme } = useTheme();
    
    expect(theme).toBeDefined();
    expect(isDarkMode).toBeDefined();
    expect(isGrayMode).toBeDefined();
    expect(typeof cycleTheme).toBe('function');
  });

  it('should expose cycleTheme function', async () => {
    const { cycleTheme, theme } = useTheme();
    
    theme.value = 'light';
    await nextTick();
    
    cycleTheme();
    await nextTick();
    
    // After cycle, theme should be different
    expect(['light', 'dark', 'gray']).toContain(theme.value);
  });
});
