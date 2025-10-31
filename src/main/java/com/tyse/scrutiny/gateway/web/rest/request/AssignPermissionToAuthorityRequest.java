package com.tyse.scrutiny.gateway.web.rest.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for assigning a permission to an authority (role).
 */
public class AssignPermissionToAuthorityRequest {

    @NotNull(message = "Authority ID must not be null")
    private Long authorityId;

    @NotNull(message = "Permission ID must not be null")
    private Long permissionId;

    public Long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public String toString() {
        return "AssignPermissionToAuthorityRequest{" + "authorityId=" + authorityId + ", permissionId=" + permissionId + '}';
    }
}
