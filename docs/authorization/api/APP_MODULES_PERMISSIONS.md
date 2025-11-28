# Permisos de Módulos de Aplicación

**Fecha:** 2025-11-27
**Changelog:** `20251112000000_app_modules_permissions.xml`

---

## Resumen

Este documento describe todos los permisos granulares creados para los módulos de la aplicación Tyse Scrutiny Gateway. Se han creado **34 permisos nuevos** (IDs 14-47) que cubren todas las funcionalidades de la aplicación.

**Total de permisos en el sistema:** 47 permisos (13 base + 34 módulos)

---

## 1. Dashboard Principal - Módulos Electorales (9 módulos, 22 permisos)

### 1.1 Divipol (División Política)

**Recurso:** `divipol`
**URL:** `/divipol`

| ID  | Permiso          | Acción | Descripción                       |
| --- | ---------------- | ------ | --------------------------------- |
| 14  | `divipol.read`   | read   | Ver datos de división política    |
| 15  | `divipol.create` | create | Crear nuevas entradas de divipol  |
| 16  | `divipol.update` | update | Actualizar información de divipol |
| 17  | `divipol.delete` | delete | Eliminar entradas de divipol      |

---

### 1.2 Statistics (Estadísticas Electorales)

**Recurso:** `statistics`
**URL:** `https://estadisticaselectorales.registraduria.gov.co/stage-one`

| ID  | Permiso             | Acción | Descripción                    |
| --- | ------------------- | ------ | ------------------------------ |
| 18  | `statistics.read`   | read   | Ver estadísticas electorales   |
| 19  | `statistics.export` | export | Exportar reportes estadísticos |

---

### 1.3 Heat Map (Mapa de Calor)

**Recurso:** `heatmap`
**URL:** `/heat-map` _(Coming Soon)_

| ID  | Permiso             | Acción    | Descripción                          |
| --- | ------------------- | --------- | ------------------------------------ |
| 20  | `heatmap.read`      | read      | Ver visualizaciones de mapa de calor |
| 21  | `heatmap.configure` | configure | Configurar parámetros del mapa       |

---

### 1.4 Vote Count (Conteo de Votos)

**Recurso:** `votecount`
**URL:** `http://186.31.4.135/CuentaVotos/`

| ID  | Permiso            | Acción | Descripción                  |
| --- | ------------------ | ------ | ---------------------------- |
| 22  | `votecount.read`   | read   | Ver datos de conteo de votos |
| 23  | `votecount.manage` | manage | Gestionar proceso de conteo  |

---

### 1.5 File Upload (Carga de Archivos)

**Recurso:** `fileupload`
**URL:** `/file-upload` _(Coming Soon)_

| ID  | Permiso             | Acción | Descripción                 |
| --- | ------------------- | ------ | --------------------------- |
| 24  | `fileupload.read`   | read   | Ver archivos cargados       |
| 25  | `fileupload.upload` | upload | Cargar archivos electorales |
| 26  | `fileupload.delete` | delete | Eliminar archivos cargados  |

---

### 1.6 Voting Juries (Jurados de Votación)

**Recurso:** `votingjuries`
**URL:** `/voting-juries` _(Coming Soon)_

| ID  | Permiso               | Acción | Descripción                       |
| --- | --------------------- | ------ | --------------------------------- |
| 27  | `votingjuries.read`   | read   | Ver información de jurados        |
| 28  | `votingjuries.manage` | manage | Gestionar asignaciones de jurados |

---

### 1.7 Trainings (Capacitaciones)

**Recurso:** `trainings`
**URL:** `http://jurna.top/`

| ID  | Permiso            | Acción | Descripción                   |
| --- | ------------------ | ------ | ----------------------------- |
| 29  | `trainings.read`   | read   | Ver programas de capacitación |
| 30  | `trainings.enroll` | enroll | Inscribirse en capacitaciones |

---

### 1.8 Witnesses (Testigos Electorales)

**Recurso:** `witnesses`
**URL:** `/witnesses` _(Coming Soon)_

| ID  | Permiso            | Acción | Descripción                       |
| --- | ------------------ | ------ | --------------------------------- |
| 31  | `witnesses.read`   | read   | Ver datos de testigos electorales |
| 32  | `witnesses.manage` | manage | Gestionar testigos electorales    |

---

### 1.9 Analytics (Analítica Electoral)

**Recurso:** `analytics`
**URL:** `/analytics` _(Coming Soon)_

