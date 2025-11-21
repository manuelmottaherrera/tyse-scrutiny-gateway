#!/bin/bash

# ===================================================================
# Script de validación de configuración para PostgreSQL dockerizado
# ===================================================================

set -e

echo "======================================"
echo "Validación de Configuración Staging"
echo "======================================"

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Contadores
ERRORS=0
WARNINGS=0

# Función para verificar archivo
check_file() {
    local file=$1
    local desc=$2

    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $desc existe"
        return 0
    else
        echo -e "${RED}✗${NC} $desc no encontrado: $file"
        ((ERRORS++))
        return 1
    fi
}

# Función para verificar servicio en docker-compose
check_service() {
    local service=$1
    local file=$2

    if grep -q "^\s*$service:" "$file" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} Servicio '$service' definido en docker-compose"
        return 0
    else
        echo -e "${RED}✗${NC} Servicio '$service' NO encontrado en docker-compose"
        ((ERRORS++))
        return 1
    fi
}

# Función para verificar variable en .env
check_env_var() {
    local var=$1
    local file=$2
    local required=$3

    if [ ! -f "$file" ]; then
        if [ "$required" = "true" ]; then
            echo -e "${RED}✗${NC} Archivo $file no existe"
            ((ERRORS++))
        fi
        return 1
    fi

    if grep -q "^$var=" "$file" 2>/dev/null; then
        local value=$(grep "^$var=" "$file" | cut -d'=' -f2)
        if [ "$value" = "CHANGE_ME" ] || [ "$value" = "CHANGE_ME_STRONG_PASSWORD" ]; then
            echo -e "${YELLOW}⚠${NC}  Variable '$var' tiene valor por defecto - debe cambiarse"
            ((WARNINGS++))
        else
            echo -e "${GREEN}✓${NC} Variable '$var' configurada"
        fi
        return 0
    else
        if [ "$required" = "true" ]; then
            echo -e "${RED}✗${NC} Variable '$var' NO encontrada en $file"
            ((ERRORS++))
        else
            echo -e "${YELLOW}⚠${NC}  Variable '$var' no definida (opcional)"
        fi
        return 1
    fi
}

echo ""
echo "1. Verificando archivos necesarios"
echo "-----------------------------------"
check_file "docker-compose.staging.yml" "Docker Compose de staging"
check_file "scripts/deploy.sh" "Script de deployment principal"
check_file ".env.staging.example" "Template de variables de entorno"
check_file "README-DOCKER-DB.md" "Documentación de BD dockerizada"

echo ""
echo "2. Verificando servicios PostgreSQL en docker-compose"
echo "------------------------------------------------------"
check_service "postgres-gateway" "docker-compose.staging.yml"
check_service "postgres-divipol" "docker-compose.staging.yml"
check_service "consul" "docker-compose.staging.yml"
check_service "kafka" "docker-compose.staging.yml"
check_service "gateway" "docker-compose.staging.yml"
check_service "divipol" "docker-compose.staging.yml"

echo ""
echo "3. Verificando volúmenes Docker definidos"
echo "------------------------------------------"
if grep -q "gateway_db_data:" "docker-compose.staging.yml" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} Volumen 'gateway_db_data' definido"
else
    echo -e "${RED}✗${NC} Volumen 'gateway_db_data' NO definido"
    ((ERRORS++))
fi

if grep -q "divipol_db_data:" "docker-compose.staging.yml" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} Volumen 'divipol_db_data' definido"
else
    echo -e "${RED}✗${NC} Volumen 'divipol_db_data' NO definido"
    ((ERRORS++))
fi

echo ""
echo "4. Verificando configuración de variables (.env.staging)"
echo "---------------------------------------------------------"
ENV_FILE=".env.staging"
EXAMPLE_FILE=".env.staging.example"

if [ -f "$ENV_FILE" ]; then
    echo -e "${GREEN}✓${NC} Archivo .env.staging existe"
    echo ""
    echo "   Variables críticas:"
    check_env_var "GATEWAY_DB_PASSWORD" "$ENV_FILE" "true"
    check_env_var "DIVIPOL_DB_PASSWORD" "$ENV_FILE" "true"
    check_env_var "JWT_SECRET" "$ENV_FILE" "true"
    check_env_var "GATEWAY_DB_NAME" "$ENV_FILE" "true"
    check_env_var "DIVIPOL_DB_NAME" "$ENV_FILE" "true"

    echo ""
    echo "   Variables de servicio:"
    check_env_var "GATEWAY_PORT" "$ENV_FILE" "false"
    check_env_var "DIVIPOL_PORT" "$ENV_FILE" "false"
    check_env_var "CONSUL_PORT" "$ENV_FILE" "false"
    check_env_var "KAFKA_PORT" "$ENV_FILE" "false"
else
    echo -e "${YELLOW}⚠${NC}  Archivo .env.staging no existe"
    echo "    Copia .env.staging.example a .env.staging y configura las variables"
    ((WARNINGS++))
fi

echo ""
echo "5. Verificando referencias a BD externa obsoletas"
echo "--------------------------------------------------"
if grep -q "DB_HOST.*localhost\|DB_HOST.*192\.168" "docker-compose.staging.yml" 2>/dev/null; then
    echo -e "${RED}✗${NC} Encontradas referencias a DB_HOST externo en docker-compose"
    echo "    Las BD ahora deben usar nombres de contenedor (postgres-gateway, postgres-divipol)"
    ((ERRORS++))
else
    echo -e "${GREEN}✓${NC} No hay referencias a DB_HOST externo"
fi

# Verificar que las apps usan los nombres correctos de contenedores
if grep -q "postgres-gateway:5432" "docker-compose.staging.yml" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} Gateway configurado para usar postgres-gateway"
else
    echo -e "${YELLOW}⚠${NC}  Verificar configuración de conexión del gateway"
    ((WARNINGS++))
fi

if grep -q "postgres-divipol:5432" "docker-compose.staging.yml" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} Divipol configurado para usar postgres-divipol"
else
    echo -e "${YELLOW}⚠${NC}  Verificar configuración de conexión de divipol"
    ((WARNINGS++))
fi

echo ""
echo "6. Verificando permisos de scripts"
echo "-----------------------------------"
for script in scripts/deploy.sh deploy-staging.sh validate-config.sh; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            echo -e "${GREEN}✓${NC} $script es ejecutable"
        else
            echo -e "${YELLOW}⚠${NC}  $script no es ejecutable (ejecuta: chmod +x $script)"
            ((WARNINGS++))
        fi
    fi
done

echo ""
echo "======================================"
echo "Resumen de Validación"
echo "======================================"

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ Configuración perfecta - Lista para deployment${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Configuración válida con $WARNINGS advertencias${NC}"
    echo "    Revisa las advertencias antes del deployment"
    # Exit with 0 for warnings (non-blocking)
    exit 0
else
    echo -e "${RED}❌ Configuración inválida - $ERRORS errores, $WARNINGS advertencias${NC}"
    echo "    Corrige los errores antes de continuar"
    # Exit with 1 only for actual errors
    exit 1
fi