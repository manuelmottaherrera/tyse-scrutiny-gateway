# Guía de Usuario - Buscador Divipol

> **Estado:** ✅ Implementado
> **Documento de diseño:** [search-design.md](../design/search-design.md)

---

## Descripción General

El buscador de Divipol permite encontrar rápidamente cualquier registro de la división política electoral de Colombia: departamentos, municipios, zonas y puestos de votación.

### Características Principales

- **Dos modos de búsqueda:** Por nombre (texto libre) y por código Divipol (9 dígitos)
- **Sugerencias en tiempo real:** Al escribir en modo nombre
- **Persistencia en URL:** Los parámetros de búsqueda se guardan en la URL para compartir
- **Paginación:** Resultados paginados con 20 items por página
- **Soporte de temas:** Compatible con modo claro, oscuro y sistema

---

## Modos de Búsqueda

### Búsqueda por Nombre

<!-- TODO: Agregar capturas de pantalla cuando esté implementado -->

1. Asegúrese de que el switch esté en **"Nombre"** (opción por defecto)
2. Escriba al menos 3 caracteres en el campo de búsqueda
3. Aparecerán sugerencias mientras escribe
4. Presione **Enter** o haga clic en **"Buscar"**
5. Los resultados reemplazarán la tabla actual

**Ejemplos de búsqueda:**

- `medellín` - Encuentra el municipio y puestos relacionados
- `colegio` - Encuentra puestos de votación en colegios
- `antioquia` - Encuentra el departamento y sus subdivisiones

### Búsqueda por Código Divipol

<!-- TODO: Agregar capturas de pantalla cuando esté implementado -->

1. Haga clic en el switch **"Cod.Divipol"**
2. El campo mostrará `000000000` con un cursor especial
3. Digite el código o **péguelo desde el portapapeles**
4. Presione **Enter** o haga clic en **"Buscar"**

**Formato del código:**

```
[DD][MMM][ZZ][PP]
 │   │    │   └── Puesto (2 dígitos)
 │   │    └────── Zona (2 dígitos)
 │   └─────────── Municipio (3 dígitos)
 └─────────────── Departamento (2 dígitos)
```

**Ejemplos:**

- `050000000` - Departamento de Antioquia
- `050010000` - Municipio de Medellín
- `050010100` - Zona 01 de Medellín
- `050010101` - Puesto 01 de la Zona 01 de Medellín

---

## Resultados de Búsqueda

- Los resultados muestran una mezcla de departamentos, municipios, zonas y puestos
- Están ordenados por código Divipol
- Se muestran 20 resultados por página
- Use los controles de paginación para navegar

### Columnas de Resultados

| Columna      | Descripción                 |
| ------------ | --------------------------- |
| Cod. Divipol | Código único del registro   |
| Tipo         | DEPTO, MPIO, ZONA o PUESTO  |
| Nombre       | Nombre del registro         |
| Potencial    | Potencial electoral total   |
| Mesas        | Número de mesas de votación |

---

## Comportamiento con Filtros

- Si tiene filtros aplicados (departamento, municipio, zona), verá una advertencia
- Al ejecutar la búsqueda, **los filtros se borrarán automáticamente**
- Los filtros se desactivan mientras hay una búsqueda activa

---

## Volver a la Vista Normal

Para regresar a la vista normal con filtros:

1. Haga clic en el botón **"Limpiar búsqueda"**
2. Los filtros se habilitarán nuevamente
3. La tabla mostrará todos los departamentos
4. Las estadísticas volverán a aparecer

---

## Exportación

- El botón de exportar permanece activo durante la búsqueda
- Exporta los resultados visibles en la tabla
- Formatos disponibles: CSV, PDF

---

## Preguntas Frecuentes

### ¿Por qué no aparecen las estadísticas durante la búsqueda?

Los resultados de búsqueda mezclan diferentes niveles (departamentos, municipios, etc.), por lo que las estadísticas agregadas no tienen sentido en ese contexto.

### ¿Puedo buscar por dirección?

Actualmente no. La búsqueda funciona por nombre de lugar o código Divipol.

### ¿El buscador encuentra resultados con tildes/sin tildes?

Sí, la búsqueda es tolerante a acentos gracias al motor de búsqueda full-text.

---

**Última actualización:** 2025-12-05
