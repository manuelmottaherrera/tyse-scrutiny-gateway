package com.tyse.scrutiny.gateway.web.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

/**
 * Request DTO for granting a direct permission to a user.
 */
public class GrantPermissionRequest implements Serializable {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Permission ID is required")
    private Long permissionId;

    private Instant expiresAt;

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    public GrantPermissionRequest() {}

    public GrantPermissionRequest(Long userId, Long permissionId, Instant expiresAt, String reason) {
        this.userId = userId;
        this.permissionId = permissionId;
        this.expiresAt = expiresAt;
        this.reason = reason;
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return (
            "GrantPermissionRequest{" +
            "userId=" +
            userId +
            ", permissionId=" +
            permissionId +
            ", expiresAt=" +
            expiresAt +
            ", reason='" +
            reason +
            '\'' +
            '}'
        );
    }
}
