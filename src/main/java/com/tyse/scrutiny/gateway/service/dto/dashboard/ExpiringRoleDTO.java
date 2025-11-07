package com.tyse.scrutiny.gateway.service.dto.dashboard;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for roles that are about to expire
 */
public class ExpiringRoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String userName;
    private String userLogin;
    private Long authorityId;
    private String authorityName;
    private Instant expiresAt;
    private Long daysUntilExpiration;

    public ExpiringRoleDTO() {}

    public ExpiringRoleDTO(
        Long id,
        Long userId,
        String userName,
        String userLogin,
        Long authorityId,
        String authorityName,
        Instant expiresAt,
        Long daysUntilExpiration
    ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userLogin = userLogin;
        this.authorityId = authorityId;
        this.authorityName = authorityName;
        this.expiresAt = expiresAt;
        this.daysUntilExpiration = daysUntilExpiration;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getDaysUntilExpiration() {
        return daysUntilExpiration;
    }

    public void setDaysUntilExpiration(Long daysUntilExpiration) {
        this.daysUntilExpiration = daysUntilExpiration;
    }

    @Override
    public String toString() {
        return (
            "ExpiringRoleDTO{" +
            "id=" +
            id +
            ", userId=" +
            userId +
            ", userName='" +
            userName +
            '\'' +
            ", userLogin='" +
            userLogin +
            '\'' +
            ", authorityId=" +
            authorityId +
            ", authorityName='" +
            authorityName +
            '\'' +
            ", expiresAt=" +
            expiresAt +
            ", daysUntilExpiration=" +
            daysUntilExpiration +
            '}'
        );
    }
}
