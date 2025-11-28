package com.tyse.scrutiny.gateway.repository.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityPermission;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.EntityManager;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.commons.lang3.RandomStringUtils;
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
    private AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

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

    // ========== Tests for findByUserRoles() ==========

    @Test
    void shouldFindPermissionsByUserRoles() {
        // Given: clean state
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user
        User testUser = createTestUser("testuser_perm_roles_" + System.currentTimeMillis());

        // Create authority (role)
        Authority adminAuthority = createAuthority("ROLE_ADMIN_TEST", "Administrator", 0);

        // Create permissions
        Permission divipolRead = createPermission("divipol.read", "divipol", "read", "Read divipol data");
        Permission statisticsRead = createPermission("statistics.read", "statistics", "read", "Read statistics");
        // Create a permission that will NOT be assigned to any role (exists in DB but not linked)
        createPermission("unassigned.read", "unassigned", "read", "Not assigned to any role");

        // Assign only some permissions to authority (unassigned.read is intentionally NOT assigned)
        createAuthorityPermission(adminAuthority.getId(), divipolRead.getId());
        createAuthorityPermission(adminAuthority.getId(), statisticsRead.getId());

        // Assign authority to user
        createUserAuthority(testUser.getId(), adminAuthority.getId(), true, null);

        // When: find permissions by user roles
        var permissions = permissionRepository.findByUserRoles(testUser.getId()).collectList().block();

        // Then: only permissions from assigned role are returned
        assertThat(permissions).hasSize(2);
        assertThat(permissions).extracting(Permission::getName).containsExactlyInAnyOrder("divipol.read", "statistics.read");
        // unassigned.read should NOT be in the list
        assertThat(permissions).extracting(Permission::getName).doesNotContain("unassigned.read");
    }

    @Test
    void shouldFindPermissionsFromMultipleRoles() {
        // Given: clean state
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user
        User testUser = createTestUser("testuser_multi_roles_" + System.currentTimeMillis());

        // Create authorities (roles)
        Authority adminAuthority = createAuthority("ROLE_ADMIN_TEST2", "Admin", 0);
        Authority userAuthority = createAuthority("ROLE_USER_TEST2", "User", 100);

        // Create permissions
        Permission adminPerm = createPermission("admin.manage", "admin", "manage", "Admin management");
        Permission userPerm = createPermission("profile.read", "profile", "read", "Read own profile");
        Permission sharedPerm = createPermission("shared.access", "shared", "access", "Shared access");

        // Assign permissions to authorities
        createAuthorityPermission(adminAuthority.getId(), adminPerm.getId());
        createAuthorityPermission(adminAuthority.getId(), sharedPerm.getId()); // shared permission
        createAuthorityPermission(userAuthority.getId(), userPerm.getId());
        createAuthorityPermission(userAuthority.getId(), sharedPerm.getId()); // same shared permission

        // Assign both authorities to user
        createUserAuthority(testUser.getId(), adminAuthority.getId(), true, null);
        createUserAuthority(testUser.getId(), userAuthority.getId(), true, null);

        // When: find permissions by user roles
        var permissions = permissionRepository.findByUserRoles(testUser.getId()).collectList().block();

        // Then: permissions from both roles are returned (DISTINCT - no duplicates)
        assertThat(permissions).hasSize(3); // admin.manage, profile.read, shared.access (deduplicated)
        assertThat(permissions).extracting(Permission::getName).containsExactlyInAnyOrder("admin.manage", "profile.read", "shared.access");
    }

    @Test
    void shouldRespectUserAuthorityIsActive() {
        // Given: clean state
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user
        User testUser = createTestUser("testuser_active_" + System.currentTimeMillis());

        // Create authorities
        Authority activeAuthority = createAuthority("ROLE_ACTIVE_TEST", "Active Role", 0);
        Authority inactiveAuthority = createAuthority("ROLE_INACTIVE_TEST", "Inactive Role", 100);

        // Create permissions
        Permission activePerm = createPermission("active.perm", "active", "perm", "Active permission");
        Permission inactivePerm = createPermission("inactive.perm", "inactive", "perm", "Inactive permission");

        // Assign permissions to authorities
        createAuthorityPermission(activeAuthority.getId(), activePerm.getId());
        createAuthorityPermission(inactiveAuthority.getId(), inactivePerm.getId());

        // Assign authorities to user (one active, one inactive)
        createUserAuthority(testUser.getId(), activeAuthority.getId(), true, null); // ACTIVE
        createUserAuthority(testUser.getId(), inactiveAuthority.getId(), false, null); // INACTIVE

        // When: find permissions by user roles
        var permissions = permissionRepository.findByUserRoles(testUser.getId()).collectList().block();

        // Then: only permissions from ACTIVE user-authority are returned
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getName()).isEqualTo("active.perm");
    }

    @Test
    void shouldRespectUserAuthorityExpiration() {
        // Given: clean state
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user
        User testUser = createTestUser("testuser_exp_" + System.currentTimeMillis());

        // Create authorities
        Authority validAuthority = createAuthority("ROLE_VALID_TEST", "Valid Role", 0);
        Authority expiredAuthority = createAuthority("ROLE_EXPIRED_TEST", "Expired Role", 100);

        // Create permissions
        Permission validPerm = createPermission("valid.perm", "valid", "perm", "Valid permission");
        Permission expiredPerm = createPermission("expired.perm", "expired", "perm", "Expired permission");

        // Assign permissions to authorities
        createAuthorityPermission(validAuthority.getId(), validPerm.getId());
        createAuthorityPermission(expiredAuthority.getId(), expiredPerm.getId());

        // Assign authorities to user (one not expired, one expired)
        createUserAuthority(testUser.getId(), validAuthority.getId(), true, null); // No expiration
        createUserAuthority(testUser.getId(), expiredAuthority.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS)); // EXPIRED

        // When: find permissions by user roles
        var permissions = permissionRepository.findByUserRoles(testUser.getId()).collectList().block();

        // Then: only permissions from non-expired user-authority are returned
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getName()).isEqualTo("valid.perm");
    }

    @Test
    void shouldRespectPermissionIsActive() {
        // Given: clean state
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user
        User testUser = createTestUser("testuser_perm_active_" + System.currentTimeMillis());

        // Create authority
        Authority authority = createAuthority("ROLE_TEST_ACTIVE", "Test Role", 0);

        // Create permissions (one active, one inactive)
        Permission activePerm = createPermission("perm.active", "perm", "active", "Active permission");
        Permission inactivePerm = new Permission();
        inactivePerm.setName("perm.inactive");
        inactivePerm.setResource("perm");
        inactivePerm.setAction("inactive");
        inactivePerm.setDescription("Inactive permission");
        inactivePerm.setIsActive(false); // INACTIVE permission
        inactivePerm.setCreatedBy(Constants.SYSTEM);
        inactivePerm.setCreatedDate(Instant.now());
        inactivePerm = permissionRepository.save(inactivePerm).block();

        // Assign both permissions to authority
        createAuthorityPermission(authority.getId(), activePerm.getId());
        createAuthorityPermission(authority.getId(), inactivePerm.getId());

        // Assign authority to user
        createUserAuthority(testUser.getId(), authority.getId(), true, null);

        // When: find permissions by user roles
        var permissions = permissionRepository.findByUserRoles(testUser.getId()).collectList().block();

        // Then: only ACTIVE permissions are returned
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getName()).isEqualTo("perm.active");
    }

    @Test
    void shouldReturnEmptyForUserWithNoRoles() {
        // Given: clean state
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user WITHOUT any roles
        User testUser = createTestUser("testuser_no_roles_" + System.currentTimeMillis());

        // Create some permissions (not assigned to any role the user has)
        createPermission("orphan.perm", "orphan", "perm", "Orphan permission");

        // When: find permissions by user roles
        var permissions = permissionRepository.findByUserRoles(testUser.getId()).collectList().block();

        // Then: empty list returned
        assertThat(permissions).isEmpty();
    }

    @Test
    void shouldReturnPermissionsOrderedByResourceAndAction() {
        // Given: clean state
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test user
        User testUser = createTestUser("testuser_order_" + System.currentTimeMillis());

        // Create authority
        Authority authority = createAuthority("ROLE_ORDER_TEST", "Order Test", 0);

        // Create permissions in random order
        Permission zPerm = createPermission("zoo.read", "zoo", "read", "Zoo read");
        Permission aPerm = createPermission("alpha.read", "alpha", "read", "Alpha read");
        Permission aWritePerm = createPermission("alpha.write", "alpha", "write", "Alpha write");
        Permission mPerm = createPermission("middle.read", "middle", "read", "Middle read");

        // Assign permissions to authority
        createAuthorityPermission(authority.getId(), zPerm.getId());
        createAuthorityPermission(authority.getId(), aPerm.getId());
        createAuthorityPermission(authority.getId(), aWritePerm.getId());
        createAuthorityPermission(authority.getId(), mPerm.getId());

        // Assign authority to user
        createUserAuthority(testUser.getId(), authority.getId(), true, null);

        // When: find permissions by user roles
        var permissions = permissionRepository.findByUserRoles(testUser.getId()).collectList().block();

        // Then: permissions are ordered by resource, then action
        assertThat(permissions).hasSize(4);
        assertThat(permissions).extracting(Permission::getName).containsExactly("alpha.read", "alpha.write", "middle.read", "zoo.read");
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

    private Authority createAuthority(String code, String name, int hierarchyLevel) {
        Authority authority = new Authority();
        authority.setCode(code);
        authority.setName(name);
        authority.setDescription(name + " role for testing");
        authority.setCategory(AuthorityCategory.SYSTEM);
        authority.setIsSystem(false);
        authority.setIsActive(true);
        authority.setHierarchyLevel(hierarchyLevel);
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
