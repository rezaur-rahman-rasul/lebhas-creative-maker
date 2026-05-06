/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        canvas: 'rgb(var(--color-canvas) / <alpha-value>)',
        surface: 'rgb(var(--color-surface) / <alpha-value>)',
        ink: 'rgb(var(--color-ink) / <alpha-value>)',
        muted: 'rgb(var(--color-muted) / <alpha-value>)',
        border: 'rgb(var(--color-border) / <alpha-value>)',
        brand: {
          50: '#eefdf7',
          100: '#d6faec',
          500: '#16a679',
          600: '#0b8f68',
          700: '#067052',
          900: '#063f31',
        },
        accent: {
          500: '#2563eb',
          600: '#1d4ed8',
        },
        alert: {
          500: '#dc2626',
          600: '#b91c1c',
        },
      },
      boxShadow: {
        soft: '0 18px 60px rgba(15, 23, 42, 0.08)',
      },
      fontFamily: {
        sans: ['Inter', 'Noto Sans Bengali', 'system-ui', 'sans-serif'],
      },
    },
  },
};
