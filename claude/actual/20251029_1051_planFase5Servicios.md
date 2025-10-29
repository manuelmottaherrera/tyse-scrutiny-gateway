# Plan Completo: FASE 5 - Servicios de Negocio

**Fecha de creación:** 2025-10-29 10:51
**Proyecto:** Tyse Scrutiny Gateway - Sistema de Autorización Enterprise
**Branch:** `feature/enterprise-authorization-system`
**Fase:** FASE 5 - Servicios de Negocio

---

## 📋 Estado Actual

### Completado en Fase 3

- ✅ 5 repositorios R2DBC implementados
- ✅ 4 row mappers creados
- ✅ 4 SQL helpers creados
- ✅ UserRepository migrado a scr_user_authority
- ✅ Lógica de expiración en queries

### Errores de Compilación Actuales

**8 errores identificados:**

- UserService.java: 5 errores
- AuthorityResource.java: 3 errores

---

## 🎯 Objetivo de Fase 5

Implementar la capa de servicios completa para el sistema de autorización enterprise:

1. Corregir errores de compilación (9 cambios)
2. Crear servicios especializados (4 nuevos)
3. Implementar scheduled jobs (2 jobs)
4. Crear DTOs de autorización (5 nuevos)
5. Tests críticos (2 clases)

---

## 🔢 Decisiones Arquitectónicas Confirmadas

### 1. DTOs Separados ✅

**Decisión:** Crear DTOs específicos para la capa de API
**Razón:** Mejor separación de capas, más control sobre API pública

### 2. Scheduled Jobs ✅

**Decisión:** Implementar jobs de cleanup y notificaciones en esta fase
**Razón:** Sistema completo end-to-end

### 3. Auditoría Completa ✅

**Decisión:** Capturar IP + User-Agent en AuthorityAudit
**Implementación:** Inyectar ServerWebExchange en métodos de servicio

### 4. Tests Críticos ✅

**Decisión:** Solo tests críticos en esta fase
**Alcance:** UserServiceIT actualizado + UserAuthorityServiceIT nuevo

---

## 📝 Fase 5.1: Correcciones Críticas (9 cambios)

### UserService.java - 5 cambios

**Ubicación:** `src/main/java/com/tyse/scrutiny/gateway/service/UserService.java`

#### Cambio 1: registerUser() - Línea 137

```java
// ACTUAL (ROTO)
return authorityRepository
    .findById(AuthoritiesConstants.USER)  // ❌ String
    .map(authorities::add)

// SOLUCIÓN
return authorityRepository
    .findByCode(AuthoritiesConstants.USER)  // ✅ findByCode()
    .map(authorities::add)
```

#### Cambio 2: createUser() - Línea 162

```java
// ACTUAL (ROTO)
return Flux.fromIterable(userDTO.getAuthorities() != null ? userDTO.getAuthorities() : new HashSet<>())
    .flatMap(authorityRepository::findById)  // ❌ String
    .doOnNext(authority -> user.getAuthorities().add(authority))

// SOLUCIÓN
return Flux.fromIterable(userDTO.getAuthorities() != null ? userDTO.getAuthorities() : new HashSet<>())
    .flatMap(authorityRepository::findByCode)  // ✅ findByCode()
    .doOnNext(authority -> user.getAuthorities().add(authority))
```

#### Cambio 3: updateUser() - Línea 203

```java
// ACTUAL (ROTO)
return userRepository
    .deleteUserAuthorities(user.getId())
    .thenMany(Flux.fromIterable(userDTO.getAuthorities()))
    .flatMap(authorityRepository::findById)  // ❌ String
    .map(managedAuthorities::add)

// SOLUCIÓN
return userRepository
    .deleteUserAuthorities(user.getId())
    .thenMany(Flux.fromIterable(userDTO.getAuthorities()))
    .flatMap(authorityRepository::findByCode)  // ✅ findByCode()
    .map(managedAuthorities::add)
```

