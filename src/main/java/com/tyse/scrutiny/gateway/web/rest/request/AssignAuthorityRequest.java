package com.tyse.scrutiny.gateway.web.rest.request;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * Request DTO for assigning an authority to a user.
 */
public class AssignAuthorityRequest implements Serializable {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Authority ID is required")
    private Long authorityId;

    private Instant expiresAt;

    public AssignAuthorityRequest() {}

    public AssignAuthorityRequest(Long userId, Long authorityId, Instant expiresAt) {
        this.userId = userId;
        this.authorityId = authorityId;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return ("AssignAuthorityRequest{" + "userId=" + userId + ", authorityId=" + authorityId + ", expiresAt=" + expiresAt + '}');
    }
}
