package com.tyse.scrutiny.gateway.service.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityPermission;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.EntityManager;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for {@link UserPermissionService}.
 *
 * <p>These tests focus on the getEffectivePermissions() method which combines:
 * - Role-based permissions (user -> user_authority -> authority_permission -> permission)
 * - Direct permissions (user -> user_permission -> permission)
 */
@IntegrationTest
@WithMockUser(username = "test-admin")
class UserPermissionServiceIT {

    @Autowired
    private UserPermissionService userPermissionService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Authority testAuthority;
    private Permission rolePermission;
    private Permission directPermission;
    private Permission sharedPermission;

    @BeforeEach
    void setUp() {
        // Clean up
        entityManager.deleteAllPermissions().block();
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user
        testUser = createTestUser("testuser_perms_" + System.currentTimeMillis());

        // Create test authority
        testAuthority = createAuthority("ROLE_TEST_PERMS", "Test Permissions Role");

        // Create permissions
        rolePermission = createPermission("module.read", "module", "read", "Read module via role");
        directPermission = createPermission("special.access", "special", "access", "Direct access permission");
        sharedPermission = createPermission("shared.view", "shared", "view", "Shared permission");
    }

    @AfterEach
    void cleanup() {
        try {
            entityManager.deleteAllPermissions().block();
            entityManager.deleteAllAuthorities().block();
            userRepository.deleteAll().block();
        } catch (Exception e) {
            System.err.println("Error during test cleanup: " + e.getMessage());
        }
    }

    // ========== Tests for getEffectivePermissions() ==========

    @Test
    void shouldGetEffectivePermissionsFromRoles() {
        // Given: User has a role with permissions
        createAuthorityPermission(testAuthority.getId(), rolePermission.getId());
        createUserAuthority(testUser.getId(), testAuthority.getId(), true, null);

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: Role permissions are returned
        assertThat(permissions).hasSize(1);
        assertThat(permissions).extracting(Permission::getName).containsExactly("module.read");
    }

    @Test
    void shouldGetEffectivePermissionsFromDirectGrants() {
        // Given: User has a direct permission grant (no role)
        userPermissionService.grantPermission(testUser.getId(), directPermission.getId(), null, "Test direct grant").block();

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: Direct permissions are returned
        assertThat(permissions).hasSize(1);
        assertThat(permissions).extracting(Permission::getName).containsExactly("special.access");
    }

    @Test
    void shouldCombineRoleAndDirectPermissions() {
        // Given: User has both role-based and direct permissions
        // Role permission
        createAuthorityPermission(testAuthority.getId(), rolePermission.getId());
        createUserAuthority(testUser.getId(), testAuthority.getId(), true, null);

        // Direct permission
        userPermissionService.grantPermission(testUser.getId(), directPermission.getId(), null, "Direct grant").block();

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: Both types of permissions are combined
        assertThat(permissions).hasSize(2);
        Set<String> permissionNames = permissions.stream().map(Permission::getName).collect(Collectors.toSet());
        assertThat(permissionNames).containsExactlyInAnyOrder("module.read", "special.access");
    }

