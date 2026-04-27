/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts,scss}"],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        display: ['Clash Display', 'sans-serif'],
        heading: ['Syne', 'sans-serif'],
        body:    ['DM Sans', 'sans-serif'],
      },
      colors: {
        void:    '#050508',
        base:    '#090910',
        surface: '#0e0e18',
        elevated:'#13131f',
        card:    '#111120',
        accent: {
          primary:   '#6366f1',
          secondary: '#8b5cf6',
          cyan:      '#22d3ee',
          gold:      '#f59e0b',
          green:     '#10b981',
          rose:      '#f43f5e',
        }
      },
      animation: {
        'fade-up':  'fadeUp 0.5s ease forwards',
        'fade-in':  'fadeIn 0.4s ease forwards',
        'float':    'float 3s ease-in-out infinite',
        'spin-slow':'spin 8s linear infinite',
        'pulse-glow':'pulse-glow 2s ease-in-out infinite',
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
      }
    }
  },
  plugins: [],
};
