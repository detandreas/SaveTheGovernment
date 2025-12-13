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
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;
import budget.model.enums.UserRole;
import budget.service.UserAuthenticationService;
import budget.repository.UserRepository;
import budget.ui_fx.util.SceneLoader;

/**
 * Controller class for handling user login functionality.
 */
public class LoginController {

    @FXML private Label loginLabel;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
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
        new UserAuthenticationService(new UserRepository()
    );
    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        loginLabel.setText(Message.LOGIN_BUTTON);
        loginButton.setText(Message.LOGIN_BUTTON);
        cancelButton.setText(Message.CANCEL_BUTTON);
        backButton.setText(Message.BACK_BUTTON);
        errorLabel.setText("");
        successLabel.setText("");
        usernameField.setPromptText(Message.USERNAME_PROMPT);
        passwordField.setPromptText(Message.PASSWORD_PROMPT);
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
    }
    /**
     * Handles the login process when the login button is clicked.
     */
    @FXML
    private void handleLogin() {
        System.out.println("--- [1] Ξεκίνησε το handleLogin ---");

        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean isAuthenticated = authService.login(username, password);
        System.out.println("--- [2] Authenticated: " + isAuthenticated);

        if (isAuthenticated) {
            errorLabel.setText("");
            successLabel.setText(Message.LOGIN_SUCCESS);

            try {
                User user = authService.getCurrentUser();
                System.out.println("--- [3] User Object: " + user);

                if (user == null) {
                    System.out.println("!!! ERROR: Ο χρήστης είναι NULL !!!");
                    return;
                }

                System.out.println("--- [4] User Role: " + user.getUserRole());

                Stage stage = (Stage) loginButton.getScene().getWindow();
                SceneLoader loader = new SceneLoader(stage);

                String viewPath = "";
                String windowTitle = "";

                switch (user.getUserRole()) {
                    case GOVERNMENT_MEMBER:
                        GovernmentMember gm = (GovernmentMember) user;
                        if (gm.getMinistry() == Ministry.FINANCE) {
                            viewPath = "/view/FinanceGovMemberDashboardView.fxml";
                            windowTitle = "Finance Government Member Dashboard";
                        } else {
                            viewPath = "/view/GovMemberDashboardView.fxml";
                            windowTitle = "Government Member Dashboard";
                        }
                        break;

                    case CITIZEN:
                        viewPath = "/view/CitizenDashboardView.fxml";
                        windowTitle = "Citizen Dashboard";
                        System.out.println("--- [5] Μπήκαμε στο Case CITIZEN");
                        break;

                    case PRIME_MINISTER:
                        viewPath = "/view/PrimeMinisterDashboardView.fxml";
                        windowTitle = "Prime Minister Dashboard";
                        break;
                }

                System.out.println("--- [6] Path που επιλέχθηκε: " + viewPath);

                if (!viewPath.isEmpty()) {
                    System.out.println("--- [7] Καλώ την loader.load()...");
                    loader.load(viewPath, windowTitle);
                    System.out.println("--- [8] Η loader.load() τελείωσε (αν δεν δεις αυτό, έσκασε μέσα στον loader)");
                } else {
                    System.out.println("!!! ERROR: Το viewPath είναι κενό! Κανένα case δεν ταίριαξε.");
                }

            } catch (Exception e) {
                System.out.println("!!! EXCEPTION CAUGHT IN LOGIN !!!");
                e.printStackTrace();
                errorLabel.setText("Σφάλμα: Δες την κονσόλα.");
            }
        } else {
            errorLabel.setText("Λάθος στοιχεία.");
        }
    }
    /**
     * Handles showing the password in plain text when the eye icon is clicked.
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
     * Handles hiding the password and showing it as masked when the eye icon is clicked.
     */
    @FXML
    private void handleHidePassword() {
        passwordField.requestFocus(); 
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);

        Image openEye = new Image(getClass().getResourceAsStream(EYE_OPEN_PATH));
        
        eyeIconImageView.setImage(openEye);
    }
    /**
     * Handles the cancel action, clearing fields and returning to the welcome view.
     */
    @FXML
    private void handleCancel() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);

        Stage stage = (Stage) cancelButton.getScene().getWindow();
        SceneLoader loader = new SceneLoader(stage);
        
        loader.load("/resources/view/WelcomeView.fxml", "Welcome");
    }
    /**
     * Handles the back action, returning to the welcome view.
     */
    @FXML
    private void handleBack() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        SceneLoader loader = new SceneLoader(stage);
        loader.load("/view/WelcomeView.fxml", "Welcome");
    }
}
