# FAQ - Sistema de Autorización Enterprise

**Versión:** 1.0
**Fecha:** 2025-11-07
**Audiencia:** Administradores y usuarios finales

---

## Tabla de Contenidos

1. [Conceptos Básicos](#conceptos-básicos)
2. [Roles y Permisos](#roles-y-permisos)
3. [Asignaciones Temporales](#asignaciones-temporales)
4. [Auditoría y Seguridad](#auditoría-y-seguridad)
5. [Troubleshooting Básico](#troubleshooting-básico)

---

## Conceptos Básicos

### 1. ¿Cuál es la diferencia entre un rol y un permiso?

**Rol (Authority):**

- Es un conjunto de permisos agrupados bajo un nombre significativo
- Ejemplo: `ROLE_ADMIN` tiene todos los permisos del sistema
- Se asigna a múltiples usuarios que comparten las mismas responsabilidades
- Gestión centralizada: cambias el rol, afectas a todos los usuarios

**Permiso (Permission):**

- Es una autorización específica sobre un recurso y acción
- Patrón: `recurso.acción` (ej: `user.create`, `report.export`)
- Se asigna a roles (común) o directamente a usuarios (excepcional)
- Gestión granular: control fino sobre qué puede hacer cada rol

**Analogía:**

- **Rol**: Es como un "cargo" en una empresa (Gerente, Supervisor, Empleado)
- **Permiso**: Es como una "tarea específica" que puede realizar ese cargo (crear reportes, aprobar gastos)

**Cuándo usar cada uno:**

- **Use roles**: Para permisos permanentes y comunes a múltiples usuarios
- **Use permisos directos**: Solo para casos excepcionales temporales

---

### 2. ¿Puedo editar o eliminar ROLE_ADMIN o ROLE_USER?

**No.** Estos roles están protegidos y **no pueden modificarse ni eliminarse**.

**Razón:**

- Son roles fundamentales del sistema (marcados con `is_system=true`)
- ROLE_ADMIN es necesario para acceder al panel de administración
- ROLE_USER es el rol base para usuarios estándar
- Están definidos en el seed data inicial del sistema

**Identificación en la UI:**

- Badge amarillo "System" en la lista de roles
- Botones de editar/eliminar deshabilitados (grises)
- Icono de candado en la tarjeta del rol

**Si necesitas un rol similar pero personalizado:**

1. Crea un nuevo rol CUSTOM (ej: `ROLE_SUPER_ADMIN`)
2. Asígnale los permisos que necesites
3. Asigna ese rol a los usuarios correspondientes

---

### 3. ¿Qué pasa cuando un rol temporal expira?

**Comportamiento automático:**

1. **Inmediatamente al expirar** (`expires_at` <= fecha actual):

   - El usuario **pierde acceso** a los permisos del rol
   - Backend valida `expires_at` en cada petición
   - Frontend muestra badge rojo "Expirado"

2. **A las 2 AM del día siguiente** (job programado):
   - El sistema marca la asignación como `is_active=false`
   - Se ejecuta `ExpiredAuthoritiesCleanupJob`
   - Se loguea cantidad de registros limpiados

**El usuario NO recibe notificación automática** (en la versión actual). Se recomienda:

- Revisar el widget "Expiring Roles" en el dashboard
- Contactar usuarios manualmente si necesitan renovación

**Para extender el acceso:**

1. El rol debe revocarse primero (si está expirado)
2. Asignar nuevamente el rol con nueva fecha de expiración

---

### 4. ¿Cómo asigno permisos a un usuario sin crear un rol?

Use **permisos directos** (Direct Permissions).

**Pasos:**

1. Ir a `/admin/user-management/{userId}`
2. En la sección "Direct Permissions", click **"Grant Direct Permission"**
3. Seleccionar permiso del dropdown
4. Ingresar razón obligatoria (justificación)
5. Opcionalmente, poner fecha de expiración
6. Click "Grant"

**Casos de uso recomendados:**

- Permisos excepcionales temporales (1-7 días)
- Testing de nuevos permisos antes de crear rol
- Workarounds de emergencia

**No recomendado para:**

- Permisos permanentes (use roles en su lugar)
- Permisos comunes a múltiples usuarios (use roles)

**Advertencia:** Los permisos directos sin fecha de expiración pueden convertirse en "permisos zombie" difíciles de rastrear. **Siempre asigne con fecha de expiración** (30 días máximo).

---

### 5. ¿Puedo ver quién cambió los permisos de un rol?

**Sí.** El sistema tiene auditoría completa.

**Desde la vista de detalle del rol** (`/admin/authority/{id}`):

1. Scroll a la sección **"Audit History"**
2. Buscar entradas con action=PERMISSIONS_ADDED o PERMISSIONS_REMOVED
3. Ver información completa:
   - Usuario que hizo el cambio
   - Fecha y hora exacta
   - IP address del usuario
   - User-Agent (navegador)
   - Permisos agregados/removidos (lista detallada)

**Ejemplo de entrada de auditoría:**

```
2025-11-05 04:30 PM - super_admin - PERMISSIONS_REMOVED
IP: 192.168.1.50
User Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
Permisos removidos: [user.delete, authority.delete]
```

**Todas las acciones auditadas:**

- CREATED, UPDATED, DELETED
- ACTIVATED, DEACTIVATED
- PERMISSIONS_ADDED, PERMISSIONS_REMOVED
- HIERARCHY_CHANGED

---

## Roles y Permisos

### 6. ¿Qué es el hierarchy_level y para qué sirve?

**Hierarchy Level** es un número que indica la prioridad/jerarquía de un rol.

**Escala:**

- **0**: Máxima jerarquía (ej: ROLE_ADMIN)
- **500**: Jerarquía media (ej: ROLE_USER)
- **999**: Jerarquía mínima

**Uso actual (v1.0):**

- Informativo y de ordenamiento en listas
- Ayuda a entender la estructura organizacional de roles

**Uso futuro planeado:**

- Herencia de permisos: un rol con nivel 100 podría heredar permisos de roles con nivel > 100
- Validaciones de seguridad: un rol de nivel 200 no podría modificar un rol de nivel 100

**Ejemplo de estructura jerárquica:**

```
0   - ROLE_ADMIN (Administrador total)
100 - ROLE_MANAGER (Gerente de equipos)
200 - ROLE_SUPERVISOR (Supervisor de operaciones)
500 - ROLE_USER (Usuario estándar)
800 - ROLE_GUEST (Invitado temporal)
```

**Al crear un rol custom, ¿qué número poner?**

- Entre 0-100: Roles de alta jerarquía (administradores)
- Entre 100-500: Roles de gestión (managers, supervisores)
- Entre 500-999: Roles operativos (usuarios finales)

---

### 7. ¿Puedo exportar la configuración de roles de dev a prod?

**Sí, pero es un proceso manual** en la versión actual (v1.0).

**Método recomendado (PostgreSQL):**

1. **Exportar desde ambiente de desarrollo:**

```bash
# Conectarse a BD de dev
psql -h localhost -U postgres -d tysescrutinygateway

# Exportar roles custom
COPY (SELECT * FROM scr_authority WHERE category='CUSTOM' AND is_system=false)
TO '/tmp/roles_export.csv' WITH CSV HEADER;

# Exportar permisos custom (si creaste nuevos)
COPY (SELECT * FROM scr_permission WHERE id > 13)
TO '/tmp/permissions_export.csv' WITH CSV HEADER;

# Exportar relación role-permiso
COPY (SELECT ap.* FROM scr_authority_permission ap
      JOIN scr_authority a ON ap.authority_id = a.id
      WHERE a.category='CUSTOM')
TO '/tmp/authority_permissions_export.csv' WITH CSV HEADER;
```

2. **Importar en ambiente de producción:**

```bash
# Conectarse a BD de prod
psql -h prod-server -U postgres -d tysescrutinygateway

# Importar roles
COPY scr_authority FROM '/tmp/roles_export.csv' WITH CSV HEADER;

# Importar permisos (si aplica)
COPY scr_permission FROM '/tmp/permissions_export.csv' WITH CSV HEADER;

# Importar relaciones
COPY scr_authority_permission FROM '/tmp/authority_permissions_export.csv' WITH CSV HEADER;
```

**Precauciones:**

- **No exporte roles SYSTEM** (ROLE_ADMIN, ROLE_USER) - ya existen en prod
- **Verifique que los IDs de permisos coincidan** entre ambientes
- **No exporte asignaciones de usuarios** (scr_user_authority) - son específicas de cada ambiente
- **Haga backup** de la BD de prod antes de importar

**Feature futura:**

- Endpoint de exportación: `GET /api/authorities/export/json`
- Endpoint de importación: `POST /api/authorities/import` (con validaciones)

---

### 8. ¿Cada cuánto se ejecuta el scheduled job de cleanup?

**Diariamente a las 2:00 AM** (hora del servidor).

**Nombre del job:** `ExpiredAuthoritiesCleanupJob`

**Qué hace:**

1. Busca asignaciones de roles con `expires_at <= NOW()` y `is_active=true`
2. Las marca como `is_active=false`
3. Busca permisos directos con `expires_at <= NOW()` y `is_active=true`
4. Los marca como `is_active=false`
5. Loguea cantidad de registros procesados

**Configuración:**

- Cron expression: `0 0 2 * * *` (anotación `@Scheduled` en código)
- Thread pool size: 2 (configurado en `application.yml`)

**Para verificar que se ejecutó:**

1. Revisar logs del servidor:

```bash
grep "ExpiredAuthoritiesCleanupJob" logs/spring.log
```

2. Buscar líneas como:

```
2025-11-07 02:00:01 INFO  - Cleaned up 5 expired authority assignments
2025-11-07 02:00:01 INFO  - Cleaned up 2 expired permission grants
```

**Si necesita ejecutar manualmente:**

- No hay endpoint expuesto (por seguridad)
- Puede ejecutar query SQL manual:

```sql
UPDATE scr_user_authority
SET is_active = false
WHERE expires_at <= NOW() AND is_active = true;
```

---

## Asignaciones Temporales

### 9. ¿Qué significa "rol de sistema" (System Role)?

**Definición:**
Un rol marcado con `is_system=true` en la base de datos.

**Características:**

- **No puede modificarse**: name, code, category son inmutables
- **No puede eliminarse**: protección contra borrado accidental
- **No puede desactivarse**: siempre `is_active=true`
- **Se crea en seed data**: definido al inicializar la aplicación

**Roles de sistema en Tyse Scrutiny:**

- `ROLE_ADMIN` (id=1): Administrador con acceso total
- `ROLE_USER` (id=2): Usuario estándar con permisos básicos

**Identificación visual:**

- Badge amarillo "System" en la lista
- Botones de editar/eliminar deshabilitados
- Mensaje de advertencia al intentar modificar

**Si necesita personalizar un rol de sistema:**

1. **No modifique el rol original**
2. Cree un rol CUSTOM nuevo (ej: `ROLE_CUSTOM_ADMIN`)
3. Copie manualmente los permisos del rol de sistema
4. Agregue/quite permisos según necesite
5. Asigne el nuevo rol a usuarios

**Razón de esta restricción:**

- Garantizar que siempre exista un rol ADMIN para acceder al sistema
- Evitar que administradores se queden sin acceso por error
- Mantener consistencia entre ambientes (dev, staging, prod)

---

### 10. ¿Puedo tener múltiples roles simultáneos?

**Sí.** Un usuario puede tener múltiples roles activos al mismo tiempo.

**Ejemplo:**
Un usuario puede tener:

- `ROLE_USER` (permanente)
- `ROLE_MANAGER` (permanente)
- `ROLE_AUDITOR` (temporal por 30 días)

**Permisos efectivos:**

- El usuario tiene la **unión** de todos los permisos de sus roles activos
- Si `ROLE_USER` tiene `report.read` y `ROLE_MANAGER` tiene `report.export`
- El usuario puede hacer ambas cosas: leer Y exportar reportes

**No hay conflictos de permisos:**

- No existe "permiso negativo" (solo grants, no revokes)
- Tener más roles siempre da **más permisos**, nunca menos

**Validación en backend:**
El sistema evalúa:

```
Usuario tiene permiso si:
  - Alguno de sus roles activos y válidos tiene el permiso, O
  - Tiene el permiso asignado directamente y está activo y válido
```

**En el JWT token:**

- Claim `auth` contiene array de códigos de roles activos
- Ejemplo: `["ROLE_USER", "ROLE_MANAGER", "ROLE_AUDITOR"]`
- El backend expande estos roles a permisos en cada petición

**Buenas prácticas:**

- **No abuse de múltiples roles**: Dificulta auditar qué permisos tiene realmente un usuario
- **Use jerarquía de roles**: Un ROLE_MANAGER debería incluir todos los permisos de ROLE_USER
- **Documente combinaciones comunes**: Si muchos usuarios tienen ROLE_A + ROLE_B, considere crear ROLE_AB

---

## Auditoría y Seguridad

### 11. ¿Cómo funciona la auditoría de cambios?

**Mecanismo automático:**
Cada vez que se crea, modifica o elimina un rol, el sistema registra automáticamente:

**Información capturada:**

- **Authority ID**: ID del rol afectado
- **Action**: Tipo de cambio (CREATED, UPDATED, DELETED, etc.)
- **Old Values**: Estado anterior en JSON
- **New Values**: Estado nuevo en JSON
- **Changed By**: Login del usuario que hizo el cambio
- **Changed Date**: Timestamp preciso
- **IP Address**: IPv4 o IPv6 del cliente
- **User-Agent**: Información del navegador/cliente

**Acciones auditadas:**

- CREATED: Rol creado desde cero
- UPDATED: Cambios en name, description, hierarchy, etc.
- DELETED: Rol eliminado permanentemente
- ACTIVATED: Rol reactivado (is_active: false → true)
- DEACTIVATED: Rol desactivado (is_active: true → false)
- PERMISSIONS_ADDED: Permisos asignados al rol
- PERMISSIONS_REMOVED: Permisos removidos del rol
- HIERARCHY_CHANGED: hierarchy_level modificado

**Dónde se almacena:**

- Tabla: `scr_authority_audit`
- Retención: Indefinida (sin límite de tiempo en v1.0)
- Tamaño: Sin límite configurado (considerar limpieza manual si crece mucho)

**Cómo consultar:**

1. UI: Detalle del rol → Sección "Audit History"
2. Dashboard: Widget "Recent Activity" (últimas 10)
3. SQL directo: `SELECT * FROM scr_authority_audit WHERE authority_id = X ORDER BY changed_date DESC;`

**No se audita (en v1.0):**

- Asignaciones de roles a usuarios (scr_user_authority) - solo metadata (assigned_by, assigned_date)
- Permisos directos (scr_user_permission) - solo metadata
- Cambios en permisos (scr_permission) - pendiente en roadmap

---

### 12. ¿Qué información de seguridad se captura en auditoría?

**IP Address:**

- IPv4: `192.168.1.100`
- IPv6: `2001:0db8:85a3::8a2e:0370:7334`
- Origen: Header `X-Forwarded-For` (si hay proxy) o IP directa del request
- Uso: Identificar desde dónde se hizo el cambio (útil para detectar accesos sospechosos)

**User-Agent:**

- Ejemplo: `Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/91.0.4472.124`
- Origen: Header HTTP `User-Agent`
- Uso: Identificar navegador, SO, dispositivo del usuario

**Changed By:**

- Login del usuario autenticado (claim `sub` del JWT)
- Ejemplo: `admin`, `super_admin`, `john.doe`
- Uso: Responsable directo del cambio

**Changed Date:**

- Timestamp preciso (microsegundos)
- Timezone: UTC (almacenado en BD)
- Ejemplo: `2025-11-07T14:30:15.123456Z`

**Uso de esta información:**

- **Auditorías de seguridad**: Detectar cambios no autorizados
- **Troubleshooting**: Rastrear qué cambio causó un problema
- **Compliance**: Cumplir con regulaciones que requieren trazabilidad (SOX, GDPR)
- **Forense**: Investigar incidentes de seguridad

---

## Troubleshooting Básico

### 13. ¿Por qué un usuario no tiene acceso a un recurso?

**Checklist de diagnóstico:**

1. **¿El usuario tiene un rol activo?**

   - Ir a `/admin/user-management/{userId}`
   - Verificar que tenga al menos 1 rol con badge verde "Activo"
   - Si solo tiene roles expirados/revocados → asignar nuevo rol

2. **¿El rol está activo?**

   - Ir a `/admin/authority/{id}`
   - Verificar que `Active` esté marcado
   - Si está inactivo → reactivar el rol

3. **¿El rol tiene el permiso necesario?**

   - En la vista de detalle del rol, sección "Permissions"
   - Buscar el permiso (ej: `report.export`)
   - Si no está → asignar el permiso al rol

4. **¿El token JWT está actualizado?**

   - Los cambios en roles NO se reflejan en tokens existentes
   - Pedir al usuario: logout + login
   - Verificar que el nuevo token incluya el rol (inspeccionar JWT en jwt.io)

5. **¿El permiso está activo?**

   - Ir a `/admin/permission`
   - Buscar el permiso
   - Verificar que `Active` esté marcado

6. **¿El endpoint requiere un permiso específico?**
   - Revisar anotación `@PreAuthorize` en el controller
   - Confirmar que el usuario tiene exactamente ese permiso

**Comandos SQL útiles:**

```sql
-- Ver roles activos y válidos del usuario
SELECT a.* FROM scr_authority a
JOIN scr_user_authority ua ON a.id = ua.authority_id
WHERE ua.user_id = X AND ua.is_active = true
  AND (ua.expires_at IS NULL OR ua.expires_at > NOW());

-- Ver permisos del rol
SELECT p.* FROM scr_permission p
JOIN scr_authority_permission ap ON p.id = ap.permission_id
WHERE ap.authority_id = Y AND p.is_active = true;
```

---

### 14. ¿Por qué el dashboard muestra "Expired Roles" si el usuario ya no tiene acceso?

**Explicación:**

El widget "Expired Roles" muestra asignaciones que:

- Tienen `expires_at <= NOW()` (ya expiraron)
- Pero **todavía** tienen `is_active=true` (no procesadas por el job)

**Esto es normal y esperado:**

- El usuario **ya perdió acceso** (el backend valida `expires_at` en cada petición)
- Pero el registro en BD no se actualizó aún
- El job programado lo actualizará a las 2 AM

**Timeline:**

```
10:00 AM - Rol expira (expires_at alcanzado)
10:00 AM - Usuario pierde acceso inmediatamente (validación backend)
10:01 AM - Dashboard muestra alerta "Expired Roles" (registro aún activo en BD)
02:00 AM (día siguiente) - Job actualiza is_active=false
02:01 AM - Dashboard ya no muestra la alerta
```

**Si necesita limpieza inmediata:**
Ejecutar query SQL manual:

```sql
UPDATE scr_user_authority
SET is_active = false,
    revoked_by = 'cleanup_manual',
    revoked_date = NOW(),
    revoked_reason = 'Cleanup manual de roles expirados'
WHERE expires_at <= NOW() AND is_active = true;
```

**Este comportamiento es por diseño:**

- Evita carga constante de escrituras en BD
- Agrupa cleanup en una ventana de mantenimiento (2 AM)
- No afecta seguridad (validación en backend es la authoritative)

---

### 15. ¿El sistema envía notificaciones cuando un rol está por expirar?

**No automáticamente** en la versión actual (v1.0).

**Alternativas manuales:**

1. **Revisar el Dashboard:**

   - Ir a `/admin/authorization-dashboard`
   - Widget "Expiring Roles" muestra roles que expirarán en próximos 7 días
   - Badges de color indican urgencia (rojo=hoy/mañana, amarillo=2-3 días, azul=4-7 días)

2. **Consulta SQL programada:**
   - Crear cron job externo que ejecute:

```sql
SELECT u.login, a.name, ua.expires_at
FROM scr_user_authority ua
JOIN scr_user u ON ua.user_id = u.id
JOIN scr_authority a ON ua.authority_id = a.id
WHERE ua.is_active = true
  AND ua.expires_at BETWEEN NOW() AND NOW() + INTERVAL '7 days'
ORDER BY ua.expires_at ASC;
```

- Enviar resultados por email a administradores

**Feature en roadmap (futuro):**

- Job programado que envíe emails automáticos
- Notificaciones in-app para usuarios
- Configuración de umbrales (alertar 3, 7, 14 días antes)

---

## Apéndice: Recursos Adicionales

- **Manual de Usuario Completo**: `/docs/user-manual/AUTHORIZATION_ADMIN_GUIDE.md`
- **Guía de Operaciones (SysOps)**: `/docs/operations/AUTHORIZATION_OPS_GUIDE.md`
- **API Reference**: `/docs/api/AUTHORIZATION_API_REFERENCE.md`
- **Troubleshooting Detallado**: `/docs/TROUBLESHOOTING.md`
- **Diagramas Arquitectónicos**: `/docs/diagrams/`

---

**Fin del FAQ**
