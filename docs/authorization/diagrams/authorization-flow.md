# Diagrama de Flujo de Autorización

**Versión:** 1.0
**Fecha:** 2025-11-07

---

## 1. Flujo Completo de Autenticación y Autorización

```mermaid
sequenceDiagram
    actor Usuario
    participant Frontend
    participant Gateway
    participant JWTFilter
    participant Spring Security
    participant AuthService
    participant UserAuthRepo
    participant Database

    %% FASE 1: LOGIN
    Usuario->>Frontend: 1. Ingresar credenciales (login)
    Frontend->>Gateway: 2. POST /api/authenticate<br/>{username, password}
    Gateway->>AuthService: 3. Validar credenciales
    AuthService->>Database: 4. SELECT * FROM scr_user<br/>WHERE login = ?
    Database-->>AuthService: 5. Usuario encontrado
    AuthService->>UserAuthRepo: 6. findValidAuthorities(userId)
    UserAuthRepo->>Database: 7. SELECT authorities<br/>WHERE is_active=true<br/>AND expires_at > NOW()
    Database-->>UserAuthRepo: 8. ["ROLE_USER", "ROLE_MANAGER"]
    UserAuthRepo-->>AuthService: 9. Lista de roles válidos
    AuthService->>AuthService: 10. Generar JWT con claim 'auth'
    Note over AuthService: JWT payload:<br/>{sub: "john.doe",<br/>auth: ["ROLE_USER", "ROLE_MANAGER"],<br/>exp: 1699372800}
    AuthService-->>Gateway: 11. JWT token
    Gateway-->>Frontend: 12. 200 OK {id_token: "eyJ..."}
    Frontend->>Frontend: 13. Almacenar JWT<br/>(localStorage/sessionStorage)
    Frontend-->>Usuario: 14. Login exitoso

    %% FASE 2: ACCESO A RECURSO PROTEGIDO
    Usuario->>Frontend: 15. Click en "Crear Usuario"
    Frontend->>Gateway: 16. POST /api/users<br/>Authorization: Bearer eyJ...
    Gateway->>JWTFilter: 17. Interceptar request
    JWTFilter->>JWTFilter: 18. Validar JWT<br/>(firma, expiración)
    JWTFilter->>JWTFilter: 19. Extraer claim 'auth'<br/>→ ["ROLE_USER", "ROLE_MANAGER"]
    JWTFilter->>Spring Security: 20. Setear SecurityContext<br/>con authorities
    Spring Security->>Spring Security: 21. Evaluar @PreAuthorize<br/>("hasAuthority('ROLE_ADMIN')")
    Note over Spring Security: Usuario tiene ROLE_USER<br/>y ROLE_MANAGER,<br/>pero NO ROLE_ADMIN
    Spring Security-->>Gateway: 22. AccessDeniedException
    Gateway-->>Frontend: 23. 403 Forbidden<br/>{error: "Access Denied"}
    Frontend-->>Usuario: 24. Error: No tiene permisos
```

---

## 2. Flujo de Asignación de Rol

```mermaid
sequenceDiagram
    actor Admin
    participant Frontend
    participant Gateway
    participant AuthorityResource
    participant UserAuthService
    participant AuthorityAuditService
    participant Database

    Admin->>Frontend: 1. Asignar ROLE_MANAGER<br/>a usuario "john.doe"<br/>con expiración: 30 días
    Frontend->>Gateway: 2. POST /api/user-authorities<br/>{userId: 123,<br/>authorityId: 3,<br/>expiresAt: "2025-12-07"}
    Gateway->>AuthorityResource: 3. @PreAuthorize<br/>("hasAuthority('ROLE_ADMIN')")
    Note over Gateway: Admin tiene ROLE_ADMIN,<br/>acceso permitido
    AuthorityResource->>UserAuthService: 4. assignAuthority(request, exchange)
    UserAuthService->>Database: 5. Verificar que no existe<br/>asignación duplicada
    Database-->>UserAuthService: 6. No existe (OK)
    UserAuthService->>UserAuthService: 7. Capturar metadata:<br/>- IP: 192.168.1.100<br/>- User-Agent: Chrome...
    UserAuthService->>Database: 8. INSERT INTO scr_user_authority<br/>(user_id, authority_id,<br/>assigned_by, assigned_date,<br/>expires_at, is_active)<br/>VALUES (123, 3, 'admin',<br/>NOW(), '2025-12-07', true)
    Database-->>UserAuthService: 9. ID=456 (auto-increment)
    UserAuthService-->>AuthorityResource: 10. UserAuthorityDTO creado
    AuthorityResource-->>Gateway: 11. 201 Created<br/>{id: 456, ...}
    Gateway-->>Frontend: 12. Confirmación
    Frontend-->>Admin: 13. "Rol asignado exitosamente"

    Note over Admin,Database: El usuario debe hacer<br/>logout/login para que<br/>el JWT se actualice<br/>con el nuevo rol
```

