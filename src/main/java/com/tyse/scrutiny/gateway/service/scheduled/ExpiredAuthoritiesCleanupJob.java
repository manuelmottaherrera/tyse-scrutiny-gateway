package com.tyse.scrutiny.gateway.service.scheduled;

import com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Scheduled job to cleanup expired authority and permission assignments.
 */
@Service
public class ExpiredAuthoritiesCleanupJob {

    private static final Logger LOG = LoggerFactory.getLogger(ExpiredAuthoritiesCleanupJob.class);

    private final UserAuthorityRepository userAuthorityRepository;
    private final UserPermissionRepository userPermissionRepository;

    public ExpiredAuthoritiesCleanupJob(
        UserAuthorityRepository userAuthorityRepository,
        UserPermissionRepository userPermissionRepository
    ) {
        this.userAuthorityRepository = userAuthorityRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    /**
     * Mark expired authority assignments as inactive.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredAuthorities() {
        LOG.info("Starting cleanup of expired authority assignments");

        userAuthorityRepository
            .findExpiredAssignments()
            .flatMap(assignment -> {
                assignment.setIsActive(false);
                return userAuthorityRepository.save(assignment);
            })
            .count()
            .doOnNext(count -> LOG.info("Cleaned up {} expired authority assignments", count))
            .doOnError(error -> LOG.error("Error cleaning up expired authority assignments", error))
            .onErrorResume(error -> Mono.empty())
            .subscribe();
    }

    /**
     * Mark expired permission grants as inactive.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredPermissions() {
        LOG.info("Starting cleanup of expired permission grants");

        userPermissionRepository
            .findExpiredGrants()
            .flatMap(grant -> {
                grant.setIsActive(false);
                return userPermissionRepository.save(grant);
            })
            .count()
            .doOnNext(count -> LOG.info("Cleaned up {} expired permission grants", count))
            .doOnError(error -> LOG.error("Error cleaning up expired permission grants", error))
            .onErrorResume(error -> Mono.empty())
            .subscribe();
    }
}
