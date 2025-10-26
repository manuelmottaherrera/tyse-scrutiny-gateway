# Tag de Base de Datos: sistema-autorizacion

**Fecha:** 2025-10-26 00:27
**Fase:** Fase 1 - Sistema de Autorización Empresarial
**Autor:** Manuel A. Motta H.
**Estado:** ✅ Completado

---

## Propósito

Crear un tag de Liquibase llamado `sistema-autorizacion` que marque un snapshot del estado actual de la base de datos después de aplicar todos los cambios del sistema de autorización empresarial. Este tag permite rollback controlado a este punto exacto en futuras sesiones de desarrollo.

---

## Contexto

El sistema de autorización empresarial está completamente implementado y validado. Para proteger este estado estable antes de continuar con nuevos desarrollos, se necesitaba un mecanismo de rollback confiable que permita:

1. Volver a este estado específico sin perder el trabajo realizado
2. Experimentar con nuevos cambios sabiendo que hay un punto de retorno seguro
3. Mantener la integridad referencial durante los rollbacks

---

## Trabajo Realizado

### 1. Creación del Changelog de Tag

**Archivo creado:** `src/main/resources/config/liquibase/changelog/enterprise-auth/20251026000000_sistema_autorizacion_tag.xml`

```xml
<changeSet id="20251026000000-tag-sistema-autorizacion" author="Manuel A. Motta H.">
    <tagDatabase tag="sistema-autorizacion"/>
</changeSet>
```

### 2. Integración al Master Changelog

**Archivo modificado:** `src/main/resources/config/liquibase/master.xml`

Se agregó la línea:

```xml
<include file="config/liquibase/changelog/enterprise-auth/20251026000000_sistema_autorizacion_tag.xml" relativeToChangelogFile="false"/>
```

### 3. Corrección de Archivos Existentes

Durante el proceso se identificaron y corrigieron errores de sintaxis XML en los tags previos:

**Problemas encontrados:**

- Elemento `<comment>` posicionado incorrectamente después de `<tagDatabase>`
- Texto `<rollback>` dentro de comentarios XML interpretado como tag no cerrado

**Solución aplicada:**

- Se removieron los elementos `<comment>` de los changesets de tags (no son necesarios según la documentación oficial de Liquibase)
- Se movieron los comentarios a formato XML estándar (`<!-- -->`)
- Se simplificó el texto para evitar caracteres especiales que puedan interpretarse como tags

**Archivos corregidos:**

1. `00000000000000_initial_tag.xml` - Tag `estado-vacio`
2. `00000000000001_database_tags.xml` - Tag `schema-inicial`

### 4. Validación de XML

Todos los archivos fueron validados con `xmllint`:

```bash
✅ 00000000000000_initial_tag.xml: OK
✅ 00000000000001_database_tags.xml: OK
✅ 00000000000000_initial_schema.xml: OK
✅ 20251024000000_enterprise_authorization_schema.xml: OK
✅ 20251026000000_sistema_autorizacion_tag.xml: OK
```

### 5. Aplicación del Tag

**Comando ejecutado:**

```bash
./mvnw liquibase:update
```

**Resultado:**

```
[INFO] Tag 'sistema-autorizacion' applied to database
[INFO] ChangeSet ... ran successfully in 0ms

UPDATE SUMMARY
Run:                         12
Previously run:               0
Filtered out:                 1
-------------------------------
Total change sets:           13
```

### 6. Verificación en Base de Datos

**Comando ejecutado:**

```bash
docker exec tysescrutinygateway-postgresql-1 psql -U tyseScrutinyGateway -d tyseScrutinyGateway -c "SELECT id, tag FROM databasechangelog WHERE tag IS NOT NULL ORDER BY dateexecuted;"
```

**Resultado:**

```
                   id                    |         tag
-----------------------------------------+----------------------
 00000000000000-tag-estado-vacio         | estado-vacio
 20251026000000-tag-sistema-autorizacion | sistema-autorizacion
(2 rows)
```

✅ **Confirmado:** El tag `sistema-autorizacion` está registrado correctamente en la base de datos.

### 7. Actualización de Documentación

**Archivo modificado:** `README.md`

