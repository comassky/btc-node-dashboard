import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import { visualizer } from 'rollup-plugin-visualizer';
import viteCompression from 'vite-plugin-compression';
import AutoImport from 'unplugin-auto-import/vite';
import Icons from 'unplugin-icons/vite';
import Inspect from 'vite-plugin-inspect';
import path from 'path';

// @ts-expect-error - Type incompatibility between plugin versions, safe to ignore
export default defineConfig(({ mode }) => ({
  define: {
    __APP_VERSION__: JSON.stringify(process.env.APP_VERSION ?? process.env.npm_package_version),
  },
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
      '@components': path.resolve(import.meta.dirname, './src/components'),
      '@cards': path.resolve(import.meta.dirname, './src/components/cards'),
      '@composables': path.resolve(import.meta.dirname, './src/composables'),
      '@types': path.resolve(import.meta.dirname, './src/types'),
      '@utils': path.resolve(import.meta.dirname, './src/utils'),
      '@assets': path.resolve(import.meta.dirname, './src/assets'),
    },
  },
  plugins: [
    tailwindcss(),
    vue(),
    AutoImport({
      imports: [
        'vue',
        'pinia',
        '@vueuse/core',
        {
          vue: ['defineAsyncComponent'],
        },
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
      eslintrc: {
        enabled: true,
      },
    }),
    Icons({
      compiler: 'vue3',
      autoInstall: false,
    }),
    Inspect(),
    ...(mode === 'production'
      ? [
          visualizer({
            filename: '../../stats.html',
            open: false,
            gzipSize: true,
            brotliSize: false,
          }),
          //Gzip
          viteCompression({
            algorithm: 'gzip',
            ext: '.gz',
            threshold: 1024,
            deleteOriginFile: false,
          }),
          // Brotli
          viteCompression({
            algorithm: 'brotliCompress',
            ext: '.br',
            threshold: 1024,
            deleteOriginFile: false,
          }),
        ]
      : []),
  ],

  base: './',

  build: {
    outDir: 'dist',
    cssMinify: 'lightningcss',
    chunkSizeWarningLimit: 600,
    sourcemap: false,
    target: 'es2020',
    rollupOptions: {
      output: {
        entryFileNames: 'assets/js/[hash:16].js',
        chunkFileNames: 'assets/js/[hash:16].js',
        assetFileNames: ({ name }) => {
          if (/\.css$/i.test(name ?? '')) {
            return 'assets/css/[hash:16][extname]';
          }
          if (/\.(png|jpe?g|svg|gif|webp|avif)$/i.test(name ?? '')) {
            return 'assets/img/[hash:16][extname]';
          }
          if (/\.(woff2?|ttf|otf|eot)$/i.test(name ?? '')) {
            return 'assets/fonts/[hash:16][extname]';
          }
          return 'assets/[hash:16][extname]';
        },
      },
    },
  },

  // Optimize pre-bundled dependencies
  optimizeDeps: {
    include: ['vue', 'pinia', 'chart.js', 'date-fns'],
  },

  server: {
    proxy: {
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
}));
