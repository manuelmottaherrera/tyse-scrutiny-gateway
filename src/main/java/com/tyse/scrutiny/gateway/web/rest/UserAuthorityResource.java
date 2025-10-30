package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import com.tyse.scrutiny.gateway.service.authorization.UserAuthorityService;
import com.tyse.scrutiny.gateway.web.rest.request.AssignAuthorityRequest;
import com.tyse.scrutiny.gateway.web.rest.request.RevokeAuthorityRequest;
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
 * REST controller for managing user authority assignments.
 * Provides endpoints for assigning and revoking roles/authorities to users.
 */
@RestController
@RequestMapping("/api/user-authorities")
@Tag(name = "User Authorities", description = "Endpoints for managing user-authority assignments")
public class UserAuthorityResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserAuthorityResource.class);

    private final UserAuthorityService userAuthorityService;

    public UserAuthorityResource(UserAuthorityService userAuthorityService) {
        this.userAuthorityService = userAuthorityService;
    }

    /**
     * GET /api/user-authorities/user/{userId} : Get all authority assignments for a user.
     *
     * @param userId the user ID
     * @return the list of authority assignments
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get all authorities for a user",
        description = "Retrieve all authority assignments (active and inactive) for a specific user"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "List of authority assignments"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Flux<UserAuthority> getAllAuthoritiesForUser(@Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        LOG.debug("REST request to get all authority assignments for user: {}", userId);
        return userAuthorityService.getAllAuthorities(userId);
    }

    /**
     * GET /api/user-authorities/user/{userId}/valid : Get valid authority assignments for a user.
     *
     * @param userId the user ID
     * @return the list of valid (active and not expired) authority assignments
     */
    @GetMapping("/user/{userId}/valid")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get valid authorities for a user",
        description = "Retrieve only valid (active and not expired) authority assignments for a specific user"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "List of valid authority assignments"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Flux<UserAuthority> getValidAuthoritiesForUser(@Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        LOG.debug("REST request to get valid authority assignments for user: {}", userId);
        return userAuthorityService.getValidAuthorities(userId);
    }

    /**
     * GET /api/user-authorities/expiring : Get authority assignments expiring soon.
     *
     * @param days number of days to look ahead (default 7)
     * @return the list of expiring assignments
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get expiring authorities",
        description = "Retrieve authority assignments that will expire within the specified number of days"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "List of expiring authority assignments"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Flux<UserAuthority> getExpiringAuthorities(
        @Parameter(description = "Number of days to look ahead", example = "7") @RequestParam(defaultValue = "7") int days
    ) {
        LOG.debug("REST request to get authority assignments expiring within {} days", days);
        return userAuthorityService.findExpiringWithinDays(days);
    }

    /**
     * POST /api/user-authorities : Assign an authority to a user.
     *
     * @param request the assignment request
     * @return the created assignment
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Assign authority to user", description = "Assign a role/authority to a user with optional expiration date")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "Authority assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public Mono<ResponseEntity<UserAuthority>> assignAuthority(@Valid @RequestBody AssignAuthorityRequest request) {
        LOG.debug("REST request to assign authority: {}", request);

        return userAuthorityService
            .assignAuthority(request.getUserId(), request.getAuthorityId(), request.getExpiresAt())
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/user-authorities/" + result.getId())).body(result);
                } catch (URISyntaxException e) {
                    LOG.error("Error creating URI for new authority assignment", e);
                    return ResponseEntity.status(HttpStatus.CREATED).body(result);
                }
            });
    }

    /**
     * DELETE /api/user-authorities/{id} : Revoke an authority assignment.
     *
     * @param id the assignment ID
     * @param request the revocation request with reason
     * @return the revoked assignment
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Revoke authority assignment", description = "Revoke an authority assignment from a user with a reason")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Authority revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
        }
    )
    public Mono<ResponseEntity<UserAuthority>> revokeAuthority(
        @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
        @Valid @RequestBody RevokeAuthorityRequest request
    ) {
        LOG.debug("REST request to revoke authority assignment {}: {}", id, request);

        return userAuthorityService
            .revokeById(id, request.getReason())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