#### Cambio 4 y 5: saveUser() - Líneas 253-267

```java
// ACTUAL (ROTO)
return userRepository
    .save(user)
    .flatMap(savedUser ->
        Flux.fromIterable(user.getAuthorities())
            .flatMap(authority -> userRepository.saveUserAuthority(
                savedUser.getId(),
                authority.getName()  // ❌ getName() + falta 3er parámetro
            ))
            .then(Mono.just(savedUser))
    );

// SOLUCIÓN
return SecurityUtils.getCurrentUserLogin()
    .switchIfEmpty(Mono.just(Constants.SYSTEM))
    .flatMap(assignedBy ->
        userRepository.save(user)
            .flatMap(savedUser ->
                Flux.fromIterable(user.getAuthorities())
                    .flatMap(authority -> userRepository.saveUserAuthority(
                        savedUser.getId(),
                        authority.getCode(),  // ✅ getCode()
                        assignedBy           // ✅ Tercer parámetro
                    ))
                    .then(Mono.just(savedUser))
            )
    );
```

### AuthorityResource.java - 3 cambios

**Ubicación:** `src/main/java/com/tyse/scrutiny/gateway/web/rest/AuthorityResource.java`

#### Cambio 1: createAuthority() - Línea 83

```java
// ACTUAL (ROTO)
return authorityRepository
    .existsById(authority.getName())  // ❌ String, no Long
    .flatMap(exists -> {
        if (exists) {
            return Mono.error(new BadRequestAlertException("authority already exists", ...));
        }
        return authorityRepository.save(authority)

// SOLUCIÓN
return authorityRepository
    .existsByCode(authority.getCode())  // ✅ existsByCode()
    .flatMap(exists -> {
        if (exists) {
            return Mono.error(new BadRequestAlertException("authority already exists", ...));
        }
        return authorityRepository.save(authority)
```

#### Cambio 2: getAuthority() - Línea 175

```java
// ACTUAL (ROTO)
@GetMapping("/{id}")
public Mono<ResponseEntity<Authority>> getAuthority(@PathVariable("id") String id) { // ❌ String
  Mono<Authority> authority = authorityRepository.findById(id); // ❌ String
  return ResponseUtil.wrapOrNotFound(authority);
}

// SOLUCIÓN
@GetMapping("/{id}")
public Mono<ResponseEntity<Authority>> getAuthority(@PathVariable("id") Long id) { // ✅ Long
  Mono<Authority> authority = authorityRepository.findById(id); // ✅ Long
  return ResponseUtil.wrapOrNotFound(authority);
}

```

#### Cambio 3: deleteAuthority() - Línea 204

```java
// ACTUAL (ROTO)
@DeleteMapping("/{id}")
public Mono<ResponseEntity<Void>> deleteAuthority(@PathVariable("id") String id) {  // ❌ String
    return authorityRepository
        .deleteById(id)  // ❌ String
        .then(Mono.just(ResponseEntity.noContent()...))
}

// SOLUCIÓN
@DeleteMapping("/{id}")
public Mono<ResponseEntity<Void>> deleteAuthority(@PathVariable("id") Long id) {  // ✅ Long
    return authorityRepository
        .deleteById(id)  // ✅ Long
        .then(Mono.just(ResponseEntity.noContent()...))
}
```

### AdminUserDTO.java - 1 cambio

**Ubicación:** `src/main/java/com/tyse/scrutiny/gateway/service/dto/AdminUserDTO.java`

#### Cambio 1: Constructor - Línea 71

```java
// ACTUAL (ROTO)
this.authorities = user.getAuthorities()
    .stream()
    .map(Authority::getName)  // ❌ getName() ahora es nombre legible, no código
    .collect(Collectors.toSet());

// SOLUCIÓN
this.authorities = user.getAuthorities()
    .stream()
    .map(Authority::getCode)  // ✅ getCode() retorna ROLE_USER, ROLE_ADMIN
    .collect(Collectors.toSet());
```

---

