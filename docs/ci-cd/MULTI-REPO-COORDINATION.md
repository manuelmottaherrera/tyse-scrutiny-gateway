# Guía de Coordinación Multi-Repositorio

Este documento explica cómo los dos repositorios separados (Gateway y Divipol) trabajan juntos en el pipeline CI/CD.

## Arquitectura de Repositorios

### ¿Por Qué Dos Repositorios?

Tyse Scrutiny usa una arquitectura **multi-repositorio** donde cada microservicio se desarrolla y versiona de forma independiente:

**Beneficios**:

- ✅ Versionado independiente y ciclos de release
- ✅ Código base más pequeño por repositorio (más fácil de navegar)
- ✅ Pipelines CI independientes (feedback más rápido)
- ✅ Propiedad y responsabilidades claras
- ✅ Flexibilidad para desplegar servicios independientemente

**Compromisos**:

- ⚠️ El despliegue requiere coordinación
- ⚠️ Necesidad de gestionar versiones entre repositorios
- ⚠️ Infraestructura compartida gestionada por Gateway

---

## Responsabilidades de los Repositorios

### Repositorio Gateway

**Ubicación**: `github.com/manuelmottaherrera/tyse-scrutiny-gateway`

**Posee**:

- Aplicación frontend React
- Backend Spring Boot Gateway
- Configuración de enrutamiento del API Gateway
- Autenticación y gestión de usuarios
- **Infraestructura compartida** (Consul, Kafka, Zookeeper)
- **Orquestación de despliegue** para todos los servicios

**CI/CD**:

- Tests: Backend + Frontend + E2E
- Construye: Imagen Docker de Gateway
- Despliega: Ambos servicios Gateway y Divipol

### Repositorio Divipol

**Ubicación**: `github.com/manuelmottaherrera/tyse-scrutiny-micro-divipol`

**Posee**:

- Microservicio Spring Boot
- Datos de división política (18,016 registros)
- Servicios de geolocalización
- Endpoints API para datos geográficos de Colombia

**CI/CD**:

- Tests: Solo backend
- Construye: Imagen Docker de Divipol
- Despliega: **Sin workflow de despliegue** (manejado por Gateway)

---

## Estrategia de Coordinación CI/CD

### Construcción Independiente, Despliegue Coordinado

```
┌──────────────────────────────────────────────────────────────────┐
│                    CI/BUILD INDEPENDIENTE                         │
└──────────────────────────────────────────────────────────────────┘

Repo Gateway                          Repo Divipol
────────────                          ────────────
Push del Desarrollador                Push del Desarrollador
     ↓                                     ↓
CI (15 min)                           CI (10 min)
  - Tests Backend                       - Tests Backend
  - Tests Frontend                      - Quality Gate
  - Tests E2E
     ↓                                     ↓
Build (5 min)                         Build (5 min)
  - JAR Maven                            - JAR Maven
  - Imagen Docker                        - Imagen Docker
     ↓                                     ↓
Push a GHCR                           Push a GHCR
  ghcr.io/.../gateway:develop           ghcr.io/.../divipol:develop

┌──────────────────────────────────────────────────────────────────┐
│                    DESPLIEGUE COORDINADO                          │
└──────────────────────────────────────────────────────────────────┘

            Disparador Manual (desde repo Gateway)
                              ↓
              Descargar AMBAS Imágenes Docker
                   (Gateway + Divipol)
                              ↓
                  Desplegar en el Servidor
                  (Stack Docker Compose)
                              ↓
                  Verificaciones de Salud
                (Verificar ambos servicios)
                              ↓
                       ✅ Éxito
```

---

## Coordinación de Despliegue

### Cómo Funciona

El **repositorio Gateway** contiene el workflow `deploy-staging.yml` que:

1. Acepta dos parámetros:

   - `gateway_tag`: Qué imagen de Gateway desplegar
   - `divipol_tag`: Qué imagen de Divipol desplegar

2. Descarga ambas imágenes desde GitHub Container Registry

3. Actualiza la configuración del despliegue

4. Despliega el stack completo (Gateway + Divipol + Infraestructura)

