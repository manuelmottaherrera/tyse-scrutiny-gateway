🗺️ ESTRUCTURA DEL PLAN (8 Fases Principales)

FASE 0: Preparación y Backup
├── 0.1 Crear rama de trabajo
├── 0.2 Backup de base de datos actual
├── 0.3 Documentar estado actual
└── 0.4 Preparar estructura de archivos

FASE 1: Diseño de Base de Datos (Liquibase)
├── 1.1 Crear changelog maestro de migración
├── 1.2 Crear nuevas tablas (authorities, permissions, etc.)
├── 1.3 Migrar datos existentes
├── 1.4 Eliminar tablas antiguas
└── 1.5 Verificar integridad

FASE 2: Actualizar Modelos de Dominio (Entities)
├── 2.1 Actualizar Authority.java
├── 2.2 Crear Permission.java
├── 2.3 Crear UserAuthority.java
├── 2.4 Crear UserPermission.java
├── 2.5 Crear AuthorityAudit.java
├── 2.6 Crear enums y constantes
└── 2.7 Actualizar User.java

FASE 3: Actualizar Repositorios (R2DBC)
├── 3.1 Actualizar AuthorityRepository
├── 3.2 Crear PermissionRepository
├── 3.3 Crear UserAuthorityRepository
├── 3.4 Crear UserPermissionRepository
├── 3.5 Crear AuthorityAuditRepository
└── 3.6 Crear custom queries reactivos

FASE 4: Actualizar DTOs y Mappers
├── 4.1 Crear AuthorityDTO mejorado
├── 4.2 Crear PermissionDTO
├── 4.3 Crear UserAuthorityDTO
├── 4.4 Actualizar AdminUserDTO
├── 4.5 Actualizar UserDTO
└── 4.6 Crear/actualizar Mappers

FASE 5: Actualizar Servicios de Negocio
├── 5.1 Actualizar UserService
├── 5.2 Crear AuthorityService
├── 5.3 Crear PermissionService
├── 5.4 Crear AuthorityAuditService
└── 5.5 Crear scheduled jobs (expiración de roles)

FASE 6: Actualizar REST Controllers
├── 6.1 Actualizar AuthorityResource
├── 6.2 Actualizar UserResource
├── 6.3 Crear PermissionResource
├── 6.4 Actualizar AccountResource
└── 6.5 Actualizar documentación OpenAPI

FASE 7: Actualizar Seguridad (Spring Security)
├── 7.1 Actualizar SecurityConfiguration
├── 7.2 Crear custom PermissionEvaluator
├── 7.3 Actualizar JWTFilter
├── 7.4 Actualizar SecurityUtils
└── 7.5 Actualizar AuthoritiesConstants

FASE 8: Testing y Datos Iniciales
├── 8.1 Actualizar datos de seed (Liquibase data)
├── 8.2 Actualizar tests unitarios
├── 8.3 Actualizar tests de integración
├── 8.4 Testing manual completo
└── 8.5 Documentación final

FASE 9: Frontend (React + TypeScript)
├── 9.1 Actualizar modelos TypeScript
├── 9.2 Actualizar servicios de API
├── 9.3 Actualizar componentes de administración
├── 9.4 Actualizar guards y permisos
└── 9.5 Testing de UI

---

📝 PLAN DETALLADO CON PROMPTS

🔧 FASE 0: Preparación y Backup

PASO 0.1: Crear rama de trabajo

📌 PROMPT PARA TI:
"Crea una nueva rama llamada 'feature/enterprise-authorization-system' desde develop"

Qué esperar:

- Nueva rama Git creada
- Checkout a la nueva rama

---

PASO 0.2: Backup de base de datos

📌 PROMPT PARA TI:
"Crea un backup completo de la base de datos PostgreSQL actual y guárdalo en un archivo SQL con timestamp"

Qué esperar:

- Dump SQL de la BD completa
- Archivo guardado en algún directorio seguro

---

PASO 0.3: Documentar estado actual

📌 PROMPT PARA TI:
"Genera un reporte del estado actual de las tablas jhi_authority, jhi_user_authority y jhi_user. Incluye:

- Conteo de registros en cada tabla
- Lista de authorities existentes
- Ejemplo de 3 usuarios con sus roles"

Qué esperar:

- Documento o salida con estadísticas actuales
- Baseline para comparar después

---

PASO 0.4: Preparar estructura de archivos

📌 PROMPT PARA TI:
"Crea la estructura de directorios necesaria para el nuevo sistema:

- Crear directorio para nuevos changelogs de Liquibase
- Crear package structure para nuevas entidades (Permission, UserAuthority, etc.)
- Crear package para nuevos DTOs
- Listar la estructura creada"

