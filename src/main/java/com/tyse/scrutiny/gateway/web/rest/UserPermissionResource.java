package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import com.tyse.scrutiny.gateway.service.authorization.UserPermissionService;
import com.tyse.scrutiny.gateway.web.rest.request.GrantPermissionRequest;
import com.tyse.scrutiny.gateway.web.rest.request.RevokePermissionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * REST controller for managing user permission grants.
 * Provides endpoints for granting and revoking direct permissions to users.
 */
@RestController
@RequestMapping("/api/user-permissions")
@Tag(name = "User Permissions", description = "Endpoints for managing direct user-permission grants")
public class UserPermissionResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserPermissionResource.class);

    private final UserPermissionService userPermissionService;

    public UserPermissionResource(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    /**
     * GET /api/user-permissions/user/{userId} : Get all direct permission grants for a user.
     *
     * @param userId the user ID
     * @return the list of direct permission grants
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get direct permissions for a user",
        description = "Retrieve all direct permission grants (active and inactive) for a specific user"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "List of direct permission grants"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Flux<UserPermission> getDirectPermissionsForUser(
        @Parameter(description = "User ID", required = true) @PathVariable Long userId
    ) {
        LOG.debug("REST request to get direct permission grants for user: {}", userId);
        return userPermissionService.getAllPermissions(userId);
    }

    /**
     * GET /api/user-permissions/user/{userId}/effective : Get effective permissions for a user.
     *
     * @param userId the user ID
     * @return the list of effective permissions (from roles and direct grants)
     */
    @GetMapping("/user/{userId}/effective")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get effective permissions for a user",
        description = "Retrieve all effective permissions for a user, combining role-based and direct permission grants"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "List of effective permissions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Flux<Permission> getEffectivePermissionsForUser(@Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        LOG.debug("REST request to get effective permissions for user: {}", userId);
        return userPermissionService.getEffectivePermissions(userId);
    }

    /**
     * POST /api/user-permissions : Grant a direct permission to a user.
     *
     * @param request the grant request
     * @return the created grant
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Grant permission to user",
        description = "Grant a direct permission to a user with optional expiration date and mandatory reason"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "Permission granted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Mono<ResponseEntity<UserPermission>> grantPermission(@Valid @RequestBody GrantPermissionRequest request) {
        LOG.debug("REST request to grant permission: {}", request);

        return userPermissionService
            .grantPermission(request.getUserId(), request.getPermissionId(), request.getExpiresAt(), request.getReason())
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/user-permissions/" + result.getId())).body(result);
                } catch (URISyntaxException e) {
                    LOG.error("Error creating URI for new permission grant", e);
                    return ResponseEntity.status(HttpStatus.CREATED).body(result);
                }
            });
    }

    /**
     * DELETE /api/user-permissions/{id} : Revoke a permission grant.
     *
     * @param id the grant ID
     * @param request the revocation request with reason
     * @return the revoked grant
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Revoke permission grant", description = "Revoke a direct permission grant from a user with a reason")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Permission revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Grant not found"),
        }
    )
    public Mono<ResponseEntity<UserPermission>> revokePermission(
        @Parameter(description = "Grant ID", required = true) @PathVariable Long id,
        @Valid @RequestBody RevokePermissionRequest request
    ) {
        LOG.debug("REST request to revoke permission grant {}: {}", id, request);

        return userPermissionService
            .revokeById(id, request.getReason())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/user-permissions/expiring : Get permission grants expiring soon.
     *
     * @param days number of days to look ahead (default 7)
     * @return the list of expiring grants
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get expiring permission grants",
        description = "Retrieve permission grants that will expire within the specified number of days"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "List of expiring permission grants"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Flux<UserPermission> getExpiringPermissions(
        @Parameter(description = "Number of days to look ahead", example = "7") @RequestParam(defaultValue = "7") int days
    ) {
        LOG.debug("REST request to get permission grants expiring within {} days", days);
        return userPermissionService.findExpiringWithinDays(days);
    }
}
