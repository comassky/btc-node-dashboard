/** @type {import('tailwindcss').Config} */
export default {
  // Dark mode is managed by the 'dark' class on the <html> element
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // 🚨 MAPPAGE CRUCIAL VERS LES VARIABLES CSS
        'bg-app': 'var(--bg-app)',
        'bg-card': 'var(--bg-card)',
        'text-primary': 'var(--text-primary)',
        'text-secondary': 'var(--text-secondary)',
        'border-strong': 'var(--border-strong)',

        // Couleurs de statut/accent
        'accent': 'var(--accent)',
        'status-success': 'var(--status-success)',
        'status-error': 'var(--status-error)',
        'status-warning': 'var(--status-warning)',
      },
    },
  },
  plugins: [],
}