package com.tyse.scrutiny.gateway.service.authorization;

import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserPermissionRepository;
import com.tyse.scrutiny.gateway.security.SecurityUtils;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing user permission grants.
 */
@Service
@Transactional
public class UserPermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(UserPermissionService.class);

    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;

    public UserPermissionService(UserPermissionRepository userPermissionRepository, PermissionRepository permissionRepository) {
        this.userPermissionRepository = userPermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Grant a permission to a user.
     *
     * @param userId the user id
     * @param permissionId the permission id
     * @param expiresAt optional expiration date
     * @param reason the reason for granting the permission
     * @return the created grant
     */
    public Mono<UserPermission> grantPermission(Long userId, Long permissionId, Instant expiresAt, String reason) {
        LOG.debug("Request to grant permission {} to user {}", permissionId, userId);

        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(grantedBy -> {
                UserPermission grant = new UserPermission();
                grant.setUserId(userId);
                grant.setPermissionId(permissionId);
                grant.setIsActive(true);
                grant.setExpiresAt(expiresAt);
                grant.setGrantedBy(grantedBy);
                grant.setGrantedDate(Instant.now());
                grant.setReason(reason);

                return userPermissionRepository
                    .save(grant)
                    .doOnNext(saved -> LOG.debug("Granted permission {} to user {} by {}: {}", permissionId, userId, grantedBy, reason));
            });
    }

    /**
     * Revoke a permission from a user.
     *
     * @param userId the user id
     * @param permissionId the permission id
     * @param reason the reason for revocation
     * @return the updated grant
     */
    public Mono<UserPermission> revokePermission(Long userId, Long permissionId, String reason) {
        LOG.debug("Request to revoke permission {} from user {}", permissionId, userId);

        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(revokedBy ->
                userPermissionRepository
                    .findByUserIdAndPermissionId(userId, permissionId)
                    .switchIfEmpty(
                        Mono.error(
                            new IllegalArgumentException(
                                "No active permission grant found for user " + userId + " and permission " + permissionId
                            )
                        )
                    )
                    .flatMap(grant -> {
                        grant.setIsActive(false);
                        grant.setRevokedBy(revokedBy);
                        grant.setRevokedDate(Instant.now());
                        grant.setRevokedReason(reason);

                        return userPermissionRepository
                            .save(grant)
                            .doOnNext(saved ->
                                LOG.debug("Revoked permission {} from user {} by {}: {}", permissionId, userId, revokedBy, reason)
                            );
                    })
            );
    }

    /**
     * Get all valid (active and not expired) permission grants for a user.
     *
     * @param userId the user id
     * @return flux of valid grants
     */
    @Transactional(readOnly = true)
    public Flux<UserPermission> getValidPermissions(Long userId) {
        LOG.debug("Request to get valid permissions for user {}", userId);
        return userPermissionRepository.findValidByUserId(userId);
    }

    /**
     * Check if a user has a specific permission (by name).
     *
     * @param userId the user id
     * @param permissionName the permission name (e.g., "user.create")
     * @return true if user has the permission, false otherwise
     */
    @Transactional(readOnly = true)
    public Mono<Boolean> userHasPermission(Long userId, String permissionName) {
        LOG.debug("Request to check if user {} has permission {}", userId, permissionName);
        return userPermissionRepository.userHasPermissionByName(userId, permissionName);
    }

    /**
     * Get effective permissions for a user (combines role-based and direct permissions).
     *
     * @param userId the user id
     * @return flux of distinct permissions
     */
    @Transactional(readOnly = true)
    public Flux<Permission> getEffectivePermissions(Long userId) {
        LOG.debug("Request to get effective permissions for user {}", userId);

        // Permissions from roles
        Flux<Permission> rolePermissions = permissionRepository.findByUserId(userId);

        // Direct permissions
        Flux<Permission> directPermissions = userPermissionRepository
            .findValidByUserId(userId)
            .flatMap(up -> permissionRepository.findById(up.getPermissionId()));

        // Merge and deduplicate
        return Flux.merge(rolePermissions, directPermissions).distinct(Permission::getId);
    }

    /**
     * Find all expired permission grants.
     *
     * @return flux of expired grants
     */
    @Transactional(readOnly = true)
    public Flux<UserPermission> findExpiredGrants() {
        LOG.debug("Request to find expired permission grants");
        return userPermissionRepository.findExpiredGrants();
    }

    /**
     * Find permission grants expiring within a specified number of days.
     *
     * @param days the number of days
     * @return flux of expiring grants
     */
    @Transactional(readOnly = true)
    public Flux<UserPermission> findExpiringWithinDays(int days) {
        LOG.debug("Request to find permission grants expiring within {} days", days);
        return userPermissionRepository.findExpiringWithinDays(days);
    }

    /**
     * Get all permission grants for a user (including inactive and expired).
     *
     * @param userId the user id
     * @return flux of all grants
     */
    @Transactional(readOnly = true)
    public Flux<UserPermission> getAllPermissions(Long userId) {
        LOG.debug("Request to get all permission grants for user {}", userId);
        return userPermissionRepository.findByUserId(userId);
    }

    /**
     * Delete a permission grant permanently.
     *
     * @param id the grant id
     * @return void
     */
    public Mono<Void> deleteGrant(Long id) {
        LOG.debug("Request to delete permission grant with id: {}", id);
        return userPermissionRepository.deleteById(id).doOnSuccess(v -> LOG.debug("Deleted permission grant with id: {}", id));
    }
}
