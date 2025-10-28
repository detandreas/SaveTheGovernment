package budget.util.constants;

public final class Message {

    private Message() { }

    // Welcome Message
    public static final String WELCOME_MESSAGE = "Welcome"
    + " to the Government Budget application!";

    // Login Messages
    public static final String INVALID_OPTION = "Invalid"
    + " option. Please try again.";

    public static final String LOGIN_MESSAGE = "Please"
    + " enter your username and your password.";

    public static final String USERNAME_PROMPT = "Username: ";

    public static final String PASSWORD_PROMPT = "Password: ";

    public static final String LOGIN_SUCCESS = "Successful login! Welcome, %s ";

    public static final String LOGIN_FAILED = "Login failed."
    + " Invalid username or password.";

    // Create Account Messages
    public static final String CREATE_ACCOUNT_MESSAGE =
    "Please fill in your details to create an account.";

    public static final String SIGNUP_ENTER_USERNAME = "Enter a username:";

    public static final String SIGNUP_ENTER_PASSWORD = "Enter a password:";

    public static final String SIGNUP_CONFIRM_PASSWORD =
    "Confirm your password:";

    public static final String SIGNUP_ENTER_FULLNAME =
    "Enter your full name:";

    public static final String SIGNUP_SELECT_ROLE =
    "Select your role (Citizen, Government Member, Prime Minister):";

    // Error Messages for Create Account
    public static final String ERROR_USERNAME_TAKEN =
    "Username is already taken. Please choose another.";

    public static final String ERROR_PASSWORD_MISMATCH =
    "Passwords do not match. Please try again.";

    public static final String ERROR_INVALID_INPUT =
    "Invalid input. Make sure all fields are filled correctly.";

    public static final String ERROR_EMPTY_FIELD =
    "This field cannot be empty.";

    public static final String ERROR_INVALID_ROLE =
    "Invalid role selected. Please choose from the list.";

    // Success Message for Create Account
    public static final String CREATE_ACCOUNT_SUCCESS =
    "Account created successfully! You can now log in using your credentials.";

    // Logout Message
    public static final String LOGOUT_SUCCESS =
    "You have been logged out successfully.";

    // Return to Main Menu Message
    public static final String RETURNING_TO_MAIN_MENU =
    "Returning to main menu...";

    // Exit Application Message
    public static final String EXIT_MESSAGE =
    "Exiting application. Goodbye!";

    // Change Request Messages - Government Member
    public static final String SELECT_ITEM_MESSAGE =
    "Choose the Budget Item to modify.";

    public static final String NEW_AMOUNT = "Enter new amount for %s :";

    public static final String CHANGE_REQUEST_SUBMITTED =
    "Change request submitted successfully for Prime Minister approval.";

    public static final String CHANGE_REQUEST_APPROVED =
    "Change request approved successfully.";

    public static final String CHANGE_REQUEST_REJECTED =
    "Change request has been rejected.";

    // No Pending Requests Message
    public static final String NO_PENDING_REQUESTS =
    "There are no pending change requests.";

    // Budget Item Deletion Not Allowed Message
    public static final String DELETE_NOT_ALLOWED_MESSAGE =
    "Deletion of this budget item is prohibited by administrative protocols.";

    // Duplicate Budget Item Error Message
    public static final String DUPLICATE_BUDGET_ITEM_ERROR =
    "Duplicate budget items with the same ID or title are not allowed. "
    + "Please use a unique ID and title.";

    // Non-Negative Amount Error Message
    public static final String NON_NEGATIVE_AMOUNT_ERROR =
    "Invalid amount. Please enter a non-negative amount.";

}
