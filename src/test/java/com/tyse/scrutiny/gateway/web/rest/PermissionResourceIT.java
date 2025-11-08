package com.tyse.scrutiny.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.web.rest.request.CreatePermissionRequest;
import com.tyse.scrutiny.gateway.web.rest.request.UpdatePermissionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link PermissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser(authorities = { "ROLE_ADMIN" })
class PermissionResourceIT {

    private static final String ENTITY_API_URL = "/api/permissions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_API_URL_RESOURCE = ENTITY_API_URL + "/resource/{resource}";

    private static final String DEFAULT_RESOURCE = "test_resource";
    private static final String DEFAULT_ACTION = "read";
    private static final String DEFAULT_DESCRIPTION = "Test permission description";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Permission permission;
    private Permission insertedPermission;

    @BeforeEach
    void initTest() {
        permission = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPermission != null) {
            permissionRepository.delete(insertedPermission).block();
            insertedPermission = null;
        }
    }

    /**
     * Create a test permission entity.
     */
    public static Permission createEntity() {
        Permission permission = new Permission();
        permission.setName(DEFAULT_RESOURCE + ":" + DEFAULT_ACTION);
        permission.setResource(DEFAULT_RESOURCE);
        permission.setAction(DEFAULT_ACTION);
        permission.setDescription(DEFAULT_DESCRIPTION);
        permission.setIsActive(true);
        return permission;
    }

    @Test
    void createPermission() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create request
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setResource(DEFAULT_RESOURCE);
        request.setAction(DEFAULT_ACTION);
        request.setDescription(DEFAULT_DESCRIPTION);

        // Create the Permission
        var returnedPermission = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(request))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Permission.class)
            .returnResult()
            .getResponseBody();

        // Validate the Permission in the database
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeCreate + 1);
        assertThat(returnedPermission).isNotNull();
        assertThat(returnedPermission.getResource()).isEqualTo(DEFAULT_RESOURCE);
        assertThat(returnedPermission.getAction()).isEqualTo(DEFAULT_ACTION);

        insertedPermission = returnedPermission;
    }

    @Test
    void getAllPermissions() {
        // Initialize the database
        insertedPermission = permissionRepository.save(permission).block();

        // Get all permissions
        webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Permission.class)
            .consumeWith(response -> {
                assertThat(response.getResponseBody()).isNotEmpty();
            });
    }

    @Test
    void getPermission() {
        // Initialize the database
        insertedPermission = permissionRepository.save(permission).block();

        // Get the permission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, insertedPermission.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Permission.class)
            .consumeWith(response -> {
                Permission foundPermission = response.getResponseBody();
                assertThat(foundPermission).isNotNull();
                assertThat(foundPermission.getId()).isEqualTo(insertedPermission.getId());
                assertThat(foundPermission.getResource()).isEqualTo(DEFAULT_RESOURCE);
            });
    }

    @Test
    void getPermissionsByResource() {
        // Initialize the database
        insertedPermission = permissionRepository.save(permission).block();

        // Get permissions by resource
        webTestClient
            .get()
            .uri(ENTITY_API_URL_RESOURCE, DEFAULT_RESOURCE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(Permission.class)
            .consumeWith(response -> {
                assertThat(response.getResponseBody()).isNotEmpty();
                assertThat(response.getResponseBody().get(0).getResource()).isEqualTo(DEFAULT_RESOURCE);
            });
    }

    @Test
    void updatePermission() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.save(permission).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update request
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setDescription("Updated description");

        // Update the permission
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, insertedPermission.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(request))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Permission.class)
            .consumeWith(response -> {
                Permission updatedPermission = response.getResponseBody();
                assertThat(updatedPermission).isNotNull();
                assertThat(updatedPermission.getDescription()).isEqualTo("Updated description");
            });

        // Validate the Permission in the database
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeUpdate);
    }

    @Test
    void getNonExistingPermission() {
        // Get the permission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    protected long getRepositoryCount() {
        return permissionRepository.count().block();
    }
}
