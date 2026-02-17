# Estado del Proyecto Tyse Scrutiny - 2026-02-17

## Resumen de Arquitectura

```
tyse-infrastructure (Docker)
├── Consul (8500) - Service discovery
├── Kafka (9092) - Mensajería
├── MinIO (9000/9001) - Storage S3
└── MailHog (1025/8025) - SMTP dev

tyse-scrutiny-gateway (8080) - PostgreSQL:5432
tyse-scrutiny-micro-divipol (8081) - PostgreSQL:5433
tyse-scrutiny-micro-scrutiny (8084) - PostgreSQL:5434
tyse-scrutiny-mock-pipeline (8082)
tyse-scrutiny-micro-notification (8085)
```

---

## Commits Pendientes de Push

| Repo               | Commits | Comando                   |
| ------------------ | ------- | ------------------------- |
| gateway            | 8       | `git push origin develop` |
| micro-divipol      | 6       | `git push origin develop` |
| mock-pipeline      | 1       | `git push origin develop` |
| micro-notification | 1       | `git push origin develop` |

---

## Pendientes por Prioridad

### Alta - Funcionalidad Core

- [ ] **Probar E14 Upload en navegador**

  - Iniciar: `./mvnw` + `npm start`
  - URL: http://localhost:9000/e14
  - Verificar upload a MinIO y mensaje en Kafka `e14-pdf-uploaded`

- [ ] **Página de Anomalías**

  - Listado de anomalías detectadas
  - Filtros: severidad, fecha, tipo
  - Requiere endpoint gateway → micro-scrutiny

- [ ] **Página de Preconteo**

  - Formulario para ingresar votos esperados por mesa
  - Habilita detección de anomalías tipo `PRECOUNT_DIFFERENCE`

- [ ] **Página de Exportación**
  - CSV/PDF de E14 procesados
  - Reportes de anomalías

### Media - Testing

- [ ] Test E2E completo: mock → persist → anomalía → notificación
- [ ] Test de carga con múltiples E14
- [ ] Dead Letter Topics (manejo de errores Kafka)

### Opcional - Mejoras UX

- [ ] Menú de navegación Divipol (sub-items: Explorador, Testigos, Organizaciones, etc.)
- [ ] Componente Breadcrumb (`Divipol / Puesto 12345 / Mesas`)
- [ ] Página de Testigos dedicada (CRUD independiente)
- [ ] Página de Detalle de Testigo

### Futuro

- [ ] Fase 6: Migración OCR + Cleaner (esperando otro desarrollador)

---

## Flujos Funcionando

### Pipeline E14 Básico

```
mock-pipeline → Kafka (e14-data-cleaned) → micro-scrutiny → PostgreSQL
```

### Pipeline Anomalías E2E

```
mock-pipeline (genera E14 con anomalía)
    → Kafka (e14-data-cleaned)
    → micro-scrutiny (detecta, publica)
    → Kafka (e14-anomaly-detected)
    → micro-notification (envía email)
    → MailHog
```

---

## Credenciales Dev

- **MinIO**: tyseadmin / tyseadmin123
- **MailHog Web**: http://localhost:8025

---

## Comandos Rápidos

```bash
# Levantar infraestructura
cd ~/repos/tyse/tyse-infrastructure && docker compose up -d

# Levantar PostgreSQL de cada micro
cd ~/repos/tyse/tyse-scrutiny-gateway && docker compose -f src/main/docker/postgresql.yml up -d
cd ~/repos/tyse/tyse-scrutiny-micro-divipol && docker compose -f src/main/docker/postgresql.yml up -d
cd ~/repos/tyse/tyse-scrutiny-micro-scrutiny && docker compose -f src/main/docker/postgresql.yml up -d

# Gateway
cd ~/repos/tyse/tyse-scrutiny-gateway
./mvnw        # Backend (terminal 1)
npm start     # Frontend (terminal 2)

# Tests
./mvnw verify && npm test
```
