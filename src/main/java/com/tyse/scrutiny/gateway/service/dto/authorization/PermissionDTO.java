package com.tyse.scrutiny.gateway.service.dto.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.tyse.scrutiny.gateway.domain.Permission}
 */
public class PermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String resource;
    private String action;
    private String description;
    private Boolean isActive;

    // Audit fields
    private String createdBy;
    private Instant createdDate;

    public PermissionDTO() {
        // Empty constructor needed for Jackson
    }

    public PermissionDTO(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
        this.resource = permission.getResource();
        this.action = permission.getAction();
        this.description = permission.getDescription();
        this.isActive = permission.getIsActive();
        this.createdBy = permission.getCreatedBy();
        this.createdDate = permission.getCreatedDate();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionDTO)) return false;
        PermissionDTO that = (PermissionDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return (
            "PermissionDTO{" +
            "id=" +
            id +
            ", name='" +
            name +
            '\'' +
            ", resource='" +
            resource +
            '\'' +
            ", action='" +
            action +
            '\'' +
            ", description='" +
            description +
            '\'' +
            ", isActive=" +
            isActive +
            ", createdBy='" +
            createdBy +
            '\'' +
            ", createdDate=" +
            createdDate +
            '}'
        );
    }
}
