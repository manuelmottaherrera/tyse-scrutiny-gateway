# Reporte: Estructura de Directorios para Sistema Enterprise

**Fecha:** 2025-10-24 14:45
**Fase:** FASE 0 - Preparación
**Paso:** 0.4 - Preparar estructura de archivos
**Propósito:** Crear la estructura de directorios necesaria para el nuevo sistema de autorización enterprise

---

## 📁 Estructura Completa Creada

### 1. Liquibase Changelogs

```
src/main/resources/config/liquibase/changelog/
└── enterprise-auth/
    └── (aquí se crearán los nuevos changelogs)
```

**Propósito:** Separar los changelogs de migración enterprise del resto para mejor organización.

---

### 2. Packages Java - Domain (Entidades)

```
src/main/java/com/tyse/scrutiny/gateway/
├── domain/
│   ├── authorization/          [NUEVO]
│   │   ├── Permission.java
│   │   ├── UserAuthority.java
│   │   ├── UserPermission.java
│   │   └── AuthorityAudit.java
│   └── enumeration/            [NUEVO]
│       ├── AuthorityCategory.java
│       └── AuditAction.java
```

**Entidades a crear:**

- `Permission` - Permisos granulares del sistema
- `UserAuthority` - Tabla pivote mejorada con auditoría
- `UserPermission` - Permisos directos a usuarios
- `AuthorityAudit` - Log de cambios en roles

**Enums a crear:**

- `AuthorityCategory` - SYSTEM, CUSTOM, TENANT_SPECIFIC
- `AuditAction` - CREATED, UPDATED, DELETED, ACTIVATED, DEACTIVATED

---

### 3. Packages Java - Repository (R2DBC)

```
src/main/java/com/tyse/scrutiny/gateway/
└── repository/
    └── authorization/          [NUEVO]
        ├── PermissionRepository.java
        ├── UserAuthorityRepository.java
        ├── UserPermissionRepository.java
        └── AuthorityAuditRepository.java
```

**Repositorios a crear:**

- Interfaces R2DBC reactivas para todas las entidades nuevas
- Custom queries con DatabaseClient si es necesario
- Métodos de consulta específicos (findByUserId, findByIsActiveTrue, etc.)

---

### 4. Packages Java - Service (Lógica de Negocio)

```
src/main/java/com/tyse/scrutiny/gateway/
└── service/
    ├── authorization/          [NUEVO]
    │   ├── AuthorityService.java
    │   ├── PermissionService.java
    │   ├── AuthorityAuditService.java
    │   └── ScheduledAuthorizationTasks.java
    └── dto/
        └── authorization/      [NUEVO]
            ├── AuthorityDTO.java
            ├── PermissionDTO.java
            ├── UserAuthorityDTO.java
            └── UserPermissionDTO.java
```

**Servicios a crear:**

- `AuthorityService` - CRUD de roles y gestión
- `PermissionService` - CRUD de permisos y verificación
- `AuthorityAuditService` - Registro y consulta de auditoría
- `ScheduledAuthorizationTasks` - Jobs para expiración automática

**DTOs a crear:**

- DTOs completos para cada entidad
- Incluir permisos efectivos calculados

---

### 5. Packages Java - Service Mappers

```
src/main/java/com/tyse/scrutiny/gateway/
└── service/
    └── mapper/
        └── authorization/      [NUEVO]
            ├── AuthorityMapper.java
            ├── PermissionMapper.java
            ├── UserAuthorityMapper.java
            └── UserPermissionMapper.java
```

**Mappers a crear:**

- Conversiones Entity ↔ DTO
- Métodos helper para transformaciones complejas

---

### 6. Packages Java - REST Controllers

```
src/main/java/com/tyse/scrutiny/gateway/
└── web/
    └── rest/
        └── authorization/      [NUEVO]
            ├── PermissionResource.java
            └── UserAuthorityResource.java
```

**Controllers a crear:**

- `PermissionResource` - CRUD de permisos
- `UserAuthorityResource` - Gestión de asignaciones
- Actualizar `AuthorityResource` existente
- Actualizar `UserResource` existente

---

### 7. Packages Java - Security & Config

```
src/main/java/com/tyse/scrutiny/gateway/
├── security/
│   └── permissions/            [NUEVO]
│       ├── PermissionEvaluator.java
│       ├── PermissionsConstants.java
│       └── SystemPermissions.java
└── config/
    └── authorization/          [NUEVO]
        └── AuthorizationConfiguration.java
```

**Clases de seguridad a crear:**

- `CustomPermissionEvaluator` - Para @PreAuthorize con permisos
- `PermissionsConstants` - Constantes de permisos del sistema
- `SystemPermissions` - Definición de permisos base

---

### 8. Packages Java - Tests

```
src/test/java/com/tyse/scrutiny/gateway/
├── domain/
│   └── authorization/          [NUEVO]
│       ├── PermissionTest.java
│       ├── UserAuthorityTest.java
│       └── ...
├── service/
│   └── authorization/          [NUEVO]
│       ├── AuthorityServiceTest.java
│       ├── PermissionServiceTest.java
│       └── ...
└── web/
    └── rest/
        └── authorization/      [NUEVO]
            ├── PermissionResourceIT.java
            └── ...
```

