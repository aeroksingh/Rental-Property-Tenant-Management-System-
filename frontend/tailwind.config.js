/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        paper: '#F2F4F2',
        surface: '#FFFFFF',
        ink: '#17242B',
        'ink-soft': '#4B5A60',
        border: '#DCE1DC',
        pine: {
          DEFAULT: '#1B4B43',
          hover: '#123832',
          light: '#E4EEEB',
        },
        gold: {
          DEFAULT: '#B8863B',
          light: '#F6ECD9',
        },
        rust: {
          DEFAULT: '#B23B30',
          light: '#F7E4E1',
        },
        steel: {
          DEFAULT: '#3E6FA0',
          light: '#E4ECF4',
        },
        moss: {
          DEFAULT: '#2E7D53',
          light: '#E2F0E7',
        },
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