5. Verifica que ambos servicios estén saludables

### ¿Por Qué Gateway Maneja el Despliegue?

El repositorio Gateway gestiona el despliegue porque:

- Posee la infraestructura compartida (Consul, Kafka)
- Tiene el archivo de orquestación docker-compose
- Necesita asegurar que todos los servicios inicien en el orden correcto
- Proporciona los scripts de despliegue y verificaciones de salud

---

## Trabajando Entre Repositorios

### Escenario 1: Feature en un Solo Repositorio

**Ejemplo**: Agregando un nuevo widget de dashboard a Gateway

```bash
# 1. Trabajar en el repo Gateway
cd tyse-scrutiny-gateway
git checkout -b feature/dashboard-widget

# 2. Desarrollar, probar, commit
git add .
git commit -m "feat(dashboard): add new metrics widget"
git push origin feature/dashboard-widget

# 3. Crear PR a develop
# CI se ejecuta automáticamente

# 4. Después del merge a develop
# Build crea nueva imagen: ghcr.io/.../gateway:develop

# 5. Desplegar
# Ir a Actions → Deploy to Staging → Run workflow
# gateway_tag: develop
# divipol_tag: develop (sin cambios)
```

### Escenario 2: Feature que Abarca Ambos Repositorios

**Ejemplo**: Gateway necesita llamar a un nuevo endpoint de Divipol

#### Paso 1: Desarrollar Endpoint de Divipol

```bash
cd tyse-scrutiny-micro-divipol
git checkout -b feature/new-geo-endpoint

# Implementar endpoint
# Escribir tests
git commit -m "feat(geo): add municipality search endpoint"
git push origin feature/new-geo-endpoint

# Crear PR, mergear a develop
# Build crea: ghcr.io/.../divipol:develop
```

#### Paso 2: Desarrollar Integración en Gateway

```bash
cd tyse-scrutiny-gateway
git checkout -b feature/use-new-geo-endpoint

# Actualizar servicio para llamar al nuevo endpoint de Divipol
# Escribir tests
git commit -m "feat(geo): integrate municipality search"
git push origin feature/use-new-geo-endpoint

# Crear PR, mergear a develop
# Build crea: ghcr.io/.../gateway:develop
```

#### Paso 3: Desplegar Ambos

```bash
# Ambas imágenes están listas
# Desplegar desde el repo Gateway:
# Actions → Deploy to Staging → Run workflow
# gateway_tag: develop
# divipol_tag: develop
```

### Escenario 3: Hotfix en Producción

**Ejemplo**: Bug crítico en Divipol, Gateway está estable

```bash
# 1. Corregir bug en Divipol
cd tyse-scrutiny-micro-divipol
git checkout -b hotfix/critical-bug
# Corregir, probar, commit
git push origin hotfix/critical-bug

# Mergear a main
# Build crea: ghcr.io/.../divipol:main

# 2. Desplegar SOLO actualización de Divipol
cd tyse-scrutiny-gateway
# Actions → Deploy to Staging → Run workflow
# gateway_tag: main (sin cambios)
# divipol_tag: main (actualizado)
```

---

## Gestión de Versiones

### Estrategia de Etiquetado de Imágenes

Ambos repositorios usan la misma estrategia de etiquetado:

| Disparador       | Tags Creados                        | Ejemplo              |
| ---------------- | ----------------------------------- | -------------------- |
| Push a `develop` | `develop`, `develop-sha-abc123`     | Desplegar staging    |
| Push a `main`    | `main`, `main-sha-abc123`, `latest` | Desplegar producción |
| Tag `v1.2.3`     | `v1.2.3`, `1.2`, `latest`           | Release semántico    |

### Rastreo de Versiones Desplegadas

#### Opción 1: Archivo de Entorno

En el servidor, `.env.staging` rastrea las versiones actuales:

```bash
IMAGE_TAG_GATEWAY=develop-sha-abc123
IMAGE_TAG_DIVIPOL=develop-sha-def456
```

#### Opción 2: Docker Inspect

