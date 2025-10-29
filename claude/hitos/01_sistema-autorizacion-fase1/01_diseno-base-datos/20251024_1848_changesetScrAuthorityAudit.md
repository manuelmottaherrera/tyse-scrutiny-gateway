# Reporte: CHANGESET 6 - scr_authority_audit

**Fecha:** 2025-10-24 18:48
**Fase:** FASE 1 - Diseño de Base de Datos
**Paso:** 1.7 - Implementar CHANGESET 6
**Propósito:** Tabla de auditoría de cambios en roles/autoridades

---

## 📋 Tabla Creada: scr_authority_audit

### Estructura (9 campos)

**Primary Key:**

- `id` - BIGINT auto-increment

**Foreign Key:**

- `authority_id` - BIGINT NOT NULL (→ scr_authority.id)

**Audit Action:**

- `action` - VARCHAR(20) NOT NULL
  - Valores: CREATED, UPDATED, DELETED, ACTIVATED, DEACTIVATED

**Changed Values (JSON):**

- `old_values` - TEXT NULL (valores anteriores en JSON)
- `new_values` - TEXT NULL (valores nuevos en JSON)

**Audit Metadata:**

- `changed_by` - VARCHAR(50) NOT NULL (quien hizo el cambio)
- `changed_date` - TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

**Security Context:**

- `ip_address` - VARCHAR(45) NULL (IPv4 o IPv6)
- `user_agent` - VARCHAR(255) NULL (browser/app)

---

## 🎯 Constraints e Índices

### Constraint

- **PK:** `pk_scr_authority_audit` en `id`

### Índices (5 total)

1. `idx_scr_authority_audit_authority` - Historial de un rol
2. `idx_scr_authority_audit_action` - Filtrar por tipo de acción
3. `idx_scr_authority_audit_changed_by` - Cambios de un usuario
4. `idx_scr_authority_audit_changed_date` - Ordenar por fecha
5. `idx_scr_authority_audit_authority_date` - Compuesto: historial de rol ordenado

---

## 🎯 Casos de Uso

### 1. Registrar Creación de Rol

```java
AuthorityAudit audit = new AuthorityAudit();
audit.setAuthorityId(newAuthority.getId());
audit.setAction("CREATED");
audit.setNewValues(objectMapper.writeValueAsString(newAuthority));
audit.setChangedBy(currentUser.getLogin());
audit.setIpAddress(request.getRemoteAddr());
audit.setUserAgent(request.getHeader("User-Agent"));
```

### 2. Registrar Actualización con Diff

```java
AuthorityAudit audit = new AuthorityAudit();
audit.setAuthorityId(authority.getId());
audit.setAction("UPDATED");
audit.setOldValues(objectMapper.writeValueAsString(oldState));
audit.setNewValues(objectMapper.writeValueAsString(newState));
audit.setChangedBy(currentUser.getLogin());
```

### 3. Queries de Auditoría

```sql
-- Historial completo de un rol
SELECT action, changed_by, changed_date, old_values, new_values
FROM scr_authority_audit
WHERE authority_id = 1
ORDER BY changed_date DESC;

-- Cambios realizados por un usuario específico
SELECT a.name, aa.action, aa.changed_date
FROM scr_authority_audit aa
JOIN scr_authority a ON aa.authority_id = a.id
WHERE aa.changed_by = 'admin'
ORDER BY aa.changed_date DESC;

-- Detectar eliminaciones
SELECT a.name, aa.changed_by, aa.changed_date
FROM scr_authority_audit aa
JOIN scr_authority a ON aa.authority_id = a.id
WHERE aa.action = 'DELETED';
```

---

## 📊 Ejemplo de JSON Almacenado

### old_values (antes del cambio)

```json
{
  "name": "User Manager",
  "code": "ROLE_USER_MANAGER",
  "description": "Manages system users",
  "is_active": true,
  "hierarchy_level": 300
}
```

### new_values (después del cambio)

```json
{
  "name": "User Administrator",
  "code": "ROLE_USER_MANAGER",
  "description": "Full user management capabilities",
  "is_active": true,
  "hierarchy_level": 250
}
```

**Cambios detectados:** name y description modificados, hierarchy_level bajó (más poder)

---

## ✅ Verificación

- ✅ XML válido
- ✅ 9 campos definidos
- ✅ Campos TEXT para JSON (old_values, new_values)
- ✅ 5 índices para queries de auditoría
- ✅ Security context (ip_address, user_agent)
- ✅ Soporte para 5 tipos de acciones

---

## 🎯 Próximo Paso

**CHANGESET 7:** Índices adicionales de performance (ya cubiertos en changesets individuales, verificar si se requieren más)

---

**CHANGESET ID:** `20251024000006-create-authority-audit`
**Estado:** ✅ Completado
