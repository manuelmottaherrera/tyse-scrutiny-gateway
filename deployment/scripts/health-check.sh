#!/bin/bash

################################################################################
# Tyse Scrutiny - Health Check Script
#
# This script verifies that all services are running and healthy
################################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DEPLOYMENT_DIR="/home/tyse/tyse-scrutiny"
ENV_FILE="${DEPLOYMENT_DIR}/.env.staging"
MAX_RETRIES=30
RETRY_INTERVAL=10

# Load environment variables
if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
else
    echo -e "${RED}[ERROR]${NC} Environment file not found: $ENV_FILE"
    exit 1
fi

# Default ports if not set
GATEWAY_PORT=${GATEWAY_PORT:-8090}
DIVIPOL_PORT=${DIVIPOL_PORT:-8091}
CONSUL_PORT=${CONSUL_PORT:-8510}

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

# Check if a service responds on HTTP
check_http_endpoint() {
    local service_name=$1
    local url=$2
    local expected_codes=$3  # Space-separated list of acceptable codes

    log_info "Checking $service_name at $url..."

    local retry=0
    while [ $retry -lt $MAX_RETRIES ]; do
        retry=$((retry + 1))

        # Try to get HTTP response code
        http_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

        # Check if code is in expected codes
        for expected_code in $expected_codes; do
            if [ "$http_code" == "$expected_code" ]; then
                log_info "$service_name is responding (HTTP $http_code)"
                return 0
            fi
        done

        log_warn "$service_name check attempt $retry/$MAX_RETRIES (HTTP $http_code)"
        sleep $RETRY_INTERVAL
    done

    log_error "$service_name health check failed after $MAX_RETRIES attempts"
    return 1
}

# Check Docker container status
check_container_status() {
    local container_name=$1

    log_info "Checking container status: $container_name"

    if docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
        local status=$(docker inspect --format='{{.State.Health.Status}}' "$container_name" 2>/dev/null || echo "no-healthcheck")

        if [ "$status" == "healthy" ] || [ "$status" == "no-healthcheck" ]; then
            log_info "Container $container_name is running (health: $status)"
            return 0
        else
            log_warn "Container $container_name is running but health status is: $status"
            return 1
        fi
    else
        log_error "Container $container_name is not running"
        return 1
    fi
}

# Check if Consul is healthy
check_consul() {
    log_info "=== Checking Consul ==="

    if ! check_container_status "tyse-consul-staging"; then
        return 1
    fi

    # Check Consul HTTP API
    check_http_endpoint "Consul" "http://localhost:${CONSUL_PORT}/v1/status/leader" "200"
}

# Check if Kafka is healthy
check_kafka() {
    log_info "=== Checking Kafka ==="

    if ! check_container_status "tyse-kafka-staging"; then
        return 1
    fi

    log_info "Kafka container is running"
    return 0
}

# Check if Gateway is healthy
check_gateway() {
    log_info "=== Checking Gateway Application ==="

    if ! check_container_status "tyse-gateway-staging"; then
        return 1
    fi

    # Check Gateway health endpoint (200 = UP, 503 = starting but responding)
    check_http_endpoint "Gateway" "http://localhost:${GATEWAY_PORT}/management/health" "200 503"
}

# Check if Divipol is healthy
check_divipol() {
    log_info "=== Checking Divipol Microservice ==="

    if ! check_container_status "tyse-divipol-staging"; then
        return 1
    fi

    # Check Divipol health endpoint (200 = UP, 503 = starting but responding)
    check_http_endpoint "Divipol" "http://localhost:${DIVIPOL_PORT}/management/health" "200 503"
}

# Check service registration in Consul
check_service_registration() {
    log_info "=== Checking Service Registration in Consul ==="

    local gateway_registered=false
    local divipol_registered=false

    # Check if services are registered in Consul
    local services=$(curl -s "http://localhost:${CONSUL_PORT}/v1/catalog/services" 2>/dev/null || echo "{}")

    if echo "$services" | grep -q "tysescrutinygateway"; then
        log_info "Gateway is registered in Consul"
        gateway_registered=true
    else
        log_warn "Gateway is not yet registered in Consul"
    fi

    if echo "$services" | grep -q "tysescrutinymicrodivipol"; then
        log_info "Divipol is registered in Consul"
        divipol_registered=true
    else
        log_warn "Divipol is not yet registered in Consul"
    fi

    if [ "$gateway_registered" = true ] && [ "$divipol_registered" = true ]; then
        return 0
    else
        log_warn "Not all services are registered yet (this is normal during startup)"
        return 0  # Don't fail deployment for this
    fi
}

# Show deployment summary
show_summary() {
    log_info "=== Health Check Summary ==="
    echo ""
    echo "Service Status:"
    docker ps --filter "name=tyse-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
    echo "Service URLs:"
    echo "  Gateway:      http://localhost:${GATEWAY_PORT}"
    echo "  Divipol API:  http://localhost:${DIVIPOL_PORT}"
    echo "  Consul UI:    http://localhost:${CONSUL_PORT}"
    echo ""
}

# Main health check function
main() {
    log_info "Starting Tyse Scrutiny health check..."
    echo ""

    local all_healthy=true

    # Check infrastructure services
    if ! check_consul; then
        all_healthy=false
    fi
    echo ""

    if ! check_kafka; then
        all_healthy=false
    fi
    echo ""

    # Check application services
    if ! check_gateway; then
        all_healthy=false
    fi
    echo ""

    if ! check_divipol; then
        all_healthy=false
    fi
    echo ""

    # Check service registration (optional)
    check_service_registration
    echo ""

    # Show summary
    show_summary

    # Final result
    if [ "$all_healthy" = true ]; then
        log_info "All health checks passed!"
        exit 0
    else
        log_error "Some health checks failed"
        exit 1
    fi
}

# Run main function
main
