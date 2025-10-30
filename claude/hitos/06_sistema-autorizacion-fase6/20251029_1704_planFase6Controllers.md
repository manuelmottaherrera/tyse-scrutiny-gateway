# Plan Completo: FASE 6 - REST API Controllers

**Fecha de creación:** 2025-10-29 17:04
**Proyecto:** Tyse Scrutiny Gateway - Sistema de Autorización Enterprise
**Branch:** `feature/enterprise-authorization-system`
**Fase:** FASE 6 - REST API Controllers

---

## 📋 Estado Actual

### Completado en Fase 5 ✅

- ✅ 4 servicios enterprise implementados (Authority, Permission, UserAuthority, UserPermission)
- ✅ 2 scheduled jobs (cleanup + warnings)
- ✅ 5 DTOs de autorización
- ✅ 9 correcciones críticas en servicios existentes
- ✅ @EnableScheduling habilitado
- ✅ 13 tests de integración (9 pasando)
- ✅ Compilación limpia sin errores
- ✅ Commit exitoso: `90978db`

### Servicios Disponibles para Exponer

1. **AuthorityService** - CRUD con auditoría completa
2. **PermissionService** - Gestión de permisos con validación
3. **UserAuthorityService** - Asignación/revocación de roles
4. **UserPermissionService** - Permisos directos temporales

---

## 🎯 Objetivo de Fase 6

Crear la capa de **REST API Controllers** para exponer los servicios enterprise vía HTTP, permitiendo la gestión completa del sistema de autorización desde clientes REST/frontend.

**Alcance:**

- 3 nuevos controllers REST
- Endpoints CRUD completos
- Documentación OpenAPI/Swagger
- Tests de integración para controllers
- Seguridad con @PreAuthorize

**NO incluye:**

- Frontend React (será Fase 7)
- Autenticación adicional (ya existe)

---

## 📝 FASE 6.1: UserAuthorityResource

### Descripción

Controller para gestionar asignaciones de authorities a usuarios.

**Ubicación:** `src/main/java/com/tyse/scrutiny/gateway/web/rest/UserAuthorityResource.java`

### Endpoints a Implementar

#### 1. GET /api/user-authorities/user/{userId}

**Descripción:** Obtener todas las authorities asignadas a un usuario
**Respuesta:** `List<UserAuthorityDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

```java
@GetMapping("/user-authorities/user/{userId}")
public Mono<ResponseEntity<List<UserAuthorityDTO>>> getUserAuthorities(@PathVariable Long userId)
```

#### 2. GET /api/user-authorities/user/{userId}/valid

**Descripción:** Obtener solo authorities válidas (activas + no expiradas)
**Respuesta:** `List<UserAuthorityDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 3. POST /api/user-authorities

**Descripción:** Asignar una authority a un usuario
**Body:** `AssignAuthorityRequest { userId, authorityId, expiresAt? }`
**Respuesta:** `UserAuthorityDTO`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 4. DELETE /api/user-authorities/{id}

**Descripción:** Revocar una asignación de authority
**Params:** `?reason=motivo`
**Respuesta:** `UserAuthorityDTO`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 5. GET /api/user-authorities/expiring

**Descripción:** Obtener asignaciones próximas a expirar
**Params:** `?days=7`
**Respuesta:** `List<UserAuthorityDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

### Request/Response DTOs Necesarios

**Crear:**

- `AssignAuthorityRequest.java` - Para POST endpoint
- `RevokeAuthorityRequest.java` - Para DELETE endpoint (con reason)

**Ya existen:**

- `UserAuthorityDTO.java` ✅

### Estimación

- **Líneas:** ~250-300
- **Tiempo:** 30-40 minutos

---

## 📝 FASE 6.2: PermissionResource

### Descripción

Controller para gestionar permisos del sistema.

**Ubicación:** `src/main/java/com/tyse/scrutiny/gateway/web/rest/PermissionResource.java`

### Endpoints a Implementar

#### 1. GET /api/permissions

**Descripción:** Obtener todos los permisos activos
**Respuesta:** `List<PermissionDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 2. GET /api/permissions/{id}

