package com.tyse.scrutiny.gateway.repository.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityPermission;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link AuthorityPermissionRepository}.
 */
@IntegrationTest
class AuthorityPermissionRepositoryIT {

    @Autowired
    private AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Authority adminAuthority;
    private Authority userAuthority;
    private Permission createPermission;
    private Permission readPermission;
    private Permission updatePermission;
    private Permission deletePermission;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        authorityPermissionRepository.deleteAll().block();
        permissionRepository.deleteAll().block();
        authorityRepository.deleteAll().block();

        // Create test authorities
        adminAuthority = new Authority();
        adminAuthority.setName("Administrator");
        adminAuthority.setCode("ROLE_ADMIN_TEST");
        adminAuthority.setDescription("Admin role for testing");
        adminAuthority.setCategory(AuthorityCategory.SYSTEM);
        adminAuthority.setIsSystem(true);
        adminAuthority.setIsActive(true);
        adminAuthority.setHierarchyLevel(0);
        adminAuthority.setCreatedBy(Constants.SYSTEM);
        adminAuthority.setCreatedDate(Instant.now());
        adminAuthority = authorityRepository.save(adminAuthority).block();

        userAuthority = new Authority();
        userAuthority.setName("User");
        userAuthority.setCode("ROLE_USER_TEST");
        userAuthority.setDescription("User role for testing");
        userAuthority.setCategory(AuthorityCategory.SYSTEM);
        userAuthority.setIsSystem(true);
        userAuthority.setIsActive(true);
        userAuthority.setHierarchyLevel(500);
        userAuthority.setCreatedBy(Constants.SYSTEM);
        userAuthority.setCreatedDate(Instant.now());
        userAuthority = authorityRepository.save(userAuthority).block();

        // Create test permissions
        createPermission = new Permission();
        createPermission.setName("user.create");
        createPermission.setResource("user");
        createPermission.setAction("create");
        createPermission.setDescription("Create users");
        createPermission.setIsActive(true);
        createPermission.setCreatedBy(Constants.SYSTEM);
        createPermission.setCreatedDate(Instant.now());
        createPermission = permissionRepository.save(createPermission).block();

        readPermission = new Permission();
        readPermission.setName("user.read");
        readPermission.setResource("user");
        readPermission.setAction("read");
        readPermission.setDescription("Read users");
        readPermission.setIsActive(true);
        readPermission.setCreatedBy(Constants.SYSTEM);
        readPermission.setCreatedDate(Instant.now());
        readPermission = permissionRepository.save(readPermission).block();

        updatePermission = new Permission();
        updatePermission.setName("user.update");
        updatePermission.setResource("user");
        updatePermission.setAction("update");
        updatePermission.setDescription("Update users");
        updatePermission.setIsActive(true);
        updatePermission.setCreatedBy(Constants.SYSTEM);
        updatePermission.setCreatedDate(Instant.now());
        updatePermission = permissionRepository.save(updatePermission).block();