Qué esperar:

- Directorios creados
- Confirmación de la estructura

---

🗄️ FASE 1: Diseño de Base de Datos (Liquibase)

PASO 1.1: Crear changelog maestro

📌 PROMPT PARA TI:
"Crea un nuevo changelog de Liquibase llamado '00000000000100_enterprise_authorization_schema.xml' que servirá como changelog
maestro para la migración del sistema de autorización. Incluye:

- Header con metadata de la migración
- Precondiciones (verificar que existen las tablas antiguas)
- Comentarios explicativos
- Estructura básica con changesets vacíos para cada paso

No implementes los changesets aún, solo la estructura."

Qué esperar:

- Archivo XML creado en src/main/resources/config/liquibase/changelog/
- Estructura básica lista para rellenar

---

PASO 1.2: Crear nuevas tablas

📌 PROMPT PARA TI:
"En el changelog '00000000000100_enterprise_authorization_schema.xml', implementa el changeset para crear las nuevas tablas del
sistema de autorización:

1. authorities (con todos los campos del diseño ideal)
2. permissions
3. authority_permissions
4. user_authorities (nueva, con auditoría completa)
5. user_permissions
6. authority_audit

Incluye todos los índices, constraints, y foreign keys necesarios. Usa las mejores prácticas de Liquibase para PostgreSQL con
R2DBC."

Qué esperar:

- Changesets completamente implementados
- Tablas listas para crear

---

PASO 1.3: Migrar datos existentes

📌 PROMPT PARA TI:
"En el mismo changelog, crea changesets para migrar los datos existentes de las tablas antiguas a las nuevas:

1. Migrar authorities (jhi_authority → authorities)
2. Migrar relaciones usuario-rol (jhi_user_authority → user_authorities)
3. Crear permisos básicos para ROLE_ADMIN y ROLE_USER
4. Asignar permisos a los roles
5. Setear campos de auditoría con valores por defecto razonables

Como los datos son de prueba, no te preocupes por la perfección, pero hazlo funcional."

Qué esperar:

- SQL de migración de datos
- Datos movidos a nuevas tablas

---

PASO 1.4: Eliminar tablas antiguas

📌 PROMPT PARA TI:
"En el mismo changelog, crea un changeset para eliminar las tablas antiguas del sistema de autorización:

1. Eliminar FK constraints primero
2. Drop table jhi_user_authority
3. Drop table jhi_authority

Incluye rollback steps para cada operación."

Qué esperar:

- Changesets para eliminar tablas viejas
- Rollback configurado

---

PASO 1.5: Verificar integridad

📌 PROMPT PARA TI:
"Crea un changeset final de verificación que:

1. Valide que todas las nuevas tablas existen
2. Valide que las tablas antiguas no existen
3. Valide que hay al menos 2 authorities (ROLE_ADMIN, ROLE_USER)
4. Valide que hay al menos 5 permissions
5. Valide que todos los usuarios tienen al menos un rol

Si alguna validación falla, debe hacer rollback de toda la migración."

Qué esperar:

- Changeset con precondiciones
- Validaciones implementadas

---

PASO 1.6: Registrar y ejecutar migración

📌 PROMPT PARA TI:
"Registra el nuevo changelog en master.xml y ejecuta la migración de Liquibase. Muéstrame:

1. El estado antes de la migración
2. La ejecución de la migración
3. El estado después de la migración
4. Listado de las nuevas tablas con sus estructuras"

Qué esperar:

- Changelog registrado en master.xml
- Migración ejecutada exitosamente
- Tablas nuevas en la BD

---

🎨 FASE 2: Actualizar Modelos de Dominio

PASO 2.1: Actualizar Authority.java

📌 PROMPT PARA TI:
"Actualiza la clase Authority.java al diseño ideal enterprise:

1. Cambiar de Persistable<String> a usar Long como ID
2. Agregar todos los campos nuevos (code, description, category, isSystem, isActive, hierarchyLevel)
3. Heredar de AbstractAuditingEntity<Long>
4. Agregar anotaciones R2DBC correctas
5. Agregar validaciones Jakarta
6. Actualizar equals/hashCode para usar Long id
7. Agregar relación @Transient con Set<Permission>

Mantén compatibilidad con R2DBC reactive."

Qué esperar:

- Authority.java completamente actualizado
- Compilación exitosa

---

PASO 2.2: Crear Permission.java

📌 PROMPT PARA TI:
"Crea la nueva clase de dominio Permission.java con:

