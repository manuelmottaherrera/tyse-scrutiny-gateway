package com.tyse.scrutiny.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository;
import com.tyse.scrutiny.gateway.service.authorization.UserAuthorityService;
import com.tyse.scrutiny.gateway.web.rest.request.AssignAuthorityRequest;
import com.tyse.scrutiny.gateway.web.rest.request.RevokeAuthorityRequest;
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
 * Integration tests for the {@link UserAuthorityResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser(authorities = { "ROLE_ADMIN" })
class UserAuthorityResourceIT {

    private static final String ENTITY_API_URL = "/api/user-authorities";
    private static final String ENTITY_API_URL_USER = ENTITY_API_URL + "/user/{userId}";
    private static final String ENTITY_API_URL_USER_VALID = ENTITY_API_URL + "/user/{userId}/valid";
    private static final String ENTITY_API_URL_EXPIRING = ENTITY_API_URL + "/expiring";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private UserAuthorityService userAuthorityService;

    @Autowired
    private WebTestClient webTestClient;

    private User testUser;
    private Authority testAuthority;
    private UserAuthority insertedUserAuthority;

    @BeforeEach
    void initTest() {
        // Create test user
        testUser = new User();
        testUser.setLogin("testuser" + System.currentTimeMillis());
        testUser.setEmail("testuser@example.com");
        testUser.setActivated(true);
        testUser = userRepository.save(testUser).block();

        // Create test authority
        testAuthority = new Authority();
        testAuthority.setName("TEST_ROLE_" + System.currentTimeMillis());
        testAuthority = authorityRepository.save(testAuthority).block();
    }

    @AfterEach
    void cleanup() {
        if (insertedUserAuthority != null) {
            userAuthorityRepository.delete(insertedUserAuthority).block();
            insertedUserAuthority = null;
        }
        if (testUser != null) {
            userRepository.delete(testUser).block();
        }
        if (testAuthority != null) {
            authorityRepository.delete(testAuthority).block();
        }
    }

    @Test
    void assignAuthority() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create request
        AssignAuthorityRequest request = new AssignAuthorityRequest();
        request.setUserId(testUser.getId());
        request.setAuthorityId(testAuthority.getId());
        request.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));

        // Assign authority
        var returnedAssignment = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(request))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(UserAuthority.class)
            .returnResult()
            .getResponseBody();

        // Validate
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeCreate + 1);
        assertThat(returnedAssignment).isNotNull();
        assertThat(returnedAssignment.getUserId()).isEqualTo(testUser.getId());
        assertThat(returnedAssignment.getAuthorityId()).isEqualTo(testAuthority.getId());
        assertThat(returnedAssignment.getIsActive()).isTrue();

        insertedUserAuthority = returnedAssignment;
    }

    @Test
    void getAllAuthoritiesForUser() {
        // Create assignment
        UserAuthority assignment = createEntity();
        insertedUserAuthority = userAuthorityRepository.save(assignment).block();

        // Get all authorities for user
        webTestClient
            .get()
            .uri(ENTITY_API_URL_USER, testUser.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(UserAuthority.class)
            .consumeWith(response -> {
                assertThat(response.getResponseBody()).isNotEmpty();
                assertThat(response.getResponseBody().get(0).getUserId()).isEqualTo(testUser.getId());
            });
    }

    @Test
    void getValidAuthoritiesForUser() {
        // Create valid assignment
        UserAuthority assignment = createEntity();
        assignment.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        insertedUserAuthority = userAuthorityRepository.save(assignment).block();

        // Get valid authorities for user
        webTestClient
            .get()
            .uri(ENTITY_API_URL_USER_VALID, testUser.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(UserAuthority.class)
            .consumeWith(response -> {
                assertThat(response.getResponseBody()).isNotEmpty();
            });
    }

    @Test
    void revokeAuthority() throws Exception {
        // Create assignment
        UserAuthority assignment = createEntity();
        insertedUserAuthority = userAuthorityRepository.save(assignment).block();

        // Revoke using service directly (WebTestClient doesn't support DELETE with body)
        userAuthorityService.revokeById(insertedUserAuthority.getId(), "Test revocation reason").block();

        // Verify revocation
        UserAuthority revokedAssignment = userAuthorityRepository.findById(insertedUserAuthority.getId()).block();
        assertThat(revokedAssignment).isNotNull();
        assertThat(revokedAssignment.getIsActive()).isFalse();
        assertThat(revokedAssignment.getRevokedReason()).isEqualTo("Test revocation reason");
    }

    @Test
    void getExpiringAuthorities() {
        // Create assignment expiring in 5 days
        UserAuthority assignment = createEntity();
        assignment.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));
        insertedUserAuthority = userAuthorityRepository.save(assignment).block();

        // Get expiring authorities
        webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(ENTITY_API_URL_EXPIRING).queryParam("days", 7).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(UserAuthority.class)
            .consumeWith(response -> {
                assertThat(response.getResponseBody()).isNotEmpty();
            });
    }

    private UserAuthority createEntity() {
        UserAuthority assignment = new UserAuthority();
        assignment.setUserId(testUser.getId());
        assignment.setAuthorityId(testAuthority.getId());
        assignment.setIsActive(true);
        assignment.setAssignedBy("system");
        assignment.setAssignedDate(Instant.now());
        return assignment;
    }

    protected long getRepositoryCount() {
        return userAuthorityRepository.count().block();
    }
}
