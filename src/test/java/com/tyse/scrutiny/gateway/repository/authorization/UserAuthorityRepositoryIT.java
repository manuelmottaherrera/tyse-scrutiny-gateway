package com.tyse.scrutiny.gateway.repository.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
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
 * Integration tests for {@link UserAuthorityRepository}.
 */
@IntegrationTest
class UserAuthorityRepositoryIT {

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Authority adminAuthority;
    private Authority userAuthority;
    private Authority managerAuthority;
    private User testUser;
    private User anotherUser;
    private Long testUserId;
    private Long anotherUserId;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        entityManager.deleteAllAuthorities().block();
        userRepository.deleteAll().block();

        // Create test users
        testUser = new User();
        testUser.setLogin("testuser_auth_" + System.currentTimeMillis());
        testUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser.setActivated(true);
        testUser.setEmail("testuser_auth@localhost");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setLangKey("en");
        testUser.setCreatedBy(Constants.SYSTEM);
        testUser = userRepository.save(testUser).block();
        testUserId = testUser.getId();

        anotherUser = new User();
        anotherUser.setLogin("anotheruser_auth_" + System.currentTimeMillis());
        anotherUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("anotheruser_auth@localhost");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setLangKey("en");
        anotherUser.setCreatedBy(Constants.SYSTEM);
        anotherUser = userRepository.save(anotherUser).block();
        anotherUserId = anotherUser.getId();

