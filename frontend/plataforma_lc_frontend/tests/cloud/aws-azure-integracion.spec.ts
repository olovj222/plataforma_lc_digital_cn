import { test, expect } from '@playwright/test';
import fs from 'node:fs';

const FRONTEND_URL = process.env.FRONTEND_URL!;
const GATEWAY_URL = process.env.GATEWAY_URL!.replace(/\/$/, '');
const AZURE_USER = process.env.AZURE_USER!;
const AZURE_PASS = process.env.AZURE_PASS!;
const RABBITMQ_UI_URL = process.env.RABBITMQ_UI_URL!.replace(/\/$/, '');
const RABBITMQ_USER = process.env.RABBITMQ_USER || 'admin';
const RABBITMQ_PASS = process.env.RABBITMQ_PASS || 'admin123';
const KAFKA_UI_URL = process.env.KAFKA_UI_URL!.replace(/\/$/, '');

const AUTH_STATE_FILE = 'evidencias-cloud/.auth-state.json';
fs.mkdirSync('evidencias-cloud', { recursive: true });

test.describe.serial('Pruebas de Integración Cloud - AWS & Azure', () => {
  // ══════════════════════════════════════════════════════════════════════════
  // 1. AUTENTICACIÓN DE USUARIOS (AZURE ENTRA ID + REDIRECCIÓN POR ROL)
  // ══════════════════════════════════════════════════════════════════════════
  test('1. Autenticación de usuarios mediante Azure Entra ID', async ({ page, context }) => {
    await page.goto(FRONTEND_URL);

    // MsalAuthenticationTemplate redirige automáticamente a Microsoft
    await page.waitForURL(/login\.microsoftonline\.com/, { timeout: 25_000 });

    const emailInput = page.locator('input[type="email"]');
    await emailInput.waitFor({ state: 'visible' });
    await emailInput.fill(AZURE_USER);
    await page.locator('input[type="submit"]').click();

    const passInput = page.locator('input[type="password"]');
    await passInput.waitFor({ state: 'visible' });
    await passInput.fill(AZURE_PASS);
    await page.locator('input[type="submit"]').click();

    // Pantalla "¿Mantener la sesión iniciada?" -> clic automático en Sí/Yes (#idSIButton9)
    const staySignedIn = page.locator('#idSIButton9');
    if (await staySignedIn.isVisible({ timeout: 6_000 }).catch(() => false)) {
      await staySignedIn.click();
    }

    // Verificar retorno al frontend y redirección automática según rol (ADMIN o PROFESOR)
    await page.waitForURL(/\/(admin\/cursos|profesor\/mis-cursos)/, { timeout: 25_000 });
    await page.waitForLoadState('networkidle');

    // Guardar localStorage con el token MSAL para la siguiente prueba
    await context.storageState({ path: AUTH_STATE_FILE });

    await page.screenshot({
      path: 'evidencias-cloud/01-autenticacion-azure.png',
      fullPage: true
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 2. CONSUMO DE MICROSERVICIOS PROTEGIDOS DESDE FRONTEND USANDO TOKEN
  // ══════════════════════════════════════════════════════════════════════════
  test('2. Consumo de microservicios protegidos con JWT y rechazo sin token', async ({
    browser,
    request
  }) => {
    const context = await browser.newContext({ storageState: AUTH_STATE_FILE });
    const page = await context.newPage();

    let calledEndpointUrl = '';
    let sentAuthHeader = '';
    let responseStatus = 0;

    // Capturar la petición real que envía Axios hacia AWS API Gateway
    const gatewayResponsePromise = page.waitForResponse(
      (res) => {
        const isGateway = res.url().startsWith(GATEWAY_URL);
        const auth = res.request().headers()['authorization'] || '';
        if (isGateway && auth.startsWith('Bearer ')) {
          calledEndpointUrl = res.url();
          sentAuthHeader = auth;
          responseStatus = res.status();
          return true;
        }
        return false;
      },
      { timeout: 25_000 }
    );

    await page.goto(FRONTEND_URL);
    await gatewayResponsePromise;
    await page.waitForLoadState('networkidle');

    // A) Validar que la petición desde el frontend llevó Bearer JWT y respondió 200 OK
    expect(sentAuthHeader).toMatch(/^Bearer [A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+$/);
    expect(responseStatus).toBe(200);

    // B) Replicar la llamada al MISMO endpoint SIN token para comprobar rechazo (401/403)
    const unauthRes = await request.get(calledEndpointUrl);
    expect([401, 403]).toContain(unauthRes.status());

    await page.screenshot({
      path: 'evidencias-cloud/02-consumo-protegido-gateway.png',
      fullPage: true
    });
    await context.close();
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 3. MONITOREO DE COLAS RABBITMQ CON DASHBOARD (CLÚSTER 2 NODOS)
  // ══════════════════════════════════════════════════════════════════════════
  test('3. Monitoreo de clúster y colas en RabbitMQ Management Dashboard', async ({
    page,
    request
  }) => {
    await page.goto(RABBITMQ_UI_URL);

    await page.locator('input[name="username"]').fill(RABBITMQ_USER);
    await page.locator('input[name="password"]').fill(RABBITMQ_PASS);
    await page.locator('input[type="submit"]').click();

    // Capturar vista Overview con el clúster (rabbitmq-1 y rabbitmq-2)
    await expect(page.locator('#main')).toContainText(/rabbitmq-1/i);
    await page.screenshot({
      path: 'evidencias-cloud/03a-rabbitmq-cluster-overview.png',
      fullPage: true
    });

    // Navegar a la pestaña Queues and Streams y validar API de gestión
    await page.locator('#tabs a[href="#/queues"]').click();
    await expect(page.locator('#main')).toContainText(/Name|Ready|Unacked|Total/i);

    const basicToken = Buffer.from(`${RABBITMQ_USER}:${RABBITMQ_PASS}`).toString('base64');
    const queuesRes = await request.get(`${RABBITMQ_UI_URL}/api/queues`, {
      headers: { Authorization: `Basic ${basicToken}` }
    });
    expect(queuesRes.status()).toBe(200);

    await page.screenshot({
      path: 'evidencias-cloud/03b-rabbitmq-colas.png',
      fullPage: true
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 4. MONITOREO DE TOPICS Y MÉTRICAS CON KAFKA UI (CLÚSTER 3 BROKERS)
  // ══════════════════════════════════════════════════════════════════════════
  test('4. Monitoreo de brokers, topics y métricas en Kafka UI', async ({ page, request }) => {
    await page.goto(KAFKA_UI_URL);

    // Verificar salud del clúster plataforma-cluster vía API de Kafka UI
    const clustersRes = await request.get(`${KAFKA_UI_URL}/api/clusters`);
    expect(clustersRes.status()).toBe(200);
    const clusters = await clustersRes.json();
    expect(clusters[0]?.name).toBe('plataforma-cluster');
    expect(clusters[0]?.status).toBe('online');

    // Verificar visualización en la interfaz web y navegar a Topics
    await expect(page.locator('body')).toContainText(/plataforma-cluster/i);
    await page.getByRole('link', { name: /topics/i }).first().click();
    await expect(page.locator('body')).toContainText(/audit-events-v1/i);

    await page.screenshot({
      path: 'evidencias-cloud/04-kafka-ui-topics-metricas.png',
      fullPage: true
    });
  });
});