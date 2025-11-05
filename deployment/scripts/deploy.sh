#!/bin/bash

################################################################################
# Tyse Scrutiny - Staging Deployment Script
#
# This script handles the deployment of the Tyse Scrutiny application to staging
# environment. It includes backup, rollback capabilities, and health checks.
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DEPLOYMENT_DIR="/home/tyse/tyse-scrutiny"
DOCKER_COMPOSE_FILE="${DEPLOYMENT_DIR}/docker-compose.staging.yml"
ENV_FILE="${DEPLOYMENT_DIR}/.env.staging"
BACKUP_DIR="${DEPLOYMENT_DIR}/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Logging functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root or with sudo
check_privileges() {
    if [ "$EUID" -eq 0 ]; then
        log_warn "Running as root - this is not recommended"
    fi
}

# Check if required files exist
check_requirements() {
    log_info "Checking requirements..."

    if [ ! -f "$ENV_FILE" ]; then
        log_error "Environment file not found: $ENV_FILE"
        log_error "Please create it from .env.staging.example"
        exit 1
    fi

    if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
        log_error "Docker Compose file not found: $DOCKER_COMPOSE_FILE"
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi

    log_info "All requirements met"
}

# Create backup directory if it doesn't exist
create_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        log_info "Created backup directory: $BACKUP_DIR"
    fi
}

# Backup current deployment state
backup_current_state() {
    log_info "Backing up current deployment state..."

    create_backup_dir

    # Backup docker-compose file
    if [ -f "$DOCKER_COMPOSE_FILE" ]; then
        cp "$DOCKER_COMPOSE_FILE" "${BACKUP_DIR}/docker-compose.staging.yml.${TIMESTAMP}"
    fi

    # Backup environment file
    if [ -f "$ENV_FILE" ]; then
        cp "$ENV_FILE" "${BACKUP_DIR}/.env.staging.${TIMESTAMP}"
    fi

    # Save running container information
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}" > "${BACKUP_DIR}/containers.${TIMESTAMP}.txt" || true

    log_info "Backup completed: ${BACKUP_DIR}/*${TIMESTAMP}"
}

# Pull latest Docker images
pull_images() {
    log_info "Pulling latest Docker images..."

    cd "$DEPLOYMENT_DIR"
    docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" pull

    log_info "Images pulled successfully"
}

# Stop current deployment
stop_services() {
    log_info "Stopping current services..."

    cd "$DEPLOYMENT_DIR"
    docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" down --remove-orphans

    log_info "Services stopped"
}

# Start new deployment
start_services() {
    log_info "Starting services..."

    cd "$DEPLOYMENT_DIR"
    docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d

    log_info "Services started"
}

# Wait for services to be ready
wait_for_services() {
    log_info "Waiting for services to be ready..."

    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))

        log_info "Health check attempt $attempt/$max_attempts..."

        if docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" ps | grep -q "healthy"; then
            log_info "Services are healthy"
            return 0
        fi

        sleep 10
    done

    log_error "Services did not become healthy in time"
    return 1
}

# Rollback to previous version
rollback() {
    log_error "Deployment failed - initiating rollback..."

    # Find most recent backup
    local latest_backup=$(ls -t ${BACKUP_DIR}/.env.staging.* 2>/dev/null | head -1)

    if [ -z "$latest_backup" ]; then
        log_error "No backup found for rollback"
        return 1
    fi

    local backup_timestamp=$(echo "$latest_backup" | sed 's/.*\.env\.staging\.//')

    log_info "Rolling back to backup: $backup_timestamp"

    # Restore files
    cp "${BACKUP_DIR}/docker-compose.staging.yml.${backup_timestamp}" "$DOCKER_COMPOSE_FILE" 2>/dev/null || true
    cp "${BACKUP_DIR}/.env.staging.${backup_timestamp}" "$ENV_FILE"

    # Restart with old configuration
    stop_services
    start_services

    log_info "Rollback completed"
}

# Cleanup old backups (keep last 5)
cleanup_old_backups() {
    log_info "Cleaning up old backups..."

    cd "$BACKUP_DIR"
    ls -t .env.staging.* 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null || true
    ls -t docker-compose.staging.yml.* 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null || true
    ls -t containers.*.txt 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null || true

    log_info "Cleanup completed"
}

# Show deployment summary
show_summary() {
    log_info "=== Deployment Summary ==="

    cd "$DEPLOYMENT_DIR"
    docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" ps

    echo ""
    log_info "Gateway URL: http://localhost:$(grep GATEWAY_PORT $ENV_FILE | cut -d'=' -f2)"
    log_info "Divipol API URL: http://localhost:$(grep DIVIPOL_PORT $ENV_FILE | cut -d'=' -f2)"
    log_info "Consul UI: http://localhost:$(grep CONSUL_PORT $ENV_FILE | cut -d'=' -f2)"
    echo ""
}

# Main deployment function
main() {
    log_info "Starting Tyse Scrutiny deployment to staging..."

    check_privileges
    check_requirements
    backup_current_state

    if ! pull_images; then
        log_error "Failed to pull images"
        exit 1
    fi

    stop_services
    start_services

    if ! wait_for_services; then
        rollback
        exit 1
    fi

    cleanup_old_backups
    show_summary

    log_info "Deployment completed successfully!"
}

# Run main function
main
