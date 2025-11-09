package budget.ui;

import budget.model.domain.user.User;
import budget.model.domain.user.Citizen;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.enums.UserRole;
import budget.service.UserAuthenticationService;
import budget.service.InputValidationService;
import budget.util.InputValidator;
import budget.constants.Message;
import budget.constants.Menu;
import budget.model.enums.Ministry;
import java.util.Scanner;
/**
 * Handles the login process and user interactions
 * related to logging in and account creation.
 */
public class LoginHandler {
    
    private final UserAuthenticationService authService;
    private final InputValidationService validationService;
    private final Scanner scanner;
    /**
     * Constructor for LoginHandler.
     * @param authService the authentication service
     */
    public LoginHandler(UserAuthenticationService authService, 
    InputValidationService validationService) {
        
        this.authService = authService;
        this.validationService = validationService;
        this.scanner = new Scanner(System.in);
    }
    /**
     * Displays the login screen and handles user input
     * for login or account creation.
     * @return the logged-in User object
     */
    public User showLoginScreen() {
        System.out.println(Message.WELCOME_MESSAGE);
        System.out.println(Menu.LOGIN_MENU);
        System.out.print(Message.SELECT_CHOICE_MESSAGE + " ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                return handleLogin();
            case "2":
                return handleAccountCreation();
            case "3":
                System.out.println(Message.EXIT_MESSAGE);
                System.exit(0);
            default:
                System.out.println(Message.INVALID_OPTION);
                return showLoginScreen();
            }
        }
    }
    /**
     * Handles the login process.
     * @return the logged-in User object
     */
    private User handleLogin() {
        System.out.println(Message.LOGIN_MESSAGE);
        System.out.print(Message.USERNAME_PROMPT);
        String username = scanner.nextLine().trim();
        System.out.print(Message.PASSWORD_PROMPT);
        String password = scanner.nextLine().trim();
        User user = authService.login(username, password);
        if (user != null) {
            System.out.printf(Message.LOGIN_SUCCESS, user.getFullName());
            return user;
        } else {
            System.out.println(Message.LOGIN_FAILED);
            return showLoginScreen();
        }
    }
    /**
     * Handles the account creation process.
     * @return the newly created User object
     */
    private User handleAccountCreation() {
        UserRole role = selectUserRole();
        UserRegistrationData data = collectUserData(role);
        
         if (role == UserRole.PRIME_MINISTER && authService.primeMinisterExists()) {
            System.out.println(Message.PRIME_MINISTER_EXISTS);
            return handleAccountCreation();
         }

         User newUser;
         switch (role) {
             case PRIME_MINISTER:
                 newUser = PrimeMinister.getInstance(data.username, data.fullName, data.password);
                 break;
             case GOVERNMENT_MEMBER:
                 newUser = new GovernmentMember(data.username, data.fullName, data.password, data.ministry);
                 break;
             case CITIZEN:
                 newUser = new Citizen(data.username, data.fullName, data.password);
             default:
                throw new IllegalStateException(Message.ERROR_INVALID_ROLE);
         }
         validationService.validateNewUser(newUser);
         System.out.println(Message.CREATE_ACCOUNT_SUCCESS);
         return newUser;
     }
    /**
     * Collects user data for account creation.
     * @return UserRegistrationData object containing user details
     */
    private UserRegistrationData collectUserData(UserRole role) {
        while (true) {
            String username = promptUsername();
            if (!InputValidator.isUserName(username)) {
                System.out.println(Message.USERNAME_LENGTH_LIMITS_MESSAGE);
                continue;
            }
            if (authService.isUsernameTaken(username)) {
                System.out.println(Message.ERROR_USERNAME_TAKEN);
                continue;
            }

            String[] passwords = promptPasswords();
            if (!InputValidator.isPasswordStrong(passwords[0])) {
                System.out.println(Menu.PASSWORD_REQUIREMENTS);
                continue;
            }
            if (!passwords[0].equals(passwords[1])) {
                System.out.println(Message.ERROR_PASSWORD_MISMATCH);
                continue;
            }

            String fullName = promptFullName();
            if (!InputValidator.isFullName(fullName)) {
                System.out.println(Message.FULLNAME_LENGTH_LIMITS_MESSAGE);
                continue;
            }

            String ministry = null;
            if (role == UserRole.GOVERNMENT_MEMBER) {
                System.out.print(Message.SIGNUP_SELECT_DEPARTMENT + " ");
                ministry = scanner.nextLine().trim();
                if (ministry.isEmpty()) {
                    System.out.println(Message.ERROR_EMPTY_FIELD);
                    continue;
                }
                if (!isValidMinistry(ministry)) {
                    System.out.println(Message.ERROR_INVALID_MINISTRY);
                    continue;
                }
                // normalize to enum name (store consistent value)
                ministry = normalizeMinistry(ministry);
            }

            return new UserRegistrationData(username, passwords[0], fullName, ministry);
        }
    }
    /**
     * Prompts the user for a username.
     * @return the entered username
     */
    private String promptUsername() {
        System.out.print(Message.SIGNUP_ENTER_USERNAME + " ");
        return scanner.nextLine().trim();
    }
    /**
     * Prompts the user for a password and confirmation.
     * @return an array containing the password and confirmation
     */
    private String[] promptPasswords() {
        System.out.print(Message.SIGNUP_ENTER_PASSWORD + " ");
        String password = scanner.nextLine().trim();
        System.out.print(Message.SIGNUP_CONFIRM_PASSWORD + " ");
        String confirmPassword = scanner.nextLine().trim();
        return new String[]{password, confirmPassword};
    }
    /**
     * Prompts the user for their full name.
     * @return the entered full name
     */
    private String promptFullName() {
        System.out.print(Message.SIGNUP_ENTER_FULLNAME + " ");
        return scanner.nextLine().trim();
    }
    /**
     * Prompts the user to select a user role.
     * @return the selected UserRole
     */
    private UserRole selectUserRole() {
        while (true) {
            System.out.println(Menu.ROLE_SELECTION_MENU);
            String roleChoice = scanner.nextLine().trim();
            switch (roleChoice) {
                case "1": return UserRole.CITIZEN;
                case "2": return UserRole.GOVERNMENT_MEMBER;
                case "3": return UserRole.PRIME_MINISTER;
                default:
                    System.out.println(Message.ERROR_INVALID_ROLE);
            }
        }
    }
    // Helper class to hold user registration data
    private static class UserRegistrationData {
        final String username;
        final String password;
        final String fullName;
        final String ministry;
        
        /**
         * Constructor for UserRegistrationData.
         * @param username the username
         * @param password the password
         * @param fullName the full name
         * @param ministry the ministry (may be null)
         */
        UserRegistrationData(String username, String password, String fullName, String ministry) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.ministry = ministry;
        }
    }

    // Helper: checks if the ministry is valid
    private boolean isValidMinistry(String ministry) {
        for (Ministry m : Ministry.values()) {
            if (m.name().equalsIgnoreCase(ministry) || m.toString().equalsIgnoreCase(ministry)) {
                return true;
            }
        }
        return false;
    }

    // Helper: returns the normalized enum name (e.g., "FINANCE")
    private String normalizeMinistry(String ministry) {
        for (Ministry m : Ministry.values()) {
            if (m.name().equalsIgnoreCase(ministry) || m.toString().equalsIgnoreCase(ministry)) {
                return m.name();
            }
        }
        return ministry;
    }
}
