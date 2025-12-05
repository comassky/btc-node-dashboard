import { ref, onMounted } from 'vue';

export function useTheme() {
    const isDarkMode = ref(true);

    const updateErrorPulseRgb = () => {
        const errorColor = isDarkMode.value ? '239, 71, 111' : '239, 68, 68';
        document.documentElement.style.setProperty('--status-error-rgb', errorColor);
    };

    const toggleDarkMode = () => {
        isDarkMode.value = !isDarkMode.value;
        localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light');
        document.documentElement.classList.toggle('dark', isDarkMode.value);
        updateErrorPulseRgb();
    };

    const loadTheme = () => {
        const savedTheme = localStorage.getItem('theme');
        isDarkMode.value = savedTheme 
            ? savedTheme === 'dark' 
            : window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        document.documentElement.classList.toggle('dark', isDarkMode.value);
        updateErrorPulseRgb();
    };

    onMounted(() => {
        loadTheme();
    });

    return {
        isDarkMode,
        toggleDarkMode,
    };
}
