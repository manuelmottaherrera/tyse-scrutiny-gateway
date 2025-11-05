# GitHub Secrets Configuration for Staging Deployment

Este documento describe todos los secrets que deben configurarse en GitHub para el deployment automático a staging.

## Cómo agregar secrets en GitHub

1. Ve a tu repositorio en GitHub
2. Click en **Settings** → **Secrets and variables** → **Actions**
3. Click en **New repository secret**
4. Agrega cada uno de los secrets listados abajo con sus valores reales

## Secrets Requeridos

### Database Configuration

```bash
STAGING_DB_HOST=<ip_servidor_postgresql>
STAGING_DB_PORT=5432
STAGING_DB_PORT_DIVIPOL=5432
```

### Gateway Database

```bash
STAGING_GATEWAY_DB_NAME=<nombre_bd_gateway>
STAGING_GATEWAY_DB_USER=<usuario_bd_gateway>
STAGING_GATEWAY_DB_PASSWORD=<contraseña_bd_gateway>
```

### Divipol Database

```bash
STAGING_DIVIPOL_DB_NAME=<nombre_bd_divipol>
STAGING_DIVIPOL_DB_USER=<usuario_bd_divipol>
STAGING_DIVIPOL_DB_PASSWORD=<contraseña_bd_divipol>
```

### Security

```bash
# Generar con: openssl rand -base64 64
STAGING_JWT_SECRET=<jwt_secret_base64>

# Ejemplo: http://192.168.0.58:8090,http://web:8090,http://localhost:8090
STAGING_CORS_ALLOWED_ORIGINS=<origenes_cors_separados_por_comas>
```

### Google reCAPTCHA

Obtén tus claves de: https://www.google.com/recaptcha/admin

```bash
# Clave pública (usada en el frontend)
# IMPORTANTE: Este secret también se usa durante el build del Docker image
# para embeber la clave en el JavaScript compilado del frontend
STAGING_RECAPTCHA_SITE_KEY=<tu_recaptcha_site_key>

# Clave privada (usada en el backend para verificación)
STAGING_RECAPTCHA_SECRET_KEY=<tu_recaptcha_secret_key>
```

## Verificación

Una vez configurados todos los secrets, el workflow generará automáticamente el archivo `.env.staging` en el servidor durante cada deployment con todos estos valores.

**IMPORTANTE**:

- El archivo `.env.staging` en el servidor será sobrescrito en cada deployment
- NO editar `.env.staging` manualmente en el servidor
- Todos los cambios de configuración deben hacerse actualizando los secrets en GitHub
