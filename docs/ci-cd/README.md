# Pipeline CI/CD - Tyse Scrutiny (Multi-Repositorio)

Pipeline completo de Integración Continua y Despliegue Continuo para la plataforma de microservicios Tyse Scrutiny.

## Resumen de la Arquitectura

Tyse Scrutiny está compuesto por **dos repositorios GitHub separados**:

1. **Repositorio Gateway** (`tyse-scrutiny-gateway`)

   - Contiene: Frontend React + Backend Spring Boot
   - Gestiona: Infraestructura compartida (Consul, Kafka)
   - Maneja: Orquestación del despliegue de ambos servicios

2. **Repositorio Divipol** (`tyse-scrutiny-micro-divipol`)
   - Contiene: Microservicio Spring Boot (solo API)
   - Proporciona: Datos de división política de Colombia

## Estructura de Repositorios

```
GitHub:
├── manuelmottaherrera/tyse-scrutiny-gateway
│   ├── .github/workflows/
│   │   ├── ci.yml            # Gateway CI (backend + frontend + E2E)
│   │   ├── build.yml         # Build Gateway Docker image
│   │   ├── deploy-staging.yml # Deploy BOTH services
│   │   └── sonarcloud.yml    # Code quality analysis
│   ├── deployment/
│   │   ├── docker-compose.staging.yml  # Full stack
│   │   ├── scripts/
│   │   └── .env.staging.example
│   └── docs/ci-cd/           # This documentation
│
└── manuelmottaherrera/tyse-scrutiny-micro-divipol
    └── .github/workflows/
        ├── ci.yml            # Divipol CI (backend tests)
        ├── build.yml         # Build Divipol Docker image
        └── sonarcloud.yml    # Code quality analysis
```

---

## Inicio Rápido

### Para Desarrolladores

#### Trabajando en Gateway

```bash
# Clonar repositorio Gateway
git clone git@github.com:manuelmottaherrera/tyse-scrutiny-gateway.git
cd tyse-scrutiny-gateway

# Crear rama de feature
git checkout -b feature/my-feature

# Hacer cambios, commit, push
git add .
git commit -m "feat: add new feature"
git push origin feature/my-feature

# CI se ejecuta automáticamente:
# ✅ Tests backend
# ✅ Tests frontend
# ✅ Verificaciones de calidad de código
```

#### Trabajando en Divipol

```bash
# Clonar repositorio Divipol
git clone git@github.com:manuelmottaherrera/tyse-scrutiny-micro-divipol.git
cd tyse-scrutiny-micro-divipol

# Crear rama de feature
git checkout -b feature/my-feature

# Hacer cambios, commit, push
git add .
git commit -m "feat: add divipol feature"
git push origin feature/my-feature

# CI se ejecuta automáticamente:
# ✅ Tests backend
# ✅ Verificaciones de calidad de código
```

### Flujo de Despliegue

```
┌─────────────────────────────────────────────────────────────────┐
│                    REPOSITORIOS SEPARADOS                        │
└─────────────────────────────────────────────────────────────────┘

Repo Gateway:                          Repo Divipol:
  Push a develop                         Push a develop
       ↓                                      ↓
  Tests CI (15 min)                      Tests CI (10 min)
       ↓                                      ↓
  Construir Imagen                       Construir Imagen
       ↓                                      ↓
  Push a GHCR                            Push a GHCR
       ↓                                      ↓
       └──────────────┬────────────────────────┘
                      ↓
         Disparador Manual de Despliegue
          (workflow_dispatch del repo Gateway)
                      ↓
          Desplegar Ambos Servicios
                      ↓
          Verificaciones de Salud
                      ↓
              ✅ Staging Activo
```

---

## Explicación de los Workflows

### Workflows del Repositorio Gateway

#### 1. Pipeline CI (`ci.yml`)

**Disparador**: Cada push/PR a main o develop

**Jobs**:

- ✅ Tests Backend (Maven + JUnit + Testcontainers)
- ✅ Tests Frontend (Jest + ESLint + Prettier)
- ✅ Tests E2E (Cypress - solo main/develop)
- ✅ Quality Gate

**Duración**: ~15 minutos

#### 2. Build (`build.yml`)

**Disparador**: Después del éxito del CI en main/develop

**Jobs**:

- 🏗️ Construir JAR de producción
- 🐳 Crear imagen Docker
- 📦 Push a GitHub Container Registry (`ghcr.io/manuelmottaherrera/tyse-scrutiny-gateway`)

**Tags**: `develop`, `main`, `develop-sha-abc123`, `latest` (solo main)

**Duración**: ~5-8 minutos

#### 3. Deploy Staging (`deploy-staging.yml`)

**Disparador**: Manual (`workflow_dispatch`)

**Qué hace**:

- Descarga las últimas imágenes de **ambos** Gateway y Divipol
- SSH al servidor de staging
- Ejecuta script de despliegue (backup, stop, start, verificación de salud)
- Verifica que ambos servicios estén saludables

**Parámetros**:

- `gateway_tag`: Tag de imagen Gateway (por defecto: `develop`)
- `divipol_tag`: Tag de imagen Divipol (por defecto: `develop`)

