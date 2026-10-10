#!/bin/bash
# =============================================================================
# get-test-token.sh
# Obtiene un token JWT de Azure AD usando el flujo client_credentials.
#
# Este flujo es el adecuado para pruebas automatizadas y CI/CD porque:
#  - No requiere intervención humana (sin login de usuario).
#  - El token se obtiene con un Service Principal (app de Azure AD).
#  - El token tiene una duración de ~1 hora, suficiente para cualquier suite.
#
# CÓMO CONFIGURARLO EN AZURE AD:
#  1. Ve a Azure Portal → App Registrations → tu app (70af68d7-...)
#  2. En "Certificates & Secrets" → crea un nuevo Client Secret → cópialo.
#  3. En "API permissions" → asegúrate de que la app tiene el permiso de la API
#     con "Application" type (no Delegated).
#  4. Exporta las variables de entorno de abajo antes de ejecutar este script.
#
# VARIABLES DE ENTORNO REQUERIDAS:
#   AZURE_TENANT_ID     → 2dcf78c8-4359-4115-8b06-50c5a455e4e0 (ya en el proyecto)
#   AZURE_CLIENT_ID     → 70af68d7-f0b7-4897-9d64-4a0b0791ca70 (ya en el proyecto)
#   AZURE_CLIENT_SECRET → El secreto que creaste en el paso 2.
#   AZURE_SCOPE         → api://70af68d7-f0b7-4897-9d64-4a0b0791ca70/.default
#
# USO:
#   export AZURE_CLIENT_SECRET="tu-secreto-aqui"
#   export TEST_BASE_URL="http://<nueva-ip-ec2>:8085"    ← actualizar tras cada reinicio
#   export TEST_JWT_TOKEN=$(bash scripts/get-test-token.sh)
#
#   # Luego lanzar los E2E:
#   mvn test -pl sistema_gestion/asistencia -Dtest=AsistenciaRestControllerE2ETest
#   mvn test -pl sistema_gestion/evaluaciones -Dtest=Evaluacionese2eTest
# =============================================================================

set -euo pipefail

TENANT_ID="${AZURE_TENANT_ID:-2dcf78c8-4359-4115-8b06-50c5a455e4e0}"
CLIENT_ID="${AZURE_CLIENT_ID:-70af68d7-f0b7-4897-9d64-4a0b0791ca70}"
CLIENT_SECRET="${AZURE_CLIENT_SECRET:?ERROR: Define AZURE_CLIENT_SECRET}"
SCOPE="${AZURE_SCOPE:-api://70af68d7-f0b7-4897-9d64-4a0b0791ca70/.default}"

TOKEN_URL="https://login.microsoftonline.com/${TENANT_ID}/oauth2/v2.0/token"

RESPONSE=$(curl -s -X POST "$TOKEN_URL" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "scope=${SCOPE}")

# Extraemos sólo el access_token del JSON de respuesta
TOKEN=$(echo "$RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ No se pudo obtener el token. Respuesta de Azure:" >&2
  echo "$RESPONSE" >&2
  exit 1
fi

# Imprimimos sólo el token (sin salto de línea) para que la subshell $(...) lo capture limpio
printf "%s" "$TOKEN"
