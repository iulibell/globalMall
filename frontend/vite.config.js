import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  define: {
    __VUE_PROD_DEVTOOLS__: false,
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    /** 与 globalLogistic/frontend（5174）区分，避免同时开发两个项目时端口冲突 */
    port: 5173,
    strictPort: true,
    proxy: {
      '/system': {
        target: 'http://127.0.0.1:8205',
        changeOrigin: true,
      },
      '/search': {
        target: 'http://127.0.0.1:8204',
        changeOrigin: true,
      },
      '/admin': {
        target: 'http://127.0.0.1:8206',
        changeOrigin: true,
      },
      '/portal': {
        target: 'http://127.0.0.1:8207',
        changeOrigin: true,
      },
    },
  },
})