1. Todos los campos del diseño ideal (id, name, resource, action, description, isActive, createdDate)
2. Anotaciones R2DBC para la tabla 'permissions'
3. Validaciones Jakarta
4. Implementar Serializable
5. equals/hashCode usando id
6. toString útil para debugging

Usa las mismas convenciones que las demás entidades del proyecto."

Qué esperar:

- Permission.java creado
- Sigue convenciones del proyecto

---

PASO 2.3: Crear UserAuthority.java

📌 PROMPT PARA TI:
"Crea la nueva clase de dominio UserAuthority.java para la tabla pivote mejorada con:

1. ID propio (BIGSERIAL)
2. Todos los campos de auditoría (assignedBy, assignedDate, expiresAt, revokedBy, revokedDate, revokedReason, isActive)
3. Referencias a User y Authority (userId, authorityId)
4. Anotaciones R2DBC
5. Validaciones Jakarta
6. Campo @Transient para cargar Authority con join
7. equals/hashCode usando id

Esta es la tabla pivote con esteroides."

Qué esperar:

- UserAuthority.java creado
- Incluye toda la auditoría

---

PASO 2.4: Crear UserPermission.java

📌 PROMPT PARA TI:
"Crea la clase de dominio UserPermission.java para permisos directos al usuario:

1. Campos: id, userId, permissionId, grantedBy, grantedDate, expiresAt, isActive, reason
2. Anotaciones R2DBC para tabla 'user_permissions'
3. Validaciones Jakarta
4. Campo @Transient para Permission
5. equals/hashCode usando id

Similar a UserAuthority pero más simple."

Qué esperar:

- UserPermission.java creado

---

PASO 2.5: Crear AuthorityAudit.java

📌 PROMPT PARA TI:
"Crea la clase de dominio AuthorityAudit.java para auditoría de cambios en roles:

1. Campos: id, authorityId, action, changedBy, changedDate, oldValues, newValues, ipAddress, userAgent
2. Usar tipo JSONB para oldValues y newValues (tipo Map o String en Java)
3. Enum AuditAction (CREATED, UPDATED, DELETED, ACTIVATED, DEACTIVATED)
4. Anotaciones R2DBC
5. equals/hashCode usando id

Esta tabla es append-only para auditoría."

Qué esperar:

- AuthorityAudit.java creado
- Enum AuditAction creado

---

PASO 2.6: Crear enums y constantes

📌 PROMPT PARA TI:
"Crea las clases de soporte necesarias:

1. Enum AuthorityCategory (SYSTEM, CUSTOM, TENANT_SPECIFIC)
2. Enum AuditAction (CREATED, UPDATED, DELETED, ACTIVATED, DEACTIVATED)
3. Actualizar AuthoritiesConstants con los nuevos roles y agregar PermissionsConstants
4. Crear clase SystemPermissions con constantes de permisos base

Colócalos en los packages apropiados siguiendo convenciones del proyecto."

Qué esperar:

- Enums creados
- Constantes actualizadas

---

PASO 2.7: Actualizar User.java

📌 PROMPT PARA TI:
"Actualiza User.java para trabajar con el nuevo sistema:

1. Mantener el Set<Authority> pero prepararlo para el nuevo diseño
2. Revisar que la relación sigue siendo @Transient (manejada por UserAuthority ahora)
3. No romper funcionalidad existente
4. Agregar comentarios sobre el cambio

Hazlo con cuidado porque User es crítico."

Qué esperar:

- User.java actualizado mínimamente
- Sin breaking changes graves

---

🔌 FASE 3: Actualizar Repositorios (R2DBC)

PASO 3.1: Actualizar AuthorityRepository

📌 PROMPT PARA TI:
"Actualiza AuthorityRepository.java:

1. Cambiar de R2dbcRepository<Authority, String> a R2dbcRepository<Authority, Long>
2. Agregar método findByName(String name): Mono<Authority>
3. Agregar método findByCode(String code): Mono<Authority>
4. Agregar método findByIsActiveTrue(): Flux<Authority>
5. Agregar método findByCategory(AuthorityCategory category): Flux<Authority>
6. Agregar custom queries si son necesarias

Usa convenciones reactive de Spring Data R2DBC."

Qué esperar:

- AuthorityRepository actualizado
- Métodos reactivos correctos

---

PASO 3.2: Crear PermissionRepository

📌 PROMPT PARA TI:
"Crea PermissionRepository.java con:

1. Extender R2dbcRepository<Permission, Long>
2. Método findByResource(String resource): Flux<Permission>
3. Método findByResourceAndAction(String resource, String action): Mono<Permission>
4. Método findByIsActiveTrue(): Flux<Permission>
5. Método findByName(String name): Mono<Permission>

