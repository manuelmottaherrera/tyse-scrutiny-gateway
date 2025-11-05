#!/bin/bash

################################################################################
# CI Local - Gateway CI Pipeline
#
# Este script replica localmente el workflow "Gateway - CI Pipeline" (ci.yml)
# Ejecuta los mismos comandos que se ejecutan en GitHub Actions.
#
# Uso:
#   ./scripts/ci-local.sh [--with-e2e]
#
# Opciones:
#   --with-e2e    Incluye los tests E2E (añade ~15 minutos)
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parse arguments
RUN_E2E=false
if [[ "$1" == "--with-e2e" ]]; then
    RUN_E2E=true
fi

# Track timing
SCRIPT_START=$(date +%s)

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Gateway - CI Pipeline (Local)${NC}"
echo -e "${BLUE}================================${NC}"
echo ""

################################################################################
# Job 1: Backend Tests
################################################################################

echo -e "${YELLOW}[1/3] Starting Backend Tests...${NC}"
BACKEND_START=$(date +%s)

echo "  → Running Maven verify with error-level logging..."
./mvnw clean verify \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.tech.jhipster=ERROR \
  -Dlogging.level.com.tyse.scrutiny=ERROR

BACKEND_END=$(date +%s)
BACKEND_TIME=$((BACKEND_END - BACKEND_START))

echo -e "${GREEN}✓ Backend Tests passed${NC} (${BACKEND_TIME}s)"
echo ""

# Check if test results exist
if [ -d "target/surefire-reports/" ]; then
    echo "  → Test results: target/surefire-reports/"
fi
if [ -d "target/site/jacoco/" ]; then
    echo "  → Coverage report: target/site/jacoco/"
fi
echo ""

################################################################################
# Job 2: Frontend Tests
################################################################################

echo -e "${YELLOW}[2/3] Starting Frontend Tests...${NC}"
FRONTEND_START=$(date +%s)

echo "  → Installing dependencies..."
npm ci --silent

echo "  → Checking code formatting..."
npm run prettier:check

echo "  → Running ESLint..."
npm run lint

echo "  → Running Jest tests with coverage..."
npm run test-ci

FRONTEND_END=$(date +%s)
FRONTEND_TIME=$((FRONTEND_END - FRONTEND_START))

echo -e "${GREEN}✓ Frontend Tests passed${NC} (${FRONTEND_TIME}s)"
echo ""

# Check if test results exist
if [ -d "target/test-results/" ]; then
    echo "  → Test results: target/test-results/"
fi
if [ -d "target/test-results/jest/coverage/" ]; then
    echo "  → Coverage report: target/test-results/jest/coverage/"
fi
echo ""

################################################################################
# Job 3: E2E Tests (Optional)
################################################################################

E2E_TIME=0
if [ "$RUN_E2E" = true ]; then
    echo -e "${YELLOW}[3/3] Starting E2E Tests...${NC}"
    E2E_START=$(date +%s)

    echo "  → Building E2E package..."
    npm run ci:e2e:package

    echo "  → Preparing E2E environment (Docker)..."
    npm run ci:e2e:prepare:docker

    echo "  → Running Cypress E2E tests..."
    npm run ci:e2e:run || E2E_FAILED=true

    echo "  → Tearing down E2E environment..."
    npm run ci:e2e:teardown:docker || true  # Don't fail on teardown

    E2E_END=$(date +%s)
    E2E_TIME=$((E2E_END - E2E_START))

    if [ "$E2E_FAILED" = true ]; then
        echo -e "${RED}✗ E2E Tests failed${NC} (${E2E_TIME}s)"
        echo ""
        exit 1
    else
        echo -e "${GREEN}✓ E2E Tests passed${NC} (${E2E_TIME}s)"
        echo ""
    fi

    # Check if test results exist
    if [ -d "target/cypress/" ]; then
        echo "  → E2E results: target/cypress/"
    fi
    echo ""
else
    echo -e "${BLUE}[3/3] E2E Tests skipped${NC} (use --with-e2e to run)"
    echo ""
fi

################################################################################
# Quality Gate
################################################################################

echo -e "${YELLOW}Quality Gate Check...${NC}"
echo "  Backend Tests: ${GREEN}success${NC}"
echo "  Frontend Tests: ${GREEN}success${NC}"

if [ "$RUN_E2E" = true ]; then
    if [ "$E2E_FAILED" = true ]; then
        echo "  E2E Tests: ${RED}failed${NC}"
    else
        echo "  E2E Tests: ${GREEN}success${NC}"
    fi
fi

echo ""

################################################################################
# Summary
################################################################################

SCRIPT_END=$(date +%s)
TOTAL_TIME=$((SCRIPT_END - SCRIPT_START))

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}✅ CI Pipeline Passed${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "Timing Summary:"
echo "  Backend Tests:   ${BACKEND_TIME}s"
echo "  Frontend Tests:  ${FRONTEND_TIME}s"
if [ "$RUN_E2E" = true ]; then
    echo "  E2E Tests:       ${E2E_TIME}s"
fi
echo "  ─────────────────────"
echo "  Total:           ${TOTAL_TIME}s"
echo ""
echo "All checks passed! ✓"
echo "The code is ready to be pushed to GitHub."
echo ""
