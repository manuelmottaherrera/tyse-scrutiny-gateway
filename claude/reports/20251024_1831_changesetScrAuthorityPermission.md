# Reporte: CHANGESET 3 - scr_authority_permission

**Fecha:** 2025-10-24 18:31
**Fase:** FASE 1 - Diseño de Base de Datos
**Paso:** 1.4 - Implementar CHANGESET 3
**Propósito:** Tabla junction N:N entre roles y permisos

---

## 📋 Tabla Creada: scr_authority_permission

### Estructura

**Primary Key:**

- `id` - BIGINT auto-increment

**Foreign Keys:**

- `authority_id` - BIGINT NOT NULL (→ scr_authority.id)
- `permission_id` - BIGINT NOT NULL (→ scr_permission.id)

**Audit Fields:**

- `granted_by` - VARCHAR(50) NOT NULL (quien asignó el permiso)
- `granted_date` - TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

### Constraints

- **PK:** `pk_scr_authority_permission` en `id`
- **UNIQUE:** `ux_scr_authority_permission` en `(authority_id, permission_id)`
  - Previene duplicados: un permiso solo puede asignarse una vez al mismo rol

### Índices

1. `idx_scr_authority_permission_authority` en `authority_id`
   - Query: "¿Qué permisos tiene este rol?"
2. `idx_scr_authority_permission_permission` en `permission_id`
   - Query: "¿Qué roles tienen este permiso?"

---

## 🎯 Casos de Uso

**Asignación de permisos a roles:**

```sql
-- ROLE_ADMIN tiene todos los permisos
INSERT INTO scr_authority_permission (authority_id, permission_id, granted_by)
VALUES
  (1, 1, 'system'), -- ROLE_ADMIN → user.create
  (1, 2, 'system'), -- ROLE_ADMIN → user.read
  (1, 3, 'system'), -- ROLE_ADMIN → user.update
  (1, 4, 'system'); -- ROLE_ADMIN → user.delete

-- ROLE_USER solo lectura
INSERT INTO scr_authority_permission (authority_id, permission_id, granted_by)
VALUES (2, 2, 'system'); -- ROLE_USER → user.read
```

**Consultas comunes:**

```sql
-- Listar permisos de un rol
SELECT p.name FROM scr_permission p
JOIN scr_authority_permission ap ON p.id = ap.permission_id
WHERE ap.authority_id = 1;

-- Listar roles con permiso específico
SELECT a.name FROM scr_authority a
JOIN scr_authority_permission ap ON a.id = ap.authority_id
WHERE ap.permission_id = 4; -- user.delete
```

---

## ✅ Verificación

- ✅ XML válido
- ✅ Tabla junction con PK propio
- ✅ Unique constraint previene duplicados
- ✅ 2 índices para queries bidireccionales
- ✅ Auditoría básica (granted_by, granted_date)

---

## 🎯 Próximo Paso

**CHANGESET 4:** scr_user_authority (asignaciones usuario-rol con auditoría completa)

---

**CHANGESET ID:** `20251024000003-create-authority-permission`
**Estado:** ✅ Completado
