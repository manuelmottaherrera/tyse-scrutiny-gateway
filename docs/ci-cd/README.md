# CI/CD Pipeline - Tyse Scrutiny (Multi-Repository)

Complete Continuous Integration and Continuous Deployment pipeline for Tyse Scrutiny microservices platform.

## Architecture Overview

Tyse Scrutiny consists of **two separate GitHub repositories**:

1. **Gateway Repository** (`tyse-scrutiny-gateway`)

   - Contains: React frontend + Spring Boot backend
   - Manages: Shared infrastructure (Consul, Kafka)
   - Handles: Deployment orchestration for both services

2. **Divipol Repository** (`tyse-scrutiny-micro-divipol`)
   - Contains: Spring Boot microservice (API only)
   - Provides: Division política data for Colombia

## Repository Structure

```
GitHub:
├── manuelmottaherrera/tyse-scrutiny-gateway
│   ├── .github/workflows/
│   │   ├── ci.yml            # Gateway CI (backend + frontend + E2E)
│   │   ├── build.yml         # Build Gateway Docker image
│   │   ├── deploy-staging.yml # Deploy BOTH services
│   │   └── sonarcloud.yml    # Code quality analysis
│   ├── deployment/
│   │   ├── docker-compose.staging.yml  # Full stack
│   │   ├── scripts/
│   │   └── .env.staging.example
│   └── docs/ci-cd/           # This documentation
│
└── manuelmottaherrera/tyse-scrutiny-micro-divipol
    └── .github/workflows/
        ├── ci.yml            # Divipol CI (backend tests)
        ├── build.yml         # Build Divipol Docker image
        └── sonarcloud.yml    # Code quality analysis
```

---

## Quick Start

### For Developers

#### Working on Gateway

```bash
# Clone Gateway repository
git clone git@github.com:manuelmottaherrera/tyse-scrutiny-gateway.git
cd tyse-scrutiny-gateway

# Create feature branch
git checkout -b feature/my-feature

# Make changes, commit, push
git add .
git commit -m "feat: add new feature"
git push origin feature/my-feature

# CI automatically runs:
# ✅ Backend tests
# ✅ Frontend tests
# ✅ Code quality checks
```

#### Working on Divipol

```bash
# Clone Divipol repository
git clone git@github.com:manuelmottaherrera/tyse-scrutiny-micro-divipol.git
cd tyse-scrutiny-micro-divipol

# Create feature branch
git checkout -b feature/my-feature

# Make changes, commit, push
git add .
git commit -m "feat: add divipol feature"
git push origin feature/my-feature

# CI automatically runs:
# ✅ Backend tests
# ✅ Code quality checks
```

### Deployment Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    SEPARATE REPOSITORIES                         │
└─────────────────────────────────────────────────────────────────┘

Gateway Repo:                          Divipol Repo:
  Push to develop                        Push to develop
       ↓                                      ↓
  CI Tests (15 min)                      CI Tests (10 min)
       ↓                                      ↓
  Build Image                            Build Image
       ↓                                      ↓
  Push to GHCR                           Push to GHCR
       ↓                                      ↓
       └──────────────┬────────────────────────┘
                      ↓
             Manual Deploy Trigger
          (Gateway repo workflow_dispatch)
                      ↓
            Deploy Both Services
                      ↓
              Health Checks
                      ↓
              ✅ Staging Live