| ID  | Permiso            | Acción | Descripción                  |
| --- | ------------------ | ------ | ---------------------------- |
| 33  | `analytics.read`   | read   | Ver analítica electoral      |
| 34  | `analytics.export` | export | Exportar reportes analíticos |

---

## 2. Módulos de Administración (7 módulos, 9 permisos)

### 2.1 Gateway

**Recurso:** `gateway`
**URL:** `/admin/gateway`

| ID  | Permiso          | Acción | Descripción                         |
| --- | ---------------- | ------ | ----------------------------------- |
| 35  | `gateway.read`   | read   | Ver rutas del gateway               |
| 36  | `gateway.manage` | manage | Gestionar configuración del gateway |

---

### 2.2 Metrics (Métricas)

**Recurso:** `metrics`
**URL:** `/admin/metrics`

| ID  | Permiso        | Acción | Descripción                   |
| --- | -------------- | ------ | ----------------------------- |
| 37  | `metrics.read` | read   | Ver métricas de la aplicación |

---

### 2.3 Health (Salud del Sistema)

**Recurso:** `health`
**URL:** `/admin/health`

| ID  | Permiso       | Acción | Descripción                          |
| --- | ------------- | ------ | ------------------------------------ |
| 38  | `health.read` | read   | Ver estado de salud de la aplicación |

---

### 2.4 Configuration (Configuración)

**Recurso:** `configuration`
**URL:** `/admin/configuration`

| ID  | Permiso                | Acción | Descripción                        |
| --- | ---------------------- | ------ | ---------------------------------- |
| 39  | `configuration.read`   | read   | Ver configuración de la aplicación |
| 40  | `configuration.update` | update | Actualizar configuración           |

---

### 2.5 Logs (Registros)

**Recurso:** `logs`
**URL:** `/admin/logs`

| ID  | Permiso       | Acción | Descripción                    |
| --- | ------------- | ------ | ------------------------------ |
| 41  | `logs.read`   | read   | Ver registros de la aplicación |
| 42  | `logs.update` | update | Actualizar niveles de log      |

---

### 2.6 API Docs (Documentación de API)

**Recurso:** `docs`
**URL:** `/admin/docs`

| ID  | Permiso     | Acción | Descripción                 |
| --- | ----------- | ------ | --------------------------- |
| 43  | `docs.read` | read   | Ver documentación de la API |

---

### 2.7 Authorization Dashboard

**Recurso:** `authorization_dashboard`
**URL:** `/admin/authorization-dashboard`

| ID  | Permiso                        | Acción | Descripción                   |
| --- | ------------------------------ | ------ | ----------------------------- |
| 44  | `authorization_dashboard.read` | read   | Ver dashboard de autorización |

---

## 3. Módulos de Cuenta (1 módulo, 3 permisos)

### 3.1 Account (Cuenta de Usuario)

**Recurso:** `account`
**URLs:** `/account/settings`, `/account/password`

| ID  | Permiso                   | Acción          | Descripción                  |
| --- | ------------------------- | --------------- | ---------------------------- |
| 45  | `account.settings.read`   | settings.read   | Ver ajustes de cuenta        |
| 46  | `account.settings.update` | settings.update | Actualizar ajustes de cuenta |
| 47  | `account.password.update` | password.update | Cambiar contraseña de cuenta |

---

## 4. Permisos Existentes (Base Authorization System)

Para referencia, estos son los permisos base del sistema de autorización (IDs 1-13):

| ID  | Permiso             | Recurso    | Acción | Descripción                    |
| --- | ------------------- | ---------- | ------ | ------------------------------ |
| 1   | `user.create`       | user       | create | Crear nuevos usuarios          |
| 2   | `user.read`         | user       | read   | Ver información de usuarios    |
| 3   | `user.update`       | user       | update | Modificar detalles de usuarios |
| 4   | `user.delete`       | user       | delete | Eliminar usuarios              |
| 5   | `authority.create`  | authority  | create | Crear nuevos roles             |
| 6   | `authority.read`    | authority  | read   | Ver información de roles       |
| 7   | `authority.update`  | authority  | update | Modificar detalles de roles    |
| 8   | `authority.delete`  | authority  | delete | Eliminar roles                 |
| 9   | `authority.assign`  | authority  | assign | Asignar roles a usuarios       |
| 10  | `permission.create` | permission | create | Crear nuevos permisos          |
| 11  | `permission.read`   | permission | read   | Ver información de permisos    |
| 12  | `permission.update` | permission | update | Modificar detalles de permisos |
| 13  | `permission.delete` | permission | delete | Eliminar permisos              |

