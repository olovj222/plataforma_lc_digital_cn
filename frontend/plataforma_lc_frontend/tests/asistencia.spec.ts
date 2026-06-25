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

test('Flujo E2E: Admin crea clase, Profesor registra asistencia y la elimina', async ({ page }) => {
  test.slow();

  // ─── PARTE 1: LOGIN COMO ADMIN Y CREAR CLASE ─────────────────────────────
  await page.goto('/');
  await login(page, 'admin1', 'admin123');

  await page.goto('/admin/clase');
  await expect(page.getByRole('heading', { name: 'Gestión de Clases' })).toBeVisible();

  const botonNuevaClase = page.getByRole('button', { name: /registrar nueva clase/i });
  await expect(botonNuevaClase).toBeVisible();
  await botonNuevaClase.click();

  // Usamos fecha y descripción dinámicas para evitar duplicados
  const fechaFutura = new Date();
  fechaFutura.setDate(fechaFutura.getDate() + Math.floor(Math.random() * 365) + 30);
  const fechaHoy = fechaFutura.toISOString().split('T')[0];
  const descripcion = `Clase de prueba E2E ${fechaHoy}`;

  await page.getByLabel('ID del Curso').fill('2');
  await page.locator('input[type="date"]').fill(fechaHoy);
  const textareaDescripcion = page.getByRole('textbox', { name: 'Descripción de la clase' });
  await textareaDescripcion.focus();
  await textareaDescripcion.fill(descripcion);
  await page.getByRole('dialog').getByRole('button', { name: /registrar clase/i }).click();
  await expect(page.getByRole('dialog')).toBeHidden();

  // Verificamos que la clase aparece en la tabla
  await expect(page.getByRole('cell', { name: descripcion })).toBeVisible();

  // ─── PARTE 2: CERRAR SESIÓN ADMIN ────────────────────────────────────────
  await page.getByRole('button', { name: /cerrar sesión/i }).click();
  await page.waitForSelector('input[name="username"]');

  // ─── PARTE 3: LOGIN COMO PROFESOR ────────────────────────────────────────
  await login(page, 'profesor1', 'profesor123');

  // ─── PARTE 4: NAVEGAR A ASISTENCIAS ──────────────────────────────────────
  await page.goto('/profesor/mis-cursos/2/asistencia');
  await expect(page.getByText('Registro de Asistencia')).toBeVisible();

  // ─── PARTE 5: ABRIR DIALOG DE REGISTRO ───────────────────────────────────
  await page.getByRole('button', { name: /registrar asistencia/i }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Registrar Asistencia' })).toBeVisible();

// ─── PARTE 6: SELECCIONAR CLASE ──────────────────────────────────────────
await page.locator('label:has-text("Clase (Sesión)") + .MuiInputBase-root').click();
const primeraClase = page.locator('li[role="option"]').first();
await expect(primeraClase).toBeVisible();
await primeraClase.click({ force: true });
await page.getByRole('heading', { name: 'Registrar Asistencia' }).click();

// ─── PARTE 7: SELECCIONAR ESTUDIANTE ─────────────────────────────────────
await page.locator('label:has-text("Estudiante") + .MuiInputBase-root').click();
const primerEstudiante = page.locator('li[role="option"]').first();
await expect(primerEstudiante).toBeVisible();
const nombreEstudiante = await primerEstudiante.textContent();
await primerEstudiante.click({ force: true });
await page.getByRole('heading', { name: 'Registrar Asistencia' }).click();

// ─── PARTE 8: SELECCIONAR ESTADO ─────────────────────────────────────────
await page.locator('label:has-text("Estado") + .MuiInputBase-root').click();
await page.locator('li:has-text("Presente")').click({ force: true });
await page.getByRole('heading', { name: 'Registrar Asistencia' }).click();
// ─── PARTE 9: INGRESAR FECHA ─────────────────────────────────────────────
await expect(page.locator('.MuiBackdrop-invisible')).toBeHidden();
await page.locator('input[type="date"]').fill(fechaHoy);

  // ─── PARTE 10: GUARDAR ───────────────────────────────────────────────────
  const valorClase = await page.locator('label:has-text("Clase (Sesión)") + .MuiInputBase-root').locator('input').inputValue();
  console.log('Valor clase seleccionado:', valorClase);
  const valorEstudiante = await page.locator('label:has-text("Estudiante") + .MuiInputBase-root').locator('input').inputValue();
  console.log('Valor estudiante seleccionado:', valorEstudiante);
  page.on('response', response => {
  if (response.url().includes('/asistencia')) {
    console.log('Asistencia status:', response.status());
    response.text().then(body => console.log('Asistencia body:', body)).catch(() => {});
  }
});
await page.getByRole('button', { name: /guardar/i }).click({ force: true });
  await page.getByRole('button', { name: /guardar/i }).click({ force: true });
  await expect(page.getByRole('dialog')).toBeHidden();

  // ─── PARTE 11: VERIFICAR QUE APARECE EN LA TABLA ─────────────────────────
  const filaAsistencia = page.getByRole('cell', { name: new RegExp(nombreEstudiante || '', 'i') }).first();
  await expect(filaAsistencia).toBeVisible();

  // ─── PARTE 12: ELIMINAR LA ASISTENCIA ────────────────────────────────────
  const fila = page.getByRole('row').filter({ hasText: nombreEstudiante || '' }).last();
  const botonEliminar = fila.getByRole('button');

  page.on('dialog', dialog => dialog.accept());
  await botonEliminar.click();

  await expect(filaAsistencia).toBeHidden();
});