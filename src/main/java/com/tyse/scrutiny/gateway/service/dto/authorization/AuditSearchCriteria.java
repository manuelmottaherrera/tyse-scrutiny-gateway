package com.tyse.scrutiny.gateway.service.dto.authorization;

import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Criteria class for searching authority audit logs with filters.
 */
public class AuditSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long authorityId;
    private String changedBy;
    private AuditAction action;
    private Instant fromDate;
    private Instant toDate;

    public AuditSearchCriteria() {
        // Empty constructor needed for Jackson
    }

    public AuditSearchCriteria(Long authorityId, String changedBy, AuditAction action, Instant fromDate, Instant toDate) {
        this.authorityId = authorityId;
        this.changedBy = changedBy;
        this.action = action;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    // Getters and setters

    public Long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    public void setToDate(Instant toDate) {
        this.toDate = toDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditSearchCriteria)) return false;
        AuditSearchCriteria that = (AuditSearchCriteria) o;
        return (
            Objects.equals(authorityId, that.authorityId) &&
            Objects.equals(changedBy, that.changedBy) &&
            action == that.action &&
            Objects.equals(fromDate, that.fromDate) &&
            Objects.equals(toDate, that.toDate)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorityId, changedBy, action, fromDate, toDate);
    }

    @Override
    public String toString() {
        return (
            "AuditSearchCriteria{" +
            "authorityId=" +
            authorityId +
            ", changedBy='" +
            changedBy +
            '\'' +
            ", action=" +
            action +
            ", fromDate=" +
            fromDate +
            ", toDate=" +
            toDate +
            '}'
        );
    }
}
