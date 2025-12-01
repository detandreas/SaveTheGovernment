package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import budget.constants.Message;
import budget.service.UserAuthenticationService;
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
    @FXML private Button backButton;

    private final UserAuthenticationService authService = 
        new UserAuthenticationService(new UserRepository());

    @FXML
    public void initialize() {
        loginLabel.setText(Message.LOGIN_MESSAGE);
        usernameLabel.setText(Message.USERNAME_PROMPT);
        passwordLabel.setText(Message.PASSWORD_PROMPT);
        loginButton.setText(Message.LOGIN_BUTTON);
        cancelButton.setText(Message.CANCEL_BUTTON);
        backButton.setText(Message.BACK_BUTTON);
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean isAuthenticated = authService.login(username, password);

        if (isAuthenticated) {
            errorLabel.setText("");
            Stage stage = (Stage) loginButton.getScene().getWindow();
            SceneLoader loader = new SceneLoader(stage);
            loader.load("/view/DashboardView.fxml", "Dashboard"); 
        } else {
            errorLabel.setText(Message.LOGIN_FAILED);
        }
    }

    @FXML
    private void handleCancel() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setText("");

        Stage stage = (Stage) cancelButton.getScene().getWindow();
        SceneLoader loader = new SceneLoader(stage);
        
        loader.load("/resources/view/WelcomeView.fxml", "Welcome");
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneLoader loader = new SceneLoader(stage);
        loader.load("/view/WelcomeView.fxml", "Welcome");
    }
}
