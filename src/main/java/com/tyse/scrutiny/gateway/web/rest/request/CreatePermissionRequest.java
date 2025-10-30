package com.tyse.scrutiny.gateway.web.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Request DTO for creating a new permission.
 */
public class CreatePermissionRequest implements Serializable {

    @NotBlank(message = "Resource is required")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "Resource must be lowercase alphanumeric + underscore")
    @Size(max = 100, message = "Resource cannot exceed 100 characters")
    private String resource;

    @NotBlank(message = "Action is required")
    @Pattern(
        regexp = "^(create|read|update|delete|execute|manage|list)$",
        message = "Action must be one of: create, read, update, delete, execute, manage, list"
    )
    private String action;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public CreatePermissionRequest() {}

    public CreatePermissionRequest(String resource, String action, String description) {
        this.resource = resource;
        this.action = action;
        this.description = description;
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

    @Override
    public String toString() {
        return (
            "CreatePermissionRequest{" +
            "resource='" +
            resource +
            '\'' +
            ", action='" +
            action +
            '\'' +
            ", description='" +
            description +
            '\'' +
            '}'
        );
    }
}
