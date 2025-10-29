package com.tyse.scrutiny.gateway.domain.enumeration;

/**
 * The AuthorityCategory enumeration.
 * Defines the category of an authority/role in the system.
 */
public enum AuthorityCategory {
    /**
     * System-defined roles that cannot be modified or deleted.
     * Examples: ROLE_ADMIN, ROLE_USER
     */
    SYSTEM("System"),

    /**
     * Custom roles defined by administrators.
     * Can be created, modified, and deleted by users with appropriate permissions.
     * Examples: ROLE_MANAGER, ROLE_SUPERVISOR
     */
    CUSTOM("Custom"),

    /**
     * Tenant-specific roles for multi-tenancy support.
     * Isolated per tenant, can be customized independently.
     * Future use: when multi-tenancy is implemented.
     */
    TENANT_SPECIFIC("Tenant Specific");

    private final String displayName;

    AuthorityCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the human-readable display name of the category.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this category represents a system-protected role.
     *
     * @return true if SYSTEM category, false otherwise
     */
    public boolean isSystemProtected() {
        return this == SYSTEM;
    }

    /**
     * Check if this category allows modifications.
     *
     * @return true if the category allows modifications
     */
    public boolean isModifiable() {
        return this != SYSTEM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
