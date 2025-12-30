import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { useTheme } from '@composables/useTheme';
import { nextTick } from 'vue';

describe('useTheme', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.className = '';

    // Mock matchMedia for consistent testing
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: query === '(prefers-color-scheme: dark)',
        media: query,
      })),
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should initialize with dark mode if system preference is dark', () => {
    const { theme } = useTheme();
    expect(theme.value).toBe('dark');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should initialize with light mode if system preference is light', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: false });
    const { theme } = useTheme();
    expect(theme.value).toBe('light');
    expect(document.documentElement.classList.contains('light')).toBe(true);
  });

  it('should load saved theme from localStorage over system preference', () => {
    localStorage.setItem('theme', 'gray');
    const { theme } = useTheme();
    expect(theme.value).toBe('gray');
    expect(document.documentElement.classList.contains('gray')).toBe(true);
  });

  it('should cycle through themes: light -> dark -> gray -> light', async () => {
    const { theme, cycleTheme } = useTheme();

    // Set initial state to light for predictable cycling
    theme.value = 'light';
    await nextTick(); // Allow watchEffect to run

    // Cycle 1: light -> dark
    cycleTheme();
    await nextTick();
    expect(theme.value).toBe('dark');
    expect(localStorage.getItem('theme')).toBe('dark');

    // Cycle 2: dark -> gray
    cycleTheme();
    await nextTick();
    expect(theme.value).toBe('gray');
    expect(localStorage.getItem('theme')).toBe('gray');

    // Cycle 3: gray -> light
    cycleTheme();
    await nextTick();
    expect(theme.value).toBe('light');
    expect(localStorage.getItem('theme')).toBe('light');
  });
});
