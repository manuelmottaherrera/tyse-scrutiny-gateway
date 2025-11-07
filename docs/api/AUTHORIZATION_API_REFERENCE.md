# API Reference - Sistema de Autorización Enterprise

**Versión:** 1.0
**Fecha:** 2025-11-07
**Base URL:** `http://localhost:8080`
**Producción:** `https://tyse.example.com`

---

## Tabla de Contenidos

1. [Autenticación](#1-autenticación)
2. [Authorities API](#2-authorities-api)
3. [Permissions API](#3-permissions-api)
4. [User Authorities API](#4-user-authorities-api)
5. [User Permissions API](#5-user-permissions-api)
6. [Authority Permissions API](#6-authority-permissions-api)
7. [Dashboard API](#7-dashboard-api)
8. [Modelos de Datos](#8-modelos-de-datos)
9. [Códigos de Error](#9-códigos-de-error)

---

## 1. Autenticación

Todos los endpoints de autorización requieren autenticación JWT.

### Obtener Token JWT

**Endpoint:** `POST /api/authenticate`

**Request Body:**

```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response (200 OK):**

```json
{
  "id_token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Uso del Token:**
Incluir en header `Authorization` de todas las peticiones:

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Expiración:** 24 horas (configurable en `application.yml`)

---

## 2. Authorities API

Gestión de roles (authorities).

### 2.1 Listar Todos los Roles

**Endpoint:** `GET /api/authorities`

**Seguridad:** Requiere `ROLE_ADMIN`

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "name": "Administrator",
    "code": "ROLE_ADMIN",
    "description": "Full system administration privileges",
    "category": "SYSTEM",
    "isSystem": true,
    "isActive": true,
    "hierarchyLevel": 0,
    "createdBy": "system",
    "createdDate": "2025-01-01T00:00:00Z",
    "lastModifiedBy": null,
    "lastModifiedDate": null
  },
  {
    "id": 3,
    "name": "Manager",
    "code": "ROLE_MANAGER",
    "description": "Can manage teams and view reports",
    "category": "CUSTOM",
    "isSystem": false,
    "isActive": true,
    "hierarchyLevel": 100,
    "createdBy": "admin",
    "createdDate": "2025-11-05T10:30:00Z",
    "lastModifiedBy": "admin",
    "lastModifiedDate": "2025-11-06T14:20:00Z"
  }
]
```

**Response Headers:**

```http
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
```

**Curl Example:**

```bash
curl -X GET "http://localhost:8080/api/authorities" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

---

### 2.2 Obtener Rol por ID

**Endpoint:** `GET /api/authorities/{id}`

**Path Parameters:**

- `id` (Long, required): ID del rol

**Response (200 OK):**

```json
{
  "id": 3,
  "name": "Manager",
  "code": "ROLE_MANAGER",
  "description": "Can manage teams and view reports",
  "category": "CUSTOM",
  "isSystem": false,
  "isActive": true,
  "hierarchyLevel": 100,
  "createdBy": "admin",
  "createdDate": "2025-11-05T10:30:00Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2025-11-06T14:20:00Z"
}
```

**Response (404 Not Found):**

```json
{
  "error": "Not Found",
  "message": "Authority not found with id: 999",
  "path": "/api/authorities/999",
  "status": 404,
  "timestamp": "2025-11-07T10:00:00Z"
}
```

---

### 2.3 Crear Rol

**Endpoint:** `POST /api/authorities`

**Request Body:**

```json
{
  "name": "Supervisor",
  "code": "ROLE_SUPERVISOR",
  "description": "Supervise operations and generate reports",
  "category": "CUSTOM",
  "isActive": true,
  "hierarchyLevel": 200
}
```

**Validaciones:**

- `name`: No puede estar vacío, máximo 100 caracteres
- `code`: Debe seguir patrón `ROLE_*`, único, máximo 50 caracteres
- `category`: Debe ser uno de: SYSTEM, CUSTOM, TENANT_SPECIFIC
- `hierarchyLevel`: Entre 0 y 999

**Response (201 Created):**

```json
{
  "id": 4,
  "name": "Supervisor",
  "code": "ROLE_SUPERVISOR",
  "description": "Supervise operations and generate reports",
  "category": "CUSTOM",
  "isSystem": false,
  "isActive": true,
  "hierarchyLevel": 200,
  "createdBy": "admin",
  "createdDate": "2025-11-07T10:30:00Z",
  "lastModifiedBy": null,
  "lastModifiedDate": null
}
```

**Response (400 Bad Request):**

```json
{
  "error": "Bad Request",
  "message": "Code must start with 'ROLE_'",
  "path": "/api/authorities",
  "status": 400,
  "timestamp": "2025-11-07T10:30:00Z"
}
```

**Response (409 Conflict):**

```json
{
  "error": "Conflict",
  "message": "Authority with code 'ROLE_SUPERVISOR' already exists",
  "path": "/api/authorities",
  "status": 409,
  "timestamp": "2025-11-07T10:30:00Z"
}
```

**Curl Example:**

```bash
curl -X POST "http://localhost:8080/api/authorities" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Supervisor",
    "code": "ROLE_SUPERVISOR",
    "description": "Supervise operations",
    "category": "CUSTOM",
    "isActive": true,
    "hierarchyLevel": 200
  }'
```

---

### 2.4 Actualizar Rol

**Endpoint:** `PUT /api/authorities/{id}`

**Path Parameters:**

- `id` (Long, required): ID del rol

**Request Body:**

```json
{
  "name": "Manager",
  "description": "Updated description: can manage teams, view reports and approve budgets",
  "hierarchyLevel": 150,
  "isActive": true
}
```

**Campos NO editables:**

- `code`: Se define al crear, no se puede cambiar
- `category`: Se define al crear
- `isSystem`: Campo de sistema

**Restricciones:**

- NO puede editar roles con `isSystem=true` (ROLE_ADMIN, ROLE_USER)

**Response (200 OK):**

```json
{
  "id": 3,
  "name": "Manager",
  "code": "ROLE_MANAGER",
  "description": "Updated description: can manage teams, view reports and approve budgets",
  "category": "CUSTOM",
  "isSystem": false,
  "isActive": true,
  "hierarchyLevel": 150,
  "createdBy": "admin",
  "createdDate": "2025-11-05T10:30:00Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2025-11-07T11:00:00Z"
}
```

**Response (403 Forbidden):**

```json
{
  "error": "Forbidden",
  "message": "Cannot modify system authority: ROLE_ADMIN",
  "path": "/api/authorities/1",
  "status": 403,
  "timestamp": "2025-11-07T11:00:00Z"
}
```

---

### 2.5 Eliminar Rol

**Endpoint:** `DELETE /api/authorities/{id}`

**Path Parameters:**

- `id` (Long, required): ID del rol

**Restricciones:**

- NO puede eliminar roles con `isSystem=true`
- NO puede eliminar roles con usuarios asignados activos

**Response (204 No Content):**
Sin body (rol eliminado exitosamente)

**Response (403 Forbidden):**

```json
{
  "error": "Forbidden",
  "message": "Cannot delete system authority",
  "path": "/api/authorities/1",
  "status": 403,
  "timestamp": "2025-11-07T11:30:00Z"
}
```

**Response (409 Conflict):**

```json
{
  "error": "Conflict",
  "message": "Cannot delete authority with active user assignments. Revoke all assignments first or deactivate the authority.",
  "path": "/api/authorities/3",
  "status": 409,
  "timestamp": "2025-11-07T11:30:00Z"
}
```

**Curl Example:**

```bash
curl -X DELETE "http://localhost:8080/api/authorities/4" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 3. Permissions API

Gestión de permisos granulares.

### 3.1 Listar Todos los Permisos

**Endpoint:** `GET /api/permissions`

**Query Parameters:**

- `resource` (String, optional): Filtrar por recurso (ej: "user")
- `isActive` (Boolean, optional): Filtrar por activos (default: true)

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "name": "user.create",
    "resource": "user",
    "action": "create",
    "description": "Create new users in the system",
    "isActive": true,
    "createdBy": "system",
    "createdDate": "2025-01-01T00:00:00Z"
  },
  {
    "id": 5,
    "name": "authority.create",
    "resource": "authority",
    "action": "create",
    "description": "Create new roles",
    "isActive": true,
    "createdBy": "system",
    "createdDate": "2025-01-01T00:00:00Z"
  }
]
```

**Curl Example:**

```bash
curl -X GET "http://localhost:8080/api/permissions?resource=user&isActive=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.2 Obtener Permiso por ID

**Endpoint:** `GET /api/permissions/{id}`

**Response (200 OK):**

```json
{
  "id": 1,
  "name": "user.create",
  "resource": "user",
  "action": "create",
  "description": "Create new users in the system",
  "isActive": true,
  "createdBy": "system",
  "createdDate": "2025-01-01T00:00:00Z"
}
```

---

### 3.3 Crear Permiso

**Endpoint:** `POST /api/permissions`

**Request Body:**

```json
{
  "resource": "report",
  "action": "export",
  "description": "Export reports to CSV or PDF",
  "isActive": true
}
```

**Validaciones:**

- `resource`: No vacío, solo letras minúsculas, números y guiones bajos
- `action`: No vacío, solo letras minúsculas
- `name`: Auto-generado como `{resource}.{action}`
- Combinación `resource + action` debe ser única

**Response (201 Created):**

```json
{
  "id": 14,
  "name": "report.export",
  "resource": "report",
  "action": "export",
  "description": "Export reports to CSV or PDF",
  "isActive": true,
  "createdBy": "admin",
  "createdDate": "2025-11-07T12:00:00Z"
}
```

**Response (409 Conflict):**

```json
{
  "error": "Conflict",
  "message": "Permission with name 'report.export' already exists",
  "path": "/api/permissions",
  "status": 409
}
```

---

### 3.4 Actualizar Permiso

**Endpoint:** `PUT /api/permissions/{id}`

**Request Body:**

```json
{
  "description": "Updated: Export reports to CSV, PDF or Excel",
  "isActive": true
}
```

**Campos editables:**

- `description`
- `isActive`

**Campos NO editables:**

- `name`, `resource`, `action` (se definen al crear)

**Response (200 OK):**

```json
{
  "id": 14,
  "name": "report.export",
  "resource": "report",
  "action": "export",
  "description": "Updated: Export reports to CSV, PDF or Excel",
  "isActive": true,
  "createdBy": "admin",
  "createdDate": "2025-11-07T12:00:00Z"
}
```

---

## 4. User Authorities API

Asignación y revocación de roles a usuarios.

### 4.1 Listar Roles de un Usuario

**Endpoint:** `GET /api/user-authorities/user/{userId}`

**Path Parameters:**

- `userId` (Long, required): ID del usuario

**Response (200 OK):**

```json
[
  {
    "id": 100,
    "userId": 123,
    "authorityId": 2,
    "authorityCode": "ROLE_USER",
    "authorityName": "User",
    "assignedBy": "admin",
    "assignedDate": "2025-11-01T10:00:00Z",
    "expiresAt": null,
    "isActive": true,
    "isExpired": false,
    "revokedBy": null,
    "revokedDate": null,
    "revokedReason": null
  },
  {
    "id": 101,
    "userId": 123,
    "authorityId": 3,
    "authorityCode": "ROLE_MANAGER",
    "authorityName": "Manager",
    "assignedBy": "admin",
    "assignedDate": "2025-11-05T14:30:00Z",
    "expiresAt": "2025-12-05T14:30:00Z",
    "isActive": true,
    "isExpired": false,
    "revokedBy": null,
    "revokedDate": null,
    "revokedReason": null
  },
  {
    "id": 99,
    "userId": 123,
    "authorityId": 4,
    "authorityCode": "ROLE_TEMP",
    "authorityName": "Temporary Role",
    "assignedBy": "admin",
    "assignedDate": "2025-10-01T10:00:00Z",
    "expiresAt": "2025-11-01T10:00:00Z",
    "isActive": false,
    "isExpired": true,
    "revokedBy": "system",
    "revokedDate": "2025-11-02T02:00:00Z",
    "revokedReason": "Automatic cleanup - expired"
  }
]
```

**Estados:**

- `isActive=true, isExpired=false`: Rol activo y válido
- `isActive=true, isExpired=true`: Rol expirado (pendiente de cleanup)
- `isActive=false, revokedBy!=null`: Rol revocado manualmente
- `isActive=false, isExpired=true`: Rol expirado y procesado por job

---

### 4.2 Listar Solo Roles Válidos de un Usuario

**Endpoint:** `GET /api/user-authorities/user/{userId}/valid`

**Descripción:** Retorna solo asignaciones activas y no expiradas.

**Response (200 OK):**

```json
[
  {
    "id": 100,
    "userId": 123,
    "authorityCode": "ROLE_USER",
    "authorityName": "User",
    "assignedDate": "2025-11-01T10:00:00Z",
    "expiresAt": null,
    "isActive": true,
    "isExpired": false
  },
  {
    "id": 101,
    "userId": 123,
    "authorityCode": "ROLE_MANAGER",
    "authorityName": "Manager",
    "assignedDate": "2025-11-05T14:30:00Z",
    "expiresAt": "2025-12-05T14:30:00Z",
    "isActive": true,
    "isExpired": false
  }
]
```

---

### 4.3 Asignar Rol a Usuario

**Endpoint:** `POST /api/user-authorities`

**Request Body (Rol permanente):**

```json
{
  "userId": 123,
  "authorityId": 3,
  "expiresAt": null
}
```

**Request Body (Rol temporal - 30 días):**

```json
{
  "userId": 123,
  "authorityId": 5,
  "expiresAt": "2025-12-07T23:59:59Z"
}
```

**Validaciones:**

- `userId`: Debe existir en `scr_user`
- `authorityId`: Debe existir en `scr_authority` y estar activo
- `expiresAt`: Si presente, debe ser fecha futura
- Combinación `userId + authorityId` debe ser única (no asignación duplicada)

**Response (201 Created):**

```json
{
  "id": 102,
  "userId": 123,
  "authorityId": 5,
  "authorityCode": "ROLE_AUDITOR",
  "authorityName": "Auditor",
  "assignedBy": "admin",
  "assignedDate": "2025-11-07T13:00:00Z",
  "expiresAt": "2025-12-07T23:59:59Z",
  "isActive": true,
  "isExpired": false,
  "revokedBy": null,
  "revokedDate": null,
  "revokedReason": null
}
```

**Response (409 Conflict):**

```json
{
  "error": "Conflict",
  "message": "User already has this authority assigned",
  "path": "/api/user-authorities",
  "status": 409
}
```

**Curl Example:**

```bash
curl -X POST "http://localhost:8080/api/user-authorities" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "authorityId": 3,
    "expiresAt": "2025-12-31T23:59:59Z"
  }'
```

---

### 4.4 Revocar Rol

**Endpoint:** `DELETE /api/user-authorities/{id}`

**Path Parameters:**

- `id` (Long, required): ID de la asignación (scr_user_authority.id)

**Request Body:**

```json
{
  "reason": "El usuario cambió de departamento y ya no requiere este rol"
}
```

**Validaciones:**

- `reason`: Obligatorio, mínimo 10 caracteres

**Response (204 No Content):**
Sin body (rol revocado exitosamente)

**Response (400 Bad Request):**

```json
{
  "error": "Bad Request",
  "message": "Revocation reason is required (minimum 10 characters)",
  "path": "/api/user-authorities/102",
  "status": 400
}
```

**Response (404 Not Found):**

```json
{
  "error": "Not Found",
  "message": "User authority assignment not found with id: 999",
  "path": "/api/user-authorities/999",
  "status": 404
}
```

**Curl Example:**

```bash
curl -X DELETE "http://localhost:8080/api/user-authorities/102" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "El usuario cambió de departamento"
  }'
```

---

### 4.5 Listar Roles Próximos a Expirar

**Endpoint:** `GET /api/user-authorities/expiring`

**Query Parameters:**

- `days` (Integer, optional): Días hacia adelante (default: 7)

**Response (200 OK):**

```json
[
  {
    "id": 103,
    "userId": 45,
    "userLogin": "john.doe",
    "authorityId": 5,
    "authorityCode": "ROLE_AUDITOR",
    "authorityName": "Auditor",
    "expiresAt": "2025-11-09T10:00:00Z",
    "daysUntilExpiration": 2,
    "isActive": true
  },
  {
    "id": 104,
    "userId": 78,
    "userLogin": "jane.smith",
    "authorityId": 3,
    "authorityCode": "ROLE_MANAGER",
    "authorityName": "Manager",
    "expiresAt": "2025-11-12T23:59:59Z",
    "daysUntilExpiration": 5,
    "isActive": true
  }
]
```

**Uso:** Para generar alertas de expiración en el dashboard.

---

## 5. User Permissions API

Permisos directos a usuarios (bypass de roles).

### 5.1 Listar Permisos Directos de un Usuario

**Endpoint:** `GET /api/user-permissions/user/{userId}`

**Response (200 OK):**

```json
[
  {
    "id": 50,
    "userId": 123,
    "permissionId": 14,
    "permissionName": "report.export",
    "grantedBy": "admin",
    "grantedDate": "2025-11-07T10:00:00Z",
    "expiresAt": "2025-11-14T10:00:00Z",
    "reason": "Necesita exportar reportes del Q4 para auditoría externa",
    "isActive": true,
    "revokedBy": null,
    "revokedDate": null,
    "revokedReason": null
  }
]
```

---

### 5.2 Asignar Permiso Directo

**Endpoint:** `POST /api/user-permissions`

**Request Body:**

```json
{
  "userId": 123,
  "permissionId": 14,
  "expiresAt": "2025-11-14T23:59:59Z",
  "reason": "Necesita exportar reportes del Q4 para auditoría externa. Aprobado por CFO vía email."
}
```

**Validaciones:**

- `reason`: Obligatorio, mínimo 10 caracteres
- `expiresAt`: Recomendado (prevenir permisos "zombie")

**Response (201 Created):**

```json
{
  "id": 51,
  "userId": 123,
  "permissionId": 14,
  "permissionName": "report.export",
  "grantedBy": "admin",
  "grantedDate": "2025-11-07T14:00:00Z",
  "expiresAt": "2025-11-14T23:59:59Z",
  "reason": "Necesita exportar reportes del Q4 para auditoría externa. Aprobado por CFO vía email.",
  "isActive": true,
  "revokedBy": null,
  "revokedDate": null,
  "revokedReason": null
}
```

---

### 5.3 Revocar Permiso Directo

**Endpoint:** `DELETE /api/user-permissions/{id}`

**Request Body (opcional):**

```json
{
  "reason": "Auditoría completada, ya no necesita el permiso"
}
```

**Response (204 No Content):**
Sin body (permiso revocado exitosamente)

---

## 6. Authority Permissions API

Asignación de permisos a roles.

### 6.1 Listar Permisos de un Rol

**Endpoint:** `GET /api/authority-permissions/authority/{authorityId}`

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "permissionId": 1,
    "permissionName": "user.create",
    "grantedBy": "system",
    "grantedDate": "2025-01-01T00:00:00Z"
  },
  {
    "id": 2,
    "permissionId": 2,
    "permissionName": "user.read",
    "grantedBy": "system",
    "grantedDate": "2025-01-01T00:00:00Z"
  }
]
```

---

### 6.2 Asignar Permiso a Rol

**Endpoint:** `POST /api/authority-permissions`

**Request Body:**

```json
{
  "authorityId": 3,
  "permissionId": 14
}
```

**Response (201 Created):**

```json
{
  "id": 50,
  "authorityId": 3,
  "permissionId": 14,
  "permissionName": "report.export",
  "grantedBy": "admin",
  "grantedDate": "2025-11-07T15:00:00Z"
}
```

**Response (409 Conflict):**

```json
{
  "error": "Conflict",
  "message": "Authority already has this permission",
  "status": 409
}
```

---

### 6.3 Remover Permiso de Rol

**Endpoint:** `DELETE /api/authority-permissions/authority/{authorityId}/permission/{permissionId}`

**Path Parameters:**

- `authorityId` (Long, required)
- `permissionId` (Long, required)

**Response (204 No Content):**
Sin body (permiso removido exitosamente)

---

## 7. Dashboard API

Métricas del sistema de autorización.

### 7.1 Obtener Métricas del Dashboard

**Endpoint:** `GET /api/authorization/dashboard/metrics`

**Response (200 OK):**

```json
{
  "totalAuthorities": 15,
  "totalPermissions": 87,
  "activeUsers": 1234,
  "expiredRoles": 3,
  "recentActivity": [
    {
      "id": 789,
      "authorityId": 3,
      "authorityCode": "ROLE_MANAGER",
      "action": "PERMISSIONS_ADDED",
      "changedBy": "admin",
      "changedDate": "2025-11-07T14:30:00Z"
    },
    {
      "id": 788,
      "authorityId": 5,
      "authorityCode": "ROLE_AUDITOR",
      "action": "CREATED",
      "changedBy": "super_admin",
      "changedDate": "2025-11-07T10:00:00Z"
    }
  ],
  "expiringRoles": [
    {
      "userId": 45,
      "userLogin": "john.doe",
      "authorityId": 5,
      "authorityCode": "ROLE_AUDITOR",
      "expiresAt": "2025-11-09T10:00:00Z",
      "daysUntilExpiration": 2
    }
  ],
  "topAuthorities": [
    {
      "authorityId": 2,
      "authorityCode": "ROLE_USER",
      "authorityName": "User",
      "totalAssignments": 987,
      "activeAssignments": 985,
      "expiredAssignments": 2
    },
    {
      "authorityId": 1,
      "authorityCode": "ROLE_ADMIN",
      "authorityName": "Administrator",
      "totalAssignments": 25,
      "activeAssignments": 25,
      "expiredAssignments": 0
    }
  ],
  "permissionUsage": [
    {
      "permissionId": 2,
      "permissionName": "user.read",
      "totalUsage": 1015,
      "usageViaRoles": 1000,
      "usageViaDirect": 15
    },
    {
      "permissionId": 1,
      "permissionName": "user.create",
      "totalUsage": 50,
      "usageViaRoles": 45,
      "usageViaDirect": 5
    }
  ]
}
```

**Descripción de campos:**

| Campo              | Descripción                                                              |
| ------------------ | ------------------------------------------------------------------------ |
| `totalAuthorities` | Cantidad total de roles (activos + inactivos)                            |
| `totalPermissions` | Cantidad total de permisos                                               |
| `activeUsers`      | Usuarios con al menos 1 rol activo y válido                              |
| `expiredRoles`     | Asignaciones expiradas pero aún `is_active=true` (pendientes de cleanup) |
| `recentActivity`   | Últimas 10 acciones de auditoría                                         |
| `expiringRoles`    | Roles que expirarán en próximos 7 días                                   |
| `topAuthorities`   | Top 10 roles por cantidad de usuarios                                    |
| `permissionUsage`  | Top 10 permisos más usados                                               |

---

## 8. Modelos de Datos

### AuthorityDTO

```typescript
{
  id: number;                    // ID único
  name: string;                  // Nombre descriptivo (ej: "Administrator")
  code: string;                  // Código técnico (ej: "ROLE_ADMIN")
  description?: string;          // Descripción opcional
  category: string;              // "SYSTEM" | "CUSTOM" | "TENANT_SPECIFIC"
  isSystem: boolean;             // true = no modificable
  isActive: boolean;             // false = soft delete
  hierarchyLevel: number;        // 0 (máximo) a 999 (mínimo)
  createdBy?: string;            // Usuario que creó
  createdDate?: string;          // ISO 8601 timestamp
  lastModifiedBy?: string;       // Último modificador
  lastModifiedDate?: string;     // ISO 8601 timestamp
}
```

### PermissionDTO

```typescript
{
  id: number;
  name: string;                  // Patrón: "resource.action"
  resource: string;              // Recurso (ej: "user", "report")
  action: string;                // Acción (ej: "create", "export")
  description?: string;
  isActive: boolean;
  createdBy?: string;
  createdDate?: string;
}
```

### UserAuthorityDTO

```typescript
{
  id: number;
  userId: number;
  authorityId: number;
  authorityCode: string;         // Ej: "ROLE_MANAGER"
  authorityName: string;         // Ej: "Manager"
  assignedBy: string;
  assignedDate: string;          // ISO 8601
  expiresAt?: string;            // ISO 8601, null = permanente
  isActive: boolean;
  isExpired: boolean;            // Computed: expiresAt <= now
  revokedBy?: string;
  revokedDate?: string;
  revokedReason?: string;
}
```

### DashboardMetricsDTO

```typescript
{
  totalAuthorities: number;
  totalPermissions: number;
  activeUsers: number;
  expiredRoles: number;
  recentActivity: RecentActivityDTO[];
  expiringRoles: ExpiringRoleDTO[];
  topAuthorities: AuthorityUsageDTO[];
  permissionUsage: PermissionUsageDTO[];
}
```

---

## 9. Códigos de Error

| Código  | Descripción           | Ejemplo                                                |
| ------- | --------------------- | ------------------------------------------------------ |
| **200** | OK                    | Request exitoso (GET, PUT)                             |
| **201** | Created               | Recurso creado (POST)                                  |
| **204** | No Content            | Recurso eliminado (DELETE)                             |
| **400** | Bad Request           | Validación fallida, parámetros incorrectos             |
| **401** | Unauthorized          | JWT inválido, expirado o ausente                       |
| **403** | Forbidden             | Usuario no tiene permiso (no ROLE_ADMIN)               |
| **404** | Not Found             | Recurso no encontrado                                  |
| **409** | Conflict              | Duplicado (código de rol, combinación resource+action) |
| **500** | Internal Server Error | Error interno del servidor                             |

### Estructura de Error

```json
{
  "error": "Forbidden",
  "message": "User does not have ROLE_ADMIN",
  "path": "/api/authorities",
  "status": 403,
  "timestamp": "2025-11-07T15:00:00Z"
}
```

---

## Apéndice A: Rate Limiting

**Límites actuales:** No implementados en v1.0

**Recomendación para producción:**

- 100 requests/minuto por usuario
- 1000 requests/minuto global

---

## Apéndice B: Paginación

**Endpoints que soportan paginación:**

- `GET /api/authorities` (próxima versión)
- `GET /api/permissions` (próxima versión)

**Parámetros:**

- `page` (Integer, default: 0)
- `size` (Integer, default: 20, max: 100)
- `sort` (String, ej: "name,asc")

---

## Apéndice C: Versionado de API

**Versión actual:** v1 (implícita en `/api/*`)

**Estrategia de versionado:** URL path (futuro: `/api/v2/authorities`)

---

**Fin de la API Reference**
