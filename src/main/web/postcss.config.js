export default {
  plugins: {
    // Tailwind CSS is now handled by @tailwindcss/vite plugin
    // Minifier CSS en production
    ...(process.env.NODE_ENV === 'production' && {
      cssnano: {
        preset: [
          'default',
          {
            discardComments: {
              removeAll: true,
            },
            normalizeWhitespace: true,
          },
        ],
      },
    }),
  },
};