**Descripción:** Obtener un permiso por ID
**Respuesta:** `PermissionDTO`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 3. GET /api/permissions/resource/{resource}

**Descripción:** Obtener permisos de un recurso
**Respuesta:** `List<PermissionDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 4. POST /api/permissions

**Descripción:** Crear un nuevo permiso
**Body:** `CreatePermissionRequest { resource, action, description }`
**Respuesta:** `PermissionDTO`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 5. PUT /api/permissions/{id}

**Descripción:** Actualizar un permiso
**Body:** `UpdatePermissionRequest { description, isActive }`
**Respuesta:** `PermissionDTO`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

### Request/Response DTOs Necesarios

**Crear:**

- `CreatePermissionRequest.java`
- `UpdatePermissionRequest.java`

**Ya existen:**

- `PermissionDTO.java` ✅

### Estimación

- **Líneas:** ~200-250
- **Tiempo:** 25-35 minutos

---

## 📝 FASE 6.3: UserPermissionResource

### Descripción

Controller para gestionar permisos directos concedidos a usuarios.

**Ubicación:** `src/main/java/com/tyse/scrutiny/gateway/web/rest/UserPermissionResource.java`

### Endpoints a Implementar

#### 1. GET /api/user-permissions/user/{userId}

**Descripción:** Obtener permisos directos de un usuario
**Respuesta:** `List<UserPermissionDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 2. GET /api/user-permissions/user/{userId}/effective

**Descripción:** Obtener permisos efectivos (role-based + direct)
**Respuesta:** `List<PermissionDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 3. POST /api/user-permissions

**Descripción:** Conceder permiso directo a usuario
**Body:** `GrantPermissionRequest { userId, permissionId, expiresAt?, reason }`
**Respuesta:** `UserPermissionDTO`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 4. DELETE /api/user-permissions/{id}

**Descripción:** Revocar permiso directo
**Params:** `?reason=motivo`
**Respuesta:** `UserPermissionDTO`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

#### 5. GET /api/user-permissions/expiring

**Descripción:** Obtener grants próximos a expirar
**Params:** `?days=7`
**Respuesta:** `List<UserPermissionDTO>`
**Seguridad:** `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`

### Request/Response DTOs Necesarios

**Crear:**

- `GrantPermissionRequest.java`
- `RevokePermissionRequest.java`

**Ya existen:**

- `UserPermissionDTO.java` ✅

### Estimación

- **Líneas:** ~250-300
- **Tiempo:** 30-40 minutos

---

## 📝 FASE 6.4: Request/Response DTOs

### DTOs a Crear (7 nuevos)

**Ubicación:** `src/main/java/com/tyse/scrutiny/gateway/web/rest/request/`

1. **AssignAuthorityRequest.java** (~30 líneas)

   ```java
   {
     userId: Long,
     authorityId: Long,
     expiresAt: Instant?
   }
   ```

2. **RevokeAuthorityRequest.java** (~20 líneas)

   ```java
   {
     reason: String
   }
   ```

3. **CreatePermissionRequest.java** (~40 líneas)

   ```java
   {
     resource: String,
     action: String,
     description: String
   }
   ```

4. **UpdatePermissionRequest.java** (~30 líneas)

   ```java
   {
     description: String?,
     isActive: Boolean?
   }
   ```

5. **GrantPermissionRequest.java** (~40 líneas)

   ```java
   {
     userId: Long,
     permissionId: Long,
     expiresAt: Instant?,
     reason: String
   }
   ```

6. **RevokePermissionRequest.java** (~20 líneas)
   ```java
   {
     reason: String
   }
   ```

### Validaciones

Agregar validaciones Jakarta:

- `@NotNull` para campos requeridos
- `@NotBlank` para strings
- `@Size(min=5, max=500)` para reason
- `@Pattern` para resource/action

### Estimación

- **Líneas:** ~180-200
- **Tiempo:** 20-25 minutos

---

## 📝 FASE 6.5: Tests de Integración

### Tests a Crear

**Ubicación:** `src/test/java/com/tyse/scrutiny/gateway/web/rest/`

1. **UserAuthorityResourceIT.java** (~300 líneas)

   - Test asignación de authority
   - Test revocación
   - Test obtener authorities de usuario
   - Test filtrado por validez
   - Test próximos a expirar

2. **PermissionResourceIT.java** (~250 líneas)

   - Test CRUD de permisos
   - Test validación de patrones
   - Test búsqueda por resource

3. **UserPermissionResourceIT.java** (~300 líneas)
   - Test grant de permiso
   - Test revocación
   - Test permisos efectivos
   - Test próximos a expirar

### Patrón de Tests

```java
@IntegrationTest
@AutoConfigureWebTestClient
class UserAuthorityResourceIT {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void testAssignAuthority() {
    webTestClient
      .post()
      .uri("/api/user-authorities")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated()
      .expectBody(UserAuthorityDTO.class);
  }
}

