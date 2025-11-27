package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import budget.constants.Message;
import budget.service.UserAuthenticationService;
import budget.model.domain.user.User;
import budget.repository.UserRepository;
import budget.ui_fx.util.SceneLoader;

public class LoginController {

    @FXML private Label loginLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label errorLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;

    private final UserAuthenticationService authService = new UserAuthenticationService(
        SceneLoader.getUserRepository()
    );

    @FXML
    public void initialize() {
        loginLabel.setText(Message.LOGIN_MESSAGE);
        usernameLabel.setText(Message.USERNAME_PROMPT);
        passwordLabel.setText(Message.PASSWORD_PROMPT);
        loginButton.setText(Message.LOGIN_BUTTON);
        cancelButton.setText(Message.CANCEL_BUTTON);
    }

     @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean isAuthenticated = authService.login(username, password);

        if (isAuthenticated) {
            errorLabel.setText("");
            // Αλλαγή σκηνής ανάλογα με τον ρόλο
            SceneLoader.loadDashboardScreen(); 
        } else {
            errorLabel.setText(Message.LOGIN_FAILED);
        }
    }

    @FXML
    private void handleCancel() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setText("");
        SceneLoader.loadWelcomeScreen();
    }
}
