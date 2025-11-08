package com.tyse.scrutiny.gateway.service.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.AuthorityAlreadyExistsException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link PermissionService}.
 */
@IntegrationTest
class PermissionServiceIT {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private com.tyse.scrutiny.gateway.repository.authorization.UserPermissionRepository userPermissionRepository;

    @BeforeEach
    void setUp() {
        // Delete child entities first to avoid FK violations
        authorityPermissionRepository.deleteAll().block();
        userPermissionRepository.deleteAll().block();
        permissionRepository.deleteAll().block();
    }

    @Test
    void shouldCreatePermission() {
        Permission permission = new Permission();
        permission.setResource("invoice");
        permission.setAction("create");
        permission.setName("invoice.create");
        permission.setDescription("Create invoices");
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());

        StepVerifier.create(permissionService.createPermission(permission))
            .assertNext(created -> {
                assertThat(created.getId()).isNotNull();
                assertThat(created.getName()).isEqualTo("invoice.create");
                assertThat(created.getIsActive()).isTrue(); // default
            })
            .verifyComplete();
    }

    @Test
    void shouldAutoCorrectPermissionName() {
        Permission permission = new Permission();
        permission.setResource("user");
        permission.setAction("read");
        permission.setName("wrong_name"); // Wrong format
        permission.setDescription("Read users");
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());

        StepVerifier.create(permissionService.createPermission(permission))
            .assertNext(created -> {
                assertThat(created.getName()).isEqualTo("user.read"); // Auto-corrected
            })
            .verifyComplete();
    }

    @Test
    void shouldFailWithInvalidResourcePattern() {
        Permission permission = new Permission();
        permission.setResource("Invalid-Resource"); // Uppercase and dash not allowed
        permission.setAction("create");
        permission.setName("Invalid-Resource.create");
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());

        StepVerifier.create(permissionService.createPermission(permission)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void shouldFailWithInvalidAction() {
        Permission permission = new Permission();
        permission.setResource("user");
        permission.setAction("invalid_action"); // Not in VALID_ACTIONS
        permission.setName("user.invalid_action");
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());

        StepVerifier.create(permissionService.createPermission(permission)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void shouldFailToCreateDuplicate() {
        Permission existing = createPermission("user", "create", "Create users");

        Permission duplicate = new Permission();
        duplicate.setResource("user");
        duplicate.setAction("create");
        duplicate.setName("user.create");
        duplicate.setCreatedBy(Constants.SYSTEM);
        duplicate.setCreatedDate(Instant.now());

        StepVerifier.create(permissionService.createPermission(duplicate)).expectError(AuthorityAlreadyExistsException.class).verify();
    }

    @Test
    void shouldUpdatePermission() {
        Permission existing = createPermission("user", "update", "Update users");

        Permission updated = new Permission();
        updated.setDescription("Update user information");
        updated.setIsActive(false);

        StepVerifier.create(permissionService.updatePermission(existing.getId(), updated))
            .assertNext(result -> {
                assertThat(result.getDescription()).isEqualTo("Update user information");
                assertThat(result.getIsActive()).isFalse();
                assertThat(result.getName()).isEqualTo("user.update"); // Not changed
            })
            .verifyComplete();
    }

    @Test
    void shouldFailToUpdateNonExistent() {
        Permission updated = new Permission();
        updated.setDescription("Updated");

        StepVerifier.create(permissionService.updatePermission(999999L, updated)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void shouldFindByResource() {
        createPermission("user", "create", "Create");
        createPermission("user", "read", "Read");
        createPermission("invoice", "create", "Create invoice");

        StepVerifier.create(permissionService.findByResource("user")).expectNextCount(2).verifyComplete();
    }

    @Test
    void shouldFindByResourceAndAction() {
        createPermission("user", "create", "Create users");

        StepVerifier.create(permissionService.findByResourceAndAction("user", "create"))
            .assertNext(found -> {
                assertThat(found.getName()).isEqualTo("user.create");
            })
            .verifyComplete();
    }

    @Test
    void shouldFindAllActive() {
        Permission active1 = createPermission("user", "create", "Create");
        Permission active2 = createPermission("user", "read", "Read");

        Permission inactive = createPermission("user", "delete", "Delete");
        inactive.setIsActive(false);
        permissionRepository.save(inactive).block();

        StepVerifier.create(permissionService.findAllActive()).expectNextCount(2).verifyComplete();
    }

    @Test
    void shouldFindById() {
        Permission created = createPermission("user", "execute", "Execute");

        StepVerifier.create(permissionService.findById(created.getId()))
            .assertNext(found -> {
                assertThat(found.getId()).isEqualTo(created.getId());
            })
            .verifyComplete();
    }

    @Test
    void shouldValidateResourceActionPattern() {
        // Valid patterns
        assertValidPattern("user", "create");
        assertValidPattern("invoice_item", "read");
        assertValidPattern("report123", "execute");

        // Invalid patterns
        assertInvalidPattern("User", "create"); // Uppercase
        assertInvalidPattern("user-item", "create"); // Dash
        assertInvalidPattern("user", "CREATE"); // Uppercase action
        assertInvalidPattern("user", "custom_action"); // Invalid action
    }

    private void assertValidPattern(String resource, String action) {
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        permission.setName(resource + "." + action);
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());

        permissionRepository.deleteAll().block();
        StepVerifier.create(permissionService.createPermission(permission)).expectNextCount(1).verifyComplete();
    }

    private void assertInvalidPattern(String resource, String action) {
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        permission.setName(resource + "." + action);
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());

        StepVerifier.create(permissionService.createPermission(permission)).expectError(IllegalArgumentException.class).verify();
    }

    private Permission createPermission(String resource, String action, String description) {
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        permission.setName(resource + "." + action);
        permission.setDescription(description);
        permission.setIsActive(true);
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());
        return permissionRepository.save(permission).block();
    }
}
