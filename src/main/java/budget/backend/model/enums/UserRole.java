package budget.backend.model.enums;

public enum UserRole {
    CITIZEN("Citizen", "Regular citizen with view-only access"),
    GOVERNMENT_MEMBER("Government Member",
    "Government official who can propose changes"),
    PRIME_MINISTER("Prime Minister",
    "Highest Authority accepts/declines proposed changes");

    private final String displayName;
    private final String description;
    /**
     * Constructor for UserRole enum.
     * @param displayName readable name of the role
     * @param description readable description of the role
     */
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    /**
     * Gets the display name for this role.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    /**
     * Gets the description for the role.
     * @return the role description
     */
    public String getDescription() {
        return description;
    }
    /**
     * Returns the display name as a String representation.
     * @return the display name
     */
    @Override
    public String toString() {
        return  displayName;
    }
}
