# Reporte: CHANGESET 9 - Foreign Key Constraints

**Fecha:** 2025-10-24 19:06
**Fase:** FASE 1 - Diseño de Base de Datos
**Paso:** 1.10 - Implementar CHANGESET 9
**Propósito:** Agregar constraints de integridad referencial

---

## 📋 Foreign Keys Agregadas (7 total)

### scr_authority_permission (2 FKs)

1. `fk_authority_permission_authority`

   - `authority_id` → `scr_authority.id`

2. `fk_authority_permission_permission`
   - `permission_id` → `scr_permission.id`

### scr_user_authority (2 FKs)

3. `fk_user_authority_user`

   - `user_id` → `scr_user.id`

4. `fk_user_authority_authority`
   - `authority_id` → `scr_authority.id`

### scr_user_permission (2 FKs)

5. `fk_user_permission_user`

   - `user_id` → `scr_user.id`

6. `fk_user_permission_permission`
   - `permission_id` → `scr_permission.id`

### scr_authority_audit (1 FK)

7. `fk_authority_audit_authority`
   - `authority_id` → `scr_authority.id`

---

## 🎯 Garantías de Integridad

### Prevención de Datos Inválidos

- ✅ No se pueden asignar roles inexistentes a usuarios
- ✅ No se pueden asignar permisos inexistentes a roles
- ✅ No se pueden asignar permisos inexistentes directamente a usuarios
- ✅ No se puede auditar cambios de roles inexistentes

### Comportamiento en Eliminaciones

**Por defecto:** RESTRICT (default de Liquibase)

- Intentar eliminar un `scr_user` con roles asignados → ERROR
- Intentar eliminar `scr_authority` con permisos → ERROR
- Intentar eliminar `scr_permission` asignado → ERROR

**Recomendación:** Usar soft delete (is_active=false) en vez de DELETE físico

---

## ✅ Verificación

- ✅ XML válido
- ✅ 7 foreign keys agregadas
- ✅ Todas las relaciones cubiertas
- ✅ Agregadas DESPUÉS de cargar datos (evita violaciones)

---

## 🎯 Próximo Paso

**CHANGESET 10:** Validation (verificar schema completo)

---

**CHANGESET ID:** `20251024000009-add-foreign-keys`
**Estado:** ✅ Completado
