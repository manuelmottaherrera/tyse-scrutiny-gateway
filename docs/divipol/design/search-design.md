# Diseño del Buscador - Módulo Divipol

> **Estado:** ✅ Implementado
> **Última actualización:** 2025-12-05

---

## 1. Resumen Ejecutivo

El buscador del módulo Divipol permitirá a los usuarios buscar registros de la división política electoral de Colombia de dos formas:

1. **Búsqueda con filtros aplicados:** Busca dentro del contexto de los filtros activos (departamento, municipio, zona)
2. **Búsqueda sin filtros:** Búsqueda global en toda la base de datos

---

## 2. Modos de Búsqueda

### 2.1 Búsqueda CON Filtros Aplicados

**Comportamiento:**

- Si el usuario tiene filtros activos (departamento, municipio y/o zona seleccionados), el buscador opera sobre ese subset de datos
- La búsqueda se realiza en el nivel inmediatamente inferior al último filtro aplicado:

| Filtros Activos                 | Busca en                    |
| ------------------------------- | --------------------------- |
| Solo Departamento               | Municipios del departamento |
| Departamento + Municipio        | Zonas del municipio         |
| Departamento + Municipio + Zona | Puestos de la zona          |

#### Decisión: ¿Frontend o Backend?

**Opción A: Búsqueda en Frontend** (datos ya cargados)

- ✅ Más rápido (sin latencia de red)
- ✅ Funciona offline
- ❌ Limitado a datos ya cargados en la tabla
- ❌ Si el usuario necesita buscar algo que no está visible, no lo encontrará

**Opción B: Búsqueda en Backend** (siempre)

- ✅ Siempre busca en toda la información del contexto (todos los municipios del depto, todas las zonas del mpio, etc.)
- ✅ Comportamiento consistente
- ✅ Resultados completos
- ❌ Requiere llamada al servidor
- ❌ Pequeña latencia

**Decisión tomada: Siempre buscar en Backend**

Razón: El usuario podría necesitar buscar información que no está actualmente cargada/visible en la tabla del frontend. Para garantizar resultados completos y un comportamiento predecible, todas las búsquedas (con o sin filtros) se realizarán en el backend.

El endpoint de búsqueda recibirá los filtros activos como parámetros opcionales:

- `codDepto` - Si hay departamento seleccionado
- `codMpio` - Si hay municipio seleccionado
- `codZona` - Si hay zona seleccionada

### 2.2 Búsqueda SIN Filtros

**Comportamiento:**

- Cuando no hay filtros aplicados, se activa la búsqueda global
- Requiere llamada al backend con endpoint de búsqueda
- El usuario puede elegir entre dos modos mediante un **switch**

#### 2.2.1 Modos de Búsqueda Global

| Modo             | Por defecto | Descripción                               |
| ---------------- | ----------- | ----------------------------------------- |
| **Nombre**       | ✅ Sí       | Búsqueda parcial por nombre en toda la BD |
| **Cod. Divipol** | No          | Búsqueda exacta por código de 9 dígitos   |

#### 2.2.2 Modo: Búsqueda por Nombre (default)

- Input de texto libre
- Busca coincidencias parciales en `nomdepto`, `nommipio`, `nompuesto`
- Insensible a mayúsculas/minúsculas
- Resultados paginados desde el backend
- **Resultados mezclados:** Departamentos, municipios, zonas y puestos que cumplan el criterio
- **Ordenamiento:** Por código divipol ascendente

#### 2.2.3 Modo: Búsqueda por Código Divipol

- Al activar este modo, el input se transforma en un campo de código formateado
- Se muestra un código inicial de **9 ceros**: `000000000`
- El cursor se posiciona automáticamente en el **primer carácter**
- El usuario va reemplazando los ceros con los dígitos del código que busca

**Formato del código:**

```
[DD][MMM][ZZ][PP]
 │   │    │   └── Puesto (2 dígitos)
 │   │    └────── Zona (2 dígitos)
 │   └─────────── Municipio (3 dígitos)
 └─────────────── Departamento (2 dígitos)

Ejemplo: 050010101 = Antioquia > Medellín > Zona 01 > Puesto 01
```

**Disparadores de búsqueda:**

