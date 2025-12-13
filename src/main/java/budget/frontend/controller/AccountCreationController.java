package budget.frontend.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import budget.backend.exceptions.ValidationException;
import budget.backend.model.domain.user.Citizen;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Ministry;
import budget.backend.model.enums.UserRole;
import budget.backend.repository.UserRepository;
import budget.backend.service.InputValidationService;
import budget.backend.service.UserAuthenticationService;
import budget.backend.util.InputValidator;
import budget.backend.util.PasswordUtils;
import budget.constants.Message;
import budget.frontend.constants.Constants;
import budget.frontend.util.SceneLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


public class AccountCreationController {

    // FXML UI Labels
    @FXML private Label createAccountLabel;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    // FXML UI Text Fields
    @FXML private TextField usernameTextField;
    @FXML private TextField fullNameTextField;
    @FXML private TextField visiblePasswordTextField;
    @FXML private TextField confirmVisiblePasswordTextField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private PasswordField passwordField;

    // FXML UI Buttons
    @FXML private Button createAccountButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button showPasswordButton;

    // FXML UI Image Views
    @FXML private ImageView eyeIconImageView;
    @FXML private ImageView confirmEyeIconImageView;

    // FXML UI Combo Boxes
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private ComboBox<Ministry> ministryComboBox;

    // FXML UI Container
    @FXML private HBox ministryContainer;


    private static final String EYE_OPEN_PATH = "/images/eye_open.png";
    private static final String EYE_CLOSED_PATH = "/images/eye_closed.png";
    private static final Logger LOGGER =
                Logger.getLogger(AccountCreationController.class.getName());

    private final UserAuthenticationService authService =
        new UserAuthenticationService(new UserRepository()
    );

