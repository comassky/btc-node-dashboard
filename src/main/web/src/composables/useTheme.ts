import { ref, onMounted } from 'vue';

const DARK_ERROR_RGB = '239, 71, 111';
const LIGHT_ERROR_RGB = '239, 68, 68';
const THEME_STORAGE_KEY = 'theme';
const DARK_THEME = 'dark';
const LIGHT_THEME = 'light';

/**
 * Theme management composable.
 * Handles dark/light mode switching with localStorage persistence.
 */
export function useTheme() {
    const isDarkMode = ref(true);

    const updateErrorPulseRgb = (darkMode: boolean) => {
        document.documentElement.style.setProperty('--status-error-rgb', 
            darkMode ? DARK_ERROR_RGB : LIGHT_ERROR_RGB);
    };

    const applyTheme = (darkMode: boolean) => {
        document.documentElement.classList.toggle('dark', darkMode);
        updateErrorPulseRgb(darkMode);
    };

    const toggleDarkMode = () => {
        isDarkMode.value = !isDarkMode.value;
        localStorage.setItem(THEME_STORAGE_KEY, isDarkMode.value ? DARK_THEME : LIGHT_THEME);
        applyTheme(isDarkMode.value);
    };

    const loadTheme = () => {
        const savedTheme = localStorage.getItem(THEME_STORAGE_KEY);
        isDarkMode.value = savedTheme 
            ? savedTheme === DARK_THEME
            : window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        applyTheme(isDarkMode.value);
    };

    onMounted(() => {
        loadTheme();
    });

    return {
        isDarkMode,
        toggleDarkMode,
    };
}
