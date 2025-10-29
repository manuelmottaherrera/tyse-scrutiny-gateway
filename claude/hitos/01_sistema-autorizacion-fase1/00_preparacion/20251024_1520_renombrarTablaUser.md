# Reporte: Renombrar Tabla jhi_user → user

**Fecha:** 2025-10-24 15:20
**Fase:** FASE 0 - Preparación
**Paso:** 0.7 (adicional) - Renombrar tabla de usuarios
**Propósito:** Eliminar el prefijo `jhi_` de la tabla de usuarios para un esquema más limpio

---

## 🎯 Objetivo

Renombrar la tabla `jhi_user` a simplemente `user`, eliminando el prefijo heredado de JHipster que ya no es necesario en nuestro diseño enterprise.

---

## 📋 Cambios Realizados

### 1. Changelog de Liquibase

**Archivo:** `src/main/resources/config/liquibase/changelog/00000000000000_initial_schema.xml`

#### Cambios:

**Antes:**

```xml
<createTable tableName="jhi_user">
    <!-- ... columnas ... -->
</createTable>

<addNotNullConstraint tableName="jhi_user" ... />
<loadData tableName="jhi_user" ... />
<dropDefaultValue tableName="jhi_user" ... />
```

**Después:**

```xml
<createTable tableName="user">
    <!-- ... columnas ... -->
</createTable>

<addNotNullConstraint tableName="user" ... />
<loadData tableName="user" ... />
<dropDefaultValue tableName="user" ... />
```

**Ubicaciones actualizadas:**

- ✅ `<createTable tableName="user">` (línea 21)
- ✅ `<addNotNullConstraint tableName="user"/>` (línea 54)
- ✅ `<loadData tableName="user"/>` (línea 60)
- ✅ `<dropDefaultValue tableName="user"/>` (línea 67)

**Comentario actualizado:**

```xml
<!--
    Modified initial schema for Enterprise Authorization System.

    REMOVED (will be replaced by enterprise-auth changelog):
    - jhi_authority (replaced by authorities with BIGINT ID)
    - jhi_user_authority (replaced by user_authorities with full audit)

    RENAMED:
    - jhi_user → user (removed jhi_ prefix)
-->
```

---

### 2. Entidad de Dominio User.java

**Archivo:** `src/main/java/com/tyse/scrutiny/gateway/domain/User.java`

#### Cambio:

**Antes:**

```java
@Table("jhi_user")
public class User extends AbstractAuditingEntity<Long> implements Serializable {
```

**Después:**

```java
@Table("user")
public class User extends AbstractAuditingEntity<Long> implements Serializable {
```

**Ubicación:** Línea 22

---

## 📊 Archivos Modificados

| Archivo                             | Cambios                              | Estado                |
| ----------------------------------- | ------------------------------------ | --------------------- |
| `00000000000000_initial_schema.xml` | 4 ocurrencias de `jhi_user` → `user` | ✅ Completado         |
| `User.java`                         | 1 anotación `@Table`                 | ✅ Completado         |
| `UserRepository.java`               | Queries SQL con `jhi_user`           | ⏳ Pendiente (FASE 3) |

---

## ⚠️ Referencias Pendientes de Actualizar

### UserRepository.java

El archivo `UserRepository.java` contiene múltiples queries SQL con referencias a `jhi_user` y `jhi_user_authority`:

```java
// Línea 47
@Query("INSERT INTO jhi_user_authority VALUES(:userId, :authority)")

// Línea 50
@Query("DELETE FROM jhi_user_authority")

// Línea 53
@Query("DELETE FROM jhi_user_authority WHERE user_id = :userId")

// Línea 101
.sql("SELECT * FROM jhi_user u LEFT JOIN jhi_user_authority ua ON u.id=ua.user_id")

// Línea 120
.sql("DELETE FROM jhi_user_authority WHERE user_id = :userId")

// Línea 128
.sql("SELECT * FROM jhi_user u LEFT JOIN jhi_user_authority ua ON u.id=ua.user_id WHERE u." + fieldName + " = :" + fieldName)
```

**Estado:** ⏳ Pendiente de actualización
**Cuándo:** FASE 3 - Actualizar Repositorios (R2DBC)
**Acción:** Estas queries se reescribirán completamente para usar:

- `user` en vez de `jhi_user`
- `user_authorities` en vez de `jhi_user_authority`
- Nuevas relaciones con `authorities` (BIGINT ID)

**Razón para NO cambiar ahora:**

- Las queries serán completamente reescritas en FASE 3
- El nuevo diseño cambia la estructura de las FK
- Evitamos hacer cambios dos veces

---

## 📊 Impacto del Cambio

### ✅ Ventajas

1. **Esquema más limpio**

   - Nombre de tabla más simple y directo
   - Elimina prefijo legacy de JHipster

2. **Consistencia con nuevas tablas**

   - Las nuevas tablas no usan prefijo `jhi_`
   - `authorities`, `permissions`, `user_authorities` (sin prefijo)
   - Ahora `user` también sin prefijo

