package com.tyse.scrutiny.gateway.domain.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import com.tyse.scrutiny.gateway.web.rest.TestUtil;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthorityAuditTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuthorityAudit.class);
        AuthorityAudit authorityAudit1 = new AuthorityAudit();
        authorityAudit1.setId(1L);
        AuthorityAudit authorityAudit2 = new AuthorityAudit();
        authorityAudit2.setId(authorityAudit1.getId());
        assertThat(authorityAudit1).isEqualTo(authorityAudit2);
        authorityAudit2.setId(2L);
        assertThat(authorityAudit1).isNotEqualTo(authorityAudit2);
        authorityAudit1.setId(null);
        assertThat(authorityAudit1).isNotEqualTo(authorityAudit2);
    }

    @Test
    void testAuthorityAuditCreation() {
        AuthorityAudit audit = new AuthorityAudit();
        audit.setId(1L);
        audit.setAuthorityId(10L);
        audit.setAction(AuditAction.CREATED);
        audit.setOldValues(null);
        audit.setNewValues("{\"name\":\"Admin\",\"code\":\"ROLE_ADMIN\"}");
        audit.setChangedBy("admin");

        Instant changedDate = Instant.now();
        audit.setChangedDate(changedDate);
        audit.setIpAddress("192.168.1.1");
        audit.setUserAgent("Mozilla/5.0");

        assertThat(audit.getId()).isEqualTo(1L);
        assertThat(audit.getAuthorityId()).isEqualTo(10L);
        assertThat(audit.getAction()).isEqualTo(AuditAction.CREATED);
        assertThat(audit.getOldValues()).isNull();
        assertThat(audit.getNewValues()).isEqualTo("{\"name\":\"Admin\",\"code\":\"ROLE_ADMIN\"}");
        assertThat(audit.getChangedBy()).isEqualTo("admin");
        assertThat(audit.getChangedDate()).isEqualTo(changedDate);
        assertThat(audit.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(audit.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    void testAuthorityAuditFluentAPI() {
        Instant changedDate = Instant.now();

        AuthorityAudit audit = new AuthorityAudit()
            .id(2L)
            .authorityId(15L)
            .action(AuditAction.UPDATED)
            .oldValues("{\"name\":\"Admin\"}")
            .newValues("{\"name\":\"Administrator\"}")
            .changedBy("system")
            .changedDate(changedDate)
            .ipAddress("10.0.0.1")
            .userAgent("Chrome");

        assertThat(audit.getId()).isEqualTo(2L);
        assertThat(audit.getAuthorityId()).isEqualTo(15L);
        assertThat(audit.getAction()).isEqualTo(AuditAction.UPDATED);
        assertThat(audit.getOldValues()).isEqualTo("{\"name\":\"Admin\"}");
        assertThat(audit.getNewValues()).isEqualTo("{\"name\":\"Administrator\"}");
        assertThat(audit.getChangedBy()).isEqualTo("system");
        assertThat(audit.getChangedDate()).isEqualTo(changedDate);
        assertThat(audit.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(audit.getUserAgent()).isEqualTo("Chrome");
    }

    @Test
    void testToString() {
        AuthorityAudit audit = new AuthorityAudit();
        audit.setId(1L);
        audit.setAuthorityId(10L);
        audit.setAction(AuditAction.CREATED);
        audit.setChangedBy("admin");

        String toString = audit.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("authorityId=10");
        assertThat(toString).contains("action='Created'"); // Enum displays with capitalization
        assertThat(toString).contains("changedBy='admin'");
    }

    @Test
    void testDefaultChangedDate() {
        AuthorityAudit audit = new AuthorityAudit();

        // Verify that changedDate is set by default
        assertThat(audit.getChangedDate()).isNotNull();
        assertThat(audit.getChangedDate()).isBefore(Instant.now().plusSeconds(1));
    }

    @Test
    void testCreateAuditAction() {
        // CREATE action: oldValues is null, newValues has data
        AuthorityAudit createAudit = new AuthorityAudit();
        createAudit.setAction(AuditAction.CREATED);
        createAudit.setOldValues(null);
        createAudit.setNewValues("{\"code\":\"ROLE_NEW\"}");

        assertThat(createAudit.getAction()).isEqualTo(AuditAction.CREATED);
        assertThat(createAudit.getOldValues()).isNull();
        assertThat(createAudit.getNewValues()).isNotNull();
    }

    @Test
    void testUpdateAuditAction() {
        // UPDATE action: both oldValues and newValues have data
        AuthorityAudit updateAudit = new AuthorityAudit();
        updateAudit.setAction(AuditAction.UPDATED);
        updateAudit.setOldValues("{\"name\":\"Old Name\"}");
        updateAudit.setNewValues("{\"name\":\"New Name\"}");

        assertThat(updateAudit.getAction()).isEqualTo(AuditAction.UPDATED);
        assertThat(updateAudit.getOldValues()).isNotNull();
        assertThat(updateAudit.getNewValues()).isNotNull();
    }

    @Test
    void testDeleteAuditAction() {
        // DELETE action: oldValues has data, newValues is null
        AuthorityAudit deleteAudit = new AuthorityAudit();
        deleteAudit.setAction(AuditAction.DELETED);
        deleteAudit.setOldValues("{\"code\":\"ROLE_OLD\"}");
        deleteAudit.setNewValues(null);

        assertThat(deleteAudit.getAction()).isEqualTo(AuditAction.DELETED);
        assertThat(deleteAudit.getOldValues()).isNotNull();
        assertThat(deleteAudit.getNewValues()).isNull();
    }

    @Test
    void testActivateDeactivateActions() {
        AuthorityAudit activateAudit = new AuthorityAudit();
        activateAudit.setAction(AuditAction.ACTIVATED);
        assertThat(activateAudit.getAction()).isEqualTo(AuditAction.ACTIVATED);

        AuthorityAudit deactivateAudit = new AuthorityAudit();
        deactivateAudit.setAction(AuditAction.DEACTIVATED);
        assertThat(deactivateAudit.getAction()).isEqualTo(AuditAction.DEACTIVATED);
    }

    @Test
    void testAuditWithIPAddress() {
        AuthorityAudit audit = new AuthorityAudit();
        audit.setIpAddress("192.168.1.100");

        assertThat(audit.getIpAddress()).isEqualTo("192.168.1.100");
    }

    @Test
    void testAuditWithUserAgent() {
        AuthorityAudit audit = new AuthorityAudit();
        audit.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        assertThat(audit.getUserAgent()).isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
    }

    @Test
    void testMultipleAuditsForSameAuthority() {
        // Authority goes through multiple changes
        AuthorityAudit audit1 = new AuthorityAudit();
        audit1.setId(1L);
        audit1.setAuthorityId(10L);
        audit1.setAction(AuditAction.CREATED);
        audit1.setChangedBy("admin");

        AuthorityAudit audit2 = new AuthorityAudit();
        audit2.setId(2L);
        audit2.setAuthorityId(10L); // Same authority
        audit2.setAction(AuditAction.UPDATED);
        audit2.setChangedBy("admin");

        AuthorityAudit audit3 = new AuthorityAudit();
        audit3.setId(3L);
        audit3.setAuthorityId(10L); // Same authority
        audit3.setAction(AuditAction.DEACTIVATED);
        audit3.setChangedBy("admin");

        assertThat(audit1.getAuthorityId()).isEqualTo(audit2.getAuthorityId());
        assertThat(audit2.getAuthorityId()).isEqualTo(audit3.getAuthorityId());
        assertThat(audit1.getAction()).isNotEqualTo(audit2.getAction());
        assertThat(audit2.getAction()).isNotEqualTo(audit3.getAction());
    }

    @Test
    void testChangedByAuditField() {
        AuthorityAudit systemChange = new AuthorityAudit();
        systemChange.setChangedBy("system");
        assertThat(systemChange.getChangedBy()).isEqualTo("system");

        AuthorityAudit adminChange = new AuthorityAudit();
        adminChange.setChangedBy("admin_user");
        assertThat(adminChange.getChangedBy()).isEqualTo("admin_user");
    }

    @Test
    void testChangedDateTimestamp() {
        Instant before = Instant.now();
        AuthorityAudit audit = new AuthorityAudit();
        Instant after = Instant.now();

        // The default changedDate should be between before and after
        assertThat(audit.getChangedDate()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }

    @Test
    void testJSONValuesFormat() {
        AuthorityAudit audit = new AuthorityAudit();
        String oldJson = "{\"name\":\"Administrator\",\"code\":\"ROLE_ADMIN\",\"isActive\":true}";
        String newJson = "{\"name\":\"Admin\",\"code\":\"ROLE_ADMIN\",\"isActive\":false}";

        audit.setOldValues(oldJson);
        audit.setNewValues(newJson);

        assertThat(audit.getOldValues()).isEqualTo(oldJson);
        assertThat(audit.getNewValues()).isEqualTo(newJson);
        assertThat(audit.getOldValues()).isNotEqualTo(audit.getNewValues());
    }
}
