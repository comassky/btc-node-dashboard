// vite.config.ts
import { defineConfig } from 'vite';
// 1. Importer le plugin Vue
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  // 2. Utiliser le plugin Vue
  plugins: [vue()],

  // (La ligne 'root' a été supprimée, car index.html est à la racine)

  base: './',

  build: {
    outDir: 'dist',
  },
});