```

### Estimación

- **Líneas:** ~850-900
- **Tiempo:** 60-80 minutos

---

## 📝 FASE 6.6: Documentación OpenAPI

### Anotaciones Swagger

Agregar a todos los controllers:

```java
@RestController
@RequestMapping("/api")
@Tag(name = "User Authority Management", description = "Endpoints para gestionar asignaciones de authorities a usuarios")
public class UserAuthorityResource {

    @Operation(
        summary = "Asignar authority a usuario",
        description = "Crea una nueva asignación de authority con expiración opcional"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Authority asignada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario o authority no encontrado"),
        @ApiResponse(responseCode = "409", description = "Authority ya asignada")
    })
    @PostMapping("/user-authorities")
    public Mono<ResponseEntity<UserAuthorityDTO>> assignAuthority(
        @RequestBody @Valid AssignAuthorityRequest request
    ) { ... }
}
```

### Swagger UI

Verificar en: `http://localhost:8080/swagger-ui.html`

### Estimación

- **Tiempo:** 15-20 minutos (agregar anotaciones durante implementación)

---

## 📊 Estimaciones Totales de Fase 6

| Componente            | Archivos | Líneas Aprox     | Tiempo Estimado |
| --------------------- | -------- | ---------------- | --------------- |
| Controllers REST      | 3        | ~700-850         | 85-115 min      |
| Request/Response DTOs | 6        | ~180-200         | 20-25 min       |
| Tests de integración  | 3        | ~850-900         | 60-80 min       |
| Documentación OpenAPI | -        | ~100             | 15-20 min       |
| **TOTAL**             | **12**   | **~1,830-2,050** | **180-240 min** |

**Tiempo estimado:** 3-4 horas

---

## 🔑 Decisiones Arquitectónicas

### 1. Seguridad en Controllers

**Decisión:** Todos los endpoints requieren `ROLE_ADMIN`

**Razón:**

- Gestión de autorización es operación administrativa sensible
- Evita escalación de privilegios
- Puede refinarse después con permisos granulares

### 2. Patrón Request/Response DTO

**Decisión:** DTOs separados para Request y Response

**Razón:**

- Mejor separación de concerns
- Validaciones específicas para input
- Response puede incluir campos calculados
- Más fácil de evolucionar

### 3. Uso de ServerWebExchange

**Decisión:** NO inyectar ServerWebExchange en controllers

**Razón:**

- Los servicios ya lo manejan internamente para auditoría
- Controllers más limpios y testeables
- WebFlux ya proporciona contexto reactivo

**Corrección:** Revisar AuthorityService para obtener exchange del contexto reactivo

### 4. Manejo de Errores

**Decisión:** Usar `@ExceptionHandler` en controllers

**Razón:**

- Respuestas HTTP consistentes
- Mapeo automático de excepciones de servicio
- Mejor experiencia de API

### 5. Paginación

