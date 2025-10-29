package com.tyse.scrutiny.gateway.domain.authorization;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A UserAuthority (User-Role Assignment).
 * Represents the many-to-many relationship between users and authorities (roles)
 * with full audit trail, expiration support, and revocation tracking.
 *
 * This is the enterprise replacement for the legacy jhi_user_authority join table.
 */
@Table("scr_user_authority")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UserAuthority implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    /**
     * Foreign key to the user (scr_user.id).
     */
    @NotNull(message = "must not be null")
    @Column("user_id")
    private Long userId;

    /**
     * Foreign key to the authority/role (scr_authority.id).
     */
    @NotNull(message = "must not be null")
    @Column("authority_id")
    private Long authorityId;

    /**
     * Whether this assignment is currently active.
     * Inactive assignments are ignored during authorization checks.
     * Setting to false is a "soft delete" alternative to actual deletion.
     */
    @NotNull(message = "must not be null")
    @Column("is_active")
    private Boolean isActive = true;

    /**
     * Optional expiration date for the assignment.
     * After this date, the role should be auto-revoked by a scheduled job.
     * Null means no expiration (permanent assignment).
     */
    @Column("expires_at")
    private Instant expiresAt;

    /**
     * User ID or username of who assigned this role.
     */
    @NotNull(message = "must not be null")
    @Size(max = 50)
    @Column("assigned_by")
    private String assignedBy;

    /**
     * Date and time when this role was assigned.
     */
    @NotNull(message = "must not be null")
    @Column("assigned_date")
    private Instant assignedDate = Instant.now();

    /**
     * User ID or username of who revoked this role (if revoked).
     * Null if not revoked.
     */
    @Size(max = 50)
    @Column("revoked_by")
    private String revokedBy;

    /**
     * Date and time when this role was revoked (if revoked).
     * Null if not revoked.
     */
    @Column("revoked_date")
    private Instant revokedDate;

    /**
     * Reason for revoking this role (if revoked).
     * Examples: "Security incident", "User left team", "Temporary assignment expired"
     */
    @Size(max = 500)
    @Column("revoked_reason")
    private String revokedReason;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public UserAuthority id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public UserAuthority userId(Long userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAuthorityId() {
        return this.authorityId;
    }

    public UserAuthority authorityId(Long authorityId) {
        this.setAuthorityId(authorityId);
        return this;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public UserAuthority isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public UserAuthority expiresAt(Instant expiresAt) {
        this.setExpiresAt(expiresAt);
        return this;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getAssignedBy() {
        return this.assignedBy;
    }

    public UserAuthority assignedBy(String assignedBy) {
        this.setAssignedBy(assignedBy);
        return this;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getAssignedDate() {
        return this.assignedDate;
    }

    public UserAuthority assignedDate(Instant assignedDate) {
        this.setAssignedDate(assignedDate);
        return this;
    }

    public void setAssignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getRevokedBy() {
        return this.revokedBy;
    }

    public UserAuthority revokedBy(String revokedBy) {
        this.setRevokedBy(revokedBy);
        return this;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public Instant getRevokedDate() {
        return this.revokedDate;
    }

    public UserAuthority revokedDate(Instant revokedDate) {
        this.setRevokedDate(revokedDate);
        return this;
    }

    public void setRevokedDate(Instant revokedDate) {
        this.revokedDate = revokedDate;
    }

    public String getRevokedReason() {
        return this.revokedReason;
    }

    public UserAuthority revokedReason(String revokedReason) {
        this.setRevokedReason(revokedReason);
        return this;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    /**
     * Check if this assignment has expired.
     *
     * @return true if expiresAt is not null and is in the past
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    /**
     * Check if this assignment is currently valid (active and not expired).
     *
     * @return true if active and not expired
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAuthority)) {
            return false;
        }
        return getId() != null && getId().equals(((UserAuthority) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserAuthority{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", authorityId=" + getAuthorityId() +
            ", isActive='" + getIsActive() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            ", assignedBy='" + getAssignedBy() + "'" +
            ", assignedDate='" + getAssignedDate() + "'" +
            ", revokedBy='" + getRevokedBy() + "'" +
            ", revokedDate='" + getRevokedDate() + "'" +
            ", revokedReason='" + getRevokedReason() + "'" +
            "}";
    }
}
