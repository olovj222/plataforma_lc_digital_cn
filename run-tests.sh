#!/bin/bash

# ─────────────────────────────────────────────────────────────────────────────
# run-tests.sh — Ejecuta todos los tests de la plataforma LC
# Uso: bash run-tests.sh
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

npx playwright test --headed
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
