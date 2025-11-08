package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.web.rest.errors.BadRequestAlertException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.tyse.scrutiny.gateway.domain.Authority}.
 */
@RestController
@RequestMapping("/api/authorities")
@Transactional
@Tag(name = "Authorities", description = "API para gestionar roles/autoridades - Solo accesible para administradores")
public class AuthorityResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityResource.class);

    private static final String ENTITY_NAME = "adminAuthority";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthorityRepository authorityRepository;
    private final AuthorityPermissionRepository authorityPermissionRepository;
    private final PermissionRepository permissionRepository;

    public AuthorityResource(
        AuthorityRepository authorityRepository,
        AuthorityPermissionRepository authorityPermissionRepository,
        PermissionRepository permissionRepository
    ) {
        this.authorityRepository = authorityRepository;
        this.authorityPermissionRepository = authorityPermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * {@code POST  /authorities} : Create a new authority.
     *
     * @param authority the authority to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authority, or with status {@code 400 (Bad Request)} if the authority has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(
        summary = "Crear nueva autoridad/rol",
        description = "Crea una nueva autoridad (rol) en el sistema. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Autoridad creada exitosamente",
                content = @Content(schema = @Schema(implementation = Authority.class))
            ),
            @ApiResponse(responseCode = "400", description = "La autoridad ya existe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Authority>> createAuthority(
        @Parameter(description = "Autoridad a crear", required = true) @Valid @RequestBody Authority authority
    ) throws URISyntaxException {
        LOG.debug("REST request to save Authority : {}", authority);

        // Validate ROLE_ prefix
        if (authority.getCode() != null && !authority.getCode().startsWith("ROLE_")) {
            return Mono.error(new BadRequestAlertException("Authority code must start with ROLE_ prefix", ENTITY_NAME, "invalidcode"));
        }

        return authorityRepository
            .existsByCode(authority.getCode())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new BadRequestAlertException("authority already exists", ENTITY_NAME, "idexists"));
                }
                return authorityRepository
                    .save(authority)
                    .map(result -> {
                        try {
                            return ResponseEntity.created(new URI("/api/authorities/" + result.getId()))
                                .headers(
                                    HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())
                                )
                                .body(result);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });
            });
    }

    /**
     * {@code GET  /authorities} : get all the authorities.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authorities in body.
     */
    @Operation(
        summary = "Obtener todas las autoridades",
        description = "Retorna la lista completa de autoridades/roles del sistema. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Lista de autoridades obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<List<Authority>> getAllAuthorities() {
        LOG.debug("REST request to get all Authorities");
        return authorityRepository.findAll().collectList();
    }

    /**
     * {@code GET  /authorities} : get all the authorities as a stream.
     * @return the {@link Flux} of authorities.
     */
    @Operation(
        summary = "Obtener autoridades como stream",
        description = "Retorna todas las autoridades como un stream reactivo (NDJSON). Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Stream de autoridades iniciado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Flux<Authority> getAllAuthoritiesAsStream() {
        LOG.debug("REST request to get all Authorities as a stream");
        return authorityRepository.findAll();
    }

    /**
     * {@code GET  /authorities/:id} : get the "id" authority.
     *
     * @param id the id of the authority to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authority, or with status {@code 404 (Not Found)}.
     */
    @Operation(
        summary = "Obtener autoridad por ID",
        description = "Retorna una autoridad específica por su ID. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Autoridad encontrada",
                content = @Content(schema = @Schema(implementation = Authority.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Autoridad no encontrada", content = @Content),
        }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Authority>> getAuthority(
        @Parameter(description = "ID de la autoridad", required = true) @PathVariable("id") Long id
    ) {
        LOG.debug("REST request to get Authority : {}", id);
        Mono<Authority> authority = authorityRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(authority);
    }

    /**
     * {@code GET  /authorities/:id/permissions} : get all permissions for an authority.
     *
     * @param id the id of the authority to get permissions for.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and list of permissions in body (can be empty).
     */
    @Operation(
        summary = "Obtener permisos de una autoridad",
        description = "Retorna la lista de permisos asignados a una autoridad. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Lista de permisos obtenida exitosamente (puede estar vacía)"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Autoridad no encontrada", content = @Content),
        }
    )
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<List<Permission>>> getAuthorityPermissions(
        @Parameter(description = "ID de la autoridad", required = true) @PathVariable("id") Long id
    ) {
        LOG.debug("REST request to get Permissions for Authority : {}", id);

        // First check if authority exists
        return authorityRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.just(ResponseEntity.notFound().build());
                }

                // Get all permission IDs for this authority
                return authorityPermissionRepository
                    .findByAuthorityId(id)
                    .flatMap(ap -> permissionRepository.findById(ap.getPermissionId()))
                    .collectList()
                    .map(permissions -> ResponseEntity.ok().body(permissions));
            });
    }

    /**
     * {@code DELETE  /authorities/:id} : delete the "id" authority.
     *
     * @param id the id of the authority to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(
        summary = "Eliminar autoridad",
        description = "Elimina una autoridad del sistema. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Autoridad eliminada exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Autoridad no encontrada", content = @Content),
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteAuthority(
        @Parameter(description = "ID de la autoridad a eliminar", required = true) @PathVariable("id") Long id
    ) {
        LOG.debug("REST request to delete Authority : {}", id);
        return authorityRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
