# Multi-Repository Coordination Guide

This document explains how the two separate repositories (Gateway and Divipol) work together in the CI/CD pipeline.

## Repository Architecture

### Why Two Repositories?

Tyse Scrutiny uses a **multi-repository** architecture where each microservice is independently developed and versioned:

**Benefits**:

- ✅ Independent versioning and release cycles
- ✅ Smaller codebase per repository (easier to navigate)
- ✅ Independent CI pipelines (faster feedback)
- ✅ Clear ownership and responsibilities
- ✅ Flexibility to deploy services independently

**Trade-offs**:

- ⚠️ Deployment requires coordination
- ⚠️ Need to manage versions across repositories
- ⚠️ Shared infrastructure managed by Gateway

---

## Repository Responsibilities

### Gateway Repository

**Location**: `github.com/manuelmottaherrera/tyse-scrutiny-gateway`

**Owns**:

- React frontend application
- Spring Boot Gateway backend
- API Gateway routing configuration
- User authentication and management
- **Shared infrastructure** (Consul, Kafka, Zookeeper)
- **Deployment orchestration** for all services

**CI/CD**:

- Tests: Backend + Frontend + E2E
- Builds: Gateway Docker image
- Deploys: Both Gateway and Divipol services

### Divipol Repository

**Location**: `github.com/manuelmottaherrera/tyse-scrutiny-micro-divipol`

**Owns**:

- Spring Boot microservice
- Division política data (18,016 records)
- Geolocation services
- API endpoints for Colombian geographic data

**CI/CD**:

- Tests: Backend only
- Builds: Divipol Docker image
- Deploys: **No deployment workflow** (handled by Gateway)

---

## CI/CD Coordination Strategy

### Independent Build, Coordinated Deploy

```
┌──────────────────────────────────────────────────────────────────┐
│                    INDEPENDENT CI/BUILD                           │
└──────────────────────────────────────────────────────────────────┘

Gateway Repo                          Divipol Repo
─────────────                         ────────────
Developer Push                        Developer Push
     ↓                                     ↓
CI (15 min)                           CI (10 min)
  - Backend Tests                       - Backend Tests
  - Frontend Tests                      - Quality Gate
  - E2E Tests
     ↓                                     ↓
Build (5 min)                         Build (5 min)
  - Maven JAR                            - Maven JAR
  - Docker Image                         - Docker Image
     ↓                                     ↓
Push to GHCR                          Push to GHCR
  ghcr.io/.../gateway:develop           ghcr.io/.../divipol:develop

┌──────────────────────────────────────────────────────────────────┐
│                    COORDINATED DEPLOYMENT                         │
└──────────────────────────────────────────────────────────────────┘

               Manual Trigger (from Gateway repo)
                              ↓
                 Pull BOTH Docker Images
                   (Gateway + Divipol)
                              ↓
                    Deploy to Server
                  (Docker Compose Stack)
                              ↓
                     Health Checks
                (Verify both services)
                              ↓
                       ✅ Success
```

---

## Deployment Coordination

### How It Works

The **Gateway repository** contains the `deploy-staging.yml` workflow that:

1. Accepts two parameters:

   - `gateway_tag`: Which Gateway image to deploy
   - `divipol_tag`: Which Divipol image to deploy

2. Pulls both images from GitHub Container Registry

3. Updates the deployment configuration

4. Deploys the complete stack (Gateway + Divipol + Infrastructure)

5. Verifies both services are healthy

### Why Gateway Handles Deployment?

The Gateway repository manages deployment because:

- It owns the shared infrastructure (Consul, Kafka)
- It has the docker-compose orchestration file
- It needs to ensure all services start in the correct order
- It provides the deployment scripts and health checks

---

## Working Across Repositories

### Scenario 1: Feature in Single Repository

**Example**: Adding a new dashboard widget to Gateway

```bash
# 1. Work in Gateway repo
cd tyse-scrutiny-gateway
git checkout -b feature/dashboard-widget

# 2. Develop, test, commit
git add .
git commit -m "feat(dashboard): add new metrics widget"
git push origin feature/dashboard-widget

# 3. Create PR to develop
# CI runs automatically

# 4. After merge to develop
# Build creates new image: ghcr.io/.../gateway:develop

# 5. Deploy
# Go to Actions → Deploy to Staging → Run workflow
# gateway_tag: develop
# divipol_tag: develop (unchanged)
```

### Scenario 2: Feature Spanning Both Repositories

**Example**: Gateway needs to call new Divipol endpoint

