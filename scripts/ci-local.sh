#!/bin/bash

################################################################################
# CI Local - Gateway CI Pipeline
#
# Este script replica localmente el workflow "Gateway - CI Pipeline" (ci.yml)
# Ejecuta los mismos comandos que se ejecutan en GitHub Actions.
#
# IMPORTANTE: Este script limpia el ambiente CI antes de ejecutar los tests
# para garantizar condiciones iniciales consistentes. Esto incluye:
#   - Eliminar directorio target/
#   - Limpiar cachés de node_modules y webpack
#   - Detener contenedores Docker de ejecuciones anteriores
#   - Matar procesos Java/Node remanentes en puertos 8080/9000
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
# Pre-flight: Clean Environment
################################################################################

echo -e "${YELLOW}[Pre-flight] Cleaning CI environment...${NC}"
CLEAN_START=$(date +%s)

# 1. Detener y limpiar contenedores Docker de ejecuciones anteriores
echo "  → Stopping and removing Docker containers from previous runs..."
docker compose -f src/main/docker/postgresql.yml down -v 2>/dev/null || true
docker compose -f src/main/docker/services-e2e.yml down -v 2>/dev/null || true

# 2. Limpiar directorio target/ (artefactos de Maven)
if [ -d "target/" ]; then
    echo "  → Removing target/ directory..."
    rm -rf target/
fi

# 3. Limpiar cachés de node_modules (webpack, babel, etc.)
if [ -d "node_modules/.cache" ]; then
    echo "  → Removing node_modules/.cache/..."
    rm -rf node_modules/.cache/
fi

# 4. Limpiar cachés de Cypress
if [ -d "$HOME/.cache/Cypress/cy" ]; then
    echo "  → Removing Cypress runtime cache..."
    rm -rf "$HOME/.cache/Cypress/cy" || true
fi

# 5. Limpiar archivos temporales de build
echo "  → Removing temporary build files..."
rm -rf .tsbuildinfo 2>/dev/null || true
rm -rf build/ 2>/dev/null || true

# 6. Matar procesos Java remanentes (del puerto 8080)
JAVA_PID=$(lsof -ti:8080 2>/dev/null || true)
if [ -n "$JAVA_PID" ]; then
    echo "  → Killing Java process on port 8080 (PID: $JAVA_PID)..."
    kill -9 $JAVA_PID 2>/dev/null || true
    sleep 2
fi

# 7. Matar procesos Node remanentes (del puerto 9000 - webpack dev server)
NODE_PID=$(lsof -ti:9000 2>/dev/null || true)
if [ -n "$NODE_PID" ]; then
    echo "  → Killing Node process on port 9000 (PID: $NODE_PID)..."
    kill -9 $NODE_PID 2>/dev/null || true
    sleep 2
fi

CLEAN_END=$(date +%s)
CLEAN_TIME=$((CLEAN_END - CLEAN_START))

echo -e "${GREEN}✓ Environment cleaned${NC} (${CLEAN_TIME}s)"
echo ""

################################################################################
# Start Docker Services (PostgreSQL only)
# Note: Consul, Kafka, MailHog are in tyse-infrastructure/ for development.
# For CI tests, only PostgreSQL is required (Spring uses Testcontainers, mail is mocked).
################################################################################

echo -e "${YELLOW}[Services] Starting Docker services...${NC}"
SERVICES_START=$(date +%s)

mkdir -p logs
echo "  → Starting PostgreSQL (port 5432)..."
docker compose -f src/main/docker/postgresql.yml up -d > logs/services-start.log 2>&1

# Esperar a que PostgreSQL esté healthy
echo -n "    PostgreSQL: "
RETRIES=30
until docker compose -f src/main/docker/postgresql.yml ps postgresql 2>/dev/null | grep -q "healthy" || [ $RETRIES -eq 0 ]; do
    echo -n "."
    sleep 2
    RETRIES=$((RETRIES - 1))
done
if [ $RETRIES -gt 0 ]; then
    echo -e " ${GREEN}healthy${NC}"
