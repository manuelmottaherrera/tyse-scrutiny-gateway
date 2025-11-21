# PostgreSQL Dockerizado para Staging

## 🚀 Cambio Importante

Las bases de datos de staging ahora están **completamente dockerizadas**. Ya no dependemos del servidor externo `db-tyse`.

## Arquitectura Actualizada

```
┌─────────────────────────────────────────────────┐
│              Docker Compose Staging              │
├─────────────────────────────────────────────────┤
│  🐘 postgres-gateway     (puerto 5432)          │
│  🐘 postgres-divipol     (puerto 5433)          │
│  📦 consul               (puerto 8510)          │
│  📦 kafka + zookeeper    (puerto 9102)          │
│  📧 mailhog              (puerto 8035)          │
│  🚪 gateway              (puerto 8090)          │
│  🗺️  divipol             (puerto 8091)          │
└─────────────────────────────────────────────────┘
```

## Ventajas de la Dockerización

✅ **Consistencia total**: Todo el stack en contenedores
✅ **Portabilidad**: Fácil mover staging a cualquier servidor
✅ **Versionado**: PostgreSQL 17.4 igual que en desarrollo
✅ **Automatización**: Liquibase maneja todas las migraciones
✅ **Aislamiento**: Cada servicio en su contenedor
✅ **Backups simples**: Volúmenes Docker persistentes

## Inicio Rápido

### 1. Configurar Variables de Entorno

```bash
cd deployment/
cp .env.staging.example .env.staging
# Editar .env.staging con las contraseñas y configuraciones
```

### 2. Ejecutar Deployment

```bash
./deploy-staging.sh
```

Este script:

- Inicia PostgreSQL en contenedores
- Espera a que las BDs estén listas
- Inicia servicios de infraestructura
- Despliega las aplicaciones
- **Liquibase aplica automáticamente**:
  - Esquemas de base de datos
  - Todas las migraciones
  - Datos iniciales (usuarios, permisos)
  - 18,016 registros de divipol

### 3. Verificar

```bash
# Ver estado de servicios
docker-compose -f docker-compose.staging.yml ps

# Ver logs
docker-compose -f docker-compose.staging.yml logs -f gateway

# Verificar salud
curl http://localhost:8090/management/health
```

## Datos Importantes

### Volúmenes Docker

Los datos persisten en volúmenes nombrados:

- `tyse-gateway-db-staging`: BD del gateway
- `tyse-divipol-db-staging`: BD del microservicio

### Conexión a las BDs

Desde el host (solo localhost):

```bash
# Gateway DB
psql -h localhost -p 5432 -U tysescrutinygateway -d tysescrutinygateway

# Divipol DB
psql -h localhost -p 5433 -U tysescrutinymicrodivipol -d tysescrutinymicrodivipol
```

Desde dentro de contenedores:

- Gateway: `postgres-gateway:5432`
- Divipol: `postgres-divipol:5432`

### Backups

```bash
# Backup Gateway
docker exec tyse-postgres-gateway-staging pg_dump \
  -U tysescrutinygateway tysescrutinygateway > backup_gateway.sql

# Backup Divipol
docker exec tyse-postgres-divipol-staging pg_dump \
  -U tysescrutinymicrodivipol tysescrutinymicrodivipol > backup_divipol.sql
```

### Restaurar desde Backup

```bash
# Restaurar Gateway
docker exec -i tyse-postgres-gateway-staging psql \
  -U tysescrutinygateway -d tysescrutinygateway < backup_gateway.sql

# Restaurar Divipol
docker exec -i tyse-postgres-divipol-staging psql \
  -U tysescrutinymicrodivipol -d tysescrutinymicrodivipol < backup_divipol.sql
```

## Liquibase y Migraciones

**No necesitas migrar datos manualmente**. Liquibase se encarga de todo:

1. Al iniciar, cada aplicación ejecuta Liquibase
2. Liquibase verifica qué changesets ya fueron aplicados
3. Aplica solo los changesets pendientes
4. La tabla `databasechangelog` rastrea el historial

Si necesitas verificar:

```bash
# Ver changesets aplicados en Gateway
docker exec tyse-postgres-gateway-staging psql \
  -U tysescrutinygateway -d tysescrutinygateway \
  -c "SELECT id, author, dateexecuted FROM databasechangelog ORDER BY dateexecuted;"
```

## Troubleshooting

### Las aplicaciones no inician

Verifica los logs de Liquibase:

```bash
docker-compose -f docker-compose.staging.yml logs gateway | grep -i liquibase
```

### Error de checksum en Liquibase

Si modificaste un changeset ya aplicado:

```bash
# Conectar a la BD
docker exec -it tyse-postgres-gateway-staging psql -U tysescrutinygateway

# Actualizar checksum (con cuidado!)
UPDATE databasechangelog
SET md5sum = 'nuevo_checksum_aqui'
WHERE id = 'id_del_changeset';
```

### Reiniciar desde cero

```bash
# Detener todo y eliminar volúmenes
docker-compose -f docker-compose.staging.yml down -v

# Iniciar de nuevo (Liquibase recreará todo)
./deploy-staging.sh
```

## Comparación: Antes vs Ahora

| Aspecto       | Antes (BD Externa)   | Ahora (Dockerizado)      |
| ------------- | -------------------- | ------------------------ |
| Ubicación BD  | ssh db-tyse          | Contenedor local         |
| Mantenimiento | Manual vía SSH       | Docker Compose           |
| Backups       | pg_dump remoto       | Docker exec local        |
| Configuración | DB_HOST=IP externa   | DB_HOST=postgres-gateway |
| Portabilidad  | Dependencia servidor | Totalmente portable      |
| Migraciones   | Manual o scripts     | Automático con Liquibase |

## Próximos Pasos

Para completar la migración en producción:

1. ✅ Actualizar `docker-compose.staging.yml`
2. ✅ Configurar variables en `.env.staging`
3. ✅ Probar deployment completo
4. ⏳ Actualizar CI/CD pipeline
5. ⏳ Documentar en wiki del proyecto

---

**Nota**: El servidor `db-tyse` ya no es necesario para staging. Todo funciona con Docker.
