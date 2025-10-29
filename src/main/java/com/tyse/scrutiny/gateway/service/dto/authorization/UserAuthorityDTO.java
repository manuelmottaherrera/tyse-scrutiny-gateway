package com.tyse.scrutiny.gateway.service.dto.authorization;

import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.tyse.scrutiny.gateway.domain.UserAuthority}
 */
public class UserAuthorityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long authorityId;
    private String authorityCode;
    private String authorityName;
    private Boolean isActive;
    private Instant expiresAt;
    private String assignedBy;
    private Instant assignedDate;
    private String revokedBy;
    private Instant revokedDate;
    private String revokedReason;

    // Computed fields
    private Boolean isExpired;
    private Boolean isValid;

    public UserAuthorityDTO() {
        // Empty constructor needed for Jackson
    }

    public UserAuthorityDTO(UserAuthority userAuthority, Authority authority) {
        this.id = userAuthority.getId();
        this.userId = userAuthority.getUserId();
        this.authorityId = userAuthority.getAuthorityId();
        this.isActive = userAuthority.getIsActive();
        this.expiresAt = userAuthority.getExpiresAt();
        this.assignedBy = userAuthority.getAssignedBy();
        this.assignedDate = userAuthority.getAssignedDate();
        this.revokedBy = userAuthority.getRevokedBy();
        this.revokedDate = userAuthority.getRevokedDate();
        this.revokedReason = userAuthority.getRevokedReason();

        // Add authority info if provided
        if (authority != null) {
            this.authorityCode = authority.getCode();
            this.authorityName = authority.getName();
        }

        // Compute fields
        this.isExpired = userAuthority.isExpired();
        this.isValid = userAuthority.isValid();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAuthorityCode() {
        return authorityCode;
    }

    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public Instant getRevokedDate() {
        return revokedDate;
    }

    public void setRevokedDate(Instant revokedDate) {
        this.revokedDate = revokedDate;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAuthorityDTO)) return false;
        UserAuthorityDTO that = (UserAuthorityDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (
            "UserAuthorityDTO{" +
            "id=" +
            id +
            ", userId=" +
            userId +
            ", authorityId=" +
            authorityId +
            ", authorityCode='" +
            authorityCode +
            '\'' +
            ", authorityName='" +
            authorityName +
            '\'' +
            ", isActive=" +
            isActive +
            ", expiresAt=" +
            expiresAt +
            ", assignedBy='" +
            assignedBy +
            '\'' +
            ", assignedDate=" +
            assignedDate +
            ", revokedBy='" +
            revokedBy +
            '\'' +
            ", revokedDate=" +
            revokedDate +
            ", revokedReason='" +
            revokedReason +
            '\'' +
            ", isExpired=" +
            isExpired +
            ", isValid=" +
            isValid +
            '}'
        );
    }
}
