package com.tyse.scrutiny.gateway.service.dto.dashboard;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for dashboard metrics
 */
public class DashboardMetricsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalAuthorities;
    private Long totalPermissions;
    private Long activeUsers;
    private Long expiredRoles;

    private List<RecentActivityDTO> recentActivity;
    private List<ExpiringRoleDTO> expiringRoles;
    private List<AuthorityUsageDTO> topAuthorities;
    private List<PermissionUsageDTO> permissionUsage;

    public DashboardMetricsDTO() {}

    public DashboardMetricsDTO(
        Long totalAuthorities,
        Long totalPermissions,
        Long activeUsers,
        Long expiredRoles,
        List<RecentActivityDTO> recentActivity,
        List<ExpiringRoleDTO> expiringRoles,
        List<AuthorityUsageDTO> topAuthorities,
        List<PermissionUsageDTO> permissionUsage
    ) {
        this.totalAuthorities = totalAuthorities;
        this.totalPermissions = totalPermissions;
        this.activeUsers = activeUsers;
        this.expiredRoles = expiredRoles;
        this.recentActivity = recentActivity;
        this.expiringRoles = expiringRoles;
        this.topAuthorities = topAuthorities;
        this.permissionUsage = permissionUsage;
    }

    public Long getTotalAuthorities() {
        return totalAuthorities;
    }

    public void setTotalAuthorities(Long totalAuthorities) {
        this.totalAuthorities = totalAuthorities;
    }

    public Long getTotalPermissions() {
        return totalPermissions;
    }

    public void setTotalPermissions(Long totalPermissions) {
        this.totalPermissions = totalPermissions;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getExpiredRoles() {
        return expiredRoles;
    }

    public void setExpiredRoles(Long expiredRoles) {
        this.expiredRoles = expiredRoles;
    }

    public List<RecentActivityDTO> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<RecentActivityDTO> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public List<ExpiringRoleDTO> getExpiringRoles() {
        return expiringRoles;
    }

    public void setExpiringRoles(List<ExpiringRoleDTO> expiringRoles) {
        this.expiringRoles = expiringRoles;
    }

    public List<AuthorityUsageDTO> getTopAuthorities() {
        return topAuthorities;
    }

    public void setTopAuthorities(List<AuthorityUsageDTO> topAuthorities) {
        this.topAuthorities = topAuthorities;
    }

    public List<PermissionUsageDTO> getPermissionUsage() {
        return permissionUsage;
    }

    public void setPermissionUsage(List<PermissionUsageDTO> permissionUsage) {
        this.permissionUsage = permissionUsage;
    }

    @Override
    public String toString() {
        return (
            "DashboardMetricsDTO{" +
            "totalAuthorities=" +
            totalAuthorities +
            ", totalPermissions=" +
            totalPermissions +
            ", activeUsers=" +
            activeUsers +
            ", expiredRoles=" +
            expiredRoles +
            ", recentActivityCount=" +
            (recentActivity != null ? recentActivity.size() : 0) +
            ", expiringRolesCount=" +
            (expiringRoles != null ? expiringRoles.size() : 0) +
            ", topAuthoritiesCount=" +
            (topAuthorities != null ? topAuthorities.size() : 0) +
            ", permissionUsageCount=" +
            (permissionUsage != null ? permissionUsage.size() : 0) +
            '}'
        );
    }
}
