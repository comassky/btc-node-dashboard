import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
// import { VitePWA } from 'vite-plugin-pwa';
import { visualizer } from 'rollup-plugin-visualizer';
import viteCompression from 'vite-plugin-compression';
import AutoImport from 'unplugin-auto-import/vite';
import Inspect from 'vite-plugin-inspect';
import path from 'path';

// @ts-expect-error - Type incompatibility between plugin versions, safe to ignore
export default defineConfig(({ mode }) => ({
  define: {
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version),
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
    Inspect(),
    // VitePWA disabled
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
    minify: 'esbuild', // esbuild is 20-40x faster than terser with comparable results
    cssMinify: true,
    cssCodeSplit: true,
    chunkSizeWarningLimit: 600,
    sourcemap: false,
    reportCompressedSize: true,
    target: 'es2020',
    // Esbuild options for better minification
    esbuild: {
      drop: mode === 'production' ? ['console', 'debugger'] : [],
      legalComments: 'none',
      treeShaking: true,
    },
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
        // Optimize chunking for better caching
        manualChunks: {
          'vue-vendor': ['vue', 'pinia'],
          'chart-vendor': ['chart.js'],
          'icons-vendor': ['@iconify/vue'],
        },
      },
      // Improve tree-shaking
      treeshake: {
        moduleSideEffects: 'no-external',
        preset: 'recommended',
      },
    },
  },

  // Optimize pre-bundled dependencies
  optimizeDeps: {
    include: ['vue', 'pinia', 'chart.js', '@iconify/vue', 'date-fns', 'ky'],
    exclude: ['@vueuse/core'],
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
