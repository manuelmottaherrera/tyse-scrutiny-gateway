# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Rules

- Hablame en español
- Una tarea compleja debe planearse correctamente. El plan debe almacenarse en `claude/actual/` mientras se trabaja en él.
- NO generar reportes de completación de tareas a menos que se solicite explícitamente.
- Mantener planes activos en `claude/actual/` y archivar en `claude/hitos/` solo cuando se complete una fase completa del proyecto.

## Project Overview

**Detinio** is a JHipster 8.11.0 microservice gateway application using:

- Spring Boot 3.4.5 with reactive WebFlux (non-blocking architecture)
- React 18 with TypeScript for the frontend
- PostgreSQL with R2DBC for reactive database access
- Consul for service discovery
- Kafka for message brokering
- JWT authentication
- Spring Cloud Gateway for routing

## Build & Run Commands

### Development

Start backend and frontend separately in two terminals:

```bash
./mvnw              # Start Spring Boot backend on port 8080
./npmw start        # Start Webpack dev server with hot reload
```

Or use concurrent watch mode:

```bash
npm run watch       # Runs both backend and frontend concurrently
```

### Testing

```bash
# Backend tests (JUnit + Spring Boot Test)
./mvnw verify

# Backend unit tests only (skips npm install)
npm run backend:unit:test

# Frontend tests (Jest)
./npmw test

# E2E tests (Cypress)
./npmw run e2e

# Performance tests (Gatling)
./mvnw gatling:test

# Lighthouse audits
./npmw run e2e:cypress:audits
```

### Production Build

```bash
# Build JAR with production profile
./mvnw -Pprod clean verify

# Build Docker image
npm run java:docker           # AMD64
npm run java:docker:arm64     # ARM64

# Run production JAR
java -jar target/*.jar
```

### Code Quality

```bash
# Linting
npm run lint
npm run lint:fix

# Formatting
npm run prettier:check
npm run prettier:format

# Sonar analysis (requires Sonar server running)
docker compose -f src/main/docker/sonar.yml up -d
./mvnw -Pprod clean verify sonar:sonar -Dsonar.login=admin -Dsonar.password=admin
```

### Docker Services

**IMPORTANTE**: Los servicios compartidos (Consul, Kafka, MinIO, MailHog) se levantan desde `tyse-infrastructure/`:

```bash
# 1. Levantar infraestructura compartida (una sola vez, desde la raíz)
cd ../tyse-infrastructure
docker compose up -d

# 2. Levantar PostgreSQL del gateway
cd ../tyse-scrutiny-gateway
docker compose -f src/main/docker/postgresql.yml up -d

# O usar services.yml que solo levanta PostgreSQL
docker compose -f src/main/docker/services.yml up -d
```

## Architecture

### Backend Structure

The backend follows JHipster conventions with reactive Spring WebFlux:

- **`com.tyse.scrutiny.gateway.config`** - Spring configuration classes
  - `SecurityConfiguration` - Spring Security setup with JWT
  - `DatabaseConfiguration` - R2DBC reactive database config
  - `SecurityJwtConfiguration` - JWT token configuration
- **`com.tyse.scrutiny.gateway.web.rest`** - REST controllers
  - All endpoints return reactive types (`Mono<>`, `Flux<>`)
- **`com.tyse.scrutiny.gateway.service`** - Business logic layer
- **`com.tyse.scrutiny.gateway.repository`** - R2DBC reactive repositories
- **`com.tyse.scrutiny.gateway.domain`** - JPA entities with R2DBC support
- **`com.tyse.scrutiny.gateway.security`** - Authentication & authorization
- **`com.tyse.scrutiny.gateway.broker`** - Kafka producer/consumer
- **`com.tyse.scrutiny.gateway.web.filter`** - Web filters (SPA routing)

### Frontend Structure

React application with Redux Toolkit for state management:

