package budget.util.constants;

public final class Limits {

    private Limits() { }

    // Username and Password Length Limits
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 32;



    // Maximum allowed change percentage for budget edits
    private static final double MAX_CHANGE_PERCENT = 0.10;

    package budget.util.constants;

/**
 * Utility class containing limits and rules for user credentials
 * and budget item edits.
 */
public final class Limits {

    private Limits() { }

    // Username and Password Length Limits
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 32;

    // Maximum allowed change percentage for budget edits
    private static final double MAX_CHANGE_PERCENT = 0.10; 

    /**
     * Enum representing the status of a proposed change to a budget item.
     */
    public enum ChangeStatus {
        NO_CHANGE,              // No change was made
        NEEDS_APPROVAL,         // Change within ±10%, requires approval
        NEEDS_EXTRA_APPROVAL,   // Change exceeds ±10%, requires extra approval
        INVALID                 // Invalid change (would make balance negative)
    }

    /**
     * Checks the status of a proposed change to a budget item's amount.
     *
     * @param oldAmount the current amount of the budget item
     * @param newAmount the proposed new amount
     * @return the ChangeStatus indicating if the change is valid and/or needs approval
     */
    public static ChangeStatus checkChange(double oldAmount, double newAmount) {
        double difference = newAmount - oldAmount; // positive for increase, negative for decrease

        if (oldAmount - newAmount < 0) {
            return ChangeStatus.INVALID;
        }

        if (oldAmount == newAmount) {
            return ChangeStatus.NO_CHANGE;
        }

        // Check if change exceeds ±10%
        double changePercent = Math.abs(difference) / oldAmount;
        if (changePercent > MAX_CHANGE_PERCENT) {
            return ChangeStatus.NEEDS_EXTRA_APPROVAL;
        }

        // Change within ±10%
        return ChangeStatus.NEEDS_APPROVAL;
    }
}


}
