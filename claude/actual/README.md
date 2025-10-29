# Carpeta de Trabajo Actual

**Fecha de creación:** 2025-10-28 23:44
**Propósito:** Almacenar reportes y contextos del trabajo en curso

---

## 📋 Descripción

Esta carpeta contiene los reportes y contextos de las sesiones de trabajo actuales. Una vez que se complete un hito o se alcance un objetivo importante, los documentos deben ser archivados en la carpeta `hitos/` con un nombre descriptivo.

---

## 🗂️ Estructura de Documentos

### Reportes

Los reportes finales de sesiones importantes deben seguir este formato:

**Formato de nombre:** `YYYYMMDD_HHMM_nombreEnCamelCase.md`

**Ejemplo:** `20251028_2344_analisisPerformance.md`

**Contenido mínimo:**

- Fecha y hora
- Fase/paso del proyecto
- Propósito
- Análisis detallado
- Conclusiones
- Siguiente paso

### Contextos

Los contextos de conversaciones importantes deben seguir este formato:

**Formato de nombre:** `YYYYMMDD_HHMM_contexto_nombreDescriptivo.md`

**Ejemplo:** `20251028_2300_contexto_refactorAutenticacion.md`

**Contenido mínimo:**

- Objetivo de la sesión
- Estado del proyecto
- Plan de trabajo
- Decisiones tomadas
- Referencias importantes

---

## 🔄 Flujo de Trabajo

### 1. Durante el Trabajo

Guarda reportes y contextos en esta carpeta (`claude/actual/`) a medida que avanzas en las tareas.

### 2. Al Completar un Hito

Cuando completes un hito importante:

1. **Crear carpeta de hito:**

   ```bash
   mkdir -p claude/hitos/XX_nombre-del-hito/{subcarpetas}
   ```

2. **Mover documentos:**

   ```bash
   mv claude/actual/*.md claude/hitos/XX_nombre-del-hito/categoria/
   ```

3. **Crear README del hito:**
   Documenta el hito completado con resumen, métricas y logros.

4. **Limpiar carpeta actual:**
   Asegúrate de que `claude/actual/` quede lista para nuevos trabajos.

---

## 📂 Ejemplos de Organización por Hito

```
claude/hitos/
├── 01_sistema-autorizacion-fase1/
│   ├── 00_preparacion/
│   ├── 01_diseno-base-datos/
│   ├── 02_validacion-testing/
│   ├── contexto_migracionSistemaAutorizacion.md
│   └── README.md
├── 02_frontend-dashboard/
│   ├── 00_analisis/
│   ├── 01_componentes/
│   └── README.md
└── 03_integracion-api-externa/
    ├── 00_investigacion/
    ├── 01_implementacion/
    └── README.md
```

---

## ✅ Checklist de Archivo

Antes de archivar un hito, asegúrate de:

- [ ] Todos los reportes tienen fecha y hora
- [ ] Los nombres de archivos son descriptivos
- [ ] Se creó un README del hito con resumen
- [ ] Los documentos están organizados en subcarpetas lógicas
- [ ] El contexto del hito está incluido
- [ ] La carpeta `actual/` quedó limpia

---

## 🎯 Convención de Nombres de Hitos

**Formato:** `XX_nombre-descriptivo-del-hito`

**Ejemplos:**

- `01_sistema-autorizacion-fase1`
- `02_dashboard-modular`
- `03_integracion-kafka`
- `04_refactor-servicios-core`

**Numeración:**

- Usa números secuenciales (01, 02, 03, ...)
- El número indica el orden cronológico de completado
- No reutilices números de hitos anteriores

---

## 📊 Estado Actual

**Hitos completados:** 1

- `01_sistema-autorizacion-fase1` ✅

**Trabajo en curso:** Pendiente de iniciar nuevo hito

---

## 📚 Referencias

- Convenciones de documentación: `../../CLAUDE.md`
- Hitos completados: `../hitos/`
- Proyecto principal: `../../README.md`