Sigue el patrón de los demás repositorios del proyecto."

Qué esperar:

- PermissionRepository creado

---

PASO 3.3: Crear UserAuthorityRepository

📌 PROMPT PARA TI:
"Crea UserAuthorityRepository.java con:

1. Extender R2dbcRepository<UserAuthority, Long>
2. findByUserIdAndIsActiveTrue(Long userId): Flux<UserAuthority>
3. findByAuthorityIdAndIsActiveTrue(Long authorityId): Flux<UserAuthority>
4. findByUserIdAndAuthorityIdAndIsActiveTrue(Long userId, Long authorityId): Mono<UserAuthority>
5. findByIsActiveTrueAndExpiresAtBefore(Instant date): Flux<UserAuthority>
6. Custom query para obtener authorities de un usuario con JOIN

Este es crítico para el sistema de autorización."

Qué esperar:

- UserAuthorityRepository creado
- Queries reactivos correctos

---

PASO 3.4: Crear UserPermissionRepository

📌 PROMPT PARA TI:
"Crea UserPermissionRepository.java con:

1. Extender R2dbcRepository<UserPermission, Long>
2. findByUserIdAndIsActiveTrue(Long userId): Flux<UserPermission>
3. findByPermissionIdAndIsActiveTrue(Long permissionId): Flux<UserPermission>
4. findByUserIdAndPermissionIdAndIsActiveTrue(Long userId, Long permissionId): Mono<UserPermission>
5. findByIsActiveTrueAndExpiresAtBefore(Instant date): Flux<UserPermission>

Similar a UserAuthorityRepository."

Qué esperar:

- UserPermissionRepository creado

---

PASO 3.5: Crear AuthorityAuditRepository

📌 PROMPT PARA TI:
"Crea AuthorityAuditRepository.java con:

1. Extender R2dbcRepository<AuthorityAudit, Long>
2. findByAuthorityId(Long authorityId): Flux<AuthorityAudit>
3. findByChangedBy(String changedBy): Flux<AuthorityAudit>
4. findByChangedDateBetween(Instant start, Instant end): Flux<AuthorityAudit>
5. findByAction(AuditAction action): Flux<AuthorityAudit>

Para consultas de auditoría."

Qué esperar:

- AuthorityAuditRepository creado

---

PASO 3.6: Callbacks R2DBC (si necesarios)

📌 PROMPT PARA TI:
"Revisa y actualiza los callbacks R2DBC existentes:

1. Verificar AuthorityCallback.java si existe
2. Crear UserAuthorityCallback si es necesario para cargar Authority en UserAuthority
3. Asegurar que los callbacks funcionen con el nuevo diseño

Los callbacks son para relaciones @Transient en R2DBC."

Qué esperar:

- Callbacks actualizados o creados
- Relaciones funcionando

---

📦 FASE 4: Actualizar DTOs y Mappers

PASO 4.1: Crear AuthorityDTO mejorado

📌 PROMPT PARA TI:
"Crea un nuevo AuthorityDTO.java (o actualiza el existente) con:

1. Todos los campos de Authority (id, name, code, description, category, isSystem, isActive, hierarchyLevel)
2. Set<PermissionDTO> permissions
3. Campos de auditoría (createdBy, createdDate, lastModifiedBy, lastModifiedDate)
4. Constructor desde Authority
5. equals/hashCode/toString

Este DTO es para el frontend y API."

Qué esperar:

- AuthorityDTO completo creado

---

PASO 4.2: Crear PermissionDTO

📌 PROMPT PARA TI:
"Crea PermissionDTO.java con:

1. Campos: id, name, resource, action, description, isActive, createdDate
2. Constructor desde Permission
3. equals/hashCode/toString
4. Validaciones Jakarta si es necesario

DTO simple para permisos."

Qué esperar:

- PermissionDTO creado

---

PASO 4.3: Crear UserAuthorityDTO

📌 PROMPT PARA TI:
"Crea UserAuthorityDTO.java con:

1. Campos: id, userId, authorityId, authority (AuthorityDTO), assignedBy, assignedDate, expiresAt, isActive
2. Campos de revocación: revokedBy, revokedDate, revokedReason
3. Constructor desde UserAuthority
4. equals/hashCode/toString

Para mostrar asignaciones de roles en el frontend."

Qué esperar:

- UserAuthorityDTO creado

---

PASO 4.4: Actualizar AdminUserDTO

📌 PROMPT PARA TI:
"Actualiza AdminUserDTO.java para el nuevo sistema:

