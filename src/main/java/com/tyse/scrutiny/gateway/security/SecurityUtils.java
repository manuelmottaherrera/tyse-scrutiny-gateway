package com.tyse.scrutiny.gateway.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    public static final String AUTHORITIES_CLAIM = "auth";

    public static final String USER_ID_CLAIM = "userId";

    private SecurityUtils() {}

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Mono<String> getCurrentUserLogin() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(authentication -> Mono.justOrEmpty(extractPrincipal(authentication)));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Mono<String> getCurrentUserJWT() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(authentication -> authentication.getCredentials() instanceof String)
            .map(authentication -> (String) authentication.getCredentials());
    }

    /**
     * Get the Id of the current user.
     *
     * @return the Id of the current user.
     */
    public static Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(authentication -> authentication.getPrincipal() instanceof ClaimAccessor)
            .map(authentication -> (ClaimAccessor) authentication.getPrincipal())
            .map(principal -> principal.getClaim(USER_ID_CLAIM));
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    public static Mono<Boolean> isAuthenticated() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getAuthorities)
            .map(authorities -> authorities.stream().map(GrantedAuthority::getAuthority).noneMatch(AuthoritiesConstants.ANONYMOUS::equals));
    }

    /**
     * Checks if the current user has any of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has any of the authorities, false otherwise.
     */
    public static Mono<Boolean> hasCurrentUserAnyOfAuthorities(String... authorities) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getAuthorities)
            .map(authorityList ->
                authorityList
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> Arrays.asList(authorities).contains(authority))
            );
    }

    /**
     * Checks if the current user has none of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has none of the authorities, false otherwise.
     */
    public static Mono<Boolean> hasCurrentUserNoneOfAuthorities(String... authorities) {
        return hasCurrentUserAnyOfAuthorities(authorities).map(result -> !result);
    }

    /**
     * Checks if the current user has a specific authority.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    public static Mono<Boolean> hasCurrentUserThisAuthority(String authority) {
        return hasCurrentUserAnyOfAuthorities(authority);
    }

    /**
     * Check if current user has specific permission.
     *
     * <p>Permissions are in the format "resource.action" (e.g., "user.create", "invoice.approve").
     * This method checks if the permission exists in the user's granted authorities.
     *
     * @param permission format "resource.action" (e.g., "user.create")
     * @return true if user has the permission, false otherwise
     */
    public static Mono<Boolean> hasPermission(String permission) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMapIterable(Authentication::getAuthorities)
            .map(GrantedAuthority::getAuthority)
            .any(auth -> auth.equals(permission))
            .defaultIfEmpty(false);
    }

    /**
     * Check if current user has specific authority/role.
     *
     * <p>This is an alias for {@link #hasPermission(String)} to provide
     * consistent API for checking both roles and permissions.
     *
     * @param authority format "ROLE_XXX"
     * @return true if user has the authority, false otherwise
     */
    public static Mono<Boolean> hasAuthority(String authority) {
        return hasPermission(authority);
    }

    /**
     * Get all permissions of current user (only permissions, not roles).
     *
     * <p>Permissions are identified by containing a dot (.) in the authority string,
     * following the format "resource.action" (e.g., "user.create", "invoice.approve").
     *
     * @return set of permissions in format "resource.action"
     */
    public static Mono<Set<String>> getCurrentUserPermissions() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMapIterable(Authentication::getAuthorities)
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.contains(".")) // Solo permisos (formato "resource.action")
            .collect(Collectors.toSet());
    }

    /**
     * Get all authorities of current user (only roles, not permissions).
     *
     * <p>Authorities are identified by starting with "ROLE_" prefix,
     * following Spring Security conventions (e.g., "ROLE_ADMIN", "ROLE_USER").
     *
     * @return set of authorities in format "ROLE_XXX"
     */
    public static Mono<Set<String>> getCurrentUserAuthorities() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMapIterable(Authentication::getAuthorities)
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_")) // Solo roles
            .collect(Collectors.toSet());
    }
}
