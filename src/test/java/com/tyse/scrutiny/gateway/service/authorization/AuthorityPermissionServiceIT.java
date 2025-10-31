package com.tyse.scrutiny.gateway.service.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link AuthorityPermissionService}.
 */
@IntegrationTest
class AuthorityPermissionServiceIT {

    @Autowired
    private AuthorityPermissionService authorityPermissionService;

    @Autowired
    private AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Authority adminAuthority;
    private Authority userAuthority;
    private Authority systemAuthority;
    private Permission createPermission;
    private Permission readPermission;
    private Permission updatePermission;
    private Permission inactivePermission;

    @BeforeEach
    void setUp() {
        authorityPermissionRepository.deleteAll().block();
        permissionRepository.deleteAll().block();
        authorityRepository.deleteAll().block();

        // Create authorities
        adminAuthority = createAuthority("ROLE_ADMIN_TEST", "Admin", false, true, 0);
        userAuthority = createAuthority("ROLE_USER_TEST", "User", false, true, 500);
        systemAuthority = createAuthority("ROLE_SYSTEM", "System", true, true, 0); // System authority

        // Create permissions
        createPermission = createPermission("user", "create", "Create users", true);
        readPermission = createPermission("user", "read", "Read users", true);
        updatePermission = createPermission("user", "update", "Update users", true);
        inactivePermission = createPermission("user", "delete", "Delete users", false); // Inactive
    }

    @Test
    void shouldAssignPermissionToAuthority() {
        StepVerifier.create(authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()))
            .assertNext(mapping -> {
                assertThat(mapping.getId()).isNotNull();
                assertThat(mapping.getAuthorityId()).isEqualTo(adminAuthority.getId());
                assertThat(mapping.getPermissionId()).isEqualTo(createPermission.getId());
                assertThat(mapping.getGrantedBy()).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    void shouldFailToAssignDuplicatePermission() {
        // Given: permission already assigned
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()).block();

        // When/Then: assigning again fails
        StepVerifier.create(authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void shouldFailToAssignToSystemAuthority() {
        StepVerifier.create(authorityPermissionService.assignPermissionToAuthority(systemAuthority.getId(), createPermission.getId()))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void shouldFailToAssignInactivePermission() {
        StepVerifier.create(authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), inactivePermission.getId()))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void shouldRevokePermissionFromAuthority() {
        // Given: permission assigned
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()).block();

        // When: revoke permission
        StepVerifier.create(authorityPermissionService.revokePermissionFromAuthority(adminAuthority.getId(), createPermission.getId()))
            .assertNext(deleted -> {
                assertThat(deleted).isTrue();
            })
            .verifyComplete();

        // Then: permission no longer assigned
        StepVerifier.create(authorityPermissionService.hasPermission(adminAuthority.getId(), createPermission.getId()))
            .assertNext(has -> {
                assertThat(has).isFalse();
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenRevokingNonExistent() {
        StepVerifier.create(authorityPermissionService.revokePermissionFromAuthority(adminAuthority.getId(), createPermission.getId()))
            .assertNext(deleted -> {
                assertThat(deleted).isFalse();
            })
            .verifyComplete();
    }

    @Test
    void shouldGetPermissionsByAuthority() {
        // Given: authority with 3 permissions
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()).block();
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), readPermission.getId()).block();
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), updatePermission.getId()).block();

        // When: get permissions
        StepVerifier.create(authorityPermissionService.getPermissionsByAuthority(adminAuthority.getId()))
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void shouldGetAuthoritiesByPermission() {
        // Given: 2 authorities with same permission
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), readPermission.getId()).block();
        authorityPermissionService.assignPermissionToAuthority(userAuthority.getId(), readPermission.getId()).block();

        // When: get authorities
        StepVerifier.create(authorityPermissionService.getAuthoritiesByPermission(readPermission.getId()))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldGetAuthorityPermissionMappings() {
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()).block();
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), readPermission.getId()).block();

        StepVerifier.create(authorityPermissionService.getAuthorityPermissionMappings(adminAuthority.getId()))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldCheckHasPermission() {
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()).block();

        StepVerifier.create(authorityPermissionService.hasPermission(adminAuthority.getId(), createPermission.getId()))
            .assertNext(has -> {
                assertThat(has).isTrue();
            })
            .verifyComplete();

        StepVerifier.create(authorityPermissionService.hasPermission(adminAuthority.getId(), updatePermission.getId()))
            .assertNext(has -> {
                assertThat(has).isFalse();
            })
            .verifyComplete();
    }

    @Test
    void shouldRemoveAllPermissionsFromAuthority() {
        // Given: authority with 3 permissions
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()).block();
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), readPermission.getId()).block();
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), updatePermission.getId()).block();

        // When: remove all
        StepVerifier.create(authorityPermissionService.removeAllPermissionsFromAuthority(adminAuthority.getId()))
            .assertNext(deletedCount -> {
                assertThat(deletedCount).isEqualTo(3L);
            })
            .verifyComplete();

        // Then: no permissions left
        StepVerifier.create(authorityPermissionService.getPermissionsByAuthority(adminAuthority.getId()))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void shouldAssignMultiplePermissions() {
        // When: batch assign
        StepVerifier.create(
            authorityPermissionService.assignMultiplePermissions(
                adminAuthority.getId(),
                Arrays.asList(createPermission.getId(), readPermission.getId(), updatePermission.getId())
            )
        )
            .expectNextCount(3)
            .verifyComplete();

        // Then: all assigned
        StepVerifier.create(authorityPermissionService.getPermissionsByAuthority(adminAuthority.getId()))
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void shouldHandlePartialFailureInBatchAssign() {
        // Given: one permission already assigned
        authorityPermissionService.assignPermissionToAuthority(adminAuthority.getId(), createPermission.getId()).block();

        // When: batch assign including duplicate
        StepVerifier.create(
            authorityPermissionService.assignMultiplePermissions(
                adminAuthority.getId(),
                Arrays.asList(createPermission.getId(), readPermission.getId(), updatePermission.getId())
            )
        )
            // Then: 2 succeed (duplicate is skipped with warning)
            .expectNextCount(2)
            .verifyComplete();
    }

    private Authority createAuthority(String code, String name, boolean isSystem, boolean isActive, int hierarchyLevel) {
        Authority authority = new Authority();
        authority.setCode(code);
        authority.setName(name);
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(isSystem);
        authority.setIsActive(isActive);
        authority.setHierarchyLevel(hierarchyLevel);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());
        return authorityRepository.save(authority).block();
    }

    private Permission createPermission(String resource, String action, String description, boolean isActive) {
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        permission.setName(resource + "." + action);
        permission.setDescription(description);
        permission.setIsActive(isActive);
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());
        return permissionRepository.save(permission).block();
    }
}
