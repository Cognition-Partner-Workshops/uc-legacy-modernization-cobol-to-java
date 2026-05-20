import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/customers': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/accounts': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/system': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/cards': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/transactions': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/transaction-types': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/batch': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/statements': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      '/api/reports': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      '/api/authorizations': {
        target: 'http://localhost:8087',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
  },
});
