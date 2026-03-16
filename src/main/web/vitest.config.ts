import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import AutoImport from 'unplugin-auto-import/vite';
import Icons from 'unplugin-icons/vite';
import path from 'path';

export default defineConfig({
  plugins: [
    vue(),
    Icons({
      compiler: 'vue3',
      autoInstall: false,
    }),
    AutoImport({
      imports: [
        'vue',
        'pinia',
        '@vueuse/core',
        {
          '@vueuse/motion': [
            'useMotion',
            'useMotionControls',
            'useMotionProperties',
            'useMotionVariants',
          ],
        },
      ],
      dts: 'src/auto-imports.d.ts',
    }),
  ],
  test: {
    globals: true,
    environment: 'happy-dom',
    // Add this section to run setup files
    setupFiles: ['src/test/setup.ts'],
    // Performance optimizations
    threads: true,
    maxThreads: 4,
    minThreads: 1,
    // Cache test results for faster re-runs
    cache: {
      dir: '.vitest',
    },
    // Isolate test modules
    isolate: true,
    // Report files
    reporters: ['default'],
    // Coverage configuration
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/main.ts',
        '**/*.d.ts',
        '**/*.config.*',
        '**/dist/**',
        'src/test/setup.ts',
      ],
      // Ignore lines with coverage annotations
      lines: 70,
      branches: 70,
      functions: 70,
      statements: 70,
    },
    // Performance optimization - disable source maps in tests
    sourcemap: false,
  },
  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@cards': path.resolve(__dirname, './src/components/cards'),
      '@composables': path.resolve(__dirname, './src/composables'),
      '@types': path.resolve(__dirname, './src/types'),
      '@utils': path.resolve(__dirname, './src/utils'),
      '@assets': path.resolve(__dirname, './src/assets'),
      '@stores': path.resolve(__dirname, './src/stores'),
    },
  },
});