        // Create test authorities
        adminAuthority = createAuthority("ROLE_ADMIN_TEST", "Administrator", 0);
        userAuthority = createAuthority("ROLE_USER_TEST", "User", 500);
        managerAuthority = createAuthority("ROLE_MANAGER_TEST", "Manager", 100);
    }

    @Test
    void shouldSaveAndRetrieveUserAuthority() {
        // Given: user-authority assignment
        UserAuthority assignment = new UserAuthority();
        assignment.setUserId(testUserId);
        assignment.setAuthorityId(adminAuthority.getId());
        assignment.setIsActive(true);
        assignment.setAssignedBy(Constants.SYSTEM);
        assignment.setAssignedDate(Instant.now());

        // When: save assignment
        UserAuthority saved = userAuthorityRepository.save(assignment).block();

        // Then: verify saved
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(testUserId);
        assertThat(saved.getAuthorityId()).isEqualTo(adminAuthority.getId());

        // When: find by id
        UserAuthority found = userAuthorityRepository.findById(saved.getId()).block();

        // Then: verify found
        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(testUserId);
        assertThat(found.getAuthorityId()).isEqualTo(adminAuthority.getId());
    }

    @Test
    void shouldFindByUserId() {
        // Given: user has 2 authorities
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(testUserId, userAuthority.getId(), true, null);
        createAssignment(anotherUserId, managerAuthority.getId(), true, null);

        // When: find by user ID
        var assignments = userAuthorityRepository.findByUserId(testUserId).collectList().block();

        // Then: user has 2 assignments
        assertThat(assignments).hasSize(2);
        assertThat(assignments)
            .extracting(UserAuthority::getAuthorityId)
            .containsExactlyInAnyOrder(adminAuthority.getId(), userAuthority.getId());
    }

    @Test
    void shouldFindByUserIdAndIsActiveTrue() {
        // Given: user has active and inactive authorities
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(testUserId, userAuthority.getId(), false, null); // inactive
        createAssignment(testUserId, managerAuthority.getId(), true, null);

        // When: find active assignments
        var activeAssignments = userAuthorityRepository.findByUserIdAndIsActiveTrue(testUserId).collectList().block();

        // Then: only active assignments returned
        assertThat(activeAssignments).hasSize(2);
        assertThat(activeAssignments)
            .extracting(UserAuthority::getAuthorityId)
            .containsExactlyInAnyOrder(adminAuthority.getId(), managerAuthority.getId());
    }

    @Test
    void shouldFindByAuthorityId() {
        // Given: 2 users have admin authority
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(anotherUserId, adminAuthority.getId(), true, null);
        createAssignment(testUserId, userAuthority.getId(), true, null);

        // When: find users with admin authority
        var assignments = userAuthorityRepository.findByAuthorityId(adminAuthority.getId()).collectList().block();

        // Then: 2 users have admin
        assertThat(assignments).hasSize(2);
        assertThat(assignments).extracting(UserAuthority::getUserId).containsExactlyInAnyOrder(testUserId, anotherUserId);
    }

    @Test
    void shouldFindByAuthorityIdAndIsActiveTrue() {
        // Given: 2 users have admin (1 active, 1 inactive)
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(anotherUserId, adminAuthority.getId(), false, null); // inactive

        // When: find active users with admin
        var activeAssignments = userAuthorityRepository.findByAuthorityIdAndIsActiveTrue(adminAuthority.getId()).collectList().block();

        // Then: only active user returned
        assertThat(activeAssignments).hasSize(1);
        assertThat(activeAssignments.get(0).getUserId()).isEqualTo(testUserId);
    }

    @Test
    void shouldFindByUserIdAndAuthorityId() {
        // Given: user has admin authority
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(testUserId, userAuthority.getId(), true, null);

        // When: find specific assignment
        UserAuthority assignment = userAuthorityRepository.findByUserIdAndAuthorityId(testUserId, adminAuthority.getId()).block();

        // Then: assignment found
        assertThat(assignment).isNotNull();
        assertThat(assignment.getUserId()).isEqualTo(testUserId);
        assertThat(assignment.getAuthorityId()).isEqualTo(adminAuthority.getId());

        // When: find non-existent assignment
        UserAuthority notFound = userAuthorityRepository.findByUserIdAndAuthorityId(testUserId, managerAuthority.getId()).block();

        // Then: not found
        assertThat(notFound).isNull();
    }

    @Test
    void shouldFindValidByUserId() {
        // Given: user has:
        // - 1 active non-expired (valid)
        // - 1 inactive (invalid)
        // - 1 expired (invalid)
        createAssignment(testUserId, adminAuthority.getId(), true, null); // Valid
        createAssignment(testUserId, userAuthority.getId(), false, null); // Inactive
        createAssignment(testUserId, managerAuthority.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS)); // Expired

        // When: find valid assignments
        var validAssignments = userAuthorityRepository.findValidByUserId(testUserId).collectList().block();

        // Then: only valid assignment returned
        assertThat(validAssignments).hasSize(1);
        assertThat(validAssignments.get(0).getAuthorityId()).isEqualTo(adminAuthority.getId());
    }

    @Test
    void shouldHandleExpirationInFuture() {
        // Given: user has authority expiring in 30 days (still valid)
        Instant futureExpiration = Instant.now().plus(30, ChronoUnit.DAYS);
        createAssignment(testUserId, adminAuthority.getId(), true, futureExpiration);

        // When: find valid assignments
        var validAssignments = userAuthorityRepository.findValidByUserId(testUserId).collectList().block();

        // Then: assignment is still valid
        assertThat(validAssignments).hasSize(1);
        assertThat(validAssignments.get(0).getExpiresAt()).isEqualTo(futureExpiration);
    }

    @Test
    void shouldHandleNullExpiration() {
        // Given: user has authority with no expiration (permanent)
        createAssignment(testUserId, adminAuthority.getId(), true, null);

        // When: find valid assignments
        var validAssignments = userAuthorityRepository.findValidByUserId(testUserId).collectList().block();

        // Then: permanent assignment is valid
        assertThat(validAssignments).hasSize(1);
        assertThat(validAssignments.get(0).getExpiresAt()).isNull();
    }

    @Test
    void shouldFindExpiredAssignments() {
        // Given: expired and non-expired assignments
        createAssignment(testUserId, adminAuthority.getId(), true, Instant.now().minus(5, ChronoUnit.DAYS));
        createAssignment(anotherUserId, userAuthority.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS));
        createAssignment(testUserId, managerAuthority.getId(), true, Instant.now().plus(30, ChronoUnit.DAYS)); // Not expired

        // When: find expired
        var expired = userAuthorityRepository.findExpiredAssignments().collectList().block();

        // Then: 2 expired assignments
        assertThat(expired).hasSize(2);
        assertThat(expired).allMatch(ua -> ua.getExpiresAt().isBefore(Instant.now()));
    }

    @Test
    void shouldFindExpiringWithinDays() {
        // Given: assignments expiring at different times
        createAssignment(testUserId, adminAuthority.getId(), true, Instant.now().plus(2, ChronoUnit.DAYS));
        createAssignment(anotherUserId, userAuthority.getId(), true, Instant.now().plus(5, ChronoUnit.DAYS));
        createAssignment(testUserId, managerAuthority.getId(), true, Instant.now().plus(20, ChronoUnit.DAYS)); // Beyond 7 days

        // When: find expiring within 7 days
        var expiringSoon = userAuthorityRepository.findExpiringWithinDays(7).collectList().block();

        // Then: 2 assignments expiring soon
        assertThat(expiringSoon).hasSize(2);
    }

    @Test
    void shouldFindByAssignedBy() {
        // Given: assignments by different users
        UserAuthority assignment1 = createAssignment(testUserId, adminAuthority.getId(), true, null);
        assignment1.setAssignedBy("admin_user");
        userAuthorityRepository.save(assignment1).block();

        UserAuthority assignment2 = createAssignment(anotherUserId, userAuthority.getId(), true, null);
        assignment2.setAssignedBy("admin_user");
        userAuthorityRepository.save(assignment2).block();

        UserAuthority assignment3 = createAssignment(testUserId, managerAuthority.getId(), true, null);
        assignment3.setAssignedBy(Constants.SYSTEM);
        userAuthorityRepository.save(assignment3).block();

        // When: find assignments by admin_user
        var assignedByAdmin = userAuthorityRepository.findByAssignedBy("admin_user").collectList().block();

        // Then: 2 assignments by admin_user
        assertThat(assignedByAdmin).hasSize(2);
        assertThat(assignedByAdmin).allMatch(ua -> ua.getAssignedBy().equals("admin_user"));
    }

    @Test
    void shouldFindRevokedAssignments() {
        // Given: revoked and active assignments
        UserAuthority revoked1 = createAssignment(testUserId, adminAuthority.getId(), false, null);
        revoked1.setRevokedBy("admin");
        revoked1.setRevokedDate(Instant.now());
        revoked1.setRevokedReason("Security incident");
        userAuthorityRepository.save(revoked1).block();

        UserAuthority revoked2 = createAssignment(anotherUserId, userAuthority.getId(), false, null);
        revoked2.setRevokedBy("security_team");
        revoked2.setRevokedDate(Instant.now());
        userAuthorityRepository.save(revoked2).block();

        createAssignment(testUserId, managerAuthority.getId(), true, null); // Not revoked

        // When: find revoked
        var revokedAssignments = userAuthorityRepository.findByRevokedByIsNotNull().collectList().block();

        // Then: 2 revoked assignments
        assertThat(revokedAssignments).hasSize(2);
        assertThat(revokedAssignments).allMatch(ua -> ua.getRevokedBy() != null);
    }

    @Test
    void shouldFindByRevokedBy() {
        // Given: assignments revoked by different users
        UserAuthority revoked1 = createAssignment(testUserId, adminAuthority.getId(), false, null);
        revoked1.setRevokedBy("admin");
        revoked1.setRevokedDate(Instant.now());
        userAuthorityRepository.save(revoked1).block();

        UserAuthority revoked2 = createAssignment(anotherUserId, userAuthority.getId(), false, null);
        revoked2.setRevokedBy("admin");
        revoked2.setRevokedDate(Instant.now());
        userAuthorityRepository.save(revoked2).block();

        UserAuthority revoked3 = createAssignment(testUserId, managerAuthority.getId(), false, null);
        revoked3.setRevokedBy("security_team");
        revoked3.setRevokedDate(Instant.now());
        userAuthorityRepository.save(revoked3).block();

        // When: find revoked by admin
        var revokedByAdmin = userAuthorityRepository.findByRevokedBy("admin").collectList().block();

        // Then: 2 revoked by admin
        assertThat(revokedByAdmin).hasSize(2);
        assertThat(revokedByAdmin).allMatch(ua -> ua.getRevokedBy().equals("admin"));
    }

    @Test
    void shouldCheckUserHasAuthority() {
        // Given: user has admin authority (valid)
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(testUserId, userAuthority.getId(), false, null); // inactive
        createAssignment(testUserId, managerAuthority.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS)); // expired

        // When: check various authorities
        Boolean hasAdmin = userAuthorityRepository.userHasAuthority(testUserId, adminAuthority.getId()).block();
        Boolean hasUser = userAuthorityRepository.userHasAuthority(testUserId, userAuthority.getId()).block();
        Boolean hasManager = userAuthorityRepository.userHasAuthority(testUserId, managerAuthority.getId()).block();

        // Then: only admin is valid
        assertThat(hasAdmin).isTrue();
        assertThat(hasUser).isFalse(); // inactive
        assertThat(hasManager).isFalse(); // expired
    }

    @Test
    void shouldCountActiveByAuthorityId() {
        // Given: admin authority assigned to multiple users
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(anotherUserId, adminAuthority.getId(), true, null);
        createAssignment(3000L, adminAuthority.getId(), false, null); // inactive
        createAssignment(4000L, adminAuthority.getId(), true, Instant.now().minus(1, ChronoUnit.DAYS)); // expired

        // When: count active
        Long activeCount = userAuthorityRepository.countActiveByAuthorityId(adminAuthority.getId()).block();

        // Then: 2 active users
        assertThat(activeCount).isEqualTo(2L);
    }

    @Test
    void shouldDeleteByUserId() {
        // Given: user has multiple authorities
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(testUserId, userAuthority.getId(), true, null);
        createAssignment(anotherUserId, managerAuthority.getId(), true, null);

        // Verify user has 2 assignments
        Long countBefore = userAuthorityRepository.findByUserId(testUserId).count().block();
        assertThat(countBefore).isEqualTo(2L);

        // When: delete all user assignments
        userAuthorityRepository.deleteByUserId(testUserId).block();

        // Then: user has no assignments
        Long countAfter = userAuthorityRepository.findByUserId(testUserId).count().block();
        assertThat(countAfter).isEqualTo(0L);

        // But other user still has assignment
        Long otherUserCount = userAuthorityRepository.findByUserId(anotherUserId).count().block();
        assertThat(otherUserCount).isEqualTo(1L);
    }

    @Test
    void shouldDeleteByAuthorityId() {
        // Given: admin authority assigned to multiple users
        createAssignment(testUserId, adminAuthority.getId(), true, null);
        createAssignment(anotherUserId, adminAuthority.getId(), true, null);
        createAssignment(testUserId, userAuthority.getId(), true, null);

        // Verify admin has 2 users
        Long countBefore = userAuthorityRepository.findByAuthorityId(adminAuthority.getId()).count().block();
        assertThat(countBefore).isEqualTo(2L);

        // When: delete all admin assignments
        userAuthorityRepository.deleteByAuthorityId(adminAuthority.getId()).block();

        // Then: admin has no users
        Long countAfter = userAuthorityRepository.findByAuthorityId(adminAuthority.getId()).count().block();
        assertThat(countAfter).isEqualTo(0L);

        // But user authority still has assignment
        Long userAuthorityCount = userAuthorityRepository.findByAuthorityId(userAuthority.getId()).count().block();
        assertThat(userAuthorityCount).isEqualTo(1L);
    }

    @Test
    void shouldValidateRevokedFields() {
        // Given: revoked assignment with all fields
        UserAuthority assignment = createAssignment(testUserId, adminAuthority.getId(), false, null);
        assignment.setRevokedBy("admin_user");
        assignment.setRevokedDate(Instant.now());
        assignment.setRevokedReason("User left company");

        // When: save
        UserAuthority saved = userAuthorityRepository.save(assignment).block();

        // Then: all revoked fields preserved
        assertThat(saved.getRevokedBy()).isEqualTo("admin_user");
        assertThat(saved.getRevokedDate()).isNotNull();
        assertThat(saved.getRevokedReason()).isEqualTo("User left company");
    }

    @Test
    void shouldHandleTemporaryAssignment() {
        // Given: temporary assignment (7 days)
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        UserAuthority assignment = createAssignment(testUserId, adminAuthority.getId(), true, expiresAt);

        // When: retrieve
        UserAuthority found = userAuthorityRepository.findByUserIdAndAuthorityId(testUserId, adminAuthority.getId()).block();

        // Then: expiration is set
        assertThat(found).isNotNull();
        assertThat(found.getExpiresAt()).isEqualTo(expiresAt);

        // And: is still valid (using entity method)
        assertThat(found.isValid()).isTrue();
        assertThat(found.isExpired()).isFalse();
    }

    @Test
    void shouldValidateEntityIsExpiredMethod() {
        // Given: expired assignment
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        UserAuthority expired = createAssignment(testUserId, adminAuthority.getId(), true, pastDate);

        // When: retrieve
        UserAuthority found = userAuthorityRepository.findByUserIdAndAuthorityId(testUserId, adminAuthority.getId()).block();

        // Then: entity recognizes expiration
        assertThat(found.isExpired()).isTrue();
        assertThat(found.isValid()).isFalse(); // expired = not valid
    }

    @Test
    void shouldValidateEntityIsValidMethod() {
        // Given: active non-expired assignment
        UserAuthority valid = createAssignment(testUserId, adminAuthority.getId(), true, null);

        // When: retrieve
        UserAuthority found = userAuthorityRepository.findByUserIdAndAuthorityId(testUserId, adminAuthority.getId()).block();

        // Then: entity is valid
        assertThat(found.isValid()).isTrue();
        assertThat(found.isExpired()).isFalse();

        // Given: inactive assignment
        valid.setIsActive(false);
        userAuthorityRepository.save(valid).block();

        // When: retrieve
        UserAuthority inactive = userAuthorityRepository.findByUserIdAndAuthorityId(testUserId, adminAuthority.getId()).block();

        // Then: entity is not valid (inactive)
        assertThat(inactive.isValid()).isFalse();
    }

    /**
     * Helper method to create authority
     */
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

    /**
     * Helper method to create user-authority assignment
     */
    private UserAuthority createAssignment(Long userId, Long authorityId, boolean isActive, Instant expiresAt) {
        UserAuthority assignment = new UserAuthority();
        assignment.setUserId(userId);
        assignment.setAuthorityId(authorityId);
        assignment.setIsActive(isActive);
        assignment.setExpiresAt(expiresAt);
        assignment.setAssignedBy(Constants.SYSTEM);
        assignment.setAssignedDate(Instant.now());
        return userAuthorityRepository.save(assignment).block();
    }
}
