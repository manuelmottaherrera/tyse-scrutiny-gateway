# Reporte de Inicio: FASE 3 - Repositorios R2DBC

**Fecha de inicio:** 2025-10-29 10:01
**Proyecto:** Tyse Scrutiny Gateway - Sistema de Autorización Enterprise
**Branch:** `feature/enterprise-authorization-system`
**Fase:** FASE 3 - Repositorios R2DBC

---

## 🎯 Objetivo de la Fase

Crear la capa de acceso a datos reactiva (repositories + row mappers + SQL helpers) para las entidades enterprise implementadas en Fase 2, migrando completamente de tablas legacy (`jhi_*`) a tablas enterprise (`scr_*`).

---

## 📋 Alcance de la Fase

### Repositorios a Crear (5 nuevos)

1. **PermissionRepository**

   - Interface básica R2DBC
   - Queries: findByName, findByResource, findByIsActiveTrue

2. **UserAuthorityRepository**

   - Interface con queries custom
   - Queries: findByUserId, findValidByUserId, findExpiredAssignments
   - Lógica de expiración y validación

3. **UserPermissionRepository**

   - Similar a UserAuthorityRepository
   - Queries: findByUserId, findValidByUserId, findExpiredGrants

4. **AuthorityAuditRepository**

   - Interface simple (solo append)
   - Queries: findByAuthorityId, findByChangedBy

5. **AuthorityRepository (actualización crítica)**
   - **BUG FIX:** Cambiar `R2dbcRepository<Authority, String>` → `R2dbcRepository<Authority, Long>`
   - Agregar: findByCode(), findByIsActiveTrue()

### Repositorios a Migrar (1 existente)

1. **UserRepository**
   - Migrar queries: `jhi_user_authority` → `scr_user_authority`
   - Cambiar joins: `authority_name` → `authority_id`
   - Agregar métodos para cargar UserAuthority y UserPermission

### Row Mappers (4 nuevos + 1 actualizado)

**Nuevos:**

1. PermissionRowMapper
2. UserAuthorityRowMapper
3. UserPermissionRowMapper
4. AuthorityAuditRowMapper

**Actualizar:**

1. UserRowMapper - Para nuevos campos de Authority

### SQL Helpers (4 nuevos)

1. PermissionSqlHelper
2. UserAuthoritySqlHelper
3. UserPermissionSqlHelper
4. AuthorityAuditSqlHelper

### Tests de Integración (Críticos)

**Foco principal:**

1. UserAuthorityRepositoryIT - Tests de expiración y validación
2. PermissionRepositoryIT - CRUD + queries por resource/action

**Actualizaciones:**

1. AuthorityRepositoryIT - Para Long ID
2. UserRepositoryIT - Para nuevos joins

---

## 🔧 Decisiones de Implementación

### 1. Migración Completa a scr_user_authority

**Decisión:** Migrar UserRepository completamente a las nuevas tablas `scr_*` en esta fase.

**Impacto:**

- Breaking change: Servicios existentes que usan UserRepository pueden fallar temporalmente
- Requiere actualizar tests existentes
- Preparación necesaria para Fase 5 (Servicios)

**Razón:** Evitar código híbrido legacy/enterprise que complique el mantenimiento.

### 2. SQL Helpers Completos

**Decisión:** Crear SQL Helpers siguiendo el patrón del proyecto (como UserSqlHelper).

**Ventajas:**

- Consistencia con código existente
- Facilita queries complejas y joins
- Mejor mantenibilidad a largo plazo

**Desventajas:**

- Más código inicial
- Mayor tiempo de implementación

### 3. Tests Críticos (No Exhaustivos)

**Decisión:** Priorizar tests de repositorios con lógica de negocio (UserAuthority, Permission).

**Foco:**

- UserAuthorityRepository: Expiración, validación, revocación
- PermissionRepository: Queries por resource/action

**Diferido para fases posteriores:**

- Tests exhaustivos de todos los queries custom
- Tests de performance y carga
- Tests de edge cases complejos

---

## 🏗️ Arquitectura y Patrones

### Patrón de Repositorio Identificado

```
{Entity}Repository (interface)
  ├── extends R2dbcRepository<Entity, ID>
  ├── extends {Entity}RepositoryInternal
  └── @Query methods (simple queries)

{Entity}RepositoryInternal (interface)
  └── Custom query methods

{Entity}RepositoryInternalImpl (class)
  ├── implements {Entity}RepositoryInternal
  ├── DatabaseClient db
  ├── R2dbcEntityTemplate template
  └── R2dbcConverter converter
```

### Patrón de Row Mapper

```java
@Service
public class {Entity}RowMapper implements BiFunction<Row, String, Entity> {
    private final ColumnConverter converter;

    public {Entity}RowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    @Override
    public Entity apply(Row row, String prefix) {
        Entity entity = new Entity();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        // ... más campos
        return entity;
    }
}
```

### Patrón de SQL Helper

```java
public class {Entity}SqlHelper {
    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        // ... más columnas
        return columns;
    }
}
```

---

## 📊 Métricas Estimadas

| Métrica                  | Estimación                        |
| ------------------------ | --------------------------------- |
| **Archivos a crear**     | ~18 archivos                      |
| **Archivos a modificar** | ~4 archivos                       |
| **Líneas de código**     | ~2,500-3,000 líneas               |
| **Repositorios nuevos**  | 4 (+ 1 actualizado)               |
| **Row Mappers**          | 4 nuevos + 1 actualizado          |
| **SQL Helpers**          | 4 nuevos                          |
| **Tests**                | 4-5 test classes (~60-80 métodos) |
| **Tiempo estimado**      | 5-6 horas                         |

