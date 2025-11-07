# Manual de Usuario - Sistema de Autorización Enterprise

**Versión:** 1.0
**Fecha:** 2025-11-07
**Audiencia:** Administradores del sistema

---

## Tabla de Contenidos

1. [Introducción al Sistema](#1-introducción-al-sistema)
2. [Gestión de Roles (Authorities)](#2-gestión-de-roles-authorities)
3. [Gestión de Permisos (Permissions)](#3-gestión-de-permisos-permissions)
4. [Asignación de Roles a Usuarios](#4-asignación-de-roles-a-usuarios)
5. [Permisos Directos (Bypass de Roles)](#5-permisos-directos-bypass-de-roles)
6. [Dashboard de Administración](#6-dashboard-de-administración)
7. [Auditoría y Reportes](#7-auditoría-y-reportes)
8. [Casos de Uso Comunes](#8-casos-de-uso-comunes)

---

## 1. Introducción al Sistema

### 1.1 ¿Qué es el Sistema de Autorización Enterprise?

Tyse Scrutiny implementa un sistema de control de acceso basado en roles (**RBAC**) con características avanzadas:

- **Roles jerárquicos**: Agrupan permisos relacionados (ej: ROLE_ADMIN, ROLE_MANAGER)
- **Permisos granulares**: Autorizaciones específicas sobre recursos (ej: `user.create`, `report.export`)
- **Asignaciones temporales**: Roles con fecha de expiración automática
- **Permisos directos**: Bypass de roles para casos excepcionales
- **Auditoría completa**: Trazabilidad total de todos los cambios

### 1.2 Conceptos Clave

#### Rol (Authority)

Un conjunto de permisos agrupados bajo un nombre significativo. Ejemplo:

- **ROLE_ADMIN**: Administrador con acceso total
- **ROLE_MANAGER**: Gerente con permisos de gestión de equipos
- **ROLE_USER**: Usuario estándar con permisos básicos

#### Permiso (Permission)

Una autorización específica sobre un recurso y acción. Patrón: `recurso.acción`

Ejemplos:

- `user.create` - Crear usuarios
- `user.read` - Ver información de usuarios
- `report.export` - Exportar reportes
- `authority.assign` - Asignar roles a usuarios

#### Asignación (User Authority)

La relación entre un usuario y un rol. Puede ser:

- **Permanente**: Sin fecha de expiración
- **Temporal**: Con fecha de expiración automática

#### Estados de Asignación

- **Activo**: El usuario tiene el rol y puede usarlo
- **Expirado**: La fecha de expiración pasó (el sistema lo desactiva automáticamente)
- **Revocado**: Un administrador revocó el rol manualmente

### 1.3 Navegación Principal

Para acceder al sistema de autorización:

1. Inicie sesión con credenciales de administrador
2. Navegue al menú **Administration** (icono de engranaje)
3. Encontrará las siguientes opciones:

| Opción                  | URL                              | Descripción                 |
| ----------------------- | -------------------------------- | --------------------------- |
| Authorities             | `/admin/authority`               | Gestión de roles            |
| Permissions             | `/admin/permission`              | Gestión de permisos         |
| Authorization Dashboard | `/admin/authorization-dashboard` | Panel de métricas y alertas |

**Nota:** La asignación de roles a usuarios se realiza desde la gestión de usuarios en `/admin/user-management`.

### 1.4 Diferencia entre Roles y Permisos Directos

| Aspecto             | Roles                                         | Permisos Directos                            |
| ------------------- | --------------------------------------------- | -------------------------------------------- |
| **Uso recomendado** | Permisos permanentes y comunes                | Casos excepcionales temporales               |
| **Gestión**         | Centralizadas (cambia el rol, afecta a todos) | Individualizadas (solo afecta al usuario)    |
| **Auditoría**       | Por rol y por usuario                         | Solo por usuario                             |
| **Ejemplo**         | "Todos los managers pueden crear reportes"    | "Juan necesita exportar datos solo este mes" |

**Recomendación:** Use roles siempre que sea posible. Los permisos directos son para situaciones excepcionales.

---

## 2. Gestión de Roles (Authorities)

### 2.1 Ver Lista de Roles

**URL:** `/admin/authority`

La lista muestra todos los roles del sistema con:

- **ID**: Identificador único
- **Name**: Nombre descriptivo (ej: "Administrator")
- **Code**: Código técnico (ej: "ROLE_ADMIN")
- **Category**: Tipo de rol (SYSTEM, CUSTOM, TENANT_SPECIFIC)
- **Active**: Estado activo/inactivo
- **System**: Indica si es un rol protegido del sistema

**Filtros disponibles:**

- Por nombre o código (búsqueda)
- Por categoría
- Por estado (activo/inactivo)

### 2.2 Crear un Nuevo Rol

**Pasos:**

1. Click en el botón **"Create new Authority"**
2. Complete el formulario:

| Campo               | Descripción                                 | Ejemplo                             | Obligatorio |
| ------------------- | ------------------------------------------- | ----------------------------------- | ----------- |
| **Name**            | Nombre descriptivo del rol                  | "Manager"                           | Sí          |
| **Code**            | Código técnico (debe empezar con "ROLE\_")  | "ROLE_MANAGER"                      | Sí          |
| **Description**     | Explicación del propósito del rol           | "Can manage teams and view reports" | No          |
| **Category**        | Tipo de rol                                 | CUSTOM                              | Sí          |
| **Hierarchy Level** | Nivel jerárquico (0=más alto, 999=más bajo) | 100                                 | Sí          |
| **Active**          | Estado inicial                              | Checked                             | Sí          |

3. Click en **"Save"**

**Resultado:**

- Se crea el rol en la tabla `scr_authority`
- Se registra en auditoría con action=CREATED
- Se muestra mensaje de confirmación

**Validaciones:**

- El código debe ser único en el sistema
- El código debe seguir el patrón `ROLE_*`
- El nombre no puede estar vacío

### 2.3 Editar un Rol Existente

**Pasos:**

1. En la lista de roles, click en el botón **"Edit"** (icono de lápiz)
2. Modifique los campos necesarios
3. Click en **"Save"**

**Campos editables:**

- Name
- Description
- Hierarchy Level
- Active/Inactive

**Campos NO editables:**

- Code (se define al crear y no cambia)
- Category (se define al crear)

**Restricciones:**

- **No puede editar roles de sistema** (is_system=true) como ROLE_ADMIN o ROLE_USER
- Aparece un mensaje de error si intenta modificar un rol protegido

### 2.4 Desactivar un Rol

**Opción 1: Soft Delete**

1. Editar el rol
2. Desmarcar el checkbox **"Active"**
3. Guardar

**Resultado:** El rol queda inactivo pero se preserva en la base de datos. Los usuarios que lo tenían asignado pierden acceso inmediatamente.

**Opción 2: Eliminar Permanentemente**

1. En la lista de roles, click en el botón **"Delete"** (icono de basura)
2. Confirmar la eliminación

**Restricciones:**

- No puede eliminar roles de sistema (ROLE_ADMIN, ROLE_USER)
- No puede eliminar roles que tienen usuarios asignados
- Si hay usuarios, primero debe revocar todas las asignaciones

### 2.5 Roles de Sistema Protegidos

Los siguientes roles están protegidos y **NO** pueden modificarse ni eliminarse:

| Código     | Nombre        | Descripción                 | Hierarchy Level |
| ---------- | ------------- | --------------------------- | --------------- |
| ROLE_ADMIN | Administrator | Acceso total al sistema     | 0 (máximo)      |
| ROLE_USER  | User          | Permisos básicos de usuario | 500 (medio)     |

**Características:**

- Campo `is_system=true` en base de datos
- Icono de candado en la UI
- Botones de editar/eliminar deshabilitados
- Aparece badge amarillo "System" en la lista

**Razón:** Estos roles son fundamentales para el funcionamiento del sistema y están definidos en el seed data inicial.

### 2.6 Nivel de Jerarquía (Hierarchy Level)

**Propósito:** Establecer orden de precedencia entre roles (usado para futuras features de herencia de permisos).

**Escala:**

- **0**: Máxima jerarquía (ej: ROLE_ADMIN)
- **500**: Jerarquía media (ej: ROLE_USER)
- **999**: Jerarquía mínima

**Uso actual:** Informativo y de ordenamiento en listas.

**Uso futuro:** Implementación de herencia de permisos (un rol con nivel 100 podría heredar permisos de roles con nivel > 100).

---

## 3. Gestión de Permisos (Permissions)

### 3.1 Ver Lista de Permisos

**URL:** `/admin/permission`

La lista muestra todos los permisos del sistema con:

- **ID**: Identificador único
- **Name**: Nombre completo (patrón resource.action)
- **Resource**: Recurso sobre el que actúa (ej: user, authority, report)
- **Action**: Acción permitida (ej: create, read, update, delete, export)
- **Description**: Explicación del permiso
- **Active**: Estado activo/inactivo

**Filtros disponibles:**

- Por recurso (dropdown)
- Por acción (dropdown)
- Por estado (activo/inactivo)
- Búsqueda por nombre

### 3.2 Convención de Nombres de Permisos

**Patrón obligatorio:** `recurso.acción`

#### Recursos Comunes

- `user` - Usuarios del sistema
- `authority` - Roles
- `permission` - Permisos
- `report` - Reportes
- `audit` - Logs de auditoría

#### Acciones Comunes (CRUD + extras)

- `create` - Crear nuevo
- `read` - Leer/Ver
- `update` - Actualizar
- `delete` - Eliminar
- `assign` - Asignar (para recursos relacionales)
- `export` - Exportar datos
- `import` - Importar datos

#### Ejemplos Válidos

```
user.create          → Crear usuarios
user.read            → Ver información de usuarios
user.update          → Modificar usuarios
user.delete          → Eliminar usuarios
authority.assign     → Asignar roles a usuarios
report.export        → Exportar reportes a CSV/PDF
audit.read           → Ver logs de auditoría
```

### 3.3 Crear un Nuevo Permiso

**Pasos:**

1. Click en el botón **"Create new Permission"**
2. Complete el formulario:

| Campo           | Descripción                     | Ejemplo                        | Obligatorio |
| --------------- | ------------------------------- | ------------------------------ | ----------- |
| **Resource**    | Recurso sobre el que actúa      | "report"                       | Sí          |
| **Action**      | Acción permitida                | "export"                       | Sí          |
| **Name**        | Auto-generado (resource.action) | "report.export"                | Auto        |
| **Description** | Explicación del permiso         | "Export reports to CSV or PDF" | No          |
| **Active**      | Estado inicial                  | Checked                        | Sí          |

3. Click en **"Save"**

**Notas:**

- El campo **Name** se genera automáticamente al ingresar resource y action
- La combinación resource+action debe ser única en el sistema
- No puede crear permisos duplicados

**Validaciones:**

- Resource y action no pueden estar vacíos
- No pueden contener espacios ni caracteres especiales
- Solo letras minúsculas, números y guiones

### 3.4 Editar un Permiso Existente

**Pasos:**

1. En la lista de permisos, click en el botón **"Edit"**
2. Modifique los campos editables:
   - Description
   - Active/Inactive
3. Click en **"Save"**

**Campos NO editables:**

- Name (se define al crear)
- Resource (se define al crear)
- Action (se define al crear)

**Razón:** Cambiar el nombre de un permiso podría romper la lógica de autorización existente en el código.

### 3.5 Desactivar un Permiso

Similar a roles, puede desactivar un permiso desmarcando el checkbox **"Active"**.

**Efecto:**

- Los roles que tienen este permiso lo pierden inmediatamente
- Los usuarios con este permiso directo lo pierden
- El permiso permanece en la base de datos (soft delete)

**No puede eliminar permanentemente** permisos que estén asignados a roles o usuarios. Primero debe removerlos de todas las asignaciones.

### 3.6 Asignar Permisos a un Rol

**Desde la vista de detalle del rol:**

1. Ir a `/admin/authority/{id}` (vista de detalle)
2. Scroll a la sección **"Permissions"**
3. Click en el botón **"Add Permission"**
4. Seleccionar permisos del dropdown (multiselect)
5. Click en **"Assign"**

**Resultado:**

- Se crean registros en `scr_authority_permission`
- Se registra en auditoría con action=PERMISSIONS_ADDED
- Se muestra lista actualizada de permisos del rol

**Permisos ya asignados** aparecen con un badge verde y no se pueden agregar nuevamente.

### 3.7 Revocar Permisos de un Rol

**Desde la vista de detalle del rol:**

1. En la lista de permisos asignados
2. Click en el botón **"Remove"** (icono de X) junto al permiso
3. Confirmar la acción

**Resultado:**

- Se elimina el registro en `scr_authority_permission`
- Se registra en auditoría con action=PERMISSIONS_REMOVED
- Todos los usuarios con este rol pierden acceso inmediatamente

---

## 4. Asignación de Roles a Usuarios

### 4.1 Ver Roles de un Usuario

**Opción 1: Desde User Management**

1. Ir a `/admin/user-management`
2. Click en un usuario de la lista
3. En la vista de detalle, ver sección **"Authorities"**

**Opción 2: Desde URL directa**

`/admin/user-management/{userId}` → Ver sección "Authorities"

**Información mostrada:**

- Lista de roles asignados (activos, expirados, revocados)
- Para cada rol:
  - Nombre del rol
  - Estado (badge verde=activo, rojo=expirado, gris=revocado)
  - Fecha de asignación
  - Asignado por (usuario que lo asignó)
  - Fecha de expiración (si aplica)
  - Razón de revocación (si fue revocado)

### 4.2 Asignar un Rol Permanente

**Pasos:**

1. Desde la vista de detalle del usuario (`/admin/user-management/{userId}`)
2. Click en el botón **"Assign Authority"**
3. En el diálogo modal, complete:
   - **Authority**: Seleccionar rol del dropdown
   - **Expires At**: Dejar vacío (para rol permanente)
4. Click en **"Assign"**

**Resultado:**

- Se crea registro en `scr_user_authority` con:
  - `user_id`, `authority_id`
  - `assigned_by` = usuario actual
  - `assigned_date` = fecha actual
  - `expires_at` = NULL
  - `is_active` = true
- El usuario tiene acceso inmediato a los permisos del rol
- Se muestra mensaje de confirmación

### 4.3 Asignar un Rol Temporal

**Pasos:**

1. Desde la vista de detalle del usuario
2. Click en **"Assign Authority"**
3. En el diálogo modal, complete:
   - **Authority**: Seleccionar rol del dropdown
   - **Expires At**: Seleccionar fecha y hora futuras
4. Click en **"Assign"**

**Resultado:**

- Se crea registro en `scr_user_authority` con `expires_at` != NULL
- El usuario tiene acceso hasta la fecha de expiración
- El sistema desactiva automáticamente el rol al expirar (job programado)

**Casos de uso:**

- Acceso temporal a contratistas (30 días)
- Permisos de emergencia (24 horas)
- Roles de prueba (1 semana)

**Advertencias:**

- La fecha debe ser futura (no puede asignar con fecha pasada)
- El sistema valida la fecha en el frontend y backend
- Se muestra alerta visual si la fecha está próxima (< 7 días)

### 4.4 Estados de Asignación

#### Activo

- `is_active=true` y (`expires_at=NULL` o `expires_at > NOW()`)
- Badge verde en la UI
- El usuario puede usar el rol

#### Expirado

- `is_active=true` pero `expires_at <= NOW()`
- Badge rojo en la UI
- El usuario NO puede usar el rol (validación en backend)
- El job programado lo marcará como `is_active=false` en el próximo ciclo (2 AM)

#### Revocado

- `is_active=false` y `revoked_by` != NULL
- Badge gris en la UI
- El usuario NO puede usar el rol
- Se muestra razón de revocación

### 4.5 Revocar un Rol

**Pasos:**

1. Desde la vista de detalle del usuario
2. En la lista de roles, click en el botón **"Revoke"** junto al rol
3. En el diálogo modal, ingresar:
   - **Reason**: Razón obligatoria de revocación (mínimo 10 caracteres)
     - Ejemplo: "El usuario cambió de departamento"
     - Ejemplo: "Finalizó el contrato temporal"
     - Ejemplo: "Revocación por seguridad - incidente reportado"
4. Click en **"Revoke"**

**Resultado:**

- Se actualiza registro en `scr_user_authority`:
  - `is_active` = false
  - `revoked_by` = usuario actual
  - `revoked_date` = fecha actual
  - `revoked_reason` = texto ingresado
- El usuario pierde acceso inmediatamente
- La razón queda registrada para auditoría

**Campos obligatorios:**

- Reason: Mínimo 10 caracteres

**Notas de auditoría:**

- La revocación es permanente (no se puede "desrevocar")
- Para dar acceso nuevamente, debe asignar el rol otra vez (nueva asignación)
- La asignación revocada queda en historial con su razón

### 4.6 Consultar Historial de Asignaciones

**En la vista de detalle del usuario**, la sección "Authorities" muestra:

- **Asignaciones activas** (arriba, destacadas)
- **Asignaciones expiradas** (en gris)
- **Asignaciones revocadas** (en gris con razón)

**Para cada asignación se muestra:**

- Nombre del rol
- Estado (badge de color)
- Assigned by: {username} on {date}
- Expires at: {date} (si aplica)
- Revoked by: {username} on {date} - Reason: {texto} (si fue revocado)

**Ordenamiento:** Activas primero, luego expiradas, luego revocadas (por fecha desc).

---

## 5. Permisos Directos (Bypass de Roles)

### 5.1 ¿Cuándo Usar Permisos Directos?

Los permisos directos **NO** requieren crear un rol. Se asignan directamente a un usuario.

**Casos de uso recomendados:**

- Permisos excepcionales temporales (1-2 días)
- Testing de nuevos permisos antes de crear rol
- Casos únicos que no justifican crear rol
- Workarounds de emergencia

**Casos NO recomendados:**

- Permisos permanentes (use roles)
- Permisos comunes a múltiples usuarios (use roles)
- Permisos estructurales del sistema (use roles)

**Ventajas:**

- Rápido (no requiere crear rol)
- Granular (solo el permiso necesario)
- Temporal (con expiración automática)

**Desventajas:**

- Difícil de gestionar a escala
- No centralizado (cambio manual por usuario)
- Puede olvidarse y quedar asignado indefinidamente

### 5.2 Ver Permisos Directos de un Usuario

**Desde la vista de detalle del usuario** (`/admin/user-management/{userId}`):

1. Scroll a la sección **"Direct Permissions"**
2. Ver lista de permisos asignados directamente (sin pasar por roles)

**Información mostrada:**

- Nombre del permiso (ej: report.export)
- Estado (activo/expirado/revocado)
- Asignado por (usuario que lo asignó)
- Fecha de asignación
- Fecha de expiración (si aplica)
- Razón de la asignación
- Razón de revocación (si fue revocado)

### 5.3 Asignar un Permiso Directo

**Pasos:**

1. Desde la vista de detalle del usuario
2. Click en el botón **"Grant Direct Permission"** (en sección "Direct Permissions")
3. En el diálogo modal, complete:
   - **Permission**: Seleccionar permiso del dropdown
   - **Expires At**: Fecha de expiración (opcional pero recomendado)
   - **Reason**: Justificación obligatoria (mínimo 10 caracteres)
     - Ejemplo: "Necesita exportar datos del Q4 para auditoría externa"
     - Ejemplo: "Testing de nuevo permiso antes de crear rol"
4. Click en **"Grant"**

**Resultado:**

- Se crea registro en `scr_user_permission` con:
  - `user_id`, `permission_id`
  - `granted_by` = usuario actual
  - `granted_date` = fecha actual
  - `expires_at` = fecha seleccionada (o NULL)
  - `reason` = justificación ingresada
  - `is_active` = true
- El usuario tiene acceso inmediato al permiso

**Campos obligatorios:**

- Permission
- Reason (mínimo 10 caracteres)

**Recomendación:** Siempre asigne con fecha de expiración (30 días máximo). Los permisos directos sin expiración son difíciles de rastrear y pueden convertirse en "permisos zombie".

### 5.4 Revocar un Permiso Directo

**Pasos:**

1. Desde la vista de detalle del usuario
2. En la lista de permisos directos, click en el botón **"Revoke"**
3. En el diálogo modal, ingresar:
   - **Reason**: Razón de revocación (opcional en este caso)
4. Click en **"Revoke"**

**Resultado:**

- Se actualiza registro en `scr_user_permission`:
  - `is_active` = false
  - `revoked_by` = usuario actual
  - `revoked_date` = fecha actual
  - `revoked_reason` = texto ingresado
- El usuario pierde acceso inmediato al permiso

---

## 6. Dashboard de Administración

### 6.1 Acceso al Dashboard

**URL:** `/admin/authorization-dashboard`

**Requisitos:**

- Autenticación con rol ROLE_ADMIN
- Navegador moderno (para visualización de gráficos)

### 6.2 Métricas Principales

El dashboard muestra 4 tarjetas de métricas en la parte superior:

#### Métrica 1: Total Authorities

- **Icono:** Shield (escudo azul)
- **Número:** Cantidad total de roles en el sistema (activos + inactivos)
- **Descripción:** Incluye roles SYSTEM y CUSTOM

#### Métrica 2: Total Permissions

- **Icono:** Key (llave cyan)
- **Número:** Cantidad total de permisos en el sistema (activos + inactivos)
- **Descripción:** Incluye permisos base + custom

#### Métrica 3: Active Users

- **Icono:** Users (usuarios verde)
- **Número:** Usuarios con al menos 1 rol activo y válido
- **Descripción:** Excluye usuarios sin roles o con roles expirados/revocados

#### Métrica 4: Expired Roles

- **Icono:** Exclamation Triangle (triángulo naranja)
- **Número:** Asignaciones expiradas pero aún `is_active=true` (necesitan cleanup)
- **Descripción:** Alerta de asignaciones que el job programado aún no procesó

**Colores:**

- Azul: Información neutral
- Verde: Estado positivo
- Naranja: Alerta (requiere atención)

### 6.3 Widget: Recent Activity

**Ubicación:** Panel izquierdo inferior

**Descripción:** Muestra las últimas 10 acciones de auditoría del sistema de autorización.

**Información mostrada por acción:**

- **Acción:** Tipo (CREATED, UPDATED, DELETED, PERMISSIONS_ADDED, etc.)
- **Rol:** Nombre del rol afectado
- **Usuario:** Quién realizó el cambio
- **Fecha:** Timestamp relativo (ej: "hace 2 horas")

**Badges de color por acción:**

- Verde: CREATED, ACTIVATED, PERMISSIONS_ADDED
- Azul: UPDATED, HIERARCHY_CHANGED
- Rojo: DELETED, DEACTIVATED, PERMISSIONS_REMOVED

**Uso:** Monitoreo rápido de actividad reciente del sistema de autorización.

### 6.4 Widget: Expiring Roles

**Ubicación:** Panel derecho superior

**Descripción:** Alertas de roles próximos a expirar en los próximos 7 días.

**Información mostrada por alerta:**

- **Usuario:** Login del usuario
- **Rol:** Nombre del rol que expira
- **Expira:** Tiempo restante (ej: "en 2 días", "mañana", "hoy")
- **Badge de urgencia:**
  - Rojo (danger): Expira hoy o mañana
  - Amarillo (warning): Expira en 2-3 días
  - Azul (info): Expira en 4-7 días

**Estados posibles:**

- **"No hay roles próximos a expirar"**: Badge verde, todo OK
- **Lista de alertas**: Ordenadas por urgencia (expiran primero arriba)

**Uso:** Permite extender asignaciones antes de que expiren o notificar a usuarios.

### 6.5 Gráfico: Top Authorities

**Ubicación:** Panel izquierdo inferior (bajo Recent Activity)

**Tipo:** Gráfico de barras horizontales

**Descripción:** Muestra los 10 roles más usados por cantidad de usuarios asignados.

**Datos mostrados:**

- **Eje X:** Cantidad de usuarios
- **Eje Y:** Nombre del rol
- **Barras:**
  - Azul: Usuarios con asignación activa
  - Gris: Usuarios con asignación expirada (incluido en total)

**Interactividad:**

- Hover sobre barra: Tooltip con cantidad exacta
- Click en barra: (no implementado en v1.0)

**Uso:** Identificar roles más populares y distribución de usuarios por rol.

### 6.6 Gráfico: Permission Usage

**Ubicación:** Panel derecho inferior

**Tipo:** Gráfico de dona (pie chart)

**Descripción:** Muestra los 10 permisos más usados en el sistema.

**Datos mostrados:**

- **Segmentos:** Cada permiso es un segmento coloreado
- **Porcentaje:** Uso relativo del permiso
- **Leyenda:** Nombre del permiso + cantidad de asignaciones

**Cálculo:** Suma de:

- Permisos asignados a roles (scr_authority_permission)
- Permisos directos a usuarios (scr_user_permission)

**Colores:** Paleta de colores automática (Recharts)

**Interactividad:**

- Hover sobre segmento: Tooltip con nombre y cantidad
- Click en leyenda: Ocultar/mostrar segmento

**Uso:** Identificar permisos más utilizados y detectar permisos sin uso (candidatos a eliminar).

### 6.7 Botón de Refresh Manual

**Ubicación:** Esquina superior derecha del dashboard

**Funcionalidad:**

- Click en el botón **"Refresh"** (icono de reload)
- Re-consulta todas las métricas al backend
- Muestra spinner durante carga
- Actualiza todos los widgets y gráficos

**Uso:** Obtener datos actualizados sin recargar la página (útil si otro admin hizo cambios).

### 6.8 Manejo de Errores

Si hay error al cargar métricas:

- Se muestra alerta roja con mensaje de error
- Los widgets muestran estado de loading indefinido
- Se puede reintentar con el botón de refresh

---

## 7. Auditoría y Reportes

### 7.1 ¿Qué se Audita?

El sistema registra automáticamente todas las operaciones en la tabla `scr_authority_audit`:

**Acciones auditadas:**

- CREATED: Rol creado
- UPDATED: Rol actualizado (name, description, hierarchy)
- DELETED: Rol eliminado
- ACTIVATED: Rol reactivado
- DEACTIVATED: Rol desactivado
- PERMISSIONS_ADDED: Permisos asignados al rol
- PERMISSIONS_REMOVED: Permisos removidos del rol
- HIERARCHY_CHANGED: Nivel de jerarquía cambiado

**Información capturada:**

- **Authority ID**: ID del rol afectado
- **Action**: Tipo de acción (enum)
- **Old Values**: Estado anterior (JSON)
- **New Values**: Estado nuevo (JSON)
- **Changed By**: Login del usuario que hizo el cambio
- **Changed Date**: Timestamp del cambio
- **IP Address**: IP del usuario (v4 o v6)
- **User Agent**: Navegador/cliente del usuario

### 7.2 Ver Historial de un Rol

**Desde la vista de detalle del rol** (`/admin/authority/{id}`):

1. Scroll a la sección **"Audit History"**
2. Ver lista de cambios ordenados por fecha (más recientes primero)

**Información mostrada por cada entrada:**

- Fecha y hora del cambio
- Usuario que hizo el cambio
- Tipo de acción (badge de color)
- Cambios realizados:
  - **Campo:** nombre del campo modificado
  - **Anterior:** valor anterior
  - **Nuevo:** valor nuevo

**Ejemplo de entrada:**

```
2025-11-07 10:30 AM - admin - UPDATED
  description: "Regular user" → "Standard user with basic permissions"

2025-11-06 02:15 PM - super_admin - PERMISSIONS_ADDED
  Permisos agregados: [user.read, user.update]
```

### 7.3 Interpretar Old Values y New Values

Los valores se almacenan como JSON en la base de datos.

**Ejemplo de UPDATED:**

```json
old_values: {"description": "Regular user", "hierarchyLevel": 500}
new_values: {"description": "Standard user", "hierarchyLevel": 400}
```

**Interpretación:**

- Se cambió la descripción de "Regular user" a "Standard user"
- Se cambió el hierarchy level de 500 a 400

**Ejemplo de PERMISSIONS_ADDED:**

```json
old_values: null
new_values: {"permissions": [{"id": 1, "name": "user.read"}, {"id": 2, "name": "user.update"}]}
```

**Interpretación:**

- Se agregaron 2 permisos al rol: user.read y user.update

### 7.4 Filtrar Auditoría

**(Funcionalidad en desarrollo - Fase 12 pendiente)**

**Filtros planeados:**

- Por rango de fechas (from-to)
- Por usuario que hizo el cambio
- Por tipo de acción (dropdown)
- Por rol específico (search)

### 7.5 Exportar Logs de Auditoría

**(Funcionalidad en desarrollo - Fase 12 pendiente)**

**Formatos planeados:**

- CSV: Para análisis en Excel
- JSON: Para procesamiento programático

**Límite:** 10,000 registros por exportación (prevenir OOM)

**Campos exportados (CSV):**

```
ID, Authority ID, Authority Code, Action Type, Changed By, Changed Date, IP Address, Old Values, New Values
```

---

## 8. Casos de Uso Comunes

### 8.1 Caso 1: Crear Rol "Manager" con Permisos Específicos

**Escenario:** Necesitas crear un rol para gerentes que puedan gestionar equipos pero no modificar roles.

**Pasos:**

1. **Crear el rol:**

   - Ir a `/admin/authority`
   - Click "Create new Authority"
   - Name: "Manager"
   - Code: "ROLE_MANAGER"
   - Description: "Can manage teams and view reports"
   - Category: CUSTOM
   - Hierarchy Level: 100
   - Guardar

2. **Asignar permisos:**

   - Ir a `/admin/authority/{id}` (detalle del rol)
   - Click "Add Permission"
   - Seleccionar:
     - `user.read` (ver usuarios)
     - `user.update` (modificar usuarios de su equipo)
     - `report.read` (ver reportes)
     - `report.export` (exportar reportes)
   - Guardar

3. **Asignar rol a usuarios:**
   - Ir a `/admin/user-management/{userId}` (cada gerente)
   - Click "Assign Authority"
   - Seleccionar "ROLE_MANAGER"
   - No poner fecha de expiración (permanente)
   - Asignar

**Resultado:** Los gerentes pueden ver y modificar usuarios, ver y exportar reportes, pero NO pueden crear/eliminar usuarios ni modificar roles.

### 8.2 Caso 2: Dar Acceso Temporal a Contratista (30 días)

**Escenario:** Un contratista externo necesita acceso temporal al sistema por 30 días para un proyecto específico.

**Pasos:**

1. **Crear el usuario** (si no existe):

   - Ir a `/admin/user-management`
   - Click "Create new User"
   - Completar datos básicos del contratista
   - Guardar

2. **Asignar rol temporal:**

   - Ir a `/admin/user-management/{userId}`
   - Click "Assign Authority"
   - Seleccionar rol apropiado (ej: ROLE_USER o un rol custom)
   - **Expires At:** Seleccionar fecha 30 días en el futuro
   - Asignar

3. **Verificar asignación:**
   - En la lista de roles del usuario, ver badge verde "Activo"
   - Ver fecha de expiración mostrada (ej: "Expires: 2025-12-07")
   - Aparecerá en el dashboard de alertas 7 días antes de expirar

**Resultado:** El contratista tiene acceso durante 30 días. Al expirar:

- El badge cambia a rojo "Expirado"
- Pierde acceso automáticamente (validación en backend)
- El job programado lo marca como `is_active=false` a las 2 AM del día siguiente

### 8.3 Caso 3: Revocar Acceso Inmediato por Seguridad

**Escenario:** Un usuario reportó su cuenta comprometida. Necesitas revocar todos sus accesos inmediatamente.

**Pasos:**

1. **Ir al perfil del usuario:**

   - `/admin/user-management/{userId}`

2. **Desactivar el usuario:**

   - Click "Edit"
   - Desmarcar "Activated"
   - Guardar
   - (Esto bloquea el login pero no revoca roles)

3. **Revocar todos los roles activos:**

   - En la sección "Authorities"
   - Para cada rol activo:
     - Click "Revoke"
     - Reason: "Cuenta comprometida - reportado el 2025-11-07 a las 3 PM"
     - Confirmar

4. **Revocar permisos directos (si tiene):**
   - En la sección "Direct Permissions"
   - Para cada permiso activo:
     - Click "Revoke"
     - Reason: "Cuenta comprometida"
     - Confirmar

**Resultado:**

- El usuario no puede hacer login (desactivado)
- Todos los tokens JWT existentes son inválidos para roles (revocados)
- Queda trazabilidad de la revocación con razón y timestamp

### 8.4 Caso 4: Auditar Quién Cambió Permisos de un Rol

**Escenario:** El rol ROLE_MANAGER perdió un permiso crítico. Necesitas saber quién lo modificó y cuándo.

**Pasos:**

1. **Ir al detalle del rol:**

   - `/admin/authority/{id}` (ROLE_MANAGER)

2. **Ver historial de auditoría:**

   - Scroll a sección "Audit History"
   - Buscar entradas con action=PERMISSIONS_REMOVED

3. **Analizar la entrada encontrada:**

   ```
   2025-11-05 04:30 PM - super_admin - PERMISSIONS_REMOVED
   IP: 192.168.1.50
   User Agent: Mozilla/5.0 ...
   Permisos removidos: [user.delete]
   ```

4. **Acciones siguientes:**
   - Contactar a "super_admin" para entender la razón
   - Si fue error, re-asignar el permiso
   - Si fue intencional, documentar la decisión

**Resultado:** Identificaste quién, cuándo, desde dónde (IP) y qué cambió exactamente.

### 8.5 Caso 5: Crear Permiso Temporal de Emergencia

**Escenario:** Un usuario necesita urgentemente exportar datos para una auditoría externa, pero no tiene el permiso y es viernes a las 5 PM (no hay tiempo de crear rol).

**Pasos:**

1. **Ir al perfil del usuario:**

   - `/admin/user-management/{userId}`

2. **Asignar permiso directo:**
   - En sección "Direct Permissions"
   - Click "Grant Direct Permission"
   - Permission: `report.export`
   - Expires At: 2025-11-08 (lunes próximo)
   - Reason: "Auditoría externa urgente - solicitud aprobada por CFO vía email del 2025-11-07"
   - Grant

**Resultado:**

- El usuario puede exportar reportes inmediatamente
- El permiso expira automáticamente el lunes (no queda "zombie")
- La razón queda registrada para justificar el grant excepcional

**Buena práctica:** El lunes, revisar si es necesario extender o si fue caso único.

### 8.6 Caso 6: Migrar Configuración de Roles de Dev a Prod

**Escenario:** Creaste varios roles custom en desarrollo y necesitas replicarlos en producción.

**Pasos (manual en v1.0):**

1. **Exportar roles desde Dev:**

   - Conectarse a BD de dev:
     ```bash
     psql -h localhost -U postgres -d tysescrutinygateway
     ```
   - Exportar tabla de roles:
     ```sql
     COPY (SELECT * FROM scr_authority WHERE category='CUSTOM')
     TO '/tmp/roles_export.csv'
     WITH CSV HEADER;
     ```
   - Exportar permisos asignados:
     ```sql
     COPY (SELECT ap.* FROM scr_authority_permission ap
           JOIN scr_authority a ON ap.authority_id = a.id
           WHERE a.category='CUSTOM')
     TO '/tmp/authority_permissions_export.csv'
     WITH CSV HEADER;
     ```

2. **Importar a Prod:**
   - Conectarse a BD de prod
   - Importar roles:
     ```sql
     COPY scr_authority FROM '/tmp/roles_export.csv' WITH CSV HEADER;
     ```
   - Importar permisos:
     ```sql
     COPY scr_authority_permission FROM '/tmp/authority_permissions_export.csv' WITH CSV HEADER;
     ```

**Resultado:** Los roles custom de dev ahora existen en prod con los mismos IDs y permisos.

**Notas:**

- Asegúrate que los IDs de permisos coinciden entre ambientes
- Los roles SYSTEM no deben exportarse (ya existen en seed data)
- Futura feature: Endpoint de exportación/importación automático

---

## Apéndice A: Glosario

| Término             | Definición                                                          |
| ------------------- | ------------------------------------------------------------------- |
| **Authority**       | Rol del sistema. Conjunto de permisos agrupados bajo un nombre.     |
| **Permission**      | Permiso granular sobre un recurso y acción. Patrón: resource.action |
| **User Authority**  | Asignación de un rol a un usuario. Puede ser temporal o permanente. |
| **User Permission** | Permiso directo asignado a un usuario sin pasar por roles.          |
| **Hierarchy Level** | Nivel de jerarquía de un rol (0=máximo, 999=mínimo).                |
| **Expired**         | Asignación cuya fecha de expiración ya pasó.                        |
| **Revoked**         | Asignación cancelada manualmente por un administrador.              |
| **System Role**     | Rol protegido que no puede modificarse (ROLE_ADMIN, ROLE_USER).     |
| **RBAC**            | Role-Based Access Control. Patrón de autorización basado en roles.  |
| **Audit**           | Registro de cambios realizados en el sistema de autorización.       |

---

## Apéndice B: Atajos de Teclado

| Atajo            | Acción                               |
| ---------------- | ------------------------------------ |
| `/admin` + Enter | Ir al menú de administración         |
| `Ctrl + K`       | Búsqueda global (si está habilitado) |

---

## Apéndice C: Contacto y Soporte

**Para reportar problemas:**

- Crear issue en repositorio del proyecto
- Contactar al equipo de desarrollo

**Documentación adicional:**

- Guía de Operaciones (para SysOps)
- API Reference (para desarrolladores)
- FAQ y Troubleshooting

---

**Fin del Manual de Usuario**
