import { computed, watchEffect } from 'vue';
import { useColorMode } from '@vueuse/core';

type Theme = 'light' | 'dark' | 'gray';

const THEMES: Theme[] = ['light', 'dark', 'gray'];

const themeRgbMap: Record<Theme, string> = {
  dark: '239, 71, 111',
  light: '239, 68, 68',
  gray: '255, 107, 107',
};

/**
 * Theme management composable using VueUse.
 * Handles theme switching with localStorage persistence and applies theme changes to the DOM.
 */
export function useTheme() {
  // Use VueUse's useColorMode with custom modes
  const mode = useColorMode({
    attribute: 'class',
    modes: {
      light: 'light',
      dark: 'dark',
      gray: 'gray',
    },
    storageKey: 'theme',
  });

  const theme = computed({
    get: () => mode.value as Theme,
    set: (val: Theme) => {
      mode.value = val;
    },
  });

  const isDarkMode = computed(() => theme.value === 'dark');
  const isGrayMode = computed(() => theme.value === 'gray');

  // Apply custom CSS variable based on theme
  watchEffect(() => {
    if (typeof document !== 'undefined') {
      const rgb = themeRgbMap[theme.value] || themeRgbMap.light;
      document.documentElement.style.setProperty('--status-error-rgb', rgb);
    }
  });

  /**
   * Cycles through themes: light -> dark -> gray -> light ...
   */
  const cycleTheme = () => {
    const currentIndex = THEMES.indexOf(theme.value);
    const nextIndex = (currentIndex + 1) % THEMES.length;
    const nextTheme = THEMES[nextIndex];
    if (nextTheme) {
      theme.value = nextTheme;
    }
  };

  // Kept for backward compatibility
  function toggleDarkMode() {
    cycleTheme();
  }

  return {
    theme,
    isDarkMode,
    isGrayMode,
    cycleTheme,
    toggleDarkMode,
  };
}
