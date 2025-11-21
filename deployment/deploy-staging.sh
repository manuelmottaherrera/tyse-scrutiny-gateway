#!/bin/bash

# ===================================================================
# Script de deployment para staging con PostgreSQL dockerizado
# ===================================================================

set -e  # Salir si hay errores

echo "======================================"
echo "Deployment de Tyse Scrutiny - Staging"
echo "======================================"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.staging.yml" ]; then
    echo -e "${RED}Error: No se encontró docker-compose.staging.yml${NC}"
    echo "Por favor ejecuta este script desde el directorio deployment/"
    exit 1
fi

# Cargar variables de entorno
if [ -f ".env.staging" ]; then
    export $(grep -v '^#' .env.staging | xargs)
    echo -e "${GREEN}✓ Variables de entorno cargadas desde .env.staging${NC}"
else
    echo -e "${YELLOW}⚠ No se encontró .env.staging${NC}"
    echo "Copiando .env.staging.example a .env.staging..."
    cp .env.staging.example .env.staging
    echo -e "${RED}IMPORTANTE: Edita .env.staging con las configuraciones correctas${NC}"
    exit 1
fi

# Función para verificar salud de un servicio
check_health() {
    local service=$1
    local url=$2
    local max_attempts=30
    local attempt=1

    echo -n "Verificando $service..."

    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            echo -e " ${GREEN}✓ UP${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    echo -e " ${RED}✗ TIMEOUT${NC}"
    return 1
}

echo ""
echo "Paso 1: Deteniendo servicios existentes"
echo "----------------------------------------"
docker-compose -f docker-compose.staging.yml down

echo ""
echo "Paso 2: Limpiando volúmenes antiguos (opcional)"
echo "------------------------------------------------"
read -p "¿Deseas limpiar los volúmenes de BD existentes? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker volume rm tyse-gateway-db-staging tyse-divipol-db-staging 2>/dev/null || true
    echo -e "${YELLOW}Volúmenes eliminados. Liquibase recreará las BDs desde cero.${NC}"
else
    echo "Manteniendo volúmenes existentes."
fi

echo ""
echo "Paso 3: Creando directorios de backup"
echo "--------------------------------------"
mkdir -p backup/gateway
mkdir -p backup/divipol
echo -e "${GREEN}✓ Directorios creados${NC}"

echo ""
echo "Paso 4: Iniciando bases de datos"
echo "---------------------------------"
docker-compose -f docker-compose.staging.yml up -d postgres-gateway postgres-divipol

# Esperar a que PostgreSQL esté listo
echo "Esperando que PostgreSQL esté listo..."
sleep 5

until docker exec tyse-postgres-gateway-staging pg_isready -U ${GATEWAY_DB_USER:-tysescrutinygateway} > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}✓ Gateway DB lista${NC}"

until docker exec tyse-postgres-divipol-staging pg_isready -U ${DIVIPOL_DB_USER:-tysescrutinymicrodivipol} > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}✓ Divipol DB lista${NC}"

echo ""
echo "Paso 5: Iniciando servicios de infraestructura"
echo "-----------------------------------------------"
docker-compose -f docker-compose.staging.yml up -d zookeeper consul kafka mailhog

# Verificar servicios de infraestructura
check_health "Consul" "http://localhost:${CONSUL_PORT:-8510}/v1/status/leader"
check_health "MailHog" "http://localhost:${MAILHOG_UI_PORT:-8035}"

echo ""
echo "Paso 6: Iniciando aplicaciones"
echo "-------------------------------"
echo -e "${BLUE}Liquibase aplicará automáticamente todas las migraciones...${NC}"
docker-compose -f docker-compose.staging.yml up -d gateway divipol

echo ""
echo "Esperando que las aplicaciones inicien (puede tomar 1-2 minutos)..."
echo "Liquibase está:"
echo "  - Creando esquemas de BD"
echo "  - Aplicando changesets"
echo "  - Cargando datos iniciales (usuarios, permisos, 18K registros divipol)"
echo ""

# Mostrar logs de Liquibase en tiempo real por unos segundos
echo "Logs de inicialización:"
echo "------------------------"
timeout 20 docker-compose -f docker-compose.staging.yml logs -f gateway divipol | grep -E "liquibase|Started|FAIL|ERROR" || true

echo ""
echo "Paso 7: Verificación de salud"
echo "------------------------------"

# Verificar aplicaciones
check_health "Gateway" "http://localhost:${GATEWAY_PORT:-8090}/management/health"
check_health "Divipol" "http://localhost:${DIVIPOL_PORT:-8091}/management/health"

echo ""
echo "Paso 8: Verificación de datos"
echo "------------------------------"

# Verificar que Liquibase completó las migraciones
echo "Changesets aplicados en Gateway:"
docker exec tyse-postgres-gateway-staging psql \
    -U ${GATEWAY_DB_USER:-tysescrutinygateway} \
    -d ${GATEWAY_DB_NAME:-tysescrutinygateway} \
    -t -c "SELECT COUNT(*) FROM databasechangelog;" 2>/dev/null || echo "Esperando..."

echo "Registros en Divipol:"
docker exec tyse-postgres-divipol-staging psql \
    -U ${DIVIPOL_DB_USER:-tysescrutinymicrodivipol} \
    -d ${DIVIPOL_DB_NAME:-tysescrutinymicrodivipol} \
    -t -c "SELECT COUNT(*) FROM divipol;" 2>/dev/null || echo "Esperando..."

echo ""
echo "======================================"
echo -e "${GREEN}¡Deployment completado!${NC}"
echo "======================================"

echo ""
echo "Servicios disponibles:"
echo "----------------------"
echo -e "Gateway:    ${BLUE}http://localhost:${GATEWAY_PORT:-8090}${NC}"
echo -e "Divipol:    ${BLUE}http://localhost:${DIVIPOL_PORT:-8091}${NC}"
echo -e "Consul:     ${BLUE}http://localhost:${CONSUL_PORT:-8510}${NC}"
echo -e "MailHog:    ${BLUE}http://localhost:${MAILHOG_UI_PORT:-8035}${NC}"

echo ""
echo "Comandos útiles:"
echo "----------------"
echo "Ver logs:           docker-compose -f docker-compose.staging.yml logs -f [servicio]"
echo "Reiniciar servicio: docker-compose -f docker-compose.staging.yml restart [servicio]"
echo "Ver estado:         docker-compose -f docker-compose.staging.yml ps"
echo "Detener todo:       docker-compose -f docker-compose.staging.yml down"
echo ""
echo "Backup de BD:       docker exec tyse-postgres-gateway-staging pg_dump -U ${GATEWAY_DB_USER:-tysescrutinygateway} ${GATEWAY_DB_NAME:-tysescrutinygateway} > backup_gateway.sql"