package com.tyse.scrutiny.gateway.domain.enumeration;

/**
 * The AuditAction enumeration.
 * Defines the type of action performed on an authority/role for audit logging.
 */
public enum AuditAction {
    /**
     * Authority was created.
     */
    CREATED("Created"),

    /**
     * Authority was updated (name, description, or other fields changed).
     */
    UPDATED("Updated"),

    /**
     * Authority was deleted (hard delete or marked for deletion).
     */
    DELETED("Deleted"),

    /**
     * Authority was activated (is_active set to true).
     */
    ACTIVATED("Activated"),

    /**
     * Authority was deactivated (is_active set to false).
     * Soft delete alternative - role exists but cannot be assigned.
     */
    DEACTIVATED("Deactivated"),

    /**
     * Permissions were added to the authority.
     */
    PERMISSIONS_ADDED("Permissions Added"),

    /**
     * Permissions were removed from the authority.
     */
    PERMISSIONS_REMOVED("Permissions Removed"),

    /**
     * Authority hierarchy level was changed.
     */
    HIERARCHY_CHANGED("Hierarchy Changed");

    private final String displayName;

    AuditAction(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the human-readable display name of the action.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this action represents a destructive operation.
     *
     * @return true if DELETED or DEACTIVATED
     */
    public boolean isDestructive() {
        return this == DELETED || this == DEACTIVATED;
    }

    /**
     * Check if this action represents a creation operation.
     *
     * @return true if CREATED or ACTIVATED
     */
    public boolean isCreation() {
        return this == CREATED || this == ACTIVATED;
    }

    /**
     * Check if this action represents a modification operation.
     *
     * @return true if UPDATED, PERMISSIONS_ADDED, PERMISSIONS_REMOVED, or HIERARCHY_CHANGED
     */
    public boolean isModification() {
        return this == UPDATED || this == PERMISSIONS_ADDED || this == PERMISSIONS_REMOVED || this == HIERARCHY_CHANGED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