## 📦 Fase 5.2: DTOs de Autorización (5 nuevos)

### Ubicación

`src/main/java/com/tyse/scrutiny/gateway/service/dto/authorization/`

### 1. AuthorityDTO.java

```java
public class AuthorityDTO implements Serializable {

  private Long id;
  private String name; // Nombre legible
  private String code; // Código único (ROLE_*)
  private String description;
  private AuthorityCategory category;
  private Boolean isSystem;
  private Boolean isActive;
  private Integer hierarchyLevel;

  // Audit fields
  private String createdBy;
  private Instant createdDate;
  private String lastModifiedBy;
  private Instant lastModifiedDate;

  public AuthorityDTO() {}

  public AuthorityDTO(Authority authority) {
    // Mapear campos
  }
  // Getters, setters, equals, hashCode, toString
}

```

### 2. PermissionDTO.java

```java
public class PermissionDTO implements Serializable {

  private Long id;
  private String name; // resource.action
  private String resource;
  private String action;
  private String description;
  private Boolean isActive;

  // Audit fields
  private String createdBy;
  private Instant createdDate;

  public PermissionDTO() {}

  public PermissionDTO(Permission permission) {
    // Mapear campos
  }
}

```

### 3. UserAuthorityDTO.java

```java
public class UserAuthorityDTO implements Serializable {

  private Long id;
  private Long userId;
  private Long authorityId;
  private String authorityCode; // Para display
  private String authorityName; // Para display
  private Boolean isActive;
  private Instant expiresAt;
  private String assignedBy;
  private Instant assignedDate;
  private String revokedBy;
  private Instant revokedDate;
  private String revokedReason;

  // Computed fields
  private Boolean isExpired;
  private Boolean isValid;

  public UserAuthorityDTO() {}

  public UserAuthorityDTO(UserAuthority ua, Authority authority) {
    // Mapear campos + authority info
    this.isExpired = ua.isExpired();
    this.isValid = ua.isValid();
  }
}

```

### 4. UserPermissionDTO.java

```java
public class UserPermissionDTO implements Serializable {

  private Long id;
  private Long userId;
  private Long permissionId;
  private String permissionName; // Para display
  private Boolean isActive;
  private Instant expiresAt;
  private String grantedBy;
  private Instant grantedDate;
  private String reason;
  private String revokedBy;
  private Instant revokedDate;
  private String revokedReason;

  // Computed fields
  private Boolean isExpired;
  private Boolean isValid;

  public UserPermissionDTO() {}

  public UserPermissionDTO(UserPermission up, Permission permission) {
    // Mapear campos + permission info
  }
}

```

### 5. AuthorityAuditDTO.java (Read-only)

```java
public class AuthorityAuditDTO implements Serializable {

  private Long id;
  private Long authorityId;
  private String authorityCode; // Para display
  private AuditAction action;
  private String oldValues; // JSON
  private String newValues; // JSON
  private String changedBy;
  private Instant changedDate;
  private String ipAddress;
  private String userAgent;

  public AuthorityAuditDTO() {}

  public AuthorityAuditDTO(AuthorityAudit audit) {
    // Mapear campos
  }
}

```

---

## 🛠️ Fase 5.3: Servicios Enterprise (4 nuevos)

### Ubicación

`src/main/java/com/tyse/scrutiny/gateway/service/authorization/`

### 1. AuthorityService.java

