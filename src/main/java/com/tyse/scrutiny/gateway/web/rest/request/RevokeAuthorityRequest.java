package com.tyse.scrutiny.gateway.web.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Request DTO for revoking an authority assignment from a user.
 */
public class RevokeAuthorityRequest implements Serializable {

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    public RevokeAuthorityRequest() {}

    public RevokeAuthorityRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "RevokeAuthorityRequest{" + "reason='" + reason + '\'' + '}';
    }
}
