# Guía de Operaciones - Sistema de Autorización Enterprise

**Versión:** 1.0
**Fecha:** 2025-11-07
**Audiencia:** Ingenieros de operaciones (SysOps, DevOps, SRE)

---

## Tabla de Contenidos

1. [Arquitectura del Sistema](#1-arquitectura-del-sistema)
2. [Base de Datos](#2-base-de-datos)
3. [Scheduled Jobs](#3-scheduled-jobs)
4. [Monitoreo y Métricas](#4-monitoreo-y-métricas)
5. [Mantenimiento](#5-mantenimiento)
6. [Backup y Restore](#6-backup-y-restore)
7. [Seguridad](#7-seguridad)
8. [Troubleshooting Avanzado](#8-troubleshooting-avanzado)
9. [Escalabilidad](#9-escalabilidad)

---

## 1. Arquitectura del Sistema

### 1.1 Stack Tecnológico

**Backend:**

- **Spring Boot 3.4.5** con Spring WebFlux (reactive, non-blocking)
- **Spring Security** con JWT authentication
- **R2DBC** (Reactive Relational Database Connectivity) para PostgreSQL
- **Liquibase** para migraciones de base de datos
- **Reactor** (Project Reactor) para programación reactiva

**Frontend:**

- **React 18** con TypeScript
- **Redux Toolkit** para gestión de estado
- **Axios** para llamadas HTTP
- **Recharts** para visualizaciones

**Base de Datos:**

- **PostgreSQL 14+** (requiere soporte de JSON)
- **Connection Pool:** R2DBC connection factory (reactivo)

**Infraestructura:**

- **Consul** para service discovery
- **Kafka** para mensajería (opcional en módulo de autorización)

### 1.2 Patrón de Diseño: RBAC

**Role-Based Access Control** con características enterprise:

- **Roles jerárquicos**: `hierarchy_level` (0=máximo, 999=mínimo)
- **Permisos granulares**: Patrón `resource.action`
- **Temporal assignments**: Roles con `expires_at` automático
- **Direct permissions**: Bypass de roles para casos excepcionales
- **Full audit trail**: Todos los cambios registrados

### 1.3 Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Authority UI │  │ Permission UI│  │  Dashboard   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/REST (JWT)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│               Spring Cloud Gateway                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Spring Security Filter Chain                         │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │  JWTFilter  │→│   @PreAuth  │→│  Controller │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Authority  │  │  Permission  │  │ UserAuthority│     │
│  │   Service    │  │   Service    │  │   Service    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │    Audit     │  │   Dashboard  │  │    Export    │     │
│  │   Service    │  │   Service    │  │   Service    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└───────────────────────────┬─────────────────────────────────┘
                            │ R2DBC (reactive)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                Repository Layer (R2DBC)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Authority  │  │  Permission  │  │ UserAuthority│     │
│  │  Repository  │  │  Repository  │  │  Repository  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL Database                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  scr_authority  │  scr_permission  │  scr_user_*    │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  Scheduled Jobs (Async)                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ExpiredAuthoritiesCleanupJob                         │  │
│  │  Cron: 0 0 2 * * * (Daily 2 AM)                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 1.4 Flujo de Autorización

```
1. Usuario hace login
   ↓
2. Backend valida credenciales
   ↓
3. Backend consulta roles activos y válidos del usuario
   ↓
4. Backend genera JWT con claim 'auth': ["ROLE_USER", "ROLE_MANAGER"]
   ↓
5. Frontend recibe JWT y lo almacena (localStorage/sessionStorage)
   ↓
6. Usuario hace request a recurso protegido
   ↓
7. Frontend envía JWT en header: Authorization: Bearer <token>
   ↓
8. Gateway valida JWT (firma, expiración)
   ↓
9. JWTFilter extrae roles del claim 'auth'
   ↓
10. Spring Security evalúa @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ↓
    ├─ Si tiene el rol → Permite acceso (200 OK)
    └─ Si NO tiene → Niega acceso (403 Forbidden)
```

### 1.5 Componentes Clave

#### REST Controllers (6)

| Controller                     | Ruta Base                      | Endpoints                |
| ------------------------------ | ------------------------------ | ------------------------ |
| AuthorityResource              | `/api/authorities`             | 5 (CRUD + list)          |
| PermissionResource             | `/api/permissions`             | 5 (CRUD + list)          |
| UserAuthorityResource          | `/api/user-authorities`        | 5 (assign, revoke, list) |
| UserPermissionResource         | `/api/user-permissions`        | 4 (grant, revoke, list)  |
| AuthorityPermissionResource    | `/api/authority-permissions`   | 3 (assign, remove, list) |
| AuthorizationDashboardResource | `/api/authorization/dashboard` | 1 (metrics)              |

#### Servicios (8)

1. **AuthorityService**: CRUD de roles + auditoría
2. **PermissionService**: CRUD de permisos
3. **UserAuthorityService**: Asignación/revocación de roles
4. **UserPermissionService**: Grant/revoke de permisos directos
5. **AuthorityPermissionService**: Asignación de permisos a roles
6. **AuthorityAuditService**: Consultas de auditoría
7. **AuditExportService**: Exportación CSV/JSON
8. **AuthorizationDashboardService**: Métricas del dashboard

#### Repositorios (6)

- R2DBC reactivos (retornan `Mono<>` o `Flux<>`)
- Queries custom con `DatabaseClient` (SQL nativo)
- Row mappers manuales para conversión

---

## 2. Base de Datos

### 2.1 Esquema de Tablas

**6 tablas principales con prefijo `scr_`:**

| Tabla                      | Registros típicos | Índices | Descripción                 |
| -------------------------- | ----------------- | ------- | --------------------------- |
| `scr_authority`            | 10-100            | 5       | Roles del sistema           |
| `scr_permission`           | 50-500            | 5       | Permisos granulares         |
| `scr_authority_permission` | 100-1000          | 2       | Junction N:N roles-permisos |
| `scr_user_authority`       | 100-10,000        | 7       | Asignaciones user-rol       |
| `scr_user_permission`      | 10-1,000          | 6       | Permisos directos           |
| `scr_authority_audit`      | 1,000-100,000+    | 5       | Logs de auditoría           |

### 2.2 Diagrama ER

Ver: [`/docs/diagrams/database-er-diagram.md`](../diagrams/database-er-diagram.md)

### 2.3 Índices Críticos

**scr_authority (5 índices):**

```sql
idx_scr_authority_name          -- Búsquedas por nombre
idx_scr_authority_code          -- Búsquedas por código (ej: ROLE_ADMIN)
idx_scr_authority_category      -- Filtros por tipo (SYSTEM, CUSTOM)
idx_scr_authority_is_active     -- Filtros por activos
idx_scr_authority_category_active -- Composite para filtros combinados
```

**scr_user_authority (7 índices):**

```sql
idx_scr_user_authority_user_id     -- Consultar roles de un usuario (HOT PATH)
idx_scr_user_authority_authority_id -- Consultar usuarios de un rol
idx_scr_user_authority_is_active    -- Filtros por activos
idx_scr_user_authority_expires_at   -- Job de cleanup (HOT PATH)
idx_scr_user_authority_expires_at_active -- Composite para alertas de expiración
idx_scr_user_authority_assigned_date -- Ordenamiento por fecha
idx_scr_user_authority_user_authority_unique -- Constraint único
```

**scr_authority_audit (5 índices):**

```sql
idx_scr_authority_audit_authority_id  -- Historial de un rol (HOT PATH)
idx_scr_authority_audit_action        -- Filtros por tipo de acción
idx_scr_authority_audit_changed_by    -- Auditoría por usuario
idx_scr_authority_audit_changed_date  -- Ordenamiento cronológico
idx_scr_authority_audit_authority_date -- Composite para queries comunes (DESC)
```

### 2.4 Mantenimiento de Índices

**Verificar uso de índices:**

```sql
-- Índices no usados (candidatos a eliminar)
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE schemaname = 'public' AND tablename LIKE 'scr_%'
  AND idx_scan = 0
ORDER BY idx_scan ASC;

-- Tamaño de índices
SELECT schemaname, tablename, indexname, pg_size_pretty(pg_relation_size(indexrelid))
FROM pg_stat_user_indexes
WHERE schemaname = 'public' AND tablename LIKE 'scr_%'
ORDER BY pg_relation_size(indexrelid) DESC;
```

**Rebuild de índices (si hay fragmentación):**

```sql
REINDEX TABLE scr_authority_audit;
VACUUM ANALYZE scr_authority_audit;
```

### 2.5 Particionamiento (Recomendación futura)

Si `scr_authority_audit` crece > 10M registros, considerar particionamiento por fecha:

```sql
-- Ejemplo: Particionamiento mensual
CREATE TABLE scr_authority_audit_y2025m01 PARTITION OF scr_authority_audit
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE scr_authority_audit_y2025m02 PARTITION OF scr_authority_audit
FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- ... etc
```

**Beneficios:**

- Queries más rápidas (solo escanean partición relevante)
- Eliminación de datos antiguos más eficiente (DROP PARTITION)
- Maintenance independiente por partición

---

## 3. Scheduled Jobs

### 3.1 ExpiredAuthoritiesCleanupJob

**Propósito:** Marcar como inactivas las asignaciones de roles y permisos directos que ya expiraron.

**Configuración:**

```java
@Scheduled(cron = "0 0 2 * * *")  // Diario a las 2 AM
@Transactional
public void cleanupExpiredAuthorities() { ... }
```

**Qué hace:**

1. Busca `scr_user_authority` con `expires_at <= NOW()` y `is_active=true`
2. Las marca como `is_active=false`
3. Loguea cantidad de registros actualizados
4. Repite para `scr_user_permission`

**Query ejecutada (aproximada):**

```sql
UPDATE scr_user_authority
SET is_active = false
WHERE expires_at <= NOW() AND is_active = true;

UPDATE scr_user_permission
SET is_active = false
WHERE expires_at <= NOW() AND is_active = true;
```

**Logs esperados:**

```
2025-11-07 02:00:01 INFO  c.t.s.g.s.s.ExpiredAuthoritiesCleanupJob - Starting cleanup of expired authorities
2025-11-07 02:00:02 INFO  c.t.s.g.s.s.ExpiredAuthoritiesCleanupJob - Cleaned up 5 expired authority assignments
2025-11-07 02:00:02 INFO  c.t.s.g.s.s.ExpiredAuthoritiesCleanupJob - Cleaned up 2 expired permission grants
2025-11-07 02:00:02 INFO  c.t.s.g.s.s.ExpiredAuthoritiesCleanupJob - Cleanup completed successfully
```

**Impacto:**

- **Performance:** Bajo (updates indexados)
- **Lock time:** Mínimo (R2DBC non-blocking)
- **Frecuencia:** 1 vez/día
- **Ventana de ejecución:** 2 AM (horario de bajo tráfico)

**Configuración en `application.yml`:**

```yaml
spring:
  task:
    scheduling:
      thread-name-prefix: tyse-scrutiny-gateway-scheduling-
      pool:
        size: 2 # Pool para scheduled tasks
```

### 3.2 Monitoreo del Job

**Health check:**

```bash
# Verificar última ejecución
grep "ExpiredAuthoritiesCleanupJob" logs/spring.log | tail -5

# Verificar que se ejecutó en las últimas 24 horas
if ! grep "ExpiredAuthoritiesCleanupJob.*Cleanup completed" logs/spring.log | grep "$(date +%Y-%m-%d)"; then
  echo "ERROR: Job no se ejecutó hoy"
  # Alertar a operaciones
fi
```

**Métricas a monitorear:**

- Tiempo de ejecución (debería ser < 5 segundos con < 1000 registros)
- Cantidad de registros procesados (alertar si > 1000)
- Errores de transacción

**Alerta recomendada (Prometheus/Grafana):**

```yaml
- alert: ExpiredAuthoritiesJobNotExecuted
  expr: |
    time() - scheduled_job_last_execution_timestamp{job="expired_authorities_cleanup"} > 90000
  for: 5m
  annotations:
    summary: 'Scheduled job no se ejecutó en las últimas 25 horas'
```

### 3.3 Desactivar el Job (si es necesario)

**Opción 1: Via profile:**

```yaml
# application-prod.yml
spring:
  task:
    scheduling:
      enabled: false # Desactiva TODOS los jobs
```

**Opción 2: Via código (comentar @Scheduled):**

```java
// @Scheduled(cron = "0 0 2 * * *")  // Comentar esta línea
public void cleanupExpiredAuthorities() { ... }
```

**Opción 3: Ejecutar manualmente:**

```bash
# Si el job está desactivado, ejecutar cleanup manual con SQL
psql -U postgres -d tysescrutinygateway -c "
UPDATE scr_user_authority SET is_active = false WHERE expires_at <= NOW() AND is_active = true;
UPDATE scr_user_permission SET is_active = false WHERE expires_at <= NOW() AND is_active = true;
"
```

---

## 4. Monitoreo y Métricas

### 4.1 Métricas de Negocio

**Endpoint:** `GET /api/authorization/dashboard/metrics`

**Métricas clave:**
| Métrica | Descripción | Umbral de Alerta |
|---------|-------------|------------------|
| `totalAuthorities` | Cantidad de roles | Alertar si > 1000 (posible explosión de roles custom) |
| `totalPermissions` | Cantidad de permisos | Alertar si > 5000 |
| `activeUsers` | Usuarios con roles válidos | Monitorear tendencia |
| `expiredRoles` | Roles expirados pero aún is_active=true | Alertar si > 100 (job no funcionando) |

**Query para Prometheus (ejemplo):**

```promql
# Cantidad de roles expirados sin limpiar
authorization_expired_roles_total > 100
```

### 4.2 Métricas de Performance

**Spring Boot Actuator:**

Habilitar endpoints en `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Métricas HTTP:**

- `http.server.requests` (latency de endpoints de autorización)
- Filtrar por tag: `uri=/api/authorities/**`

**Métricas de DB:**

- `r2dbc.pool.acquired` (conexiones en uso)
- `r2dbc.pool.idle` (conexiones disponibles)
- `r2dbc.pool.pending` (requests esperando conexión)

**Queries lentas a monitorear:**

```sql
-- Habilitar logging de queries lentas en postgresql.conf
log_min_duration_statement = 1000  # Loguear queries > 1 segundo

-- Consultar queries lentas desde logs
SELECT * FROM pg_stat_statements
WHERE query LIKE '%scr_%'
ORDER BY mean_exec_time DESC
LIMIT 10;
```

### 4.3 Alertas Recomendadas

**Prometheus Alert Rules:**

```yaml
groups:
  - name: authorization
    rules:
      - alert: HighExpiredRolesCount
        expr: authorization_expired_roles_total > 100
        for: 1h
        annotations:
          summary: 'Demasiados roles expirados sin limpiar (> 100)'
          description: 'El job de cleanup puede no estar funcionando'

      - alert: AuthorizationAPISlowResponse
        expr: histogram_quantile(0.95, http_server_requests_seconds_bucket{uri=~"/api/author.*"}) > 2
        for: 5m
        annotations:
          summary: 'API de autorización respondiendo lento (p95 > 2s)'

      - alert: AuditTableGrowing
        expr: authorization_audit_records_total > 1000000
        annotations:
          summary: 'Tabla de auditoría tiene > 1M registros, considerar limpieza'
```

### 4.4 Logs Importantes

**Ubicación:** `logs/spring.log`

**Eventos a monitorear:**

```bash
# Creación/modificación de roles
grep "AuthorityService" logs/spring.log | grep -E "created|updated|deleted"

# Errores de autorización
grep "ERROR" logs/spring.log | grep -E "Authority|Permission|Access"

# Scheduled job execution
grep "ExpiredAuthoritiesCleanupJob" logs/spring.log

# Slow queries (si r2dbc logging está habilitado)
grep "Executing SQL" logs/spring.log | grep -oP "took \K\d+(?= ms)" | awk '$1 > 1000'
```

**Configurar log rotation:**

```yaml
# logback-spring.xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
<file>logs/spring.log</file>
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
<fileNamePattern>logs/spring.%d{yyyy-MM-dd}.log.gz</fileNamePattern>
<maxHistory>30</maxHistory>  <!-- Mantener 30 días -->
<totalSizeCap>10GB</totalSizeCap>  <!-- Límite total de logs -->
</rollingPolicy>
</appender>
```

---

## 5. Mantenimiento

### 5.1 Limpieza de Auditoría Antigua

**Política recomendada:** Mantener auditoría de los últimos 12 meses.

**Procedimiento:**

1. **Backup antes de eliminar:**

```sql
-- Crear tabla de backup
CREATE TABLE scr_authority_audit_archive_2024 AS
SELECT * FROM scr_authority_audit
WHERE changed_date < '2024-01-01';

-- Verificar cantidad
SELECT COUNT(*) FROM scr_authority_audit_archive_2024;
```

2. **Exportar a archivo (opcional):**

```bash
pg_dump -U postgres -d tysescrutinygateway \
  -t scr_authority_audit_archive_2024 \
  --data-only \
  -f audit_archive_2024.sql
```

3. **Eliminar registros antiguos:**

```sql
-- Eliminar en batches para evitar lock prolongado
DO $$
DECLARE
  batch_size INT := 10000;
  deleted INT;
BEGIN
  LOOP
    DELETE FROM scr_authority_audit
    WHERE id IN (
      SELECT id FROM scr_authority_audit
      WHERE changed_date < NOW() - INTERVAL '12 months'
      LIMIT batch_size
    );

    GET DIAGNOSTICS deleted = ROW_COUNT;
    EXIT WHEN deleted = 0;

    RAISE NOTICE 'Deleted % rows', deleted;
    PERFORM pg_sleep(0.1);  -- Pausa entre batches
  END LOOP;
END $$;
```

4. **Vacuum para recuperar espacio:**

```sql
VACUUM FULL scr_authority_audit;
ANALYZE scr_authority_audit;
```

**Automatizar con cron:**

```bash
# /etc/cron.monthly/cleanup_audit.sh
#!/bin/bash
psql -U postgres -d tysescrutinygateway << EOF
  DELETE FROM scr_authority_audit
  WHERE changed_date < NOW() - INTERVAL '12 months';
  VACUUM ANALYZE scr_authority_audit;
EOF
```

### 5.2 Análisis de Roles No Utilizados

**Identificar roles sin usuarios asignados:**

```sql
SELECT a.id, a.name, a.code, a.category
FROM scr_authority a
LEFT JOIN scr_user_authority ua ON a.id = ua.authority_id AND ua.is_active = true
WHERE a.category = 'CUSTOM' AND a.is_active = true
GROUP BY a.id
HAVING COUNT(ua.id) = 0;
```

**Acción recomendada:**

- Desactivar roles sin uso en los últimos 6 meses
- Documentar razón antes de eliminar

### 5.3 Detección de Permisos Huérfanos

**Permisos no asignados a ningún rol ni usuario:**

```sql
SELECT p.id, p.name, p.resource, p.action
FROM scr_permission p
WHERE p.is_active = true
  AND NOT EXISTS (
    SELECT 1 FROM scr_authority_permission ap WHERE ap.permission_id = p.id
  )
  AND NOT EXISTS (
    SELECT 1 FROM scr_user_permission up WHERE up.permission_id = p.id AND up.is_active = true
  );
```

**Acción recomendada:**

- Revisar si son permisos legacy
- Desactivar o eliminar si no se usan

### 5.4 Vacuum y Analyze Regular

**Programar en cron:**

```bash
# /etc/cron.weekly/vacuum_authorization_tables.sh
#!/bin/bash
psql -U postgres -d tysescrutinygateway << EOF
  VACUUM ANALYZE scr_authority;
  VACUUM ANALYZE scr_permission;
  VACUUM ANALYZE scr_user_authority;
  VACUUM ANALYZE scr_authority_audit;
EOF
```

---

## 6. Backup y Restore

### 6.1 Backup de Tablas de Autorización

**Backup completo (schema + data):**

```bash
pg_dump -U postgres -d tysescrutinygateway \
  -t scr_authority \
  -t scr_permission \
  -t scr_authority_permission \
  -t scr_user_authority \
  -t scr_user_permission \
  -t scr_authority_audit \
  -f backup_authorization_$(date +%Y%m%d).sql
```

**Backup solo datos (CSV):**

```bash
psql -U postgres -d tysescrutinygateway << EOF
  \copy scr_authority TO 'scr_authority.csv' CSV HEADER;
  \copy scr_permission TO 'scr_permission.csv' CSV HEADER;
  \copy scr_authority_permission TO 'scr_authority_permission.csv' CSV HEADER;
EOF
```

**Comprimir backup:**

```bash
gzip backup_authorization_$(date +%Y%m%d).sql
```

### 6.2 Restore desde Backup

**Restore completo:**

```bash
# PRECAUCIÓN: Esto reemplaza datos existentes
psql -U postgres -d tysescrutinygateway -f backup_authorization_20251107.sql
```

**Restore incremental (solo roles custom):**

```sql
-- Backup de roles actuales primero
CREATE TABLE scr_authority_before_restore AS SELECT * FROM scr_authority;

-- Restaurar solo roles CUSTOM desde backup
COPY scr_authority (id, name, code, description, category, is_system, is_active, hierarchy_level, created_by, created_date)
FROM '/path/to/scr_authority.csv' CSV HEADER
WHERE category = 'CUSTOM';
```

### 6.3 Estrategia de Backup Recomendada

**Niveles de backup:**

1. **Backup diario automatizado (retención: 7 días):**

```bash
# Cron: 0 3 * * * (3 AM diario)
pg_dump -U postgres -d tysescrutinygateway -t "scr_*" | gzip > /backups/daily/auth_$(date +%Y%m%d).sql.gz
```

2. **Backup semanal (retención: 4 semanas):**

```bash
# Cron: 0 4 * * 0 (4 AM domingos)
cp /backups/daily/auth_$(date +%Y%m%d).sql.gz /backups/weekly/
```

3. **Backup mensual (retención: 12 meses):**

```bash
# Cron: 0 5 1 * * (5 AM primer día del mes)
cp /backups/daily/auth_$(date +%Y%m%d).sql.gz /backups/monthly/
```

**Almacenamiento remoto:**

```bash
# Upload a S3 / Google Cloud Storage
aws s3 cp backup_authorization_$(date +%Y%m%d).sql.gz s3://backups-tyse/authorization/
```

### 6.4 Testing de Restore

**Periodicidad:** Mensual

**Procedimiento:**

1. Crear DB temporal: `tysescrutinygateway_test`
2. Restore backup: `psql -d tysescrutinygateway_test -f backup.sql`
3. Verificar integridad:

```sql
SELECT COUNT(*) FROM scr_authority;
SELECT COUNT(*) FROM scr_user_authority WHERE is_active = true;
-- Comparar con producción
```

4. Eliminar DB temporal

---

## 7. Seguridad

### 7.1 Hardening de Base de Datos

**Permisos mínimos:**

```sql
-- Usuario de aplicación (solo DML, no DDL)
CREATE USER tyse_app WITH PASSWORD 'strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO tyse_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO tyse_app;

-- Usuario de lectura (para reportes)
CREATE USER tyse_readonly WITH PASSWORD 'strong_password';
GRANT SELECT ON ALL TABLES IN SCHEMA public TO tyse_readonly;
```

**Evitar usar usuario postgres en producción.**

### 7.2 Rotación de Secrets

**JWT Secret:**

- Ubicación: `application-prod.yml` → `jhipster.security.authentication.jwt.base64-secret`
- Recomendación: Rotar cada 6 meses
- Impacto: Invalida todos los tokens existentes (usuarios deben re-login)

**Procedimiento de rotación:**

1. Generar nuevo secret: `openssl rand -base64 64`
2. Actualizar `application-prod.yml`
3. Restart aplicación (downtime: ~30 segundos)
4. Usuarios deben hacer logout/login

**DB Password:**

- Ubicación: `application-prod.yml` → `spring.r2dbc.password`
- Recomendación: Rotar cada 3 meses
- Impacto: Aplicación no puede conectar a BD (requiere restart)

**Procedimiento de rotación:**

1. Cambiar password en PostgreSQL: `ALTER USER tyse_app PASSWORD 'new_password';`
2. Actualizar `application-prod.yml`
3. Restart aplicación

### 7.3 Auditoría de Accesos

**Registrar todos los accesos admin:**

```sql
-- Query para auditar accesos a endpoints de autorización
SELECT changed_by, ip_address, COUNT(*) as access_count
FROM scr_authority_audit
WHERE changed_date > NOW() - INTERVAL '30 days'
GROUP BY changed_by, ip_address
ORDER BY access_count DESC;
```

**Alertar accesos sospechosos:**

```sql
-- IPs con muchas modificaciones en corto tiempo
SELECT ip_address, changed_by, COUNT(*) as changes,
       MAX(changed_date) as last_change
FROM scr_authority_audit
WHERE changed_date > NOW() - INTERVAL '1 hour'
GROUP BY ip_address, changed_by
HAVING COUNT(*) > 50;  -- Más de 50 cambios/hora
```

### 7.4 Principio de Mínimo Privilegio

**Revisar periódicamente:**

```sql
-- Usuarios con múltiples roles (potencial sobre-privilegio)
SELECT u.login, COUNT(ua.authority_id) as role_count,
       STRING_AGG(a.code, ', ') as roles
FROM scr_user u
JOIN scr_user_authority ua ON u.id = ua.user_id
JOIN scr_authority a ON ua.authority_id = a.id
WHERE ua.is_active = true
GROUP BY u.login
HAVING COUNT(ua.authority_id) > 3
ORDER BY role_count DESC;
```

**Acción:** Consolidar roles si un usuario tiene > 3 roles.

### 7.5 Compliance (GDPR, SOX, etc.)

**Retención de auditoría:**

- GDPR: Mínimo 3 meses, máximo necesario para propósito
- SOX: Mínimo 7 años para cambios en autorizaciones financieras

**Right to be forgotten:**

```sql
-- Anonimizar auditoría de un usuario (GDPR)
UPDATE scr_authority_audit
SET changed_by = 'anonymized_user_' || MD5(changed_by),
    ip_address = '0.0.0.0',
    user_agent = 'anonymized'
WHERE changed_by = 'user_to_delete';
```

---

## 8. Troubleshooting Avanzado

### 8.1 Deadlocks en PostgreSQL

**Síntoma:** Transacciones bloqueadas en `scr_user_authority` o `scr_authority_audit`.

**Diagnóstico:**

```sql
-- Ver locks activos
SELECT locktype, relation::regclass, mode, transactionid, pid, granted
FROM pg_locks
WHERE NOT granted
ORDER BY pid;

-- Ver queries esperando locks
SELECT pid, usename, pg_blocking_pids(pid) as blocked_by, query
FROM pg_stat_activity
WHERE cardinality(pg_blocking_pids(pid)) > 0;
```

**Solución:**

```sql
-- Terminar proceso bloqueante (CUIDADO: puede causar rollback)
SELECT pg_terminate_backend(PID_BLOQUEANTE);
```

**Prevención:**

- Mantener transacciones cortas
- Evitar modificaciones masivas sin batches
- Usar índices correctos (reduce contention)

### 8.2 Connection Pool Exhausted

**Síntoma:** `TimeoutException: Could not acquire connection from pool`

**Diagnóstico:**

```sql
-- Ver conexiones activas
SELECT COUNT(*), state FROM pg_stat_activity
WHERE datname = 'tysescrutinygateway'
GROUP BY state;
```

**Configuración actual (R2DBC):**

```yaml
spring:
  r2dbc:
    pool:
      initial-size: 10
      max-size: 20 # Aumentar si es necesario
      max-idle-time: 30m
```

**Solución:**

1. Aumentar `max-size` en `application.yml`
2. Verificar que no hay connection leaks (use try-with-resources en Mono/Flux)
3. Restart aplicación

### 8.3 High CPU Usage

**Causa común:** Queries sin índices en tablas grandes.

**Diagnóstico:**

```sql
-- Queries más costosas (requiere pg_stat_statements)
SELECT query, calls, total_exec_time, mean_exec_time, rows
FROM pg_stat_statements
WHERE query LIKE '%scr_%'
ORDER BY total_exec_time DESC
LIMIT 10;

-- Escaneos secuenciales (no usan índice)
SELECT schemaname, tablename, seq_scan, seq_tup_read, idx_scan
FROM pg_stat_user_tables
WHERE schemaname = 'public' AND tablename LIKE 'scr_%'
  AND seq_scan > idx_scan  -- Más seq scans que index scans
ORDER BY seq_tup_read DESC;
```

**Solución:**

1. Identificar query sin índice
2. Crear índice apropiado
3. Reindex tablas existentes

### 8.4 Memory Leak (Java Heap)

**Síntoma:** OOM (Out of Memory) después de varias horas.

**Diagnóstico:**

```bash
# Activar heap dump on OOM
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof -jar app.jar

# Analizar heap dump con VisualVM o Eclipse MAT
```

**Causas comunes:**

- Flux/Mono no disposed (memory leak reactivo)
- Cache sin límite de tamaño
- Auditoría acumulando objetos en memoria

**Solución temporal:**

```bash
# Restart aplicación con más memoria
java -Xmx2G -Xms1G -jar app.jar
```

---

## 9. Escalabilidad

### 9.1 Escalamiento Horizontal

**Consideraciones:**

**Stateless:** La aplicación es stateless (no hay sesiones en memoria), se puede escalar horizontalmente sin problemas.

**Load Balancer:**

```nginx
# nginx.conf
upstream tyse_gateway {
  server gateway-1:8080;
  server gateway-2:8080;
  server gateway-3:8080;
}

server {
  location /api/ {
    proxy_pass http://tyse_gateway;
  }
}
```

**Sticky Sessions:** NO necesarias (JWT en cada request).

### 9.2 Escalamiento de Base de Datos

**Read Replicas:**

- Consultas de auditoría (read-heavy) → Read replica
- Operaciones de escritura → Primary

**Configuración (R2DBC):**

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://primary:5432/tysescrutinygateway
    # Para reads, configurar datasource secundario
    read-replica:
      url: r2dbc:postgresql://replica:5432/tysescrutinygateway
```

**Connection Pooling:**

- HikariCP no es compatible con R2DBC (usa r2dbc-pool)
- Configurar max-size según: `(core_count * 2) + effective_spindle_count`

### 9.3 Caching

**Candidatos para caching:**

- Roles activos de un usuario (TTL: 5 minutos)
- Permisos de un rol (TTL: 10 minutos)
- Dashboard metrics (TTL: 1 minuto)

**Implementación con Caffeine:**

```java
@Configuration
public class CacheConfig {

  @Bean
  public Cache<String, List<IAuthority>> authorityCache() {
    return Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(10_000).build();
  }
}

```

**Invalidación:**

- Al crear/modificar/eliminar rol → invalidar cache de ese rol
- Al asignar/revocar rol a usuario → invalidar cache del usuario

### 9.4 Límites de Escala

**Límites actuales (sin optimización):**

- **Usuarios:** 100,000+
- **Roles:** 1,000
- **Permisos:** 10,000
- **Asignaciones:** 1,000,000+
- **Auditoría:** 100,000,000+ (con particionamiento)

**Bottlenecks potenciales:**

- Tabla `scr_authority_audit` creciendo sin control
- Queries de dashboard sin cache
- Scheduled job procesando miles de registros

**Mitigaciones:**

- Particionamiento de auditoría
- Cache en dashboard
- Job en paralelo (multiple threads)

---

## Apéndice A: Comandos Útiles

### PostgreSQL

```bash
# Conectar a BD
psql -U postgres -d tysescrutinygateway

# Backup
pg_dump -U postgres tysescrutinygateway > backup.sql

# Restore
psql -U postgres tysescrutinygateway < backup.sql

# Ver tamaño de tablas
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables
WHERE schemaname = 'public' AND tablename LIKE 'scr_%'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Docker

```bash
# Ver logs del container
docker logs -f tyse-gateway

# Entrar al container
docker exec -it tyse-gateway bash

# Restart container
docker restart tyse-gateway
```

### Aplicación

```bash
# Ver threads activos
jstack PID | grep "scheduling"

# Ver memoria usada
jmap -heap PID

# Ver CPU por thread
top -H -p PID
```

---

## Apéndice B: Checklist de Deploy

- [ ] Backup de BD antes de deploy
- [ ] Verificar migraciones de Liquibase (dry-run)
- [ ] Verificar secrets (JWT, DB password)
- [ ] Smoke test de endpoints críticos
- [ ] Verificar scheduled job ejecutándose (revisar logs post-deploy)
- [ ] Verificar métricas de dashboard
- [ ] Rollback plan documentado

---

**Fin de la Guía de Operaciones**
