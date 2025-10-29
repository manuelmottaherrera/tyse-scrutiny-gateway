package com.tyse.scrutiny.gateway.domain.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.web.rest.TestUtil;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class UserAuthorityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserAuthority.class);
        UserAuthority userAuthority1 = new UserAuthority();
        userAuthority1.setId(1L);
        UserAuthority userAuthority2 = new UserAuthority();
        userAuthority2.setId(userAuthority1.getId());
        assertThat(userAuthority1).isEqualTo(userAuthority2);
        userAuthority2.setId(2L);
        assertThat(userAuthority1).isNotEqualTo(userAuthority2);
        userAuthority1.setId(null);
        assertThat(userAuthority1).isNotEqualTo(userAuthority2);
    }

    @Test
    void testUserAuthorityCreation() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setId(1L);
        userAuthority.setUserId(100L);
        userAuthority.setAuthorityId(1L);
        userAuthority.setIsActive(true);
        userAuthority.setAssignedBy("admin");
        userAuthority.setAssignedDate(Instant.now());

        assertThat(userAuthority.getId()).isEqualTo(1L);
        assertThat(userAuthority.getUserId()).isEqualTo(100L);
        assertThat(userAuthority.getAuthorityId()).isEqualTo(1L);
        assertThat(userAuthority.getIsActive()).isTrue();
        assertThat(userAuthority.getAssignedBy()).isEqualTo("admin");
        assertThat(userAuthority.getAssignedDate()).isNotNull();
    }

    @Test
    void testIsExpiredWithFutureDate() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

        assertThat(userAuthority.isExpired()).isFalse();
    }

    @Test
    void testIsExpiredWithPastDate() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

        assertThat(userAuthority.isExpired()).isTrue();
    }

    @Test
    void testIsExpiredWithNullDate() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setExpiresAt(null);

        assertThat(userAuthority.isExpired()).isFalse();
    }

    @Test
    void testIsValidWhenActiveAndNotExpired() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setIsActive(true);
        userAuthority.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

        assertThat(userAuthority.isValid()).isTrue();
    }

    @Test
    void testIsValidWhenInactive() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setIsActive(false);
        userAuthority.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

        assertThat(userAuthority.isValid()).isFalse();
    }

    @Test
    void testIsValidWhenExpired() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setIsActive(true);
        userAuthority.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

        assertThat(userAuthority.isValid()).isFalse();
    }

    @Test
    void testRevocation() {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setIsActive(false);
        userAuthority.setRevokedBy("admin");
        userAuthority.setRevokedDate(Instant.now());
        userAuthority.setRevokedReason("Security incident");

        assertThat(userAuthority.getIsActive()).isFalse();
        assertThat(userAuthority.getRevokedBy()).isEqualTo("admin");
        assertThat(userAuthority.getRevokedReason()).isEqualTo("Security incident");
        assertThat(userAuthority.getRevokedDate()).isNotNull();
    }

    @Test
    void testFluentAPI() {
        Instant now = Instant.now();
        UserAuthority userAuthority = new UserAuthority()
            .id(1L)
            .userId(100L)
            .authorityId(2L)
            .isActive(true)
            .expiresAt(now.plus(30, ChronoUnit.DAYS))
            .assignedBy("admin")
            .assignedDate(now);

        assertThat(userAuthority.getId()).isEqualTo(1L);
        assertThat(userAuthority.getUserId()).isEqualTo(100L);
        assertThat(userAuthority.getAuthorityId()).isEqualTo(2L);
        assertThat(userAuthority.getIsActive()).isTrue();
    }
}
