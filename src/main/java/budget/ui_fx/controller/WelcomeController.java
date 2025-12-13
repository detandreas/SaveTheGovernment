package budget.ui_fx.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import budget.ui_fx.util.SceneLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller for the welcome screen.
 */
public class WelcomeController {
    private static final Logger LOGGER =
                        Logger.getLogger(WelcomeController.class.getName());

    @FXML private Label nameLabel;
    @FXML private Label welcomeLabel;
    @FXML private Button loginButton;
    @FXML private Button createAccountButton;
//    /**
//     * Initializes the welcome screen with application name
//     *                                         and welcome message.
//     */
//    @FXML
//    public void initialize() {
//        nameLabel.setText(Message.APPLICATION_NAME);
//        welcomeLabel.setText(Message.WELCOME_MESSAGE);
//    }
    //η initialize μεθοδο καλό είναι χρησιμοποιείται για την διαμόρφωση
    // του controller πριν εμφανισθεί στο GUI
    /**
     * Handles the login button action to load the Login View.
     */
    @FXML
    private void handleLogin() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            SceneLoader.load(
                stage,
                "/view/LoginView.fxml",
                "Login"
            );
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(
                Level.SEVERE,
                "Error loading Login View"
            );
        }
    }
    /**
     * Handles the create account button action to
     *                                      load the Account Creation View.
     */
    @FXML
    private void handleAccountCreation() {
        try {
            Stage stage = (Stage) createAccountButton.getScene().getWindow();
            SceneLoader.load(
                            stage,
                            "/view/AccountCreationView.fxml",
                            "Create New Account"
                        );
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(
                Level.SEVERE,
                "Δεν βρέθηκε το αρχείο AccountCreationView.fxml"
            );
        }
    }
}
