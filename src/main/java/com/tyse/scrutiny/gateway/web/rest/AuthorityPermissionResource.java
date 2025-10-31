package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.service.authorization.AuthorityPermissionService;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuthorityPermissionDTO;
import com.tyse.scrutiny.gateway.service.dto.authorization.PermissionDTO;
import com.tyse.scrutiny.gateway.web.rest.request.AssignPermissionToAuthorityRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing authority-permission mappings.
 *
 * <p>This controller provides endpoints to:
 * <ul>
 *   <li>Assign permissions to authorities (roles)</li>
 *   <li>Revoke permissions from authorities</li>
 *   <li>Query which permissions a role has</li>
 *   <li>Query which roles have a permission</li>
 * </ul>
 *
 * <p><strong>Security:</strong> All endpoints require ROLE_ADMIN authority.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Authority-Permission Management", description = "Endpoints para gestionar la asignación de permisos a roles")
public class AuthorityPermissionResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityPermissionResource.class);

    private final AuthorityPermissionService authorityPermissionService;

    public AuthorityPermissionResource(AuthorityPermissionService authorityPermissionService) {
        this.authorityPermissionService = authorityPermissionService;
    }

    /**
     * {@code POST  /authority-permissions} : Assign a permission to an authority (role).
     *
     * @param request the assignment request containing authorityId and permissionId
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new mapping
     */
    @PostMapping("/authority-permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Asignar permiso a rol",
        description = "Crea una asignación que otorga un permiso específico a todos los usuarios con el rol especificado"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "201", description = "Permiso asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o asignación ya existe"),
            @ApiResponse(responseCode = "404", description = "Rol o permiso no encontrado"),
        }
    )
    public Mono<ResponseEntity<AuthorityPermissionDTO>> assignPermissionToAuthority(
        @Valid @RequestBody AssignPermissionToAuthorityRequest request
    ) {
        LOG.debug("REST request to assign permission {} to authority {}", request.getPermissionId(), request.getAuthorityId());

        return authorityPermissionService
            .assignPermissionToAuthority(request.getAuthorityId(), request.getPermissionId())
            .map(AuthorityPermissionDTO::new)
            .map(dto -> {
                try {
                    return ResponseEntity.created(new URI("/api/authority-permissions/" + dto.getId())).body(dto);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
                }
            });
    }

    /**
     * {@code DELETE  /authority-permissions/authority/:authorityId/permission/:permissionId} :
     * Revoke a permission from an authority (role).
     *
     * @param authorityId the ID of the authority
     * @param permissionId the ID of the permission
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)} if the mapping was deleted
     */
    @DeleteMapping("/authority-permissions/authority/{authorityId}/permission/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Revocar permiso de rol",
        description = "Elimina la asignación entre un rol y un permiso. Los usuarios con ese rol ya no tendrán el permiso."
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "204", description = "Permiso revocado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Rol o permiso no encontrado"),
        }
    )
    public Mono<ResponseEntity<Void>> revokePermissionFromAuthority(
        @PathVariable("authorityId") Long authorityId,
        @PathVariable("permissionId") Long permissionId
    ) {
        LOG.debug("REST request to revoke permission {} from authority {}", permissionId, authorityId);

        return authorityPermissionService
            .revokePermissionFromAuthority(authorityId, permissionId)
            .map(deleted -> {
                if (deleted) {
                    return ResponseEntity.noContent().<Void>build();
                } else {
                    return ResponseEntity.notFound().<Void>build();
                }
            });
    }

    /**
     * {@code GET  /authority-permissions/authority/:authorityId/permissions} :
     * Get all permissions assigned to an authority (role).
     *
     * @param authorityId the ID of the authority
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of permissions in body
     */
    @GetMapping("/authority-permissions/authority/{authorityId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Obtener permisos de un rol", description = "Retorna todos los permisos asignados a un rol específico")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Permisos retornados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
        }
    )
    public Flux<PermissionDTO> getPermissionsByAuthority(@PathVariable("authorityId") Long authorityId) {
        LOG.debug("REST request to get permissions for authority : {}", authorityId);
        return authorityPermissionService.getPermissionsByAuthority(authorityId).map(PermissionDTO::new);
    }

    /**
     * {@code GET  /authority-permissions/authority/:authorityId/mappings} :
     * Get all authority-permission mapping records for an authority.
     *
     * @param authorityId the ID of the authority
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of mappings in body
     */
    @GetMapping("/authority-permissions/authority/{authorityId}/mappings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Obtener mapeos de permisos de un rol",
        description = "Retorna todos los registros de asignación (con metadatos) para un rol específico"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Mapeos retornados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
        }
    )
    public Flux<AuthorityPermissionDTO> getAuthorityPermissionMappings(@PathVariable("authorityId") Long authorityId) {
        LOG.debug("REST request to get authority-permission mappings for authority : {}", authorityId);
        return authorityPermissionService.getAuthorityPermissionMappings(authorityId).map(AuthorityPermissionDTO::new);
    }

    /**
     * {@code GET  /authority-permissions/authority/:authorityId/permission/:permissionId/exists} :
     * Check if an authority has a specific permission.
     *
     * @param authorityId the ID of the authority
     * @param permissionId the ID of the permission
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and body true/false
     */
    @GetMapping("/authority-permissions/authority/{authorityId}/permission/{permissionId}/exists")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Verificar si rol tiene permiso",
        description = "Verifica si un rol específico tiene asignado un permiso específico"
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Verificación exitosa") })
    public Mono<ResponseEntity<Boolean>> hasPermission(
        @PathVariable("authorityId") Long authorityId,
        @PathVariable("permissionId") Long permissionId
    ) {
        LOG.debug("REST request to check if authority {} has permission {}", authorityId, permissionId);
        return authorityPermissionService.hasPermission(authorityId, permissionId).map(ResponseEntity::ok);
    }

    /**
     * {@code DELETE  /authority-permissions/authority/:authorityId} :
     * Remove all permissions from an authority (role).
     *
     * @param authorityId the ID of the authority
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)}
     */
    @DeleteMapping("/authority-permissions/authority/{authorityId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Remover todos los permisos de un rol",
        description = "Elimina todas las asignaciones de permisos para un rol específico"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "204", description = "Permisos removidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
        }
    )
    public Mono<ResponseEntity<Void>> removeAllPermissionsFromAuthority(@PathVariable("authorityId") Long authorityId) {
        LOG.debug("REST request to remove all permissions from authority : {}", authorityId);
        return authorityPermissionService
            .removeAllPermissionsFromAuthority(authorityId)
            .map(count -> ResponseEntity.noContent().<Void>build());
    }
}