else
    echo -e " ${RED}timeout${NC}"
    echo "    Check logs/services-start.log for details"
    exit 1
fi

SERVICES_END=$(date +%s)
SERVICES_TIME=$((SERVICES_END - SERVICES_START))

echo -e "${GREEN}✓ Docker services ready${NC} (${SERVICES_TIME}s)"
echo ""

################################################################################
# Liquibase Verification (Update → Rollback → Update)
################################################################################

echo -e "${YELLOW}[Liquibase] Verifying migrations and rollback capability...${NC}"
ROLLBACK_START=$(date +%s)

mkdir -p logs

# Compilar proyecto (necesario para que Liquibase encuentre los recursos)
echo "  → Compiling project for Liquibase..."
if ! ./mvnw compile -DskipTests -q > logs/liquibase-compile.log 2>&1; then
    echo -e "    ${RED}✗ Error: Compilation failed${NC}"
    echo "    Check logs/liquibase-compile.log for details"
    tail -20 logs/liquibase-compile.log
    exit 1
fi

# Paso 1: Aplicar todos los changesets
echo "  → Step 1: Applying all changesets with liquibase:update..."
if ./mvnw liquibase:update \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.liquibase=INFO \
  > logs/liquibase-update-1.log 2>&1; then

    # Contar changesets aplicados
    APPLIED=$(grep -c "ChangeSet.*ran successfully" logs/liquibase-update-1.log 2>/dev/null || echo "0")
    echo -e "    ${GREEN}✓ Applied $APPLIED changeset(s) successfully${NC}"
else
    echo -e "    ${RED}✗ Error: Failed to apply changesets${NC}"
    echo "    Check logs/liquibase-update-1.log for details"
    tail -20 logs/liquibase-update-1.log
    exit 1
fi

# Paso 2: Rollback hasta el tag inicial
echo "  → Step 2: Testing rollback to tag 'estado-vacio'..."
if ./mvnw liquibase:rollback -Dliquibase.rollbackTag=estado-vacio \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.liquibase=INFO \
  > logs/liquibase-rollback.log 2>&1; then

    # Contar cuántos changesets se hicieron rollback
    ROLLED_BACK=$(grep -c "Rolling Back Changeset" logs/liquibase-rollback.log 2>/dev/null || echo "0")
    echo -e "    ${GREEN}✓ Rolled back $ROLLED_BACK changeset(s) successfully${NC}"
else
    echo -e "    ${RED}✗ Error: Rollback failed${NC}"
    echo "    Check logs/liquibase-rollback.log for details"
    tail -20 logs/liquibase-rollback.log

    # Mostrar análisis de changesets sin rollback
    echo ""
    echo "  → Analyzing changeset files for missing rollback tags..."
    TOTAL_CHANGESETS=0
    CHANGESETS_WITH_ROLLBACK=0

    LIQUIBASE_DIR="src/main/resources/config/liquibase"
    if [ -d "$LIQUIBASE_DIR" ]; then
        for xml_file in $(find "$LIQUIBASE_DIR" -name "*.xml" -type f 2>/dev/null); do
            CHANGESETS_IN_FILE=$(grep -c "<changeSet" "$xml_file" 2>/dev/null || echo "0")
            TOTAL_CHANGESETS=$((TOTAL_CHANGESETS + CHANGESETS_IN_FILE))
            ROLLBACKS_IN_FILE=$(grep -c "<rollback" "$xml_file" 2>/dev/null || echo "0")
            CHANGESETS_WITH_ROLLBACK=$((CHANGESETS_WITH_ROLLBACK + ROLLBACKS_IN_FILE))
        done

        if [ "$TOTAL_CHANGESETS" -gt 0 ]; then
            MISSING=$((TOTAL_CHANGESETS - CHANGESETS_WITH_ROLLBACK))
            echo "    Total changesets: $TOTAL_CHANGESETS"
            echo "    With <rollback> tag: $CHANGESETS_WITH_ROLLBACK"
            echo -e "    ${YELLOW}Missing rollback: $MISSING changeset(s)${NC}"
        fi
    fi
    exit 1
