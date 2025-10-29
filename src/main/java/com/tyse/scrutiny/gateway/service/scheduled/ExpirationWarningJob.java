package com.tyse.scrutiny.gateway.service.scheduled;

import com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Scheduled job to detect and warn about expiring authority and permission assignments.
 */
@Service
public class ExpirationWarningJob {

    private static final Logger LOG = LoggerFactory.getLogger(ExpirationWarningJob.class);
    private static final int WARNING_DAYS = 7;

    private final UserAuthorityRepository userAuthorityRepository;
    private final UserPermissionRepository userPermissionRepository;

    public ExpirationWarningJob(UserAuthorityRepository userAuthorityRepository, UserPermissionRepository userPermissionRepository) {
        this.userAuthorityRepository = userAuthorityRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    /**
     * Detect authority assignments expiring within the next 7 days.
     * Runs daily at 9 AM.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringAuthorities() {
        LOG.info("Checking for authority assignments expiring within {} days", WARNING_DAYS);

        userAuthorityRepository
            .findExpiringWithinDays(WARNING_DAYS)
            .doOnNext(assignment ->
                LOG.warn(
                    "Authority assignment {} for user {} expires at {}",
                    assignment.getAuthorityId(),
                    assignment.getUserId(),
                    assignment.getExpiresAt()
                )
            )
            .count()
            .doOnNext(count -> LOG.info("Found {} expiring authority assignments", count))
            .doOnError(error -> LOG.error("Error checking expiring authority assignments", error))
            .onErrorResume(error -> Mono.empty())
            .subscribe();
        // TODO: Integrate with MailService to send email notifications
        // Example implementation:
        // userAuthorityRepository.findExpiringWithinDays(WARNING_DAYS)
        //     .flatMap(assignment -> userRepository.findById(assignment.getUserId())
        //         .flatMap(user -> authorityRepository.findById(assignment.getAuthorityId())
        //             .flatMap(authority -> mailService.sendExpirationWarningEmail(
        //                 user.getEmail(),
        //                 authority.getName(),
        //                 assignment.getExpiresAt()
        //             ))
        //         )
        //     )
        //     .subscribe();
    }

    /**
     * Detect permission grants expiring within the next 7 days.
     * Runs daily at 9 AM.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringPermissions() {
        LOG.info("Checking for permission grants expiring within {} days", WARNING_DAYS);

        userPermissionRepository
            .findExpiringWithinDays(WARNING_DAYS)
            .doOnNext(grant ->
                LOG.warn("Permission grant {} for user {} expires at {}", grant.getPermissionId(), grant.getUserId(), grant.getExpiresAt())
            )
            .count()
            .doOnNext(count -> LOG.info("Found {} expiring permission grants", count))
            .doOnError(error -> LOG.error("Error checking expiring permission grants", error))
            .onErrorResume(error -> Mono.empty())
            .subscribe();
        // TODO: Integrate with MailService to send email notifications
    }
}
