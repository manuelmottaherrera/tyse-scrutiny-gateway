package com.tyse.scrutiny.gateway.service.dto.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.AuthorityPermission;
import java.time.Instant;

/**
 * A DTO for the {@link AuthorityPermission} entity.
 */
public class AuthorityPermissionDTO {

    private Long id;
    private Long authorityId;
    private Long permissionId;
    private String grantedBy;
    private Instant grantedDate;

    public AuthorityPermissionDTO() {}

    public AuthorityPermissionDTO(AuthorityPermission authorityPermission) {
        this.id = authorityPermission.getId();
        this.authorityId = authorityPermission.getAuthorityId();
        this.permissionId = authorityPermission.getPermissionId();
        this.grantedBy = authorityPermission.getGrantedBy();
        this.grantedDate = authorityPermission.getGrantedDate();
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

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    public Instant getGrantedDate() {
        return grantedDate;
    }

    public void setGrantedDate(Instant grantedDate) {
        this.grantedDate = grantedDate;
    }

    @Override
    public String toString() {
        return (
            "AuthorityPermissionDTO{" +
            "id=" +
            id +
            ", authorityId=" +
            authorityId +
            ", permissionId=" +
            permissionId +
            ", grantedBy='" +
            grantedBy +
            '\'' +
            ", grantedDate=" +
            grantedDate +
            '}'
        );
    }
}
