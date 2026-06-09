/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          primary: '#1E3A8A',    // Azul Câmaras
          secondary: '#059669',  // Verde Feirantes
          accent: '#F59E0B',     // Âmbar Notícias
          dark: '#0F172A',       // Texto Principal
          light: '#F8FAFC',      // Fundo App
        }
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui'], // Tipografia limpa
      }
    },
  },
  plugins: [],
}