- **`src/main/webapp/app/modules/`** - Feature modules
  - `login/` - Authentication UI
  - `account/` - User account management (settings, password reset, registration)
  - `administration/` - Admin pages (user management, metrics, health, gateway routes)
  - `home/` - Dashboard with modular grid layout
  - `divipol/` - **Módulo de División Política y Testigos Electorales**
    - `divipol.tsx` - Página principal con explorador DIVIPOL
    - `divipol.reducer.ts` - Redux slice del módulo
    - `components/` - Componentes reutilizables (DivipolTable, PuestoInfoTab, etc.)
    - `pages/` - **Páginas con rutas dedicadas (Full Page Routes)**
      - `puesto-detail-page.tsx` - Detalle de puesto con tabs
      - `organizaciones-page.tsx` - CRUD de organizaciones políticas
      - `comisiones-page.tsx` - CRUD de comisiones escrutadoras
      - `reclamaciones-page.tsx` - Listado de reclamaciones
      - `configuracion-electoral-page.tsx` - Configuración del sistema
- **`src/main/webapp/app/shared/`** - Shared utilities
  - `reducers/` - Redux slices (authentication, locale, application-profile)
  - `components/` - Reusable components (dashboard, theme-toggle, brand-logo)
  - `context/theme-context/` - Theme provider for light/dark/system modes
  - `custom-hooks/recaptcha/` - Google reCAPTCHA v3 integration
  - `layout/` - Header, footer, menus
  - `auth/` - PrivateRoute component for protected routes
  - `error/` - Error boundaries and 404 page
  - `services/divipol.service.ts` - **API client para micro-divipol**
- **`src/main/webapp/app/config/`** - Redux store configuration
  - Injectable reducer pattern for lazy loading
  - Middleware: error handling, notifications, loading bar, logger
- **`src/main/webapp/app/entities/`** - Entity CRUD routes

### Rutas del Módulo DIVIPOL

El módulo divipol usa **Full Page Routes** (no modales) para mejor UX mobile y URL-driven state:

```
/divipol                        → DivipolPage (explorador jerárquico)
/divipol/puestos/:puestoId      → PuestoDetailPage (tabs: info, jurados, testigos)
/divipol/organizaciones         → OrganizacionesPage (CRUD)
/divipol/comisiones             → ComisionesPage (CRUD)
/divipol/reclamaciones          → ReclamacionesPage (listado con filtros)
/divipol/configuracion          → ConfiguracionElectoralPage (admin)
```

**Patrón URL-driven:** El estado de navegación se refleja en la URL. Al refrescar, la app restaura el estado exacto:

- Tabs activos via query param: `/divipol/puestos/123?tab=testigos`
- Filtros via query params: `/divipol/reclamaciones?estado=PRESENTADA`

### State Management

Redux Toolkit with injectable reducers for code splitting:

- `authentication` - Current user, login state, JWT token
- `locale` - i18n language selection (Spanish/English)
- `application-profile` - Active Spring profiles and ribbon configuration

Use `useAppSelector` and `useAppDispatch` hooks for type-safe Redux access.

### Frontend Styling

#### Color Variables (IMPORTANT)

All SCSS files MUST use color variables defined in `src/main/webapp/app/_color-variables.scss` instead of hardcoded color values.

**Why?**

- Ensures consistent theming across light and dark modes
- Centralizes color management for easier maintenance
- Improves accessibility and visual consistency

**How to use:**

```scss
// Import variables at the top of your SCSS file
@import '../../color-variables';

// Use variables instead of hardcoded colors
.my-component {
  color: $color-text-primary; // NOT #333
  background: $color-white; // NOT #fff
  border: 1px solid $color-gray-light; // NOT #eee
  box-shadow: 0 2px 4px $color-shadow-medium; // NOT rgba(0,0,0,0.15)
}
```

**Available variable categories:**

- **Base colors**: `$color-white`, `$color-black`, `$color-gray-*`
- **Text colors**: `$color-text-primary`, `$color-text-secondary`, `$color-text-tertiary`
- **State colors**: `$color-danger`, `$color-warning`, `$color-success`
- **Shadows**: `$color-shadow-primary`, `$color-shadow-medium`, `$color-shadow-strong`, etc.
- **Dark theme**: `$color-dark-overlay-*`, `$color-dark-background`
- **Component-specific**: See `_color-variables.scss` for the complete list

