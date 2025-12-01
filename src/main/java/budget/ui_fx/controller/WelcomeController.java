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
    private void handleLogin() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            SceneLoader loader = new SceneLoader(stage);
            loader.load("/view/LoginView.fxml", "Login");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading Login View");
        }
    }

    @FXML
private void handleAccountCreation() {
    try {
        Stage stage = (Stage) createAccountButton.getScene().getWindow();
        SceneLoader loader = new SceneLoader(stage);
        loader.load("/view/AccountCreationView.fxml", "Create New Account");
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Δεν βρέθηκε το αρχείο AccountCreationView.fxml");
    }
}
}
