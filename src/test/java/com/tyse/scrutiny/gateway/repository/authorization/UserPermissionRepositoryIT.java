package com.tyse.scrutiny.gateway.repository.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import com.tyse.scrutiny.gateway.repository.EntityManager;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link UserPermissionRepository}.
 */
@IntegrationTest
class UserPermissionRepositoryIT {

    @Autowired
    private UserPermissionRepository userPermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Permission createPermission;
    private Permission readPermission;
    private Permission updatePermission;
    private Permission deletePermission;
    private User testUser;
    private User anotherUser;
    private Long testUserId;
    private Long anotherUserId;

    @BeforeEach
    void setUp() {
        // Clean up before each test - respect FK constraints order
        entityManager.deleteAllPermissions().block(); // Cleans scr_user_permission
        entityManager.deleteAll("scr_user_authority").block(); // Clean user_authority before users
        userRepository.deleteAll().block();

        // Create test users
        testUser = new User();
        testUser.setLogin("testuser_perm_" + System.currentTimeMillis());
        testUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser.setActivated(true);
        testUser.setEmail("testuser_perm@localhost");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setLangKey("en");
        testUser.setCreatedBy(Constants.SYSTEM);
        testUser = userRepository.save(testUser).block();
        testUserId = testUser.getId();

        anotherUser = new User();
        anotherUser.setLogin("anotheruser_perm_" + System.currentTimeMillis());
        anotherUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("anotheruser_perm@localhost");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setLangKey("en");
        anotherUser.setCreatedBy(Constants.SYSTEM);
        anotherUser = userRepository.save(anotherUser).block();
        anotherUserId = anotherUser.getId();

        // Create test permissions
        createPermission = createPermission("user.create", "user", "create", "Create users");
        readPermission = createPermission("user.read", "user", "read", "Read users");
        updatePermission = createPermission("user.update", "user", "update", "Update users");
        deletePermission = createPermission("user.delete", "user", "delete", "Delete users");
    }

    @Test
    void shouldSaveAndRetrieveUserPermission() {
        // Given: user-permission grant
        UserPermission grant = new UserPermission();
        grant.setUserId(testUserId);
        grant.setPermissionId(createPermission.getId());
        grant.setIsActive(true);
        grant.setGrantedBy(Constants.SYSTEM);
        grant.setGrantedDate(Instant.now());
        grant.setReason("Emergency access");

        // When: save grant
        UserPermission saved = userPermissionRepository.save(grant).block();

        // Then: verify saved
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(testUserId);
        assertThat(saved.getPermissionId()).isEqualTo(createPermission.getId());
        assertThat(saved.getReason()).isEqualTo("Emergency access");

        // When: find by id
        UserPermission found = userPermissionRepository.findById(saved.getId()).block();

        // Then: verify found
        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(testUserId);
        assertThat(found.getPermissionId()).isEqualTo(createPermission.getId());
    }

    @Test
    void shouldFindByUserId() {
        // Given: user has 3 permissions
        createGrant(testUserId, createPermission.getId(), true, null, "Project access");
        createGrant(testUserId, readPermission.getId(), true, null, "Normal access");
        createGrant(testUserId, updatePermission.getId(), true, null, "Temporary");
        createGrant(anotherUserId, deletePermission.getId(), true, null, "Admin override");

        // When: find by user ID
        var grants = userPermissionRepository.findByUserId(testUserId).collectList().block();

        // Then: user has 3 grants
        assertThat(grants).hasSize(3);
        assertThat(grants)
            .extracting(UserPermission::getPermissionId)
            .containsExactlyInAnyOrder(createPermission.getId(), readPermission.getId(), updatePermission.getId());
    }

    @Test
    void shouldFindByUserIdAndIsActiveTrue() {
        // Given: user has active and inactive grants
        createGrant(testUserId, createPermission.getId(), true, null, "Active");
        createGrant(testUserId, readPermission.getId(), false, null, "Revoked"); // inactive
        createGrant(testUserId, updatePermission.getId(), true, null, "Active");

        // When: find active grants
        var activeGrants = userPermissionRepository.findByUserIdAndIsActiveTrue(testUserId).collectList().block();

        // Then: only active grants returned
        assertThat(activeGrants).hasSize(2);
        assertThat(activeGrants)
            .extracting(UserPermission::getPermissionId)
            .containsExactlyInAnyOrder(createPermission.getId(), updatePermission.getId());
    }