---

## 5. Asignación de Permisos a Roles

### ROLE_ADMIN (authority_id=1)

✅ **Tiene TODOS los 47 permisos del sistema** (IDs 1-47)

### ROLE_USER (authority_id=2)

✅ **Tiene permisos básicos de lectura:**

- `user.read` (ID: 2)
- `authority.read` (ID: 6)
- `permission.read` (ID: 11)

**Nota:** Los usuarios regulares pueden tener permisos adicionales de módulos asignados según sus necesidades específicas.

---

## 6. Patrón de Nombres de Permisos

Los permisos siguen el patrón: `{resource}.{action}`

### Recursos Identificados:

- **Dashboard:** `divipol`, `statistics`, `heatmap`, `votecount`, `fileupload`, `votingjuries`, `trainings`, `witnesses`, `analytics`
- **Admin:** `gateway`, `metrics`, `health`, `configuration`, `logs`, `docs`, `authorization_dashboard`
- **Account:** `account`
- **Base System:** `user`, `authority`, `permission`

### Acciones Comunes:

- **CRUD:** `create`, `read`, `update`, `delete`
- **Específicas:** `export`, `configure`, `manage`, `upload`, `enroll`, `assign`
- **Compuestas:** `settings.read`, `settings.update`, `password.update`

---

## 7. Uso en el Frontend

### 7.1 Obtención de Permisos

El endpoint `GET /api/account` retorna la información del usuario autenticado **incluyendo sus permisos efectivos**:

```json
{
  "id": 1,
  "login": "admin",
  "firstName": "Administrator",
  "lastName": "Administrator",
  "email": "admin@localhost",
  "authorities": ["ROLE_ADMIN"],
  "permissions": ["divipol.read", "divipol.create", "statistics.read", "statistics.export", "heatmap.read", "..."]
}
```

Los permisos se calculan combinando:

- **Permisos de roles:** `user → user_authority → authority_permission → permission`
- **Permisos directos:** `user → user_permission → permission`

### 7.2 Verificación de Permisos en Componentes

```typescript
import { useAppSelector } from 'app/config/store';

// Ejemplo: Verificar si el usuario puede exportar estadísticas
const canExportStatistics = useAppSelector(state =>
  state.authentication.account.permissions?.includes('statistics.export')
);

// Ejemplo: Renderizado condicional
{canExportStatistics && (
  <Button onClick={handleExport}>
    <Translate contentKey="entity.action.export">Export</Translate>
  </Button>
)}
```

### 7.3 Filtrado de Módulos en Dashboard

El `DashboardGrid` filtra los módulos según los permisos del usuario:

```typescript
// dashboard-grid.tsx
const userPermissions: string[] = useAppSelector(state => state.authentication.account?.permissions || []);

const modules: Module[] = [
  {
    id: 'divipol',
    title: 'Divipol',
    requiredPermission: 'divipol.read', // Permiso requerido para ver este módulo
    // ...
  },
  {
    id: 'statistics',
    title: 'Statistics',
    requiredPermission: 'statistics.read',
    // ...
  },
];

// Solo muestra módulos para los que el usuario tiene permiso
const accessibleModules = modules.filter(module => userPermissions.includes(module.requiredPermission));
```

---

## 8. Uso en el Backend

Para proteger endpoints con permisos:

```java
@RestController
@RequestMapping("/api/statistics")
public class StatisticsResource {

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasPermission(null, 'statistics.read')")
  public Flux<StatisticsDTO> getStatistics() {
    // ...
  }

  @PostMapping("/export")
  @PreAuthorize("hasPermission(null, 'statistics.export')")
  public Mono<ResponseEntity<byte[]>> exportStatistics() {
    // ...
  }
}

```

---

## 9. Aplicación de Migraciones

Para aplicar los nuevos permisos, simplemente reinicia la aplicación:

```bash
./mvnw spring-boot:run
```

Liquibase ejecutará automáticamente:

1. `20251112000001-load-app-permissions` - Carga los 34 nuevos permisos
2. `20251112000002-assign-permissions-to-admin` - Asigna todos los permisos a ROLE_ADMIN

---

## 10. Rollback

Si necesitas revertir los cambios:

```bash
./mvnw liquibase:rollback -Dliquibase.rollbackCount=2
```

Esto eliminará:

- Las 34 asignaciones de permisos a ROLE_ADMIN (IDs 17-50)
- Los 34 permisos de módulos (IDs 14-47)

---

**Fin del Documento**
