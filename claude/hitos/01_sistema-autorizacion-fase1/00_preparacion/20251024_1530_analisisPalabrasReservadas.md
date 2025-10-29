# Análisis: Palabras Reservadas en Bases de Datos

**Fecha:** 2025-10-24 15:30
**Propósito:** Evaluar el riesgo de usar nombres de tablas sin prefijo y proponer alternativas

---

## 🔍 Palabras Reservadas por DBMS

### `user` como Palabra Reservada

| Base de Datos  | ¿Es Reservada? | Requiere Escape | Notas                                                 |
| -------------- | -------------- | --------------- | ----------------------------------------------------- |
| **PostgreSQL** | ✅ Sí          | Opcional        | Se puede usar sin quotes, pero puede causar confusión |
| **MySQL**      | ✅ Sí          | Sí (backticks)  | Requiere \`user\`                                     |
| **Oracle**     | ✅ Sí          | Sí (quotes)     | Requiere "user"                                       |
| **SQL Server** | ✅ Sí          | Sí (brackets)   | Requiere [user]                                       |
| **H2**         | ✅ Sí          | Sí              | Base de datos de tests                                |
| **SQLite**     | ❌ No          | No              | No es reservada                                       |

**Conclusión:** `user` es palabra reservada en **casi todos** los DBMS principales.

---

### Otras palabras en nuestro diseño

| Palabra      | PostgreSQL | MySQL | Oracle | SQL Server | Riesgo   |
| ------------ | ---------- | ----- | ------ | ---------- | -------- |
| `user`       | ✅         | ✅    | ✅     | ✅         | 🔴 ALTO  |
| `permission` | ❌         | ❌    | ❌     | ❌         | 🟢 BAJO  |
| `authority`  | ❌         | ❌    | ❌     | ❌         | 🟢 BAJO  |
| `role`       | ❌         | ❌    | ❌     | ❌         | 🟢 BAJO  |
| `group`      | ⚠️         | ⚠️    | ⚠️     | ⚠️         | 🟡 MEDIO |

---

## 🎯 Propuesta de Prefijo

### Opciones de Prefijo

| Opción      | Ejemplo     | Pros                        | Contras                                |
| ----------- | ----------- | --------------------------- | -------------------------------------- |
| **`app_`**  | `app_user`  | Genérico, corto             | Poco descriptivo                       |
| **`sys_`**  | `sys_user`  | Común en sistemas           | Puede confundir con tablas del sistema |
| **`tyse_`** | `tyse_user` | Identifica el proyecto      | Largo, específico del proyecto         |
| **`scr_`**  | `scr_user`  | Acrónimo de Scrutiny        | Corto, relacionado al proyecto         |
| **`ts_`**   | `ts_user`   | Muy corto, acrónimo de TySE | Riesgo de colisión, poco descriptivo   |

---

## 💡 Recomendación: Usar prefijo `scr_`

### Justificación

**`scr_`** = **Scr**utiny

✅ **Ventajas:**

1. **Corto** (3 caracteres + underscore)
2. **Relacionado al proyecto** (Scrutiny Gateway)
3. **Único** (baja probabilidad de colisión)
4. **Evita palabras reservadas** en todos los DBMS
5. **Consistente** con naming de esquemas enterprise
6. **Escalable** si en el futuro hay múltiples apps en la misma BD

✅ **Ejemplos de uso:**

```sql
scr_user
scr_authority
scr_permission
scr_user_authority
scr_user_permission
scr_authority_permission
scr_authority_audit
```

---

## 📊 Schema Propuesto con Prefijo

### Tablas del Sistema Enterprise

| Tabla Anterior          | Tabla con Prefijo          | Descripción              |
| ----------------------- | -------------------------- | ------------------------ |
| `user`                  | `scr_user`                 | Usuarios del sistema     |
| `authorities`           | `scr_authority`            | Roles del sistema        |
| `permissions`           | `scr_permission`           | Permisos granulares      |
| `authority_permissions` | `scr_authority_permission` | N:N roles-permisos       |
| `user_authorities`      | `scr_user_authority`       | Asignaciones usuario-rol |
| `user_permissions`      | `scr_user_permission`      | Permisos directos        |
| `authority_audit`       | `scr_authority_audit`      | Log de cambios           |

---

## 🎨 Alternativa: Prefijo `tyse_`

Si prefieres algo más corporativo:

**`tyse_`** = **TySE** (nombre de la empresa)

✅ **Ventajas:**

- Identifica claramente que son tablas de TySE
- Si hay múltiples aplicaciones TySE, todas comparten prefijo
- Más formal y corporativo

⚠️ **Desventajas:**

- Un carácter más largo (4 vs 3)
- Menos específico de Scrutiny

**Ejemplos:**

```sql
tyse_user
tyse_authority
tyse_permission
```

---

## 📋 Comparación Visual

### Sin Prefijo (actual)

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    login VARCHAR(50)
);

CREATE TABLE authority (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50)
);
```

