package com.tyse.scrutiny.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuthorityPermissionDTO;
import com.tyse.scrutiny.gateway.service.dto.authorization.PermissionDTO;
import com.tyse.scrutiny.gateway.web.rest.request.AssignPermissionToAuthorityRequest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for {@link AuthorityPermissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = "10000")
class AuthorityPermissionResourceIT {

    private static final String API_URL = "/api/authority-permissions";

    @Autowired
    private AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Authority adminAuthority;
    private Authority userAuthority;
    private Permission createPermission;
    private Permission readPermission;
    private Permission updatePermission;

    @BeforeEach
    void setUp() {
        authorityPermissionRepository.deleteAll().block();
        permissionRepository.deleteAll().block();
        authorityRepository.deleteAll().block();

        // Create test authorities
        adminAuthority = createAuthority("ROLE_ADMIN_TEST", "Admin", false, 0);
        userAuthority = createAuthority("ROLE_USER_TEST", "User", false, 500);

        // Create test permissions
        createPermission = createPermission("user", "create", "Create users");
        readPermission = createPermission("user", "read", "Read users");
        updatePermission = createPermission("user", "update", "Update users");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldAssignPermissionToAuthority() {
        // Given: assignment request
        AssignPermissionToAuthorityRequest request = new AssignPermissionToAuthorityRequest();
        request.setAuthorityId(adminAuthority.getId());
        request.setPermissionId(createPermission.getId());

        // When: POST /api/authority-permissions
        webTestClient
            .post()
            .uri(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            // Then: 201 Created with mapping
            .exchange()
            .expectStatus()
            .isCreated()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(AuthorityPermissionDTO.class)
            .value(mapping -> {
                assertThat(mapping.getId()).isNotNull();
                assertThat(mapping.getAuthorityId()).isEqualTo(adminAuthority.getId());
                assertThat(mapping.getPermissionId()).isEqualTo(createPermission.getId());
                assertThat(mapping.getGrantedBy()).isNotNull();
            });
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldFailToAssignDuplicatePermission() {
        // Given: permission already assigned
        assignPermission(adminAuthority.getId(), createPermission.getId());

        // Given: duplicate request
        AssignPermissionToAuthorityRequest request = new AssignPermissionToAuthorityRequest();
        request.setAuthorityId(adminAuthority.getId());
        request.setPermissionId(createPermission.getId());

        // When/Then: 400 Bad Request
        webTestClient
            .post()
            .uri(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .is4xxClientError();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldFailToAssignWithInvalidRequest() {
        // Given: invalid request (null values)
        AssignPermissionToAuthorityRequest request = new AssignPermissionToAuthorityRequest();
        // authorityId and permissionId are null

        // When/Then: 400 Bad Request
        webTestClient
            .post()
            .uri(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldFailToAssignNonExistentAuthority() {
        // Given: non-existent authority
        AssignPermissionToAuthorityRequest request = new AssignPermissionToAuthorityRequest();
        request.setAuthorityId(999999L); // Non-existent
        request.setPermissionId(createPermission.getId());

        // When/Then: 404 Not Found or 4xx error
        webTestClient
            .post()
            .uri(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .is4xxClientError();
    }

    @Test
    void shouldFailToAssignWithoutAuthentication() {
        // Given: request without authentication
        AssignPermissionToAuthorityRequest request = new AssignPermissionToAuthorityRequest();
        request.setAuthorityId(adminAuthority.getId());
        request.setPermissionId(createPermission.getId());

        // When/Then: 401 Unauthorized
        webTestClient
            .post()
            .uri(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER") // Not ROLE_ADMIN
    void shouldFailToAssignWithoutAdminRole() {
        // Given: user without ROLE_ADMIN
        AssignPermissionToAuthorityRequest request = new AssignPermissionToAuthorityRequest();
        request.setAuthorityId(adminAuthority.getId());
        request.setPermissionId(createPermission.getId());

        // When/Then: 403 Forbidden
        webTestClient
            .post()
            .uri(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isForbidden();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldRevokePermissionFromAuthority() {
        // Given: permission assigned
        assignPermission(adminAuthority.getId(), createPermission.getId());

        // When: DELETE /api/authority-permissions/authority/{id}/permission/{id}
        webTestClient
            .delete()
            .uri(API_URL + "/authority/{authorityId}/permission/{permissionId}", adminAuthority.getId(), createPermission.getId())
            // Then: 204 No Content
            .exchange()
            .expectStatus()
            .isNoContent();

        // Verify: permission no longer assigned
        Long count = authorityPermissionRepository.countByAuthorityId(adminAuthority.getId()).block();
        assertThat(count).isZero();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnNotFoundWhenRevokingNonExistent() {
        // When: DELETE non-existent mapping
        webTestClient
            .delete()
            .uri(API_URL + "/authority/{authorityId}/permission/{permissionId}", adminAuthority.getId(), createPermission.getId())
            // Then: 404 Not Found
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void shouldFailToRevokeWithoutAuthentication() {
        webTestClient
            .delete()
            .uri(API_URL + "/authority/{authorityId}/permission/{permissionId}", adminAuthority.getId(), createPermission.getId())
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldFailToRevokeWithoutAdminRole() {
        webTestClient
            .delete()
            .uri(API_URL + "/authority/{authorityId}/permission/{permissionId}", adminAuthority.getId(), createPermission.getId())
            .exchange()
            .expectStatus()
            .isForbidden();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldGetPermissionsByAuthority() {
        // Given: authority with 3 permissions
        assignPermission(adminAuthority.getId(), createPermission.getId());
        assignPermission(adminAuthority.getId(), readPermission.getId());
        assignPermission(adminAuthority.getId(), updatePermission.getId());

        // When: GET /api/authority-permissions/authority/{id}/permissions
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permissions", adminAuthority.getId())
            // Then: 200 OK with 3 permissions
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(PermissionDTO.class)
            .hasSize(3)
            .value(permissions -> {
                assertThat(permissions)
                    .extracting(PermissionDTO::getName)
                    .containsExactlyInAnyOrder("user.create", "user.read", "user.update");
            });
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnEmptyListForAuthorityWithoutPermissions() {
        // When: GET permissions for authority without any
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permissions", adminAuthority.getId())
            // Then: 200 OK with empty list
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PermissionDTO.class)
            .hasSize(0);
    }

    @Test
    void shouldFailToGetPermissionsWithoutAuthentication() {
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permissions", adminAuthority.getId())
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldFailToGetPermissionsWithoutAdminRole() {
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permissions", adminAuthority.getId())
            .exchange()
            .expectStatus()
            .isForbidden();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldGetAuthorityPermissionMappings() {
        // Given: authority with mappings
        assignPermission(adminAuthority.getId(), createPermission.getId());
        assignPermission(adminAuthority.getId(), readPermission.getId());

        // When: GET /api/authority-permissions/authority/{id}/mappings
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/mappings", adminAuthority.getId())
            // Then: 200 OK with 2 mappings (with metadata)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(AuthorityPermissionDTO.class)
            .hasSize(2)
            .value(mappings -> {
                assertThat(mappings).allMatch(m -> m.getAuthorityId().equals(adminAuthority.getId()));
                assertThat(mappings).allMatch(m -> m.getGrantedBy() != null);
                assertThat(mappings).allMatch(m -> m.getGrantedDate() != null);
            });
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldCheckIfAuthorityHasPermission() {
        // Given: authority with permission
        assignPermission(adminAuthority.getId(), createPermission.getId());

        // When: GET /api/authority-permissions/authority/{id}/permission/{id}/exists
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permission/{permissionId}/exists", adminAuthority.getId(), createPermission.getId())
            // Then: 200 OK with true
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Boolean.class)
            .isEqualTo(true);

        // When: check non-existent permission
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permission/{permissionId}/exists", adminAuthority.getId(), updatePermission.getId())
            // Then: 200 OK with false
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Boolean.class)
            .isEqualTo(false);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldRemoveAllPermissionsFromAuthority() {
        // Given: authority with 3 permissions
        assignPermission(adminAuthority.getId(), createPermission.getId());
        assignPermission(adminAuthority.getId(), readPermission.getId());
        assignPermission(adminAuthority.getId(), updatePermission.getId());

        // Verify 3 permissions exist
        Long countBefore = authorityPermissionRepository.countByAuthorityId(adminAuthority.getId()).block();
        assertThat(countBefore).isEqualTo(3L);

        // When: DELETE /api/authority-permissions/authority/{id}
        webTestClient
            .delete()
            .uri(API_URL + "/authority/{authorityId}", adminAuthority.getId())
            // Then: 204 No Content
            .exchange()
            .expectStatus()
            .isNoContent();

        // Verify: all permissions removed
        Long countAfter = authorityPermissionRepository.countByAuthorityId(adminAuthority.getId()).block();
        assertThat(countAfter).isZero();
    }

    @Test
    void shouldFailToRemoveAllWithoutAuthentication() {
        webTestClient.delete().uri(API_URL + "/authority/{authorityId}", adminAuthority.getId()).exchange().expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldFailToRemoveAllWithoutAdminRole() {
        webTestClient.delete().uri(API_URL + "/authority/{authorityId}", adminAuthority.getId()).exchange().expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldHandleConcurrentAssignments() {
        // When: multiple permissions assigned to same authority
        AssignPermissionToAuthorityRequest request1 = new AssignPermissionToAuthorityRequest();
        request1.setAuthorityId(adminAuthority.getId());
        request1.setPermissionId(createPermission.getId());

        AssignPermissionToAuthorityRequest request2 = new AssignPermissionToAuthorityRequest();
        request2.setAuthorityId(adminAuthority.getId());
        request2.setPermissionId(readPermission.getId());

        AssignPermissionToAuthorityRequest request3 = new AssignPermissionToAuthorityRequest();
        request3.setAuthorityId(adminAuthority.getId());
        request3.setPermissionId(updatePermission.getId());

        webTestClient.post().uri(API_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(request1).exchange().expectStatus().isCreated();

        webTestClient.post().uri(API_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(request2).exchange().expectStatus().isCreated();

        webTestClient.post().uri(API_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(request3).exchange().expectStatus().isCreated();

        // Then: all 3 permissions assigned
        Long count = authorityPermissionRepository.countByAuthorityId(adminAuthority.getId()).block();
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldHandleMultipleAuthoritiesWithSamePermission() {
        // Given: same permission assigned to 2 different authorities
        assignPermission(adminAuthority.getId(), readPermission.getId());
        assignPermission(userAuthority.getId(), readPermission.getId());

        // When: get permissions for each authority
        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permissions", adminAuthority.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PermissionDTO.class)
            .hasSize(1);

        webTestClient
            .get()
            .uri(API_URL + "/authority/{authorityId}/permissions", userAuthority.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PermissionDTO.class)
            .hasSize(1);

        // Then: both have the permission
        Long count = authorityPermissionRepository.countByPermissionId(readPermission.getId()).block();
        assertThat(count).isEqualTo(2L);
    }

    /**
     * Helper method to create authority
     */
    private Authority createAuthority(String code, String name, boolean isSystem, int hierarchyLevel) {
        Authority authority = new Authority();
        authority.setCode(code);
        authority.setName(name);
        authority.setCategory(AuthorityCategory.CUSTOM);
        authority.setIsSystem(isSystem);
        authority.setIsActive(true);
        authority.setHierarchyLevel(hierarchyLevel);
        authority.setCreatedBy(Constants.SYSTEM);
        authority.setCreatedDate(Instant.now());
        return authorityRepository.save(authority).block();
    }

    /**
     * Helper method to create permission
     */
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

    /**
     * Helper method to assign permission to authority
     */
    private void assignPermission(Long authorityId, Long permissionId) {
        AssignPermissionToAuthorityRequest request = new AssignPermissionToAuthorityRequest();
        request.setAuthorityId(authorityId);
        request.setPermissionId(permissionId);

        webTestClient.post().uri(API_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated();
    }
}