```bash
# SSH al servidor
ssh tu-usuario@tu-servidor

# Verificar imágenes actuales
docker inspect tyse-gateway-staging | grep Image
docker inspect tyse-divipol-staging | grep Image
```

#### Opción 3: Logs de Despliegue

Los logs de despliegue de GitHub Actions muestran qué versiones fueron desplegadas:

```
Gateway → Actions → Deploy to Staging → Latest run
```

---

## Gestión de Dependencias

### Cambios Incompatibles (Breaking Changes)

Cuando Divipol introduce un **cambio incompatible en la API**:

1. **Versionar la API** (ej: `/api/v2/municipalities`)
2. **Mantener endpoint antiguo** temporalmente para compatibilidad hacia atrás
3. **Actualizar Gateway** para usar el nuevo endpoint
4. **Desplegar ambos** simultáneamente
5. **Deprecar endpoint antiguo** después de actualizar Gateway

### Migraciones de Base de Datos

#### Base de Datos Gateway

- Gestionada por el repositorio Gateway
- Changelog Liquibase en `tyse-scrutiny-gateway/src/main/resources/config/liquibase/`
- Se ejecuta automáticamente al iniciar Gateway

#### Base de Datos Divipol

- Gestionada por el repositorio Divipol
- Changelog Liquibase en `tyse-scrutiny-micro-divipol/src/main/resources/config/liquibase/`
- Se ejecuta automáticamente al iniciar Divipol

**Importante**: Ambas bases de datos son separadas (puertos diferentes: 5432 vs 5433)

---

## Patrones de Comunicación

### Gateway → Divipol

**Método**: Llamadas HTTP REST via service discovery de Consul

```java
// Gateway llamando a Divipol
@Service
public class DivipolClient {

  private final WebClient webClient;

  public Mono<Municipality> getMunicipality(String code) {
    return webClient.get().uri("http://tysescrutinymicrodivipol/api/municipalities/{code}", code).retrieve().bodyToMono(Municipality.class);
  }
}

```

El nombre de servicio `tysescrutinymicrodivipol` es resuelto por Consul.

### Divipol → Gateway

**Método**: Eventos Kafka (asíncrono)

```java
// Divipol publicando evento
@Service
public class DivipolEventPublisher {

  @Autowired
  private KafkaTemplate<String, DivipolEvent> kafkaTemplate;

  public void publishUpdate(DivipolEvent event) {
    kafkaTemplate.send("divipol-updates", event);
  }
}

```

Gateway se suscribe al topic `divipol-updates`.

---

## Testing Entre Repositorios

### Tests Unitarios

- Se ejecutan independientemente en cada repositorio
- Mockear dependencias de otros servicios

### Tests de Integración

- Cada repositorio prueba su propia integración con dependencias externas (BD, Kafka)
- Usar Testcontainers para testing aislado

### Tests de Contrato

**Recomendado** (aún no implementado):

Usar Spring Cloud Contract o Pact para testing de contratos de API:

1. Divipol publica el contrato de API
2. Gateway hace tests contra el contrato
3. CI falla si el contrato se rompe

### Tests E2E

- Se ejecutan en el **repositorio Gateway** únicamente
- Prueban el flujo completo de la aplicación (UI → Gateway → Divipol → BD)
- Requieren que ambos servicios estén corriendo

---

## Mejores Prácticas de Despliegue

### 1. Siempre Probar Antes de Desplegar

```bash
# Ejecutar tests localmente antes de hacer push
cd tyse-scrutiny-gateway
./mvnw verify
npm run test-ci

cd tyse-scrutiny-micro-divipol
./mvnw verify
```

### 2. Desplegar Durante Horas de Bajo Tráfico

- Programar despliegues para horarios no pico
- Notificar al equipo antes del despliegue

### 3. Monitorear Después del Despliegue

```bash
# Verificar logs inmediatamente después del despliegue
ssh tu-servidor
docker logs -f tyse-gateway-staging
docker logs -f tyse-divipol-staging

# Monitorear salud
watch curl http://localhost:8090/management/health
watch curl http://localhost:8091/management/health
```

