export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
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
