package com.tyse.scrutiny.gateway.domain.authorization;

import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * An AuthorityAudit (Authority Change Log).
 * Comprehensive audit trail of all changes made to authorities/roles in the system.
 *
 * Captures:
 * - What changed (old vs new values as JSON)
 * - Who made the change (changed_by)
 * - When it happened (changed_date)
 * - Where it came from (ip_address, user_agent)
 * - Why it happened (action type)
 */
@Table("scr_authority_audit")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuthorityAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    /**
     * Foreign key to the authority that was changed (scr_authority.id).
     */
    @NotNull(message = "must not be null")
    @Column("authority_id")
    private Long authorityId;

    /**
     * Type of action performed on the authority.
     * Example: CREATED, UPDATED, DELETED, ACTIVATED, DEACTIVATED
     */
    @NotNull(message = "must not be null")
    @Column("action")
    private AuditAction action;

    /**
     * JSON representation of the authority's state before the change.
     * Null for CREATE actions.
     * Example: {"name":"Admin","code":"ROLE_ADMIN","isActive":true}
     */
    @Column("old_values")
    private String oldValues;

    /**
     * JSON representation of the authority's state after the change.
     * Null for DELETE actions.
     * Example: {"name":"Administrator","code":"ROLE_ADMIN","isActive":true}
     */
    @Column("new_values")
    private String newValues;

    /**
     * User ID or username of who made the change.
     */
    @NotNull(message = "must not be null")
    @Size(max = 50)
    @Column("changed_by")
    private String changedBy;

    /**
     * Date and time when the change occurred.
     */
    @NotNull(message = "must not be null")
    @Column("changed_date")
    private Instant changedDate = Instant.now();

    /**
     * IP address of the client that made the change.
     * Useful for security investigations and compliance.
     */
    @Size(max = 50)
    @Column("ip_address")
    private String ipAddress;

    /**
     * User agent (browser/client info) of the client that made the change.
     * Useful for tracking automation vs manual changes.
     */
    @Size(max = 500)
    @Column("user_agent")
    private String userAgent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AuthorityAudit id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorityId() {
        return this.authorityId;
    }

    public AuthorityAudit authorityId(Long authorityId) {
        this.setAuthorityId(authorityId);
        return this;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public AuditAction getAction() {
        return this.action;
    }

    public AuthorityAudit action(AuditAction action) {
        this.setAction(action);
        return this;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getOldValues() {
        return this.oldValues;
    }

    public AuthorityAudit oldValues(String oldValues) {
        this.setOldValues(oldValues);
        return this;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return this.newValues;
    }

    public AuthorityAudit newValues(String newValues) {
        this.setNewValues(newValues);
        return this;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getChangedBy() {
        return this.changedBy;
    }

    public AuthorityAudit changedBy(String changedBy) {
        this.setChangedBy(changedBy);
        return this;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Instant getChangedDate() {
        return this.changedDate;
    }

    public AuthorityAudit changedDate(Instant changedDate) {
        this.setChangedDate(changedDate);
        return this;
    }

    public void setChangedDate(Instant changedDate) {
        this.changedDate = changedDate;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public AuthorityAudit ipAddress(String ipAddress) {
        this.setIpAddress(ipAddress);
        return this;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public AuthorityAudit userAgent(String userAgent) {
        this.setUserAgent(userAgent);
        return this;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorityAudit)) {
            return false;
        }
        return getId() != null && getId().equals(((AuthorityAudit) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuthorityAudit{" +
            "id=" + getId() +
            ", authorityId=" + getAuthorityId() +
            ", action='" + getAction() + "'" +
            ", oldValues='" + getOldValues() + "'" +
            ", newValues='" + getNewValues() + "'" +
            ", changedBy='" + getChangedBy() + "'" +
            ", changedDate='" + getChangedDate() + "'" +
            ", ipAddress='" + getIpAddress() + "'" +
            ", userAgent='" + getUserAgent() + "'" +
            "}";
    }
}
