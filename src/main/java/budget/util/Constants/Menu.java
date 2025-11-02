package budget.util.Constants;

import java.util.List;
/**
 * Utility class containing menu options for various user roles
 * and actions within the application.
 */
public final class Menu {

    private Menu() { }

    //Login Menu
    public static final List<String> LOGIN_MENU = List.of(
        "Login Menu:",
        "1. Log In",
        "2. Create New Account",
        "3. Exit Application"
    );
    // Role Selection Menu
    public static final List<String> ROLE_SELECTION_MENU = List.of(
        "Please select your Role:",
        "1. Citizen",
        "2. Government Member",
        "3. Prime Minister"
    );
    // Citizen Main Menu
    public static final List<String> CITIZEN_MAIN_MENU = List.of(
        "Please select an option: ",
        "1. View total budget",
        "2. View change history",
        "3. View statistics",
        "4. Logout."
    );
    // Government Member Main Menu
    public static final List<String> GOVERNMENT_MEMBER_MAIN_MENU = List.of(
        "Please select an option: ",
        "1. View total budget",
        "2. View change history",
        "3. View statistics",
        "4. Manage budget items (only edit)",
        "5. View your request history",
        "6. Logout"
    );
    // Ministry of Finance Member Main Menu
    public static final List<String> FINANCE_MEMBER_MAIN_MENU = List.of(
        "Please select an option: ",
        "1. View total budget",
        "2. View change history",
        "3. View statistics",
        "4. Manage budget items (add, delete, edit)",
        "5. Manage pending change requests",
        "6. View your request history",
        "7. Logout"
    );
    // Prime Minister Main Menu
    public static final List<String> PRIME_MINISTER_MAIN_MENU = List.of(
        "Please select an option: ",
        "1. View total budget",
        "2. View change history",
        "3. View statistics",
        "4. Manage pending change requests",
        "5. Logout"
    );
    // Pending Requests Submenu - Prime Minister
    public static final List<String> PENDING_REQUESTS_SUBMENU = List.of(
        "Please select an option: ",
        "1. Approve request",
        "2. Reject request",
        "3. Return to main menu"
    );
    // Confirmation Submenu for Change Requests
    public static final List<String> CONFIRMATION_SUBMENU = List.of(
        "Are you sure you want to submit the "
        + "request for Prime Minister approval?",
        "1. YES",
        "2. NO"
    );
    // Submenu for managing budget items
    public static final List<String> MANAGE_BUDGET_ITEMS_SUBMENU = List.of(
        "Please select an option: ",
        "1. Add budget item",
        "2. Delete budget item",
        "3. Edit budget item",
        "4. Return to main menu"
    );
    // Password Requirements
    public static final List<String> PASSWORD_REQUIREMENTS = List.of(
        "Password Requirements:",
        "- At least 8 characters and at most 32 characters.",
        "- At least one uppercase letter (A-Z).",
        "- At least one lowercase letter (a-z).",
        "- At least one digit (0-9).",
        "- At least one special character from @#$%^&+=!.",
        "- Only contain allowed characters (letters, digits,"
        + "and specified special characters)"
    );

}
