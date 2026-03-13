import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import { visualizer } from 'rollup-plugin-visualizer';
import viteCompression from 'vite-plugin-compression';
import AutoImport from 'unplugin-auto-import/vite';
import Icons from 'unplugin-icons/vite';
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
      '@stores': path.resolve(__dirname, './src/stores'),
    },
  },
  plugins: [
    tailwindcss(),
    vue({
      // Enable template optimization in production  
      template: {
        compilerOptions: {
          nodeTransforms: mode === 'production' ? [] : undefined,
        },
      },
    }),
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
      // Only process icons in production
      warn: mode === 'development',
    }),
    // Visualizer only in production
    ...(mode === 'production'
      ? [
          visualizer({
            filename: '../../stats.html',
            open: false,
            gzipSize: true,
            brotliSize: true,
            
          }),
          // Gzip compression
          viteCompression({
            algorithm: 'gzip',
            ext: '.gz',
            threshold: 1024,
            deleteOriginFile: false,
            disable: false,
          }),
          // Brotli compression (better compression ratio, newer browsers)
          viteCompression({
            algorithm: 'brotliCompress',
            ext: '.br',
            threshold: 1024,
            deleteOriginFile: false,
            disable: false,
            compressionOptions: {
              level: 11, // Maximum compression level for Brotli
            },
          }),
        ]
      : []),
  ],

  base: './',

  build: {
    outDir: 'dist',
    minify: 'esbuild',
    cssMinify: 'lightningcss',
    cssCodeSplit: true,
    chunkSizeWarningLimit: 600,
    sourcemap: false,
    reportCompressedSize: true,
    target: 'es2020',
    
    // Advanced esbuild configuration
    esbuild: {
      drop: mode === 'production' ? ['console', 'debugger'] : [],
      legalComments: 'none',
      treeShaking: true,
      minifyIdentifiers: true,
      minifySyntax: true,
      minifyWhitespace: true,
    },
    
    rollupOptions: {
      output: {
        // Hash-based file naming for better cache busting
        entryFileNames: 'assets/js/[hash:16].js',
        chunkFileNames: 'assets/js/chunk-[hash:16].js',
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
        
        // Optimized chunk splitting - separate vendor chunks for better caching
        manualChunks: (id) => {
          // Separate vendor code
          if (id.includes('node_modules')) {
            // Separate large dependencies
            if (id.includes('chart.js')) {
              return 'vendor-charts';
            }
            if (id.includes('vue') || id.includes('pinia') || id.includes('@vueuse')) {
              return 'vendor-vue';
            }
            if (id.includes('@floating-ui') || id.includes('@iconify')) {
              return 'vendor-ui';
            }
            return 'vendor';
          }
          
          // Separate route-based code
          if (id.includes('src/components')) {
            return 'components';
          }
          if (id.includes('src/composables')) {
            return 'composables';
          }
        },
        
        // Optimization settings
        inlineDynamicImports: false,
        compact: true,
        generatedCode: {
          preset: 'es2020',
        },
      },
      
      // Enhanced tree-shaking
      treeshake: {
        moduleSideEffects: false,
        preset: 'recommended',
        propertyReadSideEffects: false,
        tryCatchDeoptimization: false,
      },
    },
  },

  // Dependency pre-bundling configuration
  optimizeDeps: {
    include: ['vue', 'pinia', 'chart.js', 'date-fns', '@floating-ui/vue'],
    exclude: ['@iconify/vue'],
  },

  server: {
    // Middleware proxy for development
    proxy: {
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws/, '/ws'),
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path,
      },
      '/q': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
    // Enable gzip compression in dev server
    middlewareMode: false,
    // Optimize for fast HMR
    hmr: {
      protocol: 'ws',
      host: 'localhost',
      port: 5173,
    },
  },

  // Performance monitoring
  logLevel: mode === 'production' ? 'error' : 'info',
}));
