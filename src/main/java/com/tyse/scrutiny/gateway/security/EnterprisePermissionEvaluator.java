package com.tyse.scrutiny.gateway.security;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Custom PermissionEvaluator for reactive Spring Security that evaluates
 * granular permissions in @PreAuthorize annotations.
 *
 * <p>This evaluator checks if the requested permission exists in the user's
 * granted authorities, which include both roles (ROLE_*) and granular
 * permissions (resource.action format).
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * @PreAuthorize("hasPermission(null, 'user.create')")
 * public Mono<User> createUser(User user) { ... }
 * }
 * </pre>
 *
 * <p>Note: This evaluator does not query the database on each evaluation.
 * Permissions are already loaded in the Authentication object by
 * {@link DomainUserDetailsService} during login.
 *
 * @see DomainUserDetailsService#createSpringSecurityUser(String, com.tyse.scrutiny.gateway.domain.User)
 */
@Component
public class EnterprisePermissionEvaluator implements PermissionEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(EnterprisePermissionEvaluator.class);

    /**
     * Evaluates if the authenticated user has the specified permission.
     *
     * <p>This method checks if the permission exists in the user's authorities
     * collection. Permissions should be in the format "resource.action"
     * (e.g., "user.create", "invoice.approve").
     *
     * @param authentication the current authentication object
     * @param targetDomainObject not used in this implementation (can be null)
     * @param permission the permission to check (e.g., "user.create")
     * @return true if the user has the permission, false otherwise
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            LOG.debug("Permission evaluation failed: authentication or permission is null");
            return false;
        }

        String permissionString = permission.toString();
        boolean hasPermission = authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(permissionString));

        if (hasPermission) {
            LOG.debug("Permission '{}' granted for user '{}'", permissionString, authentication.getName());
        } else {
            LOG.debug(
                "Permission '{}' denied for user '{}' - available authorities: {}",
                permissionString,
                authentication.getName(),
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
            );
        }

        return hasPermission;
    }

    /**
     * Evaluates if the authenticated user has the specified permission for a target object.
     *
     * <p>This method delegates to {@link #hasPermission(Authentication, Object, Object)}
     * as the current implementation does not use target object filtering.
     *
     * @param authentication the current authentication object
     * @param targetId the identifier of the target object (not used)
     * @param targetType the type of the target object (not used)
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }
}