**Before adding new colors:**

1. Check if a suitable variable already exists in `_color-variables.scss`
2. If not, add the new color variable to `_color-variables.scss` following the existing pattern
3. Use the new variable in your component

#### CSS/SCSS Best Practices

##### !important Usage Policy

The codebase follows **strict guidelines** for `!important` usage to maintain clean, maintainable CSS:

**✅ Allowed cases (justified):**

- **Utility classes**: Classes designed to override component styles
  - `.bg-*`, `.text-*` color utilities
  - Spacing utilities (`.pad-*`, `.margin-*`)
  - `.fullscreen` and similar layout utilities
- **Accessibility overrides**: Disabled states that must always be visible
  - `:disabled` form controls
  - `[disabled]` attribute selectors
- **Theme system overrides**: Framework theme variables
  - Navbar background colors
  - Bootstrap variable overrides in `themes.scss`

**❌ Prohibited cases:**

- Component-specific styling
- Layout positioning (flexbox, grid)
- Typography (except utility classes)
- Color overrides (use custom classes or CSS variables instead)
- Any case where specificity can be increased

##### When you need to override styles:

Follow this decision tree:

1. **First**: Try increasing selector specificity

   ```scss
   // Instead of:
   .badge {
     color: white !important;
   }

   // Use more specific selector:
   .dashboard .module-card .badge {
     color: white;
   }
   ```

2. **Second**: Use CSS custom properties (CSS variables)

   ```scss
   :root {
     --badge-color: black;
   }
   [data-theme='dark'] {
     --badge-color: white;
   }
   .badge {
     color: var(--badge-color);
   }
   ```

3. **Third**: Create a custom utility/helper class

   ```scss
   // Create semantic class in themes.scss
   .badge-coming-soon {
     background-color: var(--bs-secondary);
     color: var(--bs-body-color);
   }
   ```

4. **Last resort**: Use `!important` ONLY for genuine utility classes

##### Examples

**❌ Wrong approach:**

```scss
.my-component {
  color: red !important; // Never do this
  background: #fff !important; // Hardcoded color + !important
}
```

**✅ Right approach:**

```scss
// Option 1: Custom class with proper cascade
.badge-coming-soon {
  background-color: var(--bs-secondary);
  color: var(--bs-body-color);
  font-weight: 700;
}

[data-theme='dark'] .badge-coming-soon {
  background-color: $color-gray-darker;
  color: $color-white-full;
}

// Option 2: Increase specificity
.dashboard .module-card .badge {
  background-color: var(--bs-secondary);
}
```

##### Sass/SCSS Resources

For more information on Sass best practices:

- [Sass Official Documentation](https://sass-lang.com/documentation)
- Specificity follows standard CSS rules
- Use nesting carefully (max 3-4 levels deep)
- Prefer composition over inheritance with `@mixin` and `@extend`

### Key Technologies

- **Reactive Programming**: All backend operations use Reactor (`Mono`, `Flux`)
- **R2DBC**: Non-blocking database driver (not JPA)
- **Spring Cloud Gateway**: Route definitions for microservice proxying
- **Liquibase**: Database migrations in `src/main/resources/config/liquibase/`
- **i18n**: Translations in `src/main/webapp/i18n/` (es, en)
- **Webpack**: Module bundling with dev server and HMR
- **Bootswatch**: UI theme (Sketchy dark variant)

## API-First Development

OpenAPI specifications can be edited and code generated:

```bash
# Edit API spec (launches Swagger Editor in Docker)
docker compose -f src/main/docker/swagger-editor.yml up -d
# Access at http://localhost:7742

# Generate API code from src/main/resources/swagger/api.yml
./mvnw generate-sources
```

Then implement generated delegate classes with `@Service` annotations.

## Important Patterns

### Adding New REST Endpoints

1. Create controller in `web.rest` package
2. Use reactive return types: `Mono<ResponseEntity<T>>` or `Flux<T>`
3. Apply `@PreAuthorize` for security
4. Add API documentation with `@Operation` (Swagger)

### Database Operations

- Use R2DBC repositories, not JPA
- All queries return `Mono<>` or `Flux<>`
- Manual SQL in repositories uses `DatabaseClient`
- Row mappers in `repository.rowmapper` package

### Frontend Routing

- Use React Router v7
- Protected routes wrap components with `<PrivateRoute>`
- Entity routes registered in `entities/routes.tsx`
- Error boundaries in `shared/error/error-boundary-routes.tsx`

### Async Reducers

New feature modules should inject reducers dynamically:

```typescript
injectableStore.injectReducer('myFeature', myFeatureReducer);
```

### Theme Context

The app supports light/dark/system themes via `ThemeContext`. New components should use:

```typescript
const { theme } = useContext(ThemeContext);
```

## Service Discovery

The gateway requires Consul running on `localhost:8500`. It will refuse to start without it in production. For development, Consul can be disabled in configuration or started with:

```bash
npm run docker:consul:up
```

## Commit Conventions

This project uses commitlint with conventional commits. Commit messages must follow the format:

```
type(scope): subject

[optional body]
```

Example: `feat(dashboard): add new metrics card`

## Configuration Profiles

- **dev** - Development mode with hot reload, debug logging, faker data
- **prod** - Production optimizations, minified assets
- **api-docs** - Enables Swagger UI at `/swagger-ui.html`
- **e2e** - For Cypress end-to-end tests
- **tls** - HTTPS configuration

Activate with: `./mvnw -Pprod` or `--spring.profiles.active=dev,api-docs`

## Testing Notes

- Backend tests use `@IntegrationTest` annotation with Testcontainers
- Frontend tests use Jest with React Testing Library
- E2E tests require backend running: `./mvnw spring-boot:run`
- Test database automatically spins up via Spring Boot Docker Compose integration

## JHipster Entities

Entity definitions are in `.jhipster/*.json`. To regenerate or create entities:

```bash
jhipster entity <entity-name>
```

This generates backend (domain, repository, service, REST) and frontend (React components, reducers).

## Git Push con Validación CI

### Problema con Pre-push Hooks

Los hooks de git tienen limitaciones de tiempo debido a timeouts de SSH (~5-7 minutos). Ejecutar CI completo (con E2E) en un pre-push hook causaba:

- Timeout de SSH ("Connection to github.com closed by remote host")
- Push incompleto o bloqueado
- Experiencia de desarrollo frustrante

### Solución: Script `push.sh`

El pre-push hook ha sido **desactivado**. En su lugar, usa el script `push.sh` que:

1. ✅ Ejecuta CI completo (backend + frontend + E2E) ANTES de abrir conexión SSH
2. ✅ Solo hace `git push` si todos los tests pasan
3. ✅ No tiene timeout porque CI y push son operaciones separadas

**Uso básico:**

```bash
# Push con validación CI completa (recomendado)
./scripts/push.sh

# Push a rama específica
./scripts/push.sh origin develop

# Push sin CI (no recomendado)
./scripts/push.sh --skip-ci
```

**Flujo de trabajo:**

```
./scripts/push.sh
  ↓
[Pre-flight] Limpia ambiente CI (~2s)
  ↓
[Job 1/3] Backend tests (~1-2 min)
  ↓
[Job 2/3] Frontend tests (~2-3 min)
  ↓
[Job 3/3] E2E tests (~2-3 min)
  ↓
[Post-flight] Limpia procesos (~1s)
  ↓
✅ CI Passed
  ↓
git push origin develop
  ↓
✅ Push exitoso (sin timeout!)
```

**Validación manual sin push:**

```bash
# Solo backend + frontend (rápido)
./scripts/ci-local.sh

# Backend + frontend + E2E (completo)
./scripts/ci-local.sh --with-e2e
```

**Push directo sin validación:**

```bash
# Solo si estás 100% seguro (no recomendado)
git push origin develop
```

**Ventajas:**

- ✅ No hay timeout de SSH
- ✅ CI completo con E2E tests
- ✅ Control total sobre cuándo validar
- ✅ Feedback claro de errores antes del push
- ✅ Opción de skip para emergencias