1. Cambiar Set<String> authorities a Set<AuthorityDTO> authorities (o mantener ambos para compatibilidad)
2. Agregar Set<String> effectivePermissions (calculado)
3. Actualizar constructor desde User para usar Authority.getId() y Authority.getName()
4. Mantener compatibilidad con código existente

Este es crítico para no romper el frontend actual."

Qué esperar:

- AdminUserDTO actualizado
- Compatibilidad mantenida

---

PASO 4.5: Actualizar UserDTO

📌 PROMPT PARA TI:
"Actualiza UserDTO.java similar a AdminUserDTO:

1. Revisar uso de authorities
2. Actualizar constructor
3. Mantener compatibilidad

UserDTO es la versión pública (menos info que AdminUserDTO)."

Qué esperar:

- UserDTO actualizado

---

PASO 4.6: Crear/Actualizar Mappers

📌 PROMPT PARA TI:
"Actualiza UserMapper.java y crea mappers necesarios:

1. Actualizar UserMapper para trabajar con nuevo Authority
2. Crear AuthorityMapper (Authority ↔ AuthorityDTO)
3. Crear PermissionMapper (Permission ↔ PermissionDTO)
4. Agregar métodos helper para conversiones

Si el proyecto usa MapStruct, usa MapStruct. Si no, mappers manuales."

Qué esperar:

- Mappers actualizados/creados
- Conversiones funcionando

---

⚙️ FASE 5: Actualizar Servicios de Negocio

PASO 5.1: Actualizar UserService

📌 PROMPT PARA TI:
"Actualiza UserService.java para trabajar con el nuevo sistema de autorización:

1. Inyectar UserAuthorityRepository
2. Actualizar método de asignar roles para usar UserAuthority
3. Actualizar método de crear usuario para asignar roles con auditoría
4. Actualizar método de actualizar usuario
5. Agregar método para obtener permisos efectivos de un usuario
6. Mantener todo reactivo (Mono/Flux)

Este es un cambio crítico, hazlo con mucho cuidado."

Qué esperar:

- UserService actualizado
- Tests de compilación pasando

---

PASO 5.2: Crear AuthorityService

📌 PROMPT PARA TI:
"Crea AuthorityService.java para gestionar roles:

1. CRUD completo para Authority (create, update, delete, findAll, findOne)
2. Método para asignar/revocar permisos a un rol
3. Método para activar/desactivar rol
4. Validaciones (no borrar roles del sistema)
5. Integración con AuthorityAuditService para registrar cambios
6. Todo reactivo (Mono/Flux)

Servicio nuevo para gestión de roles."

Qué esperar:

- AuthorityService creado
- CRUD funcional

---

PASO 5.3: Crear PermissionService

📌 PROMPT PARA TI:
"Crea PermissionService.java para gestionar permisos:

1. CRUD completo para Permission
2. Método para obtener permisos de un rol
3. Método para obtener permisos efectivos de un usuario (rol + directos)
4. Método para verificar si usuario tiene permiso específico
5. Todo reactivo (Mono/Flux)

Servicio para lógica de permisos."

Qué esperar:

- PermissionService creado
- Lógica de permisos implementada

---

PASO 5.4: Crear AuthorityAuditService

📌 PROMPT PARA TI:
"Crea AuthorityAuditService.java para auditoría:

1. Método para registrar cambio en Authority
2. Método para obtener historial de un Authority
3. Método para obtener cambios por usuario
4. Método para obtener cambios en rango de fechas
5. Helper para convertir objetos a JSON
6. Todo reactivo (Mono/Flux)

Servicio para tracking de cambios."

Qué esperar:

- AuthorityAuditService creado
- Auditoría funcionando

---

PASO 5.5: Crear scheduled jobs

📌 PROMPT PARA TI:
"Crea un componente ScheduledAuthorizationTasks.java con:

1. Job para revocar automáticamente roles expirados (cada hora)
2. Job para revocar permisos directos expirados (cada hora)
3. Job para limpiar auditorías antiguas (mensual, opcional)
4. Logging apropiado
5. Manejo de errores

Usa @Scheduled de Spring."

Qué esperar:

- Scheduled jobs creados
- Configurados con cron

---

🌐 FASE 6: Actualizar REST Controllers

PASO 6.1: Actualizar AuthorityResource

📌 PROMPT PARA TI:
"Actualiza AuthorityResource.java (si existe) o créalo:

