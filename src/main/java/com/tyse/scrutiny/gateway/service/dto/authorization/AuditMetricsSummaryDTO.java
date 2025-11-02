package com.tyse.scrutiny.gateway.service.dto.authorization;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DTO for audit metrics summary.
 * Contains aggregated statistics about audit logs.
 */
public class AuditMetricsSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalAudits;
    private Map<String, Long> auditsByAction;
    private List<UserActivityDTO> topUsers;
    private ActivityPeriodDTO recentActivity;

    public AuditMetricsSummaryDTO() {
        // Empty constructor needed for Jackson
    }

    public AuditMetricsSummaryDTO(
        Long totalAudits,
        Map<String, Long> auditsByAction,
        List<UserActivityDTO> topUsers,
        ActivityPeriodDTO recentActivity
    ) {
        this.totalAudits = totalAudits;
        this.auditsByAction = auditsByAction;
        this.topUsers = topUsers;
        this.recentActivity = recentActivity;
    }

    // Getters and setters

    public Long getTotalAudits() {
        return totalAudits;
    }

    public void setTotalAudits(Long totalAudits) {
        this.totalAudits = totalAudits;
    }

    public Map<String, Long> getAuditsByAction() {
        return auditsByAction;
    }

    public void setAuditsByAction(Map<String, Long> auditsByAction) {
        this.auditsByAction = auditsByAction;
    }

    public List<UserActivityDTO> getTopUsers() {
        return topUsers;
    }

    public void setTopUsers(List<UserActivityDTO> topUsers) {
        this.topUsers = topUsers;
    }

    public ActivityPeriodDTO getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(ActivityPeriodDTO recentActivity) {
        this.recentActivity = recentActivity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditMetricsSummaryDTO)) return false;
        AuditMetricsSummaryDTO that = (AuditMetricsSummaryDTO) o;
        return (
            Objects.equals(totalAudits, that.totalAudits) &&
            Objects.equals(auditsByAction, that.auditsByAction) &&
            Objects.equals(topUsers, that.topUsers) &&
            Objects.equals(recentActivity, that.recentActivity)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalAudits, auditsByAction, topUsers, recentActivity);
    }

    @Override
    public String toString() {
        return (
            "AuditMetricsSummaryDTO{" +
            "totalAudits=" +
            totalAudits +
            ", auditsByAction=" +
            auditsByAction +
            ", topUsers=" +
            topUsers +
            ", recentActivity=" +
            recentActivity +
            '}'
        );
    }
}
