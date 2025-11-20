package budget.ui;

import budget.model.domain.user.User;
import budget.model.domain.user.Citizen;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.enums.UserRole;
import budget.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserAuthenticationService authService;
    private final InputValidationService validationService;
    private final Scanner scanner;
    /**
     * Constructor for LoginHandler.
     * @param authService the authentication service
     */
    public LoginHandler(UserRepository userRepository, UserAuthenticationService authService, 
    InputValidationService validationService) {
        this.userRepository = userRepository;
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
        while (true) {
            System.out.println(Menu.LOGIN_MENU);
            System.out.print(Message.SELECT_CHOICE_MESSAGE + " ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    User user = handleLogin();
                    if (user != null) return user;
                    break;
                case "2":
                    user = handleAccountCreation();
                    if (user != null) return user;
                    break;
                case "3":
                    System.out.println(Menu.EXIT_CONFIRMATION_MENU);
                    String exitChoice = scanner.nextLine().trim();
                    if (exitChoice.equals("1")) {
                        System.out.println(Message.EXIT_MESSAGE);
                        System.exit(0);
                    } else if (exitChoice.equals("2")) {
                        break; // Return to main login menu
                    } else {
                        System.out.println(Message.INVALID_OPTION);
                    }
                default:
                    System.out.println(Message.INVALID_OPTION);
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
        boolean isLoggedIn = authService.login(username, password);
        User user = isLoggedIn ? authService.getCurrentUser() : null;
        if (user != null) {
            System.out.printf(Message.LOGIN_SUCCESS, user.getFullName());
            MenuHandler menuHandler = new MenuHandler(scanner, authService);
            menuHandler.showMenu(user);
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
        
         if (role == UserRole.PRIME_MINISTER && userRepository.primeMinisterExists()) {
            System.out.println(Message.PRIME_MINISTER_EXISTS);
            return handleAccountCreation();
         }
         Ministry ministry = null;
        if (role == UserRole.GOVERNMENT_MEMBER) {
            ministry = selectMinistry();
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
                 break;
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
            if (userRepository.usernameExists(username)) {
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
            Ministry ministry = null;
            if (role == UserRole.GOVERNMENT_MEMBER) {
                ministry = selectMinistry();
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
    private Ministry selectMinistry() {
        while (true) {
            System.out.println(Message.SIGNUP_SELECT_DEPARTMENT);
            Ministry[] ministries = Ministry.values();
            for (int i = 0; i < ministries.length; i++) {
                System.out.printf("%d. %s%n", i + 1, ministries[i].getDisplayName());
            }
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                int choiceIndex = choice - 1;
                if (choiceIndex >= 0 && choiceIndex < ministries.length) {
                    return ministries[choiceIndex];
                } else {
                    System.out.println(Message.ERROR_INVALID_MINISTRY);
                }
            } catch (Exception e) {
                scanner.nextLine();
                System.out.println(Message.ERROR_INVALID_INPUT);
            }
        }
    }
    // Helper class to hold user registration data
    private static class UserRegistrationData {
        final String username;
        final String password;
        final String fullName;
        final Ministry ministry;
        
        /**
         * Constructor for UserRegistrationData.
         * @param username the username
         * @param password the password
         * @param fullName the full name
         * @param ministry the ministry (may be null)
         */
        UserRegistrationData(String username, String password, String fullName, Ministry ministry) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.ministry = ministry;
        }
    }

    
}