    @Test
    void shouldFindByPermissionId() {
        // Given: 2 users have read permission
        createGrant(testUserId, readPermission.getId(), true, null, "Normal access");
        createGrant(anotherUserId, readPermission.getId(), true, null, "Temporary access");
        createGrant(testUserId, createPermission.getId(), true, null, "Admin access");

        // When: find users with read permission
        var grants = userPermissionRepository.findByPermissionId(readPermission.getId()).collectList().block();

        // Then: 2 users have read
        assertThat(grants).hasSize(2);
        assertThat(grants).extracting(UserPermission::getUserId).containsExactlyInAnyOrder(testUserId, anotherUserId);
    }

    @Test
    void shouldFindByPermissionIdAndIsActiveTrue() {
        // Given: 2 users have create permission (1 active, 1 inactive)
        createGrant(testUserId, createPermission.getId(), true, null, "Active");
        createGrant(anotherUserId, createPermission.getId(), false, null, "Revoked"); // inactive

        // When: find active users with create permission
        var activeGrants = userPermissionRepository.findByPermissionIdAndIsActiveTrue(createPermission.getId()).collectList().block();

        // Then: only active user returned
        assertThat(activeGrants).hasSize(1);
        assertThat(activeGrants.get(0).getUserId()).isEqualTo(testUserId);
    }

    @Test
    void shouldFindByUserIdAndPermissionId() {
        // Given: user has create permission
        createGrant(testUserId, createPermission.getId(), true, null, "Test");
        createGrant(testUserId, readPermission.getId(), true, null, "Test");

        // When: find specific grant
        UserPermission grant = userPermissionRepository.findByUserIdAndPermissionId(testUserId, createPermission.getId()).block();

        // Then: grant found
        assertThat(grant).isNotNull();
        assertThat(grant.getUserId()).isEqualTo(testUserId);
        assertThat(grant.getPermissionId()).isEqualTo(createPermission.getId());

        // When: find non-existent grant
        UserPermission notFound = userPermissionRepository.findByUserIdAndPermissionId(testUserId, deletePermission.getId()).block();

        // Then: not found
        assertThat(notFound).isNull();
    }

    @Test
    void shouldFindValidByUserId() {
        // Given: user has:
        // - 1 active non-expired (valid)
        // - 1 inactive (invalid)
        // - 1 expired (invalid)
        createGrant(testUserId, createPermission.getId(), true, null, "Valid"); // Valid
        createGrant(testUserId, readPermission.getId(), false, null, "Inactive"); // Inactive
        createGrant(testUserId, updatePermission.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS), "Expired"); // Expired

        // When: find valid grants
        var validGrants = userPermissionRepository.findValidByUserId(testUserId).collectList().block();