- Presionar **Enter**
- Clic en **botón de buscar**
- NO se busca automáticamente mientras escribe

**Funcionalidades del input:**

- ✅ Permite **pegar desde el portapapeles** (el usuario puede copiar códigos de otras fuentes)
- ✅ Permite **editar cualquier dígito** (no solo al final)
- ✅ Reemplaza el carácter donde está el cursor y avanza al siguiente

**Lógica de búsqueda por código:**

- Los códigos se "completan" con ceros a la derecha
- Si busco `010000000` (código del departamento Antioquia):
  1. Muestra primero **Antioquia** (coincidencia exacta del departamento)
  2. Luego muestra todos los registros que **inicien con `01`** (municipios, zonas, puestos de Antioquia)
  3. Ordenados por código divipol ascendente

**Si no hay resultados:**

- Mostrar mensaje informativo: "No se encontraron resultados para el código [X]"

---

## 3. Diseño de UI/UX

### 3.1 Componente de Búsqueda

```
┌─────────────────────────────────────────────────────────────┐
│  🔍  [                    Buscar...                    ]    │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Estado: Con Filtros Aplicados (Advertencia)

Cuando el usuario tiene filtros activos y hace clic/focus en el buscador, aparece una advertencia:

```
┌─────────────────────────────────────────────────────────────┐
│   [ Nombre ◉ ]  [ ○ Cod.Divipol ]                          │
│                                                             │
│  🔍  [                    Buscar...                    ]    │
│      ┌──────────────────────────────────────────────────┐  │
│      │ ⚠️ Al buscar se borrarán los filtros activos    │  │
│      └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**Especificaciones del indicador:**

- **Posición:** Inmediatamente debajo del input de búsqueda
- **Color:** Color de alerta (amarillo/naranja) para ser llamativo
- **Texto:** "Al buscar se borrarán los filtros activos"
- **Comportamiento:**
  - Aparece al hacer focus en el input cuando hay filtros activos
  - Desaparece cuando el usuario ejecuta la búsqueda
  - Desaparece si el usuario hace clic fuera del input (blur)
- **NO tiene botón X:** El usuario simplemente ejecuta la búsqueda o cancela

### 3.3 Estado: Sin Filtros

Sin filtros aplicados, aparece el **switch** de modo de búsqueda:

#### 3.3.1 Modo Nombre (default)

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   [ Nombre ◉ ]  [ ○ Cod.Divipol ]                          │
│                                                             │
│  🔍  [          Buscar por nombre...                   ]    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 3.3.2 Modo Código Divipol

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   [ ○ Nombre ]  [ Cod.Divipol ◉ ]                          │
│                                                             │
│  🔍  [ █0 0 0 0 0 0 0 0 0 ]  [Buscar]                       │
│       ▲                                                     │
│       └── Cursor tipo terminal (cuadrado con el carácter)  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Estilo del cursor:**

- **Tipo terminal/consola:** Cuadrado sólido que contiene el carácter
- Esto da claridad visual de cuál carácter será reemplazado
- El cursor se puede mover con las flechas del teclado

**Comportamiento del input de código:**

- Se muestra `000000000` como valor inicial editable
- Cursor inicia en posición 0 (primer dígito)
- Solo acepta dígitos numéricos (0-9)
- Al escribir: reemplaza el carácter bajo el cursor y avanza
- Máximo 9 caracteres (fijo)
- Permite pegar códigos desde el portapapeles

**Ejemplo de interacción:**

```
Estado inicial:    [█0][0][0][0][0][0][0][0][0]  cursor en pos 0
Usuario digita 0:  [0][█0][0][0][0][0][0][0][0]  cursor en pos 1
Usuario digita 5:  [0][5][█0][0][0][0][0][0][0]  cursor en pos 2
Usuario digita 0:  [0][5][0][█0][0][0][0][0][0]  cursor en pos 3
Usuario digita 0:  [0][5][0][0][█0][0][0][0][0]  cursor en pos 4
Usuario digita 1:  [0][5][0][0][1][█0][0][0][0]  cursor en pos 5
...
Usuario presiona Enter o clic en [Buscar] → Se ejecuta la búsqueda
```

**Edición en medio del código:**

