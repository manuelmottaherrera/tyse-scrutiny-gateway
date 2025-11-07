# Diagrama de Arquitectura de Componentes

**Versión:** 1.0
**Fecha:** 2025-11-07

---

## 1. Arquitectura General del Sistema

```mermaid
graph TB
    subgraph "Frontend - React 18"
        UI[React Components]
        Redux[Redux Toolkit Store]
        API[Axios API Clients]
        UI --> Redux
        Redux --> API
    end

    subgraph "Backend - Spring Boot 3.4.5"
        Gateway[Spring Cloud Gateway]
        Security[Spring Security + JWT]
        Controllers[REST Controllers]
        Services[Service Layer]
        Repos[R2DBC Repositories]

        Gateway --> Security
        Security --> Controllers
        Controllers --> Services
        Services --> Repos
    end

    subgraph "Database - PostgreSQL"
        Tables[(scr_* tables)]
    end

    subgraph "Jobs - Scheduled Tasks"
        CleanupJob[ExpiredAuthoritiesCleanupJob<br/>Cron: 2 AM daily]
    end

    API -->|HTTP/REST + JWT| Gateway
    Repos -->|R2DBC Reactive| Tables
    CleanupJob -.->|Async update| Tables

    style UI fill:#61dafb
    style Gateway fill:#6db33f
    style Tables fill:#336791
    style CleanupJob fill:#ff6b6b
```

---

## 2. Arquitectura de Capas (Backend)

```mermaid
graph TD
    subgraph "Layer 1: Entry Point"
        GW[Spring Cloud Gateway<br/>Port 8080]
    end

    subgraph "Layer 2: Security"
        JWT[JWTFilter<br/>Validate token + extract roles]
        SEC[SecurityConfiguration<br/>@PreAuthorize evaluation]
    end

    subgraph "Layer 3: Controllers (REST API)"
        C1[AuthorityResource<br/>/api/authorities]
        C2[PermissionResource<br/>/api/permissions]
        C3[UserAuthorityResource<br/>/api/user-authorities]
        C4[AuthorizationDashboardResource<br/>/api/authorization/dashboard]
    end

    subgraph "Layer 4: Services (Business Logic)"
        S1[AuthorityService<br/>CRUD + Audit]
        S2[PermissionService<br/>CRUD]
        S3[UserAuthorityService<br/>Assign/Revoke]
        S4[AuthorityAuditService<br/>Query audit logs]
        S5[AuthorizationDashboardService<br/>Metrics aggregation]
        S6[AuditExportService<br/>CSV/JSON export]
    end

    subgraph "Layer 5: Repositories (Data Access)"
        R1[AuthorityRepository<br/>R2DBC]
        R2[PermissionRepository<br/>R2DBC]
        R3[UserAuthorityRepository<br/>R2DBC]
        R4[AuthorityAuditRepository<br/>R2DBC]
    end

    subgraph "Layer 6: Database"
        DB[(PostgreSQL<br/>scr_* tables)]
    end

    GW --> JWT
    JWT --> SEC
    SEC --> C1 & C2 & C3 & C4
    C1 --> S1
    C2 --> S2
    C3 --> S3
    C4 --> S5
    S1 --> R1 & R4
    S2 --> R2
    S3 --> R3
    S4 --> R4
    S5 --> R1 & R2 & R3 & R4
    S6 --> R4
    R1 & R2 & R3 & R4 --> DB

    style GW fill:#6db33f
    style JWT fill:#ff6b6b
    style DB fill:#336791
```

---

## 3. Componentes Backend Detallados

### 3.1 REST Controllers (6 componentes)

