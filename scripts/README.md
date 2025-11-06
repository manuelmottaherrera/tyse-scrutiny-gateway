# Scripts de Desarrollo

Este directorio contiene scripts útiles para el desarrollo y CI/CD del proyecto.

## Tabla de Contenidos

- [ci-local.sh](#ci-localsh) - Ejecuta CI localmente
- [setup-branch-protection.sh](#setup-branch-protectionsh) - Configura protección de ramas en GitHub

## ci-local.sh

Script que replica localmente el workflow **"Gateway - CI Pipeline"** de GitHub Actions.

### Uso

```bash
# Ejecutar sin tests E2E (más rápido, ~5-10 minutos)
./scripts/ci-local.sh

# Ejecutar con tests E2E (completo, ~20-25 minutos)
./scripts/ci-local.sh --with-e2e
```

### Qué hace el script

El script ejecuta exactamente los mismos comandos que se ejecutan en el workflow `.github/workflows/ci.yml`:

#### Job 1: Backend Tests

- ✅ Ejecuta `./mvnw clean verify` con logging de errores
- ✅ Genera reportes de tests en `target/surefire-reports/`
- ✅ Genera reportes de cobertura en `target/site/jacoco/`

#### Job 2: Frontend Tests

- ✅ Instala dependencias con `npm ci`
- ✅ Valida formato con `npm run prettier:check`
- ✅ Ejecuta linting con `npm run lint`
- ✅ Ejecuta tests con `npm run test-ci`
- ✅ Genera reportes en `target/test-results/`

#### Job 3: E2E Tests (Opcional con --with-e2e)

- ✅ Construye paquete E2E con `npm run ci:e2e:package`
- ✅ Prepara ambiente Docker con `npm run ci:e2e:prepare:docker`
- ✅ Ejecuta Cypress con `npm run ci:e2e:run`
- ✅ Limpia ambiente con `npm run ci:e2e:teardown:docker`

#### Quality Gate

- ✅ Verifica que todos los jobs pasaron exitosamente
- ✅ Muestra resumen de tiempos de ejecución

### Ejemplo de salida

```
================================
Gateway - CI Pipeline (Local)
================================

[1/3] Starting Backend Tests...
  → Running Maven verify with error-level logging...
✓ Backend Tests passed (62s)

  → Test results: target/surefire-reports/
  → Coverage report: target/site/jacoco/

[2/3] Starting Frontend Tests...
  → Installing dependencies...
  → Checking code formatting...
  → Running ESLint...
  → Running Jest tests with coverage...
✓ Frontend Tests passed (45s)

  → Test results: target/test-results/
  → Coverage report: target/test-results/jest/coverage/

[3/3] E2E Tests skipped (use --with-e2e to run)

Quality Gate Check...
  Backend Tests: success
  Frontend Tests: success

================================
✅ CI Pipeline Passed
================================

Timing Summary:
  Backend Tests:   62s
  Frontend Tests:  45s
  ─────────────────────
  Total:           107s

All checks passed! ✓
The code is ready to be pushed to GitHub.
```

### Cuándo usar este script

**Antes de hacer push a GitHub:**

```bash
./scripts/ci-local.sh
```

Esto asegura que tu código pasará el CI de GitHub Actions sin errores.

**Antes de hacer merge a main/develop:**

```bash
./scripts/ci-local.sh --with-e2e
```

Esto ejecuta el pipeline completo incluyendo tests E2E.

### Requisitos

- **Java 17** - Verificar con `java -version`
- **Node.js 22.15.0+** - Verificar con `node -v`
- **Docker** (solo para E2E) - Verificar con `docker --version`

### Pre-push Hook Automático

**Importante:** Este proyecto tiene configurado un pre-push hook de Git que ejecuta automáticamente el CI completo (con E2E) antes de permitir push a las ramas `main` o `develop`.

**Comportamiento:**

- Al hacer `git push` a `main` o `develop`, se ejecuta automáticamente `./scripts/ci-local.sh --with-e2e`
- El push solo se completa si todos los tests pasan exitosamente
- Para otras ramas, el push se ejecuta sin validación

**Omitir el hook (no recomendado):**

```bash
git push --no-verify
```

**Configuración:** El hook está en `.husky/pre-push`

### Notas

- El script sale con código de error si algún test falla
- Los reportes de cobertura y tests se guardan en `target/`
- El script usa los mismos flags y configuraciones que GitHub Actions
- Con el pre-push hook activo, no necesitas ejecutar manualmente antes de push a main/develop

---

## setup-branch-protection.sh

Script para configurar automáticamente las reglas de protección de ramas en GitHub.

### Uso

```bash
./scripts/setup-branch-protection.sh
```

### Qué hace el script

Configura las siguientes reglas de protección:

#### Para la rama `main`:

- ✅ Requiere Pull Request con 1 aprobación
- ✅ Requiere que pasen status checks: backend-tests, frontend-tests, e2e-tests, quality-gate
- ✅ Requiere resolución de conversaciones
- ✅ Aplica reglas incluso para administradores
- ✅ No permite force pushes ni eliminación de la rama

#### Para la rama `develop`:

- ✅ Requiere Pull Request con 1 aprobación
- ✅ Requiere que pasen status checks: backend-tests, frontend-tests, quality-gate
- ✅ Requiere resolución de conversaciones
- ✅ Permite bypass para administradores (en caso de emergencia)
- ✅ No permite force pushes ni eliminación de la rama

### Prerequisitos

1. **GitHub CLI instalado:**

   ```bash
   # Ubuntu/Debian
   sudo apt install gh

   # O descargar desde
   # https://cli.github.com/
   ```

2. **Autenticado con GitHub:**

   ```bash
   gh auth login
   ```

3. **Permisos de administrador** en el repositorio

### Ejemplo de salida

```
================================
Branch Protection Setup
================================

Repositorio: usuario/tyse-scrutiny-gateway

⚠️  Esto configurará reglas de protección para las ramas main y develop

Configuración para main:
  - Require PR with 1 approval
  - Require status checks: backend-tests, frontend-tests, e2e-tests, quality-gate
  - Require conversation resolution
  - Enforce for admins

Configuración para develop:
  - Require PR with 1 approval
  - Require status checks: backend-tests, frontend-tests, quality-gate
  - Require conversation resolution

¿Continuar? (y/N): y

Configurando protección para rama 'main'...
✓ Protección configurada para 'main'

Configurando protección para rama 'develop'...
✓ Protección configurada para 'develop'

================================
✅ Branch Protection Configurada
================================
```

### Verificar configuración

**Via interfaz web:**

- GitHub → Settings → Branches

**Via CLI:**

```bash
gh api repos/OWNER/REPO/branches/main/protection | jq
gh api repos/OWNER/REPO/branches/develop/protection | jq
```

### Configuración manual

Si prefieres configurar manualmente o necesitas personalizar las reglas, consulta la documentación detallada en:

📄 [`.github/BRANCH_PROTECTION.md`](../.github/BRANCH_PROTECTION.md)

### Troubleshooting

**Error: "Required status checks not found"**

Los status checks deben ejecutarse al menos una vez antes de configurar la protección:

1. Crea un PR de prueba
2. Espera a que GitHub Actions ejecute el workflow `ci.yml`
3. Una vez que los checks aparezcan, ejecuta el script nuevamente

**Error: "Not authorized"**

Asegúrate de tener permisos de administrador en el repositorio.

### Beneficios de Branch Protection

✅ **No se puede hacer merge si el CI falla**
✅ **Garantiza revisión de código**
✅ **Previene cambios accidentales en main/develop**
✅ **Mantiene historial limpio**
✅ **Detecta problemas antes del merge**

### Workflow con Branch Protection

```bash
# 1. Crear feature branch
git checkout -b feature/nueva-funcionalidad

# 2. Hacer cambios y push
git push -u origin feature/nueva-funcionalidad

# 3. Crear PR en GitHub
gh pr create --base develop --title "feat: nueva funcionalidad"

# 4. GitHub Actions ejecuta automáticamente el CI
#    - Si falla: Fix y push, se re-ejecuta automáticamente
#    - Si pasa: Solicitar review

# 5. Una vez aprobado y con CI verde:
#    - Merge via interfaz de GitHub
#    - El botón de merge solo está habilitado si todo pasó
```