    /**
     * Initializes the controller by setting up UI components and bindings.
     */
    @FXML
    public void initialize() {
        passwordField.textProperty()
                    .bindBidirectional(visiblePasswordTextField.textProperty());
        confirmPasswordField.textProperty()
                    .bindBidirectional(confirmVisiblePasswordTextField
                                        .textProperty());

        roleComboBox.getItems().setAll(UserRole.values());
        ministryComboBox.getItems().setAll(Ministry.values());

        // προσθήκη listener γισ handling της περίπτωσης
        // που ο χρήστης διαλέγει role govMember
        // και επαναφορά
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == UserRole.GOVERNMENT_MEMBER) {
                ministryContainer.setVisible(true);
                ministryContainer.setManaged(true);
            } else {
                ministryContainer.setVisible(false);
                ministryContainer.setManaged(false);
                ministryComboBox.setValue(null);
            }
        });

        ministryContainer.setVisible(false);
        ministryContainer.setManaged(false);
    }
    /**
     * Handles the event when the user clicks to show the password.
     * Switches the visibility of the password fields and updates the eye icon.
     * @param event the event that will be handled.
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
     * Handles the event when the user clicks to show the confirm password.
     * Switches the visibility of the confirm password fields
     *                                              and updates the eye icon.
     * @param event the event that will be handled.
     */
    @FXML
    private void handleShowConfirmPassword(MouseEvent event) {
        confirmVisiblePasswordTextField.requestFocus();
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setManaged(false);
        confirmVisiblePasswordTextField.setVisible(true);
        confirmVisiblePasswordTextField.setManaged(true);

        Image closedEye =
                    new Image(getClass().getResourceAsStream(EYE_CLOSED_PATH));

        confirmEyeIconImageView.setImage(closedEye);
    }
    /**
     * Handles the event when the user clicks to hide the password.
     * Switches the visibility of the password fields and updates the eye icon.
     * @param event the event that will be handled.
     */
    @FXML
    private void handleHidePassword(MouseEvent event) {
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
     * Handles the event when the user clicks to hide the confirm password.
     * Switches the visibility of the confirm password fields and
     *                                                  updates the eye icon.
     * @param event the event that will be handled.
     */
    @FXML
    private void handleHideConfirmPassword(MouseEvent event) {
        confirmPasswordField.requestFocus();
        confirmVisiblePasswordTextField.setVisible(false);
        confirmVisiblePasswordTextField.setManaged(false);
        confirmPasswordField.setVisible(true);
        confirmPasswordField.setManaged(true);

        Image openEye =
                    new Image(getClass().getResourceAsStream(EYE_OPEN_PATH));

        confirmEyeIconImageView.setImage(openEye);
    }
    /**
     * Handles the account creation process when the user clicks
     *                                              the create account button.
     * Validates input and interacts with the authentication service
     *                                          to create a new user account.
     */
    @FXML
    private void handleCreateAccount() {
        clearLabels();

        try {
            String username = usernameTextField.getText();
            String password = passwordField.getText();
            String fullName = fullNameTextField.getText();
            Ministry ministry = ministryComboBox.getValue();
            UserRole role = roleComboBox.getValue();

            validatePassword(password);
            String hashedPassword = PasswordUtils.hashPassword(password);
            User userToValidate = createUser(username, fullName, hashedPassword,
                                                        role, ministry);

            if (userToValidate != null) {
                createAccount(userToValidate, username, password,
                                fullName, role, ministry);
                showSuccessMessage();
            }
        } catch (ValidationException e) {
                LOGGER.log(
                    Level.INFO,
                    "Απέτυχε το validation: {0}",
                    e.getMessage()
                );
                showErrorMessage(e.getMessage());
            }
        }

    /**
     * Clears error and success labels.
     */
    private void clearLabels() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
        successLabel.setVisible(false);
    }
    /**
     * Validates that the password meets strength requirements.
     * @param password the password to validate
     * @throws ValidationException if password is not strong
     */
    private void validatePassword(String password) throws ValidationException {
        boolean strong = InputValidator.isPasswordStrong(password);
        if (!strong) {
            throw new ValidationException(
                Message.PASSWORD_COMPLEXITY_FAIL_MESSAGE);
        }
    }

    /**
     * Creates a User object based on the provided role.
     * @param username the username
     * @param fullName the full name
     * @param hashedPassword the hashed password
     * @param role the user role
     * @param ministry the ministry (required for GOVERNMENT_MEMBER)
     * @return the created User object
     */
    private User createUser(String username, String fullName,
                String hashedPassword, UserRole role, Ministry ministry) {
        return switch (role) {
            case CITIZEN -> new Citizen(username, fullName, hashedPassword);
            case GOVERNMENT_MEMBER -> new GovernmentMember(username, fullName,
                                                    hashedPassword, ministry);
            case PRIME_MINISTER -> PrimeMinister.getInstance(username,
                                                    fullName, hashedPassword);
            };
        }

    /**
     * Validates and creates the user account.
     * @param userToValidate the user object to validate
     * @param username the username
     * @param password the plain password
     * @param fullName the full name
     * @param role the user role
     * @param ministry the ministry
     * @throws ValidationException if validation fails
     */
    private void createAccount(User userToValidate, String username,
                                String password, String fullName,
                                UserRole role, Ministry ministry)
                                throws ValidationException {
        InputValidationService validationService =
                                            new InputValidationService();
        validationService.validateNewUser(userToValidate);
        authService.signUp(username, password, fullName, role, ministry);
    }
    /**
     * Displays the success message.
     */
    private void showSuccessMessage() {
        successLabel.setText(Message.CREATE_ACCOUNT_SUCCESS);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    /**
     * Displays an error message.
     * @param message the error message to display
     */
    private void showErrorMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }
    /**
     * Resets all the field.
     */
    @FXML
    private void handleCancel() {
        usernameTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        fullNameTextField.clear();
        roleComboBox.setValue(null);
        ministryComboBox.setValue(null);
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
    /**
     * Handles navigation to the Welcome View when user presses
     *                                                  the back button.
     */
    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            SceneLoader.load(stage, Constants.WELCOME_VIEW, "Welcome");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
