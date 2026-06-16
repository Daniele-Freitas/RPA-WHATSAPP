/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#e1fbf4',
          100: '#baf5e3',
          400: '#4ade80',
          500: '#25D366', // Verde Oficial do WhatsApp
          600: '#16a34a', // Hover dos botões
          900: '#14532d',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'], 
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
};