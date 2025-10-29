# Corrección de Rollback en Liquibase

**Fecha:** 2025-10-26 00:46
**Fase:** Fase 1 - Sistema de Autorización Empresarial
**Autor:** Manuel A. Motta H.
**Estado:** ✅ Completado

---

## Propósito

Corregir el changeset de `initial_schema.xml` para que tenga un bloque de rollback explícito que permita revertir completamente todas las operaciones, incluyendo la eliminación de la tabla `scr_user` y sus datos.

---

## Contexto

Durante las pruebas de rollback al tag `estado-vacio`, se detectó que la tabla `scr_user` no estaba siendo eliminada correctamente. El problema era que el changeset solo tenía un rollback parcial para `dropDefaultValue`, pero no para las otras operaciones (`createTable`, `addNotNullConstraint`, `loadData`).

---

## Problema Detectado

### Error Encontrado

Al ejecutar:

```bash
./mvnw liquibase:rollback -Dliquibase.rollbackTag=estado-vacio
./mvnw liquibase:update
```

Se obtenía el error:

```
ERROR: relation "scr_user" already exists
```

### Causa Raíz

El changeset `00000000000001` en `initial_schema.xml` tenía un bloque de rollback incompleto:

**❌ Rollback Original (Incompleto):**

```xml
<rollback>
    <addDefaultValue tableName="scr_user" columnName="created_date" defaultValueComputed="CURRENT_TIMESTAMP"/>
</rollback>
```

Este rollback solo restauraba el valor por defecto de la columna `created_date`, pero no:

- ❌ Eliminaba los datos de la tabla
- ❌ Eliminaba la constraint NOT NULL de `password_hash`
- ❌ Eliminaba la tabla `scr_user`

---

## Solución Implementada

### Rollback Completo Agregado

**✅ Rollback Corregido (Completo):**

```xml
<rollback>
    <!-- Rollback in reverse order of operations -->
    <addDefaultValue tableName="scr_user" columnName="created_date" defaultValueComputed="CURRENT_TIMESTAMP"/>
    <delete tableName="scr_user"/>
    <dropNotNullConstraint tableName="scr_user" columnName="password_hash" columnDataType="varchar(60)"/>
    <dropTable tableName="scr_user"/>
</rollback>
```

### Orden de Rollback

El rollback debe ejecutar las operaciones en **orden inverso** al changeset:

| Orden Forward (Changeset) | Operación              | Orden Reverse (Rollback) | Operación Inversa       |
| ------------------------- | ---------------------- | ------------------------ | ----------------------- |
| 1️⃣                        | `createTable`          | 4️⃣                       | `dropTable`             |
| 2️⃣                        | `addNotNullConstraint` | 3️⃣                       | `dropNotNullConstraint` |
| 3️⃣                        | `loadData`             | 2️⃣                       | `delete`                |
| 4️⃣                        | `dropDefaultValue`     | 1️⃣                       | `addDefaultValue`       |

---

## Validación Realizada

### 1. Validación de XML

```bash
xmllint --noout /path/to/00000000000000_initial_schema.xml
# ✅ XML válido
```

### 2. Ciclo Completo de Pruebas

**a) Aplicar todos los changesets:**

```bash
./mvnw liquibase:update
# ✅ 12 changesets aplicados exitosamente
```

**b) Rollback completo al estado vacío:**

```bash
./mvnw liquibase:rollback -Dliquibase.rollbackTag=estado-vacio
# ✅ Todos los changesets revertidos
```

**c) Verificar eliminación de tabla:**

```bash
docker exec tysescrutinygateway-postgresql-1 psql -U tyseScrutinyGateway -d tyseScrutinyGateway -c "\dt"
# Resultado:
#  Schema |         Name          | Type  |        Owner
# --------+-----------------------+-------+---------------------
#  public | databasechangelog     | table | tyseScrutinyGateway
#  public | databasechangeloglock | table | tyseScrutinyGateway
# ✅ Tabla scr_user eliminada correctamente
```

**d) Re-aplicar changesets:**

```bash
./mvnw liquibase:update
# ✅ 12 changesets aplicados exitosamente
```

**e) Rollback al tag sistema-autorizacion:**

```bash
./mvnw liquibase:rollback -Dliquibase.rollbackTag=sistema-autorizacion
# ✅ Rollback exitoso
```

**f) Update final:**

```bash
./mvnw liquibase:update
# ✅ Tag sistema-autorizacion restaurado
```

---

## Resultado

### Estado Final

✅ **Changesets con Rollback Completo:**

