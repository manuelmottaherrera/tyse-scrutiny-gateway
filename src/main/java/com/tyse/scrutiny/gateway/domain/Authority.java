package com.tyse.scrutiny.gateway.domain;

import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * An Authority (Role).
 * Represents a role in the enterprise authorization system with full audit trail.
 * Migrated from legacy JHipster String-based authority to Long ID with rich metadata.
 */
@Table("scr_authority")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Authority extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    /**
     * Human-readable name of the authority/role.
     * Example: "Administrator", "User", "Manager"
     */
    @NotNull(message = "must not be null")
    @Size(max = 100)
    @Column("name")
    private String name;

    /**
     * Unique code/identifier of the authority.
     * Typically in ROLE_* format for Spring Security compatibility.
     * Example: "ROLE_ADMIN", "ROLE_USER", "ROLE_MANAGER"
     * This is the equivalent of the legacy 'name' field that was used as ID.
     */
    @NotNull(message = "must not be null")
    @Size(max = 50)
    @Column("code")
    private String code;

    /**
     * Detailed description of what this authority/role represents.
     */
    @Size(max = 500)
    @Column("description")
    private String description;

    /**
     * Category of the authority (SYSTEM, CUSTOM, TENANT_SPECIFIC).
     * System authorities cannot be modified or deleted.
     */
    @NotNull(message = "must not be null")
    @Column("category")
    private AuthorityCategory category = AuthorityCategory.CUSTOM;

    /**
     * Whether this authority is a system-defined role that cannot be modified.
     * System roles: ROLE_ADMIN, ROLE_USER
     */
    @NotNull(message = "must not be null")
    @Column("is_system")
    private Boolean isSystem = false;

    /**
     * Whether this authority is currently active.
     * Inactive authorities cannot be assigned to users.
     */
    @NotNull(message = "must not be null")
    @Column("is_active")
    private Boolean isActive = true;

    /**
     * Hierarchy level of the authority.
     * Lower numbers = higher privilege (0 = highest, e.g., ADMIN).
     * Used for role comparison and inheritance.
     */
    @NotNull(message = "must not be null")
    @Column("hierarchy_level")
    private Integer hierarchyLevel = 1000;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    @Override
    public Long getId() {
        return this.id;
    }

    public Authority id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Authority name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public Authority code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return this.description;
    }

    public Authority description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AuthorityCategory getCategory() {
        return this.category;
    }

    public Authority category(AuthorityCategory category) {
        this.setCategory(category);
        return this;
    }

    public void setCategory(AuthorityCategory category) {
        this.category = category;
    }

    public Boolean getIsSystem() {
        return this.isSystem;
    }

    public Authority isSystem(Boolean isSystem) {
        this.setIsSystem(isSystem);
        return this;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public Authority isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getHierarchyLevel() {
        return this.hierarchyLevel;
    }

    public Authority hierarchyLevel(Integer hierarchyLevel) {
        this.setHierarchyLevel(hierarchyLevel);
        return this;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Authority)) {
            return false;
        }
        return getId() != null && getId().equals(((Authority) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Authority{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", code='" + getCode() + "'" +
            ", description='" + getDescription() + "'" +
            ", category='" + getCategory() + "'" +
            ", isSystem='" + getIsSystem() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", hierarchyLevel=" + getHierarchyLevel() +
            ", createdBy='" + getCreatedBy() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
