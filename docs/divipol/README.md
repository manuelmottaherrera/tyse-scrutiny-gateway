# Documentación Módulo Divipol

Documentación del módulo de División Política Electoral de Colombia (DIVIPOL).

---

## Índice de Documentación

### Diseño Técnico

| Documento                                            | Descripción                          |
| ---------------------------------------------------- | ------------------------------------ |
| **[Diseño del Buscador](./design/search-design.md)** | Especificación completa del buscador |

### Guías de Usuario

| Documento                                                     | Descripción              |
| ------------------------------------------------------------- | ------------------------ |
| **[Manual del Buscador](./user-manual/search-user-guide.md)** | Guía de uso del buscador |

---

## Descripción del Módulo

El módulo Divipol gestiona y consulta la división política electoral de Colombia:

- **18,016 registros** de la estructura electoral
- Jerarquía: Departamento → Municipio → Zona → Puesto
- Datos de potencial electoral (femenino, masculino, total)
- Cantidad de mesas por ubicación

### Funcionalidades

| Funcionalidad            | Estado          | Documentación                                                                     |
| ------------------------ | --------------- | --------------------------------------------------------------------------------- |
| Visualización jerárquica | ✅ Implementado | -                                                                                 |
| Filtros cascada          | ✅ Implementado | -                                                                                 |
| Filtros con URL params   | ✅ Implementado | -                                                                                 |
| Estadísticas             | ✅ Implementado | -                                                                                 |
| **Buscador**             | ✅ Implementado | [Diseño](./design/search-design.md), [Manual](./user-manual/search-user-guide.md) |
| Exportación CSV/PDF      | Pendiente       | -                                                                                 |
| Vista detalle de puestos | Pendiente       | -                                                                                 |

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│  Frontend (Gateway :8080)                                   │
│  ├── DivipolPage                                            │
│  ├── DivipolFilters                                         │
│  ├── DivipolTable                                           │
│  ├── DivipolStatsCards                                      │
│  ├── DivipolSearch (nuevo)                                  │
│  └── DivipolExport                                          │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ HTTP via Consul
┌─────────────────────────────────────────────────────────────┐
│  API (Microservicio :8081)                                  │
│  ├── /api/divipol/departamentos                             │
│  ├── /api/divipol/municipios                                │
│  ├── /api/divipol/zonas                                     │
│  ├── /api/divipol/puestos                                   │
│  ├── /api/divipol/stats/*                                   │
│  └── /api/divipol/search (nuevo)                            │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ R2DBC
┌─────────────────────────────────────────────────────────────┐
│  PostgreSQL (:5433)                                         │
│  ├── tabla: divipol                                         │
│  └── vistas: view_divipol_*                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Enlaces Relacionados

- **Microservicio:** `tyse-scrutiny-micro-divipol`
- **Frontend:** `src/main/webapp/app/modules/divipol/`
- **i18n:** `src/main/webapp/i18n/*/divipol.json`

---

**Última actualización:** 2025-12-05
