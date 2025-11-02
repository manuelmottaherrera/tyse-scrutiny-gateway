# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Rules

- Hablame en español
- Una tarea compleja debe planearse correctamente. El plan debe almacenarse en `claude/actual/` mientras se trabaja en él.
- NO generar reportes de completación de tareas a menos que se solicite explícitamente.
- Mantener planes activos en `claude/actual/` y archivar en `claude/hitos/` solo cuando se complete una fase completa del proyecto.

## Project Overview

**Tyse Scrutiny Gateway** is a JHipster 8.11.0 microservice gateway application using:

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

```bash
# Start all required services
docker compose -f src/main/docker/services.yml up -d

# Individual services
npm run docker:db:up          # PostgreSQL
npm run docker:consul:up      # Consul
npm run docker:kafka:up       # Kafka
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
- **`src/main/webapp/app/shared/`** - Shared utilities
  - `reducers/` - Redux slices (authentication, locale, application-profile)
  - `components/` - Reusable components (dashboard, theme-toggle, brand-logo)
  - `context/theme-context/` - Theme provider for light/dark/system modes
  - `custom-hooks/recaptcha/` - Google reCAPTCHA v3 integration
  - `layout/` - Header, footer, menus
  - `auth/` - PrivateRoute component for protected routes
  - `error/` - Error boundaries and 404 page
- **`src/main/webapp/app/config/`** - Redux store configuration
  - Injectable reducer pattern for lazy loading
  - Middleware: error handling, notifications, loading bar, logger
- **`src/main/webapp/app/entities/`** - Entity CRUD routes

### State Management

Redux Toolkit with injectable reducers for code splitting:

- `authentication` - Current user, login state, JWT token
- `locale` - i18n language selection (Spanish/English)
- `application-profile` - Active Spring profiles and ribbon configuration

Use `useAppSelector` and `useAppDispatch` hooks for type-safe Redux access.

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
