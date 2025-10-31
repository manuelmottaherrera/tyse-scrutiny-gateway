package com.tyse.scrutiny.gateway.domain.authorization;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A AuthorityPermission (Authority-Permission Junction Table).
 * Represents the many-to-many relationship between authorities (roles) and permissions.
 *
 * <p>This entity maps which permissions are granted to which authority/role.
 * For example:
 * - ROLE_ADMIN has permissions: user.create, user.update, user.delete, ...
 * - ROLE_USER has permissions: user.read, own-profile.update
 *
 * <p>This is the core of role-based permission inheritance. When a user is assigned
 * a role (via UserAuthority), they automatically inherit all permissions assigned
 * to that role through this table.
 *
 * <h3>Permission Inheritance Flow:</h3>
 * <pre>
 * User → UserAuthority → Authority → AuthorityPermission → Permission
 * (john) → (ROLE_ADMIN) → (admin role) → (mapping) → (user.create)
 * </pre>
 *
 * <h3>Example Scenario:</h3>
 * <pre>
 * // Grant "user.create" permission to ROLE_ADMIN
 * AuthorityPermission mapping = new AuthorityPermission();
 * mapping.setAuthorityId(adminRoleId);  // ROLE_ADMIN
 * mapping.setPermissionId(userCreatePermissionId);  // user.create
 * mapping.setGrantedBy("system");
 * save(mapping);
 *
 * // Now all users with ROLE_ADMIN can create users
 * </pre>
 */
@Table("scr_authority_permission")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuthorityPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    /**
     * Foreign key to the authority (scr_authority.id).
     * References the role that is being granted the permission.
     */
    @NotNull(message = "must not be null")
    @Column("authority_id")
    private Long authorityId;

    /**
     * Foreign key to the permission (scr_permission.id).
     * References the permission being assigned to the role.
     */
    @NotNull(message = "must not be null")
    @Column("permission_id")
    private Long permissionId;

    /**
     * User ID or username of who granted this permission to the authority.
     * Typically "system" for initial setup, or an admin user login for runtime assignments.
     */
    @NotNull(message = "must not be null")
    @Size(max = 50)
    @Column("granted_by")
    private String grantedBy;

    /**
     * Date and time when this permission was granted to the authority.
     * Defaults to current timestamp when the record is created.
     */
    @NotNull(message = "must not be null")
    @Column("granted_date")
    private Instant grantedDate = Instant.now();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AuthorityPermission id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorityId() {
        return this.authorityId;
    }

    public AuthorityPermission authorityId(Long authorityId) {
        this.setAuthorityId(authorityId);
        return this;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public Long getPermissionId() {
        return this.permissionId;
    }

    public AuthorityPermission permissionId(Long permissionId) {
        this.setPermissionId(permissionId);
        return this;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getGrantedBy() {
        return this.grantedBy;
    }

    public AuthorityPermission grantedBy(String grantedBy) {
        this.setGrantedBy(grantedBy);
        return this;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    public Instant getGrantedDate() {
        return this.grantedDate;
    }

    public AuthorityPermission grantedDate(Instant grantedDate) {
        this.setGrantedDate(grantedDate);
        return this;
    }

    public void setGrantedDate(Instant grantedDate) {
        this.grantedDate = grantedDate;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorityPermission)) {
            return false;
        }
        return getId() != null && getId().equals(((AuthorityPermission) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuthorityPermission{" +
            "id=" + getId() +
            ", authorityId=" + getAuthorityId() +
            ", permissionId=" + getPermissionId() +
            ", grantedBy='" + getGrantedBy() + "'" +
            ", grantedDate='" + getGrantedDate() + "'" +
            "}";
    }
}
