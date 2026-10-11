#!/bin/bash

# ─────────────────────────────────────────────────────────────────────────────
# run-tests.sh — Ejecuta todos los tests de la plataforma LC vía Docker
# ─────────────────────────────────────────────────────────────────────────────

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$REPO_ROOT/plataforma_lc/sistema_gestion"
FRONTEND_DIR="$REPO_ROOT/frontend/plataforma_lc_frontend"

FAILED=0

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║    PLATAFORMA LC — SUITE DE TESTS (DOCKER IN HOST)   ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

# Mostramos el entorno activo para trazabilidad
if [ -n "${TEST_BASE_URL:-}" ]; then
  echo "  🌐 Modo AWS   → TEST_BASE_URL = $TEST_BASE_URL"
else
  echo "  🏠 Modo local → Los E2E de backend se saltarán (TEST_BASE_URL no definida)"
fi
echo ""

# ─── 1. TESTS DE BACKEND (Maven en contenedor) ────────────────────────────────
echo "▶ [1/3] Ejecutando tests de backend (Maven en Docker)..."
echo ""

if [ ! -d "$BACKEND_DIR" ]; then
  echo "❌ No se encontró $BACKEND_DIR"
  exit 1
fi

docker run --rm \
  --net=host \
  -v "$BACKEND_DIR":/app \
  -w /app \
  -v "$HOME/.m2":/root/.m2 \
  -e TEST_BASE_URL="$TEST_BASE_URL" \
  -e TEST_JWT_TOKEN="$TEST_JWT_TOKEN" \
  -e TEST_FRONTEND_URL="$TEST_FRONTEND_URL" \
  maven:3.9-eclipse-temurin-17 mvn test --no-transfer-progress

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

# ─── 2. TESTS UNITARIOS E INTEGRACIÓN DEL FRONTEND (Vitest en Docker) ──────────
echo "▶ [2/3] Ejecutando tests unitarios e integración del frontend (Vitest)..."
echo ""

if [ ! -d "$FRONTEND_DIR" ]; then
  echo "❌ No se encontró $FRONTEND_DIR"
  exit 1
fi

docker run --rm \
  --net=host \
  -v "$FRONTEND_DIR":/app \
  -w /app \
  -e TEST_BASE_URL="$TEST_BASE_URL" \
  -e TEST_JWT_TOKEN="$TEST_JWT_TOKEN" \
  -e TEST_FRONTEND_URL="$TEST_FRONTEND_URL" \
  node:18-alpine npx vitest run

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

# ─── 3. TESTS E2E DEL FRONTEND (Playwright en Docker) ─────────────────────────
echo "▶ [3/3] Ejecutando tests E2E (Playwright)..."
echo ""

docker run --rm \
  --net=host \
  -v "$FRONTEND_DIR":/app \
  -w /app \
  -e TEST_BASE_URL="$TEST_BASE_URL" \
  -e TEST_JWT_TOKEN="$TEST_JWT_TOKEN" \
  -e TEST_FRONTEND_URL="$TEST_FRONTEND_URL" \
  mcr.microsoft.com/playwright:v1.40.0-jammy npx playwright test

PLAYWRIGHT_EXIT=$?

if [ $PLAYWRIGHT_EXIT -ne 0 ]; then
  echo ""
  echo "❌ Tests E2E FALLARON"
  FAILED=1
else
  echo ""
  echo "✅ Tests E2E OK"
fi

echo ""

# ─── RESUMEN FINAL ────────────────────────────────────────────────────────────
echo "╔══════════════════════════════════════════════════════╗"
echo "║                   RESUMEN FINAL                      ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

[ $BACKEND_EXIT -eq 0 ]     && echo "  ✅ Backend     — OK" || echo "  ❌ Backend     — FALLÓ"
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