```
Código actual:     [0][5][0][0][1][0][1][0][1]
Usuario clic en pos 4:  [0][5][0][0][█1][0][1][0][1]
Usuario digita 2:  [0][5][0][0][2][█0][1][0][1]  ← Se reemplazó el 1 por 2
```

---

## 4. Campos Buscables

### 4.1 Código Divipol

| Campo               | Formato   | Ejemplo     | Tipo de Búsqueda |
| ------------------- | --------- | ----------- | ---------------- |
| Código completo     | 9 dígitos | `050010101` | Exacta           |
| Código departamento | 2 dígitos | `05`        | Exacta           |
| Código municipio    | 5 dígitos | `05001`     | Exacta           |
| Código zona         | 7 dígitos | `0500101`   | Exacta           |
| Código puesto       | 9 dígitos | `050010101` | Exacta           |

**Comportamiento:** Si el usuario ingresa un código numérico, se busca coincidencia exacta.

### 4.2 Nombres

| Campo       | Ejemplo              | Tipo de Búsqueda |
| ----------- | -------------------- | ---------------- |
| `nomdepto`  | "ANTIOQUIA"          | Full-text search |
| `nommipio`  | "MEDELLÍN"           | Full-text search |
| `nompuesto` | "I.E. JORGE ROBLEDO" | Full-text search |

**Comportamiento:** Full-text search de PostgreSQL con idioma español.

**Ventajas de Full-text search:**

- Tolerante a orden de palabras: "robledo jorge" encuentra "JORGE ROBLEDO"
- Stemming: "colegios" encuentra "colegio"
- Ignora palabras comunes (stopwords): "el", "la", "de"
- Ranking por relevancia
- Más rápido con índices GIN

---

## 5. Paginación

### 5.1 Especificaciones

- **Tamaño de página:** 20 registros por defecto
- **Controles:**
  - Botones Anterior/Siguiente
  - Selector de página
  - Indicador "Mostrando X-Y de Z resultados"

### 5.2 Implementación

**Backend:**

- Parámetros: `page`, `size`, `sort`
- Respuesta incluye: `totalElements`, `totalPages`, `currentPage`

**Frontend:**

- Componente de paginación reutilizable
- Estado en Redux para página actual y tamaño

---

## 6. Autocompletado / Sugerencias

### 6.1 Comportamiento

- Mientras el usuario escribe, se muestran sugerencias
- **Debounce:** 300ms para evitar llamadas excesivas al backend
- **Mínimo de caracteres:** 3 caracteres para activar la búsqueda
- **Máximo de sugerencias:** 5 sugerencias visibles

### 6.2 Configuración Definida

| Parámetro          | Valor                   |
| ------------------ | ----------------------- |
| Debounce time      | 300ms                   |
| Mínimo caracteres  | 3                       |
| Límite sugerencias | 5                       |
| Cache frontend     | Opcional, mejora futura |

---

## 7. Arquitectura Técnica

### 7.1 Frontend

**Nuevos componentes:**

- `DivipolSearch.tsx` - Input de búsqueda principal
- `DivipolSearchResults.tsx` - Lista de resultados/sugerencias
- `FilterIndicator.tsx` - Indicador "Con filtros aplicados"

**Modificaciones al reducer:**

```typescript
// Nuevos campos en DivipolState
{
  // ... campos existentes ...
  searchTerm: string,
  searchMode: 'with-filters' | 'global',
  searchResults: DivipolSearchResult[],
  searchLoading: boolean,
  searchPagination: {
    page: number,
    size: number,
    totalElements: number,
    totalPages: number
  }
}
```

### 7.2 Backend (Microservicio)

**Nuevo endpoint:**

```
GET /api/divipol/search
```

**Parámetros:**
| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `q` | string | Sí | Término de búsqueda |
| `codDepto` | integer | No | Filtro por departamento |
| `codMpio` | integer | No | Filtro por municipio |
| `codZona` | integer | No | Filtro por zona |
| `page` | integer | No | Número de página (default: 0) |
| `size` | integer | No | Tamaño de página (default: 20) |

**Respuesta:**

