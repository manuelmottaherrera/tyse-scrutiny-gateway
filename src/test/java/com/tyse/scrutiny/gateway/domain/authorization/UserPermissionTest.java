package com.tyse.scrutiny.gateway.domain.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.web.rest.TestUtil;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class UserPermissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserPermission.class);
        UserPermission userPermission1 = new UserPermission();
        userPermission1.setId(1L);
        UserPermission userPermission2 = new UserPermission();
        userPermission2.setId(userPermission1.getId());
        assertThat(userPermission1).isEqualTo(userPermission2);
        userPermission2.setId(2L);
        assertThat(userPermission1).isNotEqualTo(userPermission2);
        userPermission1.setId(null);
        assertThat(userPermission1).isNotEqualTo(userPermission2);
    }

    @Test
    void testUserPermissionCreation() {
        UserPermission userPermission = new UserPermission();
        userPermission.setId(1L);
        userPermission.setUserId(100L);
        userPermission.setPermissionId(5L);
        userPermission.setIsActive(true);
        userPermission.setGrantedBy("admin");
        userPermission.setGrantedDate(Instant.now());
        userPermission.setReason("Temporary project access");

        assertThat(userPermission.getId()).isEqualTo(1L);
        assertThat(userPermission.getUserId()).isEqualTo(100L);
        assertThat(userPermission.getPermissionId()).isEqualTo(5L);
        assertThat(userPermission.getIsActive()).isTrue();
        assertThat(userPermission.getGrantedBy()).isEqualTo("admin");
        assertThat(userPermission.getReason()).isEqualTo("Temporary project access");
    }

    @Test
    void testIsExpiredWithFutureDate() {
        UserPermission userPermission = new UserPermission();
        userPermission.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));

        assertThat(userPermission.isExpired()).isFalse();
    }

    @Test
    void testIsExpiredWithPastDate() {
        UserPermission userPermission = new UserPermission();
        userPermission.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));

        assertThat(userPermission.isExpired()).isTrue();
    }

    @Test
    void testIsExpiredWithNullDate() {
        UserPermission userPermission = new UserPermission();
        userPermission.setExpiresAt(null);

        assertThat(userPermission.isExpired()).isFalse();
    }

    @Test
    void testIsValidWhenActiveAndNotExpired() {
        UserPermission userPermission = new UserPermission();
        userPermission.setIsActive(true);
        userPermission.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));

        assertThat(userPermission.isValid()).isTrue();
    }

    @Test
    void testIsValidWhenInactive() {
        UserPermission userPermission = new UserPermission();
        userPermission.setIsActive(false);
        userPermission.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));

        assertThat(userPermission.isValid()).isFalse();
    }

    @Test
    void testIsValidWhenExpired() {
        UserPermission userPermission = new UserPermission();
        userPermission.setIsActive(true);
        userPermission.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

        assertThat(userPermission.isValid()).isFalse();
    }

    @Test
    void testRevocation() {
        UserPermission userPermission = new UserPermission();
        userPermission.setIsActive(false);
        userPermission.setRevokedBy("admin");
        userPermission.setRevokedDate(Instant.now());
        userPermission.setRevokedReason("Project completed");

        assertThat(userPermission.getIsActive()).isFalse();
        assertThat(userPermission.getRevokedBy()).isEqualTo("admin");
        assertThat(userPermission.getRevokedReason()).isEqualTo("Project completed");
        assertThat(userPermission.getRevokedDate()).isNotNull();
    }

    @Test
    void testFluentAPI() {
        Instant now = Instant.now();
        UserPermission userPermission = new UserPermission()
            .id(1L)
            .userId(100L)
            .permissionId(5L)
            .isActive(true)
            .expiresAt(now.plus(7, ChronoUnit.DAYS))
            .grantedBy("manager")
            .grantedDate(now)
            .reason("Emergency access");

        assertThat(userPermission.getId()).isEqualTo(1L);
        assertThat(userPermission.getUserId()).isEqualTo(100L);
        assertThat(userPermission.getPermissionId()).isEqualTo(5L);
        assertThat(userPermission.getReason()).isEqualTo("Emergency access");
    }
}
