/** @type {import('tailwindcss').Config} */
export default {
  // Enable class-based dark mode so toggling the `dark` class on <html> controls the theme
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: 'var(--ink)',
        inksoft: 'var(--ink-soft)',
        bg: 'var(--bg)',
        card: 'var(--card)',
        line: 'var(--line)',
        linestrong: 'var(--line-strong)',
        amber: 'var(--amber)',
        amberbg: 'var(--amber-bg)',
        green: 'var(--green)',
        greenbg: 'var(--green-bg)',
        red: 'var(--red)',
        redbg: 'var(--red-bg)',
        sidebar: 'var(--sidebar)',
      },
      fontFamily: {
        sans: ['"IBM Plex Sans"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
}
