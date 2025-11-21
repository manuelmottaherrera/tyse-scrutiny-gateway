# Documentación Tyse Scrutiny Gateway

Documentación técnica del proyecto **Tyse Scrutiny Gateway**.

---

## 📚 Índice de Documentación

### Desarrollo y CI/CD

| Documento                                                       | Descripción                             |
| --------------------------------------------------------------- | --------------------------------------- |
| **[Pipeline CI/CD](./ci-cd/)**                                  | Integración continua y deployment       |
| **[Versionado Automático](./ci-cd/versionado-automatico.md)**   | Standard-version y conventional commits |
| **[Deployment a Staging](./ci-cd/deployment-staging.md)**       | Guía de deployment a staging            |
| **[Deployment a Producción](./ci-cd/deployment-produccion.md)** | Proceso formal de releases              |

### Funcionalidades del Sistema

| Documento                                       | Descripción                    |
| ----------------------------------------------- | ------------------------------ |
| **[Sistema de Autorización](./authorization/)** | Permisos enterprise granulares |
| **[Sistema de Traducciones](./traduction/)**    | i18n backend y frontend        |

### Documentación General

| Documento                     | Descripción                      |
| ----------------------------- | -------------------------------- |
| **[CLAUDE.md](../CLAUDE.md)** | Arquitectura y guía del proyecto |
| **[README.md](../README.md)** | README principal del repositorio |

---

## 🚀 Inicio Rápido

### Setup Inicial

```bash
# Clonar repositorio
git clone git@github.com:manuelmottaherrera/tyse-scrutiny-gateway.git
cd tyse-scrutiny-gateway

# Instalar dependencias
npm install

# Levantar servicios requeridos
docker compose -f src/main/docker/services.yml up -d

# Iniciar aplicación
./mvnw
```

### Desarrollo Diario

```bash
# Hacer cambios con conventional commits
git commit -m "feat(module): descripción"

# Push con validación CI
./scripts/push.sh

# Ver documentación de deployment para más detalles
```

---

## 🔧 Comandos Esenciales

```bash
# Desarrollo
./mvnw                      # Backend
npm start                   # Frontend con hot-reload
./scripts/ci-local.sh       # Tests locales

# Build
./mvnw -Pprod clean package # Build producción

# Release (solo en main)
npm run release             # Generar nueva versión
npm run release:dry-run     # Simular release

# Docker
docker compose -f src/main/docker/services.yml up -d  # Servicios
npm run java:docker         # Build imagen
```

---

## 🌍 Ambientes

| Ambiente       | URL                 | Rama       | Versionado      |
| -------------- | ------------------- | ---------- | --------------- |
| **Local**      | `localhost:8080`    | feature/\* | N/A             |
| **Staging**    | `192.168.0.58:8090` | `develop`  | git describe    |
| **Producción** | TBD                 | `main`     | Tags semánticos |

---

## 🔗 Enlaces Útiles

### Herramientas Locales

- **Aplicación:** http://localhost:8080
- **Consul UI:** http://localhost:8500
- **Swagger UI:** http://localhost:8080/swagger-ui.html (con perfil api-docs)

### Documentación Externa

- **JHipster:** https://www.jhipster.tech/documentation-archive/v8.11.0/
- **Spring Boot:** https://docs.spring.io/spring-boot/docs/3.4.5/reference/
- **React:** https://react.dev/

---

## 📞 Soporte

- **Documentación:** Esta carpeta `docs/`
- **Arquitectura:** [CLAUDE.md](../CLAUDE.md)
- **Issues:** [GitHub Issues](https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/issues)

---

## 📅 Información

- **Última Actualización:** 2025-11-21
- **Versión:** v1.0.0
- **Autor:** Manuel A. Motta H.
