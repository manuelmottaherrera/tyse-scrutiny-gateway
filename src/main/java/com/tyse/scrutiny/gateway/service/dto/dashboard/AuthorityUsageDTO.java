package com.tyse.scrutiny.gateway.service.dto.dashboard;

import java.io.Serializable;

/**
 * DTO for authority usage statistics
 */
public class AuthorityUsageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long authorityId;
    private String authorityName;
    private String authorityCode;
    private Long userCount;
    private Long activeCount;
    private Long expiredCount;

    public AuthorityUsageDTO() {}

    public AuthorityUsageDTO(Long authorityId, String authorityName, String authorityCode, Long userCount) {
        this.authorityId = authorityId;
        this.authorityName = authorityName;
        this.authorityCode = authorityCode;
        this.userCount = userCount;
    }

    public AuthorityUsageDTO(
        Long authorityId,
        String authorityName,
        String authorityCode,
        Long userCount,
        Long activeCount,
        Long expiredCount
    ) {
        this.authorityId = authorityId;
        this.authorityName = authorityName;
        this.authorityCode = authorityCode;
        this.userCount = userCount;
        this.activeCount = activeCount;
        this.expiredCount = expiredCount;
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

    public String getAuthorityCode() {
        return authorityCode;
    }

    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Long activeCount) {
        this.activeCount = activeCount;
    }

    public Long getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(Long expiredCount) {
        this.expiredCount = expiredCount;
    }

    @Override
    public String toString() {
        return (
            "AuthorityUsageDTO{" +
            "authorityId=" +
            authorityId +
            ", authorityName='" +
            authorityName +
            '\'' +
            ", authorityCode='" +
            authorityCode +
            '\'' +
            ", userCount=" +
            userCount +
            ", activeCount=" +
            activeCount +
            ", expiredCount=" +
            expiredCount +
            '}'
        );
    }
}
