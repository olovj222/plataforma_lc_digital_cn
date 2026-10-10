import { defineConfig, devices } from '@playwright/test';
import path from 'path';

export default defineConfig({
  testDir: './tests',
  // El global setup inyecta el token Azure AD en el localStorage una sola vez
  // antes de que arranquen los tests.
  globalSetup: './tests/global-setup.ts',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  // Timeout más alto para flujos con Azure AD y esperas de infraestructura
  timeout: 60_000,
  use: {
    // La URL se lee de la variable de entorno.
    // En local: TEST_FRONTEND_URL=http://localhost:5173 (o se omite y usa el default)
    // En AWS:   TEST_FRONTEND_URL=http://<nueva-ip-ec2>:5173
    // Así nunca hay una IP hardcodeada en el código.
    baseURL: process.env.TEST_FRONTEND_URL ?? 'http://localhost:5173',
    trace: 'on-first-retry',
    // Todos los tests cargan la sesión guardada por global-setup → ya están "logueados"
    storageState: path.join(__dirname, 'tests/.auth/session.json'),
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});