        // Then: only valid grant returned
        assertThat(validGrants).hasSize(1);
        assertThat(validGrants.get(0).getPermissionId()).isEqualTo(createPermission.getId());
    }

    @Test
    void shouldHandleExpirationInFuture() {
        // Given: user has permission expiring in 30 days (still valid)
        // PostgreSQL stores timestamps with microsecond precision, truncate to avoid nano precision loss
        Instant futureExpiration = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS);
        createGrant(testUserId, createPermission.getId(), true, futureExpiration, "Temporary escalation");

        // When: find valid grants
        var validGrants = userPermissionRepository.findValidByUserId(testUserId).collectList().block();

        // Then: grant is still valid
        assertThat(validGrants).hasSize(1);
        assertThat(validGrants.get(0).getExpiresAt()).isEqualTo(futureExpiration);
    }

    @Test
    void shouldHandleNullExpiration() {
        // Given: user has permission with no expiration (permanent)
        createGrant(testUserId, createPermission.getId(), true, null, "Permanent grant");

        // When: find valid grants
        var validGrants = userPermissionRepository.findValidByUserId(testUserId).collectList().block();

        // Then: permanent grant is valid
        assertThat(validGrants).hasSize(1);
        assertThat(validGrants.get(0).getExpiresAt()).isNull();
    }

    @Test
    void shouldFindExpiredGrants() {
        // Given: expired and non-expired grants
        createGrant(testUserId, createPermission.getId(), true, Instant.now().minus(5, ChronoUnit.DAYS), "Expired 5 days ago");
        createGrant(anotherUserId, readPermission.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS), "Expired yesterday");
        createGrant(testUserId, updatePermission.getId(), true, Instant.now().plus(30, ChronoUnit.DAYS), "Future"); // Not expired

        // When: find expired
        var expired = userPermissionRepository.findExpiredGrants().collectList().block();

        // Then: 2 expired grants
        assertThat(expired).hasSize(2);
        assertThat(expired).allMatch(up -> up.getExpiresAt().isBefore(Instant.now()));
    }

    @Test
    void shouldFindExpiringWithinDays() {
        // Given: grants expiring at different times
        createGrant(testUserId, createPermission.getId(), true, Instant.now().plus(2, ChronoUnit.DAYS), "Expiring soon");
        createGrant(anotherUserId, readPermission.getId(), true, Instant.now().plus(5, ChronoUnit.DAYS), "Expiring soon");
        createGrant(testUserId, updatePermission.getId(), true, Instant.now().plus(20, ChronoUnit.DAYS), "Future"); // Beyond 7 days

        // When: find expiring within 7 days
        var expiringSoon = userPermissionRepository.findExpiringWithinDays(7).collectList().block();

        // Then: 2 grants expiring soon
        assertThat(expiringSoon).hasSize(2);
    }

    @Test
    void shouldFindByGrantedBy() {
        // Given: grants by different users
        UserPermission grant1 = createGrant(testUserId, createPermission.getId(), true, null, "Emergency");
        grant1.setGrantedBy("admin_user");
        userPermissionRepository.save(grant1).block();

        UserPermission grant2 = createGrant(anotherUserId, readPermission.getId(), true, null, "Override");
        grant2.setGrantedBy("admin_user");
        userPermissionRepository.save(grant2).block();

        UserPermission grant3 = createGrant(testUserId, updatePermission.getId(), true, null, "System");
        grant3.setGrantedBy(Constants.SYSTEM);
        userPermissionRepository.save(grant3).block();

        // When: find grants by admin_user
        var grantedByAdmin = userPermissionRepository.findByGrantedBy("admin_user").collectList().block();

        // Then: 2 grants by admin_user
        assertThat(grantedByAdmin).hasSize(2);
        assertThat(grantedByAdmin).allMatch(up -> up.getGrantedBy().equals("admin_user"));
    }

    @Test
    void shouldFindRevokedGrants() {
        // Given: revoked and active grants
        UserPermission revoked1 = createGrant(testUserId, createPermission.getId(), false, null, "Security incident");
        revoked1.setRevokedBy("security_team");
        revoked1.setRevokedDate(Instant.now());
        revoked1.setRevokedReason("Breach detected");
        userPermissionRepository.save(revoked1).block();

        UserPermission revoked2 = createGrant(anotherUserId, readPermission.getId(), false, null, "Project ended");
        revoked2.setRevokedBy("project_manager");
        revoked2.setRevokedDate(Instant.now());
        userPermissionRepository.save(revoked2).block();

        createGrant(testUserId, updatePermission.getId(), true, null, "Active"); // Not revoked

        // When: find revoked
        var revokedGrants = userPermissionRepository.findByRevokedByIsNotNull().collectList().block();

        // Then: 2 revoked grants
        assertThat(revokedGrants).hasSize(2);
        assertThat(revokedGrants).allMatch(up -> up.getRevokedBy() != null);
    }

    @Test
    void shouldFindByRevokedBy() {
        // Given: grants revoked by different users
        UserPermission revoked1 = createGrant(testUserId, createPermission.getId(), false, null, "Revoked");
        revoked1.setRevokedBy("admin");
        revoked1.setRevokedDate(Instant.now());
        userPermissionRepository.save(revoked1).block();

        UserPermission revoked2 = createGrant(anotherUserId, readPermission.getId(), false, null, "Revoked");
        revoked2.setRevokedBy("admin");
        revoked2.setRevokedDate(Instant.now());
        userPermissionRepository.save(revoked2).block();

        UserPermission revoked3 = createGrant(testUserId, updatePermission.getId(), false, null, "Revoked");
        revoked3.setRevokedBy("security_team");
        revoked3.setRevokedDate(Instant.now());
        userPermissionRepository.save(revoked3).block();

        // When: find revoked by admin
        var revokedByAdmin = userPermissionRepository.findByRevokedBy("admin").collectList().block();

        // Then: 2 revoked by admin
        assertThat(revokedByAdmin).hasSize(2);
        assertThat(revokedByAdmin).allMatch(up -> up.getRevokedBy().equals("admin"));
    }

    @Test
    void shouldFindByReason() {
        // Given: grants with different reasons
        createGrant(testUserId, createPermission.getId(), true, null, "Emergency access");
        createGrant(anotherUserId, readPermission.getId(), true, null, "Emergency access");
        createGrant(testUserId, updatePermission.getId(), true, null, "Temporary escalation");

        // When: find by reason
        var emergencyGrants = userPermissionRepository.findByReason("Emergency access").collectList().block();

        // Then: 2 emergency grants
        assertThat(emergencyGrants).hasSize(2);
        assertThat(emergencyGrants).allMatch(up -> up.getReason().equals("Emergency access"));
    }

    @Test
    void shouldCheckUserHasPermission() {
        // Given: user has create permission (valid)
        createGrant(testUserId, createPermission.getId(), true, null, "Active");
        createGrant(testUserId, readPermission.getId(), false, null, "Revoked"); // inactive
        createGrant(testUserId, updatePermission.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS), "Expired"); // expired

        // When: check various permissions
        Boolean hasCreate = userPermissionRepository.userHasPermission(testUserId, createPermission.getId()).block();
        Boolean hasRead = userPermissionRepository.userHasPermission(testUserId, readPermission.getId()).block();
        Boolean hasUpdate = userPermissionRepository.userHasPermission(testUserId, updatePermission.getId()).block();

        // Then: only create is valid
        assertThat(hasCreate).isTrue();
        assertThat(hasRead).isFalse(); // inactive
        assertThat(hasUpdate).isFalse(); // expired
    }

    @Test
    void shouldCheckUserHasPermissionByName() {
        // Given: user has user.create permission (valid)
        createGrant(testUserId, createPermission.getId(), true, null, "Active");
        createGrant(testUserId, readPermission.getId(), false, null, "Revoked"); // inactive

        // When: check by permission name
        Boolean hasCreate = userPermissionRepository.userHasPermissionByName(testUserId, "user.create").block();
        Boolean hasRead = userPermissionRepository.userHasPermissionByName(testUserId, "user.read").block();
        Boolean hasDelete = userPermissionRepository.userHasPermissionByName(testUserId, "user.delete").block();

        // Then: only create is valid
        assertThat(hasCreate).isTrue();
        assertThat(hasRead).isFalse(); // inactive
        assertThat(hasDelete).isFalse(); // not granted
    }

    @Test
    void shouldCountActiveByPermissionId() {
        // Given: create permission granted to multiple users
        createGrant(testUserId, createPermission.getId(), true, null, "Active");
        createGrant(anotherUserId, createPermission.getId(), true, null, "Active");
        // Use readPermission and updatePermission for inactive/expired to avoid FK violations
        createGrant(testUserId, readPermission.getId(), false, null, "Inactive"); // inactive
        createGrant(anotherUserId, updatePermission.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS), "Expired"); // expired

        // When: count active for createPermission
        Long activeCount = userPermissionRepository.countActiveByPermissionId(createPermission.getId()).block();

        // Then: 2 active users have createPermission
        assertThat(activeCount).isEqualTo(2L);
    }

    @Test
    void shouldFindTemporaryGrantsByUserId() {
        // Given: user has permanent and temporary grants
        createGrant(testUserId, createPermission.getId(), true, null, "Permanent"); // No expiration
        createGrant(testUserId, readPermission.getId(), true, Instant.now().plus(7, ChronoUnit.DAYS), "Temporary 7 days");
        createGrant(testUserId, updatePermission.getId(), true, Instant.now().plus(30, ChronoUnit.DAYS), "Temporary 30 days");

        // When: find temporary grants
        var temporaryGrants = userPermissionRepository.findTemporaryGrantsByUserId(testUserId).collectList().block();

        // Then: 2 temporary grants (those with expiration)
        assertThat(temporaryGrants).hasSize(2);
        assertThat(temporaryGrants).allMatch(up -> up.getExpiresAt() != null);
    }

    @Test
    void shouldDeleteByUserId() {
        // Given: user has multiple permissions
        createGrant(testUserId, createPermission.getId(), true, null, "Test");
        createGrant(testUserId, readPermission.getId(), true, null, "Test");
        createGrant(anotherUserId, updatePermission.getId(), true, null, "Test");

        // Verify user has 2 grants
        Long countBefore = userPermissionRepository.findByUserId(testUserId).count().block();
        assertThat(countBefore).isEqualTo(2L);

        // When: delete all user grants
        userPermissionRepository.deleteByUserId(testUserId).block();

        // Then: user has no grants
        Long countAfter = userPermissionRepository.findByUserId(testUserId).count().block();
        assertThat(countAfter).isEqualTo(0L);

        // But other user still has grant
        Long otherUserCount = userPermissionRepository.findByUserId(anotherUserId).count().block();
        assertThat(otherUserCount).isEqualTo(1L);
    }

    @Test
    void shouldDeleteByPermissionId() {
        // Given: create permission granted to multiple users
        createGrant(testUserId, createPermission.getId(), true, null, "Test");
        createGrant(anotherUserId, createPermission.getId(), true, null, "Test");
        createGrant(testUserId, readPermission.getId(), true, null, "Test");

        // Verify create has 2 users
        Long countBefore = userPermissionRepository.findByPermissionId(createPermission.getId()).count().block();
        assertThat(countBefore).isEqualTo(2L);

        // When: delete all create grants
        userPermissionRepository.deleteByPermissionId(createPermission.getId()).block();

        // Then: create has no users
        Long countAfter = userPermissionRepository.findByPermissionId(createPermission.getId()).count().block();
        assertThat(countAfter).isEqualTo(0L);

        // But read permission still has grant
        Long readCount = userPermissionRepository.findByPermissionId(readPermission.getId()).count().block();
        assertThat(readCount).isEqualTo(1L);
    }

    @Test
    void shouldValidateRevokedFields() {
        // Given: revoked grant with all fields
        UserPermission grant = createGrant(testUserId, createPermission.getId(), false, null, "Security test");
        grant.setRevokedBy("admin_user");
        grant.setRevokedDate(Instant.now());
        grant.setRevokedReason("User left company");

        // When: save
        UserPermission saved = userPermissionRepository.save(grant).block();

        // Then: all revoked fields preserved
        assertThat(saved.getRevokedBy()).isEqualTo("admin_user");
        assertThat(saved.getRevokedDate()).isNotNull();
        assertThat(saved.getRevokedReason()).isEqualTo("User left company");
    }

    @Test
    void shouldHandleTemporaryGrant() {
        // Given: temporary grant (7 days)
        // PostgreSQL stores timestamps with microsecond precision, truncate to avoid nano precision loss
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS);
        UserPermission grant = createGrant(testUserId, createPermission.getId(), true, expiresAt, "Project access");

        // When: retrieve
        UserPermission found = userPermissionRepository.findByUserIdAndPermissionId(testUserId, createPermission.getId()).block();

        // Then: expiration is set
        assertThat(found).isNotNull();
        assertThat(found.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(found.getReason()).isEqualTo("Project access");

        // And: is still valid (using entity method)
        assertThat(found.isValid()).isTrue();
        assertThat(found.isExpired()).isFalse();
    }

    @Test
    void shouldValidateEntityIsExpiredMethod() {
        // Given: expired grant
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        UserPermission expired = createGrant(testUserId, createPermission.getId(), true, pastDate, "Expired");

        // When: retrieve
        UserPermission found = userPermissionRepository.findByUserIdAndPermissionId(testUserId, createPermission.getId()).block();

        // Then: entity recognizes expiration
        assertThat(found.isExpired()).isTrue();
        assertThat(found.isValid()).isFalse(); // expired = not valid
    }

    @Test
    void shouldValidateEntityIsValidMethod() {
        // Given: active non-expired grant
        UserPermission valid = createGrant(testUserId, createPermission.getId(), true, null, "Valid");

        // When: retrieve
        UserPermission found = userPermissionRepository.findByUserIdAndPermissionId(testUserId, createPermission.getId()).block();

        // Then: entity is valid
        assertThat(found.isValid()).isTrue();
        assertThat(found.isExpired()).isFalse();

        // Given: inactive grant
        valid.setIsActive(false);
        userPermissionRepository.save(valid).block();

        // When: retrieve
        UserPermission inactive = userPermissionRepository.findByUserIdAndPermissionId(testUserId, createPermission.getId()).block();

        // Then: entity is not valid (inactive)
        assertThat(inactive.isValid()).isFalse();
    }

    /**
     * Helper method to create permission
     */
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

    /**
     * Helper method to create user-permission grant
     */
    private UserPermission createGrant(Long userId, Long permissionId, boolean isActive, Instant expiresAt, String reason) {
        UserPermission grant = new UserPermission();
        grant.setUserId(userId);
        grant.setPermissionId(permissionId);
        grant.setIsActive(isActive);
        grant.setExpiresAt(expiresAt);
        grant.setGrantedBy(Constants.SYSTEM);
        grant.setGrantedDate(Instant.now());
        grant.setReason(reason);
        return userPermissionRepository.save(grant).block();
    }
}
