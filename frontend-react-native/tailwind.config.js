/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/app/**/*.{js,jsx,ts,tsx}',
    './src/components/**/*.{js,jsx,ts,tsx}',
    './src/auth/**/*.{js,jsx,ts,tsx}',
  ],
  presets: [require('nativewind/preset')],
  theme: {
    extend: {
      colors: {
        so2: {
          primary: '#3F00FF',
          success: '#D1FAE5',
          successText: '#065F46',
          pending: '#FEF3C7',
          pendingText: '#92400E',
          danger: '#FEE2E2',
          dangerText: '#991B1B',
          draft: '#F3F4F6',
          draftText: '#1F2937',
          white: '#FFFFFF',
          background: '#F9FAFB',
          card: '#FFFFFF',
          border: '#E5E7EB',
          text: '#111827',
          textMuted: '#6B7280',
        },
      },
    },
  },
  plugins: [],
};
