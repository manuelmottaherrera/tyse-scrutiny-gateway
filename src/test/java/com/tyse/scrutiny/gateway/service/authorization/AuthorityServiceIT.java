package com.tyse.scrutiny.gateway.service.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityAuditRepository;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.AuthorityAlreadyExistsException;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.AuthorityNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link AuthorityService}.
 */
@IntegrationTest
class AuthorityServiceIT {

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AuthorityAuditRepository authorityAuditRepository;

    @Autowired
    private com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private com.tyse.scrutiny.gateway.repository.authorization.UserPermissionRepository userPermissionRepository;

    private ServerWebExchange mockExchange;

    @BeforeEach
    void setUp() {
        // Clean up before each test - delete child entities first to avoid FK violations
        authorityAuditRepository.deleteAll().block();
        authorityPermissionRepository.deleteAll().block();
        userAuthorityRepository.deleteAll().block();
        userPermissionRepository.deleteAll().block();
        authorityRepository.deleteAll().block();

        // Create mock exchange for audit logging
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test")
            .remoteAddress(new java.net.InetSocketAddress("192.168.1.100", 8080))
            .header(HttpHeaders.USER_AGENT, "Test User Agent")
            .build();
        mockExchange = MockServerWebExchange.from(request);
    }

    @Test
    void shouldCreateAuthority() {
        // Given: new authority
        Authority authority = new Authority();
        authority.setName("Test Manager");
        authority.setCode("ROLE_TEST_MANAGER");
        authority.setDescription("Manager role for testing");
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(false);
        authority.setHierarchyLevel(100);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());

        // When: create authority
        StepVerifier.create(authorityService.createAuthority(authority, mockExchange))
            // Then: authority is created
            .assertNext(created -> {
                assertThat(created.getId()).isNotNull();
                assertThat(created.getCode()).isEqualTo("ROLE_TEST_MANAGER");
                assertThat(created.getIsActive()).isTrue(); // default
            })
            .verifyComplete();

