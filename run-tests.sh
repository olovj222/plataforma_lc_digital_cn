#!/bin/bash

# ─────────────────────────────────────────────────────────────────────────────
# run-tests.sh — Suite de tests ejecutada en Docker cargando .env automáticamente
# ─────────────────────────────────────────────────────────────────────────────

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$REPO_ROOT/.env"
BACKEND_ROOT_DIR="$REPO_ROOT/plataforma_lc"
FRONTEND_DIR="$REPO_ROOT/frontend/plataforma_lc_frontend"

FAILED=0

# 1. Cargar .env en la sesión del script si existe
if [ -f "$ENV_FILE" ]; then
  set -o allexport
  source "$ENV_FILE"
  set +o allexport
fi

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║    PLATAFORMA LC — SUITE DE TESTS (DOCKER IN HOST)   ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

if [ -n "${TEST_BASE_URL:-}" ]; then
  echo "  🌐 Modo AWS   → TEST_BASE_URL = $TEST_BASE_URL"
else
  echo "  🏠 Modo local → Los E2E de backend se saltarán"
fi
echo ""

ENV_DOCKER_FLAG=""
if [ -f "$ENV_FILE" ]; then
  ENV_DOCKER_FLAG="--env-file $ENV_FILE"
fi

# ─── 1. TESTS DE BACKEND (Maven con POM Padre) ───────────────────────────────
echo "▶ [1/3] Ejecutando tests de backend (Maven en Docker)..."
echo ""

if [ ! -d "$BACKEND_ROOT_DIR" ]; then
  echo "❌ No se encontró $BACKEND_ROOT_DIR"
  exit 1
fi

docker run --rm \
  --net=host \
  $ENV_DOCKER_FLAG \
  -v "$BACKEND_ROOT_DIR":/app \
  -w /app/sistema_gestion \
  -v "$HOME/.m2":/root/.m2 \
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

# ─── 2. TESTS UNITARIOS E INTEGRACIÓN DEL FRONTEND (Vitest) ──────────────────
echo "▶ [2/3] Ejecutando tests unitarios e integración del frontend (Vitest)..."
echo ""

if [ ! -d "$FRONTEND_DIR" ]; then
  echo "❌ No se encontró $FRONTEND_DIR"
  exit 1
fi

docker run --rm \
  --net=host \
  $ENV_DOCKER_FLAG \
  -v "$FRONTEND_DIR":/app \
  -w /app \
  -v "$HOME/.npm":/root/.npm \
  node:18-alpine sh -c "npm install && npx vitest run"

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

docker run --rm \
  --net=host \
  $ENV_DOCKER_FLAG \
  -v "$FRONTEND_DIR":/app \
  -w /app \
  -v "$HOME/.npm":/root/.npm \
  mcr.microsoft.com/playwright:v1.40.0-jammy sh -c "npm install && npx playwright test"

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