3. **SQL más legible**

   ```sql
   -- Antes
   SELECT * FROM jhi_user u
   JOIN jhi_user_authority ua ON u.id = ua.user_id

   -- Después
   SELECT * FROM user u
   JOIN user_authorities ua ON u.id = ua.user_id
   ```

4. **Estándares modernos**
   - Nombre genérico `user` es más estándar
   - Menos acoplamiento a JHipster

### ⚠️ Consideraciones

1. **Palabra reservada en algunos DBs**

   - `user` es palabra reservada en algunos RDBMS
   - PostgreSQL: ✅ No es problema (puede usarse sin quotes)
   - MySQL: ⚠️ Requeriría backticks (pero no usamos MySQL)

2. **Breaking change con JHipster**
   - Generadores de JHipster esperan `jhi_user`
   - Pero ya decidimos separarnos de la compatibilidad estricta

---

## 🎯 Comparación del Schema

### Antes del Cambio

```sql
CREATE TABLE jhi_user (
    id BIGINT PRIMARY KEY,
    login VARCHAR(50) UNIQUE,
    -- ... más columnas ...
);
```

### Después del Cambio

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    login VARCHAR(50) UNIQUE,
    -- ... más columnas ...
);
```

---

## ✅ Verificación

### Changelog XML

```bash
grep -n "tableName=" 00000000000000_initial_schema.xml
```

**Resultado:**

```
21:        <createTable tableName="user">
54:                              tableName="user"/>
60:                  tableName="user"
67:        <dropDefaultValue tableName="user"
71:        <createTable tableName="jhi_date_time_wrapper">
```

✅ Todas las referencias actualizadas (excepto la tabla de test que se mantiene)

### Entidad Java

```bash
grep -n '@Table' User.java
```

**Resultado:**

```
22:@Table("user")
```

✅ Anotación actualizada correctamente

---

## 🔄 Migración de Datos

### Al ejecutar Liquibase

Cuando se ejecute `./mvnw liquibase:update`:

1. ✅ Creará tabla `user` (no `jhi_user`)
2. ✅ Cargará datos desde `user.csv` en tabla `user`
3. ✅ La aplicación buscará tabla `user` (por la anotación `@Table`)

**Sin conflictos:** Todo está sincronizado

---

## 📝 Tareas Pendientes para Próximas Fases

### FASE 3: Actualizar Repositorios

**Archivo:** `UserRepository.java`

Queries a actualizar:

- [ ] `INSERT INTO jhi_user_authority` → `user_authorities`
- [ ] `DELETE FROM jhi_user_authority` → `user_authorities`
- [ ] `SELECT * FROM jhi_user u LEFT JOIN jhi_user_authority` → `user u LEFT JOIN user_authorities`

**Nota:** Estas queries además cambiarán para usar el nuevo diseño con `authority_id` en vez de `authority_name`

---

## 🎯 Estado Actual del Schema

```
┌────────────────────────────────────┐
│  TABLAS DESPUÉS DE LIQUIBASE:      │
├────────────────────────────────────┤
│  ✅ user (nueva)                   │
│  ❌ jhi_user (no se crea)          │
│  ❌ jhi_authority (no se crea)     │
│  ❌ jhi_user_authority (no se crea)│
│  ✅ jhi_date_time_wrapper (test)   │
└────────────────────────────────────┘
```

---

## 🎯 Próximos Pasos

Con la tabla `user` renombrada, ahora podemos:

1. ✅ **FASE 1, PASO 1.1:** Crear changelog maestro enterprise
2. ✅ **FASE 1, PASO 1.2:** Crear nuevas tablas (authorities, permissions, etc.)
3. ✅ **FASE 3:** Actualizar UserRepository con las nuevas queries

---

## 📊 Resumen de Cambios

| Elemento              | Estado Anterior | Estado Nuevo | Fase de Actualización |
| --------------------- | --------------- | ------------ | --------------------- |
| Tabla en Liquibase    | `jhi_user`      | `user`       | ✅ FASE 0             |
| Anotación `@Table`    | `jhi_user`      | `user`       | ✅ FASE 0             |
| Queries en Repository | `jhi_user`      | Pendiente    | ⏳ FASE 3             |
| CSV de datos          | `user.csv`      | Sin cambios  | -                     |

---

## ✅ Conclusión

**Estado:** Renombrado completado en Liquibase y entidad de dominio

**Cambios aplicados:**

- ✅ Changelog XML actualizado (4 ocurrencias)
- ✅ Entidad User.java actualizada (1 ocurrencia)
- ✅ Comentarios actualizados

**Pendiente:**

- ⏳ Queries SQL en UserRepository (se harán en FASE 3)

**Listo para:** FASE 1 - Crear changelog de tablas enterprise

---

**Fecha de modificación:** 2025-10-24 15:20
**Archivos modificados:** 2
**Estado:** ✅ Completado