```json
{
  "content": [
    {
      "tipo": "DEPARTAMENTO|MUNICIPIO|ZONA|PUESTO",
      "codigo": "050010101",
      "nombre": "I.E. JORGE ROBLEDO",
      "nombreCompleto": "ANTIOQUIA > MEDELLÍN > ZONA 01 > I.E. JORGE ROBLEDO",
      "codDepto": 5,
      "codMpio": 1,
      "codZona": 1,
      "codPuesto": "01",
      "potencialTotal": 12345,
      "mesas": 15
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

### 7.3 Base de Datos

**Columna de búsqueda full-text:**

```sql
-- Agregar columna tsvector para búsqueda combinada
ALTER TABLE divipol ADD COLUMN search_vector tsvector;

-- Poblar la columna con datos de todos los campos de nombre
UPDATE divipol SET search_vector =
  to_tsvector('spanish', COALESCE(nomdepto, '') || ' ' ||
                         COALESCE(nommipio, '') || ' ' ||
                         COALESCE(nompuesto, ''));

-- Trigger para mantener actualizado (si se insertan/actualizan registros)
CREATE OR REPLACE FUNCTION divipol_search_trigger() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('spanish',
    COALESCE(NEW.nomdepto, '') || ' ' ||
    COALESCE(NEW.nommipio, '') || ' ' ||
    COALESCE(NEW.nompuesto, ''));
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER divipol_search_update
  BEFORE INSERT OR UPDATE ON divipol
  FOR EACH ROW EXECUTE FUNCTION divipol_search_trigger();
```

**Índices requeridos:**

```sql
-- Para búsqueda por código (exacta) - prefijos de código
CREATE INDEX idx_divipol_codigo ON divipol (coddepto, codmipio, codzona, codpuesto);

-- Para búsqueda full-text (por nombre)
CREATE INDEX idx_divipol_search ON divipol USING gin (search_vector);
```

**Query de búsqueda full-text:**

```sql
-- Ejemplo de búsqueda por nombre
SELECT * FROM divipol
WHERE search_vector @@ plainto_tsquery('spanish', 'jorge robledo')
ORDER BY ts_rank(search_vector, plainto_tsquery('spanish', 'jorge robledo')) DESC;
```

---

## 8. Comportamiento de Resultados de Búsqueda

### 8.1 Visualización de Resultados

Cuando se ejecuta una búsqueda:

1. **Los resultados reemplazan la tabla actual**

   - La tabla de datos normales desaparece
   - Se muestra la tabla de resultados de búsqueda

2. **Los filtros se desactivan**

   - Si había filtros aplicados, se borran
   - Los dropdowns de filtros se deshabilitan (grayed out)
   - No se puede filtrar sobre los resultados de búsqueda

3. **Aparece botón "Limpiar búsqueda"**
   - Visible mientras haya una búsqueda activa
   - Al hacer clic: limpia la búsqueda y vuelve a la vista normal

### 8.2 Mockup: Estado de Búsqueda Activa

```
┌─────────────────────────────────────────────────────────────┐
│  DIVIPOL - División Política                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   [ Nombre ◉ ]  [ ○ Cod.Divipol ]                          │
│                                                             │
│  🔍  [ medellín                    ]  [Buscar]              │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🔎 Resultados para "medellín"    [Limpiar búsqueda] │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ [Depto ▼] [Municipio ▼] [Zona ▼]  ← DESHABILITADOS │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Cod.Divipol │ Tipo  │ Nombre           │ Potencial │   │
│  ├─────────────┼───────┼──────────────────┼───────────┤   │
│  │ 05001       │ MPIO  │ MEDELLÍN         │ 1.234.567 │   │
│  │ 050010101   │PUESTO │ I.E. MEDELLÍN... │    12.345 │   │
│  │ ...         │ ...   │ ...              │ ...       │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Mostrando 1-20 de 150 resultados    [<] [1] [2] [3] [>]   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 8.3 Interacción con Resultados

- **Los resultados NO son seleccionables/clickeables** (por ahora)
- Más adelante se implementará una vista de detalle solo para puestos
- El usuario solo puede:
  - Navegar páginas de resultados
  - Limpiar la búsqueda para volver a la vista normal
  - Hacer una nueva búsqueda

### 8.4 Estadísticas Durante Búsqueda

