# Plan de Implementación: FASE 7 - Integración con Spring Security

**Fecha:** 2025-10-30 02:52
**Proyecto:** Tyse Scrutiny Gateway - Sistema de Autorización Enterprise
**Branch:** `feature/enterprise-authorization-system`
**Fase:** FASE 7 - Integración con Spring Security
**Estado:** ✅ COMPLETADA

---

## 🎯 Objetivo

Integrar el sistema de autorización enterprise con Spring Security, **reemplazando completamente** el sistema legacy de JHipster. El sistema usará exclusivamente las nuevas tablas (`scr_authority`, `scr_permission`, `scr_user_authority`, `scr_user_permission`).

**IMPORTANTE:** No mantener compatibilidad con sistema legacy. Reemplazo total.

---

## 📋 Subtareas de Fase 7

### 7.1: Actualizar DomainUserDetailsService ⏳

**Objetivo:** Cargar authorities y permisos desde las nuevas tablas enterprise

**Archivo a modificar:**

- `src/main/java/com/tyse/scrutiny/gateway/security/DomainUserDetailsService.java`

**Cambios necesarios:**

1. Inyectar `UserAuthorityService` y `UserPermissionService`
2. Modificar método `createSpringSecurityUser()` para:
   - Cargar authorities válidas desde `scr_user_authority`
   - Cargar permissions válidas desde `scr_user_permission`
   - Cargar permissions heredadas de authorities (via `scr_authority_permission`)
3. Convertir a `GrantedAuthority`:
   - Authorities: `SimpleGrantedAuthority(authorityCode)` → "ROLE_ADMIN"
   - Permissions: `SimpleGrantedAuthority(resource + ":" + action)` → "user:create"
4. Retornar colección combinada (roles + permisos)

**Lógica de permisos efectivos:**

```java
Set<GrantedAuthority> authorities = new HashSet<>();

// 1. Cargar roles/authorities
userAuthorityService.getValidAuthorities(userId)
    .flatMap(ua -> authorityRepository.findById(ua.getAuthorityId()))
    .map(a -> new SimpleGrantedAuthority(a.getCode()))
    .collect(Collectors.toSet());

// 2. Cargar permisos directos
userPermissionService.getValidPermissions(userId)
    .flatMap(up -> permissionRepository.findById(up.getPermissionId()))
    .map(p -> new SimpleGrantedAuthority(p.getResource() + ":" + p.getAction()))
    .collect(Collectors.toSet());

// 3. Cargar permisos heredados de roles
// (via authorityPermissionRepository)
```

**Estimación:** ~1 hora

---

### 7.2: Crear PermissionEvaluator Personalizado ⏳

**Objetivo:** Implementar evaluación de permisos para `@PreAuthorize` con SpEL

**Archivo a crear:**

- `src/main/java/com/tyse/scrutiny/gateway/security/EnterprisePermissionEvaluator.java`

**Implementación:**

```java
@Component
public class EnterprisePermissionEvaluator implements ReactivePermissionEvaluator {

  @Override
  public Mono<Boolean> hasPermission(Mono<Authentication> authentication, Object targetDomainObject, Object permission) {
    return authentication
      .flatMapIterable(Authentication::getAuthorities)
      .map(GrantedAuthority::getAuthority)
      .any(auth -> auth.equals(permission.toString()))
      .defaultIfEmpty(false);
  }

  @Override
  public Mono<Boolean> hasPermission(Mono<Authentication> authentication, Serializable targetId, String targetType, Object permission) {
    return hasPermission(authentication, null, permission);
  }
}

```

**Uso:**

```java
@PreAuthorize("hasPermission(null, 'user:create')")
public Mono<User> createUser(User user) { ... }
```

**Estimación:** ~45 minutos

---

### 7.3: Configurar PermissionEvaluator en SecurityConfiguration ⏳

**Objetivo:** Habilitar evaluación de permisos en Spring Security

**Archivo a modificar:**

- `src/main/java/com/tyse/scrutiny/gateway/config/SecurityConfiguration.java`

**Cambios:**

```java
@Configuration
@EnableReactiveMethodSecurity // Ya existe
public class SecurityConfiguration {

  @Bean
  public ReactiveMethodSecurityExpressionHandler methodSecurityExpressionHandler(EnterprisePermissionEvaluator permissionEvaluator) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
  }
}

```

**Estimación:** ~20 minutos

---

### 7.4: Actualizar SecurityUtils ⏳

**Objetivo:** Agregar métodos utilitarios para verificar permisos

**Archivo a modificar:**

- `src/main/java/com/tyse/scrutiny/gateway/security/SecurityUtils.java`

**Métodos a agregar:**

