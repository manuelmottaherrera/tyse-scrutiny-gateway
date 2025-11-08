# Branch Protection Rules Configuration

Este documento describe cómo configurar las reglas de protección de ramas en GitHub para garantizar la calidad del código antes de hacer merge.

## Configuración Recomendada

### Para la rama `main`

**Acceso:** Settings → Branches → Add branch protection rule

**Branch name pattern:** `main`

**Configuración requerida:**

#### Require a pull request before merging

- ✅ **Require approvals:** 1
- ✅ **Dismiss stale pull request approvals when new commits are pushed**
- ✅ **Require review from Code Owners** (opcional)

#### Require status checks to pass before merging

- ✅ **Require status checks to pass before merging**
- ✅ **Require branches to be up to date before merging**

**Status checks requeridos:**

- `backend-tests` (del workflow `ci.yml`)
- `frontend-tests` (del workflow `ci.yml`)
- `e2e-tests` (del workflow `ci.yml`)
- `quality-gate` (del workflow `ci.yml`)

#### Require conversation resolution before merging

- ✅ **Require conversation resolution before merging**

#### Do not allow bypassing the above settings

- ✅ **Do not allow bypassing the above settings**
- ⚠️ **Excepciones:** Solo administradores en emergencias

---

### Para la rama `develop`

**Branch name pattern:** `develop`

**Configuración recomendada:**

#### Require a pull request before merging

- ✅ **Require approvals:** 1 (o 0 para equipos pequeños)
- ✅ **Dismiss stale pull request approvals when new commits are pushed**

#### Require status checks to pass before merging

- ✅ **Require status checks to pass before merging**
- ✅ **Require branches to be up to date before merging**

**Status checks requeridos:**

- `backend-tests` (del workflow `ci.yml`)
- `frontend-tests` (del workflow `ci.yml`)
- `quality-gate` (del workflow `ci.yml`)

**Nota:** E2E tests son opcionales en `develop` para agilizar el desarrollo.

#### Require conversation resolution before merging

- ✅ **Require conversation resolution before merging**

---

## Verificar Configuración Actual

Puedes verificar las reglas de protección actuales usando GitHub CLI:

```bash
# Instalar gh CLI si no lo tienes
# https://cli.github.com/

# Ver reglas de protección para main
gh api repos/:owner/:repo/branches/main/protection

# Ver reglas de protección para develop
gh api repos/:owner/:repo/branches/develop/protection
```

## Aplicar Configuración via GitHub CLI

Puedes aplicar las reglas automáticamente con:

```bash
# Para main
gh api -X PUT repos/:owner/:repo/branches/main/protection \
  -f required_status_checks[strict]=true \
  -f 'required_status_checks[contexts][]=backend-tests' \
  -f 'required_status_checks[contexts][]=frontend-tests' \
  -f 'required_status_checks[contexts][]=e2e-tests' \
  -f 'required_status_checks[contexts][]=quality-gate' \
  -f required_pull_request_reviews[required_approving_review_count]=1 \
  -f required_pull_request_reviews[dismiss_stale_reviews]=true \
  -f required_conversation_resolution=true \
  -f enforce_admins=true

# Para develop
gh api -X PUT repos/:owner/:repo/branches/develop/protection \
  -f required_status_checks[strict]=true \
  -f 'required_status_checks[contexts][]=backend-tests' \
  -f 'required_status_checks[contexts][]=frontend-tests' \
  -f 'required_status_checks[contexts][]=quality-gate' \
  -f required_pull_request_reviews[required_approving_review_count]=1 \
  -f required_pull_request_reviews[dismiss_stale_reviews]=true \
  -f required_conversation_resolution=true \
  -f enforce_admins=false
```

## Aplicar Configuración via Interfaz Web

### Paso a Paso (Recomendado para primera vez)

1. Ve a tu repositorio en GitHub
2. Click en **Settings** (⚙️)
3. En el menú lateral, click en **Branches**
4. Click en **Add branch protection rule**
5. Ingresa `main` como Branch name pattern
6. Configura las opciones según la sección "Configuración Recomendada"
7. Click en **Create** o **Save changes**
8. Repite el proceso para `develop`

## Workflow Típico con Branch Protection

### Antes de Branch Protection

```bash
git checkout develop
git merge feature/nueva-funcionalidad
git push  # Push directo, sin validación
```

### Después de Branch Protection

```bash
# 1. Crear PR desde feature branch
git checkout feature/nueva-funcionalidad
git push -u origin feature/nueva-funcionalidad

# 2. Crear PR en GitHub
gh pr create --base develop --title "feat: nueva funcionalidad"

# 3. GitHub Actions ejecuta automáticamente:
#    - Backend tests
#    - Frontend tests
#    - Quality gate

# 4. Si los tests fallan:
#    - Fix los errores localmente
#    - Push nuevos commits
#    - GitHub re-ejecuta los tests automáticamente

# 5. Si los tests pasan:
#    - Solicitar review (si está configurado)
#    - Una vez aprobado, hacer merge via interfaz de GitHub
```

## Beneficios

✅ **No se puede hacer merge si el CI falla**
✅ **Garantiza calidad de código en main/develop**
✅ **Historial limpio y confiable**
✅ **Detecta problemas antes de merge**
✅ **Requiere revisión de código (si está configurado)**

## Excepciones

En casos de emergencia, los administradores pueden:

1. Temporalmente deshabilitar la protección
2. Hacer el merge de emergencia
3. Re-habilitar la protección inmediatamente
4. Crear un PR de follow-up para fix

**Comando para bypass (solo admins):**

```bash
git push --force  # Solo si enforce_admins=false
```

## Testing de la Configuración

Para probar que funciona correctamente:

1. Crea un branch de prueba: `git checkout -b test/branch-protection`
2. Haz un cambio que rompa los tests
3. Push y crea un PR hacia `develop`
4. Verifica que GitHub muestre: ❌ "Some checks were not successful"
5. Verifica que el botón "Merge" esté deshabilitado
6. Fix los tests y push
7. Verifica que ahora muestre: ✅ "All checks have passed"
8. El botón "Merge" debe estar habilitado

## Troubleshooting

### "Required status checks not found"

Si ves este mensaje, significa que los nombres de los status checks no coinciden:

**Solución:**

1. Ve a un PR existente
2. Mira los nombres exactos de los checks que aparecen
3. Usa esos nombres en la configuración

Los nombres deben ser exactamente:

- `backend-tests`
- `frontend-tests`
- `e2e-tests`
- `quality-gate`

### "Branch is not up to date"

Si ves este mensaje al intentar merge:

**Solución:**

```bash
git checkout tu-feature-branch
git pull origin develop
git push
```

Esto actualiza tu branch con los últimos cambios de develop.

## Referencias

- [GitHub Docs: Branch Protection Rules](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [GitHub Docs: Required Status Checks](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches#require-status-checks-before-merging)
