package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import budget.constants.Message;
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

    @FXML
    public void initialize() {
        loginLabel.setText(Message.LOGIN_LABEL);
        usernameLabel.setText(Message.USERNAME_LABEL);
        passwordLabel.setText(Message.PASSWORD_LABEL);
        loginButton.setText(Message.LOGIN_BUTTON);
        cancelButton.setText(Message.CANCEL_BUTTON);
        errorLabel.setText("");
    }
}