```java
@Service
public class AuthorityService {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorityService.class);

  private final AuthorityRepository authorityRepository;
  private final AuthorityAuditRepository auditRepository;

  public AuthorityService(AuthorityRepository authorityRepository, AuthorityAuditRepository auditRepository) {
    this.authorityRepository = authorityRepository;
    this.auditRepository = auditRepository;
  }

  // CRUD con auditoría
  public Mono<Authority> createAuthority(Authority authority, ServerWebExchange exchange) {
    // Validar no existe código
    // Validar categoria
    // Save
    // Crear audit log con IP/User-Agent
  }

  public Mono<Authority> updateAuthority(Long id, Authority authority, ServerWebExchange exchange) {
    // Cargar old values
    // Validar no es sistema si intenta modificar
    // Update
    // Crear audit log con diff
  }

  public Mono<Void> deactivateAuthority(Long id, ServerWebExchange exchange) {
    // Soft delete: isActive = false
    // Crear audit log
  }

  public Flux<Authority> findActiveAuthorities() {
    return authorityRepository.findByIsActiveTrue();
  }

  public Flux<Authority> findByCategory(AuthorityCategory category) {
    return authorityRepository.findByCategoryAndIsActiveTrue(category);
  }

  public Mono<Authority> findByCode(String code) {
    return authorityRepository.findByCode(code);
  }

  // Método helper para auditoría
  private Mono<Void> createAuditLog(Long authorityId, AuditAction action, String oldValues, String newValues, ServerWebExchange exchange) {
    // Extraer IP y User-Agent
    // Obtener current user
    // Crear y guardar AuthorityAudit
  }
}

```

### 2. PermissionService.java

```java
@Service
public class PermissionService {

  private static final Logger LOG = LoggerFactory.getLogger(PermissionService.class);

  private final PermissionRepository permissionRepository;

  public PermissionService(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public Mono<Permission> createPermission(Permission permission) {
    // Validar patrón resource.action
    // Validar name = resource + "." + action
    // Verificar no existe
    // Save
  }

  public Mono<Permission> updatePermission(Long id, Permission permission) {
    // Update
  }

  public Flux<Permission> findByResource(String resource) {
    return permissionRepository.findByResourceAndIsActiveTrue(resource);
  }

  public Mono<Permission> findByResourceAndAction(String resource, String action) {
    return permissionRepository.findByResourceAndAction(resource, action);
  }

  public Flux<Permission> findAllActive() {
    return permissionRepository.findByIsActiveTrue();
  }

  // Validación de patrón
  private boolean isValidPermissionPattern(String resource, String action) {
    // resource: lowercase, alphanumeric + underscore
    // action: create, read, update, delete, execute, etc.
  }
}

```

### 3. UserAuthorityService.java

```java
@Service
public class UserAuthorityService {

  private static final Logger LOG = LoggerFactory.getLogger(UserAuthorityService.class);

  private final UserAuthorityRepository userAuthorityRepository;
  private final AuthorityRepository authorityRepository;

  public UserAuthorityService(UserAuthorityRepository userAuthorityRepository, AuthorityRepository authorityRepository) {
    this.userAuthorityRepository = userAuthorityRepository;
    this.authorityRepository = authorityRepository;
  }

  public Mono<UserAuthority> assignAuthority(Long userId, Long authorityId, Instant expiresAt) {
    return SecurityUtils.getCurrentUserLogin()
      .switchIfEmpty(Mono.just(Constants.SYSTEM))
      .flatMap(assignedBy -> {
        UserAuthority assignment = new UserAuthority();
        assignment.setUserId(userId);
        assignment.setAuthorityId(authorityId);
        assignment.setIsActive(true);
        assignment.setExpiresAt(expiresAt);
        assignment.setAssignedBy(assignedBy);
        assignment.setAssignedDate(Instant.now());

        return userAuthorityRepository
          .save(assignment)
          .doOnNext(saved -> LOG.debug("Assigned authority {} to user {} by {}", authorityId, userId, assignedBy));
      });
  }

  public Mono<UserAuthority> revokeAuthority(Long userId, Long authorityId, String reason) {
    return SecurityUtils.getCurrentUserLogin()
      .switchIfEmpty(Mono.just(Constants.SYSTEM))
      .flatMap(revokedBy ->
        userAuthorityRepository
          .findByUserIdAndAuthorityId(userId, authorityId)
          .flatMap(assignment -> {
            assignment.setIsActive(false);
            assignment.setRevokedBy(revokedBy);
            assignment.setRevokedDate(Instant.now());
            assignment.setRevokedReason(reason);

            return userAuthorityRepository
              .save(assignment)
              .doOnNext(saved -> LOG.debug("Revoked authority {} from user {} by {}: {}", authorityId, userId, revokedBy, reason));
          })
      );
  }

  public Flux<UserAuthority> getValidAuthorities(Long userId) {
    return userAuthorityRepository.findValidByUserId(userId);
  }

  public Mono<Boolean> userHasAuthority(Long userId, String authorityCode) {
    return authorityRepository
      .findByCode(authorityCode)
      .flatMap(authority -> userAuthorityRepository.userHasAuthority(userId, authority.getId()))
      .defaultIfEmpty(false);
  }

  public Flux<UserAuthority> findExpiredAssignments() {
    return userAuthorityRepository.findExpiredAssignments();
  }

  public Flux<UserAuthority> findExpiringWithinDays(int days) {
    return userAuthorityRepository.findExpiringWithinDays(days);
  }
}

```