1. Endpoint GET /api/authorities (listar todos)
2. Endpoint GET /api/authorities/{id} (obtener uno)
3. Endpoint POST /api/authorities (crear)
4. Endpoint PUT /api/authorities/{id} (actualizar)
5. Endpoint DELETE /api/authorities/{id} (eliminar si no es sistema)
6. Endpoint GET /api/authorities/{id}/permissions (permisos del rol)
7. Endpoint POST /api/authorities/{id}/permissions (asignar permiso)
8. Endpoint DELETE /api/authorities/{id}/permissions/{permissionId} (revocar permiso)
9. Usar AuthorityService
10. @PreAuthorize solo ADMIN
11. Documentación OpenAPI
12. ResponseEntity con Mono/Flux

REST API completo para gestión de roles."

Qué esperar:

- AuthorityResource completo
- Endpoints funcionales

---

PASO 6.2: Actualizar UserResource

📌 PROMPT PARA TI:
"Actualiza UserResource.java para trabajar con nuevo sistema:

1. Actualizar endpoints que devuelven usuarios para usar nuevo AdminUserDTO
2. Actualizar endpoint de crear usuario
3. Actualizar endpoint de actualizar usuario
4. Agregar endpoint GET /api/users/{login}/authorities (ver roles asignados)
5. Agregar endpoint POST /api/users/{login}/authorities/{authorityId} (asignar rol)
6. Agregar endpoint DELETE /api/users/{login}/authorities/{authorityId} (revocar rol)
7. Agregar endpoint GET /api/users/{login}/effective-permissions (permisos efectivos)
8. Actualizar documentación OpenAPI

Hazlo gradualmente para no romper endpoints existentes."

Qué esperar:

- UserResource actualizado
- Nuevos endpoints agregados

---

PASO 6.3: Crear PermissionResource

📌 PROMPT PARA TI:
"Crea PermissionResource.java:

1. GET /api/permissions (listar todos)
2. GET /api/permissions/{id} (obtener uno)
3. POST /api/permissions (crear)
4. PUT /api/permissions/{id} (actualizar)
5. DELETE /api/permissions/{id} (eliminar)
6. GET /api/permissions/by-resource/{resource} (filtrar por recurso)
7. @PreAuthorize solo ADMIN
8. Documentación OpenAPI
9. ResponseEntity con Mono/Flux

REST API para gestión de permisos."

Qué esperar:

- PermissionResource creado
- CRUD completo

---

PASO 6.4: Actualizar AccountResource

📌 PROMPT PARA TI:
"Actualiza AccountResource.java si es necesario:

1. Verificar endpoint GET /api/account
2. Asegurar que devuelve authorities correctamente
3. Actualizar para usar nuevo AdminUserDTO
4. No romper funcionalidad de login/registro

AccountResource es para el usuario autenticado actual."

Qué esperar:

- AccountResource actualizado si es necesario
- Login funcionando

---

PASO 6.5: Actualizar documentación OpenAPI

📌 PROMPT PARA TI:
"Revisa y actualiza las anotaciones OpenAPI en todos los controllers actualizados:

1. @Operation con descripciones claras
2. @ApiResponse para cada posible respuesta
3. @Parameter para parámetros
4. Ejemplos en @Schema
5. Tags apropiados

Para que Swagger UI esté bien documentado."

Qué esperar:

- Documentación OpenAPI completa
- Swagger UI actualizado

---

🔐 FASE 7: Actualizar Seguridad (Spring Security)

PASO 7.1: Actualizar SecurityConfiguration

📌 PROMPT PARA TI:
"Actualiza SecurityConfiguration.java:

1. Verificar que el authentication manager sigue funcionando
2. Actualizar configuración de authorities
3. Agregar configuración para PermissionEvaluator (siguiente paso)
4. Revisar filtros de seguridad
5. Mantener compatibilidad con JWT

Este es crítico para seguridad."

Qué esperar:

- SecurityConfiguration actualizado
- Seguridad funcionando

---

PASO 7.2: Crear custom PermissionEvaluator

📌 PROMPT PARA TI:
"Crea CustomPermissionEvaluator.java que implemente PermissionEvaluator:

1. Implementar hasPermission(Authentication, Object, Object)
2. Implementar hasPermission(Authentication, Serializable, String, Object)
3. Lógica para verificar permisos del usuario (roles + permisos directos)
4. Integrar con PermissionService
5. Cachear resultados si es posible (Reactor cache)

Esto permite usar @PreAuthorize('hasPermission(...)')"

Qué esperar:

- PermissionEvaluator creado
- @PreAuthorize con permisos funcionando

---

PASO 7.3: Actualizar JWTFilter

📌 PROMPT PARA TI:
"Revisa y actualiza JWTFilter.java si es necesario:

1. Verificar que carga authorities correctamente
2. Asegurar que funciona con Long IDs
3. Mantener compatibilidad con tokens existentes

