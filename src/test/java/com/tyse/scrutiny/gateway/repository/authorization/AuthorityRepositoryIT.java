package com.tyse.scrutiny.gateway.repository.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link AuthorityRepository}.
 */
@IntegrationTest
class AuthorityRepositoryIT {

    @Autowired
    private AuthorityRepository authorityRepository;

    private Authority testAuthority;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        authorityRepository.deleteAll().block();

        // Create a test authority
        testAuthority = new Authority();
        testAuthority.setName("Test Manager");
        testAuthority.setCode("ROLE_TEST_MANAGER");
        testAuthority.setDescription("Test manager role for integration tests");
        testAuthority.setCategory(AuthorityCategory.CUSTOM);
        testAuthority.setIsSystem(false);
        testAuthority.setIsActive(true);
        testAuthority.setHierarchyLevel(100);
        testAuthority.setCreatedBy(Constants.SYSTEM);
        testAuthority.setCreatedDate(Instant.now());
    }

    @Test
    void shouldSaveAndRetrieveAuthority() {
        // When: save authority
        Authority saved = authorityRepository.save(testAuthority).block();

        // Then: verify saved
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Manager");
        assertThat(saved.getCode()).isEqualTo("ROLE_TEST_MANAGER");
        assertThat(saved.getCategory()).isEqualTo(AuthorityCategory.CUSTOM);

        // When: find by id
        Authority found = authorityRepository.findById(saved.getId()).block();

        // Then: verify found
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Manager");
        assertThat(found.getCode()).isEqualTo("ROLE_TEST_MANAGER");
    }

    @Test
    void shouldFindByCode() {
        // Given: saved authority
        authorityRepository.save(testAuthority).block();

        // When: find by code
        Authority found = authorityRepository.findByCode("ROLE_TEST_MANAGER").block();

        // Then: verify found
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Manager");
        assertThat(found.getDescription()).isEqualTo("Test manager role for integration tests");
    }

    @Test
    void shouldReturnEmptyWhenCodeNotFound() {
        // When: find non-existent code
        Authority found = authorityRepository.findByCode("ROLE_NONEXISTENT").block();

        // Then: should be null
        assertThat(found).isNull();
    }

    @Test
    void shouldFindActiveAuthorities() {
        // Given: active and inactive authorities
        testAuthority.setIsActive(true);
        authorityRepository.save(testAuthority).block();

        Authority inactive = new Authority();
        inactive.setName("Inactive Role");
        inactive.setCode("ROLE_INACTIVE");
        inactive.setDescription("Inactive test role");
        inactive.setCategory(AuthorityCategory.CUSTOM);
        inactive.setIsSystem(false);
        inactive.setIsActive(false);
        inactive.setHierarchyLevel(200);
        inactive.setCreatedBy(Constants.SYSTEM);
        inactive.setCreatedDate(Instant.now());
        authorityRepository.save(inactive).block();

        // When: find active authorities
        var activeAuthorities = authorityRepository.findByIsActiveTrue().collectList().block();

        // Then: only active authority is returned
        assertThat(activeAuthorities).hasSize(1);
        assertThat(activeAuthorities.get(0).getCode()).isEqualTo("ROLE_TEST_MANAGER");
    }

    @Test
    void shouldFindSystemAuthorities() {
        // Given: system and custom authorities
        Authority systemAuth = new Authority();
        systemAuth.setName("System Admin");
        systemAuth.setCode("ROLE_SYSTEM_ADMIN");
        systemAuth.setDescription("System administrator");
        systemAuth.setCategory(AuthorityCategory.SYSTEM);
        systemAuth.setIsSystem(true);
        systemAuth.setIsActive(true);
        systemAuth.setHierarchyLevel(0);
        systemAuth.setCreatedBy(Constants.SYSTEM);
        systemAuth.setCreatedDate(Instant.now());
        authorityRepository.save(systemAuth).block();

        testAuthority.setIsSystem(false);
        authorityRepository.save(testAuthority).block();

        // When: find system authorities
        var systemAuthorities = authorityRepository.findByIsSystemTrue().collectList().block();

        // Then: only system authority is returned
        assertThat(systemAuthorities).hasSize(1);
        assertThat(systemAuthorities.get(0).getCode()).isEqualTo("ROLE_SYSTEM_ADMIN");
        assertThat(systemAuthorities.get(0).getIsSystem()).isTrue();
    }

    @Test
    void shouldFindByCategoryAndActive() {
        // Given: multiple authorities with different categories
        testAuthority.setCategory(AuthorityCategory.CUSTOM);
        testAuthority.setIsActive(true);
        authorityRepository.save(testAuthority).block();

        Authority businessAuth = new Authority();
        businessAuth.setName("Business Manager");
        businessAuth.setCode("ROLE_BUSINESS_MANAGER");
        businessAuth.setDescription("Business management role");
        businessAuth.setCategory(AuthorityCategory.CUSTOM);
        businessAuth.setIsSystem(false);
        businessAuth.setIsActive(true);
        businessAuth.setHierarchyLevel(150);
        businessAuth.setCreatedBy(Constants.SYSTEM);
        businessAuth.setCreatedDate(Instant.now());
        authorityRepository.save(businessAuth).block();

        Authority inactiveCustom = new Authority();
        inactiveCustom.setName("Inactive Custom");
        inactiveCustom.setCode("ROLE_INACTIVE_CUSTOM");
        inactiveCustom.setDescription("Inactive custom role");
        inactiveCustom.setCategory(AuthorityCategory.CUSTOM);
        inactiveCustom.setIsSystem(false);
        inactiveCustom.setIsActive(false);
        inactiveCustom.setHierarchyLevel(250);
        inactiveCustom.setCreatedBy(Constants.SYSTEM);
        inactiveCustom.setCreatedDate(Instant.now());
        authorityRepository.save(inactiveCustom).block();

        // When: find active CUSTOM authorities
        var customAuthorities = authorityRepository.findByCategoryAndIsActiveTrue(AuthorityCategory.CUSTOM).collectList().block();

        // Then: only active custom authorities are returned
        assertThat(customAuthorities).hasSize(1);
        assertThat(customAuthorities.get(0).getCode()).isEqualTo("ROLE_TEST_MANAGER");
        assertThat(customAuthorities.get(0).getCategory()).isEqualTo(AuthorityCategory.CUSTOM);

        // When: find active CUSTOM authorities (business category doesn't exist)
        var businessAuthorities = authorityRepository.findByCategoryAndIsActiveTrue(AuthorityCategory.CUSTOM).collectList().block();

        // Then: only business authority is returned
        assertThat(businessAuthorities).hasSize(1);
        assertThat(businessAuthorities.get(0).getCode()).isEqualTo("ROLE_BUSINESS_MANAGER");
    }

    @Test
    void shouldFindByHierarchyLevel() {
        // Given: authorities with different hierarchy levels
        Authority admin = new Authority();
        admin.setName("Admin");
        admin.setCode("ROLE_ADMIN_TEST");
        admin.setDescription("Admin with level 0");
        admin.setCategory(AuthorityCategory.SYSTEM);
        admin.setIsSystem(true);
        admin.setIsActive(true);
        admin.setHierarchyLevel(0);
        admin.setCreatedBy(Constants.SYSTEM);
        admin.setCreatedDate(Instant.now());
        authorityRepository.save(admin).block();

        testAuthority.setHierarchyLevel(100);
        authorityRepository.save(testAuthority).block();

        Authority lowLevel = new Authority();
        lowLevel.setName("Low Level Role");
        lowLevel.setCode("ROLE_LOW_LEVEL");
        lowLevel.setDescription("Low privilege role");
        lowLevel.setCategory(AuthorityCategory.CUSTOM);
        lowLevel.setIsSystem(false);
        lowLevel.setIsActive(true);
        lowLevel.setHierarchyLevel(500);
        lowLevel.setCreatedBy(Constants.SYSTEM);
        lowLevel.setCreatedDate(Instant.now());
        authorityRepository.save(lowLevel).block();

        // When: find authorities with hierarchy level <= 100
        var highPrivilegeRoles = authorityRepository.findByHierarchyLevelLessThanEqual(100).collectList().block();

        // Then: only authorities with level 0 and 100 are returned
        assertThat(highPrivilegeRoles).hasSize(2);
        assertThat(highPrivilegeRoles).extracting(Authority::getHierarchyLevel).containsExactlyInAnyOrder(0, 100);
    }

    @Test
    void shouldCheckIfCodeExists() {
        // Given: saved authority
        authorityRepository.save(testAuthority).block();

        // When: check if code exists
        Boolean exists = authorityRepository.existsByCode("ROLE_TEST_MANAGER").block();
        Boolean notExists = authorityRepository.existsByCode("ROLE_NONEXISTENT").block();

        // Then: verify existence checks
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldUpdateAuthority() {
        // Given: saved authority
        Authority saved = authorityRepository.save(testAuthority).block();
        assertThat(saved).isNotNull();

        // When: update authority
        saved.setName("Updated Manager");
        saved.setDescription("Updated description");
        saved.setLastModifiedBy("admin");
        saved.setLastModifiedDate(Instant.now());
        Authority updated = authorityRepository.save(saved).block();

        // Then: verify updated
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Manager");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getLastModifiedBy()).isEqualTo("admin");
        assertThat(updated.getCode()).isEqualTo("ROLE_TEST_MANAGER"); // Code should not change
    }

    @Test
    void shouldDeleteAuthority() {
        // Given: saved authority
        Authority saved = authorityRepository.save(testAuthority).block();
        assertThat(saved).isNotNull();
        Long id = saved.getId();

        // When: delete authority
        authorityRepository.deleteById(id).block();

        // Then: authority should not exist
        Authority found = authorityRepository.findById(id).block();
        assertThat(found).isNull();
    }

    @Test
    void shouldCountAuthorities() {
        // Given: multiple authorities
        authorityRepository.save(testAuthority).block();

        Authority another = new Authority();
        another.setName("Another Role");
        another.setCode("ROLE_ANOTHER");
        another.setDescription("Another test role");
        another.setCategory(AuthorityCategory.CUSTOM);
        another.setIsSystem(false);
        another.setIsActive(true);
        another.setHierarchyLevel(150);
        another.setCreatedBy(Constants.SYSTEM);
        another.setCreatedDate(Instant.now());
        authorityRepository.save(another).block();

        // When: count authorities
        Long count = authorityRepository.count().block();

        // Then: verify count
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void shouldFindAllAuthorities() {
        // Given: multiple authorities
        authorityRepository.save(testAuthority).block();

        Authority systemAuth = new Authority();
        systemAuth.setName("System Role");
        systemAuth.setCode("ROLE_SYSTEM_TEST");
        systemAuth.setDescription("System test role");
        systemAuth.setCategory(AuthorityCategory.SYSTEM);
        systemAuth.setIsSystem(true);
        systemAuth.setIsActive(true);
        systemAuth.setHierarchyLevel(10);
        systemAuth.setCreatedBy(Constants.SYSTEM);
        systemAuth.setCreatedDate(Instant.now());
        authorityRepository.save(systemAuth).block();

        // When: find all authorities
        var allAuthorities = authorityRepository.findAll().collectList().block();

        // Then: all authorities are returned
        assertThat(allAuthorities).hasSize(2);
        assertThat(allAuthorities).extracting(Authority::getCode).containsExactlyInAnyOrder("ROLE_TEST_MANAGER", "ROLE_SYSTEM_TEST");
    }

    @Test
    void shouldValidateAuditFields() {
        // Given: authority with audit fields
        testAuthority.setCreatedBy("test_user");
        Instant createdDate = Instant.now();
        testAuthority.setCreatedDate(createdDate);

        // When: save authority
        Authority saved = authorityRepository.save(testAuthority).block();

        // Then: audit fields are preserved
        assertThat(saved).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo("test_user");
        assertThat(saved.getCreatedDate()).isNotNull();

        // When: update authority
        saved.setLastModifiedBy("admin_user");
        saved.setLastModifiedDate(Instant.now());
        Authority updated = authorityRepository.save(saved).block();

        // Then: modification audit fields are set
        assertThat(updated.getLastModifiedBy()).isEqualTo("admin_user");
        assertThat(updated.getLastModifiedDate()).isNotNull();
        assertThat(updated.getCreatedBy()).isEqualTo("test_user"); // Original creator preserved
    }

    @Test
    void shouldHandleMultipleCategories() {
        // Given: authorities in all categories
        Authority system = new Authority();
        system.setName("System");
        system.setCode("ROLE_SYS");
        system.setCategory(AuthorityCategory.SYSTEM);
        system.setIsSystem(true);
        system.setIsActive(true);
        system.setHierarchyLevel(0);
        system.setCreatedBy(Constants.SYSTEM);
        system.setCreatedDate(Instant.now());
        authorityRepository.save(system).block();

        Authority business = new Authority();
        business.setName("Business");
        business.setCode("ROLE_BIZ");
        business.setCategory(AuthorityCategory.CUSTOM);
        business.setIsSystem(false);
        business.setIsActive(true);
        business.setHierarchyLevel(100);
        business.setCreatedBy(Constants.SYSTEM);
        business.setCreatedDate(Instant.now());
        authorityRepository.save(business).block();

        testAuthority.setCategory(AuthorityCategory.CUSTOM);
        authorityRepository.save(testAuthority).block();

        // When: query all
        var all = authorityRepository.findAll().collectList().block();

        // Then: all categories are present (SYSTEM and CUSTOM, business role also uses CUSTOM)
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Authority::getCategory).contains(AuthorityCategory.SYSTEM, AuthorityCategory.CUSTOM);
    }

    @Test
    void shouldFindAllWithUserCount() {
        // Given: saved authorities
        authorityRepository.save(testAuthority).block();

        Authority another = new Authority();
        another.setName("Another Role");
        another.setCode("ROLE_ANOTHER_TEST");
        another.setDescription("Another role for testing");
        another.setCategory(AuthorityCategory.CUSTOM);
        another.setIsSystem(false);
        another.setIsActive(true);
        another.setHierarchyLevel(200);
        another.setCreatedBy(Constants.SYSTEM);
        another.setCreatedDate(Instant.now());
        authorityRepository.save(another).block();

        // When: find all with user count (custom query)
        var authoritiesWithCount = authorityRepository.findAllWithUserCount().collectList().block();

        // Then: authorities are returned (user count will be 0 without actual user assignments)
        assertThat(authoritiesWithCount).isNotNull();
        assertThat(authoritiesWithCount).hasSizeGreaterThanOrEqualTo(2);
    }
}