### 4. UserPermissionService.java

```java
@Service
public class UserPermissionService {

  private static final Logger LOG = LoggerFactory.getLogger(UserPermissionService.class);

  private final UserPermissionRepository userPermissionRepository;
  private final PermissionRepository permissionRepository;

  public UserPermissionService(UserPermissionRepository userPermissionRepository, PermissionRepository permissionRepository) {
    this.userPermissionRepository = userPermissionRepository;
    this.permissionRepository = permissionRepository;
  }

  public Mono<UserPermission> grantPermission(Long userId, Long permissionId, Instant expiresAt, String reason) {
    return SecurityUtils.getCurrentUserLogin()
      .switchIfEmpty(Mono.just(Constants.SYSTEM))
      .flatMap(grantedBy -> {
        UserPermission grant = new UserPermission();
        grant.setUserId(userId);
        grant.setPermissionId(permissionId);
        grant.setIsActive(true);
        grant.setExpiresAt(expiresAt);
        grant.setGrantedBy(grantedBy);
        grant.setGrantedDate(Instant.now());
        grant.setReason(reason);

        return userPermissionRepository
          .save(grant)
          .doOnNext(saved -> LOG.debug("Granted permission {} to user {} by {}: {}", permissionId, userId, grantedBy, reason));
      });
  }

  public Mono<UserPermission> revokePermission(Long userId, Long permissionId, String reason) {
    return SecurityUtils.getCurrentUserLogin()
      .switchIfEmpty(Mono.just(Constants.SYSTEM))
      .flatMap(revokedBy ->
        userPermissionRepository
          .findByUserIdAndPermissionId(userId, permissionId)
          .flatMap(grant -> {
            grant.setIsActive(false);
            grant.setRevokedBy(revokedBy);
            grant.setRevokedDate(Instant.now());
            grant.setRevokedReason(reason);

            return userPermissionRepository.save(grant);
          })
      );
  }

  public Flux<UserPermission> getValidPermissions(Long userId) {
    return userPermissionRepository.findValidByUserId(userId);
  }

  public Mono<Boolean> userHasPermission(Long userId, String permissionName) {
    return userPermissionRepository.userHasPermissionByName(userId, permissionName);
  }

  // Combina permisos de roles + permisos directos
  public Flux<Permission> getEffectivePermissions(Long userId) {
    Flux<Permission> rolePermissions = permissionRepository.findByUserId(userId);
    Flux<Permission> directPermissions = userPermissionRepository
      .findValidByUserId(userId)
      .flatMap(up -> permissionRepository.findById(up.getPermissionId()));

    return Flux.merge(rolePermissions, directPermissions).distinct(Permission::getId);
  }
}

```

---

## ⚙️ Fase 5.4: Excepciones Personalizadas (4 nuevas)

### Ubicación

`src/main/java/com/tyse/scrutiny/gateway/service/authorization/exceptions/`

### 1. AuthorityAlreadyExistsException.java

