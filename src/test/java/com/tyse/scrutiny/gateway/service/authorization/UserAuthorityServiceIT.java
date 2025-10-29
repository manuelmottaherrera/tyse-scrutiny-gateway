package com.tyse.scrutiny.gateway.service.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for {@link UserAuthorityService}.
 */
@IntegrationTest
@WithMockUser(username = "test-user")
class UserAuthorityServiceIT {

    private static final String TEST_USER_LOGIN = "testuser_authority";
    private static final String TEST_USER_EMAIL = "testuser_authority@localhost";
    private static final String TEST_AUTHORITY_CODE = "ROLE_TEST";
    private static final String TEST_AUTHORITY_NAME = "Test Authority";

    @Autowired
    private UserAuthorityService userAuthorityService;

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Authority authority;

    @BeforeEach
    void init() {
        // Create test user
        user = new User();
        user.setLogin(TEST_USER_LOGIN);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(TEST_USER_EMAIL);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        user = userRepository.save(user).block();

        // Create test authority
        authority = new Authority();
        authority.setCode(TEST_AUTHORITY_CODE);
        authority.setName(TEST_AUTHORITY_NAME);
        authority.setDescription("Test authority for integration tests");
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsActive(true);
        authority.setIsSystem(false);
        authority.setHierarchyLevel(5);
        authority.setCreatedBy(Constants.SYSTEM);
        authority = authorityRepository.save(authority).block();
    }

    @AfterEach
    void cleanup() {
        // Delete all user_authority assignments first (to avoid FK violations)
        userAuthorityRepository.deleteAll().block();

        // Delete all test users (matching pattern)
        userRepository
            .findAll()
            .filter(u -> u.getLogin().startsWith("testuser"))
            .flatMap(u -> userRepository.deleteById(u.getId()))
            .blockLast();

        // Delete all test authorities (matching pattern)
        authorityRepository
            .findAll()
            .filter(a -> a.getCode().startsWith("ROLE_TEST") || a.getCode().startsWith("ROLE_EXPIRED"))
            .flatMap(a -> authorityRepository.deleteById(a.getId()))
            .blockLast();
    }

