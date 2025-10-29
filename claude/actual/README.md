# Carpeta de Trabajo Actual

**Fecha de creación:** 2025-10-28 23:44
**Propósito:** Almacenar planes y contextos temporales durante el trabajo en un hito

---

## 📋 Descripción

Esta carpeta contiene **planes y contextos temporales** mientras se trabaja en un hito. Los **reportes finales se crean directamente** en la carpeta del hito correspondiente (`claude/hitos/XX_nombre-hito/`).

**⚠️ IMPORTANTE:** Esta carpeta es solo para trabajo en progreso. Los reportes de sesión y documentación final van directo al hito.

---

## 🗂️ Qué va en esta carpeta

### Planes de Trabajo

Mientras trabajas en un hito, guarda aquí los planes:

**Formato:** `YYYYMMDD_HHMM_plan[Nombre].md`
**Ejemplo:** `20251029_1051_planFase5Servicios.md`

**Contenido:**

- Objetivo del trabajo
- Tareas a realizar
- Decisiones pendientes
- Referencias necesarias

### Contextos Temporales

Contextos de conversaciones que aún están en progreso:

**Formato:** `YYYYMMDD_HHMM_contexto_[nombre].md`
**Ejemplo:** `20251029_1000_contexto_migracion.md`

---

## 🔄 Nuevo Flujo de Trabajo

### 1. Al Iniciar un Hito

```bash
# Crea la carpeta del hito
mkdir -p claude/hitos/XX_nombre-del-hito

# Trabaja con planes en claude/actual/
```

### 2. Durante el Trabajo

- **Planes**: Guarda en `claude/actual/`
- **Reportes**: Crea directamente en `claude/hitos/XX_nombre-hito/YYYYMMDD_HHMM_reporte.md`

### 3. Al Completar una Sesión

```bash
# Crea reporte DIRECTAMENTE en el hito
vim claude/hitos/05_sistema-autorizacion-fase5/20251029_1615_completacionFase5.md
```

### 4. Al Completar el Hito

```bash
# 1. Mueve el plan de actual/ al hito
mv claude/actual/20251029_1051_planFase5.md claude/hitos/05_sistema-autorizacion-fase5/

# 2. Crea README.md del hito
vim claude/hitos/05_sistema-autorizacion-fase5/README.md

# 3. Limpia actual/
rm claude/actual/*.md  # Solo si ya están archivados
```

---

## 📂 Nueva Estructura de Hitos

```
claude/hitos/
└── 05_sistema-autorizacion-fase5/
    ├── README.md                              # Resumen ejecutivo
    ├── 20251029_1051_planFase5Servicios.md   # Plan (movido de actual/)
    ├── 20251029_1615_completacionFase5.md    # Reporte (creado directo)
    └── 00_preparacion/                        # Subcarpetas opcionales
        └── analisisServicios.md
```

---

## ✅ Checklist de Archivo

Al completar un hito:

- [ ] Plan movido de `actual/` al hito
- [ ] Reportes creados directamente en el hito (NO en actual/)
- [ ] README.md del hito creado con resumen ejecutivo
- [ ] Documentos organizados (con subcarpetas si es necesario)
- [ ] Carpeta `actual/` limpia de archivos archivados
- [ ] Actualizado `claude/actual/README.md` con estado actual

---

## 🎯 Convención de Nombres

### Hitos

**Formato:** `XX_nombre-descriptivo-del-hito`

**Ejemplos:**

- `05_sistema-autorizacion-fase5`
- `06_sistema-autorizacion-fase6`
- `07_frontend-autorizacion`

### Reportes (directo en hito)

**Formato:** `YYYYMMDD_HHMM_nombreDescriptivo.md`

**Ejemplos:**

- `20251029_1615_completacionFase5.md`
- `20251029_1700_pruebasIntegracion.md`

---

## 📊 Estado Actual

**Última actualización:** 2025-10-29 11:00

**Hitos completados:** 4

- `01_sistema-autorizacion-fase1` ✅ - Base de datos y entidades del sistema de autorización
- `02_organizacion-documentacion` ✅ - Reorganización de documentación del proyecto
- `03_sistema-autorizacion-fase2` ✅ - Entidades enterprise completas (Permission, UserAuthority, UserPermission, AuthorityAudit)
- `04_sistema-autorizacion-fase3` ✅ - Repositorios R2DBC, Row Mappers y SQL Helpers

**Trabajo en curso:** Fase 5 - Servicios de Negocio

- Corrección de errores de compilación
- Implementación de servicios enterprise
- DTOs de autorización
- Scheduled jobs para cleanup y notificaciones
- Tests críticos

---

## 📚 Referencias

- Convenciones de documentación: `../../CLAUDE.md`
- Hitos completados: `../hitos/`
- Proyecto principal: `../../README.md`