- **Las tarjetas de estadísticas se OCULTAN** durante una búsqueda activa
- Razón: Los resultados de búsqueda mezclan diferentes niveles (deptos, mpios, zonas, puestos), por lo que las estadísticas agregadas no tienen sentido
- Las estadísticas reaparecen al limpiar la búsqueda

### 8.5 Exportación Durante Búsqueda

- **El botón de exportar se MANTIENE** durante búsqueda
- Exporta la información que esté visible en la tabla (sea filtrada o resultado de búsqueda)
- Formatos de exportación (futura implementación):
  - CSV
  - PDF

### 8.6 Volver a la Vista Normal

Al hacer clic en "Limpiar búsqueda":

1. Se limpia el input de búsqueda
2. Se oculta el banner de resultados
3. Se habilitan los filtros
4. Se muestra la tabla normal (todos los departamentos)
5. Se muestran las tarjetas de estadísticas generales

---

## 9. Flujos de Usuario

### 9.1 Flujo: Búsqueda por Nombre

```
1. Usuario está en la página de Divipol (con o sin filtros)
2. Switch está en [Nombre ◉] (por defecto)
3. Usuario escribe "medellín" en el buscador (mín. 3 caracteres)
4. Después de 300ms (debounce), se muestran sugerencias
5. Usuario presiona Enter o clic en [Buscar]
6. Los filtros se desactivan y se borran
7. Los resultados reemplazan la tabla
8. Aparece banner "Resultados para 'medellín'" con botón [Limpiar búsqueda]
9. Usuario navega páginas de resultados
10. Usuario hace clic en [Limpiar búsqueda] para volver a la vista normal
```

### 9.2 Flujo: Búsqueda por Código Divipol

```
1. Usuario está en la página de Divipol
2. Usuario hace clic en switch [Cod.Divipol]
3. El input cambia a formato código: [█0][0][0][0][0][0][0][0][0]
4. Cursor se posiciona en el primer dígito
5. Usuario digita o pega un código (ej: "050010101")
6. Usuario presiona Enter o clic en [Buscar]
7. Los filtros se desactivan (si había)
8. Se busca el código en el backend
9. Se muestran resultados (código exacto + códigos que inician igual)
10. Aparece banner con botón [Limpiar búsqueda]
11. Usuario hace clic en [Limpiar búsqueda] para volver
```

### 9.3 Flujo: Volver a Vista Normal

```
1. Usuario tiene búsqueda activa (resultados visibles)
2. Usuario hace clic en [Limpiar búsqueda]
3. Se limpia el input de búsqueda
4. Se oculta el banner de resultados
5. Se habilitan los filtros (vacíos)
6. Se muestra la tabla con todos los departamentos
7. Se cargan las estadísticas generales
```

---

## 10. Requisitos Técnicos Adicionales

### 10.1 Internacionalización (i18n)

Todos los textos del buscador deben estar traducidos en los idiomas soportados (es, en).

**Claves de traducción requeridas:**

```json
{
  "divipol": {
    "search": {
      "placeholder": "Buscar...",
      "placeholderByName": "Buscar por nombre...",
      "placeholderByCode": "Código Divipol",
      "switchName": "Nombre",
      "switchCode": "Cod.Divipol",
      "buttonSearch": "Buscar",
      "buttonClear": "Limpiar búsqueda",
      "resultsFor": "Resultados para",
      "noResults": "No se encontraron resultados",
      "noResultsCode": "No se encontraron resultados para el código",
      "warningFiltersWillClear": "Al buscar se borrarán los filtros activos",
      "showingResults": "Mostrando {{from}}-{{to}} de {{total}} resultados"
    }
  }
}
```

**Archivos a modificar:**

- `src/main/webapp/i18n/es/divipol.json`
- `src/main/webapp/i18n/en/divipol.json`

### 10.2 Soporte de Temas (Dark/Light)

Los componentes del buscador deben ser compatibles con los temas dark y light del sistema.

**Consideraciones:**

- Usar variables de color definidas en `_color-variables.scss`
- NO usar colores hardcodeados
- El indicador de advertencia (amarillo/naranja) debe ser visible en ambos temas
- El cursor tipo terminal del input de código debe contrastar en ambos temas
- Los estados hover/focus deben ser visibles en ambos temas