fi

# Paso 3: Re-aplicar todos los changesets (dejar BD lista para tests)
echo "  → Step 3: Re-applying all changesets..."
if ./mvnw liquibase:update \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.liquibase=INFO \
  > logs/liquibase-update-2.log 2>&1; then

    REAPPLIED=$(grep -c "ChangeSet.*ran successfully" logs/liquibase-update-2.log 2>/dev/null || echo "0")
    echo -e "    ${GREEN}✓ Re-applied $REAPPLIED changeset(s) - Database ready${NC}"
else
    echo -e "    ${RED}✗ Error: Failed to re-apply changesets${NC}"
    echo "    Check logs/liquibase-update-2.log for details"
    tail -20 logs/liquibase-update-2.log
    exit 1
fi

ROLLBACK_END=$(date +%s)
ROLLBACK_TIME=$((ROLLBACK_END - ROLLBACK_START))
echo -e "${GREEN}✓ Liquibase verification passed${NC} (${ROLLBACK_TIME}s)"
echo ""

################################################################################
# Job 1: Backend Tests
################################################################################

echo -e "${YELLOW}[Job 1/3] Starting Backend Tests...${NC}"
BACKEND_START=$(date +%s)

echo "  → Running Maven verify with error-level logging..."
./mvnw verify \
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

echo -e "${YELLOW}[Job 2/3] Starting Frontend Tests...${NC}"
FRONTEND_START=$(date +%s)

echo "  → Installing dependencies..."
echo "     (Logging to: logs/npm-ci.log)"
mkdir -p logs
npm ci > logs/npm-ci.log 2>&1 &
NPM_PID=$!

# Monitor npm ci progress
while kill -0 $NPM_PID 2>/dev/null; do
    sleep 5
    if [ -f logs/npm-ci.log ]; then
        tail -n 3 logs/npm-ci.log | sed 's/^/     /'
    fi
done

# Wait for npm ci to complete and check exit code
wait $NPM_PID
NPM_EXIT_CODE=$?
if [ $NPM_EXIT_CODE -ne 0 ]; then
    echo -e "${RED}✗ npm ci failed${NC}"
    echo "Last 20 lines of logs/npm-ci.log:"
    tail -n 20 logs/npm-ci.log
    exit 1
