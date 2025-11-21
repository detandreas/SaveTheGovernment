package budget.model.enums;

public enum Status {
    PENDING("Change approval is still pending"),
    APPROVED("Change is approved"),
    REJECTED("Change is rejected");

    private final String description;
    /**
     * Unaccesible constructor for Status enum.
     * @param description Status description
     */
    Status(String description) {
        this.description = description;
    }
    /**
     * Return description of a status.
     * @return a string representing the
     * description of a status
     */
    public String getDescription() {
        return description;
    }
    /**
     * String representation of the a status.
     */
    @Override
    public String toString() {
        return description;
    }
}
