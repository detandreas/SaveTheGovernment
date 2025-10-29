package budget.model;

public enum Ministry {
    HEALTH("Health"),
    EDUCATION("Education"),
    DEFENSE("Defense"),
    FINANCE("Finance"),
    INFRASTRUCTURE("Infrastructure"),
    FOREIGN_AFFAIRS("Foreign Affairs"),
    INTERIOR("Interior"),
    DEVELOPMENT("Development"),
    LABOUR("Labour"),
    JUSTICE("Justice"),
    AGRICULTURE("Agriculture");

    private final String displayName;
    /**
     * Constructor for Ministry enum.
     * @param displayName readable name of ministry
     */
    Ministry(String displayName) {
        this.displayName = displayName;
    }
    /**
     * Gets the display name for this ministry.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    /**
     * Returns the display name as a String representation.
     * @return the display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
