package com.tyse.scrutiny.gateway.repository.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.EntityManager;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link PermissionRepository}.
 */
@IntegrationTest
class PermissionRepositoryIT {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private EntityManager entityManager;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        entityManager.deleteAllPermissions().block();

        // Create a test permission
        testPermission = new Permission();
        testPermission.setName("user.create");
        testPermission.setResource("user");
        testPermission.setAction("create");
        testPermission.setDescription("Allows creating new users");
        testPermission.setIsActive(true);
        testPermission.setCreatedBy(Constants.SYSTEM);
        testPermission.setCreatedDate(Instant.now());
    }

    @Test
    void shouldSaveAndRetrievePermission() {
        // When: save permission
        Permission saved = permissionRepository.save(testPermission).block();

        // Then: verify saved
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("user.create");
        assertThat(saved.getResource()).isEqualTo("user");
        assertThat(saved.getAction()).isEqualTo("create");

        // When: find by id
        Permission found = permissionRepository.findById(saved.getId()).block();

        // Then: verify found
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("user.create");
    }

    @Test
    void shouldFindByName() {
        // Given: saved permission
        permissionRepository.save(testPermission).block();

        // When: find by name
        Permission found = permissionRepository.findByName("user.create").block();

        // Then: verify found
        assertThat(found).isNotNull();
        assertThat(found.getResource()).isEqualTo("user");
        assertThat(found.getAction()).isEqualTo("create");
    }

    @Test
    void shouldFindByResource() {
        // Given: multiple permissions for user resource
        permissionRepository.save(testPermission).block();

        Permission userRead = new Permission();
        userRead.setName("user.read");
        userRead.setResource("user");
        userRead.setAction("read");
        userRead.setDescription("Read users");
        userRead.setIsActive(true);
        userRead.setCreatedBy(Constants.SYSTEM);
        userRead.setCreatedDate(Instant.now());
        permissionRepository.save(userRead).block();

        Permission invoiceCreate = new Permission();
        invoiceCreate.setName("invoice.create");
        invoiceCreate.setResource("invoice");
        invoiceCreate.setAction("create");
        invoiceCreate.setDescription("Create invoices");
        invoiceCreate.setIsActive(true);
        invoiceCreate.setCreatedBy(Constants.SYSTEM);
        invoiceCreate.setCreatedDate(Instant.now());
        permissionRepository.save(invoiceCreate).block();

        // When: find by resource "user"
        var userPermissions = permissionRepository.findByResource("user").collectList().block();

        // Then: only user permissions are returned
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions).extracting(Permission::getName).containsExactlyInAnyOrder("user.create", "user.read");
    }

    @Test
    void shouldFindByAction() {
        // Given: multiple permissions with "create" action
        permissionRepository.save(testPermission).block(); // user.create

        Permission invoiceCreate = new Permission();
        invoiceCreate.setName("invoice.create");
        invoiceCreate.setResource("invoice");
        invoiceCreate.setAction("create");
        invoiceCreate.setDescription("Create invoices");
        invoiceCreate.setIsActive(true);
        invoiceCreate.setCreatedBy(Constants.SYSTEM);
        invoiceCreate.setCreatedDate(Instant.now());
        permissionRepository.save(invoiceCreate).block();

        Permission userRead = new Permission();
        userRead.setName("user.read");
        userRead.setResource("user");
        userRead.setAction("read");
        userRead.setDescription("Read users");
        userRead.setIsActive(true);
        userRead.setCreatedBy(Constants.SYSTEM);
        userRead.setCreatedDate(Instant.now());
        permissionRepository.save(userRead).block();

        // When: find by action "create"
        var createPermissions = permissionRepository.findByAction("create").collectList().block();

        // Then: only create permissions are returned
        assertThat(createPermissions).hasSize(2);
        assertThat(createPermissions).extracting(Permission::getName).containsExactlyInAnyOrder("user.create", "invoice.create");
    }

    @Test
    void shouldFindActivePermissions() {
        // Given: active and inactive permissions
        testPermission.setIsActive(true);
        permissionRepository.save(testPermission).block();

        Permission inactive = new Permission();
        inactive.setName("user.delete");
        inactive.setResource("user");
        inactive.setAction("delete");
        inactive.setDescription("Delete users");
        inactive.setIsActive(false);
        inactive.setCreatedBy(Constants.SYSTEM);
        inactive.setCreatedDate(Instant.now());
        permissionRepository.save(inactive).block();

        // When: find active permissions
        var activePermissions = permissionRepository.findByIsActiveTrue().collectList().block();

        // Then: only active permission is returned
        assertThat(activePermissions).hasSize(1);
        assertThat(activePermissions.get(0).getName()).isEqualTo("user.create");
    }

    @Test
    void shouldFindByResourceAndAction() {
        // Given: saved permissions
        permissionRepository.save(testPermission).block();

        Permission userRead = new Permission();
        userRead.setName("user.read");
        userRead.setResource("user");
        userRead.setAction("read");
        userRead.setDescription("Read users");
        userRead.setIsActive(true);
        userRead.setCreatedBy(Constants.SYSTEM);
        userRead.setCreatedDate(Instant.now());
        permissionRepository.save(userRead).block();

        // When: find by resource and action
        Permission found = permissionRepository.findByResourceAndAction("user", "create").block();

        // Then: verify found correct permission
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("user.create");
        assertThat(found.getDescription()).isEqualTo("Allows creating new users");
    }

    @Test
    void shouldFindActivePermissionsByResource() {
        // Given: active and inactive permissions for user resource
        testPermission.setIsActive(true);
        permissionRepository.save(testPermission).block();

        Permission userDelete = new Permission();
        userDelete.setName("user.delete");
        userDelete.setResource("user");
        userDelete.setAction("delete");
        userDelete.setDescription("Delete users");
        userDelete.setIsActive(false); // Inactive
        userDelete.setCreatedBy(Constants.SYSTEM);
        userDelete.setCreatedDate(Instant.now());
        permissionRepository.save(userDelete).block();

        // When: find active permissions for user resource
        var activeUserPermissions = permissionRepository.findByResourceAndIsActiveTrue("user").collectList().block();

        // Then: only active user permissions are returned
        assertThat(activeUserPermissions).hasSize(1);
        assertThat(activeUserPermissions.get(0).getName()).isEqualTo("user.create");
    }

    @Test
    void shouldCheckIfNameExists() {
        // Given: saved permission
        permissionRepository.save(testPermission).block();

        // When: check if name exists
        Boolean exists = permissionRepository.existsByName("user.create").block();
        Boolean notExists = permissionRepository.existsByName("nonexistent.permission").block();

        // Then: verify existence checks
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldUpdatePermission() {
        // Given: saved permission
        Permission saved = permissionRepository.save(testPermission).block();
        assertThat(saved).isNotNull();

        // When: update permission
        saved.setDescription("Updated description");
        saved.setLastModifiedBy("admin");
        saved.setLastModifiedDate(Instant.now());
        Permission updated = permissionRepository.save(saved).block();

        // Then: verify updated
        assertThat(updated).isNotNull();
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        // Note: lastModifiedBy is set by R2DBC auditing to current security context user
        assertThat(updated.getLastModifiedBy()).isNotNull();
    }

    @Test
    void shouldDeletePermission() {
        // Given: saved permission
        Permission saved = permissionRepository.save(testPermission).block();
        assertThat(saved).isNotNull();
        Long id = saved.getId();

        // When: delete permission
        permissionRepository.deleteById(id).block();

        // Then: permission should not exist
        Permission found = permissionRepository.findById(id).block();
        assertThat(found).isNull();
    }

    @Test
    void shouldCountPermissions() {
        // Given: multiple permissions
        permissionRepository.save(testPermission).block();

        Permission userRead = new Permission();
        userRead.setName("user.read");
        userRead.setResource("user");
        userRead.setAction("read");
        userRead.setDescription("Read users");
        userRead.setIsActive(true);
        userRead.setCreatedBy(Constants.SYSTEM);
        userRead.setCreatedDate(Instant.now());
        permissionRepository.save(userRead).block();

        // When: count permissions
        Long count = permissionRepository.count().block();

        // Then: verify count
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void shouldValidateResourceActionPattern() {
        // Given: permissions following resource.action pattern
        permissionRepository.save(testPermission).block(); // user.create

        Permission reportExport = new Permission();
        reportExport.setName("report.export");
        reportExport.setResource("report");
        reportExport.setAction("export");
        reportExport.setDescription("Export reports");
        reportExport.setIsActive(true);
        reportExport.setCreatedBy(Constants.SYSTEM);
        reportExport.setCreatedDate(Instant.now());
        permissionRepository.save(reportExport).block();

        // When: retrieve all permissions
        var allPermissions = permissionRepository.findAll().collectList().block();

        // Then: all permissions follow resource.action pattern
        assertThat(allPermissions).isNotEmpty();
        assertThat(allPermissions).allMatch(p -> p.getName().equals(p.getResource() + "." + p.getAction()));
    }
}
