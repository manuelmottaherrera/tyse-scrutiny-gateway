package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.service.authorization.PermissionService;
import com.tyse.scrutiny.gateway.service.dto.authorization.PermissionDTO;
import com.tyse.scrutiny.gateway.web.rest.request.CreatePermissionRequest;
import com.tyse.scrutiny.gateway.web.rest.request.UpdatePermissionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing {@link Permission}.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Permission Management", description = "Endpoints para gestionar permisos del sistema")
public class PermissionResource {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionResource.class);

    private final PermissionService permissionService;

    public PermissionResource(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * {@code GET  /permissions} : get all active permissions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of permissions in body.
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Obtener todos los permisos activos", description = "Retorna la lista de permisos activos del sistema")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista de permisos retornada exitosamente") })
    public Flux<PermissionDTO> getAllPermissions() {
        LOG.debug("REST request to get all active Permissions");
        return permissionService.findAllActive().map(PermissionDTO::new);
    }

    /**
     * {@code GET  /permissions/:id} : get the "id" permission.
     *
     * @param id the id of the permission to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the permission, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Obtener permiso por ID", description = "Retorna un permiso específico por su ID")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Permiso encontrado"),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado"),
        }
    )
    public Mono<ResponseEntity<PermissionDTO>> getPermission(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Permission : {}", id);
        return permissionService
            .findById(id)
            .map(PermissionDTO::new)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * {@code GET  /permissions/resource/:resource} : get permissions by resource.
     *
     * @param resource the resource name to filter by.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of permissions in body.
     */
    @GetMapping("/permissions/resource/{resource}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Obtener permisos por recurso", description = "Retorna todos los permisos asociados a un recurso específico")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Permisos retornados exitosamente") })
    public Flux<PermissionDTO> getPermissionsByResource(@PathVariable("resource") String resource) {
        LOG.debug("REST request to get Permissions by resource : {}", resource);
        return permissionService.findByResource(resource).map(PermissionDTO::new);
    }

    /**
     * {@code POST  /permissions} : Create a new permission.
     *
     * @param request the permission to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new permission, or with status {@code 400 (Bad Request)} if the permission has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Crear nuevo permiso",
        description = "Crea un nuevo permiso en el sistema con validación de patrón resource.action"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "201", description = "Permiso creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o permiso ya existe"),
        }
    )
    public Mono<ResponseEntity<PermissionDTO>> createPermission(@Valid @RequestBody CreatePermissionRequest request)
        throws URISyntaxException {
        LOG.debug("REST request to create Permission : {}", request);

        Permission permission = new Permission();
        permission.setResource(request.getResource());
        permission.setAction(request.getAction());
        permission.setName(request.getResource() + "." + request.getAction());
        permission.setDescription(request.getDescription());

        return permissionService
            .createPermission(permission)
            .map(PermissionDTO::new)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/permissions/" + result.getId())).body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /permissions/:id} : Updates an existing permission.
     *
     * @param id the id of the permission to update.
     * @param request the permission to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated permission,
     * or with status {@code 400 (Bad Request)} if the permission is not valid,
     * or with status {@code 404 (Not Found)} if the permission is not found.
     */
    @PutMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Actualizar permiso", description = "Actualiza la descripción y estado de un permiso existente")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Permiso actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado"),
        }
    )
    public Mono<ResponseEntity<PermissionDTO>> updatePermission(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdatePermissionRequest request
    ) {
        LOG.debug("REST request to update Permission : {}, {}", id, request);

        Permission permission = new Permission();
        permission.setDescription(request.getDescription());
        permission.setIsActive(request.getIsActive());

        return permissionService
            .updatePermission(id, permission)
            .map(PermissionDTO::new)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