```mermaid
classDiagram
    class AuthorityResource {
        +getAllAuthorities() Flux~AuthorityDTO~
        +getAuthority(id) Mono~AuthorityDTO~
        +createAuthority(dto) Mono~AuthorityDTO~
        +updateAuthority(id, dto) Mono~AuthorityDTO~
        +deleteAuthority(id) Mono~Void~
    }

    class PermissionResource {
        +getAllPermissions() Flux~PermissionDTO~
        +getPermission(id) Mono~PermissionDTO~
        +createPermission(dto) Mono~PermissionDTO~
        +updatePermission(id, dto) Mono~PermissionDTO~
    }

    class UserAuthorityResource {
        +getUserAuthorities(userId) Flux~UserAuthorityDTO~
        +getValidAuthorities(userId) Flux~UserAuthorityDTO~
        +assignAuthority(request) Mono~UserAuthorityDTO~
        +revokeAuthority(id, request) Mono~Void~
        +getExpiringAuthorities(days) Flux~UserAuthorityDTO~
    }

    class AuthorityPermissionResource {
        +getPermissions(authorityId) Flux~PermissionDTO~
        +assignPermission(authorityId, permissionId) Mono~Void~
        +removePermission(authorityId, permissionId) Mono~Void~
    }

    class AuthorizationDashboardResource {
        +getDashboardMetrics() Mono~DashboardMetricsDTO~
    }

    class AuthorityAuditResource {
        +searchAudits(criteria, pageable) Mono~Page~
        +exportToCsv(criteria) Mono~byte[]~
        +exportToJson(criteria) Mono~byte[]~
    }
```

**Seguridad:**

- Todos los endpoints requieren `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`
- JWT validation en `JWTFilter`

---

### 3.2 Service Layer (8 componentes)

```mermaid
classDiagram
    class AuthorityService {
        -AuthorityRepository repository
        -AuthorityAuditService auditService
        +createAuthority(dto, exchange) Mono~Authority~
        +updateAuthority(id, dto, exchange) Mono~Authority~
        +deactivateAuthority(id, exchange) Mono~Void~
        +findActiveAuthorities() Flux~Authority~
        -captureMetadata(exchange) Metadata
        -createAuditLog(action, old, new)
    }

    class UserAuthorityService {
        -UserAuthorityRepository repository
        +assignAuthority(userId, authorityId, expiresAt) Mono~UserAuthority~
        +revokeById(id, reason) Mono~Void~
        +getValidAuthorities(userId) Flux~UserAuthority~
        +findExpiringWithinDays(days) Flux~UserAuthority~
    }

    class AuthorizationDashboardService {
        -AuthorityRepository authRepo
        -PermissionRepository permRepo
        -UserAuthorityRepository uaRepo
        -DatabaseClient db
        +getDashboardMetrics() Mono~DashboardMetricsDTO~
        -getTotalAuthorities() Mono~Long~
        -getActiveUsers() Mono~Long~
        -getExpiringRoles(days) Flux~ExpiringRoleDTO~
        -getTopAuthorities(limit) Flux~AuthorityUsageDTO~
    }

    class AuditExportService {
        -AuthorityAuditRepository repository
        +exportToCsv(criteria) Mono~byte[]~
        +exportToJson(criteria) Mono~byte[]~
        -escapeCsv(value) String
        -buildCsvRow(audit) String
    }

    AuthorityService --> AuthorityAuditService : creates audit logs
    UserAuthorityService --> AuthorityService : validates authority exists
    AuthorizationDashboardService --> UserAuthorityService : queries metrics
```

**Patrón de Auditoría:**

```java
// En AuthorityService
public Mono<Authority> updateAuthority(Long id, AuthorityDTO dto, ServerWebExchange exchange) {
  return repository
    .findById(id)
    .flatMap(oldAuthority -> {
      // Capturar estado anterior
      String oldValues = toJson(oldAuthority);

      // Aplicar cambios
      updateEntity(oldAuthority, dto);

      // Guardar
      return repository
        .save(oldAuthority)
        .flatMap(newAuthority -> {
          // Capturar estado nuevo
          String newValues = toJson(newAuthority);

          // Crear log de auditoría
          return auditService
            .createAuditLog(newAuthority.getId(), AuditAction.UPDATED, oldValues, newValues, exchange)
            .thenReturn(newAuthority);
        });
    });
}

```

---

### 3.3 Repository Layer (6 componentes)

```mermaid
classDiagram
    class R2dbcRepository~T~ {
        <<interface>>
        +findById(id) Mono~T~
        +findAll() Flux~T~
        +save(entity) Mono~T~
        +delete(entity) Mono~Void~
    }

    class AuthorityRepository {
        +findByCode(code) Mono~Authority~
        +findByCategory(category) Flux~Authority~
        +findAllActive() Flux~Authority~
    }

    class PermissionRepository {
        +findByName(name) Mono~Permission~
        +findByResource(resource) Flux~Permission~
        +findByResourceAndAction(r, a) Mono~Permission~
    }

    class UserAuthorityRepository {
        +findByUserId(userId) Flux~UserAuthority~
        +findValidByUserId(userId) Flux~UserAuthority~
        +findExpiredAssignments() Flux~UserAuthority~
        +findExpiringWithinDays(days) Flux~UserAuthority~
    }

    class AuthorityAuditRepository {
        +findByAuthorityId(id) Flux~AuthorityAudit~
        +searchByFilters(criteria, pageable) Flux~AuthorityAudit~
        +countByFilters(criteria) Mono~Long~
    }

    R2dbcRepository <|-- AuthorityRepository
    R2dbcRepository <|-- PermissionRepository
    R2dbcRepository <|-- UserAuthorityRepository
    R2dbcRepository <|-- AuthorityAuditRepository
```