---

## 3. Flujo de Auditoría de Cambios

```mermaid
sequenceDiagram
    actor Admin
    participant Frontend
    participant Gateway
    participant AuthorityService
    participant AuthorityAuditService
    participant Database

    Admin->>Frontend: 1. Editar ROLE_MANAGER<br/>(cambiar description)
    Frontend->>Gateway: 2. PUT /api/authorities/3<br/>{description: "New desc"}
    Gateway->>AuthorityService: 3. updateAuthority(id, dto, exchange)
    AuthorityService->>Database: 4. SELECT * FROM scr_authority<br/>WHERE id = 3
    Database-->>AuthorityService: 5. Old state:<br/>{id: 3, description: "Old desc"}
    AuthorityService->>AuthorityService: 6. Verificar NO is_system
    Note over AuthorityService: Si is_system=true,<br/>lanzar error
    AuthorityService->>Database: 7. UPDATE scr_authority<br/>SET description = "New desc"<br/>WHERE id = 3
    Database-->>AuthorityService: 8. Updated (1 row)

    %% AUDITORÍA
    AuthorityService->>AuthorityAuditService: 9. createAuditLog(<br/>authorityId: 3,<br/>action: UPDATED,<br/>oldValues: {...},<br/>newValues: {...},<br/>exchange)
    AuthorityAuditService->>AuthorityAuditService: 10. Extraer metadata:<br/>- IP: 192.168.1.100<br/>- User-Agent: Chrome...<br/>- Changed By: admin
    AuthorityAuditService->>Database: 11. INSERT INTO scr_authority_audit<br/>(authority_id, action,<br/>old_values, new_values,<br/>changed_by, changed_date,<br/>ip_address, user_agent)
    Database-->>AuthorityAuditService: 12. Audit ID=789
    AuthorityAuditService-->>AuthorityService: 13. Auditoría registrada
    AuthorityService-->>Gateway: 14. 200 OK<br/>{id: 3, description: "New desc"}
    Gateway-->>Frontend: 15. Confirmación
    Frontend-->>Admin: 16. "Rol actualizado exitosamente"
```

---

## 4. Flujo de Expiración Automática (Scheduled Job)

```mermaid
sequenceDiagram
    participant ScheduledJob
    participant UserAuthRepo
    participant Database
    participant Logger

    Note over ScheduledJob: Cron: 0 0 2 * * *<br/>(Diario a las 2 AM)

    ScheduledJob->>ScheduledJob: 1. cleanupExpiredAuthorities()
    ScheduledJob->>Logger: 2. Log: "Starting cleanup..."

    %% CLEANUP DE ROLES
    ScheduledJob->>UserAuthRepo: 3. findExpiredAssignments()
    UserAuthRepo->>Database: 4. SELECT id FROM scr_user_authority<br/>WHERE expires_at <= NOW()<br/>AND is_active = true
    Database-->>UserAuthRepo: 5. [ID: 100, 200, 300]
    UserAuthRepo-->>ScheduledJob: 6. Lista de 3 asignaciones

    loop Para cada asignación expirada
        ScheduledJob->>Database: 7. UPDATE scr_user_authority<br/>SET is_active = false<br/>WHERE id = ?
    end

    Database-->>ScheduledJob: 8. 3 rows updated
    ScheduledJob->>Logger: 9. Log: "Cleaned up 3 expired authorities"

    %% CLEANUP DE PERMISOS DIRECTOS
    ScheduledJob->>UserAuthRepo: 10. findExpiredPermissionGrants()
    UserAuthRepo->>Database: 11. SELECT id FROM scr_user_permission<br/>WHERE expires_at <= NOW()<br/>AND is_active = true
    Database-->>UserAuthRepo: 12. [ID: 50]
    UserAuthRepo-->>ScheduledJob: 13. Lista de 1 permiso

    ScheduledJob->>Database: 14. UPDATE scr_user_permission<br/>SET is_active = false<br/>WHERE id = 50
    Database-->>ScheduledJob: 15. 1 row updated
    ScheduledJob->>Logger: 16. Log: "Cleaned up 1 expired permission"

    ScheduledJob->>Logger: 17. Log: "Cleanup completed successfully"
```