    @Test
    void shouldDeduplicatePermissionsFromMultipleSources() {
        // Given: Same permission granted via role AND directly
        // Role permission
        createAuthorityPermission(testAuthority.getId(), sharedPermission.getId());
        createUserAuthority(testUser.getId(), testAuthority.getId(), true, null);

        // Same permission granted directly
        userPermissionService.grantPermission(testUser.getId(), sharedPermission.getId(), null, "Redundant grant").block();

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: Permission is not duplicated (DISTINCT)
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getName()).isEqualTo("shared.view");
    }

    @Test
    void shouldReturnEmptyForUserWithNoPermissions() {
        // Given: User with no roles or direct permissions
        // (testUser exists but has nothing assigned)

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: Empty list returned
        assertThat(permissions).isEmpty();
    }

    @Test
    void shouldIgnoreInactiveUserAuthority() {
        // Given: User has inactive role assignment
        createAuthorityPermission(testAuthority.getId(), rolePermission.getId());
        createUserAuthority(testUser.getId(), testAuthority.getId(), false, null); // INACTIVE

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: No permissions returned (role is inactive)
        assertThat(permissions).isEmpty();
    }

    @Test
    void shouldIgnoreExpiredUserAuthority() {
        // Given: User has expired role assignment
        createAuthorityPermission(testAuthority.getId(), rolePermission.getId());
        createUserAuthority(testUser.getId(), testAuthority.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS)); // EXPIRED

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: No permissions returned (role is expired)
        assertThat(permissions).isEmpty();
    }

    @Test
    void shouldIgnoreInactiveDirectPermissionGrant() {
        // Given: User has a direct permission grant that will be revoked
        userPermissionService.grantPermission(testUser.getId(), directPermission.getId(), null, "Test").block();

        // Revoke the permission (makes it inactive)
        userPermissionService.revokePermission(testUser.getId(), directPermission.getId(), "Testing inactive").block();

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: No permissions returned (grant is inactive)
        assertThat(permissions).isEmpty();
    }

    @Test
    void shouldIgnoreExpiredDirectPermissionGrant() {
        // Given: User has expired direct permission grant
        userPermissionService
            .grantPermission(testUser.getId(), directPermission.getId(), Instant.now().minus(1, ChronoUnit.DAYS), "Expired grant")
            .block();

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: No permissions returned (grant is expired)
        assertThat(permissions).isEmpty();
    }

    @Test
    void shouldGetEffectivePermissionsFromMultipleRoles() {
        // Given: User has multiple roles with different permissions
        Permission adminPerm = createPermission("admin.manage", "admin", "manage", "Admin management");
        Permission userPerm = createPermission("user.view", "user", "view", "User view");

        Authority adminRole = createAuthority("ROLE_ADMIN_TEST", "Admin Test");
        Authority userRole = createAuthority("ROLE_USER_TEST", "User Test");

        createAuthorityPermission(adminRole.getId(), adminPerm.getId());
        createAuthorityPermission(userRole.getId(), userPerm.getId());

        createUserAuthority(testUser.getId(), adminRole.getId(), true, null);
        createUserAuthority(testUser.getId(), userRole.getId(), true, null);

        // When: Get effective permissions
        List<Permission> permissions = userPermissionService.getEffectivePermissions(testUser.getId()).collectList().block();

        // Then: Permissions from both roles are returned
        assertThat(permissions).hasSize(2);
        Set<String> permissionNames = permissions.stream().map(Permission::getName).collect(Collectors.toSet());
        assertThat(permissionNames).containsExactlyInAnyOrder("admin.manage", "user.view");
    }

    // ========== Tests for grantPermission() ==========

    @Test
    void shouldGrantPermissionToUser() {
        // When: Grant permission
        UserPermission grant = userPermissionService
            .grantPermission(testUser.getId(), directPermission.getId(), null, "Test reason")
            .block();

        // Then: Grant created successfully
        assertThat(grant).isNotNull();
        assertThat(grant.getId()).isNotNull();
        assertThat(grant.getUserId()).isEqualTo(testUser.getId());
        assertThat(grant.getPermissionId()).isEqualTo(directPermission.getId());
        assertThat(grant.getIsActive()).isTrue();
        assertThat(grant.getGrantedBy()).isEqualTo("test-admin");
        assertThat(grant.getReason()).isEqualTo("Test reason");
    }

    @Test
    void shouldGrantPermissionWithExpiration() {
        // Given: Expiration in the future
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        // When: Grant permission with expiration
        UserPermission grant = userPermissionService
            .grantPermission(testUser.getId(), directPermission.getId(), expiresAt, "Temporary access")
            .block();

        // Then: Grant has expiration set
        assertThat(grant).isNotNull();
        assertThat(grant.getExpiresAt()).isEqualTo(expiresAt);
    }

    // ========== Tests for revokePermission() ==========

    @Test
    void shouldRevokePermission() {
        // Given: An active permission grant
        userPermissionService.grantPermission(testUser.getId(), directPermission.getId(), null, "Test").block();

        // When: Revoke permission
        UserPermission revoked = userPermissionService
            .revokePermission(testUser.getId(), directPermission.getId(), "No longer needed")
            .block();

        // Then: Grant is revoked
        assertThat(revoked).isNotNull();
        assertThat(revoked.getIsActive()).isFalse();
        assertThat(revoked.getRevokedBy()).isEqualTo("test-admin");
        assertThat(revoked.getRevokedDate()).isNotNull();
        assertThat(revoked.getRevokedReason()).isEqualTo("No longer needed");
    }

    @Test
    void shouldFailToRevokeNonExistentPermission() {
        // When/Then: Try to revoke permission that was never granted
        assertThatThrownBy(() -> userPermissionService.revokePermission(testUser.getId(), directPermission.getId(), "reason").block())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No active permission grant found");
    }

    // ========== Tests for userHasPermission() ==========

    @Test
    void shouldCheckUserHasDirectPermission() {
        // Given: User has direct permission
        userPermissionService.grantPermission(testUser.getId(), directPermission.getId(), null, "Test").block();

        // When: Check permission
        Boolean hasPermission = userPermissionService.userHasPermission(testUser.getId(), "special.access").block();

        // Then: Returns true
        assertThat(hasPermission).isTrue();
    }

    @Test
    void shouldReturnFalseForUnassignedPermission() {
        // When: Check permission that was never assigned
        Boolean hasPermission = userPermissionService.userHasPermission(testUser.getId(), "nonexistent.permission").block();

        // Then: Returns false
        assertThat(hasPermission).isFalse();
    }

    // ========== Helper Methods ==========

    private User createTestUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(login + "@localhost");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        return userRepository.save(user).block();
    }

    private Authority createAuthority(String code, String name) {
        Authority authority = new Authority();
        authority.setCode(code);
        authority.setName(name);
        authority.setDescription(name + " for testing");
        authority.setCategory(AuthorityCategory.SYSTEM);
        authority.setIsSystem(false);
        authority.setIsActive(true);
        authority.setHierarchyLevel(0);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());
        return authorityRepository.save(authority).block();
    }

    private Permission createPermission(String name, String resource, String action, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setResource(resource);
        permission.setAction(action);
        permission.setDescription(description);
        permission.setIsActive(true);
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());
        return permissionRepository.save(permission).block();
    }

    private AuthorityPermission createAuthorityPermission(Long authorityId, Long permissionId) {
        AuthorityPermission ap = new AuthorityPermission();
        ap.setAuthorityId(authorityId);
        ap.setPermissionId(permissionId);
        ap.setGrantedBy(Constants.SYSTEM);
        ap.setGrantedDate(Instant.now());
        return authorityPermissionRepository.save(ap).block();
    }

    private UserAuthority createUserAuthority(Long userId, Long authorityId, boolean isActive, Instant expiresAt) {
        UserAuthority ua = new UserAuthority();
        ua.setUserId(userId);
        ua.setAuthorityId(authorityId);
        ua.setIsActive(isActive);
        ua.setExpiresAt(expiresAt);
        ua.setAssignedBy(Constants.SYSTEM);
        ua.setAssignedDate(Instant.now());
        return userAuthorityRepository.save(ua).block();
    }
}
