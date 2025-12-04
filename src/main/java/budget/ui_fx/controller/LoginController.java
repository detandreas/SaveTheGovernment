package budget.ui_fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import budget.constants.Message;
import budget.service.UserAuthenticationService;
import budget.repository.UserRepository;
import budget.ui_fx.util.SceneLoader;

public class LoginController {

    @FXML private Label loginLabel;
    @FXML private Label errorLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ImageView eyeIconImageView;
    @FXML private Button showPasswordButton;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;

    private final String EYE_OPEN_PATH = "/images/eye_open.png";
    private final String EYE_CLOSED_PATH = "/images/eye_closed.png";

    private final UserAuthenticationService authService = 
        new UserAuthenticationService(new UserRepository());

    @FXML
    public void initialize() {
        loginLabel.setText(Message.LOGIN_BUTTON);
        loginButton.setText(Message.LOGIN_BUTTON);
        cancelButton.setText(Message.CANCEL_BUTTON);
        backButton.setText(Message.BACK_BUTTON);
        errorLabel.setText("");
        usernameField.setPromptText(Message.USERNAME_PROMPT);
        passwordField.setPromptText(Message.PASSWORD_PROMPT);
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
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
    private void handleShowPassword(MouseEvent event) {
        visiblePasswordField.requestFocus(); 
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        visiblePasswordField.setVisible(true);
        visiblePasswordField.setManaged(true);
        
        // 1. Δημιουργήστε ένα νέο αντικείμενο Image χρησιμοποιώντας το eye_closed.png
        // (Η διαδρομή ξεκινά από το resources, π.χ. /images/eye_closed.png)
        Image closedEye = new Image(getClass().getResourceAsStream(EYE_CLOSED_PATH));
        
        // 2. Ορίστε τη νέα εικόνα στο ImageView
        eyeIconImageView.setImage(closedEye);
    }

    @FXML
    private void handleHidePassword() {
        passwordField.requestFocus(); 
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);

        // 1. Δημιουργήστε ένα νέο αντικείμενο Image χρησιμοποιώντας το eye_open.png
        Image openEye = new Image(getClass().getResourceAsStream(EYE_OPEN_PATH));
        
        // 2. Ορίστε τη νέα εικόνα στο ImageView
        eyeIconImageView.setImage(openEye);
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
