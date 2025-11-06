#!/bin/bash

################################################################################
# Setup Branch Protection Rules
#
# Este script configura las reglas de protección de ramas para main y develop
# usando GitHub CLI (gh).
#
# Prerequisitos:
#   - GitHub CLI instalado (https://cli.github.com/)
#   - Autenticado con 'gh auth login'
#   - Permisos de admin en el repositorio
#
# Uso:
#   ./scripts/setup-branch-protection.sh
################################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Branch Protection Setup${NC}"
echo -e "${BLUE}================================${NC}"
echo ""

# Check if gh is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI (gh) no está instalado${NC}"
    echo ""
    echo "Instálalo desde: https://cli.github.com/"
    echo ""
    echo "O en Ubuntu/Debian:"
    echo "  sudo apt install gh"
    echo ""
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ No estás autenticado con GitHub CLI${NC}"
    echo ""
    echo "Ejecuta: gh auth login"
    echo ""
    exit 1
fi

# Get repository info
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)

echo -e "${BLUE}Repositorio:${NC} $REPO"
echo ""

# Confirm
echo -e "${YELLOW}⚠️  Esto configurará reglas de protección para las ramas main y develop${NC}"
echo ""
echo "Configuración para main:"
echo "  - Require PR with 1 approval"
echo "  - Require status checks: backend-tests, frontend-tests, e2e-tests, quality-gate"
echo "  - Require conversation resolution"
echo "  - Enforce for admins"
echo ""
echo "Configuración para develop:"
echo "  - Require PR with 1 approval"
echo "  - Require status checks: backend-tests, frontend-tests, quality-gate"
echo "  - Require conversation resolution"
echo ""

read -p "¿Continuar? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cancelado."
    exit 0
fi

echo ""
echo -e "${YELLOW}Configurando protección para rama 'main'...${NC}"

# Configure main branch protection
if gh api -X PUT "repos/$REPO/branches/main/protection" \
  --input - <<EOF
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["backend-tests", "frontend-tests", "e2e-tests", "quality-gate"]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "required_approving_review_count": 1
  },
  "required_conversation_resolution": true,
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
EOF
then
    echo -e "${GREEN}✓ Protección configurada para 'main'${NC}"
else
    echo -e "${RED}✗ Error configurando protección para 'main'${NC}"
    echo ""
    echo "Posibles causas:"
    echo "  - No tienes permisos de admin"
    echo "  - La rama 'main' no existe"
    echo "  - Los status checks no existen (deben ejecutarse al menos una vez)"
    echo ""
    exit 1
fi

echo ""
echo -e "${YELLOW}Configurando protección para rama 'develop'...${NC}"

# Configure develop branch protection
if gh api -X PUT "repos/$REPO/branches/develop/protection" \
  --input - <<EOF
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["backend-tests", "frontend-tests", "quality-gate"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "required_approving_review_count": 1
  },
  "required_conversation_resolution": true,
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
EOF
then
    echo -e "${GREEN}✓ Protección configurada para 'develop'${NC}"
else
    echo -e "${RED}✗ Error configurando protección para 'develop'${NC}"
    echo ""
    echo "Posibles causas:"
    echo "  - No tienes permisos de admin"
    echo "  - La rama 'develop' no existe"
    echo "  - Los status checks no existen (deben ejecutarse al menos una vez)"
    echo ""
    exit 1
fi

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}✅ Branch Protection Configurada${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

echo "Verificar configuración:"
echo "  - GitHub → Settings → Branches"
echo ""
echo "O usando CLI:"
echo "  gh api repos/$REPO/branches/main/protection | jq"
echo "  gh api repos/$REPO/branches/develop/protection | jq"
echo ""

echo -e "${BLUE}Próximos pasos:${NC}"
echo "  1. Crear un PR de prueba hacia develop"
echo "  2. Verificar que los status checks se ejecuten"
echo "  3. Verificar que no puedes hacer merge hasta que pasen"
echo ""
