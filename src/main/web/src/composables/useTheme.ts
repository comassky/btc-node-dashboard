import { ref, onMounted, computed } from 'vue';

const DARK_ERROR_RGB = '239, 71, 111';
const LIGHT_ERROR_RGB = '239, 68, 68';
const THEME_STORAGE_KEY = 'theme';
const DARK_THEME = 'dark';
const LIGHT_THEME = 'light';
const GRAY_THEME = 'gray';

/**
 * Theme management composable.
 * Handles dark/light mode switching with localStorage persistence.
 *
 * @returns Reactive dark mode state and toggle function
 */
export function useTheme() {
    // theme: 'light' | 'dark' | 'gray'
    const theme = ref<'light' | 'dark' | 'gray'>(DARK_THEME);
    const isDarkMode = computed(() => theme.value === DARK_THEME);
    const isGrayMode = computed(() => theme.value === GRAY_THEME);

    /**
     * Updates the CSS variable for error pulse color based on theme.
     * @param darkMode Whether dark mode is enabled
     */
    const updateErrorPulseRgb = (themeValue: string) => {
        if (themeValue === DARK_THEME) {
            document.documentElement.style.setProperty('--status-error-rgb', DARK_ERROR_RGB);
        } else if (themeValue === GRAY_THEME) {
            document.documentElement.style.setProperty('--status-error-rgb', '255, 107, 107');
        } else {
            document.documentElement.style.setProperty('--status-error-rgb', LIGHT_ERROR_RGB);
        }
    };

    /**
     * Applies the theme to the document root.
     * @param darkMode Whether dark mode is enabled
     */
    const applyTheme = (themeValue: string) => {
        document.documentElement.classList.remove(DARK_THEME, LIGHT_THEME, GRAY_THEME);
        document.documentElement.classList.add(themeValue);
        updateErrorPulseRgb(themeValue);
    };

    /**
     * Toggles between dark and light mode, saving preference to localStorage.
     */
    // Cycle theme: light -> dark -> gray -> light ...
    const cycleTheme = () => {
        if (theme.value === LIGHT_THEME) theme.value = DARK_THEME;
        else if (theme.value === DARK_THEME) theme.value = GRAY_THEME;
        else theme.value = LIGHT_THEME;
        localStorage.setItem(THEME_STORAGE_KEY, theme.value);
        applyTheme(theme.value);
    };

    /**
     * Loads the theme from localStorage or system preference.
     */
    const loadTheme = () => {
        const savedTheme = localStorage.getItem(THEME_STORAGE_KEY);
        if (savedTheme === DARK_THEME || savedTheme === LIGHT_THEME || savedTheme === GRAY_THEME) {
            theme.value = savedTheme;
        } else {
            theme.value = window.matchMedia('(prefers-color-scheme: dark)').matches ? DARK_THEME : LIGHT_THEME;
        }
        applyTheme(theme.value);
    };

    onMounted(() => {
        loadTheme();
    });

    return {
        theme,
        isDarkMode,
        isGrayMode,
        cycleTheme,
    };
}