        // Verify audit log was created
        StepVerifier.create(authorityAuditRepository.findByAuthorityIdOrderByChangedDateDesc(authority.getId()))
            .assertNext(audit -> {
                assertThat(audit.getAction()).isEqualTo(AuditAction.CREATED);
                assertThat(audit.getOldValues()).isNull();
                assertThat(audit.getNewValues()).isNotNull();
                assertThat(audit.getIpAddress()).isEqualTo("192.168.1.100");
            })
            .verifyComplete();
    }

    @Test
    void shouldSetDefaultIsActiveOnCreate() {
        // Given: authority without isActive set
        Authority authority = new Authority();
        authority.setName("Test Role");
        authority.setCode("ROLE_TEST");
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(false);
        authority.setHierarchyLevel(100);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());
        // isActive not set

        // When: create authority
        StepVerifier.create(authorityService.createAuthority(authority, mockExchange))
            // Then: isActive defaults to true
            .assertNext(created -> {
                assertThat(created.getIsActive()).isTrue();
            })
            .verifyComplete();
    }

    @Test
    void shouldFailToCreateDuplicateAuthority() {
        // Given: existing authority
        Authority existing = new Authority();
        existing.setName("Admin");
        existing.setCode("ROLE_ADMIN_TEST");
        existing.setCategory(AuthorityCategory.SYSTEM);
        existing.setIsSystem(true);
        existing.setIsActive(true);
        existing.setHierarchyLevel(0);
        existing.setCreatedBy(Constants.SYSTEM);
        existing.setCreatedDate(Instant.now());
        authorityRepository.save(existing).block();

        // Given: duplicate authority with same code
        Authority duplicate = new Authority();
        duplicate.setName("Another Admin");
        duplicate.setCode("ROLE_ADMIN_TEST"); // Same code
        duplicate.setCategory(AuthorityCategory.CUSTOM);
        duplicate.setIsSystem(false);
        duplicate.setIsActive(true);
        duplicate.setHierarchyLevel(10);
        duplicate.setCreatedBy(Constants.SYSTEM);
        duplicate.setCreatedDate(Instant.now());

        // When/Then: creation fails
        StepVerifier.create(authorityService.createAuthority(duplicate, mockExchange))
            .expectError(AuthorityAlreadyExistsException.class)
            .verify();
    }

    @Test
    void shouldUpdateAuthority() {
        // Given: existing custom authority
        Authority existing = new Authority();
        existing.setName("Manager");
        existing.setCode("ROLE_MANAGER_TEST");
        existing.setDescription("Original description");
        existing.setCategory(AuthorityCategory.CUSTOM);
        existing.setIsSystem(false);
        existing.setIsActive(true);
        existing.setHierarchyLevel(100);
        existing.setCreatedBy(Constants.SYSTEM);
        existing.setCreatedDate(Instant.now());
        Authority saved = authorityRepository.save(existing).block();

        // Given: updated data
        Authority updated = new Authority();
        updated.setName("Senior Manager");
        updated.setDescription("Updated description");
        updated.setCategory(AuthorityCategory.CUSTOM);
        updated.setHierarchyLevel(50);
        updated.setIsActive(true);

        // When: update authority
        StepVerifier.create(authorityService.updateAuthority(saved.getId(), updated, mockExchange))
            // Then: authority is updated
            .assertNext(result -> {
                assertThat(result.getName()).isEqualTo("Senior Manager");
                assertThat(result.getDescription()).isEqualTo("Updated description");
                assertThat(result.getCategory()).isEqualTo(AuthorityCategory.CUSTOM);
                assertThat(result.getHierarchyLevel()).isEqualTo(50);
                assertThat(result.getCode()).isEqualTo("ROLE_MANAGER_TEST"); // Code not changed
            })
            .verifyComplete();

        // Verify audit log was created
        StepVerifier.create(authorityAuditRepository.findByAuthorityIdOrderByChangedDateDesc(saved.getId()))
            .assertNext(audit -> {
                assertThat(audit.getAction()).isEqualTo(AuditAction.UPDATED);
                assertThat(audit.getOldValues()).isNotNull();
                assertThat(audit.getNewValues()).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    void shouldFailToUpdateNonExistentAuthority() {
        // Given: non-existent authority ID
        Long nonExistentId = 999999L;

        Authority updated = new Authority();
        updated.setName("Updated");
        updated.setCategory(AuthorityCategory.CUSTOM);
        updated.setHierarchyLevel(100);

        // When/Then: update fails
        StepVerifier.create(authorityService.updateAuthority(nonExistentId, updated, mockExchange))
            .expectError(AuthorityNotFoundException.class)
            .verify();
    }

    @Test
    void shouldFailToUpdateSystemAuthority() {
        // Given: existing system authority
        Authority systemAuth = new Authority();
        systemAuth.setName("System Admin");
        systemAuth.setCode("ROLE_SYSTEM_ADMIN_TEST");
        systemAuth.setDescription("System authority");
        systemAuth.setCategory(AuthorityCategory.SYSTEM);
        systemAuth.setIsSystem(true); // System authority
        systemAuth.setIsActive(true);
        systemAuth.setHierarchyLevel(0);
        systemAuth.setCreatedBy(Constants.SYSTEM);
        systemAuth.setCreatedDate(Instant.now());
        Authority saved = authorityRepository.save(systemAuth).block();

        // Given: attempted update
        Authority updated = new Authority();
        updated.setName("Modified System Admin");
        updated.setCategory(AuthorityCategory.CUSTOM);
        updated.setHierarchyLevel(100);

        // When/Then: update fails
        StepVerifier.create(authorityService.updateAuthority(saved.getId(), updated, mockExchange))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void shouldDeactivateAuthority() {
        // Given: existing active authority
        Authority existing = new Authority();
        existing.setName("Manager");
        existing.setCode("ROLE_MANAGER_TEST");
        existing.setCategory(AuthorityCategory.CUSTOM);
        existing.setIsSystem(false);
        existing.setIsActive(true);
        existing.setHierarchyLevel(100);
        existing.setCreatedBy(Constants.SYSTEM);
        existing.setCreatedDate(Instant.now());
        Authority saved = authorityRepository.save(existing).block();

        // When: deactivate authority
        StepVerifier.create(authorityService.deactivateAuthority(saved.getId(), mockExchange)).verifyComplete();

        // Then: authority is deactivated
        StepVerifier.create(authorityRepository.findById(saved.getId()))
            .assertNext(deactivated -> {
                assertThat(deactivated.getIsActive()).isFalse();
            })
            .verifyComplete();

        // Verify audit log
        StepVerifier.create(authorityAuditRepository.findByAuthorityIdOrderByChangedDateDesc(saved.getId()))
            .assertNext(audit -> {
                assertThat(audit.getAction()).isEqualTo(AuditAction.DEACTIVATED);
            })
            .verifyComplete();
    }

    @Test
    void shouldFailToDeactivateSystemAuthority() {
        // Given: system authority
        Authority systemAuth = new Authority();
        systemAuth.setName("System Admin");
        systemAuth.setCode("ROLE_SYSTEM_ADMIN_TEST");
        systemAuth.setCategory(AuthorityCategory.SYSTEM);
        systemAuth.setIsSystem(true);
        systemAuth.setIsActive(true);
        systemAuth.setHierarchyLevel(0);
        systemAuth.setCreatedBy(Constants.SYSTEM);
        systemAuth.setCreatedDate(Instant.now());
        Authority saved = authorityRepository.save(systemAuth).block();

        // When/Then: deactivation fails
        StepVerifier.create(authorityService.deactivateAuthority(saved.getId(), mockExchange))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void shouldFailToDeactivateNonExistentAuthority() {
        // Given: non-existent authority ID
        Long nonExistentId = 999999L;

        // When/Then: deactivation fails
        StepVerifier.create(authorityService.deactivateAuthority(nonExistentId, mockExchange))
            .expectError(AuthorityNotFoundException.class)
            .verify();
    }

    @Test
    void shouldFindActiveAuthorities() {
        // Given: mix of active and inactive authorities
        Authority active1 = createAuthority("ROLE_ACTIVE1", "Active 1", true);
        Authority active2 = createAuthority("ROLE_ACTIVE2", "Active 2", true);
        Authority inactive = createAuthority("ROLE_INACTIVE", "Inactive", false);

        // When: find active authorities
        StepVerifier.create(authorityService.findActiveAuthorities())
            // Then: only active authorities returned
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldFindByCategory() {
        // Given: authorities in different categories
        Authority system = new Authority();
        system.setName("System");
        system.setCode("ROLE_SYSTEM_TEST");
        system.setCategory(AuthorityCategory.SYSTEM);
        system.setIsSystem(true);
        system.setIsActive(true);
        system.setHierarchyLevel(0);
        system.setCreatedBy(Constants.SYSTEM);
        system.setCreatedDate(Instant.now());
        authorityRepository.save(system).block();

        Authority business = new Authority();
        business.setName("Business");
        business.setCode("ROLE_BUSINESS_TEST");
        business.setCategory(AuthorityCategory.CUSTOM);
        business.setIsSystem(false);
        business.setIsActive(true);
        business.setHierarchyLevel(100);
        business.setCreatedBy(Constants.SYSTEM);
        business.setCreatedDate(Instant.now());
        authorityRepository.save(business).block();

        Authority custom = new Authority();
        custom.setName("Custom");
        custom.setCode("ROLE_CUSTOM_TEST");
        custom.setCategory(AuthorityCategory.CUSTOM);
        custom.setIsSystem(false);
        custom.setIsActive(true);
        custom.setHierarchyLevel(200);
        custom.setCreatedBy(Constants.SYSTEM);
        custom.setCreatedDate(Instant.now());
        authorityRepository.save(custom).block();

        // When: find by CUSTOM category (business role uses CUSTOM category)
        StepVerifier.create(authorityService.findByCategory(AuthorityCategory.CUSTOM))
            // Then: only custom authority returned
            .assertNext(auth -> {
                assertThat(auth.getCategory()).isEqualTo(AuthorityCategory.CUSTOM);
                assertThat(auth.getCode()).isEqualTo("ROLE_BUSINESS_TEST");
            })
            .verifyComplete();
    }

    @Test
    void shouldFindByCode() {
        // Given: existing authority
        Authority authority = createAuthority("ROLE_FIND_TEST", "Find Test", true);

        // When: find by code
        StepVerifier.create(authorityService.findByCode("ROLE_FIND_TEST"))
            // Then: authority found
            .assertNext(found -> {
                assertThat(found.getCode()).isEqualTo("ROLE_FIND_TEST");
                assertThat(found.getName()).isEqualTo("Find Test");
            })
            .verifyComplete();
    }

    @Test
    void shouldFailToFindByNonExistentCode() {
        // When/Then: find fails
        StepVerifier.create(authorityService.findByCode("ROLE_NONEXISTENT")).expectError(AuthorityNotFoundException.class).verify();
    }

    @Test
    void shouldGetAuditHistory() {
        // Given: authority with multiple changes
        Authority authority = new Authority();
        authority.setName("Test");
        authority.setCode("ROLE_AUDIT_TEST");
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(false);
        authority.setIsActive(true);
        authority.setHierarchyLevel(100);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());

        // Create authority (generates CREATED audit)
        Authority created = authorityService.createAuthority(authority, mockExchange).block();

        // Update authority (generates UPDATED audit)
        Authority updated = new Authority();
        updated.setName("Test Updated");
        updated.setCategory(AuthorityCategory.CUSTOM);
        updated.setHierarchyLevel(50);
        authorityService.updateAuthority(created.getId(), updated, mockExchange).block();

        // When: get audit history
        StepVerifier.create(authorityService.getAuditHistory(created.getId()))
            // Then: 2 audit records (most recent first)
            .expectNextCount(2)
            .verifyComplete();

        // Verify order (most recent first)
        StepVerifier.create(authorityService.getAuditHistory(created.getId()).take(1))
            .assertNext(audit -> {
                assertThat(audit.getAction()).isEqualTo(AuditAction.UPDATED);
            })
            .verifyComplete();
    }

    @Test
    void shouldCreateAuditWithoutExchange() {
        // Given: authority without exchange (exchange = null)
        Authority authority = new Authority();
        authority.setName("Test No Exchange");
        authority.setCode("ROLE_NO_EXCHANGE");
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(false);
        authority.setIsActive(true);
        authority.setHierarchyLevel(100);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());

        // When: create authority without exchange
        Authority created = authorityService.createAuthority(authority, null).block();

        // Then: authority is created and audit log exists (without IP/UA)
        StepVerifier.create(authorityAuditRepository.findByAuthorityIdOrderByChangedDateDesc(created.getId()))
            .assertNext(audit -> {
                assertThat(audit.getAction()).isEqualTo(AuditAction.CREATED);
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleHierarchyLevels() {
        // Given: authorities with different hierarchy levels
        Authority high = createAuthorityWithHierarchy("ROLE_HIGH", "High Priority", 0);
        Authority medium = createAuthorityWithHierarchy("ROLE_MEDIUM", "Medium Priority", 100);
        Authority low = createAuthorityWithHierarchy("ROLE_LOW", "Low Priority", 500);

        // Then: verify hierarchy levels
        assertThat(high.getHierarchyLevel()).isEqualTo(0);
        assertThat(medium.getHierarchyLevel()).isEqualTo(100);
        assertThat(low.getHierarchyLevel()).isEqualTo(500);
    }

    /**
     * Helper method to create authority
     */
    private Authority createAuthority(String code, String name, boolean isActive) {
        Authority authority = new Authority();
        authority.setCode(code);
        authority.setName(name);
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(false);
        authority.setIsActive(isActive);
        authority.setHierarchyLevel(100);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());
        return authorityRepository.save(authority).block();
    }

    /**
     * Helper method to create authority with specific hierarchy
     */
    private Authority createAuthorityWithHierarchy(String code, String name, int hierarchyLevel) {
        Authority authority = new Authority();
        authority.setCode(code);
        authority.setName(name);
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(false);
        authority.setIsActive(true);
        authority.setHierarchyLevel(hierarchyLevel);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());
        return authorityRepository.save(authority).block();
    }
}