**Uso de DatabaseClient (queries custom):**

```java
@Repository
public class UserAuthorityRepositoryImpl {

  private final DatabaseClient databaseClient;

  public Flux<UserAuthority> findValidByUserId(Long userId) {
    String sql =
      """
      SELECT * FROM scr_user_authority
      WHERE user_id = :userId
        AND is_active = true
        AND (expires_at IS NULL OR expires_at > NOW())
      ORDER BY assigned_date DESC
      """;

    return databaseClient.sql(sql).bind("userId", userId).map(userAuthorityRowMapper).all();
  }
}

```

---

## 4. Componentes Frontend (React)

```mermaid
graph TD
    subgraph "Redux Store"
        A[authoritySlice<br/>21 async thunks]
        P[permissionSlice<br/>18 async thunks]
        UA[userAuthoritySlice<br/>15 async thunks]
    end

    subgraph "API Services"
        API1[authority.service.ts<br/>8 métodos HTTP]
        API2[permission.service.ts<br/>7 métodos HTTP]
        API3[user-authority.service.ts<br/>9 métodos HTTP]
        API4[dashboard.service.ts<br/>1 método HTTP]
    end

    subgraph "UI Components"
        subgraph "Authority Management"
            C1[authority-list.tsx]
            C2[authority-detail.tsx]
            C3[authority-form.tsx]
            C4[authority-delete-dialog.tsx]
        end

        subgraph "Permission Management"
            C5[permission-list.tsx]
            C6[permission-detail.tsx]
            C7[permission-form.tsx]
        end

        subgraph "User Authority Assignment"
            C8[user-authority-list.tsx]
            C9[assign-authority-dialog.tsx]
            C10[revoke-authority-dialog.tsx]
        end

        subgraph "Dashboard"
            C11[authorization-dashboard.tsx]
            C12[MetricCard.tsx]
            C13[RecentActivityWidget.tsx]
            C14[ExpiringRolesWidget.tsx]
            C15[TopAuthoritiesChart.tsx]
            C16[PermissionUsageChart.tsx]
        end
    end

    C1 & C2 & C3 & C4 --> A
    C5 & C6 & C7 --> P
    C8 & C9 & C10 --> UA
    C11 --> C12 & C13 & C14 & C15 & C16
    C11 --> API4
    A --> API1
    P --> API2
    UA --> API3

    style A fill:#764abc
    style P fill:#764abc
    style UA fill:#764abc
    style API1 fill:#61dafb
    style C11 fill:#ff6b6b
```

---

### 4.1 Redux State Management

**authority.reducer.ts:**

```typescript
interface AuthorityState {
  authorities: IAuthority[]; // Lista de todos los roles
  authority: IAuthority | null; // Rol seleccionado (detalle)
  loading: boolean; // Estado de carga
  error: string | null; // Mensajes de error
  updating: boolean; // Estado de actualización
  deleteSuccess: boolean; // Confirmación de eliminación
}

// 21 Async Thunks:
// - fetchAuthorities, fetchAuthority, createAuthority, updateAuthority
// - deleteAuthority, deactivateAuthority, activateAuthority
// - fetchAuthorityPermissions, assignPermissionToAuthority
// - removePermissionFromAuthority, etc.
```

**userAuthority.reducer.ts:**

```typescript
interface UserAuthorityState {
  userAuthorities: IUserAuthority[]; // Asignaciones del usuario
  validAuthorities: IUserAuthority[]; // Solo asignaciones válidas
  expiringAuthorities: IUserAuthority[]; // Próximas a expirar
  loading: boolean;
  error: string | null;
}

// 15 Async Thunks:
// - fetchUserAuthorities, fetchValidAuthorities, assignAuthority
// - revokeAuthority, fetchExpiringAuthorities, etc.
```