fi

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
    echo -e "${YELLOW}[Job 3/3] Starting E2E Tests...${NC}"
    E2E_START=$(date +%s)

    # Build E2E JAR for gateway
    echo "  → Building E2E package for gateway..."
    npm run ci:e2e:package

    # Build microservice divipol Docker image
    echo "  → Building microservice divipol Docker image..."
    MICRO_DIVIPOL_PATH="../tyse-scrutiny-micro-divipol"
    if [ -d "$MICRO_DIVIPOL_PATH" ]; then
        (cd "$MICRO_DIVIPOL_PATH" && ./mvnw -ntp verify -DskipTests -Pprod jib:dockerBuild) > logs/micro-divipol-build.log 2>&1
        if [ $? -eq 0 ]; then
            echo -e "    ${GREEN}✓ Microservice divipol image built${NC}"
        else
            echo -e "    ${RED}✗ Failed to build microservice divipol image${NC}"
            echo "    Check logs/micro-divipol-build.log for details"
            tail -20 logs/micro-divipol-build.log
            exit 1
        fi
    else
        echo -e "    ${RED}✗ Microservice divipol not found at $MICRO_DIVIPOL_PATH${NC}"
        echo "    E2E tests require the microservice. Please clone it first."
        exit 1
    fi

    # Stop any existing services and start E2E services
    echo "  → Preparing E2E environment (Docker with microservice)..."
    docker compose -f src/main/docker/postgresql.yml down -v 2>/dev/null || true

    echo "    Starting E2E services..."
    if ! docker compose -f src/main/docker/services-e2e.yml up -d > logs/e2e-services-start.log 2>&1; then
        echo -e "    ${RED}✗ Failed to start E2E services${NC}"
        echo "    Check logs/e2e-services-start.log for details"
        cat logs/e2e-services-start.log | tail -30
        exit 1
    fi

    # Show running containers
    echo "    Containers started:"
    docker compose -f src/main/docker/services-e2e.yml ps --format "table {{.Name}}\t{{.Status}}" | sed 's/^/      /'

    # Wait for PostgreSQL divipol to be healthy first
    echo -n "    Waiting for postgresql-divipol: "
    RETRIES=30
    until docker compose -f src/main/docker/services-e2e.yml ps postgresql-divipol 2>/dev/null | grep -q "healthy" || [ $RETRIES -eq 0 ]; do
        echo -n "."
        sleep 2
        RETRIES=$((RETRIES - 1))
    done
    if [ $RETRIES -gt 0 ]; then
        echo -e " ${GREEN}healthy${NC}"
    else
        echo -e " ${RED}timeout${NC}"
        docker compose -f src/main/docker/services-e2e.yml logs postgresql-divipol | tail -20
        exit 1
    fi

    # Wait for microservice divipol to be healthy
    echo -n "    Waiting for micro-divipol: "
    RETRIES=60
    until docker compose -f src/main/docker/services-e2e.yml ps micro-divipol 2>/dev/null | grep -q "healthy" || [ $RETRIES -eq 0 ]; do
        echo -n "."
        sleep 5
        RETRIES=$((RETRIES - 1))
    done
    if [ $RETRIES -gt 0 ]; then
        echo -e " ${GREEN}healthy${NC}"
    else
        echo -e " ${RED}timeout${NC}"
        echo "    Microservice logs:"
        docker compose -f src/main/docker/services-e2e.yml logs micro-divipol | tail -50
        exit 1
    fi

    # Verify microservice is responding
    echo -n "    Verifying micro-divipol API: "
    if curl -sf http://localhost:8081/management/health > /dev/null 2>&1; then
        echo -e "${GREEN}OK${NC}"
    else
        echo -e "${YELLOW}warning (health endpoint not responding)${NC}"
    fi

    # Run E2E tests
    echo "  → Running Cypress E2E tests..."
    npm run ci:e2e:run || E2E_FAILED=true

    # Teardown E2E environment
    echo "  → Tearing down E2E environment..."
    docker compose -f src/main/docker/services-e2e.yml down -v 2>/dev/null || true

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
    echo -e "${BLUE}[Job 3/3] E2E Tests skipped${NC} (use --with-e2e to run)"
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
echo "  Environment Cleanup:     ${CLEAN_TIME}s"
echo "  Docker Services:         ${SERVICES_TIME}s"
echo "  Liquibase Verification:  ${ROLLBACK_TIME}s"
echo "  Backend Tests:           ${BACKEND_TIME}s"
echo "  Frontend Tests:          ${FRONTEND_TIME}s"
if [ "$RUN_E2E" = true ]; then
    echo "  E2E Tests:               ${E2E_TIME}s"
fi
echo "  ─────────────────────────────────"
echo "  Total:                   ${TOTAL_TIME}s"
echo ""
echo "All checks passed! ✓"
echo "The code is ready to be pushed to GitHub."
echo ""

################################################################################
# Post-flight: Cleanup Background Processes
################################################################################

# Stop all Docker services (PostgreSQL)
echo -e "${YELLOW}[Post-flight] Cleaning up Docker services...${NC}"
docker compose -f src/main/docker/postgresql.yml down -v 2>/dev/null || true

# Wait for any background jobs to finish
wait

# Ensure no orphaned Java processes are running
JAVA_PID=$(lsof -ti:8080 2>/dev/null || true)
if [ -n "$JAVA_PID" ]; then
    kill -9 $JAVA_PID 2>/dev/null || true
fi

# Explicit exit to ensure proper return to calling process (git push hook)
exit 0