JWTFilter parsea el token y carga authorities."

Qué esperar:

- JWTFilter funcionando
- Tokens validando correctamente

---

PASO 7.4: Actualizar SecurityUtils

📌 PROMPT PARA TI:
"Actualiza SecurityUtils.java:

1. Método getCurrentUserLogin(): Mono<String>
2. Método isAuthenticated(): Mono<Boolean>
3. Método hasCurrentUserThisAuthority(String authority): Mono<Boolean>
4. Agregar hasCurrentUserPermission(String permission): Mono<Boolean>
5. Agregar getCurrentUserAuthorities(): Mono<Set<String>>
6. Mantener reactivo

Utilities para trabajar con seguridad en código."

Qué esperar:

- SecurityUtils actualizado
- Nuevos métodos agregados

---

PASO 7.5: Actualizar AuthoritiesConstants

📌 PROMPT PARA TI:
"Actualiza AuthoritiesConstants.java y crea PermissionsConstants.java:

1. Mantener ADMIN = 'ROLE_ADMIN', USER = 'ROLE_USER'
2. Agregar nuevas constantes si las hay
3. Crear PermissionsConstants con permisos estándar (USER_CREATE, USER_READ, etc.)

Constantes para usar en @PreAuthorize."

Qué esperar:

- Constantes actualizadas
- PermissionsConstants creado

---

🧪 FASE 8: Testing y Datos Iniciales

PASO 8.1: Actualizar datos de seed (Liquibase)

📌 PROMPT PARA TI:
"Actualiza o crea los archivos CSV de datos iniciales en src/main/resources/config/liquibase/data/:

1. authority.csv - Roles del sistema (ROLE_ADMIN, ROLE_USER, ROLE_MANAGER)
2. permission.csv - Permisos base del sistema
3. authority_permission.csv - Asignar permisos a roles
4. user.csv - Usuario admin y user de prueba
5. user_authority.csv - Asignar roles a usuarios

Datos limpios y funcionales para desarrollo."

Qué esperar:

- CSVs actualizados
- Datos de seed correctos

---

PASO 8.2: Actualizar tests unitarios

📌 PROMPT PARA TI:
"Actualiza los tests unitarios afectados:

1. AuthorityTest.java
2. UserTest.java
3. AuthorityServiceTest (crear si no existe)
4. PermissionServiceTest (crear)
5. UserServiceTest

Usa mocks para repositorios. Prueba la lógica de negocio."

Qué esperar:

- Tests unitarios pasando
- Cobertura razonable

---

PASO 8.3: Actualizar tests de integración

📌 PROMPT PARA TI:
"Actualiza los tests de integración:

1. AuthorityResourceIT.java
2. UserResourceIT.java
3. PermissionResourceIT (crear)
4. AccountResourceIT.java

Usa @IntegrationTest. Prueba endpoints completos con BD."

Qué esperar:

- Tests de integración pasando
- APIs probadas end-to-end

---

PASO 8.4: Testing manual completo

📌 PROMPT PARA TI:
"Realiza testing manual completo:

1. Levantar aplicación (./mvnw)
2. Verificar que inicia correctamente
3. Probar login con usuario admin
4. Probar login con usuario regular
5. Probar endpoints de /api/authorities
6. Probar endpoints de /api/permissions
7. Probar endpoints de /api/users
8. Verificar Swagger UI en /swagger-ui.html
9. Probar asignar/revocar roles
10. Verificar logs por errores

Dame un reporte de qué funciona y qué no."

Qué esperar:

- Reporte de testing manual
- Lista de bugs encontrados

---

PASO 8.5: Documentación final

📌 PROMPT PARA TI:
"Crea documentación del nuevo sistema:

1. README.md o AUTHORIZATION.md explicando el nuevo sistema
2. Diagrama ER de las nuevas tablas (puede ser texto/mermaid)
3. Ejemplos de uso de permisos en código
4. Guía de migración de datos (aunque no aplicable para ti)
5. FAQ común

Documentación para el futuro equipo."

Qué esperar:

- Documentación completa
- Archivos .md creados

---

🎨 FASE 9: Frontend (React + TypeScript)

PASO 9.1: Actualizar modelos TypeScript

📌 PROMPT PARA TI:
"Actualiza los modelos TypeScript en src/main/webapp/app/shared/model/:

1. authority.model.ts - Agregar campos nuevos (id, code, description, etc.)
2. Crear permission.model.ts
3. Crear user-authority.model.ts
4. Actualizar user.model.ts para usar nuevo authority

Tipos correctos para TypeScript."

Qué esperar:

- Modelos TypeScript actualizados
- Sin errores de compilación TS