**Variables sugeridas:**

```scss
// En componentes SCSS del buscador
@import '../../color-variables';

.divipol-search {
  background: $color-white;
  color: $color-text-primary;
  border: 1px solid $color-gray-light;

  &__warning {
    background: $color-warning;
    color: $color-text-primary;
  }

  &__code-cursor {
    background: $color-text-primary;
    color: $color-white;
  }
}

// Dark theme overrides via data-theme attribute
[data-theme='dark'] {
  .divipol-search {
    // Variables automáticamente ajustadas por el sistema de temas
  }
}
```

### 10.3 Testing

#### 10.3.1 Tests Unitarios (Frontend - Jest)

**Componentes a testear:**

- `DivipolSearch.tsx` - Input principal y switch de modo
- `DivipolCodeInput.tsx` - Input de código con cursor especial
- `DivipolSearchResults.tsx` - Visualización de resultados
- Reducer: acciones de búsqueda, paginación, limpiar

**Casos de prueba sugeridos:**

```typescript
// DivipolSearch.spec.tsx
describe('DivipolSearch', () => {
  it('should render with Name mode by default');
  it('should switch to Code mode when clicking switch');
  it('should show warning when filters are active');
  it('should trigger search on Enter key');
  it('should trigger search on button click');
  it('should respect minimum character limit (3)');
  it('should debounce input (300ms)');
  it('should clear search and restore filters on clear button');
});

// DivipolCodeInput.spec.tsx
describe('DivipolCodeInput', () => {
  it('should initialize with 000000000');
  it('should replace character at cursor position');
  it('should advance cursor after typing');
  it('should allow editing middle digits');
  it('should handle paste from clipboard');
  it('should only accept numeric input');
  it('should have max length of 9');
});

// divipol.reducer.spec.ts (search actions)
describe('Divipol Search Reducer', () => {
  it('should handle searchByName action');
  it('should handle searchByCode action');
  it('should handle clearSearch action');
  it('should update pagination');
  it('should set loading state during search');
  it('should handle search errors');
});
```

#### 10.3.2 Tests de Integración (Backend - JUnit)

**Clases a testear:**

- `DivipolSearchRepository` - Queries de búsqueda full-text
- `DivipolService` - Lógica de búsqueda y paginación
- `DivipolResource` - Endpoint REST de búsqueda

**Casos de prueba sugeridos:**

```java
// DivipolSearchRepositoryIT.java
@Test
void shouldSearchByNameWithFullText();

@Test
void shouldSearchByCodePrefix();

@Test
void shouldReturnPaginatedResults();

@Test
void shouldOrderResultsByCode();

@Test
void shouldHandleAccentsAndCase();

@Test
void shouldFindByPartialWords();

// DivipolResourceIT.java
@Test
void shouldReturnSearchResults();

@Test
void shouldValidateMinimumQueryLength();

@Test
void shouldReturnEmptyForNoMatches();

@Test
void shouldRespectPaginationParams();

```

#### 10.3.3 Tests E2E (Cypress)

**Archivo:** `src/test/javascript/cypress/e2e/divipol/divipol-search.cy.ts`

**Escenarios a testear:**

```typescript
describe('Divipol Search', () => {
  describe('Search by Name', () => {
    it('should search and display results');
    it('should show suggestions while typing');
    it('should clear filters when searching');
    it('should paginate results');
    it('should return to normal view on clear');
  });

  describe('Search by Code', () => {
    it('should switch to code mode');
    it('should show code input with zeros');
    it('should search on Enter');
    it('should handle paste from clipboard');
    it('should show related codes when searching prefix');
  });

  describe('UI States', () => {
    it('should show warning when filters active');
    it('should hide stats during search');
    it('should keep export button during search');
    it('should disable filters during search');
  });

  describe('Themes', () => {
    it('should display correctly in light theme');
    it('should display correctly in dark theme');
  });
});
```

### 10.4 Documentación

#### 10.4.1 Documentación Técnica (este documento)

**Ubicación:** `docs/divipol/design/search-design.md`

Este documento contiene:

- Especificación funcional completa
- Diseños de UI/UX
- Arquitectura técnica
- Decisiones de diseño

#### 10.4.2 Guía de Usuario