    @Test
    void testAssignAuthority() {
        // When: Assign authority to user
        UserAuthority assignment = userAuthorityService.assignAuthority(user.getId(), authority.getId(), null).block();

        // Then: Assignment created successfully
        assertThat(assignment).isNotNull();
        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getUserId()).isEqualTo(user.getId());
        assertThat(assignment.getAuthorityId()).isEqualTo(authority.getId());
        assertThat(assignment.getIsActive()).isTrue();
        assertThat(assignment.getAssignedBy()).isEqualTo("test-user");
        assertThat(assignment.getAssignedDate()).isNotNull();
        assertThat(assignment.getExpiresAt()).isNull();
        assertThat(assignment.getRevokedBy()).isNull();
        assertThat(assignment.getRevokedDate()).isNull();
    }

    @Test
    void testAssignAuthorityWithExpiration() {
        // Given: Expiration date in the future
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        // When: Assign authority with expiration
        UserAuthority assignment = userAuthorityService.assignAuthority(user.getId(), authority.getId(), expiresAt).block();

        // Then: Assignment created with expiration
        assertThat(assignment).isNotNull();
        assertThat(assignment.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(assignment.isExpired()).isFalse();
        assertThat(assignment.isValid()).isTrue();
    }

    @Test
    void testAssignmentIsExpired() {
        // Given: Expiration date in the past
        Instant expiresAt = Instant.now().minus(1, ChronoUnit.DAYS);

        // When: Assign authority with past expiration
        UserAuthority assignment = userAuthorityService.assignAuthority(user.getId(), authority.getId(), expiresAt).block();

        // Then: Assignment is expired
        assertThat(assignment).isNotNull();
        assertThat(assignment.isExpired()).isTrue();
        assertThat(assignment.isValid()).isFalse();
    }

    @Test
    void testGetValidAuthorities() {
        // Given: One valid assignment and one expired
        Instant futureDate = Instant.now().plus(7, ChronoUnit.DAYS);
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);

        userAuthorityService.assignAuthority(user.getId(), authority.getId(), futureDate).block();

        // Create second authority (expired)
        Authority expiredAuthority = new Authority();
        expiredAuthority.setCode("ROLE_EXPIRED");
        expiredAuthority.setName("Expired Authority");
        expiredAuthority.setCategory(AuthorityCategory.CUSTOM);
        expiredAuthority.setIsActive(true);
        expiredAuthority.setIsSystem(false);
        expiredAuthority.setCreatedBy(Constants.SYSTEM);
        expiredAuthority = authorityRepository.save(expiredAuthority).block();

        userAuthorityService.assignAuthority(user.getId(), expiredAuthority.getId(), pastDate).block();

        // When: Get valid authorities
        List<UserAuthority> validAuthorities = userAuthorityService.getValidAuthorities(user.getId()).collectList().block();

        // Then: Only non-expired authority returned
        assertThat(validAuthorities).hasSize(1);
        assertThat(validAuthorities.get(0).getAuthorityId()).isEqualTo(authority.getId());

        // Cleanup
        authorityRepository.deleteById(expiredAuthority.getId()).block();
    }

    @Test
    void testRevokeAuthority() {
        // Given: An active assignment
        UserAuthority assignment = userAuthorityService.assignAuthority(user.getId(), authority.getId(), null).block();
        assertThat(assignment.getIsActive()).isTrue();

        // When: Revoke the authority
        String reason = "Test revocation";
        UserAuthority revokedAssignment = userAuthorityService.revokeAuthority(user.getId(), authority.getId(), reason).block();

        // Then: Assignment is revoked
        assertThat(revokedAssignment).isNotNull();
        assertThat(revokedAssignment.getIsActive()).isFalse();
        assertThat(revokedAssignment.getRevokedBy()).isEqualTo("test-user");
        assertThat(revokedAssignment.getRevokedDate()).isNotNull();
        assertThat(revokedAssignment.getRevokedReason()).isEqualTo(reason);
        assertThat(revokedAssignment.isValid()).isFalse();
    }

    @Test
    void testRevokeNonExistentAuthority() {
        // When: Try to revoke an authority that was never assigned
        // Then: Should throw error
        assertThatThrownBy(() -> userAuthorityService.revokeAuthority(user.getId(), authority.getId(), "reason").block())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No active authority assignment found");
    }

    @Test
    void testUserHasAuthority() {
        // Given: Authority assigned to user
        userAuthorityService.assignAuthority(user.getId(), authority.getId(), null).block();

        // When: Check if user has authority
        Boolean hasAuthority = userAuthorityService.userHasAuthority(user.getId(), TEST_AUTHORITY_CODE).block();

        // Then: Returns true
        assertThat(hasAuthority).isTrue();
    }

    @Test
    void testUserDoesNotHaveAuthority() {
        // When: Check if user has authority (without assigning)
        Boolean hasAuthority = userAuthorityService.userHasAuthority(user.getId(), TEST_AUTHORITY_CODE).block();

        // Then: Returns false
        assertThat(hasAuthority).isFalse();
    }

    @Test
    void testUserHasAuthorityReturnsFalseAfterRevocation() {
        // Given: Authority assigned then revoked
        userAuthorityService.assignAuthority(user.getId(), authority.getId(), null).block();
        userAuthorityService.revokeAuthority(user.getId(), authority.getId(), "test").block();

        // When: Check if user has authority
        Boolean hasAuthority = userAuthorityService.userHasAuthority(user.getId(), TEST_AUTHORITY_CODE).block();

        // Then: Returns false
        assertThat(hasAuthority).isFalse();
    }

    @Test
    void testFindExpiredAssignments() {
        // Given: Two assignments, one expired
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        userAuthorityService.assignAuthority(user.getId(), authority.getId(), pastDate).block();

        // Create second user with non-expired assignment
        User user2Temp = new User();
        user2Temp.setLogin("testuser2");
        user2Temp.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user2Temp.setActivated(true);
        user2Temp.setEmail("testuser2@localhost");
        user2Temp.setFirstName("Test2");
        user2Temp.setLastName("User2");
        user2Temp.setLangKey("en");
        user2Temp.setCreatedBy(Constants.SYSTEM);
        final User user2 = userRepository.save(user2Temp).block();

        Instant futureDate = Instant.now().plus(7, ChronoUnit.DAYS);
        userAuthorityService.assignAuthority(user2.getId(), authority.getId(), futureDate).block();

        // When: Find expired assignments
        List<UserAuthority> expiredAssignments = userAuthorityService.findExpiredAssignments().collectList().block();

        // Then: Only expired assignment returned
        assertThat(expiredAssignments).hasSizeGreaterThanOrEqualTo(1);
        assertThat(expiredAssignments).anyMatch(a -> a.getUserId().equals(user.getId()) && a.isExpired());

        // Cleanup
        userAuthorityRepository.deleteByUserId(user2.getId()).block();
        userRepository.deleteById(user2.getId()).block();
    }

    @Test
    void testFindExpiringWithinDays() {
        // Given: Assignment expiring in 3 days
        Instant expiresIn3Days = Instant.now().plus(3, ChronoUnit.DAYS);
        userAuthorityService.assignAuthority(user.getId(), authority.getId(), expiresIn3Days).block();

        // Create assignment expiring in 10 days
        User user2Temp = new User();
        user2Temp.setLogin("testuser3");
        user2Temp.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user2Temp.setActivated(true);
        user2Temp.setEmail("testuser3@localhost");
        user2Temp.setFirstName("Test3");
        user2Temp.setLastName("User3");
        user2Temp.setLangKey("en");
        user2Temp.setCreatedBy(Constants.SYSTEM);
        final User user2 = userRepository.save(user2Temp).block();

        Instant expiresIn10Days = Instant.now().plus(10, ChronoUnit.DAYS);
        userAuthorityService.assignAuthority(user2.getId(), authority.getId(), expiresIn10Days).block();

        // When: Find assignments expiring within 7 days
        List<UserAuthority> expiringAssignments = userAuthorityService.findExpiringWithinDays(7).collectList().block();

        // Then: Only assignment expiring in 3 days returned
        assertThat(expiringAssignments).isNotEmpty();
        assertThat(expiringAssignments).anyMatch(a -> a.getUserId().equals(user.getId()));
        assertThat(expiringAssignments).noneMatch(a -> a.getUserId().equals(user2.getId()));

        // Cleanup
        userAuthorityRepository.deleteByUserId(user2.getId()).block();
        userRepository.deleteById(user2.getId()).block();
    }

    @Test
    void testGetAllAuthorities() {
        // Given: Two assignments (one active, one revoked)
        userAuthorityService.assignAuthority(user.getId(), authority.getId(), null).block();

        Authority authority2Temp = new Authority();
        authority2Temp.setCode("ROLE_TEST2");
        authority2Temp.setName("Test Authority 2");
        authority2Temp.setCategory(AuthorityCategory.CUSTOM);
        authority2Temp.setIsActive(true);
        authority2Temp.setIsSystem(false);
        authority2Temp.setCreatedBy(Constants.SYSTEM);
        final Authority authority2 = authorityRepository.save(authority2Temp).block();

        userAuthorityService.assignAuthority(user.getId(), authority2.getId(), null).block();
        userAuthorityService.revokeAuthority(user.getId(), authority2.getId(), "test").block();

        // When: Get all authorities (including inactive)
        List<UserAuthority> allAuthorities = userAuthorityService.getAllAuthorities(user.getId()).collectList().block();

        // Then: Both assignments returned
        assertThat(allAuthorities).hasSize(2);
        assertThat(allAuthorities).anyMatch(a -> a.getAuthorityId().equals(authority.getId()) && a.getIsActive());
        assertThat(allAuthorities).anyMatch(a -> a.getAuthorityId().equals(authority2.getId()) && !a.getIsActive());

        // Cleanup
        authorityRepository.deleteById(authority2.getId()).block();
    }

    @Test
    void testDeleteAssignment() {
        // Given: An assignment
        UserAuthority assignment = userAuthorityService.assignAuthority(user.getId(), authority.getId(), null).block();
        Long assignmentId = assignment.getId();

        // When: Delete the assignment
        userAuthorityService.deleteAssignment(assignmentId).block();

        // Then: Assignment no longer exists
        UserAuthority deletedAssignment = userAuthorityRepository.findById(assignmentId).block();
        assertThat(deletedAssignment).isNull();
    }
}