---

PASO 9.2: Actualizar servicios de API

📌 PROMPT PARA TI:
"Actualiza los servicios de API en src/main/webapp/app/shared/reducers/ o services/:

1. Actualizar llamadas a /api/authorities
2. Crear servicio para /api/permissions
3. Actualizar servicio de users
4. Usar axios con tipos correctos

Capa de servicios para llamar al backend."

Qué esperar:

- Servicios API actualizados
- Llamadas HTTP correctas

---

PASO 9.3: Actualizar componentes de administración

📌 PROMPT PARA TI:
"Actualiza los componentes de administración de usuarios en src/main/webapp/app/modules/administration/:

1. Actualizar user-management.tsx para mostrar nuevos campos
2. Actualizar user-management-detail.tsx
3. Actualizar user-management-update.tsx para asignar roles con el nuevo sistema
4. Crear authority-management.tsx (listado de roles)
5. Crear permission-management.tsx (listado de permisos)

UI de administración completa."

Qué esperar:

- Componentes actualizados
- UI funcional

---

PASO 9.4: Actualizar guards y permisos

📌 PROMPT PARA TI:
"Revisa y actualiza la lógica de permisos en el frontend:

1. Revisar PrivateRoute.tsx
2. Agregar helper hasPermission() si es necesario
3. Actualizar menús para mostrar/ocultar según permisos
4. Actualizar header/navbar

Control de acceso en el frontend."

Qué esperar:

- Guards actualizados
- Permisos funcionando en UI

---

PASO 9.5: Testing de UI

📌 PROMPT PARA TI:
"Testing manual del frontend:

1. Levantar frontend (./npmw start)
2. Probar login
3. Navegar a Administración > Gestión de Usuarios
4. Navegar a Administración > Gestión de Roles (si lo creaste)
5. Navegar a Administración > Gestión de Permisos (si lo creaste)
6. Probar asignar/revocar roles desde UI
7. Verificar que menús aparecen según permisos
8. Probar en diferentes navegadores

Dame un reporte de qué funciona en la UI."

Qué esperar:

- Reporte de testing UI
- Screenshots si hay problemas

---

🎯 RESUMEN DE PROMPTS POR FASE

FASE 0 (Preparación): 4 prompts
FASE 1 (Base de Datos): 6 prompts
FASE 2 (Entidades): 7 prompts
FASE 3 (Repositorios): 6 prompts
FASE 4 (DTOs): 6 prompts
FASE 5 (Servicios): 5 prompts
FASE 6 (Controllers): 5 prompts
FASE 7 (Seguridad): 5 prompts
FASE 8 (Testing): 5 prompts
FASE 9 (Frontend): 5 prompts

TOTAL: ~54 prompts

---

📊 Estimación de Tiempo

- FASE 0-1 (Preparación + BD): 2-3 horas
- FASE 2-3 (Entidades + Repos): 3-4 horas
- FASE 4 (DTOs): 1-2 horas
- FASE 5 (Servicios): 3-4 horas
- FASE 6 (Controllers): 2-3 horas
- FASE 7 (Seguridad): 2-3 horas
- FASE 8 (Testing): 3-4 horas
- FASE 9 (Frontend): 3-4 horas

TOTAL ESTIMADO: 19-27 horas de trabajo efectivo

Distribuido en sesiones de 2-3 horas, serían unas 8-10 sesiones.

---

✅ Checkpoints Importantes

Después de cada fase, deberías poder:

- ✅ Fase 1: BD migrada, tablas nuevas funcionando
- ✅ Fase 3: Código compila sin errores
- ✅ Fase 5: Backend arranca sin errores
- ✅ Fase 6: APIs responden correctamente
- ✅ Fase 7: Login funciona
- ✅ Fase 8: Tests pasan
- ✅ Fase 9: Frontend funciona

---

🆘 Plan de Rollback

Si algo sale mal en cualquier punto:

📌 PROMPT DE EMERGENCIA:
"Necesito hacer rollback completo. Por favor:

1. Revertir todos los cambios de Git en la rama actual
2. Hacer rollback de Liquibase a la versión anterior
3. Restaurar el backup de la base de datos
4. Verificar que todo vuelve al estado inicial"

---

🚀 ¿Listo para Empezar?

Cuando estés listo, dime:

"Empecemos con la Fase 0, Paso 0.1"

Y vamos paso a paso, a tu ritmo. Podemos pausar en cualquier momento y retomar después. Lo importante es ir con calma, verificar
cada paso, y no apresurarnos.

¿Tienes alguna pregunta sobre el plan antes de empezar? 😊

---