```java
public class AuthorityAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AuthorityAlreadyExistsException(String code) {
    super("Authority with code '" + code + "' already exists");
  }
}

```

### 2. AuthorityNotFoundException.java

```java
public class AuthorityNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AuthorityNotFoundException(Long id) {
    super("Authority not found with id: " + id);
  }

  public AuthorityNotFoundException(String code) {
    super("Authority not found with code: " + code);
  }
}

```

### 3. InvalidAuthorityAssignmentException.java

```java
public class InvalidAuthorityAssignmentException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidAuthorityAssignmentException(String message) {
    super(message);
  }
}

```

### 4. PermissionDeniedException.java

```java
public class PermissionDeniedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PermissionDeniedException(String message) {
    super(message);
  }
}

```

---

## ⏰ Fase 5.5: Scheduled Jobs (2 nuevos)

### Ubicación

`src/main/java/com/tyse/scrutiny/gateway/service/scheduled/`

### 1. ExpiredAuthoritiesCleanupJob.java

```java
@Service
public class ExpiredAuthoritiesCleanupJob {

  private static final Logger LOG = LoggerFactory.getLogger(ExpiredAuthoritiesCleanupJob.class);

  private final UserAuthorityRepository userAuthorityRepository;
  private final UserPermissionRepository userPermissionRepository;

  public ExpiredAuthoritiesCleanupJob(UserAuthorityRepository userAuthorityRepository, UserPermissionRepository userPermissionRepository) {
    this.userAuthorityRepository = userAuthorityRepository;
    this.userPermissionRepository = userPermissionRepository;
  }

  /**
   * Marca como inactivas las asignaciones expiradas.
   * Ejecuta diariamente a las 2 AM.
   */
  @Scheduled(cron = "0 0 2 * * *")
  public void cleanupExpiredAuthorities() {
    LOG.info("Starting cleanup of expired authority assignments");

    userAuthorityRepository
      .findExpiredAssignments()
      .flatMap(assignment -> {
        assignment.setIsActive(false);
        return userAuthorityRepository.save(assignment);
      })
      .count()
      .doOnNext(count -> LOG.info("Cleaned up {} expired authority assignments", count))
      .subscribe();
  }

  @Scheduled(cron = "0 0 2 * * *")
  public void cleanupExpiredPermissions() {
    LOG.info("Starting cleanup of expired permission grants");

    userPermissionRepository
      .findExpiredGrants()
      .flatMap(grant -> {
        grant.setIsActive(false);
        return userPermissionRepository.save(grant);
      })
      .count()
      .doOnNext(count -> LOG.info("Cleaned up {} expired permission grants", count))
      .subscribe();
  }
}

```

### 2. ExpirationWarningJob.java

```java
@Service
public class ExpirationWarningJob {

  private static final Logger LOG = LoggerFactory.getLogger(ExpirationWarningJob.class);
  private static final int WARNING_DAYS = 7;

  private final UserAuthorityRepository userAuthorityRepository;
  private final UserPermissionRepository userPermissionRepository;

  public ExpirationWarningJob(UserAuthorityRepository userAuthorityRepository, UserPermissionRepository userPermissionRepository) {
    this.userAuthorityRepository = userAuthorityRepository;
    this.userPermissionRepository = userPermissionRepository;
  }

  /**
   * Detecta asignaciones que expiran en los próximos 7 días.
   * Ejecuta diariamente a las 9 AM.
   */
  @Scheduled(cron = "0 0 9 * * *")
  public void checkExpiringAuthorities() {
    LOG.info("Checking for authority assignments expiring within {} days", WARNING_DAYS);

    userAuthorityRepository
      .findExpiringWithinDays(WARNING_DAYS)
      .doOnNext(assignment ->
        LOG.warn(
          "Authority assignment {} for user {} expires at {}",
          assignment.getAuthorityId(),
          assignment.getUserId(),
          assignment.getExpiresAt()
        )
      )
      .count()
      .doOnNext(count -> LOG.info("Found {} expiring authority assignments", count))
      .subscribe();
    // TODO: Enviar notificaciones por email (integrar con MailService)
  }

  @Scheduled(cron = "0 0 9 * * *")
  public void checkExpiringPermissions() {
    LOG.info("Checking for permission grants expiring within {} days", WARNING_DAYS);

    userPermissionRepository
      .findExpiringWithinDays(WARNING_DAYS)
      .doOnNext(grant ->
        LOG.warn("Permission grant {} for user {} expires at {}", grant.getPermissionId(), grant.getUserId(), grant.getExpiresAt())
      )
      .count()
      .doOnNext(count -> LOG.info("Found {} expiring permission grants", count))
      .subscribe();
    // TODO: Enviar notificaciones por email
  }
}

```