```java
/**
 * Check if current user has specific permission.
 * @param permission format "resource:action" (e.g., "user:create")
 * @return true if user has the permission
 */
public static Mono<Boolean> hasPermission(String permission) {
  return ReactiveSecurityContextHolder.getContext()
    .map(SecurityContext::getAuthentication)
    .flatMapIterable(Authentication::getAuthorities)
    .map(GrantedAuthority::getAuthority)
    .any(auth -> auth.equals(permission))
    .defaultIfEmpty(false);
}

/**
 * Check if current user has specific authority/role.
 * @param authority format "ROLE_XXX"
 */
public static Mono<Boolean> hasAuthority(String authority) {
  return hasPermission(authority);
}

/**
 * Get all permissions of current user (only permissions, not roles).
 * @return set of permissions in format "resource:action"
 */
public static Mono<Set<String>> getCurrentUserPermissions() {
  return ReactiveSecurityContextHolder.getContext()
    .map(SecurityContext::getAuthentication)
    .flatMapIterable(Authentication::getAuthorities)
    .map(GrantedAuthority::getAuthority)
    .filter(auth -> auth.contains(":")) // Solo permisos
    .collect(Collectors.toSet());
}

/**
 * Get all authorities of current user (only roles, not permissions).
 * @return set of authorities in format "ROLE_XXX"
 */
public static Mono<Set<String>> getCurrentUserAuthorities() {
  return ReactiveSecurityContextHolder.getContext()
    .map(SecurityContext::getAuthentication)
    .flatMapIterable(Authentication::getAuthorities)
    .map(GrantedAuthority::getAuthority)
    .filter(auth -> auth.startsWith("ROLE_")) // Solo roles
    .collect(Collectors.toSet());
}

```

**Estimación:** ~30 minutos

---

### 7.5: Actualizar JWT Token Claims ⏳

**Objetivo:** Incluir permisos en el JWT token

**Archivos a revisar/modificar:**

- Buscar clase que genera JWT (probablemente en `security/jwt/`)
- Puede ser `TokenProvider.java` o similar

**Cambios necesarios:**

1. Al generar JWT, incluir claim adicional con permisos
2. Formato del token:

```json
{
  "sub": "admin",
  "auth": ["ROLE_ADMIN", "ROLE_USER"],
  "permissions": ["user:create", "user:update", "invoice:approve"],
  "userId": 1
}
```

3. Actualizar lectura del token para cargar permisos en Authentication

**Nota:** Si JWT usa OAuth2/OIDC estándar, los permisos pueden ir en el claim "authorities" junto con roles.

**Estimación:** ~1 hora

---

### 7.6: Actualizar Tests de Seguridad ⏳

**Objetivo:** Validar integración con tests

**Tests a crear/actualizar:**

#### **EnterprisePermissionEvaluatorTest.java** (Nuevo)

```java
@Test
void hasPermission_withValidPermission_returnsTrue() {
  // Setup: usuario con permiso "user:create"
  // When: hasPermission(auth, null, "user:create")
  // Then: true
}

@Test
void hasPermission_withoutPermission_returnsFalse() {
  // Setup: usuario sin permiso "user:delete"
  // When: hasPermission(auth, null, "user:delete")
  // Then: false
}

```

#### **SecurityUtilsTest.java** (Actualizar)

```java
@Test
void hasPermission_withValidPermission_returnsTrue() { ... }

@Test
void getCurrentUserPermissions_returnsOnlyPermissions() {
    // Verificar que solo retorna permisos (con ":")
    // No retorna roles (ROLE_XXX)
}
```

#### **DomainUserDetailsServiceIT.java** (Actualizar)

```java
@Test
void loadUserByUsername_loadsPermissionsFromEnterpriseSystem() {
  // Setup: usuario con authority y permissions
  // When: cargar usuario
  // Then: authorities contiene roles Y permisos
}

@Test
void loadUserByUsername_includesInheritedPermissions() {
  // Setup: usuario con ROLE_ADMIN que tiene permisos
  // When: cargar usuario
  // Then: incluye permisos del rol
}

@Test
void loadUserByUsername_excludesExpiredPermissions() {
  // Setup: usuario con permiso expirado
  // When: cargar usuario
  // Then: NO incluye permiso expirado
}

```

**Estimación:** ~2 horas

---

### 7.7: Crear Anotación @RequiresPermission (Opcional) ⏳

**Objetivo:** Simplificar uso de permisos con anotación custom

**Archivo a crear:**

- `src/main/java/com/tyse/scrutiny/gateway/security/RequiresPermission.java`

**Implementación:**

```java
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(null, #root.methodName)")
public @interface RequiresPermission {
  /**
   * Permission in format "resource:action"
   */
  String value();
}

```

