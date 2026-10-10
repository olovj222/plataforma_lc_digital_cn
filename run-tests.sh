#!/bin/bash

# ─────────────────────────────────────────────────────────────────────────────
# run-tests.sh — Ejecuta todos los tests de la plataforma LC
#
# USO LOCAL (sin AWS):
#   bash run-tests.sh
#   → Corre unitarios + integración. Los E2E de backend y frontend se saltan.
#
# USO CON EC2 (tras cada reinicio del laboratorio AWS):
#   export TEST_BASE_URL=http://<nueva-ip-ec2>:8085
#   export TEST_JWT_TOKEN=$(bash scripts/get-test-token.sh)
#   export TEST_FRONTEND_URL=http://<nueva-ip-ec2>:5173   # si el frontend está en EC2
#   bash run-tests.sh
#   → Corre TODO, incluyendo los E2E contra el stack real.
# ─────────────────────────────────────────────────────────────────────────────

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$REPO_ROOT/plataforma_lc/sistema_gestion"
FRONTEND_DIR="$REPO_ROOT/frontend/plataforma_lc_frontend"

FAILED=0

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║         PLATAFORMA LC — SUITE DE TESTS               ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

# Mostramos el entorno activo para trazabilidad
if [ -n "${TEST_BASE_URL:-}" ]; then
  echo "  🌐 Modo AWS   → TEST_BASE_URL = $TEST_BASE_URL"
else
  echo "  🏠 Modo local → Los E2E de backend se saltarán (TEST_BASE_URL no definida)"
fi
echo ""

# ─── 1. TESTS DE BACKEND (todos los ms con un solo comando) ──────────────────
echo "▶ [1/3] Ejecutando tests de backend (Maven)..."
echo ""

cd "$BACKEND_DIR" || { echo "❌ No se encontró $BACKEND_DIR"; exit 1; }

mvn test --no-transfer-progress
BACKEND_EXIT=$?

if [ $BACKEND_EXIT -ne 0 ]; then
  echo ""
  echo "❌ Tests de backend FALLARON"
  FAILED=1
else
  echo ""
  echo "✅ Tests de backend OK"
fi

echo ""

# ─── 2. TESTS UNITARIOS E INTEGRACIÓN DEL FRONTEND (Vitest) ──────────────────
echo "▶ [2/3] Ejecutando tests unitarios e integración del frontend (Vitest)..."
echo ""

cd "$FRONTEND_DIR" || { echo "❌ No se encontró $FRONTEND_DIR"; exit 1; }

npx vitest run
VITEST_EXIT=$?

if [ $VITEST_EXIT -ne 0 ]; then
  echo ""
  echo "❌ Tests de Vitest FALLARON"
  FAILED=1
else
  echo ""
  echo "✅ Tests de Vitest OK"
fi

echo ""

# ─── 3. TESTS E2E DEL FRONTEND (Playwright) ──────────────────────────────────
echo "▶ [3/3] Ejecutando tests E2E (Playwright)..."
echo ""

cd "$FRONTEND_DIR" || { echo "❌ No se encontró $FRONTEND_DIR"; exit 1; }

# Corremos en modo headless por defecto (compatible con CI y con EC2 sin GUI).
# Para ver el navegador en local: npx playwright test --headed
npx playwright test
PLAYWRIGHT_EXIT=$?

if [ $PLAYWRIGHT_EXIT -ne 0 ]; then
  echo ""
  echo "❌ Tests E2E FALLARON — abriendo reporte..."
  FAILED=1
  npx playwright show-report
fi

echo ""

# ─── RESUMEN FINAL ────────────────────────────────────────────────────────────
echo "╔══════════════════════════════════════════════════════╗"
echo "║                   RESUMEN FINAL                      ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

[ $BACKEND_EXIT -eq 0 ]    && echo "  ✅ Backend     — OK" || echo "  ❌ Backend     — FALLÓ"
[ $VITEST_EXIT -eq 0 ]     && echo "  ✅ Vitest      — OK" || echo "  ❌ Vitest      — FALLÓ"
[ $PLAYWRIGHT_EXIT -eq 0 ] && echo "  ✅ E2E         — OK" || echo "  ❌ E2E         — FALLÓ"

echo ""

if [ $FAILED -ne 0 ]; then
  echo "❌ Algunos tests fallaron."
  exit 1
else
  echo "✅ Todos los tests pasaron."
  exit 0
fi
