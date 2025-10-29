# Reporte: CHANGESET 8 - Load Initial Data

**Fecha:** 2025-10-24 19:00
**Fase:** FASE 1 - Diseño de Base de Datos
**Paso:** 1.9 - Implementar CHANGESET 8
**Propósito:** Cargar datos semilla del sistema de autorización

---

## 📋 Archivos CSV Creados

### 1. authority.csv (2 registros)

- ROLE_ADMIN (id=1, hierarchy=0, is_system=true)
- ROLE_USER (id=2, hierarchy=500, is_system=true)

### 2. permission.csv (13 registros)

**user.\*** (4 permisos):

- user.create, user.read, user.update, user.delete

**authority.\*** (5 permisos):

- authority.create, authority.read, authority.update, authority.delete, authority.assign

**permission.\*** (4 permisos):

- permission.create, permission.read, permission.update, permission.delete

### 3. authority_permission.csv (16 registros)

**ROLE_ADMIN** (id=1) - TODOS los permisos (13 permisos)
**ROLE_USER** (id=2) - Solo lectura (3 permisos):

- user.read (id=2)
- authority.read (id=6)
- permission.read (id=11)

### 4. user_authority.csv (3 registros)

- Usuario admin (id=1) → ROLE_ADMIN + ROLE_USER
- Usuario user (id=2) → ROLE_USER

---

## 🎯 Datos Cargados en Changelog

### loadData Statements (4 total)

1. **scr_authority** - 2 roles del sistema
2. **scr_permission** - 13 permisos base
3. **scr_authority_permission** - 16 asignaciones rol-permiso
4. **scr_user_authority** - 3 asignaciones usuario-rol

Todos con:

- `separator=";"`
- `usePreparedStatements="true"`
- Type conversions para numeric, boolean, timestamp

---

## ✅ Verificación

- ✅ XML válido
- ✅ 4 archivos CSV creados
- ✅ 4 loadData statements en changelog
- ✅ Type conversions configurados
- ✅ Datos consistentes (IDs correctos en FKs)

---

## 🎯 Resultado Esperado

**Después de ejecutar CHANGESET 8:**

- 2 authorities activas
- 13 permissions activas
- ROLE_ADMIN con permisos completos
- ROLE_USER con permisos de solo lectura
- admin y user con sus roles asignados

---

## 🎯 Próximo Paso

**CHANGESET 9:** Add Foreign Key Constraints

---

**CHANGESET ID:** `20251024000008-load-initial-data`
**Estado:** ✅ Completado
