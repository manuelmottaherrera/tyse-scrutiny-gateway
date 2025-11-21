# Sistema de Traducciones i18n en el Backend

## Visión General

El backend implementa un sistema de internacionalización (i18n) basado en Spring Boot's MessageSource que permite devolver mensajes de error en el idioma preferido del usuario (español o inglés).

## Flujo de Traducción

```
┌──────────────┐
│   Frontend   │ Envía: X-Locale: es
└──────┬───────┘
       │
       ▼
┌──────────────────────────┐
│  ExceptionTranslator     │
│  1. Lee X-Locale header  │
│  2. Obtiene Locale       │
│  3. Traduce mensaje      │
│  4. Actualiza response   │
└──────┬───────────────────┘
       │
       ▼
┌──────────────┐
│   Response   │ { "detail": "¡La cuenta de correo ya está en uso!" }
└──────────────┘
```

## Componentes Principales

### 1. ExceptionTranslator.java

Ubicación: `src/main/java/com/tyse/scrutiny/gateway/web/rest/errors/ExceptionTranslator.java`

**Responsabilidad:** Interceptar todas las excepciones y traducir los mensajes de error.

**Métodos clave:**

#### getLocaleFromRequest()

```java
private Locale getLocaleFromRequest(ServerWebExchange request) {
  // Prioridad 1: Header X-Locale (preferencia del usuario)
  String xLocale = request.getRequest().getHeaders().getFirst("X-Locale");
  if (xLocale != null && !xLocale.isEmpty()) {
    return Locale.forLanguageTag(xLocale);
  }

  // Prioridad 2: Accept-Language (browser default)
  if (!request.getRequest().getHeaders().getAcceptLanguageAsLocales().isEmpty()) {
    return request.getRequest().getHeaders().getAcceptLanguageAsLocales().get(0);
  }

  // Prioridad 3: Default español
  return new Locale("es");
}

```

#### customizeProblem()

```java
protected ProblemDetailWithCause customizeProblem(ProblemDetailWithCause problem, Throwable err, ServerWebExchange request) {
  if (err instanceof EmailAlreadyUsedException) {
    Locale locale = getLocaleFromRequest(request);
    String translatedDetail = messageSource.getMessage(
      "error.emailexists", // Key en properties
      null, // Parámetros (opcional)
      "Email is already in use!", // Fallback
      locale // Locale del usuario
    );
    problem.setDetail(translatedDetail);
  }
  return problem;
}

```

### 2. MessageSource

**Qué es:** Interface de Spring que carga y resuelve mensajes desde archivos `.properties`.

**Configuración en application.yml:**

```yaml
spring:
  messages:
    basename: i18n/messages
```

**Estructura de archivos:**

```
src/main/resources/i18n/
├── messages.properties       # Fallback
├── messages_es.properties    # Español
└── messages_en.properties    # Inglés
```

### 3. Archivos de Propiedades

**messages_es.properties:**

```properties
error.emailexists=¡La cuenta de correo ya está en uso!
error.userexists=¡El nombre de usuario ya existe!
```

**messages_en.properties:**

```properties
error.emailexists=Email is already in use!
error.userexists=Login name already used!
```

## Cómo Agregar una Nueva Traducción

### Paso 1: Agregar traducciones a properties

**messages_es.properties:**

```properties
error.mierror=¡Mensaje de error en español!
```

**messages_en.properties:**

```properties
error.mierror=Error message in English!
```

### Paso 2: Implementar traducción en ExceptionTranslator

```java
if (err instanceof MiExcepcion) {
    Locale locale = getLocaleFromRequest(request);
    String translatedDetail = messageSource.getMessage(
        "error.mierror",
        null,
        "Default message",
        locale
    );
    problem.setDetail(translatedDetail);
}
```

### Paso 3: Agregar traducciones a test resources

**⚠️ IMPORTANTE:** Los archivos en `src/test/resources/i18n/` sobrescriben los de `src/main/resources/` durante tests.

Agregar las mismas traducciones en:

- `src/test/resources/i18n/messages_es.properties`
- `src/test/resources/i18n/messages_en.properties`

## Traducción con Parámetros

**Properties:**

```properties
error.authority.inactive=La autoridad "{0}" está inactiva
```

**Código:**

```java
String translatedDetail = messageSource.getMessage(
  "error.authority.inactive",
  new Object[] { authorityCode }, // {0} = authorityCode
  locale
);
// Resultado: "La autoridad "ROLE_ADMIN" está inactiva"

```

## Headers HTTP

### X-Locale

Enviado por el frontend en cada request:

```
X-Locale: es
```

Configurado en `axios-interceptor.ts`:

```typescript
config.headers['X-Locale'] = Storage.session.get('locale', 'es');
```

### Accept-Language

Fallback si no existe X-Locale:

```
Accept-Language: es-ES,es;q=0.9,en;q=0.8
```

## Response JSON

```json
{
  "type": "https://www.jhipster.tech/problem/email-already-used",
  "title": "Email is already in use!",
  "status": 400,
  "message": "error.emailexists",
  "detail": "¡La cuenta de correo ya está en uso!",
  "params": "userManagement"
}
```

**Campos:**

- `message`: Key de traducción (para debugging)
- `detail`: Mensaje traducido según locale del usuario ✅

## Testing

### Estructura de Tests

```java
@Test
void testEmailAlreadyUsedWithSpanishLocale() {
  webTestClient
    .get()
    .uri("/api/exception-translator-test/email-already-used")
    .header("X-Locale", "es") // ← Locale español
    .exchange()
    .expectStatus()
    .isBadRequest()
    .expectBody()
    .jsonPath("$.detail")
    .isEqualTo("¡La cuenta de correo ya está en uso!"); // ← Verificar traducción
}

```

### Ejecutar Tests

```bash
./mvnw test -Dtest=ExceptionTranslatorIT
```

## Troubleshooting

### Problema: Traducción siempre en inglés

**Diagnóstico:**

1. Verificar que el header `X-Locale` se envía correctamente
2. Agregar logging:

```java
LOG.debug("X-Locale: {}", xLocale);
LOG.debug("Resolved locale: {}", locale);
```

**Solución:**

- Frontend: Verificar `axios-interceptor.ts`
- Backend: Verificar `getLocaleFromRequest()`

### Problema: Tests fallan pero app funciona

**Causa:** Falta traducción en `src/test/resources/i18n/`

**Solución:**

```bash
# Copiar traducciones a test resources
echo "error.emailexists=¡La cuenta de correo ya está en uso!" >> src/test/resources/i18n/messages_es.properties
```

## Referencias

- ExceptionTranslator.java:424 - `getLocaleFromRequest()`
- ExceptionTranslator.java:138-143 - Traducción de EmailAlreadyUsedException
- application.yml - `spring.messages.basename`
- messages_es.properties - Traducciones en español
- messages_en.properties - Traducciones en inglés
