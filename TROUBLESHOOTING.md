# Troubleshooting

Este documento contiene soluciones a problemas comunes encontrados durante el desarrollo y testing del proyecto.

## Tabla de Contenidos

- [Tests de Integración con Testcontainers](#tests-de-integración-con-testcontainers)
  - [Error: "client version 1.32 is too old" con Docker 29.0+](#error-client-version-132-is-too-old-con-docker-290)

---

## Tests de Integración con Testcontainers

### Error: "client version 1.32 is too old" con Docker 29.0+

#### Síntoma

Al ejecutar tests de integración que usan Testcontainers, aparece el siguiente error:

```
o.t.d.DockerClientProviderStrategy : Could not find a valid Docker environment.
Attempted configurations were:
    UnixSocketClientProviderStrategy: failed with exception BadRequestException
    (Status 400: {"message":"client version 1.32 is too old.
    Minimum supported API version is 1.44, please upgrade your client to a newer version"})
```

#### Causa

Docker 29.0.0 incrementó el requisito mínimo de API version a 1.44, pero la biblioteca `docker-java` (usada por Testcontainers) utiliza por defecto la versión 1.32 cuando no se especifica explícitamente.

**Referencia:** https://github.com/testcontainers/testcontainers-java/issues/11212

#### Solución

**Paso 1:** Crear el archivo `~/.docker-java.properties` en el home del usuario:

```properties
# Docker Java API configuration
# Force modern API version for Docker 29.0.0+
api.version=1.45
```

**Paso 2:** Verificar la configuración del POM (ya está configurado en este proyecto):

```xml
<!-- En la sección <properties> -->
<argLine>-Djava.security.egd=file:/dev/./urandom -Xmx1G -Dapi.version=1.45</argLine>

<!-- En maven-surefire-plugin -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <api.version>1.45</api.version>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

**Paso 3:** Ejecutar los tests:

```bash
./mvnw clean test
```

#### Verificación

Para verificar que la solución funciona, ejecuta los tests de integración:

```bash
# Test específico
./mvnw test -Dtest=AccountResourceIT#testUpdateLocaleToSpanish

# Todos los tests de integración
./mvnw verify
```

#### Notas Adicionales

- **NO es necesario** actualizar las versiones de `testcontainers` o `docker-java` en el POM
- El archivo `~/.docker-java.properties` es la solución más limpia y portable
- La propiedad del sistema en el POM es redundante pero útil como respaldo
- El archivo `~/.testcontainers.properties` se crea automáticamente y no interfiere con la solución

#### Versiones Afectadas

- **Docker:** 29.0.0 y superiores
- **Testcontainers:** Todas las versiones (hasta la resolución oficial del issue)
- **docker-java:** Versiones que no especifican explícitamente la API version

#### Alternativas (No Recomendadas)

Si no puedes crear el archivo `~/.docker-java.properties`:

1. **Downgrade de Docker:** Instalar Docker 28.x en lugar de 29.x
2. **Variable de entorno:** `export api.version=1.45` (solo funciona para la sesión actual)
3. **Configuración programática:** Crear un `@TestConfiguration` que configure docker-java (más complejo)

---

## Agregar Nuevos Problemas

Si encuentras un problema que requiere investigación para resolverse, documéntalo aquí siguiendo este formato:

```markdown
### [Título descriptivo del problema]

#### Síntoma

[Descripción del error o comportamiento inesperado]

#### Causa

[Explicación de por qué ocurre el problema]

#### Solución

[Pasos específicos para resolver el problema]

#### Verificación

[Cómo confirmar que el problema está resuelto]
```
