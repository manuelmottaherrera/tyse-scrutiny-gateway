package com.tyse.scrutiny.gateway.service.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.AuthorityAlreadyExistsException;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing permissions.
 */
@Service
@Transactional
public class PermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionService.class);

    // Pattern for resource: lowercase alphanumeric + underscore
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    // Valid actions
    private static final String[] VALID_ACTIONS = { "create", "read", "update", "delete", "execute", "manage", "list" };

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Create a new permission.
     *
     * @param permission the permission to create
     * @return the created permission
     */
    public Mono<Permission> createPermission(Permission permission) {
        LOG.debug("Request to create Permission: {}", permission);

        // Validate pattern
        if (!isValidPermissionPattern(permission.getResource(), permission.getAction())) {
            return Mono.error(
                new IllegalArgumentException(
                    "Invalid permission pattern. Resource must be lowercase alphanumeric+underscore, action must be valid"
                )
            );
        }

        // Ensure name is in format resource.action
        String expectedName = permission.getResource() + "." + permission.getAction();
        if (!expectedName.equals(permission.getName())) {
            LOG.warn("Permission name mismatch. Expected: {}, Got: {}. Setting to expected value.", expectedName, permission.getName());
            permission.setName(expectedName);
        }

        // Check if already exists
        return permissionRepository
            .existsByName(permission.getName())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(
                        new AuthorityAlreadyExistsException("Permission with name '" + permission.getName() + "' already exists")
                    );
                }

                // Set default values
                if (permission.getIsActive() == null) {
                    permission.setIsActive(true);
                }

                return permissionRepository
                    .save(permission)
                    .doOnNext(savedPermission -> LOG.debug("Created permission: {}", savedPermission));
            });
    }

    /**
     * Update an existing permission.
     *
     * @param id the id of the permission to update
     * @param permission the permission with updated data
     * @return the updated permission
     */
    public Mono<Permission> updatePermission(Long id, Permission permission) {
        LOG.debug("Request to update Permission with id: {}", id);

        return permissionRepository
            .findById(id)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Permission not found with id: " + id)))
            .flatMap(existingPermission -> {
                // Update fields
                existingPermission.setDescription(permission.getDescription());

                if (permission.getIsActive() != null) {
                    existingPermission.setIsActive(permission.getIsActive());
                }

                // Note: resource, action, and name should not be changed
                // as they define the permission's identity

                return permissionRepository
                    .save(existingPermission)
                    .doOnNext(updatedPermission -> LOG.debug("Updated permission: {}", updatedPermission));
            });
    }

    /**
     * Find permissions by resource.
     *
     * @param resource the resource name
     * @return flux of permissions for the resource
     */
    @Transactional(readOnly = true)
    public Flux<Permission> findByResource(String resource) {
        LOG.debug("Request to get Permissions by resource: {}", resource);
        return permissionRepository.findByResourceAndIsActiveTrue(resource);
    }

    /**
     * Find a permission by resource and action.
     *
     * @param resource the resource name
     * @param action the action name
     * @return the permission if found
     */
    @Transactional(readOnly = true)
    public Mono<Permission> findByResourceAndAction(String resource, String action) {
        LOG.debug("Request to get Permission by resource: {} and action: {}", resource, action);
        return permissionRepository.findByResourceAndAction(resource, action);
    }

    /**
     * Get all active permissions.
     *
     * @return flux of active permissions
     */
    @Transactional(readOnly = true)
    public Flux<Permission> findAllActive() {
        LOG.debug("Request to get all active Permissions");
        return permissionRepository.findByIsActiveTrue();
    }

    /**
     * Validate permission pattern.
     *
     * @param resource the resource name
     * @param action the action name
     * @return true if valid, false otherwise
     */
    private boolean isValidPermissionPattern(String resource, String action) {
        if (resource == null || action == null) {
            return false;
        }

        // Validate resource pattern
        if (!RESOURCE_PATTERN.matcher(resource).matches()) {
            return false;
        }

        // Validate action
        for (String validAction : VALID_ACTIONS) {
            if (validAction.equals(action)) {
                return true;
            }
        }

        return false;
    }
}