---

## ⚠️ Riesgos Identificados

### 1. Breaking Changes en UserRepository

**Riesgo:** La migración de `jhi_user_authority` a `scr_user_authority` romperá código existente.

**Mitigación:**

- Actualizar tests en la misma fase
- Documentar cambios para Fase 5 (Servicios)
- Mantener backup de implementación original

### 2. Conflicto Authority String vs Long ID

**Riesgo:** AuthorityRepository actual usa `String` pero Authority entity ya usa `Long` (desde Fase 2).

**Impacto:** Código actualmente en conflicto, puede no compilar.

**Mitigación:** Priorizar corrección de AuthorityRepository como primera tarea.

### 3. Tests Existentes Pueden Fallar

**Riesgo:** Cambios en esquema de BD pueden romper tests de integración existentes.

**Mitigación:**

- Ejecutar tests frecuentemente durante desarrollo
- Actualizar tests en paralelo con implementación
- Usar Testcontainers para esquema limpio

---

## 🗂️ Orden de Implementación

### Fase 3.1: Corrección Crítica

1. ✅ Crear estructura `repository/authorization/`
2. ⏳ Actualizar AuthorityRepository (String → Long ID)

### Fase 3.2: Repositorios Simples

3. Crear PermissionRepository + implementation
4. Crear AuthorityAuditRepository

### Fase 3.3: Repositorios Complejos

5. Crear UserAuthorityRepository + implementation
6. Crear UserPermissionRepository + implementation

### Fase 3.4: Row Mappers

7. Crear PermissionRowMapper
8. Crear UserAuthorityRowMapper
9. Crear UserPermissionRowMapper
10. Crear AuthorityAuditRowMapper
11. Actualizar UserRowMapper

### Fase 3.5: SQL Helpers

12. Crear PermissionSqlHelper
13. Crear UserAuthoritySqlHelper
14. Crear UserPermissionSqlHelper
15. Crear AuthorityAuditSqlHelper

### Fase 3.6: Migración UserRepository

16. Actualizar UserRepository (queries + joins)
17. Actualizar UserRepositoryInternalImpl

### Fase 3.7: Tests Críticos

18. Crear PermissionRepositoryIT
19. Crear UserAuthorityRepositoryIT (con tests de expiración)
20. Actualizar AuthorityRepositoryIT
21. Actualizar UserRepositoryIT

### Fase 3.8: Validación y Documentación

22. Ejecutar suite completa de tests
23. Verificar esquema BD con queries manuales
24. Crear reporte final
25. Archivar en hitos/

---

## 🔍 Análisis de Código Existente Completado

### Hallazgos Clave

1. **UserRepository es complejo:**

   - Usa patrón `Tuple2` para joins
   - Método `groupBy + collectList` para agregación
   - Override de `delete()` para FK dependencies

2. **Row Mappers usan ColumnConverter:**

   - Conversión automática de tipos
   - Soporte para Enums
   - Prefix-based para joins

3. **Tests usan @IntegrationTest:**

   - Testcontainers para PostgreSQL embebido
   - Kafka embebido con @EmbeddedKafka
   - Cleanup pattern: join tables primero

4. **EntityManager es helper poderoso:**
   - LinkTable para tablas join
   - Queries dinámicas
   - Método `updateLinkTable()` útil

---

## 📚 Referencias Técnicas

### Entidades Fase 2 (Base)

- Authority (Long ID, auditoría)
- Permission (resource.action)
- UserAuthority (expiración, revocación)
- UserPermission (grants temporales)
- AuthorityAudit (log de cambios)

### Schema Liquibase

- Changelog: `20251024000000_enterprise_authorization_schema.xml`
- Tablas: scr_authority, scr_permission, scr_user_authority, scr_user_permission, scr_authority_audit

### Documentación Previa

- Fase 1: `claude/hitos/01_sistema-autorizacion-fase1/`
- Fase 2: `claude/hitos/03_sistema-autorizacion-fase2/`

---

## ✅ Estado Inicial

**Branch:** `feature/enterprise-authorization-system`
**Commits recientes:**

- fabd947 - feat(auth): Implementar entidades enterprise Fase 2
- d13843a - docs(auth): Reorganizar documentación en hitos completados

**Estructura creada:**

- ✅ `src/main/java/com/tyse/scrutiny/gateway/repository/authorization/` (directorio vacío listo)

**Siguiente paso:** Actualizar AuthorityRepository.java

---

## 🎯 Criterios de Éxito

La Fase 3 se considerará exitosa cuando:

- ✅ Todos los repositorios enterprise estén implementados
- ✅ AuthorityRepository use Long ID correctamente
- ✅ UserRepository migrado completamente a scr_user_authority
- ✅ Row mappers y SQL helpers funcionando
- ✅ Tests críticos pasen exitosamente
- ✅ No haya errores de compilación
- ✅ Queries reactivas retornen Mono<>/Flux<> correctamente
- ✅ Schema de BD coincida con entidades

---

**Fecha de inicio:** 2025-10-29 10:01
**Estado:** 🚀 EN PROGRESO - Fase 3.1 (Corrección Crítica)
**Próximo:** Actualizar AuthorityRepository.java

---

## 📝 Log de Progreso

### 2025-10-29 10:01

- ✅ Creada estructura de directorios `repository/authorization/`
- ✅ Reporte de inicio documentado
- ⏳ Iniciando corrección de AuthorityRepository...
