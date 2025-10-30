package com.tyse.scrutiny.gateway.service.authorization;

import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository;
import com.tyse.scrutiny.gateway.security.SecurityUtils;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing user authority assignments.
 */
@Service
@Transactional
public class UserAuthorityService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAuthorityService.class);

    private final UserAuthorityRepository userAuthorityRepository;
    private final AuthorityRepository authorityRepository;

    public UserAuthorityService(UserAuthorityRepository userAuthorityRepository, AuthorityRepository authorityRepository) {
        this.userAuthorityRepository = userAuthorityRepository;
        this.authorityRepository = authorityRepository;
    }

    /**
     * Assign an authority to a user.
     *
     * @param userId the user id
     * @param authorityId the authority id
     * @param expiresAt optional expiration date
     * @return the created assignment
     */
    public Mono<UserAuthority> assignAuthority(Long userId, Long authorityId, Instant expiresAt) {
        LOG.debug("Request to assign authority {} to user {}", authorityId, userId);

        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(assignedBy -> {
                UserAuthority assignment = new UserAuthority();
                assignment.setUserId(userId);
                assignment.setAuthorityId(authorityId);
                assignment.setIsActive(true);
                assignment.setExpiresAt(expiresAt);
                assignment.setAssignedBy(assignedBy);
                assignment.setAssignedDate(Instant.now());

                return userAuthorityRepository
                    .save(assignment)
                    .doOnNext(saved -> LOG.debug("Assigned authority {} to user {} by {}", authorityId, userId, assignedBy));
            });
    }

    /**
     * Revoke an authority from a user.
     *
     * @param userId the user id
     * @param authorityId the authority id
     * @param reason the reason for revocation
     * @return the updated assignment
     */
    public Mono<UserAuthority> revokeAuthority(Long userId, Long authorityId, String reason) {
        LOG.debug("Request to revoke authority {} from user {}", authorityId, userId);

        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(revokedBy ->
                userAuthorityRepository
                    .findByUserIdAndAuthorityId(userId, authorityId)
                    .switchIfEmpty(
                        Mono.error(
                            new IllegalArgumentException(
                                "No active authority assignment found for user " + userId + " and authority " + authorityId
                            )
                        )
                    )
                    .flatMap(assignment -> {
                        assignment.setIsActive(false);
                        assignment.setRevokedBy(revokedBy);
                        assignment.setRevokedDate(Instant.now());
                        assignment.setRevokedReason(reason);

                        return userAuthorityRepository
                            .save(assignment)
                            .doOnNext(saved ->
                                LOG.debug("Revoked authority {} from user {} by {}: {}", authorityId, userId, revokedBy, reason)
                            );
                    })
            );
    }

    /**
     * Get all valid (active and not expired) authority assignments for a user.
     *
     * @param userId the user id
     * @return flux of valid assignments
     */
    @Transactional(readOnly = true)
    public Flux<UserAuthority> getValidAuthorities(Long userId) {
        LOG.debug("Request to get valid authorities for user {}", userId);
        return userAuthorityRepository.findValidByUserId(userId);
    }

    /**
     * Check if a user has a specific authority (by code).
     *
     * @param userId the user id
     * @param authorityCode the authority code (e.g., "ROLE_ADMIN")
     * @return true if user has the authority, false otherwise
     */
    @Transactional(readOnly = true)
    public Mono<Boolean> userHasAuthority(Long userId, String authorityCode) {
        LOG.debug("Request to check if user {} has authority {}", userId, authorityCode);
        return authorityRepository
            .findByCode(authorityCode)
            .flatMap(authority -> userAuthorityRepository.userHasAuthority(userId, authority.getId()))
            .defaultIfEmpty(false);
    }

    /**
     * Find all expired authority assignments.
     *
     * @return flux of expired assignments
     */
    @Transactional(readOnly = true)
    public Flux<UserAuthority> findExpiredAssignments() {
        LOG.debug("Request to find expired authority assignments");
        return userAuthorityRepository.findExpiredAssignments();
    }

    /**
     * Find authority assignments expiring within a specified number of days.
     *
     * @param days the number of days
     * @return flux of expiring assignments
     */
    @Transactional(readOnly = true)
    public Flux<UserAuthority> findExpiringWithinDays(int days) {
        LOG.debug("Request to find authority assignments expiring within {} days", days);
        return userAuthorityRepository.findExpiringWithinDays(days);
    }

    /**
     * Get all authority assignments for a user (including inactive and expired).
     *
     * @param userId the user id
     * @return flux of all assignments
     */
    @Transactional(readOnly = true)
    public Flux<UserAuthority> getAllAuthorities(Long userId) {
        LOG.debug("Request to get all authority assignments for user {}", userId);
        return userAuthorityRepository.findByUserId(userId);
    }

    /**
     * Revoke an authority assignment by its ID.
     *
     * @param id the assignment id
     * @param reason the reason for revocation
     * @return the updated assignment
     */
    public Mono<UserAuthority> revokeById(Long id, String reason) {
        LOG.debug("Request to revoke authority assignment with id: {}", id);

        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(revokedBy ->
                userAuthorityRepository
                    .findById(id)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("No authority assignment found with id " + id)))
                    .flatMap(assignment -> {
                        assignment.setIsActive(false);
                        assignment.setRevokedBy(revokedBy);
                        assignment.setRevokedDate(Instant.now());
                        assignment.setRevokedReason(reason);

                        return userAuthorityRepository
                            .save(assignment)
                            .doOnNext(saved -> LOG.debug("Revoked authority assignment {} by {}: {}", id, revokedBy, reason));
                    })
            );
    }

    /**
     * Delete an authority assignment permanently.
     *
     * @param id the assignment id
     * @return void
     */
    public Mono<Void> deleteAssignment(Long id) {
        LOG.debug("Request to delete authority assignment with id: {}", id);
        return userAuthorityRepository.deleteById(id).doOnSuccess(v -> LOG.debug("Deleted authority assignment with id: {}", id));
    }
}
