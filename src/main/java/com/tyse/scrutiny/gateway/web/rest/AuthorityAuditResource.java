package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import com.tyse.scrutiny.gateway.service.authorization.AuditExportService;
import com.tyse.scrutiny.gateway.service.authorization.AuthorityAuditService;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuditMetricsSummaryDTO;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuditSearchCriteria;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuthorityAuditDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for querying {@link com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit}.
 * Provides endpoints for audit log queries, filtering, and reporting.
 * All endpoints are read-only and require ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/authority-audits")
@Transactional(readOnly = true)
@Tag(name = "Authority Audits", description = "API para consultar logs de auditoría de roles - Solo lectura para administradores")
public class AuthorityAuditResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityAuditResource.class);

    private final AuthorityAuditService auditService;
    private final AuditExportService exportService;

    public AuthorityAuditResource(AuthorityAuditService auditService, AuditExportService exportService) {
        this.auditService = auditService;
        this.exportService = exportService;
    }

    /**
     * {@code GET  /authority-audits} : get all the authority audits with pagination.
     *
     * @param page the page number (default: 0)
     * @param size the page size (default: 20)
     * @param sort the sort order (default: changedDate,desc)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of audits in body.
     */
    @Operation(
        summary = "Obtener todos los logs de auditoría",
        description = "Retorna una lista paginada de todos los cambios realizados en roles/autoridades. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de auditorías obtenida exitosamente",
                content = @Content(schema = @Schema(implementation = AuthorityAuditDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Flux<AuthorityAuditDTO> getAllAudits(
        @Parameter(description = "Número de página") @RequestParam(value = "page", defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página") @RequestParam(value = "size", defaultValue = "20") int size,
        @Parameter(description = "Ordenamiento") @RequestParam(value = "sort", defaultValue = "changedDate,desc") String sort
    ) {
        LOG.debug("REST request to get all AuthorityAudits - page: {}, size: {}", page, size);

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        return auditService.findAll(pageable);
    }

    /**
     * {@code GET  /authority-audits/:id} : get the "id" authority audit.
     *
     * @param id the id of the audit log to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the audit, or with status {@code 404 (Not Found)}.
     */
    @Operation(
        summary = "Obtener log de auditoría por ID",
        description = "Retorna un registro de auditoría específico por su ID. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Auditoría encontrada",
                content = @Content(schema = @Schema(implementation = AuthorityAuditDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Auditoría no encontrada", content = @Content),
        }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<AuthorityAuditDTO>> getAudit(
        @Parameter(description = "ID del log de auditoría", required = true) @PathVariable("id") Long id
    ) {
        LOG.debug("REST request to get AuthorityAudit : {}", id);
        Mono<AuthorityAuditDTO> audit = auditService.findOne(id);
        return ResponseUtil.wrapOrNotFound(audit);
    }

    /**
     * {@code GET  /authority-audits/authority/:authorityId} : get all audits for a specific authority.
     *
     * @param authorityId the id of the authority
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of audits in body.
     */
    @Operation(
        summary = "Obtener historial de cambios de un rol",
        description = "Retorna todos los cambios realizados a un rol específico, ordenados por fecha descendente. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Historial de cambios obtenido exitosamente",
                content = @Content(schema = @Schema(implementation = AuthorityAuditDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/authority/{authorityId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Flux<AuthorityAuditDTO> getAuditsByAuthority(
        @Parameter(description = "ID del rol", required = true) @PathVariable("authorityId") Long authorityId
    ) {
        LOG.debug("REST request to get audits for authority: {}", authorityId);
        return auditService.findByAuthorityId(authorityId);
    }

    /**
     * {@code GET  /authority-audits/search} : search audits with filters.
     *
     * @param authorityId filter by authority ID (optional)
     * @param changedBy filter by user who made the change (optional)
     * @param action filter by action type (optional)
     * @param fromDate filter by start date (optional)
     * @param toDate filter by end date (optional)
     * @param page the page number (default: 0)
     * @param size the page size (default: 20)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of matching audits in body.
     */
    @Operation(
        summary = "Búsqueda avanzada de auditorías",
        description = "Permite buscar logs de auditoría con múltiples filtros opcionales. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Búsqueda completada exitosamente",
                content = @Content(schema = @Schema(implementation = AuthorityAuditDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Flux<AuthorityAuditDTO> searchAudits(
        @Parameter(description = "ID del rol") @RequestParam(value = "authorityId", required = false) Long authorityId,
        @Parameter(description = "Usuario que hizo el cambio") @RequestParam(value = "changedBy", required = false) String changedBy,
        @Parameter(description = "Tipo de acción") @RequestParam(value = "action", required = false) AuditAction action,
        @Parameter(description = "Fecha desde (ISO-8601)") @RequestParam(value = "fromDate", required = false) Instant fromDate,
        @Parameter(description = "Fecha hasta (ISO-8601)") @RequestParam(value = "toDate", required = false) Instant toDate,
        @Parameter(description = "Número de página") @RequestParam(value = "page", defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página") @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        LOG.debug(
            "REST request to search audits - authorityId: {}, changedBy: {}, action: {}, fromDate: {}, toDate: {}",
            authorityId,
            changedBy,
            action,
            fromDate,
            toDate
        );

        AuditSearchCriteria criteria = new AuditSearchCriteria(authorityId, changedBy, action, fromDate, toDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedDate"));

        return auditService.searchAudits(criteria, pageable);
    }

    /**
     * {@code GET  /authority-audits/recent} : get recent audit logs.
     *
     * @param limit the maximum number of entries to return (default: 50, max: 200)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of recent audits in body.
     */
    @Operation(
        summary = "Obtener cambios recientes",
        description = "Retorna los últimos N cambios realizados en roles, ordenados por fecha descendente. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Cambios recientes obtenidos exitosamente",
                content = @Content(schema = @Schema(implementation = AuthorityAuditDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Flux<AuthorityAuditDTO> getRecentAudits(
        @Parameter(description = "Número máximo de entradas (máx: 200)") @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        LOG.debug("REST request to get {} recent audits", limit);

        // Cap the limit to prevent excessive data retrieval
        int safeLimit = Math.min(limit, 200);

        return auditService.findRecent(safeLimit);
    }

    /**
     * {@code GET  /authority-audits/export/csv} : export audit logs to CSV.
     *
     * @param authorityId filter by authority ID (optional)
     * @param changedBy filter by user who made the change (optional)
     * @param action filter by action type (optional)
     * @param fromDate filter by start date (optional)
     * @param toDate filter by end date (optional)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and CSV file as body.
     */
    @Operation(
        summary = "Exportar auditorías a CSV",
        description = "Exporta logs de auditoría a formato CSV con filtros opcionales. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Archivo CSV generado exitosamente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/export/csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<byte[]>> exportToCsv(
        @Parameter(description = "ID del rol") @RequestParam(value = "authorityId", required = false) Long authorityId,
        @Parameter(description = "Usuario que hizo el cambio") @RequestParam(value = "changedBy", required = false) String changedBy,
        @Parameter(description = "Tipo de acción") @RequestParam(value = "action", required = false) AuditAction action,
        @Parameter(description = "Fecha desde (ISO-8601)") @RequestParam(value = "fromDate", required = false) Instant fromDate,
        @Parameter(description = "Fecha hasta (ISO-8601)") @RequestParam(value = "toDate", required = false) Instant toDate
    ) {
        LOG.debug("REST request to export audits to CSV - authorityId: {}, changedBy: {}, action: {}", authorityId, changedBy, action);

        AuditSearchCriteria criteria = new AuditSearchCriteria(authorityId, changedBy, action, fromDate, toDate);

        return exportService
            .exportToCsv(criteria)
            .map(csvBytes -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData("attachment", "authority-audit-log.csv");
                headers.setContentLength(csvBytes.length);

                return ResponseEntity.ok().headers(headers).body(csvBytes);
            });
    }

    /**
     * {@code GET  /authority-audits/export/json} : export audit logs to JSON.
     *
     * @param authorityId filter by authority ID (optional)
     * @param changedBy filter by user who made the change (optional)
     * @param action filter by action type (optional)
     * @param fromDate filter by start date (optional)
     * @param toDate filter by end date (optional)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and JSON file as body.
     */
    @Operation(
        summary = "Exportar auditorías a JSON",
        description = "Exporta logs de auditoría a formato JSON con filtros opcionales. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Archivo JSON generado exitosamente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/export/json")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<byte[]>> exportToJson(
        @Parameter(description = "ID del rol") @RequestParam(value = "authorityId", required = false) Long authorityId,
        @Parameter(description = "Usuario que hizo el cambio") @RequestParam(value = "changedBy", required = false) String changedBy,
        @Parameter(description = "Tipo de acción") @RequestParam(value = "action", required = false) AuditAction action,
        @Parameter(description = "Fecha desde (ISO-8601)") @RequestParam(value = "fromDate", required = false) Instant fromDate,
        @Parameter(description = "Fecha hasta (ISO-8601)") @RequestParam(value = "toDate", required = false) Instant toDate
    ) {
        LOG.debug("REST request to export audits to JSON - authorityId: {}, changedBy: {}, action: {}", authorityId, changedBy, action);

        AuditSearchCriteria criteria = new AuditSearchCriteria(authorityId, changedBy, action, fromDate, toDate);

        return exportService
            .exportToJson(criteria)
            .map(jsonBytes -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setContentDispositionFormData("attachment", "authority-audit-log.json");
                headers.setContentLength(jsonBytes.length);

                return ResponseEntity.ok().headers(headers).body(jsonBytes);
            });
    }

    /**
     * {@code GET  /authority-audits/metrics/summary} : get comprehensive audit metrics summary.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the metrics summary in body.
     */
    @Operation(
        summary = "Obtener resumen estadístico de auditorías",
        description = "Retorna métricas agregadas de auditoría incluyendo contadores por acción, usuarios más activos y actividad por períodos. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Resumen de métricas obtenido exitosamente",
                content = @Content(schema = @Schema(implementation = AuditMetricsSummaryDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/metrics/summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<AuditMetricsSummaryDTO>> getMetricsSummary() {
        LOG.debug("REST request to get audit metrics summary");
        return auditService.getMetricsSummary().map(ResponseEntity::ok);
    }

    /**
     * {@code GET  /authority-audits/metrics/by-authority/:authorityId} : get metrics for a specific authority.
     *
     * @param authorityId the id of the authority
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the metrics in body.
     */
    @Operation(
        summary = "Obtener métricas de un rol específico",
        description = "Retorna estadísticas de auditoría para un rol en particular, incluyendo total de cambios, distribución por tipo de acción y último cambio. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Métricas del rol obtenidas exitosamente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/metrics/by-authority/{authorityId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> getMetricsByAuthority(
        @Parameter(description = "ID del rol", required = true) @PathVariable("authorityId") Long authorityId
    ) {
        LOG.debug("REST request to get metrics for authority: {}", authorityId);
        return auditService.getMetricsByAuthority(authorityId).map(ResponseEntity::ok);
    }
}