---

### 4.2 Flujo de Datos (Frontend → Backend)

```mermaid
sequenceDiagram
    participant Component
    participant Redux
    participant Thunk
    participant API
    participant Backend

    Component->>Redux: 1. dispatch(fetchAuthorities())
    Redux->>Thunk: 2. Execute async thunk
    Thunk->>Thunk: 3. dispatch(pending)
    Thunk->>API: 4. authorityService.getAll()
    API->>Backend: 5. GET /api/authorities<br/>Authorization: Bearer <JWT>
    Backend-->>API: 6. 200 OK [authorities]
    API-->>Thunk: 7. Response data
    Thunk->>Thunk: 8. dispatch(fulfilled, data)
    Thunk-->>Redux: 9. Update state.authorities
    Redux-->>Component: 10. Re-render with new data
```

---

## 5. Scheduled Jobs Architecture

```mermaid
graph LR
    subgraph "Spring Task Scheduler"
        TS[Task Scheduler<br/>Pool size: 2 threads]
    end

    subgraph "Cleanup Job"
        CJ[ExpiredAuthoritiesCleanupJob<br/>@Scheduled cron: 0 0 2 * * *]
        CJ1[cleanupExpiredAuthorities]
        CJ2[cleanupExpiredPermissions]
        CJ --> CJ1
        CJ --> CJ2
    end

    subgraph "Database Operations"
        Q1[Query expired authorities]
        U1[UPDATE is_active=false]
        Q2[Query expired permissions]
        U2[UPDATE is_active=false]
        CJ1 --> Q1 --> U1
        CJ2 --> Q2 --> U2
    end

    TS -.->|Executes daily 2 AM| CJ
    U1 --> DB[(scr_user_authority)]
    U2 --> DB2[(scr_user_permission)]

    style CJ fill:#ff6b6b
    style TS fill:#6db33f
```

**Configuración:**

```yaml
# application.yml
spring:
  task:
    scheduling:
      thread-name-prefix: tyse-scrutiny-gateway-scheduling-
      pool:
        size: 2 # 2 threads para scheduled tasks
```

**Anotación:**

```java
@Component
public class ExpiredAuthoritiesCleanupJob {

  @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
  @Transactional
  public void cleanupExpiredAuthorities() {
    // Cleanup logic
  }
}

```

---

## 6. Flujo de Datos Reactivo (R2DBC)

```mermaid
graph LR
    subgraph "Application Thread"
        C[Controller<br/>Non-blocking]
        S[Service<br/>Reactive]
    end

    subgraph "R2DBC Driver"
        CP[Connection Pool<br/>max-size: 20]
        D[Database Driver<br/>Non-blocking IO]
    end

    subgraph "PostgreSQL"
        PG[(Database)]
    end

    C -->|Mono/Flux| S
    S -->|Mono/Flux| CP
    CP -->|Acquire connection| D
    D -.->|Async query| PG
    PG -.->|Result stream| D
    D -->|Release connection| CP
    CP -->|Mono/Flux| S
    S -->|Mono/Flux| C

    style C fill:#6db33f
    style S fill:#6db33f
    style CP fill:#ff6b6b
    style PG fill:#336791
```

**Ventajas del stack reactivo:**

- **Non-blocking I/O**: Un thread puede manejar miles de requests
- **Backpressure**: Control de flujo (no overflow de memoria)
- **Composition**: Fácil encadenar operaciones asíncronas
- **Resource efficiency**: Menos threads = menos overhead

**Ejemplo:**

```java
// Traditional (blocking)
public List<Authority> getAll() {
  return repository.findAll(); // Thread bloqueado hasta que BD responde
}

// Reactive (non-blocking)
public Flux<Authority> getAll() {
  return repository.findAll(); // Thread libre, BD procesa async
}

```

---

## 7. Integración con Spring Security

```mermaid
graph TD
    Request[HTTP Request<br/>+ JWT token] --> Gateway[Spring Cloud Gateway]
    Gateway --> Chain[SecurityWebFilterChain]

    subgraph "Security Filters"
        Chain --> F1[JWTFilter<br/>Extract + validate token]
        F1 --> F2[ReactiveAuthenticationManager<br/>Set SecurityContext]
        F2 --> F3[AuthorizationWebFilter<br/>@PreAuthorize evaluation]
    end

    F3 --> Decision{Authorized?}
    Decision -->|Yes| Controller[REST Controller<br/>Execute business logic]
    Decision -->|No| Deny[403 Forbidden]

    Controller --> Response[HTTP Response]
    Deny --> Response

    style Decision fill:#ff6b6b
    style Controller fill:#6db33f
    style Deny fill:#ffcccc
```

