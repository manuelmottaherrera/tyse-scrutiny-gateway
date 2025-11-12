# Documentación del Sistema de Autorización Enterprise (SAE)

**Versión:** 1.0
**Fecha:** 2025-11-12
**Proyecto:** Tyse Scrutiny Gateway

---

## Índice General

Esta carpeta contiene toda la documentación relacionada con el **Sistema de Autorización Enterprise** implementado en Tyse Scrutiny Gateway. El sistema provee control de acceso basado en roles (RBAC) con características avanzadas como asignaciones temporales, permisos directos y auditoría completa.

---

## Documentación por Audiencia

### Para Administradores del Sistema

- **[Manual de Usuario](user-manual/AUTHORIZATION_ADMIN_GUIDE.md)** - Guía completa para gestionar roles, permisos y usuarios
- **[FAQ](FAQ.md)** - Preguntas frecuentes sobre el sistema de autorización
- **[Troubleshooting](TROUBLESHOOTING.md)** - Solución de problemas comunes

### Para Ingenieros de Operaciones (DevOps/SRE)

- **[Guía de Operaciones](operations/AUTHORIZATION_OPS_GUIDE.md)** - Despliegue, monitoreo, backup, escalabilidad

### Para Desarrolladores

- **[API Reference](api/AUTHORIZATION_API_REFERENCE.md)** - Documentación completa de todos los endpoints REST
- **[Permisos de Módulos de Aplicación](api/APP_MODULES_PERMISSIONS.md)** - API específica de permisos por módulos

### Diagramas Técnicos

- **[Flujo de Autorización](diagrams/authorization-flow.md)** - Diagramas de secuencia y flujos de datos
- **[Arquitectura de Componentes](diagrams/component-architecture.md)** - Estructura de capas y componentes
- **[Diagrama ER de Base de Datos](diagrams/database-er-diagram.md)** - Esquema completo de tablas y relaciones

---

## Estructura de Carpetas

```
docs/authorization/
├── README.md                                      # Este archivo
├── FAQ.md                                         # Preguntas frecuentes
├── TROUBLESHOOTING.md                             # Solución de problemas
├── api/
│   ├── AUTHORIZATION_API_REFERENCE.md             # API completa
│   └── APP_MODULES_PERMISSIONS.md                 # API de permisos de módulos
├── diagrams/
│   ├── authorization-flow.md                      # Flujos y secuencias
│   ├── component-architecture.md                  # Arquitectura técnica
│   └── database-er-diagram.md                     # Modelo de datos
├── operations/
│   └── AUTHORIZATION_OPS_GUIDE.md                 # Guía operativa
└── user-manual/
    └── AUTHORIZATION_ADMIN_GUIDE.md               # Manual del administrador
```

---

## Inicio Rápido

### Como Administrador

1. Leer el **[Manual de Usuario](user-manual/AUTHORIZATION_ADMIN_GUIDE.md)** (sección 1: Introducción)
2. Acceder al dashboard: `/admin/authorization-dashboard`
3. Consultar **[FAQ](FAQ.md)** para conceptos clave

### Como Desarrollador

1. Revisar **[API Reference](api/AUTHORIZATION_API_REFERENCE.md)** para conocer los endpoints disponibles
2. Estudiar **[Arquitectura de Componentes](diagrams/component-architecture.md)** para entender la estructura
3. Ver **[Diagrama ER](diagrams/database-er-diagram.md)** para comprender el modelo de datos

### Como Operador/SRE

1. Leer **[Guía de Operaciones](operations/AUTHORIZATION_OPS_GUIDE.md)** completa
2. Configurar monitoreo siguiendo sección 4: Monitoreo y Métricas
3. Establecer backup siguiendo sección 6: Backup y Restore

---

## Características Principales del SAE

✅ **Roles Jerárquicos** - Organización de permisos con niveles de precedencia
✅ **Permisos Granulares** - Control fino con patrón `resource.action`
✅ **Asignaciones Temporales** - Roles con expiración automática
✅ **Permisos Directos** - Bypass de roles para casos excepcionales
✅ **Auditoría Completa** - Trazabilidad total de cambios con IP, User-Agent, timestamps
✅ **Dashboard Interactivo** - Métricas en tiempo real, alertas de expiración
✅ **Roles de Sistema Protegidos** - ROLE_ADMIN y ROLE_USER no modificables
✅ **Cleanup Automático** - Job programado para roles expirados

---

## Tecnologías Utilizadas

**Backend:**

- Spring Boot 3.4.5 con Spring WebFlux (reactive)
- Spring Security con JWT authentication
- R2DBC para acceso reactivo a PostgreSQL
- Liquibase para migraciones de BD

**Frontend:**

- React 18 con TypeScript
- Redux Toolkit para gestión de estado
- Recharts para visualizaciones

**Base de Datos:**

- PostgreSQL 14+ con soporte JSON

---

## Soporte y Contacto

**Para reportar problemas:**

- Crear issue en el repositorio del proyecto
- Etiqueta: `authorization`

**Documentación adicional:**

- Ver carpeta `docs/ci-cd/` para CI/CD general del proyecto
- Consultar `CLAUDE.md` en raíz del proyecto para guías de desarrollo

---

**Última actualización:** 2025-11-12
