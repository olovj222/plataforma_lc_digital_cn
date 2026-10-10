import { test, expect } from '@playwright/test';

/**
 * E2E: Flujo completo de asistencia.
 *
 * La autenticación Azure AD NO se gestiona aquí. El archivo global-setup.ts
 * inyecta el token en el localStorage antes de que arranque cualquier test,
 * y playwright.config.ts carga ese estado de sesión en cada test automáticamente.
 *
 * Variable de entorno necesaria (aparte de las del global-setup):
 *   TEST_FRONTEND_URL → URL pública del frontend (leída en playwright.config.ts).
 */
test.describe.configure({ mode: 'serial' });

test('Flujo E2E: Admin crea clase, Profesor registra asistencia y la elimina', async ({ page }) => {
  test.slow();

  // ⏱️ Sincronización de infraestructura (Eureka & Gateway)
  // Se reduce a 5s porque la sesión ya está inyectada, no hay redirect de Azure AD.
  await page.waitForTimeout(5000);

  // ─── PARTE 1: NAVEGAR COMO ADMIN A GESTIÓN DE CLASES ─────────────────────
  // La sesión ya está activa gracias al storageState del global-setup.
  // No es necesario ningún login manual.
  await page.goto('/admin/clase');
  await expect(page.getByRole('heading', { name: 'Gestión de Clases' })).toBeVisible();

  const botonNuevaClase = page.getByRole('button', { name: /registrar nueva clase/i });
  await expect(botonNuevaClase).toBeVisible();
  await botonNuevaClase.click();

  // Usamos fecha dinámica para evitar duplicados entre ejecuciones
  const fechaFutura = new Date();
  fechaFutura.setDate(fechaFutura.getDate() + Math.floor(Math.random() * 365) + 30);
  const fechaHoy = fechaFutura.toISOString().split('T')[0];
  const descripcion = `Clase de prueba E2E ${fechaHoy}`;

  await page.getByLabel('ID del Curso').fill('2');
  await page.locator('input[type="date"]').fill(fechaHoy);
  const textareaDescripcion = page.getByRole('textbox', { name: 'Descripción de la clase' });
  await textareaDescripcion.focus();
  await textareaDescripcion.fill(descripcion);
  await expect(textareaDescripcion).toHaveValue(descripcion);

  // 🔄 Captura segura del POST a /clase
  const respuestaClasePromise = page.waitForResponse(
    response => response.url().includes('/clase') && response.request().method() === 'POST',
    { timeout: 10000 }
  );

  await page.getByRole('dialog').getByRole('button', { name: /registrar clase/i }).click();

  const respuestaClase = await respuestaClasePromise;
  console.log('👉 [E2E] Clase HTTP status:', respuestaClase.status());

  await expect(page.getByRole('dialog')).toBeHidden();
  await expect(page.getByRole('cell', { name: descripcion })).toBeVisible();

  // ─── PARTE 2: CAMBIAR A ROL PROFESOR ─────────────────────────────────────
  // En lugar de hacer logout/login (que requeriría Azure AD de nuevo),
  // navegamos directamente a la ruta del profesor. La sesión soporta ambos roles
  // porque el token de prueba tiene los permisos necesarios.
  await page.goto('/profesor/mis-cursos/2/asistencia');
  await expect(page.getByText('Registro de Asistencia')).toBeVisible();

  // ─── PARTE 3: ABRIR DIALOG DE REGISTRO ───────────────────────────────────
  await page.getByRole('button', { name: /registrar asistencia/i }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Registrar Asistencia' })).toBeVisible();

  // ─── PARTE 4: SELECCIONAR CLASE ──────────────────────────────────────────
  await page.locator('label:has-text("Clase (Sesión)") + .MuiInputBase-root').click();
  const primeraClase = page.locator('li[role="option"]').first();
  await expect(primeraClase).toBeVisible();
  await primeraClase.click({ force: true });
  await page.getByRole('heading', { name: 'Registrar Asistencia' }).click();

  // ─── PARTE 5: SELECCIONAR ESTUDIANTE ─────────────────────────────────────
  await page.locator('label:has-text("Estudiante") + .MuiInputBase-root').click();
  const primerEstudiante = page.locator('li[role="option"]').first();
  await expect(primerEstudiante).toBeVisible();
  const nombreEstudiante = await primerEstudiante.textContent();
  await primerEstudiante.click({ force: true });
  await page.getByRole('heading', { name: 'Registrar Asistencia' }).click();

  // ─── PARTE 6: SELECCIONAR ESTADO ─────────────────────────────────────────
  await page.locator('label:has-text("Estado") + .MuiInputBase-root').click();
  await page.locator('li:has-text("Presente")').click({ force: true });
  await page.getByRole('heading', { name: 'Registrar Asistencia' }).click();

  // ─── PARTE 7: INGRESAR FECHA ─────────────────────────────────────────────
  await expect(page.locator('.MuiBackdrop-invisible')).toBeHidden();
  await page.locator('input[type="date"]').fill(fechaHoy);

  // ─── PARTE 8: GUARDAR ────────────────────────────────────────────────────
  const respuestaAsistenciaPromise = page.waitForResponse(
    response => response.url().includes('/asistencia') && response.request().method() === 'POST',
    { timeout: 10000 }
  );

  await page.getByRole('button', { name: /guardar/i }).click({ force: true });

  const respuestaAsistencia = await respuestaAsistenciaPromise;
  console.log('👉 [E2E] Asistencia HTTP status:', respuestaAsistencia.status());

  await expect(page.getByRole('dialog')).toBeHidden();

  // ─── PARTE 9: VERIFICAR QUE APARECE EN LA TABLA ──────────────────────────
  const filaAsistencia = page.getByRole('cell', { name: new RegExp(nombreEstudiante || '', 'i') }).first();
  await expect(filaAsistencia).toBeVisible();

  // ─── PARTE 10: ELIMINAR LA ASISTENCIA ────────────────────────────────────
  const fila = page.getByRole('row').filter({ hasText: nombreEstudiante || '' }).last();
  const botonEliminar = fila.getByRole('button');

  page.on('dialog', dialog => dialog.accept());
  await botonEliminar.click();

  await expect(filaAsistencia).toBeHidden();
});