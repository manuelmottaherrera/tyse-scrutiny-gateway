package com.tyse.scrutiny.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import com.tyse.scrutiny.gateway.repository.EntityManager;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityAuditRepository;
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
 * Integration tests for the {@link AuthorityAuditResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser(authorities = { "ROLE_ADMIN" })
class AuthorityAuditResourceIT {

    private static final String ENTITY_API_URL = "/api/authority-audits";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_API_URL_AUTHORITY = ENTITY_API_URL + "/authority/{authorityId}";
    private static final String ENTITY_API_URL_SEARCH = ENTITY_API_URL + "/search";
    private static final String ENTITY_API_URL_RECENT = ENTITY_API_URL + "/recent";
    private static final String ENTITY_API_URL_EXPORT_CSV = ENTITY_API_URL + "/export/csv";
    private static final String ENTITY_API_URL_EXPORT_JSON = ENTITY_API_URL + "/export/json";
    private static final String ENTITY_API_URL_METRICS_SUMMARY = ENTITY_API_URL + "/metrics/summary";
    private static final String ENTITY_API_URL_METRICS_BY_AUTHORITY = ENTITY_API_URL + "/metrics/by-authority/{authorityId}";

    private static final Long DEFAULT_AUTHORITY_ID = 1L;
    private static final AuditAction DEFAULT_ACTION = AuditAction.CREATED;
    private static final String DEFAULT_OLD_VALUES = "{\"name\":\"Old Role\"}";
    private static final String DEFAULT_NEW_VALUES = "{\"name\":\"New Role\"}";
    private static final String DEFAULT_CHANGED_BY = "admin";
    private static final Instant DEFAULT_CHANGED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final String DEFAULT_IP_ADDRESS = "192.168.1.100";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuthorityAuditRepository auditRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private AuthorityAudit authorityAudit;

    private AuthorityAudit insertedAudit;

    /**
     * Create an entity for this test.
     */
    public static AuthorityAudit createEntity() {
        return new AuthorityAudit()
            .authorityId(DEFAULT_AUTHORITY_ID)
            .action(DEFAULT_ACTION)
            .oldValues(DEFAULT_OLD_VALUES)
            .newValues(DEFAULT_NEW_VALUES)
            .changedBy(DEFAULT_CHANGED_BY)
            .changedDate(DEFAULT_CHANGED_DATE)
            .ipAddress(DEFAULT_IP_ADDRESS)
            .userAgent(DEFAULT_USER_AGENT);
    }

    @BeforeEach
    void initTest() {
        authorityAudit = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAudit != null) {
            auditRepository.delete(insertedAudit).block();
            insertedAudit = null;
        }
        // Clean all audit records for testing
        auditRepository.deleteAll().block();
    }

    @Test
    void getAllAudits() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Get all the audits
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?page=0&size=20")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(insertedAudit.getId().intValue()))
            .jsonPath("$.[*].authorityId")
            .value(hasItem(DEFAULT_AUTHORITY_ID.intValue()))
            .jsonPath("$.[*].action")
            .value(hasItem(DEFAULT_ACTION.name()))
            .jsonPath("$.[*].changedBy")
            .value(hasItem(DEFAULT_CHANGED_BY));
    }

    @Test
    void getAudit() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Get the audit
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, insertedAudit.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(insertedAudit.getId().intValue()))
            .jsonPath("$.authorityId")
            .value(is(DEFAULT_AUTHORITY_ID.intValue()))
            .jsonPath("$.action")
            .value(is(DEFAULT_ACTION.name()))
            .jsonPath("$.changedBy")
            .value(is(DEFAULT_CHANGED_BY))
            .jsonPath("$.ipAddress")
            .value(is(DEFAULT_IP_ADDRESS));
    }

    @Test
    void getNonExistingAudit() throws Exception {
        // Get the audit
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void getAuditsByAuthority() throws Exception {
        // Initialize the database with multiple audits for the same authority
        insertedAudit = auditRepository.save(authorityAudit).block();
        AuthorityAudit audit2 = auditRepository
            .save(createEntity().action(AuditAction.UPDATED).changedDate(Instant.now().truncatedTo(ChronoUnit.MILLIS).plusSeconds(1)))
            .block();

        // Get audits by authority
        webTestClient
            .get()
            .uri(ENTITY_API_URL_AUTHORITY, DEFAULT_AUTHORITY_ID)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].authorityId")
            .value(everyItem(is(DEFAULT_AUTHORITY_ID.intValue())))
            .jsonPath("$.length()")
            .value(is(2));

        // Cleanup
        auditRepository.delete(audit2).block();
    }

    @Test
    void searchAuditsWithFilters() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Search with filters
        webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(ENTITY_API_URL_SEARCH)
                    .queryParam("authorityId", DEFAULT_AUTHORITY_ID)
                    .queryParam("changedBy", DEFAULT_CHANGED_BY)
                    .queryParam("action", DEFAULT_ACTION.name())
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(insertedAudit.getId().intValue()))
            .jsonPath("$.[*].authorityId")
            .value(hasItem(DEFAULT_AUTHORITY_ID.intValue()))
            .jsonPath("$.[*].changedBy")
            .value(hasItem(DEFAULT_CHANGED_BY));
    }

    @Test
    void searchAuditsWithDateRange() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        Instant fromDate = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant toDate = Instant.now().plus(1, ChronoUnit.DAYS);

        // Search with date range - just verify the endpoint works
        webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(ENTITY_API_URL_SEARCH)
                    .queryParam("fromDate", fromDate.toString())
                    .queryParam("toDate", toDate.toString())
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .isArray();
    }

    @Test
    void getRecentAudits() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Get recent audits
        webTestClient
            .get()
            .uri(ENTITY_API_URL_RECENT + "?limit=10")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(insertedAudit.getId().intValue()));
    }

    @Test
    void exportAuditsAsCsv() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Export as CSV
        webTestClient
            .get()
            .uri(ENTITY_API_URL_EXPORT_CSV)
            .accept(MediaType.parseMediaType("text/csv"))
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.parseMediaType("text/csv"))
            .expectHeader()
            .valueEquals("Content-Disposition", "form-data; name=\"attachment\"; filename=\"authority-audit-log.csv\"")
            .expectBody(byte[].class)
            .value(bytes -> {
                String csv = new String(bytes);
                assertThat(csv).contains("ID,Authority ID,Authority Code,Action Type,Changed By");
                assertThat(csv).contains(DEFAULT_CHANGED_BY);
                assertThat(csv).contains(DEFAULT_ACTION.name());
            });
    }

    @Test
    void exportAuditsAsJson() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Export as JSON
        webTestClient
            .get()
            .uri(ENTITY_API_URL_EXPORT_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectHeader()
            .valueEquals("Content-Disposition", "form-data; name=\"attachment\"; filename=\"authority-audit-log.json\"")
            .expectBody(byte[].class)
            .value(bytes -> {
                String json = new String(bytes);
                assertThat(json).contains("\"changedBy\":\"" + DEFAULT_CHANGED_BY + "\"");
                assertThat(json).contains("\"action\":\"" + DEFAULT_ACTION.name() + "\"");
            });
    }

    @Test
    void getMetricsSummary() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Get metrics summary
        webTestClient
            .get()
            .uri(ENTITY_API_URL_METRICS_SUMMARY)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.totalAudits")
            .isNumber()
            .jsonPath("$.auditsByAction")
            .isMap()
            .jsonPath("$.topUsers")
            .isArray()
            .jsonPath("$.recentActivity")
            .isMap()
            .jsonPath("$.recentActivity.last24Hours")
            .isNumber();
    }

    @Test
    void getMetricsByAuthority() throws Exception {
        // Initialize the database
        insertedAudit = auditRepository.save(authorityAudit).block();

        // Get metrics for specific authority
        webTestClient
            .get()
            .uri(ENTITY_API_URL_METRICS_BY_AUTHORITY, DEFAULT_AUTHORITY_ID)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.totalChanges")
            .isNumber()
            .jsonPath("$.changesByAction")
            .isMap()
            .jsonPath("$.lastChange")
            .isMap();
    }

    @Test
    @WithMockUser(authorities = { "ROLE_USER" })
    void getAllAuditsAsNonAdminShouldFail() throws Exception {
        // Try to access audits as non-admin
        webTestClient.get().uri(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(authorities = { "ROLE_USER" })
    void getMetricsSummaryAsNonAdminShouldFail() throws Exception {
        // Try to access metrics as non-admin
        webTestClient.get().uri(ENTITY_API_URL_METRICS_SUMMARY).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isForbidden();
    }
}
