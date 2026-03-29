import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://app:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://app:8080',
        ws: true,
        changeOrigin: true,
      },
    },
  },
})
