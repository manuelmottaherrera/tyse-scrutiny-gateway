# Reporte: CHANGESET 4 - scr_user_authority

**Fecha:** 2025-10-24 18:37
**Fase:** FASE 1 - Diseño de Base de Datos
**Paso:** 1.5 - Implementar CHANGESET 4
**Propósito:** Tabla de asignaciones usuario-rol con auditoría completa y roles temporales

---

## 📋 Tabla Creada: scr_user_authority

### Estructura (11 campos)

**Primary Key:**

- `id` - BIGINT auto-increment

**Foreign Keys:**

- `user_id` - BIGINT NOT NULL (→ scr_user.id)
- `authority_id` - BIGINT NOT NULL (→ scr_authority.id)

**Temporal Roles:**

- `expires_at` - TIMESTAMP NULL (fecha de expiración del rol)

**Status:**

- `is_active` - BOOLEAN NOT NULL DEFAULT true

**Assignment Audit:**

- `assigned_by` - VARCHAR(50) NOT NULL (quien asignó)
- `assigned_date` - TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

**Revocation Audit:**

- `revoked_by` - VARCHAR(50) NULL
- `revoked_date` - TIMESTAMP NULL
- `revoked_reason` - VARCHAR(500) NULL

### Mejoras vs jhi_user_authority Legacy

| Característica   | Legacy                              | Enterprise             |
| ---------------- | ----------------------------------- | ---------------------- |
| Primary Key      | Composite (user_id, authority_name) | BIGINT id              |
| Authority FK     | VARCHAR(50) name                    | BIGINT id              |
| Auditoría        | ❌ Ninguna                          | ✅ Completa            |
| Roles temporales | ❌ No                               | ✅ expires_at          |
| Revocación       | ❌ Solo DELETE                      | ✅ Soft delete + razón |
| Total campos     | 2                                   | 11                     |

---

## 🎯 Constraints e Índices

### Constraints

- **PK:** `pk_scr_user_authority` en `id`
- **UNIQUE:** `ux_scr_user_authority` en `(user_id, authority_id)`
  - Previene asignar el mismo rol dos veces al mismo usuario

### Índices (5 total)

1. `idx_scr_user_authority_user` - Roles de un usuario
2. `idx_scr_user_authority_authority` - Usuarios con un rol específico
3. `idx_scr_user_authority_is_active` - Solo asignaciones activas
4. `idx_scr_user_authority_expires_at` - Detectar roles expirados (jobs programados)
5. `idx_scr_user_authority_user_active` - Compuesto: roles activos de usuario

---

## 🎯 Casos de Uso Habilitados

### 1. Roles Temporales

```java
// Asignar ADMIN por 7 días
UserAuthority ua = new UserAuthority();
ua.setUserId(userId);
ua.setAuthorityId(adminRoleId);
ua.setAssignedBy("manager_login");
ua.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
// ✅ Auto-revocado después de 7 días (job programado)
```

### 2. Revocación con Auditoría

```java
// Revocar rol con razón
ua.setActive(false);
ua.setRevokedBy("admin_login");
ua.setRevokedDate(Instant.now());
ua.setRevokedReason("Security incident - access suspended");
// ✅ Historial completo preservado
```

### 3. Queries Comunes

```sql
-- Roles activos de un usuario (no expirados)
SELECT a.* FROM scr_authority a
JOIN scr_user_authority ua ON a.id = ua.authority_id
WHERE ua.user_id = 123
  AND ua.is_active = true
  AND (ua.expires_at IS NULL OR ua.expires_at > NOW());

-- Detectar roles expirados (job programado)
SELECT * FROM scr_user_authority
WHERE is_active = true
  AND expires_at IS NOT NULL
  AND expires_at <= NOW();
```

---

## ✅ Verificación

- ✅ XML válido
- ✅ 11 campos definidos
- ✅ Unique constraint en (user_id, authority_id)
- ✅ 5 índices optimizados
- ✅ Soporte roles temporales (expires_at)
- ✅ Auditoría completa (assigned_by, revoked_by, dates, reason)

---

## 🎯 Próximo Paso

**CHANGESET 5:** scr_user_permission (permisos directos a usuarios)

---

**CHANGESET ID:** `20251024000004-create-user-authority`
**Estado:** ✅ Completado