```

---

## Workflows Explained

### Gateway Repository Workflows

#### 1. CI Pipeline (`ci.yml`)

**Trigger**: Every push/PR to main or develop

**Jobs**:

- ✅ Backend Tests (Maven + JUnit + Testcontainers)
- ✅ Frontend Tests (Jest + ESLint + Prettier)
- ✅ E2E Tests (Cypress - only main/develop)
- ✅ Quality Gate

**Duration**: ~15 minutes

#### 2. Build (`build.yml`)

**Trigger**: After CI success on main/develop

**Jobs**:

- 🏗️ Build production JAR
- 🐳 Create Docker image
- 📦 Push to GitHub Container Registry (`ghcr.io/manuelmottaherrera/tyse-scrutiny-gateway`)

**Tags**: `develop`, `main`, `develop-sha-abc123`, `latest` (main only)

**Duration**: ~5-8 minutes

#### 3. Deploy Staging (`deploy-staging.yml`)

**Trigger**: Manual (`workflow_dispatch`)

**What it does**:

- Pulls latest images for **both** Gateway and Divipol
- SSH to staging server
- Runs deployment script (backup, stop, start, health check)
- Verifies both services are healthy

**Parameters**:

- `gateway_tag`: Gateway image tag (default: `develop`)
- `divipol_tag`: Divipol image tag (default: `develop`)

**Duration**: ~3-5 minutes

#### 4. SonarCloud (`sonarcloud.yml`)

**Trigger**: Every push/PR (parallel with CI)

**Analyzes**:

- Java backend code
- TypeScript/React frontend code
- Test coverage
- Security vulnerabilities

**Duration**: ~8-10 minutes

---

### Divipol Repository Workflows

#### 1. CI Pipeline (`ci.yml`)

**Trigger**: Every push/PR to main or develop

**Jobs**:

- ✅ Backend Tests (Maven + JUnit)
- ✅ Quality Gate

**Duration**: ~10 minutes

#### 2. Build (`build.yml`)

**Trigger**: After CI success on main/develop

**Jobs**:

- 🏗️ Build production JAR
- 🐳 Create Docker image
- 📦 Push to GitHub Container Registry (`ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol`)

**Tags**: `develop`, `main`, `develop-sha-abc123`, `latest` (main only)

**Duration**: ~5 minutes

#### 3. SonarCloud (`sonarcloud.yml`)

**Trigger**: Every push/PR (parallel with CI)

**Analyzes**:

- Java backend code
- Test coverage
- Security vulnerabilities

**Duration**: ~5-8 minutes

---

## How to Deploy

### Automatic Flow (Recommended)

1. **Merge Gateway PR to develop**:

   ```bash
   # CI runs → Build creates image
   ```

2. **Merge Divipol PR to develop**:

   ```bash
   # CI runs → Build creates image
   ```

3. **Deploy from Gateway repo**:
   - Go to Gateway repository on GitHub
   - Click **Actions** → **Deploy to Staging**
   - Click **Run workflow**
   - Leave tags as `develop` (or specify custom tags)
   - Click **Run workflow**

### Manual Deployment with Specific Versions

If you want to deploy specific versions (e.g., testing a hotfix):

1. Go to **Gateway** repository → **Actions** → **Deploy to Staging**
2. Click **Run workflow**
3. Enter custom tags:
   - `gateway_tag`: `develop-sha-abc123` (or any tag)
   - `divipol_tag`: `develop-sha-def456` (or any tag)
4. Click **Run workflow**

---

## Access Deployed Services

After successful deployment:

- **Gateway UI**: http://your-server:8090
- **Gateway API Docs**: http://your-server:8090/swagger-ui.html
- **Divipol API**: http://your-server:8091/swagger-ui.html
- **Consul UI**: http://your-server:8510
- **Health Checks**:
  - Gateway: http://your-server:8090/management/health
  - Divipol: http://your-server:8091/management/health

---

## Required GitHub Secrets

### Gateway Repository

| Secret                      | Description         | Example                 |
| --------------------------- | ------------------- | ----------------------- |
| `SSH_HOST`                  | Staging server IP   | `192.168.1.50`          |
| `SSH_USER`                  | SSH username        | `deploy`                |
| `SSH_PRIVATE_KEY`           | SSH private key     | `-----BEGIN RSA...`     |
| `STAGING_HOST`              | Same as SSH_HOST    | `192.168.1.50`          |
| `SONAR_TOKEN`               | SonarCloud token    | `squ_abc123...`         |
| `SONAR_ORGANIZATION`        | SonarCloud org      | `manuelmottaherrera`    |
| `SONAR_PROJECT_KEY_GATEWAY` | Gateway project key | `tyse-scrutiny-gateway` |

### Divipol Repository

| Secret                      | Description         | Example                       |
| --------------------------- | ------------------- | ----------------------------- |
| `SONAR_TOKEN`               | SonarCloud token    | `squ_abc123...`               |
| `SONAR_ORGANIZATION`        | SonarCloud org      | `manuelmottaherrera`          |
| `SONAR_PROJECT_KEY_DIVIPOL` | Divipol project key | `tyse-scrutiny-micro-divipol` |

---

## Server Configuration

All deployment files are in the **Gateway repository** under `deployment/`:

### On Staging Server

**Location**: `/opt/tyse-scrutiny/`

```
/opt/tyse-scrutiny/
├── docker-compose.staging.yml
├── .env.staging              # Your configuration (not in git)
├── scripts/
│   ├── deploy.sh            # Deployment script
│   └── health-check.sh      # Health verification
└── backups/                  # Automatic backups
```

### Environment File (`.env.staging`)

Copy from template:

```bash
cd /opt/tyse-scrutiny
cp .env.staging.example .env.staging
nano .env.staging
```

**Key variables**:

```bash
# Image configuration (separate repos!)
REGISTRY=ghcr.io
GATEWAY_IMAGE=manuelmottaherrera/tyse-scrutiny-gateway
DIVIPOL_IMAGE=manuelmottaherrera/tyse-scrutiny-micro-divipol
IMAGE_TAG_GATEWAY=develop
IMAGE_TAG_DIVIPOL=develop