**Nota:** Asegurarse de que `@EnableScheduling` esté en la clase principal `TyseScrutinyGatewayApp.java`

---

## 🧪 Fase 5.6: Tests Críticos (2 clases)

### 1. Actualizar UserServiceIT.java

**Ubicación:** `src/test/java/com/tyse/scrutiny/gateway/service/UserServiceIT.java`

**Cambios necesarios:**

- Actualizar setup para usar `authorityRepository.findByCode()` en lugar de `findById()`
- Verificar que authorities se asignan correctamente con nuevo schema
- Tests existentes deben seguir pasando

### 2. Crear UserAuthorityServiceIT.java

**Ubicación:** `src/test/java/com/tyse/scrutiny/gateway/service/authorization/UserAuthorityServiceIT.java`

```java
@IntegrationTest
class UserAuthorityServiceIT {

  @Autowired
  private UserAuthorityService userAuthorityService;

  @Autowired
  private UserAuthorityRepository userAuthorityRepository;

  @Autowired
  private AuthorityRepository authorityRepository;

  @Autowired
  private UserRepository userRepository;

  private User user;
  private Authority authority;

  @BeforeEach
  void init() {
    // Crear user de prueba
    // Crear authority de prueba
  }

  @AfterEach
  void cleanup() {
    userAuthorityRepository.deleteByUserId(user.getId()).block();
    userRepository.deleteById(user.getId()).block();
    authorityRepository.deleteById(authority.getId()).block();
  }

  @Test
  void testAssignAuthority() {
    // Asignar rol a usuario
    // Verificar assignment creado
    // Verificar isActive = true
  }

  @Test
  void testAssignWithExpiration() {
    // Asignar con expiresAt futuro
    // Verificar isValid() = true
    // Verificar isExpired() = false
  }

  @Test
  void testExpiredAssignment() {
    // Asignar con expiresAt pasado
    // Verificar isValid() = false
    // Verificar isExpired() = true
    // Verificar NO aparece en findValidByUserId()
  }

  @Test
  void testRevokeAuthority() {
    // Asignar rol
    // Revocar con razón
    // Verificar isActive = false
    // Verificar revokedBy, revokedDate, revokedReason
  }

  @Test
  void testUserHasAuthority() {
    // Asignar rol
    // Verificar userHasAuthority() = true
    // Revocar
    // Verificar userHasAuthority() = false
  }

  @Test
  void testFindExpiringWithinDays() {
    // Crear assignment expirando en 3 días
    // Crear assignment expirando en 10 días
    // findExpiringWithinDays(7)
    // Verificar solo el primero aparece
  }
}

```

---

## 📂 Estructura de Archivos Final

