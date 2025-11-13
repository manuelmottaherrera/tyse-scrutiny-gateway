package com.tyse.scrutiny.gateway.service.authorization;

import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityPermission;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.security.SecurityUtils;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.AuthorityNotFoundException;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.InactiveAuthorityException;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.PermissionDeniedException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing authority-permission mappings.
 *
 * <p>This service handles the assignment and revocation of permissions to/from authorities (roles).
 * It ensures that:
 * <ul>
 *   <li>Only active authorities and permissions can be mapped</li>
 *   <li>System authorities cannot be modified</li>
 *   <li>Duplicate assignments are prevented</li>
 *   <li>All operations are audited with who performed them</li>
 * </ul>
 *
 * <h3>Permission Inheritance Flow:</h3>
 * <pre>
 * User → UserAuthority → Authority → AuthorityPermission → Permission
 *
 * Example:
 * 1. Admin assigns "user.create" permission to ROLE_ADMIN (this service)
 * 2. Admin assigns ROLE_ADMIN to user John (UserAuthorityService)
 * 3. John logs in and gets all permissions from ROLE_ADMIN (DomainUserDetailsService)
 * 4. John can now perform actions protected by "user.create" (EnterprisePermissionEvaluator)
 * </pre>
 */
@Service
@Transactional
public class AuthorityPermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityPermissionService.class);

    private final AuthorityPermissionRepository authorityPermissionRepository;
    private final AuthorityRepository authorityRepository;
    private final PermissionRepository permissionRepository;

    public AuthorityPermissionService(
        AuthorityPermissionRepository authorityPermissionRepository,
        AuthorityRepository authorityRepository,
        PermissionRepository permissionRepository
    ) {
        this.authorityPermissionRepository = authorityPermissionRepository;
        this.authorityRepository = authorityRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Assign a permission to an authority (role).
     *
     * <p>This creates a mapping in scr_authority_permission that grants the permission
     * to all users who have this authority.
     *
     * @param authorityId the ID of the authority (role)
     * @param permissionId the ID of the permission
     * @return the created authority-permission mapping
     * @throws AuthorityNotFoundException if the authority doesn't exist
     * @throws PermissionDeniedException if the permission doesn't exist
     * @throws IllegalStateException if the authority is a system authority
     * @throws IllegalStateException if the mapping already exists
     */
    public Mono<AuthorityPermission> assignPermissionToAuthority(Long authorityId, Long permissionId) {
        LOG.debug("Request to assign permission {} to authority {}", permissionId, authorityId);

        return validateAuthorityAndPermission(authorityId, permissionId)
            .flatMap(validation -> authorityPermissionRepository.existsByAuthorityIdAndPermissionId(authorityId, permissionId))
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(
                        new IllegalStateException(
                            String.format("Permission %d is already assigned to authority %d", permissionId, authorityId)
                        )
                    );
                }

                return SecurityUtils.getCurrentUserLogin()
                    .defaultIfEmpty(Constants.SYSTEM)
                    .flatMap(currentUser -> {
                        AuthorityPermission mapping = new AuthorityPermission();
                        mapping.setAuthorityId(authorityId);
                        mapping.setPermissionId(permissionId);
                        mapping.setGrantedBy(currentUser);
                        mapping.setGrantedDate(Instant.now());

                        return authorityPermissionRepository
                            .save(mapping)
                            .doOnNext(saved ->
                                LOG.info("Permission {} assigned to authority {} by {}", permissionId, authorityId, currentUser)
                            );
                    });
            });
    }

    /**
     * Revoke a permission from an authority (role).
     *
     * <p>This removes the mapping from scr_authority_permission. Users with this authority
     * will no longer inherit this permission.
     *
     * @param authorityId the ID of the authority (role)
     * @param permissionId the ID of the permission
     * @return true if the mapping was deleted, false if it didn't exist
     */
    public Mono<Boolean> revokePermissionFromAuthority(Long authorityId, Long permissionId) {
        LOG.debug("Request to revoke permission {} from authority {}", permissionId, authorityId);

        return validateAuthorityAndPermission(authorityId, permissionId).flatMap(validation ->
            authorityPermissionRepository
                .deleteByAuthorityIdAndPermissionId(authorityId, permissionId)
                .map(deletedCount -> {
                    boolean deleted = deletedCount > 0;
                    if (deleted) {
                        LOG.info("Permission {} revoked from authority {}", permissionId, authorityId);
                    } else {
                        LOG.warn("Attempted to revoke non-existent permission {} from authority {}", permissionId, authorityId);
                    }
                    return deleted;
                })
        );
    }

    /**
     * Get all permissions assigned to an authority (role).
     *
     * <p>Returns the full Permission entities, not just the mapping records.
     * This is useful for displaying what a role can do.
     *
     * @param authorityId the ID of the authority (role)
     * @return flux of permissions
     */
    @Transactional(readOnly = true)
    public Flux<Permission> getPermissionsByAuthority(Long authorityId) {
        LOG.debug("Request to get permissions for authority {}", authorityId);

        return authorityRepository
            .findById(authorityId)
            .switchIfEmpty(Mono.error(new AuthorityNotFoundException(authorityId)))
            .flatMapMany(authority -> permissionRepository.findByAuthorityId(authorityId))
            .doOnComplete(() -> LOG.debug("Retrieved permissions for authority {}", authorityId));
    }

    /**
     * Get all authorities that have a specific permission.
     *
     * <p>Returns the full Authority entities. Useful for impact analysis:
     * "Which roles will be affected if I deactivate this permission?"
     *
     * @param permissionId the ID of the permission
     * @return flux of authorities
     */
    @Transactional(readOnly = true)
    public Flux<Authority> getAuthoritiesByPermission(Long permissionId) {
        LOG.debug("Request to get authorities with permission {}", permissionId);

        return permissionRepository
            .findById(permissionId)
            .switchIfEmpty(Mono.error(new PermissionDeniedException("Permission not found: " + permissionId)))
            .flatMapMany(permission ->
                authorityPermissionRepository
                    .findByPermissionId(permissionId)
                    .flatMap(mapping -> authorityRepository.findById(mapping.getAuthorityId()))
            )
            .doOnComplete(() -> LOG.debug("Retrieved authorities with permission {}", permissionId));
    }

    /**
     * Get all authority-permission mappings for an authority.
     *
     * <p>Returns the junction table records with metadata (who granted, when).
     *
     * @param authorityId the ID of the authority
     * @return flux of authority-permission mappings
     */
    @Transactional(readOnly = true)
    public Flux<AuthorityPermission> getAuthorityPermissionMappings(Long authorityId) {
        LOG.debug("Request to get authority-permission mappings for authority {}", authorityId);

        return authorityRepository
            .findById(authorityId)
            .switchIfEmpty(Mono.error(new AuthorityNotFoundException(authorityId)))
            .flatMapMany(authority -> authorityPermissionRepository.findByAuthorityId(authorityId));
    }

    /**
     * Check if an authority has a specific permission.
     *
     * @param authorityId the ID of the authority
     * @param permissionId the ID of the permission
     * @return true if the authority has the permission, false otherwise
     */
    @Transactional(readOnly = true)
    public Mono<Boolean> hasPermission(Long authorityId, Long permissionId) {
        return authorityPermissionRepository.existsByAuthorityIdAndPermissionId(authorityId, permissionId);
    }

    /**
     * Remove all permissions from an authority.
     *
     * <p>Useful when deactivating or deleting a role. This ensures no orphaned
     * permission mappings remain.
     *
     * @param authorityId the ID of the authority
     * @return number of deleted mappings
     */
    public Mono<Long> removeAllPermissionsFromAuthority(Long authorityId) {
        LOG.debug("Request to remove all permissions from authority {}", authorityId);

        return validateAuthority(authorityId).flatMap(authority ->
            authorityPermissionRepository
                .deleteByAuthorityId(authorityId)
                .doOnNext(deletedCount ->
                    LOG.info("Removed {} permissions from authority {} ({})", deletedCount, authorityId, authority.getCode())
                )
        );
    }

    /**
     * Batch assign multiple permissions to an authority.
     *
     * @param authorityId the ID of the authority
     * @param permissionIds the IDs of the permissions to assign
     * @return flux of created authority-permission mappings
     */
    public Flux<AuthorityPermission> assignMultiplePermissions(Long authorityId, Iterable<Long> permissionIds) {
        LOG.debug("Request to batch assign permissions to authority {}", authorityId);

        return Flux.fromIterable(permissionIds).flatMap(permissionId ->
            assignPermissionToAuthority(authorityId, permissionId).onErrorResume(error -> { // Continue on error (e.g., if one permission is already assigned)
                LOG.warn("Failed to assign permission {} to authority {}: {}", permissionId, authorityId, error.getMessage());
                return Mono.empty();
            })
        );
    }

    /**
     * Validate that authority exists, is active, and is not a system authority.
     */
    private Mono<Authority> validateAuthority(Long authorityId) {
        return authorityRepository
            .findById(authorityId)
            .switchIfEmpty(Mono.error(new AuthorityNotFoundException(authorityId)))
            .flatMap(authority -> {
                if (Boolean.TRUE.equals(authority.getIsSystem())) {
                    return Mono.error(new IllegalStateException("Cannot modify system authority: " + authority.getCode()));
                }
                if (!Boolean.TRUE.equals(authority.getIsActive())) {
                    return Mono.error(new InactiveAuthorityException(authority.getCode()));
                }
                return Mono.just(authority);
            });
    }

    /**
     * Validate that both authority and permission exist and are active.
     */
    private Mono<Boolean> validateAuthorityAndPermission(Long authorityId, Long permissionId) {
        return Mono.zip(
            validateAuthority(authorityId),
            permissionRepository
                .findById(permissionId)
                .switchIfEmpty(Mono.error(new PermissionDeniedException("Permission not found: " + permissionId)))
                .flatMap(permission -> {
                    if (!Boolean.TRUE.equals(permission.getIsActive())) {
                        return Mono.error(new IllegalStateException("Cannot assign inactive permission: " + permission.getName()));
                    }
                    return Mono.just(permission);
                })
        ).map(tuple -> true); // Both valid
    }
}