#### Step 1: Develop Divipol Endpoint

```bash
cd tyse-scrutiny-micro-divipol
git checkout -b feature/new-geo-endpoint

# Implement endpoint
# Write tests
git commit -m "feat(geo): add municipality search endpoint"
git push origin feature/new-geo-endpoint

# Create PR, merge to develop
# Build creates: ghcr.io/.../divipol:develop
```

#### Step 2: Develop Gateway Integration

```bash
cd tyse-scrutiny-gateway
git checkout -b feature/use-new-geo-endpoint

# Update service to call new Divipol endpoint
# Write tests
git commit -m "feat(geo): integrate municipality search"
git push origin feature/use-new-geo-endpoint

# Create PR, merge to develop
# Build creates: ghcr.io/.../gateway:develop
```

#### Step 3: Deploy Both

```bash
# Both images are now ready
# Deploy from Gateway repo:
# Actions → Deploy to Staging → Run workflow
# gateway_tag: develop
# divipol_tag: develop
```

### Scenario 3: Hotfix in Production

**Example**: Critical bug in Divipol, Gateway is stable

```bash
# 1. Fix bug in Divipol
cd tyse-scrutiny-micro-divipol
git checkout -b hotfix/critical-bug
# Fix, test, commit
git push origin hotfix/critical-bug

# Merge to main
# Build creates: ghcr.io/.../divipol:main

# 2. Deploy ONLY Divipol update
cd tyse-scrutiny-gateway
# Actions → Deploy to Staging → Run workflow
# gateway_tag: main (unchanged)
# divipol_tag: main (updated)
```

---

## Version Management

### Image Tagging Strategy

Both repositories use the same tagging strategy:

| Trigger           | Tags Created                        | Example           |
| ----------------- | ----------------------------------- | ----------------- |
| Push to `develop` | `develop`, `develop-sha-abc123`     | Deploy staging    |
| Push to `main`    | `main`, `main-sha-abc123`, `latest` | Deploy production |
| Tag `v1.2.3`      | `v1.2.3`, `1.2`, `latest`           | Semantic release  |

### Tracking Deployed Versions

#### Option 1: Environment File

On the server, `.env.staging` tracks current versions:

```bash
IMAGE_TAG_GATEWAY=develop-sha-abc123
IMAGE_TAG_DIVIPOL=develop-sha-def456
```

#### Option 2: Docker Inspect

```bash
# SSH to server
ssh your-user@your-server

# Check current images
docker inspect tyse-gateway-staging | grep Image
docker inspect tyse-divipol-staging | grep Image
```

#### Option 3: Deployment Logs

GitHub Actions deployment logs show which versions were deployed:

```
Gateway → Actions → Deploy to Staging → Latest run
```

---

## Dependency Management

### Breaking Changes

When Divipol introduces a **breaking API change**:

1. **Version the API** (e.g., `/api/v2/municipalities`)
2. **Keep old endpoint** temporarily for backward compatibility
3. **Update Gateway** to use new endpoint
4. **Deploy both** simultaneously
5. **Deprecate old endpoint** after Gateway is updated

### Database Migrations

#### Gateway Database

- Managed by Gateway repository
- Liquibase changelog in `tyse-scrutiny-gateway/src/main/resources/config/liquibase/`
- Runs automatically on Gateway startup

#### Divipol Database

- Managed by Divipol repository
- Liquibase changelog in `tyse-scrutiny-micro-divipol/src/main/resources/config/liquibase/`
- Runs automatically on Divipol startup

**Important**: Both databases are separate (different ports: 5432 vs 5433)

---

## Communication Patterns

### Gateway → Divipol

**Method**: HTTP REST calls via Consul service discovery

```java
// Gateway calling Divipol
@Service
public class DivipolClient {

  private final WebClient webClient;

  public Mono<Municipality> getMunicipality(String code) {
    return webClient.get().uri("http://tysescrutinymicrodivipol/api/municipalities/{code}", code).retrieve().bodyToMono(Municipality.class);
  }
}

```

Service name `tysescrutinymicrodivipol` is resolved by Consul.

### Divipol → Gateway

**Method**: Kafka events (asynchronous)

```java
// Divipol publishing event
@Service
public class DivipolEventPublisher {

  @Autowired
  private KafkaTemplate<String, DivipolEvent> kafkaTemplate;

  public void publishUpdate(DivipolEvent event) {
    kafkaTemplate.send("divipol-updates", event);
  }
}

```

Gateway subscribes to `divipol-updates` topic.

---

