# Documentación Módulo Divipol

Documentación del módulo de División Política Electoral de Colombia (DIVIPOL) y Sistema de Testigos Electorales.

---

## Índice de Documentación

### Diseño Técnico

| Documento                                            | Descripción                          |
| ---------------------------------------------------- | ------------------------------------ |
| **[Diseño del Buscador](./design/search-design.md)** | Especificación completa del buscador |
| **[Modelo de Datos](./modelo-datos.md)**             | Diagrama ER del sistema de testigos  |
| **[Rutas Frontend](./rutas-frontend.md)**            | Mapa de rutas del módulo             |

### Guías de Usuario

| Documento                                                     | Descripción              |
| ------------------------------------------------------------- | ------------------------ |
| **[Manual del Buscador](./user-manual/search-user-guide.md)** | Guía de uso del buscador |

---

## Descripción del Módulo

El módulo Divipol gestiona:

### 1. División Política Electoral

- **18,016 registros** de la estructura electoral
- Jerarquía: Departamento → Municipio → Zona → Puesto
- Datos de potencial electoral (femenino, masculino, total)
- Cantidad de mesas por ubicación

### 2. Sistema de Testigos Electorales

Implementación completa según normativa colombiana (Ley 1475/2011, Código Electoral, Resolución CNE 09458/2025):

- **Organizaciones Políticas** - Partidos, movimientos, coaliciones
- **Testigos Electorales** - Registro y gestión de personas
- **Mesas de Votación** - Auto-generadas desde divipol
- **Comisiones Escrutadoras** - Auxiliares, municipales, distritales, generales
- **Asignación de Testigos** - A mesas y comisiones con reglas normativas
- **Credenciales** - E15 (mesa) y E16 (comisión)
- **Reclamaciones** - Registro y resolución de reclamaciones electorales
- **Configuración Electoral** - Períodos de inscripción, límites

---

## Funcionalidades

| Funcionalidad                | Estado          | Documentación                                                                     |
| ---------------------------- | --------------- | --------------------------------------------------------------------------------- |
| Visualización jerárquica     | ✅ Implementado | -                                                                                 |
| Filtros cascada              | ✅ Implementado | -                                                                                 |
| Filtros con URL params       | ✅ Implementado | -                                                                                 |
| Estadísticas                 | ✅ Implementado | -                                                                                 |
| **Buscador**                 | ✅ Implementado | [Diseño](./design/search-design.md), [Manual](./user-manual/search-user-guide.md) |
| Exportación CSV/PDF          | ✅ Implementado | -                                                                                 |
| **Detalle de Puesto**        | ✅ Implementado | Full page con tabs (info, jurados, testigos)                                      |
| **Organizaciones Políticas** | ✅ Implementado | CRUD completo                                                                     |
| **Comisiones Escrutadoras**  | ✅ Implementado | CRUD completo                                                                     |
| **Testigos Electorales**     | ✅ Implementado | Registro, asignación a mesas/comisiones                                           |
| **Credenciales E15/E16**     | ✅ Implementado | Ciclo de vida completo                                                            |
| **Reclamaciones**            | ✅ Implementado | Registro y resolución                                                             |
| **Configuración Electoral**  | ✅ Implementado | Períodos de inscripción, límites                                                  |

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Frontend (Gateway :8080)                                                   │
│  ├── DivipolPage (/divipol)                                                 │
│  │   ├── DivipolFilters, DivipolTable, DivipolStatsCards                    │
│  │   ├── DivipolSearch, DivipolExport                                       │
│  │   └── Hook: useDivipolSearchParams (URL-driven state)                    │
│  ├── PuestoDetailPage (/divipol/puestos/:id)                                │
│  │   ├── PuestoInfoTab, PuestoJuradosTab, PuestoTestigosTab                 │
│  │   └── Tabs con query param: ?tab=info|jurados|testigos                   │
│  ├── OrganizacionesPage (/divipol/organizaciones)                           │
│  ├── ComisionesPage (/divipol/comisiones)                                   │
│  ├── ReclamacionesPage (/divipol/reclamaciones)                             │
│  └── ConfiguracionElectoralPage (/divipol/configuracion)                    │
└─────────────────────────────────────────────────────────────────────────────┘
                          │
                          ▼ HTTP via Consul