- `00000000000000_initial_schema.xml::00000000000001` - Ahora tiene rollback completo que:
  - Restaura el default value de `created_date`
  - Elimina todos los datos de `scr_user`
  - Elimina la constraint NOT NULL de `password_hash`
  - Elimina completamente la tabla `scr_user`

### Pruebas Validadas

| Prueba                          | Comando                                                                  | Resultado                      |
| ------------------------------- | ------------------------------------------------------------------------ | ------------------------------ |
| Update completo                 | `./mvnw liquibase:update`                                                | ✅ 12 changesets aplicados     |
| Rollback a estado-vacio         | `./mvnw liquibase:rollback -Dliquibase.rollbackTag=estado-vacio`         | ✅ Todas las tablas eliminadas |
| Re-aplicar update               | `./mvnw liquibase:update`                                                | ✅ Sistema restaurado          |
| Rollback a sistema-autorizacion | `./mvnw liquibase:rollback -Dliquibase.rollbackTag=sistema-autorizacion` | ✅ Tag revertido               |
| Update final                    | `./mvnw liquibase:update`                                                | ✅ Tag restaurado              |

---

## Lecciones Aprendidas

### 1. Bloques de Rollback Explícitos

**Liquibase no siempre genera rollbacks automáticos completos.** Es mejor ser explícito:

```xml
<changeSet id="..." author="...">
    <!-- Operaciones forward -->
    <createTable .../>
    <addNotNullConstraint .../>
    <loadData .../>
    <dropDefaultValue .../>

    <!-- Rollback explícito en orden inverso -->
    <rollback>
        <addDefaultValue .../>
        <delete .../>
        <dropNotNullConstraint .../>
        <dropTable .../>
    </rollback>
</changeSet>
```

### 2. Orden de Operaciones en Rollback

El rollback debe ejecutarse en **orden inverso** estricto:

- Última operación del changeset → Primera operación del rollback
- Primera operación del changeset → Última operación del rollback

### 3. Validación de Rollback

Siempre probar el ciclo completo:

1. `liquibase:update` (aplicar)
2. `liquibase:rollback` (revertir)
3. Verificar estado de DB (`\dt` en psql)
4. `liquibase:update` (re-aplicar)
5. Confirmar que todo funciona

### 4. Operaciones Comunes de Rollback

| Operación Forward         | Operación Rollback             |
| ------------------------- | ------------------------------ |
| `createTable`             | `dropTable`                    |
| `addColumn`               | `dropColumn`                   |
| `addNotNullConstraint`    | `dropNotNullConstraint`        |
| `addForeignKeyConstraint` | `dropForeignKeyConstraint`     |
| `addDefaultValue`         | `dropDefaultValue`             |
| `loadData`                | `delete` (con WHERE apropiado) |
| `insert`                  | `delete` (con WHERE apropiado) |

---

## Archivos Modificados

### Archivo Principal

**`src/main/resources/config/liquibase/changelog/00000000000000_initial_schema.xml`**

**Cambios:**

- Líneas 72-78: Agregado bloque de rollback completo con 4 operaciones

**Diff:**

```diff
         <dropDefaultValue tableName="scr_user" columnName="created_date" columnDataType="${datetimeType}"/>

         <rollback>
+            <!-- Rollback in reverse order of operations -->
             <addDefaultValue tableName="scr_user" columnName="created_date" defaultValueComputed="CURRENT_TIMESTAMP"/>
+            <delete tableName="scr_user"/>
+            <dropNotNullConstraint tableName="scr_user" columnName="password_hash" columnDataType="varchar(60)"/>
+            <dropTable tableName="scr_user"/>
         </rollback>
     </changeSet>
```

---

## Conclusiones

1. ✅ Rollback completo implementado y validado
2. ✅ Ciclo update → rollback → update funciona perfectamente
3. ✅ Tabla `scr_user` se elimina completamente durante rollback
4. ✅ Datos se restauran correctamente durante re-aplicación
5. ✅ Tags de Liquibase funcionan correctamente con rollback completo

El sistema de rollback de Liquibase está ahora completamente funcional y permite volver a cualquier tag definido sin dejar residuos en la base de datos.

---

## Siguiente Paso

**Recomendación:** Continuar con la Fase 2 del sistema de autorización, sabiendo que existe un mecanismo de rollback confiable y probado.

---

## Referencias

- Archivo modificado: `src/main/resources/config/liquibase/changelog/00000000000000_initial_schema.xml`
- Documentación: `README.md` sección "Liquibase Database Management"
- Tag actual: `sistema-autorizacion`
- Documentación Liquibase Rollback: https://docs.liquibase.com/commands/rollback/rollback-by-tag.html
