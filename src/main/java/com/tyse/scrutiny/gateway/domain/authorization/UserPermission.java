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
 * A UserPermission (Direct Permission Assignment).
 * Represents direct permissions granted to a user without requiring a role.
 *
 * This allows fine-grained permission control, such as:
 * - Granting "invoice.approve" to a user for 30 days without making them a full Manager
 * - Temporary elevated permissions for specific tasks
 * - Exception-based access control
 */
@Table("scr_user_permission")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UserPermission implements Serializable {

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
     * Foreign key to the permission (scr_permission.id).
     */
    @NotNull(message = "must not be null")
    @Column("permission_id")
    private Long permissionId;

    /**
     * Whether this permission grant is currently active.
     * Inactive grants are ignored during authorization checks.
     */
    @NotNull(message = "must not be null")
    @Column("is_active")
    private Boolean isActive = true;

    /**
     * Optional expiration date for the permission grant.
     * After this date, the permission should be auto-revoked by a scheduled job.
     * Null means no expiration (permanent grant).
     */
    @Column("expires_at")
    private Instant expiresAt;

    /**
     * User ID or username of who granted this permission.
     */
    @NotNull(message = "must not be null")
    @Size(max = 50)
    @Column("granted_by")
    private String grantedBy;

    /**
     * Date and time when this permission was granted.
     */
    @NotNull(message = "must not be null")
    @Column("granted_date")
    private Instant grantedDate = Instant.now();

    /**
     * Reason for granting this direct permission.
     * Examples: "Temporary project access", "Emergency authorization", "Exception approval"
     */
    @Size(max = 500)
    @Column("reason")
    private String reason;

    /**
     * User ID or username of who revoked this permission (if revoked).
     * Null if not revoked.
     */
    @Size(max = 50)
    @Column("revoked_by")
    private String revokedBy;

    /**
     * Date and time when this permission was revoked (if revoked).
     * Null if not revoked.
     */
    @Column("revoked_date")
    private Instant revokedDate;

    /**
     * Reason for revoking this permission (if revoked).
     * Examples: "Project completed", "Security concern", "Temporary grant expired"
     */
    @Size(max = 500)
    @Column("revoked_reason")
    private String revokedReason;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public UserPermission id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public UserPermission userId(Long userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPermissionId() {
        return this.permissionId;
    }

    public UserPermission permissionId(Long permissionId) {
        this.setPermissionId(permissionId);
        return this;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public UserPermission isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public UserPermission expiresAt(Instant expiresAt) {
        this.setExpiresAt(expiresAt);
        return this;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getGrantedBy() {
        return this.grantedBy;
    }

    public UserPermission grantedBy(String grantedBy) {
        this.setGrantedBy(grantedBy);
        return this;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    public Instant getGrantedDate() {
        return this.grantedDate;
    }

    public UserPermission grantedDate(Instant grantedDate) {
        this.setGrantedDate(grantedDate);
        return this;
    }

    public void setGrantedDate(Instant grantedDate) {
        this.grantedDate = grantedDate;
    }

    public String getReason() {
        return this.reason;
    }

    public UserPermission reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRevokedBy() {
        return this.revokedBy;
    }

    public UserPermission revokedBy(String revokedBy) {
        this.setRevokedBy(revokedBy);
        return this;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public Instant getRevokedDate() {
        return this.revokedDate;
    }

    public UserPermission revokedDate(Instant revokedDate) {
        this.setRevokedDate(revokedDate);
        return this;
    }

    public void setRevokedDate(Instant revokedDate) {
        this.revokedDate = revokedDate;
    }

    public String getRevokedReason() {
        return this.revokedReason;
    }

    public UserPermission revokedReason(String revokedReason) {
        this.setRevokedReason(revokedReason);
        return this;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    /**
     * Check if this permission grant has expired.
     *
     * @return true if expiresAt is not null and is in the past
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    /**
     * Check if this permission grant is currently valid (active and not expired).
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
        if (!(o instanceof UserPermission)) {
            return false;
        }
        return getId() != null && getId().equals(((UserPermission) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserPermission{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", permissionId=" + getPermissionId() +
            ", isActive='" + getIsActive() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            ", grantedBy='" + getGrantedBy() + "'" +
            ", grantedDate='" + getGrantedDate() + "'" +
            ", reason='" + getReason() + "'" +
            ", revokedBy='" + getRevokedBy() + "'" +
            ", revokedDate='" + getRevokedDate() + "'" +
            ", revokedReason='" + getRevokedReason() + "'" +
            "}";
    }
}