## Testing Across Repositories

### Unit Tests

- Run independently in each repository
- Mock dependencies from other services

### Integration Tests

- Each repository tests its own integration with external dependencies (DB, Kafka)
- Use Testcontainers for isolated testing

### Contract Tests

**Recommended** (not yet implemented):

Use Spring Cloud Contract or Pact for API contract testing:

1. Divipol publishes API contract
2. Gateway tests against contract
3. CI fails if contract is broken

### E2E Tests

- Run in **Gateway repository** only
- Test full application flow (UI → Gateway → Divipol → DB)
- Requires both services running

---

## Deployment Best Practices

### 1. Always Test Before Deploying

```bash
# Run tests locally before pushing
cd tyse-scrutiny-gateway
./mvnw verify
npm run test-ci

cd tyse-scrutiny-micro-divipol
./mvnw verify
```

### 2. Deploy During Low-Traffic Hours

- Schedule deployments for off-peak hours
- Notify team before deployment

### 3. Monitor After Deployment

```bash
# Check logs immediately after deploy
ssh your-server
docker logs -f tyse-gateway-staging
docker logs -f tyse-divipol-staging

# Monitor health
watch curl http://localhost:8090/management/health
watch curl http://localhost:8091/management/health
```

### 4. Have a Rollback Plan

```bash
# Quick rollback: deploy previous tags
# Actions → Deploy to Staging → Run workflow
# gateway_tag: develop-sha-previous
# divipol_tag: develop-sha-previous
```

### 5. Document Breaking Changes

When making breaking changes:

- Update API documentation
- Add migration guide
- Notify team via PR description
- Coordinate deployment

---

## Troubleshooting Multi-Repo Issues

### Issue: Gateway Can't Reach Divipol

**Symptoms**:

- Gateway logs show connection refused
- `404 Not Found` for Divipol endpoints

**Check**:

```bash
# 1. Is Divipol running?
docker ps | grep tyse-divipol

# 2. Is Divipol registered in Consul?
curl http://localhost:8510/v1/catalog/services | grep divipol

# 3. Can Gateway reach Divipol?
docker exec tyse-gateway-staging curl http://divipol:8081/management/health
```

**Solution**:

- Ensure both services are on same Docker network
- Check Consul configuration
- Verify service names match

### Issue: Version Mismatch After Deployment

**Symptoms**:

- Old Divipol code running despite new deployment
- API returns old response format

**Check**:

```bash
# Check deployed image tag
docker inspect tyse-divipol-staging | grep -A 5 "Image"
```

**Solution**:

- Verify correct image tag in deployment workflow
- Force pull latest image: `docker compose pull divipol`
- Redeploy

### Issue: Breaking Change Breaks Production

**Prevention**:

1. Use feature flags
2. Version your APIs (`/api/v1`, `/api/v2`)
3. Maintain backward compatibility
4. Deploy to staging first

**Recovery**:

```bash
# Rollback to last known good versions
# Check git tags for stable versions
git tag -l

# Deploy stable versions
# gateway_tag: v1.2.3
# divipol_tag: v1.1.5
```

---

## Future Improvements

### Potential Enhancements

1. **Automated Deployment Triggers**

   - Deploy automatically when both images are ready
   - Use GitHub repository_dispatch API

2. **Contract Testing**

   - Implement Spring Cloud Contract
   - Verify API compatibility in CI

3. **Shared Configuration Repo**

   - Store common configs (JWT secret, DB passwords)
   - Use Git submodules or separate config repo

4. **Monorepo Consideration**

   - Evaluate moving to monorepo if coordination becomes complex
   - Tools: Nx, Turborepo, Lerna

5. **Service Mesh**
   - Implement Istio or Linkerd for service communication
   - Better observability and traffic management

---

## Summary

**Key Points**:

- ✅ Two independent repositories for flexibility
- ✅ Independent CI/Build, coordinated deployment
- ✅ Gateway owns infrastructure and deployment
- ✅ Use semantic versioning and tagging
- ✅ Deploy both services together via Gateway workflow
- ✅ Monitor both services after deployment
- ✅ Have rollback plan ready

**Workflow Checklist**:

- [ ] Develop feature in appropriate repo
- [ ] Write tests
- [ ] Create PR and get approval
- [ ] Merge to develop
- [ ] Wait for build to complete
- [ ] Deploy via Gateway workflow
- [ ] Verify both services healthy
- [ ] Monitor logs

---

**Last Updated**: 2025-01-04
**Maintained by**: Gateway Repository