**Decisión:** NO implementar paginación en Fase 6

**Razón:**

- Volumen de datos bajo (authorities y permissions limitados)
- Puede agregarse después si es necesario
- Simplifica implementación inicial

---

## 🚀 Plan de Ejecución

### Orden de Implementación

1. ✅ Crear plan (este documento)
2. ⏳ Crear Request/Response DTOs (6 archivos)
3. ⏳ Implementar PermissionResource (más simple, sin expiración)
4. ⏳ Implementar UserAuthorityResource (con expiración)
5. ⏳ Implementar UserPermissionResource (con expiración + efectivos)
6. ⏳ Agregar documentación OpenAPI
7. ⏳ Crear tests de integración (3 archivos)
8. ⏳ Compilar y ejecutar tests
9. ⏳ Generar reporte de completación
10. ⏳ Crear README del hito 06

### Verificaciones de Calidad

- [ ] Compilación sin errores
- [ ] Tests pasando (mínimo 70%)
- [ ] Swagger UI funcional
- [ ] Endpoints probados con Postman/curl
- [ ] Seguridad @PreAuthorize en todos los endpoints
- [ ] Validaciones Jakarta en Request DTOs

---

## 📚 Referencias Útiles

### Código Existente

- **AccountResource.java** → Patrón de controller reactivo
- **UserResource.java** → Ejemplo de CRUD REST
- **AuthorityResource.java** → Ya existe (podría extenderse)

### Servicios a Consumir

- **UserAuthorityService** → `src/main/java/com/tyse/scrutiny/gateway/service/authorization/`
- **PermissionService** → `src/main/java/com/tyse/scrutiny/gateway/service/authorization/`
- **UserPermissionService** → `src/main/java/com/tyse/scrutiny/gateway/service/authorization/`

### DTOs Existentes

- **UserAuthorityDTO** → `src/main/java/com/tyse/scrutiny/gateway/service/dto/authorization/`
- **PermissionDTO** → `src/main/java/com/tyse/scrutiny/gateway/service/dto/authorization/`
- **UserPermissionDTO** → `src/main/java/com/tyse/scrutiny/gateway/service/dto/authorization/`

---

## ⚠️ Consideraciones Importantes

### 1. AuthorityService y ServerWebExchange

El método `createAuthority()` requiere `ServerWebExchange` para auditoría. Opciones:

**Opción A (Recomendada):** Hacer exchange opcional

```java
public Mono<Authority> createAuthority(Authority authority, @Nullable ServerWebExchange exchange)
```

**Opción B:** Obtener del contexto reactivo

```java
Mono.deferContextual(ctx -> {
    ServerWebExchange exchange = ctx.getOrDefault("exchange", null);
    ...
})
```

### 2. Manejo de Errores Consistente

Crear `@RestControllerAdvice` global:

```java
@RestControllerAdvice
public class AuthorizationExceptionHandler {
    @ExceptionHandler(AuthorityNotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(AuthorityNotFoundException ex) {
        return ResponseEntity.status(404).body(...);
    }
}
```

### 3. Tests con Testcontainers

Los tests necesitan:

- PostgreSQL testcontainer (ya configurado)
- Datos de prueba en `@BeforeEach`
- Cleanup en `@AfterEach`
- Usuario autenticado con `@WithMockUser`

---

## 🎯 Criterios de Éxito

- [ ] 3 controllers REST implementados
- [ ] 6 Request/Response DTOs creados
- [ ] Todos los endpoints documentados con OpenAPI
- [ ] Tests de integración creados (mínimo 70% passing)
- [ ] Compilación limpia sin warnings críticos
- [ ] Swagger UI accesible y funcional
- [ ] Seguridad implementada en todos los endpoints
- [ ] Validaciones Jakarta en Request DTOs
- [ ] Reporte de completación generado

---

**Última actualización:** 2025-10-29 17:04
**Autor:** Claude Code
**Branch:** feature/enterprise-authorization-system
**Siguiente:** Implementación de DTOs y controllers
