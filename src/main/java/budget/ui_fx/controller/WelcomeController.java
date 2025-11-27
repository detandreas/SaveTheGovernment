package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import budget.constants.Message;
import budget.ui_fx.util.SceneLoader;

public class WelcomeController {

    @FXML private Label welcomeLabel;
    @FXML private Button loginButton;
    @FXML private Button createAccountButton;

    @FXML
    public void initialize() {
        welcomeLabel.setText(Message.WELCOME_MESSAGE);
    }
    
    @FXML
    private void goToLoginView() {
        SceneLoader loader = new SceneLoader((Stage) loginButton.getScene().getWindow());
        loader.load("/fxml/loginView.fxml", "Login");
    }

    @FXML
    private void goToAccountCreationView() {
        SceneLoader loader = new SceneLoader((Stage) createAccountButton.getScene().getWindow());
        loader.load("/fxml/AccountCreationView.fxml", "Create Account");
    }
}
