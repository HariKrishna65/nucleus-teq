import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { port: 5173 },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.js',
    coverage: {
      include: ['src/pages/RegisterPage.jsx'],
      reporter: ['text', 'html'],
      thresholds: { lines: 50, functions: 50, branches: 50, statements: 50 },
    },
  },
});