**Duración**: ~3-5 minutos

#### 4. SonarCloud (`sonarcloud.yml`)

**Disparador**: Cada push/PR (en paralelo con CI)

**Analiza**:

- Código backend Java
- Código frontend TypeScript/React
- Cobertura de tests
- Vulnerabilidades de seguridad

**Duración**: ~8-10 minutos

---

### Workflows del Repositorio Divipol

#### 1. Pipeline CI (`ci.yml`)

**Disparador**: Cada push/PR a main o develop

**Jobs**:

- ✅ Tests Backend (Maven + JUnit)
- ✅ Quality Gate

**Duración**: ~10 minutos

#### 2. Build (`build.yml`)

**Disparador**: Después del éxito del CI en main/develop

**Jobs**:

- 🏗️ Construir JAR de producción
- 🐳 Crear imagen Docker
- 📦 Push a GitHub Container Registry (`ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol`)

**Tags**: `develop`, `main`, `develop-sha-abc123`, `latest` (solo main)

**Duración**: ~5 minutos

#### 3. SonarCloud (`sonarcloud.yml`)

**Disparador**: Cada push/PR (en paralelo con CI)

**Analiza**:

- Código backend Java
- Cobertura de tests
- Vulnerabilidades de seguridad

**Duración**: ~5-8 minutos

---

## Cómo Desplegar

### Flujo Automático (Recomendado)

1. **Mergear PR de Gateway a develop**:

   ```bash
   # CI se ejecuta → Build crea imagen
   ```

2. **Mergear PR de Divipol a develop**:

   ```bash
   # CI se ejecuta → Build crea imagen
   ```

3. **Desplegar desde el repo Gateway**:
   - Ir al repositorio Gateway en GitHub
   - Hacer clic en **Actions** → **Deploy to Staging**
   - Hacer clic en **Run workflow**
   - Dejar los tags como `develop` (o especificar tags personalizados)
   - Hacer clic en **Run workflow**

### Despliegue Manual con Versiones Específicas

Si deseas desplegar versiones específicas (ej: probar un hotfix):

1. Ir al repositorio **Gateway** → **Actions** → **Deploy to Staging**
2. Hacer clic en **Run workflow**
3. Ingresar tags personalizados:
   - `gateway_tag`: `develop-sha-abc123` (o cualquier tag)
   - `divipol_tag`: `develop-sha-def456` (o cualquier tag)
4. Hacer clic en **Run workflow**

---

## Acceso a Servicios Desplegados

Después de un despliegue exitoso:

- **Gateway UI**: http://tu-servidor:8090
- **Gateway API Docs**: http://tu-servidor:8090/swagger-ui.html
- **Divipol API**: http://tu-servidor:8091/swagger-ui.html
- **Consul UI**: http://tu-servidor:8510
- **Verificaciones de Salud**:
  - Gateway: http://tu-servidor:8090/management/health
  - Divipol: http://tu-servidor:8091/management/health

---

## Secrets de GitHub Requeridos

### Repositorio Gateway

| Secret                      | Descripción          | Ejemplo                 |
| --------------------------- | -------------------- | ----------------------- |
| `SSH_HOST`                  | IP servidor staging  | `192.168.1.50`          |
| `SSH_USER`                  | Usuario SSH          | `deploy`                |
| `SSH_PRIVATE_KEY`           | Clave privada SSH    | `-----BEGIN RSA...`     |
| `STAGING_HOST`              | Igual a SSH_HOST     | `192.168.1.50`          |
| `SONAR_TOKEN`               | Token SonarCloud     | `squ_abc123...`         |
| `SONAR_ORGANIZATION`        | Org SonarCloud       | `manuelmottaherrera`    |
| `SONAR_PROJECT_KEY_GATEWAY` | Key proyecto Gateway | `tyse-scrutiny-gateway` |

### Repositorio Divipol

| Secret                      | Descripción          | Ejemplo                       |
| --------------------------- | -------------------- | ----------------------------- |
| `SONAR_TOKEN`               | Token SonarCloud     | `squ_abc123...`               |
| `SONAR_ORGANIZATION`        | Org SonarCloud       | `manuelmottaherrera`          |
| `SONAR_PROJECT_KEY_DIVIPOL` | Key proyecto Divipol | `tyse-scrutiny-micro-divipol` |

---

## Configuración del Servidor

Todos los archivos de despliegue están en el **repositorio Gateway** bajo `deployment/`:

### En el Servidor de Staging

**Ubicación**: `/opt/tyse-scrutiny/`

```
/opt/tyse-scrutiny/
├── docker-compose.staging.yml
├── .env.staging              # Tu configuración (no en git)
├── scripts/
│   ├── deploy.sh            # Script de despliegue
│   └── health-check.sh      # Verificación de salud
└── backups/                  # Backups automáticos
```

### Archivo de Entorno (`.env.staging`)

Copiar desde la plantilla:

```bash
cd /opt/tyse-scrutiny
cp .env.staging.example .env.staging
nano .env.staging
```