```
src/main/java/com/tyse/scrutiny/gateway/
├── service/
│   ├── UserService.java ✨ (9 líneas modificadas)
│   ├── authorization/
│   │   ├── AuthorityService.java (nuevo)
│   │   ├── PermissionService.java (nuevo)
│   │   ├── UserAuthorityService.java (nuevo)
│   │   ├── UserPermissionService.java (nuevo)
│   │   └── exceptions/
│   │       ├── AuthorityAlreadyExistsException.java
│   │       ├── AuthorityNotFoundException.java
│   │       ├── InvalidAuthorityAssignmentException.java
│   │       └── PermissionDeniedException.java
│   ├── scheduled/
│   │   ├── ExpiredAuthoritiesCleanupJob.java
│   │   └── ExpirationWarningJob.java
│   └── dto/
│       ├── AdminUserDTO.java ✨ (1 línea modificada)
│       └── authorization/
│           ├── AuthorityDTO.java (nuevo)
│           ├── PermissionDTO.java (nuevo)
│           ├── UserAuthorityDTO.java (nuevo)
│           ├── UserPermissionDTO.java (nuevo)
│           └── AuthorityAuditDTO.java (nuevo)
└── web/rest/
    └── AuthorityResource.java ✨ (3 líneas modificadas)

src/test/java/com/tyse/scrutiny/gateway/service/
├── UserServiceIT.java ✨ (actualizar)
└── authorization/
    └── UserAuthorityServiceIT.java (nuevo)
```

---

## 🔢 Orden de Implementación Recomendado

### Paso 1: Correcciones (Compilación exitosa)

1. UserService.java - 5 cambios
2. AuthorityResource.java - 3 cambios
3. AdminUserDTO.java - 1 cambio
4. **Verificar compilación:** `./mvnw clean compile -DskipTests`

### Paso 2: Excepciones

5. Crear 4 excepciones personalizadas

### Paso 3: DTOs

6. Crear 5 DTOs de autorización

### Paso 4: Servicios Core

7. AuthorityService
8. PermissionService
9. UserAuthorityService
10. UserPermissionService

### Paso 5: Scheduled Jobs

11. ExpiredAuthoritiesCleanupJob
12. ExpirationWarningJob
13. Verificar `@EnableScheduling` en app principal

### Paso 6: Tests Críticos

14. Actualizar UserServiceIT
15. Crear UserAuthorityServiceIT
16. **Ejecutar tests:** `./mvnw test`

### Paso 7: Documentación

17. Crear reporte de completado
18. Crear README de hito
19. Mover a `claude/hitos/05_sistema-autorizacion-fase5/`

---

## 📊 Métricas Estimadas

| Componente   | Archivos | Líneas     | Tiempo  |
| ------------ | -------- | ---------- | ------- |
| Correcciones | 3        | ~50        | 1h      |
| Excepciones  | 4        | ~100       | 30min   |
| DTOs         | 5        | ~600       | 2h      |
| Servicios    | 4        | ~1,500     | 4h      |
| Jobs         | 2        | ~200       | 1.5h    |
| Tests        | 2        | ~400       | 2h      |
| Docs         | 3        | -          | 1h      |
| **TOTAL**    | **23**   | **~2,850** | **12h** |

---

## ✅ Criterios de Éxito

Fase 5 completada cuando:

- ✅ 0 errores de compilación
- ✅ 4 servicios enterprise implementados
- ✅ 5 DTOs creados
- ✅ 2 scheduled jobs funcionando
- ✅ Auditoría con IP/User-Agent implementada
- ✅ Tests críticos pasando
- ✅ Documentación completa

---

## 🔗 Referencias Útiles

### Código Existente a Consultar

- **UserService.java** - Patrón de servicios, inyección, logging
- **MailService.java** - Patrón @Service, async operations
- **UserServiceIT.java** - Patrón de tests con @IntegrationTest
- **SecurityUtils.java** - getCurrentUserLogin()
- **Constants.java** - SYSTEM constant

### Patrones Identificados

- Constructor injection (no @Autowired)
- Logger: `private static final Logger LOG = LoggerFactory.getLogger(XxxService.class);`
- Reactive chains con `flatMap()`, `map()`, `then()`
- Error handling con `Mono.error()`
- Logging con `doOnNext()`

---

**Última actualización:** 2025-10-29 10:51
**Estado:** LISTO PARA IMPLEMENTACIÓN
**Branch:** feature/enterprise-authorization-system
