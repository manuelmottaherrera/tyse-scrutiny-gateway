package com.tyse.scrutiny.gateway.service.dto.authorization;

import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.tyse.scrutiny.gateway.domain.Authority}
 */
public class AuthorityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String code;
    private String description;
    private AuthorityCategory category;
    private Boolean isSystem;
    private Boolean isActive;
    private Integer hierarchyLevel;

    // Audit fields
    private String createdBy;
    private Instant createdDate;
    private String lastModifiedBy;
    private Instant lastModifiedDate;

    public AuthorityDTO() {
        // Empty constructor needed for Jackson
    }

    public AuthorityDTO(Authority authority) {
        this.id = authority.getId();
        this.name = authority.getName();
        this.code = authority.getCode();
        this.description = authority.getDescription();
        this.category = authority.getCategory();
        this.isSystem = authority.getIsSystem();
        this.isActive = authority.getIsActive();
        this.hierarchyLevel = authority.getHierarchyLevel();
        this.createdBy = authority.getCreatedBy();
        this.createdDate = authority.getCreatedDate();
        this.lastModifiedBy = authority.getLastModifiedBy();
        this.lastModifiedDate = authority.getLastModifiedDate();
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AuthorityCategory getCategory() {
        return category;
    }

    public void setCategory(AuthorityCategory category) {
        this.category = category;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
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

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorityDTO)) return false;
        AuthorityDTO that = (AuthorityDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return (
            "AuthorityDTO{" +
            "id=" +
            id +
            ", name='" +
            name +
            '\'' +
            ", code='" +
            code +
            '\'' +
            ", description='" +
            description +
            '\'' +
            ", category=" +
            category +
            ", isSystem=" +
            isSystem +
            ", isActive=" +
            isActive +
            ", hierarchyLevel=" +
            hierarchyLevel +
            ", createdBy='" +
            createdBy +
            '\'' +
            ", createdDate=" +
            createdDate +
            ", lastModifiedBy='" +
            lastModifiedBy +
            '\'' +
            ", lastModifiedDate=" +
            lastModifiedDate +
            '}'
        );
    }
}