**Uso:**

```java
// Antes:
@PreAuthorize("hasPermission(null, 'user:create')")
public Mono<User> createUser(User user) { ... }

// Después:
@RequiresPermission("user:create")
public Mono<User> createUser(User user) { ... }
```

**Estimación:** ~20 minutos

---

## 📊 Resumen de Estimaciones

| Subtarea  | Descripción                              | Tiempo           | Prioridad  |
| --------- | ---------------------------------------- | ---------------- | ---------- |
| 7.1       | Actualizar DomainUserDetailsService      | 1 hora           | ⚠️ CRÍTICO |
| 7.2       | Crear PermissionEvaluator                | 45 min           | ⚠️ CRÍTICO |
| 7.3       | Configurar en SecurityConfiguration      | 20 min           | ⚠️ CRÍTICO |
| 7.4       | Actualizar SecurityUtils                 | 30 min           | 🔴 Alto    |
| 7.5       | Actualizar JWT claims                    | 1 hora           | 🔴 Alto    |
| 7.6       | Tests de seguridad                       | 2 horas          | 🔴 Alto    |
| 7.7       | Anotación @RequiresPermission (opcional) | 20 min           | 🟡 Bajo    |
| **TOTAL** | **7 subtareas**                          | **~5.5-6 horas** |            |

---

## 🔄 Orden de Implementación Recomendado

### Fase A: Carga de Permisos (1.5h) ✅ COMPLETADA

1. ✅ **7.1** - Actualizar DomainUserDetailsService
2. ✅ **7.4** - Actualizar SecurityUtils

**Resultado:** Sistema carga permisos en contexto de seguridad

### Fase B: Evaluación de Permisos (1h) ✅ COMPLETADA

3. ✅ **7.2** - Crear EnterprisePermissionEvaluator
4. ✅ **7.3** - Configurar en SecurityConfiguration

**Resultado:** `@PreAuthorize("hasPermission(...)")` funcional

### Fase C: JWT y Validación (3h) ✅ COMPLETADA

5. ✅ **7.5** - Actualizar JWT claims
6. ✅ **7.6** - Tests de seguridad

**Resultado:** Sistema completamente validado

### Fase D: Mejoras Opcionales (20min) ✅ COMPLETADA

7. ✅ **7.7** - Anotación @RequiresPermission (opcional)

**Resultado:** Sintaxis más limpia

---

## 🎯 Objetivos de Salida de Fase 7

Al finalizar esta fase, el sistema debe:

✅ **Cargar authorities exclusivamente desde `scr_user_authority`**
✅ **Cargar permisos desde `scr_user_permission`**
✅ **Incluir permisos heredados de roles (via `scr_authority_permission`)**
✅ **Excluir authorities/permissions expiradas o inactivas**
✅ **Soportar `@PreAuthorize("hasPermission(null, 'user:create')")`**
✅ **Generar JWT con claim de permisos**
✅ **Proveer métodos utilitarios en `SecurityUtils`**
✅ **Pasar todos los tests de seguridad**
✅ **NO usar sistema legacy en absoluto**

---

## 🔧 Decisiones de Diseño

### 1. Formato de Permisos

**Formato:** `resource:action`

**Ejemplos:**

- `user:create`
- `user:read`
- `user:update`
- `user:delete`
- `invoice:approve`
- `report:export`

**Razón:** Estándar común, fácil de parsear y entender

### 2. Conversión a GrantedAuthority

```java
// Authorities (Roles)
Authority{code="ROLE_ADMIN"}
  → SimpleGrantedAuthority("ROLE_ADMIN")

// Permissions
Permission{resource="user", action="create"}
  → SimpleGrantedAuthority("user:create")
```

### 3. Permisos Efectivos

```
Permisos Efectivos = Permisos Directos ∪ Permisos Heredados de Roles
```

**Fuentes:**

1. **Directos:** `scr_user_permission` (grants directos al usuario)
2. **Heredados:** `scr_user_authority` → `scr_authority` → `scr_authority_permission` → `scr_permission`

### 4. Filtrado de Válidos

Solo cargar si:

- `is_active = true`
- `expires_at IS NULL OR expires_at > NOW()`

### 5. Eliminación del Sistema Legacy

**Tablas a NO usar:**

- ❌ `jhi_user_authority` (tabla legacy simple)
- ❌ `jhi_authority` (si existe como tabla separada legacy)

**Usar exclusivamente:**

- ✅ `scr_user_authority`
- ✅ `scr_authority`
- ✅ `scr_permission`
- ✅ `scr_user_permission`
- ✅ `scr_authority_permission`

---

## 📝 Archivos a Crear/Modificar

### Nuevos Archivos (4)