---

## 5. Flujo de Revocación Manual de Rol

```mermaid
sequenceDiagram
    actor Admin
    participant Frontend
    participant Gateway
    participant UserAuthService
    participant Database

    Admin->>Frontend: 1. Revocar ROLE_TEMP del usuario<br/>Reason: "Proyecto finalizado"
    Frontend->>Gateway: 2. DELETE /api/user-authorities/456<br/>Body: {reason: "Proyecto finalizado"}
    Gateway->>UserAuthService: 3. revokeById(456, reason, exchange)
    UserAuthService->>Database: 4. SELECT * FROM scr_user_authority<br/>WHERE id = 456
    Database-->>UserAuthService: 5. {id: 456, user_id: 123,<br/>authority_id: 3, is_active: true}

    UserAuthService->>UserAuthService: 6. Extraer metadata:<br/>- Revoked By: admin<br/>- IP: 192.168.1.100

    UserAuthService->>Database: 7. UPDATE scr_user_authority<br/>SET is_active = false,<br/>revoked_by = 'admin',<br/>revoked_date = NOW(),<br/>revoked_reason = 'Proyecto finalizado'<br/>WHERE id = 456

    Database-->>UserAuthService: 8. 1 row updated
    UserAuthService-->>Gateway: 9. 204 No Content
    Gateway-->>Frontend: 10. Confirmación
    Frontend-->>Admin: 11. "Rol revocado exitosamente"

    Note over Admin,Database: El usuario pierde acceso<br/>inmediatamente en próximo<br/>request (JWT se valida<br/>contra BD en cada petición)
```

---

## 6. Flujo de Validación de Permisos en Request

```mermaid
flowchart TD
    A[Request con JWT] --> B{JWT válido?}
    B -->|No| C[401 Unauthorized]
    B -->|Sí| D[Extraer claim 'auth']
    D --> E{Endpoint tiene @PreAuthorize?}
    E -->|No| F[Permitir acceso]
    E -->|Sí| G[Evaluar expresión]
    G --> H{Usuario tiene autoridad?}
    H -->|Sí| F
    H -->|No| I[403 Forbidden]

    style A fill:#e1f5ff
    style C fill:#ffcccc
    style F fill:#ccffcc
    style I fill:#ffcccc
```

---

## 7. Estados de Asignación de Rol

```mermaid
stateDiagram-v2
    [*] --> Activo: Asignar rol<br/>(is_active=true,<br/>expires_at=NULL o futuro)

    Activo --> Expirado_Pendiente: Fecha de expiración alcanzada<br/>(expires_at <= NOW())

    Expirado_Pendiente --> Expirado_Procesado: Scheduled job ejecuta<br/>(is_active=false)

    Activo --> Revocado: Revocación manual<br/>(is_active=false,<br/>revoked_by != NULL)

    Expirado_Pendiente --> Revocado: Revocación manual antes<br/>del job programado

    Revocado --> [*]: No se puede revertir<br/>(crear nueva asignación)

    Expirado_Procesado --> [*]: No se puede revertir<br/>(crear nueva asignación)

    note right of Activo
        Usuario tiene acceso
        Backend valida en cada request
    end note

    note right of Expirado_Pendiente
        Usuario YA NO tiene acceso
        (validación en backend)
        Pero is_active aún es true
    end note

    note right of Revocado
        Incluye razón de revocación
        Metadata completa de auditoría
    end note
```

