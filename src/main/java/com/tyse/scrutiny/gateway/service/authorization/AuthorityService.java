package com.tyse.scrutiny.gateway.service.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityAuditRepository;
import com.tyse.scrutiny.gateway.security.SecurityUtils;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.AuthorityAlreadyExistsException;
import com.tyse.scrutiny.gateway.service.authorization.exceptions.AuthorityNotFoundException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing authorities with audit logging.
 */
@Service
@Transactional
public class AuthorityService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityService.class);

    private final AuthorityRepository authorityRepository;
    private final AuthorityAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AuthorityService(AuthorityRepository authorityRepository, AuthorityAuditRepository auditRepository, ObjectMapper objectMapper) {
        this.authorityRepository = authorityRepository;
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new authority with audit logging.
     *
     * @param authority the authority to create
     * @param exchange the server web exchange for extracting IP and User-Agent
     * @return the created authority
     */
    public Mono<Authority> createAuthority(Authority authority, ServerWebExchange exchange) {
        LOG.debug("Request to create Authority: {}", authority);

        return authorityRepository
            .existsByCode(authority.getCode())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new AuthorityAlreadyExistsException(authority.getCode()));
                }

                // Set default values
                if (authority.getIsActive() == null) {
                    authority.setIsActive(true);
                }

                return authorityRepository
                    .save(authority)
                    .flatMap(savedAuthority ->
                        createAuditLog(savedAuthority.getId(), AuditAction.CREATED, null, toJson(savedAuthority), exchange).thenReturn(
                            savedAuthority
                        )
                    )
                    .doOnNext(savedAuthority -> LOG.debug("Created authority: {}", savedAuthority));
            });
    }

    /**
     * Update an existing authority with audit logging.
     *
     * @param id the id of the authority to update
     * @param authority the authority with updated data
     * @param exchange the server web exchange for extracting IP and User-Agent
     * @return the updated authority
     */
    public Mono<Authority> updateAuthority(Long id, Authority authority, ServerWebExchange exchange) {
        LOG.debug("Request to update Authority: {}", authority);

        return authorityRepository
            .findById(id)
            .switchIfEmpty(Mono.error(new AuthorityNotFoundException(id)))
            .flatMap(existingAuthority -> {
                // Prevent modification of system authorities
                if (Boolean.TRUE.equals(existingAuthority.getIsSystem())) {
                    return Mono.error(new IllegalStateException("Cannot modify system authority: " + existingAuthority.getCode()));
                }

                String oldValues = toJson(existingAuthority);

                // Update fields
                existingAuthority.setName(authority.getName());
                existingAuthority.setDescription(authority.getDescription());
                existingAuthority.setCategory(authority.getCategory());
                existingAuthority.setHierarchyLevel(authority.getHierarchyLevel());

                if (authority.getIsActive() != null) {
                    existingAuthority.setIsActive(authority.getIsActive());
                }

                return authorityRepository
                    .save(existingAuthority)
                    .flatMap(updatedAuthority ->
                        createAuditLog(
                            updatedAuthority.getId(),
                            AuditAction.UPDATED,
                            oldValues,
                            toJson(updatedAuthority),
                            exchange
                        ).thenReturn(updatedAuthority)
                    )
                    .doOnNext(updatedAuthority -> LOG.debug("Updated authority: {}", updatedAuthority));
            });
    }

    /**
     * Deactivate an authority (soft delete) with audit logging.
     *
     * @param id the id of the authority to deactivate
     * @param exchange the server web exchange for extracting IP and User-Agent
     * @return void
     */
    public Mono<Void> deactivateAuthority(Long id, ServerWebExchange exchange) {
        LOG.debug("Request to deactivate Authority with id: {}", id);

        return authorityRepository
            .findById(id)
            .switchIfEmpty(Mono.error(new AuthorityNotFoundException(id)))
            .flatMap(authority -> {
                if (Boolean.TRUE.equals(authority.getIsSystem())) {
                    return Mono.error(new IllegalStateException("Cannot deactivate system authority: " + authority.getCode()));
                }

                String oldValues = toJson(authority);
                authority.setIsActive(false);

                return authorityRepository
                    .save(authority)
                    .flatMap(deactivatedAuthority ->
                        createAuditLog(
                            deactivatedAuthority.getId(),
                            AuditAction.DEACTIVATED,
                            oldValues,
                            toJson(deactivatedAuthority),
                            exchange
                        )
                    )
                    .doOnNext(v -> LOG.debug("Deactivated authority with id: {}", id));
            });
    }

    /**
     * Get all active authorities.
     *
     * @return flux of active authorities
     */
    @Transactional(readOnly = true)
    public Flux<Authority> findActiveAuthorities() {
        LOG.debug("Request to get all active Authorities");
        return authorityRepository.findByIsActiveTrue();
    }

    /**
     * Get authorities by category.
     *
     * @param category the authority category
     * @return flux of authorities in the given category
     */
    @Transactional(readOnly = true)
    public Flux<Authority> findByCategory(AuthorityCategory category) {
        LOG.debug("Request to get Authorities by category: {}", category);
        return authorityRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * Find an authority by its code.
     *
     * @param code the authority code (e.g., "ROLE_ADMIN")
     * @return the authority if found
     */
    @Transactional(readOnly = true)
    public Mono<Authority> findByCode(String code) {
        LOG.debug("Request to get Authority by code: {}", code);
        return authorityRepository.findByCode(code).switchIfEmpty(Mono.error(new AuthorityNotFoundException(code)));
    }

    /**
     * Get audit history for an authority.
     *
     * @param authorityId the id of the authority
     * @return flux of audit records
     */
    @Transactional(readOnly = true)
    public Flux<AuthorityAudit> getAuditHistory(Long authorityId) {
        LOG.debug("Request to get audit history for Authority with id: {}", authorityId);
        return auditRepository.findByAuthorityIdOrderByChangedDateDesc(authorityId);
    }

    /**
     * Create an audit log entry.
     *
     * @param authorityId the id of the authority
     * @param action the audit action
     * @param oldValues the old values as JSON
     * @param newValues the new values as JSON
     * @param exchange the server web exchange for extracting IP and User-Agent
     * @return void
     */
    private Mono<Void> createAuditLog(
        Long authorityId,
        AuditAction action,
        String oldValues,
        String newValues,
        ServerWebExchange exchange
    ) {
        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(currentUser -> {
                AuthorityAudit audit = new AuthorityAudit();
                audit.setAuthorityId(authorityId);
                audit.setAction(action);
                audit.setOldValues(oldValues);
                audit.setNewValues(newValues);
                audit.setChangedBy(currentUser);
                audit.setChangedDate(Instant.now());

                // Extract IP address and User-Agent from exchange
                if (exchange != null) {
                    String ipAddress = exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown";
                    audit.setIpAddress(ipAddress);

                    String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
                    audit.setUserAgent(userAgent != null ? userAgent : "unknown");
                }

                return auditRepository.save(audit).then();
            })
            .doOnNext(v -> LOG.debug("Created audit log for authority {} with action {}", authorityId, action));
    }

    /**
     * Convert an object to JSON string.
     *
     * @param object the object to convert
     * @return JSON string or null if conversion fails
     */
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting object to JSON", e);
            return null;
        }
    }
}