**Variables clave**:

```bash
# Configuración de imágenes (repos separados!)
REGISTRY=ghcr.io
GATEWAY_IMAGE=manuelmottaherrera/tyse-scrutiny-gateway
DIVIPOL_IMAGE=manuelmottaherrera/tyse-scrutiny-micro-divipol
IMAGE_TAG_GATEWAY=develop
IMAGE_TAG_DIVIPOL=develop

# Puertos (personalizar para evitar conflictos)
GATEWAY_PORT=8090
DIVIPOL_PORT=8091
CONSUL_PORT=8510
KAFKA_PORT=9102

# Base de datos (tu servidor de base de datos)
DB_HOST=tu-ip-servidor-bd
GATEWAY_DB_PASSWORD=tu-contraseña
DIVIPOL_DB_PASSWORD=tu-contraseña

# Seguridad
JWT_SECRET=tu-jwt-secret-base64
```

---

## Tareas Comunes

### Verificar Estado del Despliegue

```bash
# SSH al servidor
ssh tu-usuario@tu-servidor

# Verificar contenedores corriendo
docker ps | grep tyse

# Ver logs
docker logs tyse-gateway-staging
docker logs tyse-divipol-staging

# Ejecutar verificación de salud
cd /opt/tyse-scrutiny
./scripts/health-check.sh
```

### Rollback del Despliegue

```bash
# SSH al servidor
ssh tu-usuario@tu-servidor
cd /opt/tyse-scrutiny

# Listar backups
ls -lh backups/

# Restaurar (reemplazar TIMESTAMP)
cp backups/.env.staging.TIMESTAMP .env.staging

# Re-desplegar
./scripts/deploy.sh
```

### Ver Estado del Pipeline

**Gateway**:

- https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/actions

**Divipol**:

- https://github.com/manuelmottaherrera/tyse-scrutiny-micro-divipol/actions

---

## Resolución de Problemas

### CI Falla en un Repositorio

**Problema**: Los tests fallan en GitHub Actions

**Solución**:

```bash
# Ejecutar tests localmente
./mvnw clean verify
npm run test-ci  # Solo Gateway
```

### Falla el Build

**Problema**: Falla la construcción de la imagen Docker

**Solución**:

- Verificar que el Dockerfile esté presente en `src/main/docker/`
- Asegurar que el JAR de producción se construye: `./mvnw -Pprod clean verify -DskipTests`
- Revisar logs de GitHub Actions para ver el error específico

### Falla el Despliegue

**Problema**: Falla el workflow de despliegue

**Verificar**:

1. Conexión SSH: `ssh tu-usuario@tu-servidor`
2. Docker login funciona: `docker login ghcr.io`
3. Las imágenes existen:
   - `ghcr.io/manuelmottaherrera/tyse-scrutiny-gateway:develop`
   - `ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:develop`
4. El archivo de entorno es correcto: `/opt/tyse-scrutiny/.env.staging`

### Servicios No Saludables

**Verificar**:

```bash
# Logs del servidor
docker logs tyse-gateway-staging
docker logs tyse-divipol-staging

# Conectividad de base de datos
psql -h servidor-bd -U tysescrutinygateway -d tysescrutinygateway
psql -h servidor-bd -U tysescrutinymicrodivipol -d tysescrutinymicrodivipol

# Conflictos de puerto
sudo netstat -tuln | grep -E '8090|8091'
```

---

## Documentación

- **[SETUP.md](./SETUP.md)** - Guía completa de configuración (servidores, bases de datos, GitHub)
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Detalles técnicos de la arquitectura
- **[MULTI-REPO-COORDINATION.md](./MULTI-REPO-COORDINATION.md)** - Guía de flujo de trabajo multi-repo

---

## Estrategia de Ramas

### Ambos Repositorios

| Rama        | CI  | Build     | Deploy |
| ----------- | --- | --------- | ------ |
| `feature/*` | ✅  | ❌        | ❌     |
| `develop`   | ✅  | ✅ (auto) | Manual |
| `main`      | ✅  | ✅ (auto) | Manual |

**Flujo de Trabajo Recomendado**:

1. Crear rama de feature
2. Desarrollar y hacer push (CI se ejecuta)
3. Crear PR a `develop`
4. Mergear después de la aprobación
5. Build ocurre automáticamente
6. Desplegar manualmente desde el repo Gateway

---

## Rendimiento

| Tarea                      | Duración   |
| -------------------------- | ---------- |
| CI Gateway                 | ~15 min    |
| Build Gateway              | ~5-8 min   |
| CI Divipol                 | ~10 min    |
| Build Divipol              | ~5 min     |
| Despliegue                 | ~3-5 min   |
| **Total (ambos + deploy)** | ~25-30 min |

---

## Soporte

Para problemas o preguntas:

1. Consultar esta documentación
2. Revisar logs de GitHub Actions
3. Verificar logs del servidor: `docker logs nombre-contenedor`
4. Crear issue en el repositorio correspondiente

---

**Última Actualización**: 2025-01-04
**Repositorio**: manuelmottaherrera/tyse-scrutiny-gateway
