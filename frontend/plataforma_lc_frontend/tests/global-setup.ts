import { chromium, FullConfig } from '@playwright/test';
import path from 'path';
import fs from 'fs';

/**
 * global-setup.ts — Se ejecuta UNA SOLA VEZ antes de toda la suite de Playwright.
 *
 * Problema que resuelve:
 *   Azure AD usa una pantalla de login externa (login.microsoftonline.com) que
 *   Playwright no puede controlar como un formulario HTML normal. Intentar hacer
 *   page.fill('input[name="username"]') contra la pantalla real de Microsoft falla
 *   porque esa página tiene protecciones anti-bot y flujos de MFA.
 *
 * Solución:
 *   En vez de simular el login de usuario, inyectamos directamente el token JWT
 *   en el localStorage de la aplicación. Esto replica exactamente lo que hace
 *   MSAL (@azure/msal-react) cuando el usuario ya ha iniciado sesión.
 *
 * Cómo funciona:
 *   1. El script abre el frontend.
 *   2. Inyecta el token de Azure AD (obtenido con get-test-token.sh) en el
 *      localStorage, con el formato que espera la librería MSAL.
 *   3. Guarda el estado del navegador (cookies + localStorage) en un archivo.
 *   4. Todos los tests reutilizan ese estado → ya "están logueados".
 *
 * Variables de entorno requeridas:
 *   TEST_FRONTEND_URL → URL pública del frontend. Ejemplo: http://54.123.45.67:5173
 *                       (o http://localhost:5173 en local)
 *   TEST_JWT_TOKEN    → Token obtenido con scripts/get-test-token.sh
 */

export const STORAGE_STATE = path.join(__dirname, '.auth/session.json');

export default async function globalSetup(config: FullConfig) {
  const frontendUrl = process.env.TEST_FRONTEND_URL ?? 'http://localhost:5173';
  const jwtToken    = process.env.TEST_JWT_TOKEN ?? '';

  if (!jwtToken) {
    console.warn('⚠️  TEST_JWT_TOKEN no está definido. Los tests E2E de frontend pueden fallar.');
  }

  // Aseguramos que el directorio .auth existe
  const authDir = path.join(__dirname, '.auth');
  if (!fs.existsSync(authDir)) fs.mkdirSync(authDir, { recursive: true });

  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page    = await context.newPage();

  // Navegamos al frontend para que el origen (origin) sea el correcto
  await page.goto(frontendUrl);

  // Inyectamos el token en el localStorage con el formato que espera MSAL.
  // La clave sigue el patrón: <clientId>.<tenantId>-login.windows.net-<clientId>--
  // MSAL busca claves que contengan "accesstoken" en el localStorage.
  const clientId = '70af68d7-f0b7-4897-9d64-4a0b0791ca70';
  const tenantId = '2dcf78c8-4359-4115-8b06-50c5a455e4e0';
  const msalKey  = `${clientId}.${tenantId}-login.windows.net-accesstoken-${clientId}--`;

  await page.evaluate(
    ({ key, token }: { key: string; token: string }) => {
      const tokenEntry = {
        homeAccountId: 'test-account',
        environment:   'login.windows.net',
        clientId,
        credentialType: 'AccessToken',
        secret: token,
        cachedAt:   String(Math.floor(Date.now() / 1000)),
        expiresOn:  String(Math.floor(Date.now() / 1000) + 3600),
        extendedExpiresOn: String(Math.floor(Date.now() / 1000) + 3600),
        target: 'User.Read',
        tokenType: 'Bearer',
      };
      localStorage.setItem(key, JSON.stringify(tokenEntry));
    },
    { key: msalKey, token: jwtToken, clientId }
  );

  // Guardamos el estado del navegador (localStorage + cookies)
  await context.storageState({ path: STORAGE_STATE });

  await browser.close();
  console.log(`✅ Sesión de prueba guardada en ${STORAGE_STATE}`);
}
