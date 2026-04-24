/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#6366F1',
          dark: '#4F46E5',
          light: '#818CF8'
        },
        surface: {
          DEFAULT: '#0F172A',
          card: '#1E293B',
          border: '#334155',
        },
        text: {
          primary: '#F8FAFC',
          muted: '#94A3B8',
          inverse: '#0F172A'
        },
        success: '#10B981',
        warning: '#F59E0B',
        danger: '#EF4444',
        info: '#3B82F6'
      }
    },
  },
  plugins: [],
}
