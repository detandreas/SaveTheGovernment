package budget.frontend.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import budget.backend.exceptions.UserNotAuthorizedException;
import budget.backend.exceptions.ValidationException;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Ministry;
import budget.backend.repository.UserRepository;
import budget.backend.service.UserAuthenticationService;
import budget.backend.util.InputValidator;
import budget.constants.Message;
import budget.frontend.constants.Constants;
import budget.frontend.util.SceneLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Controller class for handling user login functionality.
 */
public class LoginController {

    @FXML private Label loginLabel;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML private TextField usernameTextField;
    @FXML private TextField visiblePasswordTextField;

    @FXML private PasswordField passwordField;

    @FXML private ImageView eyeIconImageView;

    @FXML private Button showPasswordButton;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;

    private static final String EYE_OPEN_PATH = "/images/eye_open.png";
    private static final String EYE_CLOSED_PATH = "/images/eye_closed.png";
    private static final Logger LOGGER =
                            Logger.getLogger(LoginController.class.getName());

    private final UserAuthenticationService authService =
        new UserAuthenticationService(new UserRepository()
    );
    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        errorLabel.setText("");
        successLabel.setText("");
        passwordField.textProperty()
                    .bindBidirectional(visiblePasswordTextField.textProperty());
    }
    /**
     * Handles the login process when the login button is clicked.
     */
    @FXML
    private void handleLogin() {
        LOGGER.log(
            Level.INFO,
            "--- [1] Ξεκίνησε το handleLogin ---"
        );

        String username = usernameTextField.getText();
        String password = passwordField.getText();

        if (!validateLoginInput(username, password)) {
            return;
        }

        try {
            authService.login(username, password);
            LOGGER.log(
                Level.INFO,
                "--- [2] Login successful ---"
            );
            handleSuccessfulLogin();
        } catch (ValidationException e) {
            LOGGER.log(
                Level.WARNING,
                "Validation error during login: {0}",
                e.getMessage()
            );
            errorLabel.setText(e.getMessage());
        } catch (UserNotAuthorizedException e) {
            LOGGER.log(
                Level.WARNING,
                "Authentication failed: {0}",
                e.getMessage()
            );
            errorLabel.setText(e.getMessage());
        }
    }
    /**
     * Validates login input using InputValidator.
     * @param username the username to validate
     * @param password the password to validate
     * @return true if input is valid, false otherwise
     */
    private boolean validateLoginInput(String username, String password) {
        if (!InputValidator.isUserName(username)) {
            errorLabel.setText("Invalid username.");
            LOGGER.log(
                Level.WARNING,
                "Invalid username format: {0}",
                username
            );
            return false;
        }

        if (!InputValidator.isNonNull(password) || password.isEmpty()) {
            errorLabel.setText("The password is mandatory.");
            LOGGER.log(
                Level.WARNING,
                "Password is null or empty"
            );
            return false;
        }

        return true;
    }
    /**
     * Handles the flow after successful authentication.
     */
    private void handleSuccessfulLogin() {
        errorLabel.setText("");
        successLabel.setText(Message.LOGIN_SUCCESS);

        try {
            User user = authService.getCurrentUser();
            LOGGER.log(
                Level.INFO,
                "--- [3] User Object: {0}",
                user
            );

            if (user == null) {
                LOGGER.log(
                    Level.WARNING,
                    "!!! ERROR: Ο χρήστης είναι NULL !!!"
                );
                errorLabel
                    .setText("An error occurred while retrieving the user.");
                return;
            }

            LOGGER.log(
                Level.INFO,
                "--- [4] User Role: {0}",
                user.getUserRole()
            );

            ViewPathInfo viewInfo = determineViewPathAndTitle(user);

            if (viewInfo != null && !viewInfo.getPath().isEmpty()) {
                navigateToDashboard(viewInfo, user);
            } else {
                LOGGER.log(
                    Level.WARNING,
                    "!!! ERROR: Το viewPath είναι κενό! "
                                                + "Κανένα case δεν ταίριαξε."
                );
                errorLabel
                .setText("An error occurred while specifying the page.");
            }

        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE,
                "!!! EXCEPTION CAUGHT IN LOGIN !!!",
                e
            );
            errorLabel.setText("Error occured.");
        }
    }
    /**
     * Determines the view path and window title based on user role.
     * @param user the authenticated user
     * @return ViewPathInfo containing path and title,
     *                                  or null if role is invalid
     */
    private ViewPathInfo determineViewPathAndTitle(User user) {
        return switch (user.getUserRole()) {
            case GOVERNMENT_MEMBER -> {
                LOGGER.log(
                    Level.INFO,
                    "--- [5] Μπήκαμε στο Case GOVERNMENT_MEMBER"
                );
                    yield determineGovernmentMemberView(
                                                    (GovernmentMember) user);
            }
            case CITIZEN -> {
                LOGGER.log(
                    Level.INFO,
                    "--- [5] Μπήκαμε στο Case CITIZEN"
                );
                 yield new ViewPathInfo(
                    Constants.CITIZEN_VIEW,
                    "Citizen Dashboard"
                );
            }
            case PRIME_MINISTER -> {
                LOGGER.log(
                    Level.INFO,
                    "--- [5] Μπήκαμε στο Case PRIME_MINISTER"
                );
                PrimeMinister.setInstance((PrimeMinister) user);
                yield new ViewPathInfo(
                    Constants.PRIME_MINISTER_VIEW,
                    "Prime Minister Dashboard"
                );
            }
            default -> {
                LOGGER.log(
                    Level.WARNING,
                    "!!! ERROR: Το viewPath είναι κενό! "
                                        + "Κανένα case δεν ταίριαξε."
                );
                yield null;
            }
        };
    }
    /**
     * Navigates to the appropriate dashboard view.
     * @param viewInfo the view path and title information
     * @param user the user who will be viewing the dashboard
     */
    private void navigateToDashboard(ViewPathInfo viewInfo, User user) {
        Stage stage = (Stage) loginButton.getScene().getWindow();

        LOGGER.log(
            Level.INFO,
            "--- [6] Path που επιλέχθηκε: {0}",
            viewInfo.getPath()
        );
        LOGGER.log(
            Level.INFO,
            "--- [7] Κλήθηκε loader.load({0}).",
            viewInfo.getPath()
        );

        loadDashboardAndUserInfo(stage, viewInfo, user);

        LOGGER.log(
            Level.FINE,
            "--- [8] Η loader.load() τελείωσε το View φορτώθηκε με επιτυχία"
        );
    }

    private void loadDashboardAndUserInfo(
        Stage stage,
        ViewPathInfo viewInfo,
        User user
    ) {
        // Φορτώνουμε το dashboard και παίρνουμε το controller
        DashboardController controller = SceneLoader.loadAndGetController(
            stage,
            viewInfo.getPath(),
            viewInfo.getTitle()
        );

        if (controller == null) {
            LOGGER.log(
                Level.WARNING,
                "!!! ERROR: Το controller είναι null !!!"
            );
            return;
        }

        controller.setUserInfo(user.getFullName(),
                                user.getUserRole().toString());
    }
    /**
     * Determines the view path for government members based on their ministry.
     * @param gm the government member
     * @return ViewPathInfo for the government member
     */
    private ViewPathInfo determineGovernmentMemberView(GovernmentMember gm) {
        if (gm.getMinistry() == Ministry.FINANCE) {
            return new ViewPathInfo(
                Constants.FINANCE_GOV_VIEW,
                "Finance Government Member Dashboard"
            );
        } else {
            return new ViewPathInfo(
                Constants.GOV_VIEW,
                "Government Member Dashboard"
            );
        }
    }
    /**
     * Handles showing the password in plain text when the eye icon is clicked.
     * @param event the event handled in this method.
     */
    @FXML
    private void handleShowPassword(MouseEvent event) {
        visiblePasswordTextField.requestFocus();
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        visiblePasswordTextField.setVisible(true);
        visiblePasswordTextField.setManaged(true);

        Image closedEye =
                    new Image(getClass().getResourceAsStream(EYE_CLOSED_PATH));

        eyeIconImageView.setImage(closedEye);
    }
    /**
     * Handles hiding the password and showing it as masked
     *                                          when the eye icon is clicked.
     */
    @FXML
    private void handleHidePassword() {
        passwordField.requestFocus();
        visiblePasswordTextField.setVisible(false);
        visiblePasswordTextField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);

        Image openEye =
                    new Image(getClass().getResourceAsStream(EYE_OPEN_PATH));

        eyeIconImageView.setImage(openEye);
    }
    /**
     * Handles the cancel action, clearing fields
     *                              and returning to the welcome view.
     */
    @FXML
    private void handleCancel() {
        usernameTextField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);

        Stage stage = (Stage) cancelButton.getScene().getWindow();
        SceneLoader.load(stage, Constants.WELCOME_VIEW, "Welcome");
    }
    /**
     * Handles the back action, returning to the welcome view.
     */
    @FXML
    private void handleBack() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneLoader.load(stage, Constants.WELCOME_VIEW, "Welcome");
    }
    /**
     * Helper class to hold view path and title information.
     */
    private static class ViewPathInfo {
        private final String path;
        private final String title;

        ViewPathInfo(
            String path,
            String title
        ) {
            this.path = path;
            this.title = title;
        }

        String getPath() {
            return path;
        }

        String getTitle() {
            return title;
        }
    }
}
