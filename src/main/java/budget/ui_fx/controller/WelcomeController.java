package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import budget.constants.Message;
import budget.ui_fx.util.SceneLoader;

/** 
 * Controller for the welcome screen.
 */
public class WelcomeController {
    @FXML private Label nameLabel;
    @FXML private Label welcomeLabel;
    @FXML private Button loginButton;
    @FXML private Button createAccountButton;
    /** 
     * Initializes the welcome screen with application name and welcome message.
     */
    @FXML
    public void initialize() {
        nameLabel.setText(Message.APPLICATION_NAME);
        welcomeLabel.setText(Message.WELCOME_MESSAGE);
    }
    /** 
     * Handles the login button action to load the Login View.
     */
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
    /** 
     * Handles the create account button action to load the Account Creation View.
     */
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
