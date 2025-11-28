package budget.constants;

/**
 * Utility class containing limits for various fields in the application.
 */
public final class Limits {

    private Limits() { }

    // Username and Password Length Limits
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 32;
    public static final int MAX_FULL_NAME_LENGTH = 50;
    // Maximum allowed change percentage for budget edits
    public static final double EDIT_CHANGE_LIMIT_PERCENT = 0.25;
    // Maximum allowed change impact for budget balance
    public static final double BALANCE_CHANGE_LIMIT_PERCENT = 0.10;
    // Maximum number of Prime Ministers allowed
    public static final int MAX_PRIME_MINISTERS = 1;
    // Minimum budget item amount
    public static final double MIN_BUDGET_ITEM_AMOUNT = 0.0;
    // Maximum number of pending requests per user
    public static final int MAX_PENDING_REQUESTS_PER_USER = 5;
    public static final double SMALL_NUMBER = 0.01;
    public static final int NUMBER_ONE_HUNDRED = 100;
    public static final int MIN_BUDGET_YEAR = 200;


}