        deletePermission = new Permission();
        deletePermission.setName("user.delete");
        deletePermission.setResource("user");
        deletePermission.setAction("delete");
        deletePermission.setDescription("Delete users");
        deletePermission.setIsActive(true);
        deletePermission.setCreatedBy(Constants.SYSTEM);
        deletePermission.setCreatedDate(Instant.now());
        deletePermission = permissionRepository.save(deletePermission).block();
    }

    @Test
    void shouldSaveAndRetrieveAuthorityPermission() {
        // Given: authority-permission mapping
        AuthorityPermission mapping = new AuthorityPermission();
        mapping.setAuthorityId(adminAuthority.getId());
        mapping.setPermissionId(createPermission.getId());
        mapping.setGrantedBy(Constants.SYSTEM);
        mapping.setGrantedDate(Instant.now());

        // When: save mapping
        AuthorityPermission saved = authorityPermissionRepository.save(mapping).block();

        // Then: verify saved
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAuthorityId()).isEqualTo(adminAuthority.getId());
        assertThat(saved.getPermissionId()).isEqualTo(createPermission.getId());
        assertThat(saved.getGrantedBy()).isEqualTo(Constants.SYSTEM);

        // When: find by id
        AuthorityPermission found = authorityPermissionRepository.findById(saved.getId()).block();

        // Then: verify found
        assertThat(found).isNotNull();
        assertThat(found.getAuthorityId()).isEqualTo(adminAuthority.getId());
        assertThat(found.getPermissionId()).isEqualTo(createPermission.getId());
    }

    @Test
    void shouldFindByAuthorityId() {
        // Given: admin has 3 permissions, user has 1 permission
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), updatePermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // When: find permissions for admin
        var adminMappings = authorityPermissionRepository.findByAuthorityId(adminAuthority.getId()).collectList().block();

        // Then: admin has 3 mappings
        assertThat(adminMappings).hasSize(3);
        assertThat(adminMappings)
            .extracting(AuthorityPermission::getPermissionId)
            .containsExactlyInAnyOrder(createPermission.getId(), readPermission.getId(), updatePermission.getId());

        // When: find permissions for user
        var userMappings = authorityPermissionRepository.findByAuthorityId(userAuthority.getId()).collectList().block();

        // Then: user has 1 mapping
        assertThat(userMappings).hasSize(1);
        assertThat(userMappings.get(0).getPermissionId()).isEqualTo(readPermission.getId());
    }

    @Test
    void shouldFindByPermissionId() {
        // Given: both admin and user have read permission, only admin has create
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // When: find authorities with read permission
        var readMappings = authorityPermissionRepository.findByPermissionId(readPermission.getId()).collectList().block();

        // Then: both authorities have read
        assertThat(readMappings).hasSize(2);
        assertThat(readMappings)
            .extracting(AuthorityPermission::getAuthorityId)
            .containsExactlyInAnyOrder(adminAuthority.getId(), userAuthority.getId());

        // When: find authorities with create permission
        var createMappings = authorityPermissionRepository.findByPermissionId(createPermission.getId()).collectList().block();

        // Then: only admin has create
        assertThat(createMappings).hasSize(1);
        assertThat(createMappings.get(0).getAuthorityId()).isEqualTo(adminAuthority.getId());
    }

    @Test
    void shouldCheckIfMappingExists() {
        // Given: admin has create permission
        createMapping(adminAuthority.getId(), createPermission.getId());

        // When: check existence
        Boolean exists = authorityPermissionRepository
            .existsByAuthorityIdAndPermissionId(adminAuthority.getId(), createPermission.getId())
            .block();
        Boolean notExists = authorityPermissionRepository
            .existsByAuthorityIdAndPermissionId(userAuthority.getId(), createPermission.getId())
            .block();

        // Then: verify
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldDeleteByAuthorityIdAndPermissionId() {
        // Given: admin has create permission
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());

        // Verify it exists
        Boolean existsBefore = authorityPermissionRepository
            .existsByAuthorityIdAndPermissionId(adminAuthority.getId(), createPermission.getId())
            .block();
        assertThat(existsBefore).isTrue();

        // When: delete specific mapping
        Long deletedCount = authorityPermissionRepository
            .deleteByAuthorityIdAndPermissionId(adminAuthority.getId(), createPermission.getId())
            .block();

        // Then: mapping is deleted
        assertThat(deletedCount).isEqualTo(1L);

        Boolean existsAfter = authorityPermissionRepository
            .existsByAuthorityIdAndPermissionId(adminAuthority.getId(), createPermission.getId())
            .block();
        assertThat(existsAfter).isFalse();

        // But other mapping still exists
        Boolean readStillExists = authorityPermissionRepository
            .existsByAuthorityIdAndPermissionId(adminAuthority.getId(), readPermission.getId())
            .block();
        assertThat(readStillExists).isTrue();
    }

    @Test
    void shouldDeleteNonExistentMapping() {
        // When: delete non-existent mapping
        Long deletedCount = authorityPermissionRepository
            .deleteByAuthorityIdAndPermissionId(adminAuthority.getId(), createPermission.getId())
            .block();

        // Then: no rows deleted
        assertThat(deletedCount).isEqualTo(0L);
    }

    @Test
    void shouldDeleteByAuthorityId() {
        // Given: admin has 4 permissions, user has 1
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), updatePermission.getId());
        createMapping(adminAuthority.getId(), deletePermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // Verify admin has 4
        Long adminCountBefore = authorityPermissionRepository.countByAuthorityId(adminAuthority.getId()).block();
        assertThat(adminCountBefore).isEqualTo(4L);

        // When: delete all admin permissions
        Long deletedCount = authorityPermissionRepository.deleteByAuthorityId(adminAuthority.getId()).block();

        // Then: 4 mappings deleted
        assertThat(deletedCount).isEqualTo(4L);

        Long adminCountAfter = authorityPermissionRepository.countByAuthorityId(adminAuthority.getId()).block();
        assertThat(adminCountAfter).isEqualTo(0L);

        // But user permission still exists
        Long userCount = authorityPermissionRepository.countByAuthorityId(userAuthority.getId()).block();
        assertThat(userCount).isEqualTo(1L);
    }

    @Test
    void shouldDeleteByPermissionId() {
        // Given: read permission granted to both admin and user
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), createPermission.getId());

        // Verify read permission has 2 authorities
        Long readCountBefore = authorityPermissionRepository.countByPermissionId(readPermission.getId()).block();
        assertThat(readCountBefore).isEqualTo(2L);

        // When: delete all mappings for read permission
        Long deletedCount = authorityPermissionRepository.deleteByPermissionId(readPermission.getId()).block();

        // Then: 2 mappings deleted
        assertThat(deletedCount).isEqualTo(2L);

        Long readCountAfter = authorityPermissionRepository.countByPermissionId(readPermission.getId()).block();
        assertThat(readCountAfter).isEqualTo(0L);

        // But create permission still exists
        Long createCount = authorityPermissionRepository.countByPermissionId(createPermission.getId()).block();
        assertThat(createCount).isEqualTo(1L);
    }

    @Test
    void shouldCountByAuthorityId() {
        // Given: different authorities with different permission counts
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), updatePermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // When: count permissions by authority
        Long adminCount = authorityPermissionRepository.countByAuthorityId(adminAuthority.getId()).block();
        Long userCount = authorityPermissionRepository.countByAuthorityId(userAuthority.getId()).block();

        // Then: verify counts
        assertThat(adminCount).isEqualTo(3L);
        assertThat(userCount).isEqualTo(1L);
    }

    @Test
    void shouldCountByPermissionId() {
        // Given: read permission granted to both authorities, create only to admin
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), createPermission.getId());

        // When: count authorities by permission
        Long readCount = authorityPermissionRepository.countByPermissionId(readPermission.getId()).block();
        Long createCount = authorityPermissionRepository.countByPermissionId(createPermission.getId()).block();

        // Then: verify counts
        assertThat(readCount).isEqualTo(2L);
        assertThat(createCount).isEqualTo(1L);
    }

    @Test
    void shouldFindByAuthorityIdIn() {
        // Given: mappings for both authorities
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // When: find by multiple authority IDs (batch query)
        var mappings = authorityPermissionRepository
            .findByAuthorityIdIn(Arrays.asList(adminAuthority.getId(), userAuthority.getId()))
            .collectList()
            .block();

        // Then: all mappings for both authorities returned
        assertThat(mappings).hasSize(3);
        assertThat(mappings).extracting(AuthorityPermission::getAuthorityId).contains(adminAuthority.getId(), userAuthority.getId());
    }

    @Test
    void shouldFindByAuthorityIdInWithEmptyList() {
        // When: find with empty list
        var mappings = authorityPermissionRepository.findByAuthorityIdIn(Arrays.asList()).collectList().block();

        // Then: empty result
        assertThat(mappings).isEmpty();
    }

    @Test
    void shouldGetPermissionStatsByAuthority() {
        // Given: different permission counts for authorities
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), updatePermission.getId());
        createMapping(adminAuthority.getId(), deletePermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // When: get stats
        var stats = authorityPermissionRepository.getPermissionStatsByAuthority().collectList().block();

        // Then: stats are returned for both authorities
        assertThat(stats).hasSize(2);

        // Admin should have 4 permissions
        var adminStats = stats.stream().filter(s -> s.authorityCode().equals("ROLE_ADMIN_TEST")).findFirst();
        assertThat(adminStats).isPresent();
        assertThat(adminStats.get().permissionCount()).isEqualTo(4L);

        // User should have 1 permission
        var userStats = stats.stream().filter(s -> s.authorityCode().equals("ROLE_USER_TEST")).findFirst();
        assertThat(userStats).isPresent();
        assertThat(userStats.get().permissionCount()).isEqualTo(1L);
    }

    @Test
    void shouldHandleMultipleMappingsForSameAuthority() {
        // Given: admin has all 4 permissions
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), updatePermission.getId());
        createMapping(adminAuthority.getId(), deletePermission.getId());

        // When: retrieve all
        var mappings = authorityPermissionRepository.findByAuthorityId(adminAuthority.getId()).collectList().block();

        // Then: all 4 mappings present
        assertThat(mappings).hasSize(4);
        assertThat(mappings).allMatch(m -> m.getAuthorityId().equals(adminAuthority.getId()));
        assertThat(mappings)
            .extracting(AuthorityPermission::getPermissionId)
            .containsExactlyInAnyOrder(
                createPermission.getId(),
                readPermission.getId(),
                updatePermission.getId(),
                deletePermission.getId()
            );
    }

    @Test
    void shouldHandleMultipleMappingsForSamePermission() {
        // Given: both authorities have read permission
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // When: retrieve all for read permission
        var mappings = authorityPermissionRepository.findByPermissionId(readPermission.getId()).collectList().block();

        // Then: both mappings present
        assertThat(mappings).hasSize(2);
        assertThat(mappings).allMatch(m -> m.getPermissionId().equals(readPermission.getId()));
        assertThat(mappings)
            .extracting(AuthorityPermission::getAuthorityId)
            .containsExactlyInAnyOrder(adminAuthority.getId(), userAuthority.getId());
    }

    @Test
    void shouldValidateGrantedByField() {
        // Given: mapping with grantedBy
        AuthorityPermission mapping = new AuthorityPermission();
        mapping.setAuthorityId(adminAuthority.getId());
        mapping.setPermissionId(createPermission.getId());
        mapping.setGrantedBy("admin_user");
        mapping.setGrantedDate(Instant.now());

        // When: save
        AuthorityPermission saved = authorityPermissionRepository.save(mapping).block();

        // Then: grantedBy is preserved
        assertThat(saved.getGrantedBy()).isEqualTo("admin_user");
    }

    @Test
    void shouldValidateGrantedDateField() {
        // Given: mapping with specific granted date
        Instant grantedDate = Instant.parse("2025-01-15T10:00:00Z");
        AuthorityPermission mapping = new AuthorityPermission();
        mapping.setAuthorityId(adminAuthority.getId());
        mapping.setPermissionId(createPermission.getId());
        mapping.setGrantedBy(Constants.SYSTEM);
        mapping.setGrantedDate(grantedDate);

        // When: save
        AuthorityPermission saved = authorityPermissionRepository.save(mapping).block();

        // Then: grantedDate is preserved
        assertThat(saved.getGrantedDate()).isEqualTo(grantedDate);
    }

    @Test
    void shouldCountAllMappings() {
        // Given: total of 5 mappings
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(adminAuthority.getId(), updatePermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());
        createMapping(userAuthority.getId(), updatePermission.getId());

        // When: count all
        Long total = authorityPermissionRepository.count().block();

        // Then: all mappings counted
        assertThat(total).isEqualTo(5L);
    }

    @Test
    void shouldDeleteAllMappings() {
        // Given: multiple mappings
        createMapping(adminAuthority.getId(), createPermission.getId());
        createMapping(adminAuthority.getId(), readPermission.getId());
        createMapping(userAuthority.getId(), readPermission.getId());

        // Verify they exist
        Long countBefore = authorityPermissionRepository.count().block();
        assertThat(countBefore).isEqualTo(3L);

        // When: delete all
        authorityPermissionRepository.deleteAll().block();

        // Then: all deleted
        Long countAfter = authorityPermissionRepository.count().block();
        assertThat(countAfter).isEqualTo(0L);
    }

    /**
     * Helper method to create authority-permission mapping
     */
    private void createMapping(Long authorityId, Long permissionId) {
        AuthorityPermission mapping = new AuthorityPermission();
        mapping.setAuthorityId(authorityId);
        mapping.setPermissionId(permissionId);
        mapping.setGrantedBy(Constants.SYSTEM);
        mapping.setGrantedDate(Instant.now());
        authorityPermissionRepository.save(mapping).block();
    }
}