**Tests a crear:**

- Tests unitarios para todas las entidades
- Tests de servicios con mocks
- Tests de integración para APIs

---

## 📊 Resumen de Packages Creados

### Packages Java (src/main)

| Package                        | Propósito            | Archivos Estimados |
| ------------------------------ | -------------------- | ------------------ |
| `domain/authorization`         | Nuevas entidades     | 4 clases           |
| `domain/enumeration`           | Enums del sistema    | 2 enums            |
| `repository/authorization`     | Repositorios R2DBC   | 4 interfaces       |
| `service/authorization`        | Lógica de negocio    | 4 clases           |
| `service/dto/authorization`    | DTOs                 | 4 clases           |
| `service/mapper/authorization` | Mappers              | 4 clases           |
| `web/rest/authorization`       | REST APIs            | 2 clases           |
| `security/permissions`         | Seguridad y permisos | 3 clases           |
| `config/authorization`         | Configuración        | 1 clase            |

**Total:** ~28 archivos nuevos en src/main

### Packages Java (src/test)

| Package                  | Propósito          | Archivos Estimados |
| ------------------------ | ------------------ | ------------------ |
| `domain/authorization`   | Tests de entidades | 8 clases           |
| `service/authorization`  | Tests de servicios | 4 clases           |
| `web/rest/authorization` | Tests de APIs      | 2 clases           |

**Total:** ~14 archivos de test

---

## 🎯 Convenciones de Organización

### Separación por Contexto

En lugar de poner todas las clases nuevas en los packages existentes, se creó una sub-estructura `authorization/` para:

✅ **Ventajas:**

- Mejor organización y claridad
- Fácil identificar código del nuevo sistema
- Reduce acoplamiento con código legacy
- Facilita future refactoring
- Mejor para IDEs (navegación por paquetes)

⚠️ **Consideraciones:**

- Rompe un poco la convención plana de JHipster
- Pero es más escalable a largo plazo

### Naming Conventions

- **Entities:** Sufijo implícito (Permission, Authority)
- **Repositories:** Sufijo `Repository`
- **Services:** Sufijo `Service`
- **DTOs:** Sufijo `DTO`
- **Mappers:** Sufijo `Mapper`
- **Resources:** Sufijo `Resource`
- **Tests:** Sufijo `Test` (unit) o `IT` (integration)

---

## 📂 Estructura Visual Completa

```
tyse-scrutiny-gateway/
│
├── src/main/
│   ├── java/com/tyse/scrutiny/gateway/
│   │   ├── config/
│   │   │   └── authorization/              ← [NUEVO]
│   │   ├── domain/
│   │   │   ├── authorization/              ← [NUEVO]
│   │   │   └── enumeration/                ← [NUEVO]
│   │   ├── repository/
│   │   │   └── authorization/              ← [NUEVO]
│   │   ├── security/
│   │   │   └── permissions/                ← [NUEVO]
│   │   ├── service/
│   │   │   ├── authorization/              ← [NUEVO]
│   │   │   ├── dto/
│   │   │   │   └── authorization/          ← [NUEVO]
│   │   │   └── mapper/
│   │   │       └── authorization/          ← [NUEVO]
│   │   └── web/
│   │       └── rest/
│   │           └── authorization/          ← [NUEVO]
│   │
│   └── resources/config/liquibase/
│       └── changelog/
│           └── enterprise-auth/            ← [NUEVO]
│
├── src/test/
│   └── java/com/tyse/scrutiny/gateway/
│       ├── domain/
│       │   └── authorization/              ← [NUEVO]
│       ├── service/
│       │   └── authorization/              ← [NUEVO]
│       └── web/
│           └── rest/
│               └── authorization/          ← [NUEVO]
│
└── claude/                                 ← [NUEVO - sesión anterior]
    ├── context/
    └── reports/
```

---

## ✅ Verificación de Creación

Todos los directorios fueron creados exitosamente:

- ✅ **10 packages** en src/main/java
- ✅ **3 packages** en src/test/java
- ✅ **1 directorio** para Liquibase changelogs
- ✅ **2 directorios** para documentación Claude

**Total:** 16 directorios nuevos creados

---

## 🎯 Siguiente Paso

**FASE 1 - PASO 1.1:** Crear changelog maestro de Liquibase

La estructura está lista para comenzar a implementar:

1. Changelogs de Liquibase (migración de BD)
2. Entidades del dominio
3. Repositorios reactivos
4. Servicios y DTOs
5. APIs REST
6. Seguridad y permisos

---

## 📝 Notas Adicionales

- Los directorios están vacíos y listos para recibir código
- No se modificó ningún archivo existente
- La estructura sigue principios de Clean Architecture
- Organización por feature (authorization) en vez de por tipo
- Preparado para ~42 archivos nuevos de código productivo

---

**Fecha de creación:** 2025-10-24 14:45
**Estado:** ✅ Completado exitosamente
