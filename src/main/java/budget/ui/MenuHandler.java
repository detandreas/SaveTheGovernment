package budget.ui;

import budget.model.domain.user.User;
import budget.model.domain.user.GovernmentMember;
import budget.model.enums.UserRole;
import budget.model.enums.Ministry;
import budget.service.UserAuthenticationService;
import budget.constants.Message;
import budget.constants.Menu;
import java.util.List;
import java.util.Scanner;

/**
 * Displays role-specific menus and handles simple menu navigation.
 */
public class MenuHandler {
    private final Scanner scanner;
    private final UserAuthenticationService authService;

    /**
     * Constructs a new MenuHandler.
     *
     * @param scanner the scanner for reading user input
     * @param authService the authentication service for user operations
     */
    public MenuHandler(Scanner scanner, UserAuthenticationService authService) {
        this.scanner = scanner;
        this.authService = authService;
    }

    /**
     * Show menu for the given user until they choose to logout.
     * This is a lightweight UI handler: unimplemented options will print a placeholder message.
     *
     * @param user the logged-in user
     */
    public void showMenu(User user) {
        boolean running = true;
        while (running) {
            displayMenuForUser(user);
            String choice = getUserChoice();
            running = handleUserChoice(user, choice);
        }
        System.out.println(Message.LOGOUT_SUCCESS);
    }

    /**
     * Displays the menu options appropriate for the user's role.
     *
     * @param user the user whose menu should be displayed
     */
    private void displayMenuForUser(User user) {
        List<String> menuLines = menuForRole(user);
        for (String line : menuLines) {
            System.out.println(line);
        }
    }

    /**
     * Gets the user's menu choice from standard input.
     *
     * @return the user's choice as a trimmed string
     */
    private String getUserChoice() {
        System.out.print(Message.SELECT_CHOICE_MESSAGE + " ");
        return scanner.nextLine().trim();
    }

    /**
     * Handles the user's menu choice based on their role.
     *
     * @param role the user's role
     * @param choice the user's menu choice
     * @return true to continue showing the menu, false to logout
     */
    private boolean handleUserChoice(User user, String choice) {
        UserRole role = user.getUserRole();
        switch (role) {
            case CITIZEN:
                return handleCitizenChoice(choice);
            case GOVERNMENT_MEMBER:
                if (user instanceof GovernmentMember gm) {
                    if (gm.getMinistry() == Ministry.FINANCE) {
                        return handleFinanceGovernmentMemberChoice(choice);
                    }
                }
                return handleGovernmentMemberChoice(choice);
            case PRIME_MINISTER:
                return handlePrimeMinisterChoice(choice);
            default:
                throw new AssertionError(Message.ERROR_INVALID_ROLE);
        }
    }


    /**
     * Returns the appropriate menu options for the given role.
     *
     * @param role the user role
     * @return list of menu options as strings
     */
    private List<String> menuForRole(User user) {
        UserRole role = user.getUserRole();
        switch (role) {
            case CITIZEN:
                return Menu.CITIZEN_MAIN_MENU;
            case GOVERNMENT_MEMBER:
                if (user instanceof GovernmentMember gm) {
                    if (gm.getMinistry() == Ministry.FINANCE) {
                        return Menu.FINANCE_MEMBER_MAIN_MENU;
                    }
                }
                return Menu.GOVERNMENT_MEMBER_MAIN_MENU;
            case PRIME_MINISTER:
                return Menu.PRIME_MINISTER_MAIN_MENU;
            default:
                throw new AssertionError(Message.ERROR_INVALID_ROLE);
        }
    }

    /**
     * Handles menu choices for citizens.
     *
     * @param choice the user's menu choice
     * @return true to continue showing menu, false to logout
     */
    private boolean handleCitizenChoice(String choice) {
        switch (choice) {
            case "1":
                citizenAction1();
                return true;
            case "2":
                citizenAction2();
                return true;
            case "3":
                citizenAction3();
                return true;
            case "4":
                return false; // logout
            default:
                System.out.println(Message.INVALID_OPTION);
                return true;
        }
    }

    /**
     * Handles menu choices for government members.
     *
     * @param choice the user's menu choice
     * @return true to continue showing menu, false to logout
     */
    private boolean handleGovernmentMemberChoice(String choice) {
        switch (choice) {
            case "1":
                governmentAction1();
                return true;
            case "2":
                governmentAction2();
                return true;
            case "3":
                governmentAction3();
                return true;
            case "4":
                governmentAction4();
                return true;
            case "5":
                governmentAction5();
                return true;
            case "6":
                return false; // logout
            default:
                System.out.println(Message.INVALID_OPTION);
                return true;
        }
    }
    /**
     * Handles menu choices for finance ministry members.
     *
     * @param choice the user's menu choice
     * @return true to continue showing menu, false to logout
     */
    private boolean handleFinanceGovernmentMemberChoice(String choice) {
        switch (choice) {
            case "1":
                financeAction1();
                return true;
            case "2":
                financeAction2();
                return true;
            case "3":
                financeAction3();
                return true;
            case "4":
                financeAction4();
                return true;
            case "5":
                financeAction5();
                return true;
            case "6":
                financeAction6();
                return true;
            case "7":
                return false; // logout
            default:
                System.out.println(Message.INVALID_OPTION);
                return true;
        }
    }

    /**
     * Handles menu choices for the prime minister.
     *
     * @param choice the user's menu choice
     * @return true to continue showing menu, false to logout
     */
    private boolean handlePrimeMinisterChoice(String choice) {
        switch (choice) {
            case "1":
                primeMinisterAction1();
                return true;
            case "2":
                primeMinisterAction2();
                return true;
            case "3":
                primeMinisterAction3();
                return true;
            case "4":
                primeMinisterAction4();
                return true;
            case "5":
                return false; // logout
            default:
                System.out.println(Message.INVALID_OPTION);
                return true;
        }
    }
}
