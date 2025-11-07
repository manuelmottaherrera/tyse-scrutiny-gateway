package com.tyse.scrutiny.gateway.service.dto.dashboard;

import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for recent audit activity
 */
public class RecentActivityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long authorityId;
    private String authorityName;
    private AuditAction action;
    private String changedBy;
    private Instant changedDate;
    private String description;

    public RecentActivityDTO() {}

    public RecentActivityDTO(
        Long id,
        Long authorityId,
        String authorityName,
        AuditAction action,
        String changedBy,
        Instant changedDate,
        String description
    ) {
        this.id = id;
        this.authorityId = authorityId;
        this.authorityName = authorityName;
        this.action = action;
        this.changedBy = changedBy;
        this.changedDate = changedDate;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Instant getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(Instant changedDate) {
        this.changedDate = changedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return (
            "RecentActivityDTO{" +
            "id=" +
            id +
            ", authorityId=" +
            authorityId +
            ", authorityName='" +
            authorityName +
            '\'' +
            ", action=" +
            action +
            ", changedBy='" +
            changedBy +
            '\'' +
            ", changedDate=" +
            changedDate +
            ", description='" +
            description +
            '\'' +
            '}'
        );
    }
}
