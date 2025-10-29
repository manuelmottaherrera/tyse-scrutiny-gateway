package com.tyse.scrutiny.gateway.domain.authorization;

import com.tyse.scrutiny.gateway.domain.AbstractAuditingEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Permission.
 * Represents a granular permission in the system following the resource.action pattern.
 * Examples: user.create, user.read, user.update, user.delete, report.export
 */
@Table("scr_permission")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Permission extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    /**
     * Unique name of the permission (typically resource.action format).
     * Example: "user.create", "report.export"
     */
    @NotNull(message = "must not be null")
    @Size(max = 100)
    @Column("name")
    private String name;

    /**
     * The resource this permission applies to.
     * Example: "user", "report", "authority"
     */
    @NotNull(message = "must not be null")
    @Size(max = 50)
    @Column("resource")
    private String resource;

    /**
     * The action allowed on the resource.
     * Example: "create", "read", "update", "delete", "export"
     */
    @NotNull(message = "must not be null")
    @Size(max = 50)
    @Column("action")
    private String action;

    /**
     * Human-readable description of what this permission allows.
     */
    @Size(max = 500)
    @Column("description")
    private String description;

    /**
     * Whether this permission is currently active.
     * Inactive permissions cannot be assigned or checked.
     */
    @NotNull(message = "must not be null")
    @Column("is_active")
    private Boolean isActive = true;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    @Override
    public Long getId() {
        return this.id;
    }

    public Permission id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Permission name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return this.resource;
    }

    public Permission resource(String resource) {
        this.setResource(resource);
        return this;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return this.action;
    }

    public Permission action(String action) {
        this.setAction(action);
        return this;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return this.description;
    }

    public Permission description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public Permission isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Permission)) {
            return false;
        }
        return getId() != null && getId().equals(((Permission) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Permission{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", resource='" + getResource() + "'" +
            ", action='" + getAction() + "'" +
            ", description='" + getDescription() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
