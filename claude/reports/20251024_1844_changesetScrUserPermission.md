# Reporte: CHANGESET 5 - scr_user_permission

**Fecha:** 2025-10-24 18:44
**Fase:** FASE 1 - Diseño de Base de Datos
**Paso:** 1.6 - Implementar CHANGESET 5
**Propósito:** Tabla de permisos directos a usuarios (bypass de roles)

---

## 📋 Tabla Creada: scr_user_permission

### Estructura (12 campos)

**Primary Key:**

- `id` - BIGINT auto-increment

**Foreign Keys:**

- `user_id` - BIGINT NOT NULL (→ scr_user.id)
- `permission_id` - BIGINT NOT NULL (→ scr_permission.id)

**Temporal Permission:**

- `expires_at` - TIMESTAMP NULL

**Status:**

- `is_active` - BOOLEAN NOT NULL DEFAULT true

**Grant Audit:**

- `granted_by` - VARCHAR(50) NOT NULL
- `granted_date` - TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- `reason` - VARCHAR(500) NULL (justificación del permiso especial)

**Revocation Audit:**

- `revoked_by` - VARCHAR(50) NULL
- `revoked_date` - TIMESTAMP NULL
- `revoked_reason` - VARCHAR(500) NULL

---

## 🎯 Constraints e Índices

### Constraints

- **PK:** `pk_scr_user_permission` en `id`
- **UNIQUE:** `ux_scr_user_permission` en `(user_id, permission_id)`

### Índices (5 total)

1. `idx_scr_user_permission_user` - Permisos directos de un usuario
2. `idx_scr_user_permission_permission` - Usuarios con permiso específico
3. `idx_scr_user_permission_is_active` - Solo permisos activos
4. `idx_scr_user_permission_expires_at` - Detectar permisos expirados
5. `idx_scr_user_permission_user_active` - Compuesto: permisos activos de usuario

---

## 🎯 Casos de Uso

### 1. Permiso Temporal sin Rol Completo

```java
// Otorgar invoice.approve por 30 días sin hacer MANAGER
UserPermission up = new UserPermission();
up.setUserId(userId);
up.setPermissionId(invoiceApprovePermissionId);
up.setGrantedBy("manager_login");
up.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
up.setReason("Temporary approval authority for Project Phoenix");
// ✅ Usuario puede aprobar facturas 30 días sin rol completo
```

### 2. Permiso Excepcional

```java
// Dar user.delete solo a usuario específico
UserPermission up = new UserPermission();
up.setUserId(userId);
up.setPermissionId(userDeletePermissionId);
up.setGrantedBy("admin");
up.setReason("Data cleanup for legacy accounts migration");
// ✅ Permiso excepcional documentado con razón
```

### 3. Query: Permisos Efectivos de Usuario

```sql
-- Permisos totales = permisos vía roles + permisos directos
SELECT DISTINCT p.name
FROM scr_permission p
WHERE p.id IN (
    -- Permisos vía roles
    SELECT ap.permission_id
    FROM scr_user_authority ua
    JOIN scr_authority_permission ap ON ua.authority_id = ap.authority_id
    WHERE ua.user_id = 123 AND ua.is_active = true
    UNION
    -- Permisos directos
    SELECT up.permission_id
    FROM scr_user_permission up
    WHERE up.user_id = 123
      AND up.is_active = true
      AND (up.expires_at IS NULL OR up.expires_at > NOW())
);
```

---

## ✅ Verificación

- ✅ XML válido
- ✅ 12 campos definidos
- ✅ Unique constraint en (user_id, permission_id)
- ✅ 5 índices optimizados
- ✅ Campo `reason` para justificar permisos especiales
- ✅ Auditoría completa (granted_by, revoked_by, dates)

---

## 🎯 Próximo Paso

**CHANGESET 6:** scr_authority_audit (log de cambios en roles)

---

**CHANGESET ID:** `20251024000005-create-user-permission`
**Estado:** ✅ Completado
