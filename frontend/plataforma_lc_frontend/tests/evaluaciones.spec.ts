import { test, expect } from '@playwright/test';

// ─── HELPER: LOGIN ───────────────────────────────────────────────────────────
async function login(page: any, username: string, password: string) {
  await page.waitForSelector('input[name="username"]');
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);
  await Promise.all([
    page.waitForNavigation(),
    page.click('id=kc-login')
  ]);
}

test('Flujo E2E: Profesor crea una evaluación y la elimina', async ({ page }) => {
  test.slow();
  await page.waitForTimeout(20000);

  // ─── PARTE 1: LOGIN COMO PROFESOR ────────────────────────────────────────
  await page.goto('/');
  await login(page, 'profesor1', 'profesor123');

  // ─── PARTE 2: NAVEGAR A EVALUACIONES ─────────────────────────────────────
  await page.goto('/profesor/mis-cursos/2/evaluaciones');
  await expect(page.getByRole('heading', { name: 'Evaluaciones del Curso' })).toBeVisible();

  // ─── PARTE 3: ABRIR DIALOG DE NUEVA EVALUACIÓN ───────────────────────────
  await page.getByRole('button', { name: /nueva evaluación/i }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Nueva Evaluación' })).toBeVisible();

  // ─── PARTE 4: LLENAR FORMULARIO ──────────────────────────────────────────
  const nombreEval = `Control E2E ${new Date().toISOString()}`;
  await page.getByLabel('Nombre').fill(nombreEval);
  await page.getByLabel('ID Estudiante').fill('11');
  await page.getByLabel('Calificación').fill('6.5');

  // ─── PARTE 5: GUARDAR ────────────────────────────────────────────────────
  await page.getByRole('button', { name: /guardar/i }).click();
  await expect(page.getByRole('dialog')).toBeHidden();

  // ─── PARTE 6: VERIFICAR QUE APARECE EN LA TABLA ──────────────────────────
  await expect(page.getByRole('cell', { name: nombreEval })).toBeVisible();

  // ─── PARTE 7: ELIMINAR LA EVALUACIÓN ─────────────────────────────────────
  const fila = page.getByRole('row').filter({ hasText: nombreEval });
  const botonEliminar = fila.getByRole('button');

  page.on('dialog', dialog => dialog.accept());
  await botonEliminar.click();

  // ─── PARTE 8: VERIFICAR QUE DESAPARECIÓ ──────────────────────────────────
  await expect(page.getByRole('cell', { name: nombreEval })).toBeHidden();
});