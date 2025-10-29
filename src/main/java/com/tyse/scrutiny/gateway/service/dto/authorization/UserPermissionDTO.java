package com.tyse.scrutiny.gateway.service.dto.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.tyse.scrutiny.gateway.domain.UserPermission}
 */
public class UserPermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long permissionId;
    private String permissionName;
    private Boolean isActive;
    private Instant expiresAt;
    private String grantedBy;
    private Instant grantedDate;
    private String reason;
    private String revokedBy;
    private Instant revokedDate;
    private String revokedReason;

    // Computed fields
    private Boolean isExpired;
    private Boolean isValid;

    public UserPermissionDTO() {
        // Empty constructor needed for Jackson
    }

    public UserPermissionDTO(UserPermission userPermission, Permission permission) {
        this.id = userPermission.getId();
        this.userId = userPermission.getUserId();
        this.permissionId = userPermission.getPermissionId();
        this.isActive = userPermission.getIsActive();
        this.expiresAt = userPermission.getExpiresAt();
        this.grantedBy = userPermission.getGrantedBy();
        this.grantedDate = userPermission.getGrantedDate();
        this.reason = userPermission.getReason();
        this.revokedBy = userPermission.getRevokedBy();
        this.revokedDate = userPermission.getRevokedDate();
        this.revokedReason = userPermission.getRevokedReason();

        // Add permission info if provided
        if (permission != null) {
            this.permissionName = permission.getName();
        }

        // Compute fields
        this.isExpired = userPermission.isExpired();
        this.isValid = userPermission.isValid();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    public Instant getGrantedDate() {
        return grantedDate;
    }

    public void setGrantedDate(Instant grantedDate) {
        this.grantedDate = grantedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public Instant getRevokedDate() {
        return revokedDate;
    }

    public void setRevokedDate(Instant revokedDate) {
        this.revokedDate = revokedDate;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPermissionDTO)) return false;
        UserPermissionDTO that = (UserPermissionDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (
            "UserPermissionDTO{" +
            "id=" +
            id +
            ", userId=" +
            userId +
            ", permissionId=" +
            permissionId +
            ", permissionName='" +
            permissionName +
            '\'' +
            ", isActive=" +
            isActive +
            ", expiresAt=" +
            expiresAt +
            ", grantedBy='" +
            grantedBy +
            '\'' +
            ", grantedDate=" +
            grantedDate +
            ", reason='" +
            reason +
            '\'' +
            ", revokedBy='" +
            revokedBy +
            '\'' +
            ", revokedDate=" +
            revokedDate +
            ", revokedReason='" +
            revokedReason +
            '\'' +
            ", isExpired=" +
            isExpired +
            ", isValid=" +
            isValid +
            '}'
        );
    }
}