### 4. Tener un Plan de Rollback

```bash
# Rollback rápido: desplegar tags anteriores
# Actions → Deploy to Staging → Run workflow
# gateway_tag: develop-sha-anterior
# divipol_tag: develop-sha-anterior
```

### 5. Documentar Cambios Incompatibles

Cuando se hacen cambios incompatibles:

- Actualizar documentación de API
- Agregar guía de migración
- Notificar al equipo via descripción del PR
- Coordinar el despliegue

---

## Resolución de Problemas Multi-Repo

### Problema: Gateway No Puede Alcanzar a Divipol

**Síntomas**:

- Los logs de Gateway muestran conexión rechazada
- `404 Not Found` para endpoints de Divipol

**Verificar**:

```bash
# 1. ¿Está corriendo Divipol?
docker ps | grep tyse-divipol

# 2. ¿Está Divipol registrado en Consul?
curl http://localhost:8510/v1/catalog/services | grep divipol

# 3. ¿Puede Gateway alcanzar a Divipol?
docker exec tyse-gateway-staging curl http://divipol:8081/management/health
```

**Solución**:

- Asegurar que ambos servicios están en la misma red Docker
- Verificar configuración de Consul
- Verificar que los nombres de servicio coincidan

### Problema: Desajuste de Versión Después del Despliegue

**Síntomas**:

- Código antiguo de Divipol corriendo a pesar del nuevo despliegue
- API retorna formato de respuesta antiguo

**Verificar**:

```bash
# Verificar tag de imagen desplegada
docker inspect tyse-divipol-staging | grep -A 5 "Image"
```

**Solución**:

- Verificar tag de imagen correcto en el workflow de despliegue
- Forzar descarga de última imagen: `docker compose pull divipol`
- Re-desplegar

### Problema: Cambio Incompatible Rompe Producción

**Prevención**:

1. Usar feature flags
2. Versionar tus APIs (`/api/v1`, `/api/v2`)
3. Mantener compatibilidad hacia atrás
4. Desplegar a staging primero

**Recuperación**:

```bash
# Rollback a últimas versiones buenas conocidas
# Verificar tags de git para versiones estables
git tag -l

# Desplegar versiones estables
# gateway_tag: v1.2.3
# divipol_tag: v1.1.5
```

---

## Mejoras Futuras

### Mejoras Potenciales

1. **Disparadores de Despliegue Automatizados**

   - Desplegar automáticamente cuando ambas imágenes estén listas
   - Usar API repository_dispatch de GitHub

2. **Testing de Contratos**

   - Implementar Spring Cloud Contract
   - Verificar compatibilidad de API en CI

3. **Repositorio de Configuración Compartida**

   - Almacenar configuraciones comunes (JWT secret, contraseñas de BD)
   - Usar submódulos Git o repositorio de configuración separado

4. **Consideración de Monorepo**

   - Evaluar cambio a monorepo si la coordinación se vuelve compleja
   - Herramientas: Nx, Turborepo, Lerna

5. **Service Mesh**
   - Implementar Istio o Linkerd para comunicación de servicios
   - Mejor observabilidad y gestión de tráfico

---

## Resumen

**Puntos Clave**:

- ✅ Dos repositorios independientes para flexibilidad
- ✅ CI/Build independiente, despliegue coordinado
- ✅ Gateway posee infraestructura y despliegue
- ✅ Usar versionado semántico y etiquetado
- ✅ Desplegar ambos servicios juntos via workflow de Gateway
- ✅ Monitorear ambos servicios después del despliegue
- ✅ Tener plan de rollback listo

**Checklist del Flujo de Trabajo**:

- [ ] Desarrollar feature en el repo apropiado
- [ ] Escribir tests
- [ ] Crear PR y obtener aprobación
- [ ] Mergear a develop
- [ ] Esperar a que el build se complete
- [ ] Desplegar via workflow de Gateway
- [ ] Verificar que ambos servicios estén saludables
- [ ] Monitorear logs

---

**Última Actualización**: 2025-01-04
**Mantenido por**: Repositorio Gateway