### Con Prefijo `scr_`

```sql
CREATE TABLE scr_user (
    id BIGINT PRIMARY KEY,
    login VARCHAR(50)
);

CREATE TABLE scr_authority (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50)
);
```

---

## 🔄 Impacto en el Código

### Entity Classes

**Antes:**

```java
@Table("user")
public class User { ... }

@Table("authority")
public class Authority { ... }
```

**Después:**

```java
@Table("scr_user")
public class User { ... }

@Table("scr_authority")
public class Authority { ... }
```

### Repositories

**Queries SQL:**

```sql
-- Antes
SELECT * FROM user u
JOIN user_authorities ua ON u.id = ua.user_id

-- Después
SELECT * FROM scr_user u
JOIN scr_user_authority ua ON u.id = ua.user_id
```

---

## 🌍 Portabilidad entre DBMS

Con el prefijo `scr_`, el código funcionará **sin modificaciones** en:

- ✅ PostgreSQL (actual)
- ✅ MySQL / MariaDB
- ✅ Oracle
- ✅ SQL Server
- ✅ H2 (tests)
- ✅ SQLite
- ✅ CockroachDB
- ✅ Amazon Aurora

**Sin necesidad de quotes, backticks o brackets.**

---

## 📝 Convención de Naming Completa

### Estándar Propuesto

```
[prefijo]_[nombre_descriptivo]

Donde:
- prefijo = scr (o tyse)
- nombre = singular, snake_case
- separador = underscore
```

**Ejemplos:**

```
scr_user                    ✅
scr_authority               ✅
scr_permission              ✅
scr_user_authority          ✅
scr_authority_permission    ✅
scr_authority_audit         ✅
```

---

## 🔍 Precedentes en la Industria

### Otros proyectos usan prefijos

| Proyecto  | Prefijo   | Ejemplo        |
| --------- | --------- | -------------- |
| WordPress | `wp_`     | `wp_users`     |
| Drupal    | `drupal_` | `drupal_users` |
| Magento   | `mg_`     | `mg_customer`  |
| GitLab    | `gitlab_` | `gitlab_users` |
| Atlassian | `AO_`     | `AO_user`      |

**Lección:** Los proyectos enterprise serios usan prefijos para evitar colisiones.

---

## ⚡ Decisión Recomendada

### Opción Recomendada: `scr_`

**Razones:**

1. ✅ Evita palabras reservadas en todos los DBMS
2. ✅ Corto y fácil de escribir
3. ✅ Identifica el proyecto (Scrutiny)
4. ✅ Escalable para futuro
5. ✅ Sigue mejores prácticas de la industria

### Naming Final

```sql
-- Core
scr_user
scr_authority
scr_permission

-- Relaciones
scr_user_authority
scr_user_permission
scr_authority_permission

-- Auditoría
scr_authority_audit
```

---

## 🎯 Próxima Acción

Si decides usar el prefijo `scr_`:

1. Actualizar `00000000000000_initial_schema.xml`

   - `user` → `scr_user`

2. Actualizar `User.java`

   - `@Table("user")` → `@Table("scr_user")`

3. Crear changelog enterprise con todas las tablas con prefijo `scr_`

4. Todos los futuros changelogs seguirán esta convención

---

## ✅ Beneficios a Largo Plazo

- 🔒 **Portabilidad garantizada** entre DBMS
- 🚀 **Sin problemas de palabras reservadas**
- 📦 **Namespace claro** (todas las tablas identificadas)
- 🔧 **Fácil de filtrar** en herramientas de BD (`SHOW TABLES LIKE 'scr_%'`)
- 👥 **Múltiples apps** pueden coexistir en la misma BD
- 📚 **Mejor para DBAs** (saben qué tablas pertenecen a qué app)

---

## 🤔 Pregunta para Decidir

**¿Prefieres `scr_` o `tyse_`?**

- **`scr_`** = Específico de Scrutiny (más técnico)
- **`tyse_`** = Corporativo, TySE company-wide (más formal)

Ambos son excelentes opciones y cumplen el objetivo de portabilidad.

---

**Fecha:** 2025-10-24 15:30
**Recomendación:** Usar prefijo `scr_` para todas las tablas