# Ports (customize to avoid conflicts)
GATEWAY_PORT=8090
DIVIPOL_PORT=8091
CONSUL_PORT=8510
KAFKA_PORT=9102

# Database (your database server)
DB_HOST=your-db-server-ip
GATEWAY_DB_PASSWORD=your-password
DIVIPOL_DB_PASSWORD=your-password

# Security
JWT_SECRET=your-jwt-secret-base64
```

---

## Common Tasks

### Check Deployment Status

```bash
# SSH to server
ssh your-user@your-server

# Check running containers
docker ps | grep tyse

# View logs
docker logs tyse-gateway-staging
docker logs tyse-divipol-staging

# Run health check
cd /opt/tyse-scrutiny
./scripts/health-check.sh
```

### Rollback Deployment

```bash
# SSH to server
ssh your-user@your-server
cd /opt/tyse-scrutiny

# List backups
ls -lh backups/

# Restore (replace TIMESTAMP)
cp backups/.env.staging.TIMESTAMP .env.staging

# Redeploy
./scripts/deploy.sh
```

### View Pipeline Status

**Gateway**:

- https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/actions

**Divipol**:

- https://github.com/manuelmottaherrera/tyse-scrutiny-micro-divipol/actions

---

## Troubleshooting

### CI Fails in One Repository

**Problem**: Tests fail in GitHub Actions

**Solution**:

```bash
# Run tests locally
./mvnw clean verify
npm run test-ci  # Gateway only
```

### Build Fails

**Problem**: Docker image build fails

**Solution**:

- Check Dockerfile is present in `src/main/docker/`
- Ensure production JAR builds: `./mvnw -Pprod clean verify -DskipTests`
- Check GitHub Actions logs for specific error

### Deployment Fails

**Problem**: Deployment workflow fails

**Check**:

1. SSH connection: `ssh your-user@your-server`
2. Docker login works: `docker login ghcr.io`
3. Images exist:
   - `ghcr.io/manuelmottaherrera/tyse-scrutiny-gateway:develop`
   - `ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:develop`
4. Environment file is correct: `/opt/tyse-scrutiny/.env.staging`

### Services Not Healthy

**Check**:

```bash
# Server logs
docker logs tyse-gateway-staging
docker logs tyse-divipol-staging

# Database connectivity
psql -h db-server -U tysescrutinygateway -d tysescrutinygateway
psql -h db-server -U tysescrutinymicrodivipol -d tysescrutinymicrodivipol

# Port conflicts
sudo netstat -tuln | grep -E '8090|8091'
```

---

## Documentation

- **[SETUP.md](./SETUP.md)** - Complete setup guide (servers, databases, GitHub)
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Technical architecture details
- **[MULTI-REPO-COORDINATION.md](./MULTI-REPO-COORDINATION.md)** - Multi-repo workflow guide

---

## Branch Strategy

### Both Repositories

| Branch      | CI  | Build     | Deploy |
| ----------- | --- | --------- | ------ |
| `feature/*` | ✅  | ❌        | ❌     |
| `develop`   | ✅  | ✅ (auto) | Manual |
| `main`      | ✅  | ✅ (auto) | Manual |

**Recommended Workflow**:

1. Create feature branch
2. Develop and push (CI runs)
3. Create PR to `develop`
4. Merge after approval
5. Build happens automatically
6. Deploy manually from Gateway repo

---

## Performance

| Task                      | Duration   |
| ------------------------- | ---------- |
| Gateway CI                | ~15 min    |
| Gateway Build             | ~5-8 min   |
| Divipol CI                | ~10 min    |
| Divipol Build             | ~5 min     |
| Deployment                | ~3-5 min   |
| **Total (both + deploy)** | ~25-30 min |

---

## Support

For issues or questions:

1. Check this documentation
2. Review GitHub Actions logs
3. Check server logs: `docker logs container-name`
4. Create issue in respective repository

---

**Last Updated**: 2025-01-04
**Repository**: manuelmottaherrera/tyse-scrutiny-gateway