**Ubicación:** `docs/divipol/user-manual/search-user-guide.md`

Debe contener:

- Descripción general del buscador
- Instrucciones de uso paso a paso
- Capturas de pantalla (agregar post-implementación)
- Ejemplos de búsqueda
- Preguntas frecuentes

**Tareas de documentación:**

- [ ] Completar guía de usuario con capturas de pantalla
- [x] Agregar ejemplos reales de búsqueda
- [x] Documentar casos de uso comunes

#### 10.4.3 Estructura de Documentación

```
docs/divipol/
├── README.md                           # Índice del módulo
├── design/
│   └── search-design.md                # Este documento
└── user-manual/
    └── search-user-guide.md            # Guía de usuario
```

---

## 11. Resumen de Decisiones

Todas las preguntas han sido definidas:

| Pregunta                      | Decisión                                            |
| ----------------------------- | --------------------------------------------------- |
| Búsqueda sin filtros          | Switch entre Nombre y Cod.Divipol                   |
| Búsqueda con filtros          | Siempre en backend                                  |
| Resultados por nombre         | Mezcla deptos/mpios/zonas/puestos, orden por código |
| Disparador búsqueda código    | Enter o botón "Buscar"                              |
| Edición código en medio       | Sí, cursor tipo terminal                            |
| Código no existe              | Mensaje + códigos que inicien igual                 |
| Switch visible                | Siempre                                             |
| Pegar portapapeles            | Sí                                                  |
| Debounce autocompletado       | 300ms                                               |
| Mínimo caracteres             | 3                                                   |
| Tamaño de página              | 20                                                  |
| Tipo búsqueda nombres         | Full-text search PostgreSQL (español)               |
| Resultados seleccionables     | No (vista detalle futura)                           |
| Visualización resultados      | Reemplazan tabla, desactivan filtros                |
| Volver a vista normal         | Botón "Limpiar búsqueda"                            |
| Límite sugerencias            | 5                                                   |
| Estadísticas durante búsqueda | Se ocultan                                          |
| Exportar durante búsqueda     | Sí, exporta lo visible en tabla (CSV/PDF futuro)    |

---

## 11. Historial de Cambios

| Fecha      | Cambio                                                                                | Autor            |
| ---------- | ------------------------------------------------------------------------------------- | ---------------- |
| 2025-12-05 | Documento inicial                                                                     | Claude + Usuario |
| 2025-12-05 | Definición de búsqueda sin filtros: switch Nombre/Código Divipol                      | Claude + Usuario |
| 2025-12-05 | Detalles de búsqueda por código: cursor terminal, Enter/botón, portapapeles           | Claude + Usuario |
| 2025-12-05 | Resultados por nombre: mezcla de niveles, orden por código                            | Claude + Usuario |
| 2025-12-05 | Decisión: búsqueda siempre en backend (con y sin filtros)                             | Claude + Usuario |
| 2025-12-05 | Configuración: debounce 300ms, mín 3 chars, página 20                                 | Claude + Usuario |
| 2025-12-05 | Decisión: Full-text search de PostgreSQL (idioma español)                             | Claude + Usuario |
| 2025-12-05 | UI de resultados: reemplazan tabla, desactivan filtros, botón "Limpiar búsqueda"      | Claude + Usuario |
| 2025-12-05 | Resultados no seleccionables (vista detalle de puestos será futura mejora)            | Claude + Usuario |
| 2025-12-05 | Límite sugerencias: 5, Stats se ocultan durante búsqueda                              | Claude + Usuario |
| 2025-12-05 | Exportar: se mantiene durante búsqueda, exporta lo visible (CSV/PDF futuro)           | Claude + Usuario |
| 2025-12-05 | Requisitos técnicos: i18n, temas dark/light, tests (Jest, JUnit, Cypress)             | Claude + Usuario |
| 2025-12-05 | Reorganización: movido a docs/divipol/design/, creada guía de usuario                 | Claude + Usuario |
| 2025-12-05 | **Diseño completo** - Listo para implementación                                       | Claude + Usuario |
| 2025-12-05 | **Implementación completa** - Backend (20 tests), Frontend (31 tests), E2E (22 tests) | Claude + Usuario |
