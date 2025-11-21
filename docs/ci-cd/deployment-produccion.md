# Guía de Deployment a Producción

## 📋 Tabla de Contenidos

- [Introducción](#introducción)
- [Pre-requisitos](#pre-requisitos)
- [Preparación del Release](#preparación-del-release)
- [Paso a Paso: Deployment a Producción](#paso-a-paso-deployment-a-producción)
- [Versionado en Producción](#versionado-en-producción)
- [Post-Deployment](#post-deployment)
- [Rollback](#rollback)
- [Troubleshooting](#troubleshooting)
- [Checklist Final](#checklist-final)

---

## Introducción

El deployment a producción es un proceso **formal y controlado** que incluye:

- ✅ Versionado semántico automático
- ✅ Tests completos obligatorios
- ✅ CHANGELOG generado automáticamente
- ✅ Tags de git para trazabilidad
- ✅ Comunicación a stakeholders

**Frecuencia recomendada:** 1-2 deployments por semana (sprints de 1-2 semanas).

---

## Pre-requisitos

### Antes de Iniciar el Release

#### 1. Verificar Rama Develop Estable

```bash
# Todos los commits deben estar en develop
git checkout develop
git pull origin develop

# Ver commits desde último tag
git log v1.0.0..HEAD --oneline

# Verificar que no hay WIP o commits de prueba
git log --grep="WIP\|TODO\|FIXME" v1.0.0..HEAD
```

#### 2. Verificar Tests Pasan

```bash
# CI completo local
./scripts/ci-local.sh --with-e2e

# O tests individuales
./mvnw verify      # Backend
npm test           # Frontend
npm run e2e        # E2E
```

#### 3. Verificar Staging Funciona

- [ ] Staging ha estado estable por al menos 24-48 horas
- [ ] No hay bugs críticos reportados
- [ ] QA ha aprobado las features
- [ ] Demos a stakeholders exitosas

#### 4. Planificar Downtime (si aplica)

- Notificar usuarios con anticipación
- Escoger ventana de bajo tráfico
- Coordinar con equipo de infraestructura

#### 5. Backup de Producción

```bash
# Backup de base de datos
pg_dump -h prod-server -U user -d db > backup-$(date +%Y%m%d-%H%M%S).sql

# Backup de archivos de configuración
tar -czf config-backup-$(date +%Y%m%d).tar.gz /path/to/config
```

---

## Preparación del Release

### Paso 1: Verificar Conventional Commits

```bash
# Ver commits desde último release
git log v1.0.0..HEAD --oneline

# Verificar que todos usan conventional commits
# ✅ feat: agregar feature X
# ✅ fix: corregir bug Y
# ❌ Agregar feature X  (falta prefijo)
```

**Corregir commits no convencionales:**

```bash
# Si hay commits sin formato correcto, usar interactive rebase
git rebase -i v1.0.0

# Cambiar 'pick' a 'reword' para commits a corregir
# Guardar y editar mensajes según conventional commits
```

---

### Paso 2: Simular Release

```bash
# Ver qué versión se generaría
npm run release:dry-run
```

**Salida esperada:**

```
✔ bumping version in package.json from 1.0.0 to 1.1.0
✔ bumping version in pom.xml from 1.0.0 to 1.1.0
✔ created CHANGELOG.md
✔ outputting changes to CHANGELOG.md

---
### [1.1.0](https://github.com/.../compare/v1.0.0...v1.1.0) (2025-11-21)

#### Features
* **auth:** agregar autenticación 2FA ([a2f4b6c](https://github.com/.../commit/a2f4b6c))
* **divipol:** búsqueda por coordenadas GPS ([b3e5c7d](https://github.com/.../commit/b3e5c7d))

#### Bug Fixes
* **api:** corregir timeout en consultas ([c4d6e8f](https://github.com/.../commit/c4d6e8f))
---

✔ committing pom.xml and package.json and CHANGELOG.md
✔ tagging release v1.1.0
ℹ Run `git push --follow-tags origin develop` to publish
```

**Verificar:**

- [ ] Versión es correcta (MAJOR/MINOR/PATCH apropiado)
- [ ] CHANGELOG incluye todos los cambios importantes
- [ ] No hay commits irrelevantes en el CHANGELOG

---

### Paso 3: Revisar CHANGELOG (Dry-Run)

```bash
# El dry-run muestra el CHANGELOG que se generaría
# Revisar que:
# - Features importantes están listadas
# - Bug fixes críticos están listados
# - Descripciones son claras para usuarios/clientes
# - No hay typos o errores gramaticales
```

Si el CHANGELOG necesita ajustes:

```bash
# Opción A: Editar mensaje de commit
git rebase -i v1.0.0
# Cambiar mensaje de commit problemático

# Opción B: Agregar nota al CHANGELOG después del release
# (Se puede editar CHANGELOG.md manualmente después de npm run release)
```

---

### Paso 4: Comunicar el Release

**Antes del deployment:**

1. Notificar a stakeholders del upcoming release
2. Compartir CHANGELOG previsto
3. Coordinar con equipo de soporte (si aplica)
4. Notificar a usuarios si hay breaking changes

**Template de email:**

```
Asunto: [Release] TyseScrutinyGateway v1.1.0 - Deployment Programado

Hola equipo,

Programamos el deployment de la versión v1.1.0 para [FECHA] a las [HORA].

Cambios principales:
- Feature: Autenticación 2FA
- Feature: Búsqueda por coordenadas GPS en módulo divipol
- Fix: Timeout corregido en API de consultas

Downtime estimado: 5-10 minutos

CHANGELOG completo: [enlace]

Cualquier pregunta, responder a este email.

Saludos,
[Tu nombre]
```

---

## Paso a Paso: Deployment a Producción

### Fase 1: Generar Release

#### Paso 1: Merge Develop a Main

```bash
# 1. Actualizar develop
git checkout develop
git pull origin develop

# 2. Cambiar a main
git checkout main
git pull origin main

# 3. Merge con --no-ff (preserva historia)
git merge develop --no-ff -m "chore: merge develop to main for v1.1.0 release

Preparing production release with:
- Feature: 2FA authentication
- Feature: GPS search in divipol
- Multiple bug fixes

See CHANGELOG.md for full details"

# 4. Resolver conflictos si los hay
# (normalmente no debería haber si main solo recibe merges de develop)
```

**¿Por qué `--no-ff`?**

- Preserva toda la historia de feature branches
- Crea merge commit explícito
- Facilita rollback (un solo commit to revert)

---

#### Paso 2: Ejecutar Release Automático

```bash
# Generar release con standard-version
npm run release
```

**Qué hace internamente:**

1. ✅ Analiza commits desde v1.0.0
2. ✅ Determina nueva versión (v1.1.0)
3. ✅ Actualiza `package.json`: `"version": "1.1.0"`
4. ✅ Actualiza `pom.xml`: `<version>1.1.0</version>`
5. ✅ Genera/actualiza `CHANGELOG.md`
6. ✅ Crea commit: `chore(release): v1.1.0`
7. ✅ Crea tag: `v1.1.0`

**Salida esperada:**

```
✔ bumping version in package.json from 1.0.0 to 1.1.0
✔ bumping version in pom.xml from 1.0.0 to 1.1.0
✔ outputting changes to CHANGELOG.md
✔ committing pom.xml and package.json and CHANGELOG.md
✔ tagging release v1.1.0
ℹ Run `git push --follow-tags origin main` to publish
```

---

#### Paso 3: Revisar Release Generado

```bash
# Ver commit de release
git log -1 --stat

# Verificar tag
git tag -l -n5 v1.1.0

# Ver CHANGELOG
head -50 CHANGELOG.md

# Verificar versiones actualizadas
grep '"version"' package.json
# "version": "1.1.0"

grep '<version>' pom.xml | head -1
# <version>1.1.0</version>
```

**Si algo no se ve bien:**

```bash
# Revertir release
git reset --hard HEAD~1
git tag -d v1.1.0

# Corregir issue
# Volver a ejecutar npm run release
```

---

#### Paso 4: Push a Repositorio

```bash
# Push con tags
git push --follow-tags origin main

# Verificar en GitHub que:
# - Tag v1.1.0 aparece en Releases
# - CHANGELOG.md está actualizado
```

**Nota:** `--follow-tags` solo pushea tags anotados del commit pusheado (más seguro que `--tags`).

---

### Fase 2: Build de Producción

#### Paso 5: Build Local (Verificación)

```bash
# Build completo con tests
./mvnw -Pprod clean verify

# Verificar que JAR tiene versión correcta
ls -lh target/*.jar
# tyse-scrutiny-gateway-1.1.0.jar

# Verificar que versión está en el bundle
strings target/classes/static/main.*.js | grep "v1.1.0"
```

---

#### Paso 6: Build en Servidor Producción

**Conectar al servidor:**

```bash
ssh usuario@production-server
```

**En el servidor:**

```bash
# 1. Ir al directorio del proyecto
cd /path/to/tyse-scrutiny-gateway

# 2. Backup de versión actual (por si acaso)
cp target/*.jar ../backups/tyse-scrutiny-gateway-$(date +%Y%m%d-%H%M%S).jar

# 3. Pull nueva versión
git fetch origin
git checkout v1.1.0  # Checkout tag específico (más seguro que main)

# 4. Verificar que estás en la versión correcta
git describe --tags
# v1.1.0

# 5. Build de producción
./mvnw -Pprod clean package -DskipTests
# Nota: Tests ya se corrieron localmente y en CI

# 6. Verificar build exitoso
ls -lh target/*.jar
# tyse-scrutiny-gateway-1.1.0.jar (debe existir)
```

---

### Fase 3: Deployment

#### Paso 7: Detener Aplicación Actual

```bash
# Método 1: Systemd (recomendado)
sudo systemctl stop tyse-scrutiny-gateway

# Método 2: Script personalizado
./scripts/stop.sh

# Método 3: Manual
kill $(cat application.pid)

# Esperar a que se detenga completamente
tail -f logs/application.log
# Debe mostrar: "Application shutdown"
```

---

#### Paso 8: Backup Pre-Deployment

```bash
# Backup de configuración
cp -r config/ ../backups/config-$(date +%Y%m%d-%H%M%S)/

# Backup de logs (opcional)
tar -czf ../backups/logs-$(date +%Y%m%d-%H%M%S).tar.gz logs/

# Backup de base de datos (si no se hizo antes)
pg_dump -h localhost -U user tyseScrutinyGateway > ../backups/db-pre-v1.1.0.sql
```

---

#### Paso 9: Ejecutar Migraciones de BD (si aplica)

```bash
# Si hay cambios de Liquibase, verificar changelog
cat src/main/resources/config/liquibase/master.xml

# Dry-run de migraciones (opcional)
./mvnw liquibase:updateSQL > migration-preview.sql
cat migration-preview.sql  # Revisar qué se ejecutará

# Ejecutar migraciones manualmente (más control)
./mvnw liquibase:update

# O dejar que Spring Boot las ejecute al iniciar
# (configurado en application-prod.yml)
```

---

#### Paso 10: Iniciar Nueva Versión

```bash
# Método 1: Systemd
sudo systemctl start tyse-scrutiny-gateway

# Método 2: Script
./scripts/start.sh

# Método 3: Manual
java -jar target/tyse-scrutiny-gateway-1.1.0.jar \
  --spring.profiles.active=prod \
  > logs/application.log 2>&1 &

# Guardar PID
echo $! > application.pid
```

---

#### Paso 11: Monitorear Inicio

```bash
# Ver logs en tiempo real
tail -f logs/application.log

# Buscar línea de inicio exitoso
# "Started TyseScrutinyGatewayApp in X.XXX seconds"

# Si hay errores, detener inmediatamente
# Ver sección de Rollback
```

---

### Fase 4: Verificación

#### Paso 12: Health Checks

```bash
# 1. Spring Boot Actuator Health
curl http://localhost:8080/management/health
# Esperado: {"status":"UP"}

# 2. Versión correcta
curl http://localhost:8080/management/info | jq '.build.version'
# Esperado: "v1.1.0"

# 3. Consul registration
curl http://localhost:8500/v1/health/service/tysescrutinygateway
# Debe retornar servicio healthy

# 4. Database connectivity
curl http://localhost:8080/api/account
# Debe retornar datos o 401 (no 500)
```

---

#### Paso 13: Smoke Tests

```bash
# Tests básicos de funcionalidad crítica

# 1. Login
curl -X POST http://localhost:8080/api/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
# Debe retornar JWT token

# 2. API endpoint crítico
curl http://localhost:8080/api/divipol/departamentos
# Debe retornar lista de departamentos

# 3. Permisos enterprise
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/users
# Debe retornar usuarios o 403
```

---

#### Paso 14: Verificación Manual en UI

1. Abrir producción en browser: `http://production-server`
2. **Verificar versión en header:** Debe mostrar `v1.1.0`
3. **Verificar NO hay ribbon de "Desarrollo"** (solo staging lo tiene)
4. **Hacer login** con usuario admin
5. **Probar features nuevas:**
   - Si agregaste 2FA, probar flujo completo
   - Si agregaste búsqueda GPS, probar endpoint
6. **Verificar features existentes no se rompieron:**
   - CRUD de usuarios
   - Permisos enterprise
   - Módulo divipol

---

## Versionado en Producción

### Versión Semántica con Tags

Producción usa **tags de git** con **Semantic Versioning**.

**Formato:** `vMAJOR.MINOR.PATCH`

**Ejemplos:**

- `v1.0.0` - Release inicial
- `v1.1.0` - Agregadas features (MINOR bump)
- `v1.1.1` - Solo bug fixes (PATCH bump)
- `v2.0.0` - Breaking changes (MAJOR bump)

### Reglas de Versionado

```
MAJOR (v1.0.0 → v2.0.0)
├─ Breaking changes
├─ Cambios incompatibles de API
└─ Requiere cambios en clientes

MINOR (v1.0.0 → v1.1.0)
├─ Nuevas features
├─ Backwards compatible
└─ Clientes pueden actualizar sin cambios

PATCH (v1.0.0 → v1.0.1)
├─ Solo bug fixes
├─ No hay nuevas features
└─ Actualización recomendada
```

### Cómo se Muestra en la Aplicación

**En producción:**

```
Header UI: TyseScrutinyGateway - v1.1.0
```

**En staging (comparación):**

```
Header UI: TyseScrutinyGateway - v1.1.0-5-ga2f4b6c
Ribbon: "Desarrollo"
```

---

## Post-Deployment

### Paso 15: Notificar Deployment Exitoso

**Template de email:**

```
Asunto: [Completado] TyseScrutinyGateway v1.1.0 Deployed

Hola equipo,

El deployment de v1.1.0 se completó exitosamente a las [HORA].

✅ Aplicación corriendo en v1.1.0
✅ Health checks pasando
✅ Smoke tests OK
✅ Sin errores en logs

Features nuevas disponibles:
- Autenticación 2FA
- Búsqueda GPS en divipol

CHANGELOG: [enlace]

Monitorearemos por las próximas 24 horas.

Saludos,
[Tu nombre]
```

---

### Paso 16: Merge Main de Vuelta a Develop

```bash
# Asegurar que develop tiene los cambios de release

# Local
git checkout develop
git merge main --no-ff -m "chore: sync develop with main after v1.1.0 release"
git push origin develop
```

**¿Por qué?**

- Versiones de `package.json` y `pom.xml` sincronizadas
- `CHANGELOG.md` actualizado en develop
- Tag disponible para futuros releases

---

### Paso 17: Monitoreo Post-Deployment

**Primeras 2 horas (críticas):**

```bash
# Ver logs continuamente
tail -f logs/application.log | grep -i "error\|exception"

# Monitorear métricas
curl http://localhost:8080/management/metrics | jq

# Ver uso de recursos
top -p $(cat application.pid)
```

**Primeras 24 horas:**

- Revisar logs cada 2-4 horas
- Monitorear reportes de usuarios/soporte
- Verificar métricas de performance (Prometheus/Grafana si está configurado)

**Primera semana:**

- Revisión diaria de logs
- Análisis de feedback de usuarios
- Identificar bugs no detectados en staging

---

### Paso 18: Crear GitHub Release (Opcional pero recomendado)

En GitHub:

1. Ir a `Releases` → `Draft a new release`
2. Seleccionar tag `v1.1.0`
3. Título: `Release v1.1.0`
4. Descripción: Copiar CHANGELOG de esta versión
5. **Attachments:** Subir JAR de producción (opcional)
6. Publish release

**O via CLI:**

```bash
gh release create v1.1.0 \
  --title "Release v1.1.0" \
  --notes-file CHANGELOG.md \
  target/tyse-scrutiny-gateway-1.1.0.jar
```

---

## Rollback

### Cuándo Hacer Rollback

**Rollback inmediato si:**

- ❌ Aplicación no inicia después de 5 minutos
- ❌ Health checks fallan continuamente
- ❌ Bug crítico detectado que afecta funcionalidad principal
- ❌ Database corruption
- ❌ Errores en cascada en logs

**Rollback considerado si:**

- ⚠️ Performance degradada significativamente
- ⚠️ Bug no crítico pero molesto para usuarios
- ⚠️ Feature nueva no funciona como esperado

---

### Rollback Rápido (Git + Redeploy)

```bash
# 1. Checkout versión anterior
git checkout v1.0.0

# 2. Build rápido (sin tests para velocidad)
./mvnw -Pprod clean package -DskipTests

# 3. Detener aplicación actual
sudo systemctl stop tyse-scrutiny-gateway

# 4. Iniciar versión anterior
sudo systemctl start tyse-scrutiny-gateway

# 5. Verificar que funciona
curl http://localhost:8080/management/health
curl http://localhost:8080/management/info | jq '.build.version'
# Debe mostrar: "v1.0.0"

# 6. Notificar rollback
```

**Tiempo estimado:** 5-10 minutos

---

### Rollback de Base de Datos

**Si hay migraciones de Liquibase:**

```bash
# Opción 1: Rollback automático de Liquibase
./mvnw liquibase:rollback -Dliquibase.rollbackTag=v1.0.0

# Opción 2: Restaurar backup
psql -h localhost -U user tyseScrutinyGateway < ../backups/db-pre-v1.1.0.sql

# Opción 3: Rollback manual de changesets específicos
./mvnw liquibase:rollback -Dliquibase.rollbackCount=3
```

---

### Rollback con Revert (Alternativa)

**Si ya hiciste push del release:**

```bash
# 1. Revertir merge de release
git checkout main
git revert -m 1 HEAD~1  # Revierte merge commit

# 2. Push revert
git push origin main

# 3. Deploy normalmente
# (sigue pasos de deployment con la versión revertida)
```

---

### Post-Rollback

1. **Investigar causa raíz**

   ```bash
   # Analizar logs de la versión fallida
   grep -i "error" logs/application-v1.1.0-failed.log
   ```

2. **Documentar incident**

   - Qué falló
   - Cuándo se detectó
   - Tiempo de downtime
   - Acciones tomadas

3. **Corregir issue**

   - Crear branch de fix
   - Replicar problema localmente
   - Corregir y probar exhaustivamente
   - Volver a staging antes de producción

4. **Planear nuevo deployment**
   - No apresurar deployment después de rollback
   - Testing extra en staging (48+ horas estable)
   - Considerar release incremental (solo el fix)

---

## Troubleshooting

### Problema 1: "Application failed to start"

**Causa:** Error de configuración o dependencia faltante.

**Diagnóstico:**

```bash
# Ver error específico
tail -100 logs/application.log

# Errores comunes:
# - Port already in use → Verificar con lsof -i :8080
# - Cannot connect to database → Verificar PostgreSQL
# - Cannot connect to Consul → Verificar Consul
```

**Solución:**
Ver logs específicos y corregir dependencia faltante o hacer rollback.

---

### Problema 2: Version Still Shows Old Version

**Causa:** Cache del browser o build incorrecto.

**Diagnóstico:**

```bash
# Verificar versión en server
curl http://localhost:8080/management/info | jq '.build.version'

# Si muestra versión vieja:
# Verificar git.properties
cat target/classes/git.properties

# Verificar JAR
unzip -p target/*.jar BOOT-INF/classes/git.properties
```

**Solución:**

```bash
# Rebuild forzando limpieza
./mvnw clean  # Limpiar todo
./mvnw -Pprod package -DskipTests  # Build de nuevo
```

---

### Problema 3: Database Migration Failed

**Síntoma:**

```
Liquibase Update Failed: constraint violation
```

**Solución:**

```bash
# Ver qué changesets fallaron
./mvnw liquibase:status

# Rollback changesets problemáticos
./mvnw liquibase:rollbackCount -Dliquibase.rollbackCount=1

# Corregir changeset
# Editar archivo de Liquibase

# Reintentarlo
./mvnw liquibase:update
```

---

## Checklist Final

### Pre-Deployment

- [ ] Tests pasan localmente (backend + frontend + E2E)
- [ ] Staging ha estado estable 24-48 horas
- [ ] QA aprueba features
- [ ] Backups de producción realizados
- [ ] Stakeholders notificados
- [ ] Ventana de deployment coordinada

### Durante Deployment

- [ ] Merge develop → main exitoso
- [ ] `npm run release` ejecutado correctamente
- [ ] Versiones actualizadas (package.json, pom.xml)
- [ ] CHANGELOG.md generado
- [ ] Tag creado: vX.Y.Z
- [ ] Push con --follow-tags exitoso
- [ ] Build de producción completado
- [ ] Aplicación anterior detenida
- [ ] Nueva versión iniciada
- [ ] Health checks pasan
- [ ] Versión correcta en UI: vX.Y.Z

### Post-Deployment

- [ ] Smoke tests exitosos
- [ ] Features nuevas funcionan
- [ ] Features existentes no se rompieron
- [ ] No hay errores en logs
- [ ] Métricas de performance normales
- [ ] Stakeholders notificados de éxito
- [ ] Main mergeado de vuelta a develop
- [ ] GitHub Release creado (opcional)
- [ ] Monitoreo activo primeras 24 horas

---

## Ver También

- [Deployment a Staging](./deployment-staging.md) - Flujo de staging con versionado dinámico
- [Versionado Automático](./versionado-automatico.md) - Cómo funciona standard-version
- [CI/CD](./ci-cd/) - Automatización con GitHub Actions

---

## Contacto de Soporte

En caso de problemas críticos durante deployment:

- **Equipo DevOps:** [email/slack]
- **Líder Técnico:** [email/slack]
- **On-Call:** [teléfono]
