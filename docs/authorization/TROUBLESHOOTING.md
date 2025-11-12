# Troubleshooting - Sistema de Autorización Enterprise

**Versión:** 1.0
**Fecha:** 2025-11-07
**Audiencia:** Administradores y equipo de soporte

---

## Tabla de Contenidos

1. [Problemas de Acceso](#problemas-de-acceso)
2. [Problemas de Roles y Permisos](#problemas-de-roles-y-permisos)
3. [Problemas de Scheduled Jobs](#problemas-de-scheduled-jobs)
4. [Problemas de Rendimiento](#problemas-de-rendimiento)
5. [Errores Comunes de API](#errores-comunes-de-api)
6. [Problemas de Dashboard](#problemas-de-dashboard)

---

## Problemas de Acceso

### Problema 1: "Access Denied" pero el usuario tiene el rol correcto

**Síntomas:**

- Usuario recibe error 403 Forbidden
- En BD, el usuario tiene el rol asignado con `is_active=true`
- El rol tiene el permiso necesario

**Causa más común:**
Token JWT desactualizado que no incluye el nuevo rol asignado.

**Diagnóstico:**

1. **Verificar token JWT del usuario:**

```bash
# Pedir al usuario que copie su token JWT (del localStorage o sessionStorage)
# Decodificar en https://jwt.io o con comando:
echo "TOKEN_AQUI" | cut -d. -f2 | base64 -d | jq .
```

2. **Verificar claim 'auth' del token:**

```json
{
  "sub": "user123",
  "auth": ["ROLE_USER"], // ¿Incluye el rol necesario?
  "exp": 1699372800
}
```

**Solución:**

1. **Refrescar token:**

   - Usuario debe hacer logout y login nuevamente
   - O implementar refresh token endpoint (si existe)

2. **Verificar que el nuevo token incluya el rol:**
   - Decodificar el nuevo token
   - Confirmar que `auth` array contiene el rol esperado

**Prevención:**

- Implementar refresh token automático antes de expiración
- Documentar que cambios en roles requieren re-login
- Considerar invalidación de tokens existentes al revocar roles

---

### Problema 2: Usuario no puede hacer login después de asignarle un rol

**Síntomas:**

- Login falla con error 401 Unauthorized
- El usuario existe en la BD
- Recientemente se le asignó un nuevo rol

**Causas posibles:**

#### Causa A: El usuario está desactivado

**Diagnóstico:**

```sql
SELECT id, login, activated FROM scr_user WHERE login = 'username';
```

**Solución:**

```sql
UPDATE scr_user SET activated = true WHERE login = 'username';
```

O desde UI:

- Ir a `/admin/user-management`
- Buscar usuario
- Editar → Marcar "Activated" → Guardar

#### Causa B: El usuario no tiene ningún rol válido

**Diagnóstico:**

```sql
SELECT ua.*, a.code FROM scr_user_authority ua
JOIN scr_authority a ON ua.authority_id = a.id
WHERE ua.user_id = USER_ID;
```

**Solución:**
Asignar al menos un rol base (ej: ROLE_USER) desde `/admin/user-management/{userId}`.

#### Causa C: Problema de credenciales (no relacionado con roles)

**Diagnóstico:**

- Revisar logs de Spring Security:

```bash
grep "Authentication attempt" logs/spring.log | grep "username"
```

**Solución:**

- Resetear contraseña del usuario

---

### Problema 3: Usuario perdió acceso repentinamente

**Síntomas:**

- Usuario tenía acceso ayer, hoy ya no
- No se hicieron cambios manuales

**Causas posibles:**

#### Causa A: Rol temporal expiró

**Diagnóstico:**

```sql
SELECT ua.*, a.name, ua.expires_at
FROM scr_user_authority ua
JOIN scr_authority a ON ua.authority_id = a.id
WHERE ua.user_id = USER_ID AND ua.expires_at <= NOW();
```

**Solución:**

- Verificar si la expiración fue intencional
- Si necesita extender acceso:
  1. Revocar la asignación expirada (si is_active=true)
  2. Asignar nuevamente el rol con nueva fecha de expiración

#### Causa B: Rol fue desactivado por otro admin

**Diagnóstico:**

```sql
SELECT a.*, a.is_active FROM scr_authority a
JOIN scr_user_authority ua ON a.id = ua.authority_id
WHERE ua.user_id = USER_ID;
```

**Auditoría:**

```sql
SELECT * FROM scr_authority_audit
WHERE authority_id = AUTHORITY_ID AND action = 'DEACTIVATED'
ORDER BY changed_date DESC LIMIT 1;
```

**Solución:**

- Contactar al admin que desactivó el rol (ver `changed_by`)
- Reactivar el rol si fue error
- Asignar rol alternativo si fue intencional

#### Causa C: Permiso fue removido del rol

**Diagnóstico:**

```sql
SELECT * FROM scr_authority_audit
WHERE authority_id = AUTHORITY_ID AND action = 'PERMISSIONS_REMOVED'
ORDER BY changed_date DESC LIMIT 5;
```

**Solución:**

- Verificar qué permiso fue removido (ver `new_values` JSON)
- Re-asignar permiso al rol si fue error
- O asignar permiso directo al usuario como workaround temporal

---

## Problemas de Roles y Permisos

### Problema 4: No puedo eliminar un rol

**Síntomas:**

- Click en botón "Delete" de un rol
- Error: "Cannot delete authority"

**Causas posibles:**

#### Causa A: El rol es de sistema (is_system=true)

**Identificación:**

- Badge amarillo "System" en la UI
- Roles: ROLE_ADMIN, ROLE_USER

**Solución:**

- Los roles de sistema **no pueden eliminarse** (by design)
- Si necesita un rol similar personalizado:
  1. Crear rol CUSTOM nuevo
  2. Copiar permisos del rol de sistema
  3. Asignar el nuevo rol a usuarios

#### Causa B: El rol tiene usuarios asignados

**Diagnóstico:**

```sql
SELECT COUNT(*) FROM scr_user_authority
WHERE authority_id = AUTHORITY_ID AND is_active = true;
```

**Solución:**

1. **Opción A: Revocar todas las asignaciones primero**

   - Ir a cada usuario con el rol
   - Revocar asignación con razón
   - Luego eliminar el rol

2. **Opción B: Desactivar en lugar de eliminar (recomendado)**
   - Editar rol
   - Desmarcar "Active"
   - Guardar
   - Efecto: usuarios pierden acceso inmediatamente, pero rol queda en BD

#### Causa C: El rol tiene permisos asignados

**Diagnóstico:**

```sql
SELECT COUNT(*) FROM scr_authority_permission
WHERE authority_id = AUTHORITY_ID;
```

**Solución:**

1. Remover todos los permisos del rol primero
2. Luego eliminar el rol

O directamente desactivar el rol (soft delete).

---

### Problema 5: Error al crear permiso: "Permission already exists"

**Síntomas:**

- Formulario de creación de permiso
- Error: "A permission with this resource and action already exists"

**Causa:**
Constraint único en BD: `resource + action` debe ser único.

**Diagnóstico:**

```sql
SELECT * FROM scr_permission
WHERE resource = 'recurso' AND action = 'accion';
```

**Soluciones:**

#### Opción A: El permiso existe pero está inactivo

**Solución:**

- Ir a `/admin/permission`
- Filtrar por "Inactive"
- Buscar el permiso
- Reactivarlo (editar → marcar "Active" → guardar)

#### Opción B: El permiso existe y está activo

**Solución:**

- No crear duplicado
- Usar el permiso existente
- Si la descripción es incorrecta, editarla

#### Opción C: Necesita un permiso similar pero diferente

**Solución:**
Cambiar el resource o action para hacerlo único:

- En lugar de `report.export`, usar `report.export_pdf`
- O usar resource más específico: `financial_report.export`

---

### Problema 6: Cambié permisos de un rol pero los usuarios no ven el cambio

**Síntomas:**

- Agregaste permiso a ROLE_MANAGER
- Usuarios con ROLE_MANAGER siguen sin poder usar el recurso

**Causa:**
Token JWT no se actualiza automáticamente.

**Diagnóstico:**

1. **Verificar que el permiso fue asignado correctamente:**

```sql
SELECT p.name FROM scr_permission p
JOIN scr_authority_permission ap ON p.id = ap.permission_id
WHERE ap.authority_id = AUTHORITY_ID;
```

2. **Verificar backend:**

- Endpoint tiene `@PreAuthorize` con permiso correcto
- Spelling del permiso coincide exactamente

**Solución:**

1. **Pedir a usuarios que hagan logout/login**

   - Esto regenera el token JWT con permisos actualizados

2. **Verificar con consulta SQL que tienen acceso:**

```sql
-- Permisos efectivos del usuario (roles + directos)
SELECT DISTINCT p.name FROM scr_permission p
LEFT JOIN scr_authority_permission ap ON p.id = ap.permission_id
LEFT JOIN scr_user_authority ua ON ap.authority_id = ua.authority_id
WHERE ua.user_id = USER_ID AND ua.is_active = true
  AND (ua.expires_at IS NULL OR ua.expires_at > NOW())
UNION
SELECT p.name FROM scr_permission p
JOIN scr_user_permission up ON p.id = up.permission_id
WHERE up.user_id = USER_ID AND up.is_active = true
  AND (up.expires_at IS NULL OR up.expires_at > NOW());
```

---

## Problemas de Scheduled Jobs

### Problema 7: Scheduled job de cleanup no se ejecuta

**Síntomas:**

- Roles expirados siguen con `is_active=true` después de varios días
- Widget "Expired Roles" en dashboard muestra muchas alertas antiguas
- Logs no muestran ejecución del job

**Diagnóstico:**

**1. Verificar configuración de scheduling:**

```yaml
# Revisar application.yml
spring:
  task:
    scheduling:
      thread-name-prefix: tyse-scrutiny-gateway-scheduling-
      pool:
        size: 2
```

**2. Verificar que scheduling esté habilitado:**

```bash
# Buscar en logs de inicio:
grep "Scheduling" logs/spring.log

# Debería aparecer:
# "Enabling scheduled jobs..."
```

**3. Verificar profile activo:**

```bash
# El job puede estar deshabilitado en ciertos profiles
grep "spring.profiles.active" logs/spring.log
```

**Solución:**

#### Si scheduling está deshabilitado:

**application.yml:**

```yaml
spring:
  task:
    scheduling:
      enabled: true # Asegurar que esté en true (o quitar línea, es true por defecto)
```

Reiniciar aplicación.

#### Si scheduling está habilitado pero no se ejecuta:

**1. Verificar anotación en código:**

```java
// En ExpiredAuthoritiesCleanupJob.java
@Scheduled(cron = "0 0 2 * * *")  // Debe tener esta anotación
public void cleanupExpiredAuthorities() { ... }
```

**2. Verificar que la clase esté registrada como @Component:**

```java
@Component  // Debe tener esta anotación
public class ExpiredAuthoritiesCleanupJob { ... }
```

**3. Ejecutar cleanup manual (workaround):**

```sql
-- Cleanup de roles expirados
UPDATE scr_user_authority
SET is_active = false,
    revoked_by = 'cleanup_manual',
    revoked_date = NOW(),
    revoked_reason = 'Cleanup manual - scheduled job no ejecutándose'
WHERE expires_at <= NOW() AND is_active = true;

-- Cleanup de permisos directos expirados
UPDATE scr_user_permission
SET is_active = false,
    revoked_by = 'cleanup_manual',
    revoked_date = NOW(),
    revoked_reason = 'Cleanup manual - scheduled job no ejecutándose'
WHERE expires_at <= NOW() AND is_active = true;
```

**4. Verificar zona horaria del servidor:**

```bash
# El cron "0 0 2 * * *" se ejecuta a las 2 AM en timezone del servidor
date
# Si el servidor está en UTC y necesitas 2 AM hora local, ajustar cron
```

**5. Monitorear próxima ejecución:**

```bash
# Dejar tail en logs:
tail -f logs/spring.log | grep "ExpiredAuthoritiesCleanupJob"

# Esperar hasta las 2 AM para ver si ejecuta
```

---

### Problema 8: Scheduled job se ejecuta pero no actualiza registros

**Síntomas:**

- Logs muestran: "Cleaned up 0 expired authority assignments"
- Pero existen registros con `expires_at <= NOW()` y `is_active=true`

**Diagnóstico:**

**1. Verificar query del job:**

```sql
-- Query que usa el job (debería retornar registros):
SELECT * FROM scr_user_authority
WHERE expires_at <= NOW() AND is_active = true;
```

**2. Verificar timestamp en BD vs aplicación:**

```sql
-- Comparar NOW() de PostgreSQL con Instant.now() de Java
SELECT NOW(), NOW() AT TIME ZONE 'UTC';
```

**Causas posibles:**

#### Causa A: Diferencia de timezone

**Problema:**

- `expires_at` está en UTC
- `NOW()` del job usa timezone del servidor (ej: GMT-5)

**Solución:**
Modificar query del job para usar UTC:

```java
// En ExpiredAuthoritiesCleanupJob.java
Instant now = Instant.now();  // Ya está en UTC

// Query debe usar:
WHERE expires_at <= :now
```

#### Causa B: Transacción no se comitea

**Problema:**

- Job ejecuta UPDATE
- Pero transacción no hace commit (exception intermedio)

**Solución:**
Verificar logs de errores:

```bash
grep "ERROR" logs/spring.log | grep "ExpiredAuthoritiesCleanupJob"
```

Agregar manejo de errores explícito:

```java
@Scheduled(cron = "0 0 2 * * *")
@Transactional
public void cleanupExpiredAuthorities() {
  try {
    // ... código del job
    log.info("Cleaned up {} expired authorities", count);
  } catch (Exception e) {
    log.error("Error in scheduled cleanup job", e);
    throw e; // Re-throw para que Spring maneje rollback
  }
}

```

---

## Problemas de Rendimiento

### Problema 9: Dashboard carga muy lento (> 5 segundos)

**Síntomas:**

- `/admin/authorization-dashboard` tarda mucho en cargar
- Spinner de loading aparece por varios segundos
- Navegador se vuelve lento

**Causas posibles:**

#### Causa A: Muchos registros de auditoría en BD

**Diagnóstico:**

```sql
SELECT COUNT(*) FROM scr_authority_audit;

-- Si > 100,000 registros, considerar limpieza
```

**Solución:**
Limpiar auditoría antigua (mantener últimos 6 meses):

```sql
-- Backup primero!
CREATE TABLE scr_authority_audit_backup AS
SELECT * FROM scr_authority_audit
WHERE changed_date < NOW() - INTERVAL '6 months';

-- Eliminar antiguos
DELETE FROM scr_authority_audit
WHERE changed_date < NOW() - INTERVAL '6 months';

-- Vacuum para liberar espacio
VACUUM FULL scr_authority_audit;
```

#### Causa B: Queries no optimizadas

**Diagnóstico:**

```sql
-- Activar logging de queries lentas en postgresql.conf:
log_min_duration_statement = 1000  # Loguear queries > 1 segundo

-- Revisar logs:
grep "duration:" /var/log/postgresql/postgresql-*.log
```

**Solución:**

- Verificar índices en tablas scr\_\*
- Agregar índices faltantes:

```sql
-- Ejemplo: si query de "expiring roles" es lenta:
CREATE INDEX IF NOT EXISTS idx_user_authority_expires_at_active
ON scr_user_authority(expires_at, is_active)
WHERE is_active = true AND expires_at IS NOT NULL;
```

#### Causa C: Demasiados widgets cargando simultáneamente

**Solución:**

- Implementar lazy loading de widgets (cargar bajo demanda)
- Implementar caching en backend (ttl: 5 minutos)
- Paginar "Recent Activity" (mostrar solo 10, no 100)

---

### Problema 10: Listado de authorities/permissions carga lento

**Síntomas:**

- `/admin/authority` tarda en cargar
- Muchos registros en lista

**Solución:**

**1. Implementar paginación:**

- Backend ya soporta `Pageable` en controllers
- Frontend debe pasar params: `?page=0&size=20`

**2. Filtrar por activos primero:**

- Mostrar solo `is_active=true` por defecto
- Opción "Show inactive" para ver todos

**3. Verificar índices:**

```sql
-- Índice en is_active debe existir:
SELECT * FROM pg_indexes WHERE tablename = 'scr_authority';

-- Si falta:
CREATE INDEX idx_scr_authority_is_active ON scr_authority(is_active);
```

---

## Errores Comunes de API

### Problema 11: Error 403 al crear rol

**Request:**

```http
POST /api/authorities
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Manager",
  "code": "ROLE_MANAGER",
  "category": "CUSTOM"
}
```

**Response:**

```http
403 Forbidden
{
  "error": "Access Denied"
}
```

**Causa:**
Usuario no tiene rol ROLE_ADMIN.

**Diagnóstico:**

```bash
# Decodificar token JWT
echo "<TOKEN>" | cut -d. -f2 | base64 -d | jq .

# Verificar claim 'auth':
# Debe contener "ROLE_ADMIN"
```

**Solución:**

- Asignar ROLE_ADMIN al usuario
- O usar token de un usuario admin

---

### Problema 12: Error 400 al crear permiso

**Request:**

```http
POST /api/permissions
{
  "resource": "report-export",
  "action": "do"
}
```

**Response:**

```http
400 Bad Request
{
  "error": "Invalid resource name. Use only lowercase letters, numbers, and underscores."
}
```

**Causa:**
Resource name contiene guiones (no permitidos).

**Solución:**

```json
{
  "resource": "report_export", // Usar underscore
  "action": "create"
}
```

O mejor aún, seguir convención:

```json
{
  "resource": "report", // Recurso simple
  "action": "export" // Acción descriptiva
}
```

---

### Problema 13: Error 409 Conflict al asignar rol

**Request:**

```http
POST /api/user-authorities
{
  "userId": 123,
  "authorityId": 2
}
```

**Response:**

```http
409 Conflict
{
  "error": "User already has this authority assigned"
}
```

**Causa:**
El usuario ya tiene ese rol asignado (activo o inactivo).

**Diagnóstico:**

```sql
SELECT * FROM scr_user_authority
WHERE user_id = 123 AND authority_id = 2;
```

**Solución:**

**Si la asignación está activa:**

- No hacer nada (ya tiene el rol)
- O si necesita cambiar fecha de expiración:
  1. Revocar asignación actual
  2. Asignar nuevamente con nueva fecha

**Si la asignación está revocada/expirada:**

- Revocarla formalmente (si is_active=true)
- Asignar nuevamente (crea nuevo registro)

---

## Problemas de Dashboard

### Problema 14: Dashboard muestra métricas incorrectas

**Síntomas:**

- "Active Users" muestra número incorrecto
- "Expired Roles" muestra 0 pero existen roles expirados

**Diagnóstico:**

**1. Verificar queries del backend:**

```java
// En AuthorizationDashboardService.java
// Método getActiveUsers() debería contar:
// - Usuarios con al menos 1 rol activo Y válido (no expirado)

```

**2. Verificar manualmente con SQL:**

```sql
-- Active Users (al menos 1 rol válido):
SELECT COUNT(DISTINCT ua.user_id) FROM scr_user_authority ua
WHERE ua.is_active = true
  AND (ua.expires_at IS NULL OR ua.expires_at > NOW());

-- Expired Roles (expirados pero aún is_active=true):
SELECT COUNT(*) FROM scr_user_authority
WHERE expires_at <= NOW() AND is_active = true;
```

**3. Comparar con dashboard:**

- Ir a `/admin/authorization-dashboard`
- Comparar números con resultados de SQL

**Solución:**

Si los números difieren:

- Revisar lógica del servicio backend
- Verificar timezone handling
- Verificar que queries usen índices correctos

**Workaround temporal:**
Click en botón "Refresh" del dashboard para recargar.

---

### Problema 15: Gráfico "Top Authorities" no muestra datos

**Síntomas:**

- Gráfico aparece vacío
- No hay barras ni leyenda

**Causas posibles:**

#### Causa A: No hay asignaciones de roles en el sistema

**Diagnóstico:**

```sql
SELECT COUNT(*) FROM scr_user_authority WHERE is_active = true;
```

**Solución:**

- Asignar al menos 1 rol a 1 usuario
- Refresh del dashboard

#### Causa B: Error de JavaScript en frontend

**Diagnóstico:**

- Abrir DevTools del navegador (F12)
- Ver consola: buscar errores de JavaScript
- Ver Network tab: verificar que `/api/authorization/dashboard/metrics` retorna datos

**Solución:**
Si el endpoint retorna datos pero el gráfico no aparece:

- Verificar que `recharts` esté instalado: `npm list recharts`
- Verificar que TopAuthoritiesChart esté importado correctamente
- Verificar formato de datos (debe ser array de objetos con authorityName y userCount)

#### Causa C: Todos los roles tienen 0 usuarios

**Diagnóstico:**

```sql
SELECT a.name, COUNT(ua.user_id) as count
FROM scr_authority a
LEFT JOIN scr_user_authority ua ON a.id = ua.authority_id AND ua.is_active = true
GROUP BY a.name
ORDER BY count DESC;
```

**Solución:**

- Asignar roles a usuarios
- Si existen asignaciones, verificar que is_active=true

---

## Apéndice A: Comandos SQL Útiles

### Ver estado completo de un usuario:

```sql
-- Roles activos y expirados
SELECT a.code, a.name, ua.is_active, ua.expires_at,
       CASE
           WHEN ua.is_active = false THEN 'Revoked'
           WHEN ua.expires_at IS NOT NULL AND ua.expires_at <= NOW() THEN 'Expired'
           ELSE 'Active'
       END as status
FROM scr_user_authority ua
JOIN scr_authority a ON ua.authority_id = a.id
WHERE ua.user_id = USER_ID
ORDER BY ua.assigned_date DESC;

-- Permisos directos
SELECT p.name, up.is_active, up.expires_at
FROM scr_user_permission up
JOIN scr_permission p ON up.permission_id = p.id
WHERE up.user_id = USER_ID;
```

### Ver historial de cambios de un rol:

```sql
SELECT changed_date, changed_by, action, old_values, new_values, ip_address
FROM scr_authority_audit
WHERE authority_id = AUTHORITY_ID
ORDER BY changed_date DESC
LIMIT 20;
```

### Detectar "permisos zombie" (sin expiración):

```sql
-- Permisos directos sin fecha de expiración
SELECT u.login, p.name, up.granted_date, up.granted_by
FROM scr_user_permission up
JOIN scr_user u ON up.user_id = u.id
JOIN scr_permission p ON up.permission_id = p.id
WHERE up.is_active = true AND up.expires_at IS NULL
ORDER BY up.granted_date ASC;
```

---

## Apéndice B: Logs Importantes

### Ubicación de logs:

```bash
logs/spring.log              # Log principal
logs/spring-{date}.log.gz    # Logs archivados
```

### Búsquedas útiles:

```bash
# Errores de autenticación
grep "Authentication" logs/spring.log | grep "failed"

# Scheduled job execution
grep "ExpiredAuthoritiesCleanupJob" logs/spring.log

# Errores de auditoría
grep "ERROR" logs/spring.log | grep "Authority"

# Queries lentas (si está habilitado)
grep "SlowQuery" logs/spring.log
```

---

## Contacto y Escalamiento

**Para problemas no resueltos:**

1. Recopilar información:
   - Logs relevantes (últimos 100 líneas)
   - Query SQL que reproduce el problema
   - Request/Response HTTP completo
   - Timestamp exacto del problema
2. Crear issue en repositorio con etiqueta "authorization"
3. Incluir pasos para reproducir
4. Contactar al equipo de desarrollo

---

**Fin del Troubleshooting**
