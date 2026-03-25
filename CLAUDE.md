# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Rules

- Hablame en español
- NO generar reportes de completación de tareas a menos que se solicite explícitamente.

## Requirements

- **Java 17** - Required for Spring Boot backend (`JAVA_HOME` must be set)
- **Node 22.22.2** - Enforced in `package.json` engines (use nvm or volta)
- **Docker** - For Testcontainers, PostgreSQL, and infrastructure services
  - Docker API version 1.45+ required (see `TROUBLESHOOTING.md` for Docker 29+ issues)

## Project Overview

**Detinio** (DEmocracia + EscruTINIO) es un sistema de escrutinio electoral. Este repositorio (`tyse-scrutiny-gateway`) es el gateway de una arquitectura de microservicios JHipster 8.11.0:

- Spring Boot 3.4.5 with reactive WebFlux (non-blocking architecture)
- React 18 with TypeScript for the frontend
- PostgreSQL with R2DBC for reactive database access
- Consul for service discovery
- Kafka for message brokering
- JWT authentication
- Spring Cloud Gateway for routing

### Microservice Architecture

```
Gateway (8080, PostgreSQL:5432)  ←→  Micro-Divipol (8081, PostgreSQL:5433)
         ↕                                    ↕
   Consul (8500) — Service Discovery    Kafka (9092) — Async Messaging
```

- **Gateway**: Frontend React + Spring Cloud Gateway + Auth JWT
- **Micro-Divipol**: API REST de división política (~18K registros, sin frontend)
- **Comunicación**: Consul (discovery), Kafka (async, topic `sse-topic`), HTTP/REST (sync vía Gateway)

## Build & Run Commands

### Development

Start backend and frontend separately in two terminals:

```bash
./mvnw              # Start Spring Boot backend on port 8080
./npmw start        # Start Webpack dev server on port 9060 (proxy → 8080)
```

Or use concurrent watch mode:

```bash
npm run watch       # Runs both backend and frontend concurrently
```

**Development Ports:** Backend: 8080 | Webpack dev server: 9060 | BrowserSync: 9000 | Micro-Divipol: 8081

**Alternative profiles:**

```bash
./mvnw -Dspring-boot.run.profiles=local-dev   # Connect to remote dev server (192.168.0.58)
./mvnw -Dspring-boot.run.profiles=docker-dev  # All services in Docker (no local Java/Node needed)
BACKEND_URL=http://remote:8080 ./npmw start    # Override webpack proxy target
```

### Testing

```bash
# Backend tests (JUnit + Spring Boot Test with Testcontainers)
./mvnw verify

# Backend unit tests only (skips npm install)
npm run backend:unit:test

# Run a single backend test class
./mvnw test -Dtest=MyTestClass

# Run a single backend test method
./mvnw test -Dtest=MyTestClass#myTestMethod

# Frontend tests (Jest)
./npmw test

# Frontend tests in watch mode
npm run test:watch

# Run a single frontend test file
npx jest --config jest.conf.js src/main/webapp/app/modules/my-module/my-component.spec.tsx

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

**IMPORTANTE**: Los servicios compartidos (Consul, Kafka, MinIO, MailHog) se levantan desde `tyse-scrutiny-infrastructure/`:

```bash
# 1. Levantar infraestructura compartida (una sola vez, desde la raíz)
cd ../tyse-scrutiny-infrastructure
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

##### `!important` Policy

`!important` is **only allowed** in: utility classes (`.bg-*`, `.text-*`, spacing), accessibility overrides (`:disabled`), and theme system overrides (`themes.scss`). **Prohibited** everywhere else.

When overriding styles, prefer in this order: (1) increase selector specificity, (2) CSS custom properties, (3) custom utility class. Use `!important` only as last resort for genuine utilities.

```scss
// ❌ Wrong
.my-component {
  color: red !important;
}

// ✅ Right — increase specificity
.dashboard .module-card .badge {
  color: white;
}
```

Max nesting depth: 3-4 levels. Prefer `@mixin` over `@extend`.

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

### Database Naming Convention

All tables use the `scr_` prefix (**Scr**utiny): `scr_[nombre_descriptivo]`

- Singular nouns, snake_case, lowercase: `scr_user`, `scr_authority_permission`
- Entity mapping: `@Table("scr_user")`
- Migrations in `src/main/resources/config/liquibase/`
- Rollback tags: `estado-vacio`, `sistema-autorizacion`

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

## Branch Model & Governance

- **`main`** ← Producción (solo merges desde develop con PR aprobado)
- **`develop`** ← Integración (PRs desde feature branches)
- **`feature/*`** ← Trabajo individual. Convención: `feature/<nombre>-<descripcion>`
- **`hotfix/*`** ← Fixes urgentes desde main

`main` y `develop` están **protegidas**: no push directo, todo via PR con al menos 1 aprobación y CI passing.

## Git Hooks & CI

### Pre-commit (Husky + lint-staged)

El pre-commit hook ejecuta `lint-staged` automáticamente: prettier y eslint sobre archivos staged.

### CI Pipeline (GitHub Actions)

El CI en `.github/workflows/ci.yml` ejecuta 5 jobs secuenciales:

1. **Code Quality** - Prettier check + nohttp validation
2. **Liquibase Verification** - Ciclo update → rollback → update contra PostgreSQL
3. **Backend Tests** - `./mvnw verify` con Testcontainers (auto-provee Postgres + Kafka)
4. **Frontend Tests** - ESLint + Jest con reporte de cobertura
5. **E2E Tests** - Cypress contra la app completa con microservicios dockerizados

### Release & Versioning

```bash
npm run release           # Bump version automático (basado en commits)
npm run release:minor     # Bump minor
npm run release:major     # Bump major
npm run release:dry-run   # Preview sin cambios
```

Usa `standard-version`: actualiza `package.json` + `pom.xml`, genera CHANGELOG, crea tag `v*`.

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
- **staging** - Staging environment configuration
- **tls** - HTTPS configuration
- **no-liquibase** - Skip database migrations
- **docker-dev** - All services in Docker containers (no local Java/Node required)
- **local-dev** - Connect to remote development server (services on 192.168.0.58)

Activate with: `./mvnw -Pprod` or `--spring.profiles.active=dev,api-docs`

## Testing Notes

- Backend tests use `@IntegrationTest` annotation with Testcontainers
- Frontend tests use Jest with React Testing Library
- E2E tests require backend running: `./mvnw spring-boot:run`
- Test database automatically spins up via Spring Boot Docker Compose integration

## Formatting & Code Style

- **Prettier** config in `.prettierrc`: width 140, single quotes, tab width 2 (JS/TS) / 4 (Java)
- **ESLint** for TypeScript/React
- **lint-staged** runs automatically on commit via Husky pre-commit hook

## Git Push con Validación CI

El pre-push hook está **desactivado** (causaba timeouts de SSH). En su lugar, usar `push.sh` que ejecuta CI completo antes del push:

```bash
./scripts/push.sh                # Push con CI completa (recomendado)
./scripts/push.sh origin develop # Push a rama específica
./scripts/push.sh --skip-ci      # Push sin CI (no recomendado)
```

**Validación manual sin push:**

```bash
./scripts/ci-local.sh            # Solo backend + frontend (rápido)
./scripts/ci-local.sh --with-e2e # Backend + frontend + E2E (completo)
```
