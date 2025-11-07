package com.tyse.scrutiny.gateway.service.dto.dashboard;

import java.io.Serializable;

/**
 * DTO for permission usage statistics
 */
public class PermissionUsageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long permissionId;
    private String permissionName;
    private String resource;
    private String action;
    private Long usageCount;
    private Long authorityCount;
    private Long directUserCount;

    public PermissionUsageDTO() {}

    public PermissionUsageDTO(Long permissionId, String permissionName, String resource, String action, Long usageCount) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.resource = resource;
        this.action = action;
        this.usageCount = usageCount;
    }

    public PermissionUsageDTO(
        Long permissionId,
        String permissionName,
        String resource,
        String action,
        Long usageCount,
        Long authorityCount,
        Long directUserCount
    ) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.resource = resource;
        this.action = action;
        this.usageCount = usageCount;
        this.authorityCount = authorityCount;
        this.directUserCount = directUserCount;
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    public Long getAuthorityCount() {
        return authorityCount;
    }

    public void setAuthorityCount(Long authorityCount) {
        this.authorityCount = authorityCount;
    }

    public Long getDirectUserCount() {
        return directUserCount;
    }

    public void setDirectUserCount(Long directUserCount) {
        this.directUserCount = directUserCount;
    }

    @Override
    public String toString() {
        return (
            "PermissionUsageDTO{" +
            "permissionId=" +
            permissionId +
            ", permissionName='" +
            permissionName +
            '\'' +
            ", resource='" +
            resource +
            '\'' +
            ", action='" +
            action +
            '\'' +
            ", usageCount=" +
            usageCount +
            ", authorityCount=" +
            authorityCount +
            ", directUserCount=" +
            directUserCount +
            '}'
        );
    }
}
