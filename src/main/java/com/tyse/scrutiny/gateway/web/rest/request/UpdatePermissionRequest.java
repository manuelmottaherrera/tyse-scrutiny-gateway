package com.tyse.scrutiny.gateway.web.rest.request;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Request DTO for updating a permission.
 */
public class UpdatePermissionRequest implements Serializable {

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Boolean isActive;

    public UpdatePermissionRequest() {}

    public UpdatePermissionRequest(String description, Boolean isActive) {
        this.description = description;
        this.isActive = isActive;
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

    @Override
    public String toString() {
        return "UpdatePermissionRequest{" + "description='" + description + '\'' + ", isActive=" + isActive + '}';
    }
}