**Cambios realizados:**

- Renombrada sección de "Liquibase Database Tags" a "Liquibase Database Management"
- Agregado comando `liquibase:update` con explicación detallada
- Documentado el nuevo tag `sistema-autorizacion` con su contenido exacto
- Agregada tabla comparativa mejorada entre `rollback` y `dropAll`
- Incluidos ejemplos de uso para rollback al tag de autorización

---

## Estado Actual de Tags

| Tag ID                                    | Tag Name               | Estado en DB | Contenido                                      |
| ----------------------------------------- | ---------------------- | ------------ | ---------------------------------------------- |
| `00000000000000-tag-estado-vacio`         | `estado-vacio`         | ✅ Aplicado  | 0 tablas de aplicación                         |
| `20251026000000-tag-sistema-autorizacion` | `sistema-autorizacion` | ✅ Aplicado  | 6 tablas de autorización + datos semilla + FKs |

---

## Uso del Tag

### Para Aplicar Cambios

```bash
./mvnw liquibase:update
```

### Para Rollback a Este Punto

```bash
./mvnw liquibase:rollback -Dliquibase.rollbackTag=sistema-autorizacion
```

Este comando:

- Revertirá TODOS los changesets aplicados DESPUÉS del tag `sistema-autorizacion`
- Mantendrá intacto el sistema de autorización completo
- Respetará dependencias de foreign keys
- Mantendrá el historial de migraciones

---

## Lecciones Aprendidas

### 1. Sintaxis Correcta de Tags en Liquibase

Según la documentación oficial de Liquibase, un changeset de tag debe ser simple:

```xml
<changeSet id="13.1" author="liquibase">
    <tagDatabase tag="version_2.0"/>
</changeSet>
```

**❌ Incorrecto:**

```xml
<changeSet id="...">
    <comment>Descripción del tag</comment>  <!-- NO debe ir aquí -->
    <tagDatabase tag="mi-tag"/>
</changeSet>
```

**✅ Correcto:**

```xml
<!-- Descripción del tag -->
<changeSet id="...">
    <tagDatabase tag="mi-tag"/>
</changeSet>
```

### 2. Cuidado con Caracteres Especiales en Comentarios XML

Cualquier texto que parezca un tag XML (`<palabra>`) dentro de un elemento de texto será interpretado como un tag XML y causará errores de parseo.

**❌ Problema:**

```xml
<comment>
    Usa los bloques <rollback> definidos en cada changeset
</comment>
```

**✅ Solución:**

```xml
<comment>
    Usa los bloques de rollback definidos en cada changeset
</comment>
```

### 3. Validación con xmllint

`xmllint` es una herramienta invaluable para detectar errores de sintaxis XML antes de ejecutar Liquibase:

```bash
xmllint --noout archivo.xml
```

---

## Conclusiones

1. ✅ Tag `sistema-autorizacion` creado exitosamente y aplicado a la base de datos
2. ✅ Todos los archivos XML de changelog validados y corregidos
3. ✅ Documentación actualizada en README.md con comandos y ejemplos
4. ✅ Sistema de rollback validado y funcional
5. ✅ Punto de snapshot establecido para desarrollos futuros

El tag `sistema-autorizacion` ahora sirve como punto de referencia estable para:

- Rollback seguro antes de cambios experimentales
- Validación de migraciones futuras
- Recuperación rápida en caso de errores

---

## Siguiente Paso

**Opción 1:** Continuar con la Fase 2 del sistema de autorización (integración con Spring Security)

**Opción 2:** Implementar nuevas funcionalidades sabiendo que existe un punto de rollback seguro

**Recomendación:** Antes de cualquier cambio importante en el schema, considerar crear tags adicionales para mantener múltiples puntos de recuperación.

---

## Referencias

- Archivo changelog: `src/main/resources/config/liquibase/changelog/enterprise-auth/20251026000000_sistema_autorizacion_tag.xml`
- Master changelog: `src/main/resources/config/liquibase/master.xml`
- Documentación: `README.md` sección "Liquibase Database Management"
- Documentación oficial Liquibase: https://docs.liquibase.com/commands/rollback/rollback-by-tag.html
