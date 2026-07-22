/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#1C1F26',
        inksoft: '#6B7280',
        bg: '#F7F7F5',
        card: '#FFFFFF',
        line: '#DEDEDA',
        linestrong: '#1C1F26',
        amber: '#B8860B',
        amberbg: '#FBF1DC',
        green: '#2F6E4F',
        greenbg: '#E9F3EC',
        red: '#A23B3B',
        redbg: '#FBEAEA',
        sidebar: '#1C1F26',
      },
      fontFamily: {
        sans: ['"IBM Plex Sans"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
}
