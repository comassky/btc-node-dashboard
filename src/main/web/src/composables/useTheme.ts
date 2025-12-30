import { ref, computed, watchEffect } from 'vue';

type Theme = 'light' | 'dark' | 'gray';

const THEMES: Theme[] = ['light', 'dark', 'gray'];
const THEME_STORAGE_KEY = 'theme';

const themeRgbMap: Record<Theme, string> = {
  dark: '239, 71, 111',
  light: '239, 68, 68',
  gray: '255, 107, 107',
};

/**
 * Determines the initial theme by checking localStorage first, then system preference.
 * This function should only be called on the client side.
 */
function getInitialTheme(): Theme {
  if (typeof window === 'undefined') {
    return 'dark'; // Default for non-browser environments
  }
  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) as Theme;
  if (THEMES.includes(savedTheme)) {
    return savedTheme;
  }
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

/**
 * Theme management composable.
 * Handles theme switching with localStorage persistence and applies theme changes to the DOM.
 */
export function useTheme() {
  // Initialize the theme with the correct value from the start.
  const theme = ref<Theme>(getInitialTheme());

  const isDarkMode = computed(() => theme.value === 'dark');
  const isGrayMode = computed(() => theme.value === 'gray');

  // This effect runs whenever `theme.value` changes, applying side effects.
  watchEffect(() => {
    if (typeof document !== 'undefined') {
      // 1. Update localStorage
      localStorage.setItem(THEME_STORAGE_KEY, theme.value);

      // 2. Update CSS variables
      const rgb = themeRgbMap[theme.value] || themeRgbMap.light;
      document.documentElement.style.setProperty('--status-error-rgb', rgb);

      // 3. Update document class
      document.documentElement.classList.remove(...THEMES);
      document.documentElement.classList.add(theme.value);
    }
  });

  /**
   * Cycles through themes: light -> dark -> gray -> light ...
   */
  const cycleTheme = () => {
    const currentIndex = THEMES.indexOf(theme.value);
    const nextIndex = (currentIndex + 1) % THEMES.length;
    theme.value = THEMES[nextIndex];
  };

  // Kept for backward compatibility with older tests if any
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
