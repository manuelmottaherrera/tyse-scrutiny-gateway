# Reporte: Implementación de Tags de Liquibase

**Fecha:** 2025-10-25 23:45
**Fase:** Post FASE 1 - Mejoras de Rollback
**Propósito:** Implementar sistema de tags en changelogs para rollback controlado

---

## 🎯 Objetivo

Implementar tags de Liquibase directamente en los changelogs XML para permitir rollback controlado a estados conocidos de la base de datos, evitando el uso destructivo de `dropAll`.

---

## ✅ Implementación

### Archivo Creado

**`00000000000000_initial_tag.xml`**

- **Ubicación:** `src/main/resources/config/liquibase/changelog/`
- **Changeset ID:** `00000000000000-tag-estado-vacio`
- **Tag creado:** `estado-vacio`

### Tag Implementado

```xml
<changeSet id="00000000000000-tag-estado-vacio" author="Manuel A. Motta H.">
    <tagDatabase tag="estado-vacio"/>
</changeSet>
```

**Posición:** ANTES de cualquier otro changelog en `master.xml`

---

## 📊 Orden de Ejecución

```
1. 00000000000000_initial_tag.xml         → Tag "estado-vacio"
2. 00000000000000_initial_schema.xml      → Tabla scr_user
3. 20251024000000_enterprise_authorization_schema.xml → Sistema autorización
```

---

## 🔄 Comandos Disponibles

### Rollback Controlado (Recomendado)

```bash
./mvnw liquibase:rollback -Dliquibase.rollbackTag=estado-vacio
```

**Comportamiento:**

- ✅ Ejecuta bloques `<rollback>` en orden inverso
- ✅ Respeta foreign keys y dependencias
- ✅ Mantiene tablas de control de Liquibase
- ✅ Permite re-aplicar con `liquibase:update`

### Rollback Destructivo (No Recomendado)

```bash
./mvnw liquibase:dropAll
```

**Comportamiento:**

- ❌ Elimina TODOS los objetos directamente
- ❌ No usa changesets ni bloques rollback
- ❌ Elimina incluso databasechangelog
- ⚠️ Requiere reconstrucción completa

---

## 📚 Documentación

**README.md actualizado** con nueva sección:

- `## Liquibase Database Tags`
- Comandos de rollback
- Comparativa Tag vs DropAll
- Recomendaciones de uso

---

## 🎯 Ventajas

1. **Rollback Controlado:** Ejecución ordenada de rollbacks por changeset
2. **Trazabilidad:** Historial de changesets se mantiene
3. **Seguridad:** Respeta dependencias de foreign keys
4. **Reproducibilidad:** Tags se aplican automáticamente en `liquibase:update`
5. **Versionado:** Tags quedan en Git, no son comandos manuales

---

## ✅ Estado

- ✅ Tag `estado-vacio` implementado
- ✅ Registrado en `master.xml` (primera posición)
- ✅ Documentado en README.md
- ⏳ Pendiente: Ejecutar `dropAll` para limpiar tag manual previo

---