---

## 8. Decisión de Autorización (Algoritmo)

```mermaid
flowchart TD
    Start[Usuario hace request] --> A[Extraer claim 'auth' del JWT]
    A --> B[Obtener lista de authorities]
    B --> C{Endpoint requiere<br/>hasAuthority?}
    C -->|No| Allow[✅ Permitir acceso]
    C -->|Sí| D[Obtener rol requerido<br/>ej: ROLE_ADMIN]
    D --> E{Rol requerido está<br/>en claim 'auth'?}
    E -->|Sí| Allow
    E -->|No| F{Endpoint requiere<br/>hasPermission?}
    F -->|No| Deny[❌ 403 Forbidden]
    F -->|Sí| G[Obtener permiso requerido<br/>ej: user.create]
    G --> H[Buscar permisos efectivos<br/>del usuario en BD]
    H --> I{Usuario tiene<br/>el permiso?}
    I -->|Sí| Allow
    I -->|No| Deny

    style Start fill:#e1f5ff
    style Allow fill:#ccffcc
    style Deny fill:#ffcccc
```

**Permisos efectivos = Permisos de roles + Permisos directos**

```sql
-- Query para permisos efectivos
SELECT DISTINCT p.name FROM scr_permission p
LEFT JOIN scr_authority_permission ap ON p.id = ap.permission_id
LEFT JOIN scr_user_authority ua ON ap.authority_id = ua.authority_id
WHERE ua.user_id = ? AND ua.is_active = true
  AND (ua.expires_at IS NULL OR ua.expires_at > NOW())
UNION
SELECT p.name FROM scr_permission p
JOIN scr_user_permission up ON p.id = up.permission_id
WHERE up.user_id = ? AND up.is_active = true
  AND (up.expires_at IS NULL OR up.expires_at > NOW());
```

---

## 9. Timeline de Expiración de Rol

```mermaid
gantt
    title Timeline de Rol Temporal (30 días)
    dateFormat YYYY-MM-DD
    section Asignación
    Asignar rol :milestone, 2025-11-01, 0d

    section Período Activo
    Usuario tiene acceso :active, 2025-11-01, 2025-12-01

    section Alertas
    Dashboard muestra alerta (7 días antes) :crit, 2025-11-24, 2025-12-01

    section Expiración
    Fecha de expiración alcanzada :milestone, 2025-12-01, 0d
    Usuario pierde acceso INMEDIATO :crit, 2025-12-01, 0d

    section Cleanup
    Job programado ejecuta (2 AM) :done, 2025-12-02, 0d
    is_active=false en BD :done, 2025-12-02, 0d
```

**Notas:**

- **2025-11-01**: Rol asignado con `expires_at = 2025-12-01`
- **2025-11-24**: Dashboard muestra alerta "Expira en 7 días"
- **2025-12-01 00:00**: Usuario pierde acceso (validación en backend)
- **2025-12-02 02:00**: Job marca `is_active=false` en BD

---

## 10. Comparación: Roles vs Permisos Directos

| Aspecto             | Via Roles                                   | Via Permisos Directos                        |
| ------------------- | ------------------------------------------- | -------------------------------------------- |
| **Asignación**      | Asignar rol al usuario                      | Grant permiso directo al usuario             |
| **Gestión**         | Centralizada (cambio en rol afecta a todos) | Individualizada (solo afecta al usuario)     |
| **Auditoría**       | Por rol y por usuario                       | Solo por usuario                             |
| **Uso recomendado** | Permisos permanentes y comunes              | Casos excepcionales temporales               |
| **Ejemplo**         | "Todos los managers pueden crear reportes"  | "Juan necesita exportar datos solo este mes" |
| **Ventaja**         | Escalable, fácil de gestionar               | Granular, flexible                           |
| **Desventaja**      | Menos flexible                              | Difícil de gestionar a escala                |

---

**Fin de los Diagramas de Flujo**
