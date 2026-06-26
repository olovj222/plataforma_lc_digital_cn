import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'


export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/setupTests.ts',
    exclude: ['**/node_modules/**', '**/tests/**'],
    // Le decimos a Vitest que procese estas librerías problemáticas internamente
    server: {
      deps: {
        inline: ['@mui/material', '@mui/system', 'react-transition-group']
      }
    }
  }
})