**Configuración de Security:**

```java
@Bean
public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
  return http
    .csrf()
    .disable()
    .authorizeExchange()
    .pathMatchers("/api/authorities/**")
    .hasAuthority("ROLE_ADMIN")
    .pathMatchers("/api/permissions/**")
    .hasAuthority("ROLE_ADMIN")
    .anyExchange()
    .authenticated()
    .and()
    .addFilterAt(jwtFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
    .build();
}

```

---

## 8. Caching Strategy (Recomendación)

```mermaid
graph TD
    Request[Request] --> Cache{Cache hit?}
    Cache -->|Yes| Return[Return cached data]
    Cache -->|No| Service[Call service]
    Service --> DB[Query database]
    DB --> Store[Store in cache<br/>TTL: 5 min]
    Store --> Return

    style Cache fill:#ff6b6b
    style Return fill:#ccffcc
```

**Candidatos para caching:**

| Dato                   | TTL    | Invalidación                    |
| ---------------------- | ------ | ------------------------------- |
| Lista de roles activos | 5 min  | Al crear/modificar/eliminar rol |
| Permisos de un rol     | 10 min | Al asignar/remover permiso      |
| Dashboard metrics      | 1 min  | Manual (refresh button)         |

**Implementación con Caffeine:**

```java
@Configuration
public class CacheConfig {

  @Bean
  public Cache<String, List<Authority>> authorityCache() {
    return Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(10_000).recordStats().build();
  }
}

```

---

## 9. Monitoreo y Observabilidad

```mermaid
graph TD
    subgraph "Application"
        App[Spring Boot App]
        Actuator[Spring Boot Actuator<br/>/actuator/metrics]
    end

    subgraph "Metrics Collection"
        Prom[Prometheus<br/>Scrapes metrics every 15s]
        Micrometer[Micrometer Registry<br/>Exports metrics]
    end

    subgraph "Visualization"
        Grafana[Grafana Dashboards<br/>Real-time charts]
    end

    subgraph "Alerting"
        Alert[AlertManager<br/>Sends notifications]
    end

    App --> Actuator
    Actuator --> Micrometer
    Micrometer --> Prom
    Prom --> Grafana
    Prom --> Alert

    style Prom fill:#e6522c
    style Grafana fill:#f46800
```

**Métricas clave:**

- `http.server.requests` (latency, throughput)
- `r2dbc.pool.acquired` (connections in use)
- `authorization.expired.roles.total` (business metric)
- `jvm.memory.used` (heap usage)

---

## 10. Deployment Architecture

```mermaid
graph TB
    subgraph "Load Balancer"
        LB[Nginx / ALB]
    end

    subgraph "Application Tier (Stateless)"
        App1[Gateway Instance 1<br/>:8080]
        App2[Gateway Instance 2<br/>:8080]
        App3[Gateway Instance 3<br/>:8080]
    end

    subgraph "Database Tier"
        Primary[(PostgreSQL Primary<br/>Read/Write)]
        Replica1[(Replica 1<br/>Read only)]
        Replica2[(Replica 2<br/>Read only)]
    end

    subgraph "Monitoring"
        Prometheus[Prometheus]
        Grafana[Grafana]
    end

    LB --> App1 & App2 & App3
    App1 & App2 & App3 -->|Write| Primary
    App1 & App2 & App3 -.->|Read| Replica1 & Replica2
    Primary -.->|Replication| Replica1 & Replica2
    App1 & App2 & App3 --> Prometheus
    Prometheus --> Grafana

    style LB fill:#269bd2
    style Primary fill:#336791
    style Replica1 fill:#336791
    style Replica2 fill:#336791
```

**Características:**

- **Horizontal scaling**: Agregar más instancias de Gateway
- **Stateless**: No hay sesiones en memoria (JWT en cada request)
- **Database replication**: Reads distribuidos, writes al primary
- **No sticky sessions**: Load balancer puede usar round-robin

---

**Fin de los Diagramas de Arquitectura**
