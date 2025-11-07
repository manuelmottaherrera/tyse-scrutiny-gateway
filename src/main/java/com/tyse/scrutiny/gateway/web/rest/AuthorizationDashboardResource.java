package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.service.authorization.AuthorizationDashboardService;
import com.tyse.scrutiny.gateway.service.dto.dashboard.DashboardMetricsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for authorization dashboard metrics.
 */
@RestController
@RequestMapping("/api/authorization/dashboard")
@Transactional(readOnly = true)
@Tag(name = "Authorization Dashboard", description = "API para métricas y estadísticas del sistema de autorización")
public class AuthorizationDashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationDashboardResource.class);

    private final AuthorizationDashboardService dashboardService;

    public AuthorizationDashboardResource(AuthorizationDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * {@code GET  /api/authorization/dashboard/metrics} : Get dashboard metrics.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and dashboard metrics in body.
     */
    @Operation(
        summary = "Obtener métricas del dashboard",
        description = "Retorna todas las métricas y estadísticas del sistema de autorización: " +
        "total de roles, permisos, usuarios activos, roles expirados, actividad reciente, " +
        "roles próximos a expirar, top autoridades y uso de permisos. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Métricas obtenidas exitosamente",
                content = @Content(schema = @Schema(implementation = DashboardMetricsDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<DashboardMetricsDTO>> getDashboardMetrics() {
        LOG.debug("REST request to get dashboard metrics");
        return dashboardService.getDashboardMetrics().map(ResponseEntity::ok);
    }
}
