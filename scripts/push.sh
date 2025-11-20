#!/bin/bash

################################################################################
# Push Script - CI Completo + Git Push
#
# Este script ejecuta el CI completo (incluyendo E2E) y hace push solo si
# todos los tests pasan. No tiene timeout de SSH porque el CI se ejecuta
# ANTES de abrir la conexión SSH con git push.
#
# Uso:
#   ./scripts/push.sh                    # Push a origin/current-branch
#   ./scripts/push.sh origin develop     # Push a origin/develop
#   ./scripts/push.sh --skip-ci          # Push sin ejecutar CI
#
# Opciones:
#   --skip-ci    Saltar CI y hacer push directamente
#
# Logs:
#   La salida completa se guarda en logs/push-YYYYMMDD-HHMMSS.log
################################################################################

set -e  # Exit on error

# Setup logging
mkdir -p logs
LOG_FILE="logs/push-$(date +%Y%m%d-%H%M%S).log"

# Redirect all output to both console and log file
exec > >(tee -a "$LOG_FILE")
exec 2>&1

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parse arguments
SKIP_CI=false
REMOTE="origin"
BRANCH=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-ci)
            SKIP_CI=true
            shift
            ;;
        *)
            if [ -z "$REMOTE" ] || [ "$REMOTE" = "origin" ]; then
                REMOTE="$1"
            elif [ -z "$BRANCH" ]; then
                BRANCH="$1"
            fi
            shift
            ;;
    esac
done

# Get current branch if not specified
if [ -z "$BRANCH" ]; then
    BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
    if [ -z "$BRANCH" ]; then
        echo -e "${RED}❌ Error: No se pudo detectar la rama actual${NC}"
        exit 1
    fi
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║       Push Script - CI Completo + Git Push        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Remote:${NC} $REMOTE"
echo -e "${YELLOW}Branch:${NC} $BRANCH"
echo ""

################################################################################
# Step 1: Ejecutar CI Completo (si no se skip)
################################################################################

if [ "$SKIP_CI" = true ]; then
    echo -e "${YELLOW}⚠️  Skipping CI validation (--skip-ci flag)${NC}"
    echo ""
else
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}Step 1: Running CI Pipeline (with E2E tests)${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "${YELLOW}⏱️  This may take 6-8 minutes...${NC}"
    echo ""

    # Run CI script with E2E tests
    if ./scripts/ci-local.sh --with-e2e; then
        echo ""
        echo -e "${GREEN}✅ CI Pipeline passed successfully!${NC}"
        echo ""
    else
        echo ""
        echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${RED}❌ CI Pipeline Failed!${NC}"
        echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo ""
        echo -e "${YELLOW}Your code did not pass the CI checks.${NC}"
        echo -e "${YELLOW}Please fix the errors before pushing.${NC}"
        echo ""
        echo -e "Options:"
        echo -e "  1. Fix the failing tests"
        echo -e "  2. Run ${BLUE}./scripts/ci-local.sh --with-e2e${NC} to debug"
        echo -e "  3. Run ${BLUE}./scripts/push.sh --skip-ci${NC} to push without CI (not recommended)"
        echo ""
        echo "📝 Log file: $LOG_FILE"
        echo ""
        exit 1
    fi
fi

################################################################################
# Step 2: Git Push
################################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Step 2: Pushing to ${REMOTE}/${BRANCH}${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Execute git push
if git push "$REMOTE" "$BRANCH"; then
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║            ✅ Push Successful!                     ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}Successfully pushed to ${REMOTE}/${BRANCH}${NC}"
    echo ""
    echo "📝 Log file: $LOG_FILE"
    echo ""
    exit 0
else
    echo ""
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}❌ Git Push Failed!${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "${YELLOW}Git push encountered an error.${NC}"
    echo -e "${YELLOW}Check the error message above for details.${NC}"
    echo ""
    echo "📝 Log file: $LOG_FILE"
    echo ""
    exit 1
fi
