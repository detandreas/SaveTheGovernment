package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import budget.ui_fx.util.SceneLoader;
import budget.constants.Message;
import budget.constants.Limits;
import budget.model.enums.UserRole;
import budget.model.enums.Ministry;
import budget.service.UserAuthenticationService;
import budget.repository.UserRepository;

public class AccountCreationController {

    @FXML private Label createAccountLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label ministryLabel;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private ComboBox<Ministry> ministryComboBox;
    @FXML private HBox ministryContainer;
    @FXML private Button createAccountButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    
    private final UserAuthenticationService authService = 
            new UserAuthenticationService(new UserRepository());

    @FXML
    public void initialize() {
        createAccountLabel.setText(Message.CREATE_ACCOUNT_MESSAGE);
        usernameLabel.setText(Message.SIGNUP_ENTER_USERNAME);
        passwordLabel.setText(Message.SIGNUP_ENTER_PASSWORD);
        confirmPasswordLabel.setText(Message.SIGNUP_CONFIRM_PASSWORD);
        fullNameLabel.setText(Message.SIGNUP_ENTER_FULLNAME);
        roleLabel.setText(Message.SIGNUP_SELECT_ROLE);
        ministryLabel.setText(Message.SIGNUP_SELECT_DEPARTMENT);
        createAccountButton.setText(Message.CREATE_ACCOUNT_BUTTON);
        cancelButton.setText(Message.CANCEL_BUTTON);
        backButton.setText(Message.BACK_BUTTON);
        errorLabel.setText("");

        roleComboBox.getItems().setAll(UserRole.values());
        ministryComboBox.getItems().setAll(Ministry.values());

        roleComboBox.setConverter(new StringConverter<UserRole>() {
            @Override
            public String toString(UserRole role) {
                if (role == null) return null;
                return role.getDisplayName(); 
            }

            //Η κλάση StringConverter απαιτεί και την fromString για να λειτουργήσει.
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

    @FXML
    private void handleCreateAccount() {
        if (!isInputValid()) {
            return; 
        }
        String username = usernameField.getText();
        String password = passwordField.getText();
        String fullName = fullNameField.getText();
        UserRole role = roleComboBox.getValue();
        Ministry ministry = ministryComboBox.getValue();

        boolean isCreated = authService.signUp(username, password, fullName, role, ministry);

        if (isCreated) {
            successLabel.setText(Message.CREATE_ACCOUNT_SUCCESS);
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
        errorLabel.setText("");
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
    private boolean isInputValid() {
        UserRole selectedRole = roleComboBox.getValue();
        Ministry selectedMinistry = ministryComboBox.getValue();
        if (usernameField.getLength() > Limits.MAX_USERNAME_LENGTH ||
            usernameField.getLength() < Limits.MIN_USERNAME_LENGTH) {
            errorLabel.setText(Message.USERNAME_LENGTH_LIMITS_MESSAGE);
            return false;
        }
        if (passwordField.getLength() > Limits.MAX_PASSWORD_LENGTH ||
            passwordField.getLength() < Limits.MIN_PASSWORD_LENGTH) {
            errorLabel.setText(Message.PASSWORD_LENGTH_LIMITS_MESSAGE);
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText(Message.ERROR_PASSWORD_MISMATCH);
            return false;
        }
        if (fullNameField.getLength() > Limits.MAX_FULL_NAME_LENGTH) {
            errorLabel.setText(Message.FULLNAME_LENGTH_LIMITS_MESSAGE);
            return false;
        }
        if (selectedRole == null) {
            errorLabel.setText(Message.ERROR_ROLE);
            return false;
        }
        if (selectedRole == UserRole.GOVERNMENT_MEMBER && selectedMinistry == null) {
            errorLabel.setText(Message.ERROR_MINISTRY);
            return false;
        }
        return true;
    }
}
