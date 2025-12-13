package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import budget.ui_fx.util.SceneLoader;
import budget.constants.Message;
import budget.exceptions.ValidationException;
import budget.model.enums.UserRole;
import budget.model.domain.user.Citizen;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;
import budget.service.InputValidationService;
import budget.service.UserAuthenticationService;
import budget.repository.UserRepository;

public class AccountCreationController {

    // FXML UI Labels
    @FXML private Label createAccountLabel;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    // FXML UI Text Fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField visiblePasswordField;
    @FXML private TextField confirmVisiblePasswordField;

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
    

    private final String EYE_OPEN_PATH = "/images/eye_open.png";
    private final String EYE_CLOSED_PATH = "/images/eye_closed.png";
    
    private final UserAuthenticationService authService = 
        new UserAuthenticationService(new UserRepository()
    );

    /**
     * Initializes the controller by setting up UI components and bindings.
     */
    @FXML
    public void initialize() {
        createAccountLabel.setText(Message.CREATE_ACCOUNT_MESSAGE);
        usernameField.setPromptText(Message.SIGNUP_ENTER_USERNAME);
        passwordField.setPromptText(Message.SIGNUP_ENTER_PASSWORD);
        confirmPasswordField.setPromptText(Message.SIGNUP_CONFIRM_PASSWORD);
        fullNameField.setPromptText(Message.SIGNUP_ENTER_FULLNAME);
        roleComboBox.setPromptText(Message.SIGNUP_SELECT_ROLE);
        ministryComboBox.setPromptText(Message.SIGNUP_SELECT_DEPARTMENT);
        createAccountButton.setText(Message.CREATE_ACCOUNT_BUTTON);
        cancelButton.setText(Message.CANCEL_BUTTON);
        backButton.setText(Message.BACK_BUTTON);
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmVisiblePasswordField.textProperty());

        roleComboBox.getItems().setAll(UserRole.values());
        ministryComboBox.getItems().setAll(Ministry.values());

        roleComboBox.setConverter(new StringConverter<UserRole>() {
            @Override
            public String toString(UserRole role) {
                if (role == null) return null;
                return role.getDisplayName(); 
            }

            @Override
            public UserRole fromString(String string) {
                return null; 
            }
        });

        ministryComboBox.setConverter(new StringConverter<Ministry>() {
            @Override
            public String toString(Ministry ministry) {
                if (ministry == null) return null;
                return ministry.getDisplayName(); 
            }
            @Override
            public Ministry fromString(String string) { 
                return null;
            }
        });

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
     */
    @FXML
    private void handleShowPassword(MouseEvent event) {
        visiblePasswordField.requestFocus(); 
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        visiblePasswordField.setVisible(true);
        visiblePasswordField.setManaged(true);
        
        Image closedEye = new Image(getClass().getResourceAsStream(EYE_CLOSED_PATH));
        
        eyeIconImageView.setImage(closedEye);
    }
    /**
     * Handles the event when the user clicks to show the confirm password.
     * Switches the visibility of the confirm password fields and updates the eye icon.
     */
    @FXML
    private void handleShowConfirmPassword(MouseEvent event) {
        confirmVisiblePasswordField.requestFocus(); 
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setManaged(false);
        confirmVisiblePasswordField.setVisible(true);
        confirmVisiblePasswordField.setManaged(true);
        
        Image closedEye = new Image(getClass().getResourceAsStream(EYE_CLOSED_PATH));

        confirmEyeIconImageView.setImage(closedEye);
    }
    /**
     * Handles the event when the user clicks to hide the password.
     * Switches the visibility of the password fields and updates the eye icon.
     */
    @FXML
    private void handleHidePassword(MouseEvent event) {
        passwordField.requestFocus(); 
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);

        Image openEye = new Image(getClass().getResourceAsStream(EYE_OPEN_PATH));
        
        eyeIconImageView.setImage(openEye);
    }
    /**
     * Handles the event when the user clicks to hide the confirm password.
     * Switches the visibility of the confirm password fields and updates the eye icon.
     */
    @FXML
    private void handleHideConfirmPassword(MouseEvent event) {
        confirmPasswordField.requestFocus();
        confirmVisiblePasswordField.setVisible(false);
        confirmVisiblePasswordField.setManaged(false);
        confirmPasswordField.setVisible(true);
        confirmPasswordField.setManaged(true);

        Image openEye = new Image(getClass().getResourceAsStream(EYE_OPEN_PATH));
        
        confirmEyeIconImageView.setImage(openEye);
    }
    /**
     * Handles the account creation process when the user clicks the create account button.
     * Validates input and interacts with the authentication service to create a new user account.
     */
    @FXML
    private void handleCreateAccount() {
        errorLabel.setVisible(false);
        errorLabel.setText(""); 
        successLabel.setVisible(false);

        String username = usernameField.getText();
        String password = passwordField.getText();
        String fullName = fullNameField.getText();
        Ministry ministry = ministryComboBox.getValue();
        UserRole role = roleComboBox.getValue();

        User userToValidate = null;

        if(role == UserRole.CITIZEN ) {
            userToValidate = new Citizen(username, fullName, password);
        } else if (role == UserRole.GOVERNMENT_MEMBER) {
            userToValidate = new GovernmentMember(username, fullName, password, ministry);
        } else {
            userToValidate = PrimeMinister.getInstance(username, fullName, password);
        }

        if (userToValidate != null) {
            try {
                InputValidationService validationService = new InputValidationService();
                validationService.validateNewUser(userToValidate); 
                boolean isCreated = authService.signUp(username, password, fullName, role, ministry);

                if (isCreated) {
                    successLabel.setText(Message.CREATE_ACCOUNT_SUCCESS);
                    successLabel.setVisible(true);
                    errorLabel.setVisible(false); 
                } else {
                    errorLabel.setText(Message.ERROR_INVALID_INPUT); 
                    errorLabel.setVisible(true);
                    successLabel.setVisible(false);
                }

            } catch (ValidationException e) {
                errorLabel.setText(e.getMessage());
                errorLabel.setVisible(true);
                successLabel.setVisible(false);
            }
        }
    }

    @FXML
    private void handleCancel() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        fullNameField.clear();
        roleComboBox.setValue(null);
        ministryComboBox.setValue(null);
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            SceneLoader loader = new SceneLoader(stage);
            loader.load("/view/WelcomeView.fxml", "Welcome");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
