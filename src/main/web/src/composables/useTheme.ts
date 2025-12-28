import { ref, onMounted, computed, watchEffect } from 'vue';

type Theme = 'light' | 'dark' | 'gray';

const THEMES: Theme[] = ['light', 'dark', 'gray'];
const THEME_STORAGE_KEY = 'theme';

const themeRgbMap: Record<Theme, string> = {
    dark: '239, 71, 111',
    light: '239, 68, 68',
    gray: '255, 107, 107',
};

/**
 * Theme management composable.
 * Handles theme switching with localStorage persistence and applies theme changes to the DOM.
 *
 * @returns Reactive theme state and control functions.
 */
export function useTheme() {
    const theme = ref<Theme>('dark'); // Default theme

    const isDarkMode = computed(() => theme.value === 'dark');
    const isGrayMode = computed(() => theme.value === 'gray');

    // This effect runs whenever `theme.value` changes, and also once initially.
    watchEffect(() => {
        // 1. Update localStorage
        localStorage.setItem(THEME_STORAGE_KEY, theme.value);

        // 2. Update CSS variables
        const rgb = themeRgbMap[theme.value] || themeRgbMap.light;
        document.documentElement.style.setProperty('--status-error-rgb', rgb);

        // 3. Update document class
        document.documentElement.classList.remove(...THEMES);
        document.documentElement.classList.add(theme.value);
    });

    /**
     * Cycles through themes: light -> dark -> gray -> light ...
     */
    const cycleTheme = () => {
        const currentIndex = THEMES.indexOf(theme.value);
        const nextIndex = (currentIndex + 1) % THEMES.length;
        theme.value = THEMES[nextIndex];
    };

    /**
     * Loads the theme from localStorage or system preference on mount.
     */
    onMounted(() => {
        const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) as Theme;
        if (THEMES.includes(savedTheme)) {
            theme.value = savedTheme;
        } else {
            // Fallback to system preference if no valid theme is saved
            theme.value = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        }
    });

    // For backward compatibility with tests expecting toggleDarkMode
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
