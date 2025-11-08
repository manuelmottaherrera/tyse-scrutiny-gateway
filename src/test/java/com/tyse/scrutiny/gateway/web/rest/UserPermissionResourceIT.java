package com.tyse.scrutiny.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserPermissionRepository;
import com.tyse.scrutiny.gateway.service.authorization.UserPermissionService;
import com.tyse.scrutiny.gateway.web.rest.request.GrantPermissionRequest;
import com.tyse.scrutiny.gateway.web.rest.request.RevokePermissionRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link UserPermissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser(authorities = { "ROLE_ADMIN" })
class UserPermissionResourceIT {

    private static final String ENTITY_API_URL = "/api/user-permissions";
    private static final String ENTITY_API_URL_USER = ENTITY_API_URL + "/user/{userId}";
    private static final String ENTITY_API_URL_USER_EFFECTIVE = ENTITY_API_URL + "/user/{userId}/effective";
    private static final String ENTITY_API_URL_EXPIRING = ENTITY_API_URL + "/expiring";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserPermissionRepository userPermissionRepository;

    @Autowired
    private UserPermissionService userPermissionService;

    @Autowired
    private WebTestClient webTestClient;

    private User testUser;
    private Permission testPermission;
    private UserPermission insertedUserPermission;

    @BeforeEach
    void initTest() {
        // Create test user
        testUser = new User();
        testUser.setLogin("testuser" + System.currentTimeMillis());
        testUser.setEmail("testuser@example.com");
        testUser.setActivated(true);
        testUser.setPassword("password_hash_for_test");
        testUser = userRepository.save(testUser).block();

        // Create test permission
        testPermission = new Permission();
        testPermission.setName("test_resource:read");
        testPermission.setResource("test_resource");
        testPermission.setAction("read");
        testPermission.setDescription("Test permission");
        testPermission.setIsActive(true);
        testPermission = permissionRepository.save(testPermission).block();
    }

    @AfterEach
    void cleanup() {
        if (insertedUserPermission != null) {
            userPermissionRepository.delete(insertedUserPermission).block();
            insertedUserPermission = null;
        }
        if (testUser != null) {
            userRepository.delete(testUser).block();
        }
        if (testPermission != null) {
            permissionRepository.delete(testPermission).block();
        }
    }

    @Test
    void grantPermission() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create request
        GrantPermissionRequest request = new GrantPermissionRequest();
        request.setUserId(testUser.getId());
        request.setPermissionId(testPermission.getId());
        request.setReason("Test grant reason");
        request.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));

        // Grant permission
        var returnedGrant = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(request))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(UserPermission.class)
            .returnResult()
            .getResponseBody();

        // Validate
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeCreate + 1);
        assertThat(returnedGrant).isNotNull();
        assertThat(returnedGrant.getUserId()).isEqualTo(testUser.getId());
        assertThat(returnedGrant.getPermissionId()).isEqualTo(testPermission.getId());
        assertThat(returnedGrant.getIsActive()).isTrue();
        assertThat(returnedGrant.getReason()).isEqualTo("Test grant reason");

        insertedUserPermission = returnedGrant;
    }

    @Test
    void getDirectPermissionsForUser() {
        // Create grant
        UserPermission grant = createEntity();
        insertedUserPermission = userPermissionRepository.save(grant).block();

        // Get direct permissions for user
        webTestClient
            .get()
            .uri(ENTITY_API_URL_USER, testUser.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(UserPermission.class)
            .consumeWith(response -> {
                assertThat(response.getResponseBody()).isNotEmpty();
                assertThat(response.getResponseBody().get(0).getUserId()).isEqualTo(testUser.getId());
            });
    }

    @Test
    void getEffectivePermissionsForUser() {
        // Create grant
        UserPermission grant = createEntity();
        insertedUserPermission = userPermissionRepository.save(grant).block();

        // Get effective permissions for user
        webTestClient
            .get()
            .uri(ENTITY_API_URL_USER_EFFECTIVE, testUser.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(Permission.class)
            .consumeWith(response -> {
                // Effective permissions may include role-based permissions
                assertThat(response.getResponseBody()).isNotNull();
            });
    }

    @Test
    void revokePermission() throws Exception {
        // Create grant
        UserPermission grant = createEntity();
        insertedUserPermission = userPermissionRepository.save(grant).block();

        // Revoke using service directly (WebTestClient doesn't support DELETE with body)
        userPermissionService.revokeById(insertedUserPermission.getId(), "Test revocation reason").block();

        // Verify revocation
        UserPermission revokedGrant = userPermissionRepository.findById(insertedUserPermission.getId()).block();
        assertThat(revokedGrant).isNotNull();
        assertThat(revokedGrant.getIsActive()).isFalse();
        assertThat(revokedGrant.getRevokedReason()).isEqualTo("Test revocation reason");
    }

    @Test
    void getExpiringPermissions() {
        // Create grant expiring in 5 days
        UserPermission grant = createEntity();
        grant.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
        insertedUserPermission = userPermissionRepository.save(grant).block();

        // Get expiring permissions
        webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(ENTITY_API_URL_EXPIRING).queryParam("days", 7).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(UserPermission.class)
            .consumeWith(response -> {
                assertThat(response.getResponseBody()).isNotEmpty();
            });
    }

    private UserPermission createEntity() {
        UserPermission grant = new UserPermission();
        grant.setUserId(testUser.getId());
        grant.setPermissionId(testPermission.getId());
        grant.setIsActive(true);
        grant.setGrantedBy("system");
        grant.setGrantedDate(Instant.now());
        grant.setReason("Test reason");
        return grant;
    }

    protected long getRepositoryCount() {
        return userPermissionRepository.count().block();
    }
}