┌─────────────────────────────────────────────────────────────────────────────┐
│  API (Microservicio :8081)                                                  │
│  ├── /api/divipol/* - Datos geográficos                                     │
│  ├── /api/testigos - Gestión de testigos                                    │
│  ├── /api/organizaciones - Organizaciones políticas                         │
│  ├── /api/comisiones - Comisiones escrutadoras                              │
│  ├── /api/credenciales - Credenciales E15/E16                               │
│  ├── /api/reclamaciones - Reclamaciones electorales                         │
│  └── /api/configuracion - Configuración electoral                           │
└─────────────────────────────────────────────────────────────────────────────┘
                          │
                          ▼ R2DBC
┌─────────────────────────────────────────────────────────────────────────────┐
│  PostgreSQL (:5433)                                                         │
│  ├── divipol, view_divipol_* - División política                            │
│  ├── mesa_votacion - Mesas de votación                                      │
│  ├── organizacion_politica - Partidos y movimientos                         │
│  ├── comision_escrutadora - Comisiones                                      │
│  ├── testigo_electoral - Testigos registrados                               │
│  ├── testigo_mesa, testigo_comision - Asignaciones                          │
│  ├── credencial - Credenciales E15/E16                                      │
│  ├── reclamacion - Reclamaciones electorales                                │
│  └── configuracion_electoral - Parámetros del sistema                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Modelo de Datos de Testigos

```
organizacion_politica (catálogo)
       │
testigo_electoral (persona, con organizacion_id)
       │
  ┌────┴─────┐
  │          │
testigo_mesa  testigo_comision
  │               │
mesa_votacion  comision_escrutadora
  │
divipol (puestos)

credencial ──→ testigo_mesa | testigo_comision
reclamacion ──→ testigo_electoral + mesa_votacion | comision_escrutadora

configuracion_electoral (parámetros de plazos y límites)
```

### Reglas Normativas Implementadas

| Regla                            | Descripción                                          |
| -------------------------------- | ---------------------------------------------------- |
| Límite de principales            | Máximo 1 testigo principal por organización por mesa |
| Límite de remanentes (<10 mesas) | Máximo 1 remanente por organización                  |
| Límite de remanentes (≥10 mesas) | Hasta 10% del total de mesas                         |
| Período de inscripción           | Validado contra configuracion_electoral              |
| Credenciales                     | Solo se pueden crear si hay asignación activa        |

---

## Rutas Frontend

| Ruta                         | Componente                 | Descripción                                |
| ---------------------------- | -------------------------- | ------------------------------------------ |
| `/divipol`                   | DivipolPage                | Explorador jerárquico de división política |
| `/divipol/puestos/:puestoId` | PuestoDetailPage           | Detalle de puesto con tabs                 |
| `/divipol/organizaciones`    | OrganizacionesPage         | CRUD de organizaciones políticas           |
| `/divipol/comisiones`        | ComisionesPage             | CRUD de comisiones escrutadoras            |
| `/divipol/reclamaciones`     | ReclamacionesPage          | Listado de reclamaciones con filtros       |
| `/divipol/configuracion`     | ConfiguracionElectoralPage | Configuración del sistema (admin)          |

**Patrón:** Full Page Routes con URL-driven state (no modales).

---

## Enlaces Relacionados

- **Microservicio:** `tyse-scrutiny-micro-divipol`
- **Frontend:** `src/main/webapp/app/modules/divipol/`
- **Servicio API:** `src/main/webapp/app/shared/services/divipol.service.ts`
- **i18n:** `src/main/webapp/i18n/*/divipol.json`

---

**Última actualización:** 2026-02-06