```
src/main/java/com/tyse/scrutiny/gateway/security/
├── EnterprisePermissionEvaluator.java        ✨ NUEVO
└── RequiresPermission.java                   ✨ NUEVO (opcional)

src/test/java/com/tyse/scrutiny/gateway/security/
├── EnterprisePermissionEvaluatorTest.java    ✨ NUEVO
└── SecurityUtilsTest.java                    ✨ NUEVO (si no existe)
```

### Archivos a Modificar (5)

```
src/main/java/com/tyse/scrutiny/gateway/
├── config/SecurityConfiguration.java         📝 MODIFICAR
├── security/DomainUserDetailsService.java    📝 MODIFICAR
├── security/SecurityUtils.java               📝 MODIFICAR
└── security/jwt/TokenProvider.java           📝 MODIFICAR (buscar)

src/test/java/com/tyse/scrutiny/gateway/
└── security/DomainUserDetailsServiceIT.java  📝 MODIFICAR (o crear)
```

**Total:** ~8-9 archivos

---

## 🚨 Puntos Críticos de Atención

### 1. Performance

**Problema:** Cargar permisos en cada autenticación puede ser costoso.

**Soluciones:**

- Incluir permisos en JWT (evita consulta DB en cada request)
- Cache en memoria (Caffeine) para permisos por usuario
- Índices en BD en columnas de filtro (`is_active`, `expires_at`, `user_id`)

### 2. Reactive Programming

**Importante:** Todo debe ser reactivo:

- `Mono<UserDetails>` en DomainUserDetailsService
- `Mono<Boolean>` en PermissionEvaluator
- `Flux<Permission>` al cargar permisos

**NO usar `.block()`** excepto en tests.

### 3. Testing con Permisos

**Mock de usuario con permisos:**

```java
@WithMockUser(
    username = "admin",
    authorities = {"ROLE_ADMIN", "user:create", "user:update", "invoice:approve"}
)
void testWithPermissions() { ... }
```

### 4. Migración de Datos Existentes

Si hay usuarios con authorities en sistema legacy:

**Opción A:** Script de migración SQL

```sql
-- Migrar de jhi_user_authority a scr_user_authority
INSERT INTO scr_user_authority (user_id, authority_id, is_active, ...)
SELECT u.id, a.id, true, ...
FROM jhi_user_authority jua
JOIN scr_user u ON ...
JOIN scr_authority a ON ...
```

**Opción B:** Script Java/Spring

- Leer de legacy
- Crear en enterprise
- Validar

---

## 🎓 Referencias Técnicas

### Spring Security Reactive

- [Method Security](https://docs.spring.io/spring-security/reference/reactive/authorization/method.html)
- [ReactivePermissionEvaluator](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/access/expression/method/MethodSecurityExpressionHandler.html)

### Reactive Streams

- [Project Reactor](https://projectreactor.io/docs/core/release/reference/)
- [Mono/Flux](https://projectreactor.io/docs/core/release/api/)

---

## ✅ Checklist de Completación

### Implementación

- [ ] 7.1: DomainUserDetailsService actualizado y funcionando
- [ ] 7.2: EnterprisePermissionEvaluator creado
- [ ] 7.3: PermissionEvaluator configurado en SecurityConfiguration
- [ ] 7.4: SecurityUtils con métodos de permisos
- [ ] 7.5: JWT incluye claim de permisos
- [ ] 7.6: Tests de seguridad pasando
- [ ] 7.7: Anotación @RequiresPermission (opcional)

### Validación

- [ ] Compilación limpia (BUILD SUCCESS)
- [ ] Login carga permisos correctamente
- [ ] `@PreAuthorize("hasPermission(...)")` funciona
- [ ] JWT contiene permisos
- [ ] Endpoints protegidos funcionan
- [ ] Tests de integración pasando
- [ ] Sistema legacy NO se usa

### Documentación

- [ ] README actualizado con ejemplos
- [ ] Javadoc en clases nuevas
- [ ] Comentarios en código crítico

---

## 🚀 Próximos Pasos Después de Fase 7

1. **Fase 8:** Tests de integración completos (ajustar tests existentes)
2. **Fase 9:** Endpoints de auditoría (`/api/audit/...`)
3. **Fase 10:** Frontend React para gestión de permisos
4. **Fase 11:** Dashboard de administración
5. **Fase 12:** Documentación de usuario final

---

**Autor:** Claude Code
**Fecha de creación:** 2025-10-30 02:52
**Estado:** Listo para iniciar implementación
**Próximo paso:** Comenzar con subtarea 7.1 (DomainUserDetailsService)
**Decisión clave:** ✅ Reemplazo total del sistema legacy, sin compatibilidad
