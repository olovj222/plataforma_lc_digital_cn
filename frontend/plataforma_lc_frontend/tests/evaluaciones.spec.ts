import { test, expect } from '@playwright/test';

/**
 * E2E: Flujo completo de evaluaciones.
 *
 * La autenticación Azure AD NO se gestiona aquí. El archivo global-setup.ts
 * inyecta el token en el localStorage antes de que arranque cualquier test,
 * y playwright.config.ts carga ese estado de sesión en cada test automáticamente.
 *
 * Variable de entorno necesaria (aparte de las del global-setup):
 *   TEST_FRONTEND_URL → URL pública del frontend (leída en playwright.config.ts).
 */
test.describe.configure({ mode: 'serial' });

test('Flujo E2E: Profesor crea una evaluación y la elimina', async ({ page }) => {
  test.slow();

  // ─── PARTE 1: NAVEGAR DIRECTAMENTE A EVALUACIONES ────────────────────────
  // La sesión ya está activa gracias al storageState del global-setup.
  // No es necesario ningún login manual ni helper de Keycloak.
  await page.goto('/profesor/mis-cursos/2/evaluaciones');
  await expect(page.getByRole('heading', { name: 'Evaluaciones del Curso' })).toBeVisible();

  // ─── PARTE 2: ABRIR DIALOG DE NUEVA EVALUACIÓN ───────────────────────────
  await page.getByRole('button', { name: /nueva evaluación/i }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Nueva Evaluación' })).toBeVisible();

  // ─── PARTE 3: LLENAR FORMULARIO ──────────────────────────────────────────
  // Nombre dinámico para evitar conflictos entre ejecuciones
  const nombreEval = `Control E2E ${new Date().toISOString()}`;
  await page.getByLabel('Nombre').fill(nombreEval);
  await page.getByLabel('ID Estudiante').fill('11');
  await page.getByLabel('Calificación').fill('6.5');

  // ─── PARTE 4: GUARDAR ────────────────────────────────────────────────────
  const respuestaEvalPromise = page.waitForResponse(
    response => response.url().includes('/evaluaciones') && response.request().method() === 'POST',
    { timeout: 10000 }
  );

  await page.getByRole('button', { name: /guardar/i }).click();

  const respuestaEval = await respuestaEvalPromise;
  console.log('👉 [E2E] Evaluación HTTP status:', respuestaEval.status());

  await expect(page.getByRole('dialog')).toBeHidden();

  // ─── PARTE 5: VERIFICAR QUE APARECE EN LA TABLA ──────────────────────────
  await expect(page.getByRole('cell', { name: nombreEval })).toBeVisible();

  // ─── PARTE 6: ELIMINAR LA EVALUACIÓN ─────────────────────────────────────
  const fila = page.getByRole('row').filter({ hasText: nombreEval });
  const botonEliminar = fila.getByRole('button');

  page.on('dialog', dialog => dialog.accept());
  await botonEliminar.click();

  // ─── PARTE 7: VERIFICAR QUE DESAPARECIÓ ──────────────────────────────────
  await expect(page.getByRole('cell', { name: nombreEval })).toBeHidden();
});