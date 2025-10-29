package com.tyse.scrutiny.gateway.service.dto.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Read-only DTO for {@link com.tyse.scrutiny.gateway.domain.AuthorityAudit}
 */
public class AuthorityAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long authorityId;
    private String authorityCode;
    private AuditAction action;
    private String oldValues;
    private String newValues;
    private String changedBy;
    private Instant changedDate;
    private String ipAddress;
    private String userAgent;

    public AuthorityAuditDTO() {
        // Empty constructor needed for Jackson
    }

    public AuthorityAuditDTO(AuthorityAudit audit) {
        this.id = audit.getId();
        this.authorityId = audit.getAuthorityId();
        this.action = audit.getAction();
        this.oldValues = audit.getOldValues();
        this.newValues = audit.getNewValues();
        this.changedBy = audit.getChangedBy();
        this.changedDate = audit.getChangedDate();
        this.ipAddress = audit.getIpAddress();
        this.userAgent = audit.getUserAgent();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public String getAuthorityCode() {
        return authorityCode;
    }

    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Instant getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(Instant changedDate) {
        this.changedDate = changedDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorityAuditDTO)) return false;
        AuthorityAuditDTO that = (AuthorityAuditDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (
            "AuthorityAuditDTO{" +
            "id=" +
            id +
            ", authorityId=" +
            authorityId +
            ", authorityCode='" +
            authorityCode +
            '\'' +
            ", action=" +
            action +
            ", oldValues='" +
            oldValues +
            '\'' +
            ", newValues='" +
            newValues +
            '\'' +
            ", changedBy='" +
            changedBy +
            '\'' +
            ", changedDate=" +
            changedDate +
            ", ipAddress='" +
            ipAddress +
            '\'' +
            ", userAgent='" +
            userAgent +
            '\'' +
            '}'
        );
    }
}
